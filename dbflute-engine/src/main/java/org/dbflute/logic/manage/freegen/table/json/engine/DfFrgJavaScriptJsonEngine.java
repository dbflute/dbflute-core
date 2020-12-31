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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.2.4 (2020/12/31 at roppongi japanese)
 */
public class DfFrgJavaScriptJsonEngine {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(DfFrgJavaScriptJsonEngine.class);

    private static volatile boolean _foundLoggingDone;

    // ===================================================================================
    //                                                                           from JSON
    //                                                                           =========
    public <RESULT> RESULT fromJson(String requestName, String resourceFile, String json) {
        return doFromJsonByScriptEngine(requestName, resourceFile, json);
    }

    protected <RESULT> RESULT doFromJsonByScriptEngine(String requestName, String resourceFile, String json) {
        // should use Ant class loader to find 'sai' engine in 'extlib' directory
        // because default constructor of the manager uses only System class loader
        // (also DfFreeGenManager)
        final ScriptEngineManager manager = new ScriptEngineManager(getClass().getClassLoader());
        final ScriptEngine engine = findScriptEngine(manager, requestName, resourceFile);
        try {
            // move to caller to share between engines
            //final String realExp;
            //if (json.startsWith("{") || json.startsWith("[")) { // map, list style
            //    realExp = json;
            //} else { // map omitted?
            //    realExp = "{" + json + "}";
            //}
            engine.eval("var result = " + json);
        } catch (ScriptException e) {
            throwJsonParseFailureException(requestName, resourceFile, e);
        }
        @SuppressWarnings("unchecked")
        final RESULT result = (RESULT) engine.get("result");
        return filterJavaScriptObject(result);
    }

    protected ScriptEngine findScriptEngine(ScriptEngineManager manager, String requestName, String resourceFile) {
        if (!_foundLoggingDone) {
            _log.info("...Finding the script engine for FreeGen");
        }
        final String saiKeyword = "sai";
        ScriptEngine engine = manager.getEngineByName(saiKeyword);
        if (engine != null) {
            if (!_foundLoggingDone) {
                _log.info(" -> found the script engine as '{}'", saiKeyword);
                _foundLoggingDone = true;
            }
        } else {
            final String nashornKeyword = "javascript";
            engine = manager.getEngineByName(nashornKeyword);
            if (engine != null) {
                if (!_foundLoggingDone) {
                    _log.info(" -> found the script engine as '{}'", nashornKeyword);
                    _foundLoggingDone = true;
                }
            } else {
                throwJsonScriptEngineNotFoundException(requestName, resourceFile);
            }
        }
        return engine;
    }

    @SuppressWarnings("unchecked")
    protected <RESULT> RESULT filterJavaScriptObject(RESULT result) {
        if (result instanceof List<?>) {
            final List<Object> srcList = (List<Object>) result;
            final List<Object> destList = new ArrayList<Object>(srcList.size());
            for (Object element : srcList) {
                destList.add(filterJavaScriptObject(element));
            }
            return (RESULT) destList;
        } else if (result instanceof Map<?, ?>) {
            final Map<Object, Object> srcMap = (Map<Object, Object>) result;
            final List<Object> challengedList = challengeList(srcMap);
            if (challengedList != null) {
                return (RESULT) filterJavaScriptObject(challengedList);
            } else {
                final Map<Object, Object> destMap = new LinkedHashMap<Object, Object>(srcMap.size());
                for (Entry<Object, Object> entry : srcMap.entrySet()) {
                    destMap.put(entry.getKey(), filterJavaScriptObject(entry.getValue()));
                }
                return (RESULT) destMap;
            }
        } else {
            return result;
        }
    }

    protected List<Object> challengeList(Map<Object, Object> map) {
        int index = 0;
        final Set<Object> keySet = map.keySet();
        for (Object key : keySet) {
            final String strKey = key.toString();
            if (Srl.isNumberHarfAll(strKey) && Integer.parseInt(strKey) == index) {
                ++index;
                continue;
            }
            return null;
        }
        return new ArrayList<Object>(map.values());
    }

    protected void throwJsonScriptEngineNotFoundException(String requestName, String resourceFile) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the JSON script engine for FreeGen.");
        br.addItem("Advice");
        br.addElement("Nashorn (JavaScript engine) is removed since Java15.");
        br.addElement("");
        br.addElement("You can use 'sai' instead of Nashorn.");
        br.addElement(" https://github.com/codelibs/sai");
        br.addElement("");
        br.addElement("Or Google Gson.");
        br.addElement(" https://github.com/google/gson");
        br.addElement("");
        br.addElement("Put the jar files (including dependencies)");
        br.addElement("on 'extlib' directory of your DBFlute client.");
        br.addItem("FreeGen Request");
        br.addElement(requestName);
        br.addItem("JSON File");
        br.addElement(resourceFile);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }

    protected void throwJsonParseFailureException(String requestName, String resourceFile, Exception cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to parse the JSON file for FreeGen.");
        br.addItem("FreeGen Request");
        br.addElement(requestName);
        br.addItem("JSON File");
        br.addElement(resourceFile);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg, cause);
    }
}
