/*
 * Copyright 2014-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.dbflute.logic.manage.freegen.table.json.engine;

import java.io.CharArrayReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import org.dbflute.exception.DfVeloScriptException;
import org.dbflute.helper.filesystem.FileTextIO;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.2.6 (2021/08/31 Tuesday at roppongi japanese)
 */
public class DfFrgVeloScriptEngine implements ScriptEngine, Invocable { // non-thread-safe

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(DfFrgVeloScriptEngine.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final ScriptEngine _realEngine; // not null
    protected final LinkedList<String> _invokingStack = new LinkedList<>(); // not null

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfFrgVeloScriptEngine(ScriptEngine realEngine) {
        _realEngine = realEngine;
    }

    // ===================================================================================
    //                                                                            Evaluate
    //                                                                            ========
    // -----------------------------------------------------
    //                                     String Expression
    //                                     -----------------
    @Override
    public Object eval(String script) throws ScriptException {
        // load() special handling for e.g. engine difference, debug logging
        final File loadedFile = handleLoadedFile(script);
        if (loadedFile != null) {
            return executeLoadingEval(loadedFile, () -> eval(adaptToReader(loadedFile, script)));
        }
        final String loadedFilePath = extractLoadedFilePath(script);
        if (loadedFilePath != null) {
            return executeLoadingEval(loadedFilePath, () -> doEval(script));
        }
        return doEval(script); // except load()
    }

    protected Object doEval(String script) throws ScriptException {
        try {
            return _realEngine.eval(script);
        } catch (ScriptException e) {
            throw prepareEvalTranslationException(script, null, e);
        }
    }

    @Override
    public Object eval(String script, Bindings bindings) throws ScriptException {
        final File loadedFile = handleLoadedFile(script);
        if (loadedFile != null) {
            return executeLoadingEval(loadedFile, () -> eval(adaptToReader(loadedFile, script), bindings));
        }
        final String loadedFilePath = extractLoadedFilePath(script);
        if (loadedFilePath != null) {
            return executeLoadingEval(loadedFilePath, () -> doEval(script, bindings));
        }
        return doEval(script, bindings);
    }

    protected Object doEval(String script, Bindings bindings) throws ScriptException {
        try {
            return _realEngine.eval(script, bindings);
        } catch (ScriptException e) {
            throw prepareEvalTranslationException(script, bindings, e);
        }
    }

    @Override
    public Object eval(String script, ScriptContext context) throws ScriptException {
        final File loadedFile = handleLoadedFile(script);
        if (loadedFile != null) {
            return executeLoadingEval(loadedFile, () -> eval(adaptToReader(loadedFile, script), context));
        }
        final String loadedFilePath = extractLoadedFilePath(script);
        if (loadedFilePath != null) {
            return executeLoadingEval(loadedFilePath, () -> doEval(script, context));
        }
        return doEval(script, context);
    }

    protected Object doEval(String script, ScriptContext context) throws ScriptException {
        try {
            return _realEngine.eval(script, context);
        } catch (ScriptException e) {
            throw prepareEvalTranslationException(script, context, e);
        }
    }

    // -----------------------------------------------------
    //                                 Reader for Expression
    //                                 ---------------------
    @Override
    public Object eval(Reader reader) throws ScriptException {
        try {
            return _realEngine.eval(reader);
        } catch (ScriptException e) {
            throw prepareEvalTranslationException(reader, null, e);
        }
    }

    @Override
    public Object eval(Reader reader, Bindings bindings) throws ScriptException {
        try {
            return _realEngine.eval(reader, bindings);
        } catch (ScriptException e) {
            throw prepareEvalTranslationException(reader, bindings, e);
        }
    }

    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        try {
            return _realEngine.eval(reader, context);
        } catch (ScriptException e) {
            throw prepareEvalTranslationException(reader, context, e);
        }
    }

    // -----------------------------------------------------
    //                                      Loading Evaluate
    //                                      ----------------
    protected Object executeLoadingEval(Object loadedFile, LoadingEvalCall evalCall) throws ScriptException {
        _log.info("...Evaluating the loaded script file: {}", loadedFile);
        Throwable cause = null;
        try {
            return evalCall.callback();
        } catch (ScriptException | RuntimeException e) {
            cause = e;
            throw e;
        } finally {
            if (cause != null) {
                _log.info("*Broken the script file loading: {}, {}", loadedFile, cause.getClass().getName());
            }
        }
    }

    public static interface LoadingEvalCall {

        Object callback() throws ScriptException;
    }

