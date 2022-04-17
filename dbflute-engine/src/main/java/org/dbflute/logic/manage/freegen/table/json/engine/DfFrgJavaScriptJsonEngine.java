/*
 * Copyright 2014-2022 the original author or authors.
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

import org.dbflute.DfBuildProperties;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.properties.DfLittleAdjustmentProperties;
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
        final ScriptEngine engine = prepareScriptEngine(manager, requestName, resourceFile);
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
            throwJsonParseFailureException(requestName, resourceFile, engine, e);
        }
        @SuppressWarnings("unchecked")
        final RESULT result = (RESULT) engine.get("result");
        return filterJavaScriptObject(result);
    }

    // -----------------------------------------------------
    //                                         Script Engine
    //                                         -------------
    protected ScriptEngine prepareScriptEngine(ScriptEngineManager manager, String requestName, String resourceFile) {
        if (!_foundLoggingDone) {
            _log.info("...Finding the script engine for FreeGen");
        }
        // same as Lasta Di logic (2021/08/31)
        final ScriptEngineFound engine = findScriptEngine(manager);
        if (engine == null) {
            throwJsonScriptEngineNotFoundException(requestName, resourceFile);
        }
        if (!_foundLoggingDone) {
            _log.info(" -> found the script engine as '{}'", engine.getEngineName());
            _foundLoggingDone = true;
        }
        return engine.getFoundEngine();
    }

    protected void throwJsonScriptEngineNotFoundException(String requestName, String resourceFile) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the JSON engine for FreeGen.");
        br.addItem("Advice");
        br.addElement("Your FreeGen request needs JavaScript engine.");
        br.addElement("But Nashorn (JavaScript engine) is removed since Java15.");
        br.addElement("");
        br.addElement("So prepare 'sai' libraries in your 'extlib' directory.");
        br.addElement("");
        br.addElement("You can download automatically by DBFlute 'sai' task like this:");
        br.addElement(" 1. execute manage.sh|bat");
        br.addElement(" 2. select 31 (sai task)");
        br.addElement("   => downloading sai libraries to 'extlib'");
        br.addElement(" 3. retry your FreeGen task");
        br.addElement("   => you can use JavaScript expression in FreeGen");
        br.addItem("FreeGen Request");
        br.addElement(requestName);
        br.addItem("JSON File");
        br.addElement(resourceFile);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }

    // -----------------------------------------------------
    //                                             Filtering
    //                                             ---------
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

    // -----------------------------------------------------
    //                                         Parse Failure
    //                                         -------------
    protected void throwJsonParseFailureException(String requestName, String resourceFile, ScriptEngine engine, Exception cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to parse the JSON file for FreeGen.");
        br.addItem("FreeGen Request");
        br.addElement(requestName);
        br.addItem("JSON File");
        br.addElement(resourceFile);
        br.addItem("JSON Engine");
        br.addElement(engine);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg, cause);
    }

    // ===================================================================================
    //                                                                  Find Script Engine
    //                                                                  ==================
    // called by e.g. DfFreeGenManager
    public ScriptEngineFound findScriptEngine(ScriptEngineManager manager) { // null allowed
        if (getLittleAdjustmentProperties().isFreeGenJavaScriptEngineNashorn()) {
            return getEngineByName(manager, "nashorn"); // for emergency debug
        }
        // name "javascript" may be conflicted if two engines exist in same JavaVM by jflute (2021/09/01)
        // rhino might be best at the future but sai has the best compatibility so first is sai
        ScriptEngineFound engine = getEngineByName(manager, "sai"); // forked from nashorn, since Java11
        if (engine == null) {
            engine = getEngineByName(manager, "rhino"); // also can use Java8
        }
        if (engine == null) {
            engine = getEngineByName(manager, "nashorn"); // embedded until Java14
        }
        if (engine == null) {
            engine = getEngineByName(manager, "javascript"); // may be conflicted so last
        }
        return engine;
    }

    protected ScriptEngineFound getEngineByName(ScriptEngineManager manager, String engineName) {
        final ScriptEngine engine = manager.getEngineByName(engineName);
        return engine != null ? new ScriptEngineFound(engineName, engine) : null;
    }

    public static class ScriptEngineFound { // to keep engine name for e.g. logging

        protected final String engineName; // not null
        protected final ScriptEngine foundEngine; // not null

        public ScriptEngineFound(String engineName, ScriptEngine foundEngine) {
            this.engineName = engineName;
            this.foundEngine = foundEngine;
        }

        @Override
        public String toString() {
            return "{" + engineName + ", " + foundEngine + "}";
        }

        public String getEngineName() {
            return engineName;
        }

        public ScriptEngine getFoundEngine() {
            return foundEngine;
        }
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfLittleAdjustmentProperties getLittleAdjustmentProperties() {
        return getProperties().getLittleAdjustmentProperties();
    }

    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }
}
