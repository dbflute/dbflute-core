/*
 * Copyright 2014-2021 the original author or authors.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.2.6 (2021/08/31 at roppongi japanese)
 */
public class DfFrgVeloScriptEngine implements ScriptEngine {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(DfFrgVeloScriptEngine.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final ScriptEngine _realEngine; // not null

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
        final File loadedFile = extractLoadedFile(script);
        if (loadedFile != null) {
            return eval(adaptToReader(loadedFile, script));
        }
        try {
            return _realEngine.eval(script);
        } catch (ScriptException e) {
            throw prepareTranslationException(script, null, e);
        }
    }

    @Override
    public Object eval(String script, Bindings bindings) throws ScriptException {
        final File loadedFile = extractLoadedFile(script);
        if (loadedFile != null) {
            return eval(adaptToReader(loadedFile, script), bindings);
        }
        try {
            return _realEngine.eval(script, bindings);
        } catch (ScriptException e) {
            throw prepareTranslationException(script, bindings, e);
        }
    }

    @Override
    public Object eval(String script, ScriptContext context) throws ScriptException {
        final File loadedFile = extractLoadedFile(script);
        if (loadedFile != null) {
            return eval(adaptToReader(loadedFile, script), context);
        }
        try {
            return _realEngine.eval(script, context);
        } catch (ScriptException e) {
            throw prepareTranslationException(script, context, e);
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
            throw prepareTranslationException(reader, null, e);
        }
    }

    @Override
    public Object eval(Reader reader, Bindings bindings) throws ScriptException {
        try {
            return _realEngine.eval(reader, bindings);
        } catch (ScriptException e) {
            throw prepareTranslationException(reader, bindings, e);
        }
    }

    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        try {
            return _realEngine.eval(reader, context);
        } catch (ScriptException e) {
            throw prepareTranslationException(reader, context, e);
        }
    }

    // -----------------------------------------------------
    //                                 Translation Exception
    //                                 ---------------------
    protected ScriptException prepareTranslationException(Object expression, Object secondary, ScriptException e) {
        // because JavaScript engine's message may not show script expression
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to evaluate the script.");
        br.addItem("Expression");
        br.addElement(expression); // script or reader
        br.addItem("Secondary");
        br.addElement(secondary); // null allowed
        final String msg = br.buildExceptionMessage();
        final ScriptException scEx = new ScriptException(msg);
        scEx.initCause(e);
        return scEx;
    }

    // ===================================================================================
    //                                                                     load() Adapting
    //                                                                     ===============
    // basically for rhino, nashorn's load() is used by many FreeGen modules so adapting here
    protected File extractLoadedFile(String script) {
        if (isLoadFunctionSupported()) {
            return null;
        }
        final String trimmed = script.trim();
        final String path;
        if (trimmed.startsWith("load(\"") && (trimmed.endsWith("\")") || trimmed.endsWith("\");"))) { // DQ
            path = Srl.extractScopeFirst(trimmed, "load(\"", "\")").getContent(); // DQ
        } else if (trimmed.startsWith("load('") && (trimmed.endsWith("')") || trimmed.endsWith("');"))) { // SQ
            path = Srl.extractScopeFirst(trimmed, "load('", "')").getContent(); // SQ
        } else {
            path = null;
        }
        return path != null ? new File(path) : null;
    }

    protected boolean isLoadFunctionSupported() { // best effort logic
        // load() is nashorn extension (and sai is forked from nashorn)
        return Srl.startsWithIgnoreCase(_realEngine.getClass().getSimpleName(), "sai", "nashorn");
    }

    protected InputStreamReader adaptToReader(File loadedFile, String script) {
        final String encoding = "UTF-8";
        InputStreamReader reader = null;
        try {
            _log.info("...Adapting load() to reader: exp={} loadedFile={}", script, loadedFile);
            reader = new InputStreamReader(new FileInputStream(loadedFile), encoding);
        } catch (UnsupportedEncodingException e) { // no way
            throw new IllegalStateException("Unknown encoding: " + encoding + ", exp=" + script, e);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Not found the loaded file: " + loadedFile + ", exp=" + script, e);
        }
        return reader;
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
}