    // -----------------------------------------------------
    //                                    Exception Handling
    //                                    ------------------
    protected ScriptException prepareEvalTranslationException(Object expression, Object secondary, ScriptException cause) {
        // because JavaScript engine's message may not show script expression
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to evaluate the script.");
        br.addItem("Expression");
        br.addElement(expression); // script or reader
        br.addItem("Secondary");
        br.addElement(secondary); // null allowed
        setupInvokingStack(br);
        final String msg = br.buildExceptionMessage();
        return new DfVeloScriptException(msg, cause);
    }

    // ===================================================================================
    //                                                                     load() Adapting
    //                                                                     ===============
    // basically for rhino, nashorn's load() is used by many FreeGen modules so adapting here
    protected File handleLoadedFile(String script) {
        if (isLoadFunctionSupported()) {
            return null;
        }
        final String path = extractLoadedFilePath(script);
        return path != null ? new File(path) : null;
    }

    protected boolean isLoadFunctionSupported() {
        return isEngineSaiOrNashorn(); // load() is nashorn extension
    }

    protected Reader adaptToReader(File loadedFile, String script) {
        try {
            final FileTextIO textIO = new FileTextIO().encodeAsUTF8();
            final String filtered = textIO.readFilteringLine(new FileInputStream(loadedFile), line -> {
                return filterLoadedLine(line);
            });
            return new DfVeloScriptInputStreamReader(filtered, loadedFile);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Not found the loaded file: " + loadedFile + ", exp=" + script, e);
        }
    }

    protected String filterLoadedLine(String line) {
        final String javaTypeExp = "Java.type('java.lang.String')"; // (maybe) nashorn object
        if (line.contains(javaTypeExp)) {
            _log.debug("Filtered loaded script line for 'Java' object:");
            _log.debug("  before: {}", line.trim()); // removing indent noise
            final String classExp = "java.lang.String"; // for rhino (also nashorn can be resolved)
            line = Srl.replace(line, javaTypeExp, classExp);
            _log.debug("  after : {}", line.trim());
        }
        // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
        // #hope jflute rhino replace() problem (2021/09/02)
        //
        // The choice of Java method java.lang.String.replace
        // matching JavaScript argument types (function,string) is ambiguous;
        // candidate methods are: 
        //    class java.lang.String replace(char,char)
        //    class java.lang.String replace(java.lang.CharSequence,java.lang.CharSequence)
        //
        // _/_/_/_/_/_/_/_/_/_/
        if (line.contains(".replace(")) {
            final List<String> partList = Srl.splitList(line, ".replace(");
            final StringBuilder sb = new StringBuilder();
            boolean replaceFiltered = false;
            int index = 0;
            for (String part : partList) {
                if (index == 0) { // before first replace()
                    sb.append(part); // normal
                } else {
                    if (part.startsWith("'")) { // e.g. .replace('sea', '')
                        sb.append(".replace(").append(part); // normal
                        // determination of argument direct is too difficult
                        // (and also determination of next replace() on same-line)
                        //  e.g.
                        //   return className.replace(/(Part|Result|Model|Bean)$/, '') + 'Part';
                        //} else if (previousPart != null && previousPart.endsWith(" className")) {
                        //    sb.append(".replace(").append(part); // normal
                        //} else if (previousPart != null && previousPart.endsWith(" definitionKey")) {
                        //    sb.append(".replace(").append(part); // normal
                    } else {
                        // e.g. .replace(/^sea/, '')
                        //   or .replace(/^sea/g, '')
                        //   or .replace(this.mystic(), '')
                        String firstArg = Srl.substringFirstFront(part, ", ");
                        String nextRear = Srl.substringFirstRear(part, ", ");
                        if (firstArg.startsWith("/") && firstArg.endsWith("/")) { // e.g. .replace(/^sea/, '')
                            final String regexp = unescapeJavaScriptRegexp(Srl.unquoteAnything(firstArg, "/"));
                            sb.append(".replaceFirst("); // Java's regex as first only
                            sb.append("'").append(regexp).append("'").append(", ").append(nextRear);
                            replaceFiltered = true;
                        } else if (firstArg.startsWith("/") && firstArg.endsWith("/g")) { // e.g. .replace(/^sea/g, '')
                            final String regexp = unescapeJavaScriptRegexp(Srl.unquoteAnything(firstArg, "/", "/g"));
                            sb.append(".replaceAll("); // Java's regex as global option
                            sb.append("'").append(regexp).append("'").append(", ").append(nextRear);
                            replaceFiltered = true;
                        } else { // e.g. .replace(this.mystic(), '')
                            sb.append(".replace(").append(part);
                            // determination of next replace() on same-line is too difficult
                            //  e.g. manager.camelize(this.subPackage(api).replace(this.behaviorSubPackage(api), '').replace(/\./g, '_'))
                        }
                    }
                }
                ++index;
            }
            if (replaceFiltered) {
                _log.debug("Filtered loaded script line for replace quatation:");
                _log.debug("  before: {}", line.trim());
                line = sb.toString();
                _log.debug("  after : {}", line.trim());
            }
        }
        return line;
    }

