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

import java.io.Reader;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import org.dbflute.helper.message.ExceptionMessageBuilder;

/**
 * @author jflute
 * @since 1.2.6 (2021/08/31 at roppongi japanese)
 */
public class DfVeloworldScriptEngine implements ScriptEngine {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final ScriptEngine _realEngine; // not null

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfVeloworldScriptEngine(ScriptEngine realEngine) {
        _realEngine = realEngine;
    }

    // ===================================================================================
    //                                                                            Evaluate
    //                                                                            ========
    @Override
    public Object eval(String script) throws ScriptException {
        try {
            return _realEngine.eval(script);
        } catch (ScriptException e) {
            throw prepareTranslationException(script, null, e);
        }
    }

    @Override
    public Object eval(String script, Bindings n) throws ScriptException {
        try {
            return _realEngine.eval(script, n);
        } catch (ScriptException e) {
            throw prepareTranslationException(script, n, e);
        }
    }

    @Override
    public Object eval(String script, ScriptContext context) throws ScriptException {
        try {
            return _realEngine.eval(script, context);
        } catch (ScriptException e) {
            throw prepareTranslationException(script, context, e);
        }
    }

    @Override
    public Object eval(Reader reader) throws ScriptException {
        try {
            return _realEngine.eval(reader);
        } catch (ScriptException e) {
            throw prepareTranslationException(reader, null, e);
        }
    }

    @Override
    public Object eval(Reader reader, Bindings n) throws ScriptException {
        try {
            return _realEngine.eval(reader, n);
        } catch (ScriptException e) {
            throw prepareTranslationException(reader, null, e);
        }
    }

    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        try {
            return _realEngine.eval(reader, context);
        } catch (ScriptException e) {
            throw prepareTranslationException(reader, null, e);
        }
    }

    protected ScriptException prepareTranslationException(Object expression, Object secondary, ScriptException e) {
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
