/*
 * Copyright 2014-2020 the original author or authors.
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

import java.lang.reflect.Method;
import java.util.Map;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.util.DfReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// tested in lastaflute-test-fortress whose runtime has Gson
/**
 * @author jflute
 * @since 1.2.4 (2020/12/31 at roppongi japanese)
 */
public class DfFrgGsonJsonEngine {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(DfFrgGsonJsonEngine.class);

    protected static Class<?> _gsonType;
    protected static Object _gsonObj;
    protected static boolean _typeInitialized;

    protected static Method _fromJsonMethod;
    protected static boolean _methodInitialized;

    // ===================================================================================
    //                                                                          Initialize
    //                                                                          ==========
    protected void initializeFromJsonMethodIfNeeds() {
        synchronized (DfFrgGsonJsonEngine.class) {
            if (_methodInitialized || _fromJsonMethod != null) {
                return;
            }
            initializeGsonTypeIfNeeds();
            if (_gsonType == null) { // means no Gson jar file in 'extlib' directory
                return;
            }
            _log.info("...Finding method of Gson for FreeGen");

            // might return null if method signature is changed (however basically no way)
            final String methodName = "fromJson";
            final Class<?>[] argTypes = new Class<?>[] { String.class, Class.class };
            _fromJsonMethod = DfReflectionUtil.getPublicMethod(_gsonType, methodName, argTypes);
            if (_fromJsonMethod != null) {
                _log.info(" -> found the {}() method of Gson: {}", methodName, _fromJsonMethod);
            } else { // basically no way, Gson may be changed
                _log.warn(" -> *not found the {}() method of Gson in spite of having Gson type: {}", methodName, _gsonType);
            }
            _methodInitialized = true;
        }
    }

    protected void initializeGsonTypeIfNeeds() {
        synchronized (DfFrgGsonJsonEngine.class) {
            if (_typeInitialized || _gsonType != null) {
                return;
            }
            final String gsonFqcn = "com.google.gson.Gson";
            try {
                _log.info("...Finding Gson type for FreeGen");

                // DfReflectionUtil uses context class loader of current thread so cannot find it
                //_gsonType = DfReflectionUtil.forName(gsonFqcn); // not null
                _gsonType = Class.forName(gsonFqcn); // not null

                _log.info(" -> found the Gson type: {}", _gsonType);
            } catch (ClassNotFoundException ignored) { // e.g. no jar file in 'extlib'
                _log.info(" -> not found the Gson type: {}", gsonFqcn);
            }
            if (_gsonType != null) {
                try {
                    _gsonObj = DfReflectionUtil.newInstance(_gsonType);
                } catch (RuntimeException continued) { // basically no way
                    _log.warn(" -> *cannot instantiate the Gson type: {}", _gsonType, continued);
                    _gsonType = null; // treated as not found
                }
            }
            _typeInitialized = true;
        }
    }

    // ===================================================================================
    //                                                                           from JSON
    //                                                                           =========
    public boolean determineFromJsonAvailable() {
        initializeFromJsonMethodIfNeeds();
        return _fromJsonMethod != null;
    }

    public <RESULT> RESULT fromJson(String requestName, String resourceFile, String json) {
        if (_fromJsonMethod == null) { // basically no way
            throw new IllegalStateException("Not found the fromJsonMethod, call isFromJsonAvailable() before.");
        }
        final Object[] args = new Object[] { json, Map.class };
        try {
            @SuppressWarnings("unchecked")
            final RESULT result = (RESULT) DfReflectionUtil.invoke(_fromJsonMethod, _gsonObj, args);
            return result;
        } catch (RuntimeException e) {
            throwJsonParseFailureException(requestName, resourceFile, json, e);
            return null; // unreachable
        }
    }

    protected void throwJsonParseFailureException(String requestName, String resourceFile, String json, Exception cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to parse the JSON file by Gson for FreeGen.");
        br.addItem("FreeGen Request");
        br.addElement(requestName);
        br.addItem("JSON File");
        br.addElement(resourceFile);
        br.addItem("Gson Method");
        br.addElement(_fromJsonMethod);
        br.addItem("Parsed JSON");
        br.addElement(json);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg, cause);
    }
}