    protected String unescapeJavaScriptRegexp(String regexp) {
        String filtered = regexp;
        filtered = Srl.replace(regexp, "\\/", "/");
        filtered = Srl.replace(regexp, "\\", "\\\\");
        return filtered;
    }

    public static class DfVeloScriptInputStreamReader extends CharArrayReader {

        protected final File _loadedFile;

        public DfVeloScriptInputStreamReader(String filtered, File loadedFile) {
            super(filtered.toCharArray());
            _loadedFile = loadedFile;
        }

        @Override
        public String toString() {
            return super.toString() + "::" + _loadedFile.getPath(); // for debug
        }
    }

    // ===================================================================================
    //                                                                            Bindings
    //                                                                            ========
    @Override
    public Bindings createBindings() {
        return _realEngine.createBindings();
    }

    @Override
    public Bindings getBindings(int scope) {
        return _realEngine.getBindings(scope);
    }

    @Override
    public void setBindings(Bindings bindings, int scope) {
        _realEngine.setBindings(bindings, scope);
    }

    @Override
    public void put(String key, Object value) {
        _realEngine.put(key, value);
    }

    @Override
    public Object get(String key) {
        return _realEngine.get(key);
    }

    // ===================================================================================
    //                                                                             Context
    //                                                                             =======
    @Override
    public ScriptContext getContext() {
        return _realEngine.getContext();
    }

    @Override
    public void setContext(ScriptContext context) {
        _realEngine.setContext(context);
    }

    // ===================================================================================
    //                                                                             Factory
    //                                                                             =======
    @Override
    public ScriptEngineFactory getFactory() {
        return _realEngine.getFactory();
    }

    // ===================================================================================
    //                                                                           Invocable
    //                                                                           =========
    // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
    // both nashorn and rhino implement this interface as ScriptEngine
    // _/_/_/_/_/_/_/_/_/_/
    // -----------------------------------------------------
    //                                         Invoke Method
    //                                         -------------
    // RemoteApiGen uses this invokeMethod() so required for compatible
    @Override
    public Object invokeMethod(Object thiz, String name, Object... args) throws ScriptException, NoSuchMethodException {
        if (!(_realEngine instanceof Invocable)) {
            throwScriptEngineNotInvocableException("invokeMethod(thiz, name, args)");
        }
        _invokingStack.push(thiz + "." + name + "()"); // without argument, too big
        try {
            return ((Invocable) _realEngine).invokeMethod(thiz, name, args);
        } catch (ScriptException | NoSuchMethodException | RuntimeException e) {
            throw prepareInvokeTranslationException(thiz, name, args, e);
        } finally {
            _invokingStack.pop();
        }
    }

    public Object invokeFunction(String name, Object... args) throws ScriptException, NoSuchMethodException {
        if (!(_realEngine instanceof Invocable)) {
            throwScriptEngineNotInvocableException("invokeFunction(name, args)");
        }
        _invokingStack.push(name + "()"); // me too
        try {
            return ((Invocable) _realEngine).invokeFunction(name, args);
        } catch (ScriptException | NoSuchMethodException | RuntimeException e) {
            throw prepareInvokeTranslationException(null, name, args, e);
        } finally {
            _invokingStack.pop();
        }
    }

    // -----------------------------------------------------
    //                                         Get Interface
    //                                         -------------
    @Override
    public <T> T getInterface(Class<T> clasz) {
        if (!(_realEngine instanceof Invocable)) {
            throwScriptEngineNotInvocableException("getInterface(clasz)");
        }
        try {
            return ((Invocable) _realEngine).getInterface(clasz);
        } catch (RuntimeException e) {
            throw prepareGetInterfaceTranslationException(null, clasz, e);
        }
    }

    @Override
    public <T> T getInterface(Object thiz, Class<T> clasz) {
        if (!(_realEngine instanceof Invocable)) {
            throwScriptEngineNotInvocableException("getInterface(thiz, clasz)");
        }
        try {
            return ((Invocable) _realEngine).getInterface(thiz, clasz);
        } catch (RuntimeException e) {
            throw prepareGetInterfaceTranslationException(thiz, clasz, e);
        }
    }

    // -----------------------------------------------------
    //                                    Exception Handling
    //                                    ------------------
    protected ScriptException prepareInvokeTranslationException(Object thiz, String name, Object[] args, Exception cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to invoke the JavaScript function.");
        br.addItem("Advice");
        br.addElement("Confirm your invokeMethod() or invokeFunction() call.");
        setupErrorSnapshotMessageIfNeeds(br);
        br.addItem("Object");
        br.addElement(thiz); // null allowed (if top-level function)
        br.addItem("Function");
        br.addElement(name);
        br.addItem("Arguments");
        br.addElement(args != null ? prepareUnwrappedArgs(args) : null);
        setupInvokingStack(br);
        final String msg = br.buildExceptionMessage();
        final DfVeloScriptException thrown = new DfVeloScriptException(msg, cause);
        showErrorSnapshotIfNeeds(thrown);
        return thrown;
    }

    protected RuntimeException prepareGetInterfaceTranslationException(Object thiz, Class<?> clasz, RuntimeException cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to get the interface.");
        br.addItem("Advice");
        br.addElement("Confirm your JavaScript engine specification.");
        setupErrorSnapshotMessageIfNeeds(br);
        br.addItem("Object");
        br.addElement(thiz); // null allowed (if top-level function)
        br.addItem("Class");
        br.addElement(clasz);
        setupInvokingStack(br);
        final String msg = br.buildExceptionMessage();
        final IllegalStateException thrown = new IllegalStateException(msg, cause);
        showErrorSnapshotIfNeeds(thrown);
        return thrown;
    }

    protected void throwScriptEngineNotInvocableException(String methodName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not implemented the invocable interface so cannot call.");
        br.addItem("Advice");
        br.addElement("Confirm your JavaScript engine specification.");
        setupErrorSnapshotMessageIfNeeds(br);
        br.addItem("Engine");
        br.addElement(_realEngine);
        br.addItem("Method");
        br.addElement(methodName);
        setupInvokingStack(br);
        final String msg = br.buildExceptionMessage();
        IllegalStateException thrown = new IllegalStateException(msg);
        showErrorSnapshotIfNeeds(thrown);
        throw thrown;
    }

    protected void setupErrorSnapshotMessageIfNeeds(ExceptionMessageBuilder br) {
        if (isShowErrorSnapshot()) {
            br.addElement("The exception chain may be stopped (by rhino).");
            br.addElement("Search by #error_snapshot for related messages.");
        }
    }

    protected void showErrorSnapshotIfNeeds(Exception thrown) {
        if (isShowErrorSnapshot()) {
            _log.warn("#error_snapshot", thrown);
        }
    }

    protected boolean isShowErrorSnapshot() {
        return isEngineRhino(); // no problem if nashorn
    }

    // ===================================================================================
    //                                                                     Whole Exception
    //                                                                     ===============
    protected void setupInvokingStack(ExceptionMessageBuilder br) {
        if (_invokingStack.isEmpty()) {
            return;
        }
        br.addItem("Invoking Stack");
        int index = 0;
        for (String invoking : prepareInvokingHierarchyList()) {
            br.addElement(Srl.indent(index * 2) + invoking);
            ++index;
        }
    }

    protected List<String> prepareInvokingHierarchyList() {
        return _invokingStack.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
    }

    // use error_snapshot instead (for rhino stop when nested invoking)
    //protected void setupNestedException(ExceptionMessageBuilder br, Exception cause) {
    //    // because e.g. rhino may ignore nested exception...? (actually shadowed)
    //    br.addItem("Nested Exception");
    //    br.addElement(cause.getClass());
    //    br.addElement(cause.getMessage());
    //    final Throwable nestedCause = cause.getCause();
    //    if (nestedCause != null) {
    //        br.addItem("Nested Nested Exception");
    //        br.addElement(nestedCause.getClass());
    //        br.addElement(nestedCause.getMessage());
    //    }
    //}

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected boolean isEngineSaiOrNashorn() { // best effort logic
        return Srl.startsWithIgnoreCase(_realEngine.getClass().getSimpleName(), "sai", "nashorn");
    }

    protected boolean isEngineRhino() { // me too
        return Srl.startsWithIgnoreCase(_realEngine.getClass().getSimpleName(), "rhino");
    }

    protected List<Object> prepareUnwrappedArgs(Object[] args) {
        // #hope jflute unwrap engine internal object (2021/09/01)
        return Arrays.asList(args);
    }

    protected String extractLoadedFilePath(String script) {
        if (!script.contains("load(")) {
            return null;
        }
        final String trimmed = script.trim();
        final String path;
        if (trimmed.startsWith("load(\"") && (trimmed.endsWith("\")") || trimmed.endsWith("\");"))) { // DQ
            path = Srl.extractScopeFirst(trimmed, "load(\"", "\")").getContent(); // DQ
        } else if (trimmed.startsWith("load('") && (trimmed.endsWith("')") || trimmed.endsWith("');"))) { // SQ
            path = Srl.extractScopeFirst(trimmed, "load('", "')").getContent(); // SQ
        } else { // cannot parse
            path = null;
        }
        return path;
    }
}
