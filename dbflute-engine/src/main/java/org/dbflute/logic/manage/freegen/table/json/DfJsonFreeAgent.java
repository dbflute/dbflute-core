/*
 * Copyright 2014-2015 the original author or authors.
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
package org.dbflute.logic.manage.freegen.table.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.manage.freegen.DfFreeGenResource;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @author p1us2er0
 */
public class DfJsonFreeAgent {

    // ===================================================================================
    //                                                                         Decode JSON
    //                                                                         ===========
    public <RESULT> RESULT decodeJsonMap(String requestName, String resourceFile) throws DfJsonUrlCannotRequestException {
        final String json;
        if (resourceFile.startsWith("http://")) { // JSON response
            json = requestJsonResponse(resourceFile);
        } else { // relative path to local file
            try (Scanner scanner = new Scanner(Paths.get(resourceFile))) {
                json = scanner.useDelimiter("\\Z").next();
            } catch (NoSuchFileException e) {
                throwJsonFileNotFoundException(requestName, resourceFile, e);
                return null; // unreachable
            } catch (IOException e) {
                throwJsonFileCannotAccessException(requestName, resourceFile, e);
                return null; // unreachable
            }
        }
        return fromJson(requestName, resourceFile, json);
    }

    protected <RESULT> RESULT fromJson(String requestName, String resourceFile, String json) {
        final ScriptEngineManager manager = new ScriptEngineManager();
        final ScriptEngine engine = manager.getEngineByName("javascript");
        try {
            final String realExp;
            if (json.startsWith("{") || json.startsWith("[")) { // map, list style
                realExp = json;
            } else { // map omitted?
                realExp = "{" + json + "}";
            }
            engine.eval("var result = " + realExp);
        } catch (ScriptException e) {
            throwJsonParseFailureException(requestName, resourceFile, e);
        }
        @SuppressWarnings("unchecked")
        final RESULT result = (RESULT) engine.get("result");
        return filterJavaScriptObject(result);
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

    protected void throwJsonFileNotFoundException(String requestName, String resourceFile, IOException cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the JSON file for FreeGen.");
        br.addItem("FreeGen Request");
        br.addElement(requestName);
        br.addItem("JSON File");
        br.addElement(resourceFile);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg, cause);
    }

    protected void throwJsonFileCannotAccessException(String requestName, String resourceFile, IOException cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Cannot access the JSON file for FreeGen.");
        br.addItem("FreeGen Request");
        br.addElement(requestName);
        br.addItem("JSON File");
        br.addElement(resourceFile);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg, cause);
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

    // -----------------------------------------------------
    //                                           Request URL
    //                                           -----------
    protected String requestJsonResponse(String resourceFile) {
        BufferedReader reader = null;
        try {
            final URL url = new URL(resourceFile);
            final URLConnection uc = url.openConnection();
            final InputStream is = uc.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is));
            final StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            throw new DfJsonUrlCannotRequestException("Failed to access to the URL: " + resourceFile, e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {}
            }
        }
    }

    public static class DfJsonUrlCannotRequestException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public DfJsonUrlCannotRequestException(String msg) {
            super(msg);
        }

        public DfJsonUrlCannotRequestException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }

    // ===================================================================================
    //                                                                       Trace KeyList
    //                                                                       =============
    public List<String> traceKeyList(String requestName, DfFreeGenResource resource, Map<String, Object> rootMap, String keyPath,
            List<String> pathList) {
        // e.g.
        //  keyPath = categories -> map.keys
        //  keyPath = categories -> map.values -> list.elements
        //  keyPath = categories -> map.values -> list.map.foo
        //  keyPath = categories -> map.foo -> map.keys
        List<String> keyList = null;
        Object current = null;
        for (String pathElement : pathList) {
            if (current == null) {
                current = rootMap.get(pathElement);
                if (current == null) {
                    throwRootMapKeyNotFoundException(requestName, resource, keyPath, pathElement);
                }
                continue;
            }
            if (pathElement.startsWith("map.")) {
                if (!(current instanceof Map<?, ?>)) {
                    if (current instanceof List<?> && ((List<?>) current).isEmpty()) {
                        current = DfCollectionUtil.emptyMap(); // if 'emptyKey: {}', empty List...
                    } else {
                        throwKeyPathExpectedMapButNotMapException(requestName, resource, keyPath, pathElement, current);
                    }
                }
                @SuppressWarnings("unchecked")
                final Map<String, Object> currentMap = (Map<String, Object>) current;
                if (pathElement.equals("map.keys")) { // found
                    keyList = new ArrayList<String>(currentMap.keySet());
                    break;
                } else if (pathElement.equals("map.values")) {
                    current = new ArrayList<Object>(currentMap.values());
                    continue;
                } else {
                    final String nextKey = Srl.substringFirstRear(pathElement, "map.");
                    current = currentMap.get(nextKey);
                    continue;
                }
            } else if (pathElement.startsWith("list.")) {
                if (!(current instanceof List<?>)) {
                    throwKeyPathExpectedListButNotListException(requestName, resource, keyPath, pathElement, current);
                }
                @SuppressWarnings("unchecked")
                final List<Object> currentList = (List<Object>) current;
                if (pathElement.equals("list.elements")) { // found
                    keyList = new ArrayList<String>();
                    for (Object element : currentList) {
                        if (!(element instanceof String)) {
                            throwKeyPathExpectedStringListButNotStringException(requestName, resource, keyPath, pathElement, currentList,
                                    element);
                        }
                        keyList.add((String) element);
                    }
                    break;
                } else if (pathElement.startsWith("list.map.")) { // found
                    final String elementKey = Srl.substringFirstRear(pathElement, "list.map.");
                    keyList = new ArrayList<String>();
                    for (Object element : currentList) {
                        if (!(element instanceof Map<?, ?>)) {
                            throwKeyPathExpectedMapListButNotMapException(requestName, resource, keyPath, pathElement, currentList, element);
                        }
                        @SuppressWarnings("unchecked")
                        final Map<String, Object> elementMap = (Map<String, Object>) element;
                        final String elementValue = (String) elementMap.get(elementKey);
                        if (elementValue != null) {
                            keyList.add(elementValue);
                        }
                    }
                    break;
                } else {
                    throwIllegalKeyPathElementException(requestName, resource, keyPath, pathElement);
                }
            } else {
                throwIllegalKeyPathElementException(requestName, resource, keyPath, pathElement);
            }
        }
        if (keyList == null) {
            String msg = "Not found the keys: keyPath=" + keyPath;
            throw new DfIllegalPropertySettingException(msg);
        }
        return keyList;
    }

    protected void throwRootMapKeyNotFoundException(String requestName, DfFreeGenResource resource, String keyPath, String rootMapKey) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the key in the root map. (FreeGen)");
        br.addItem("Request Name");
        br.addElement(requestName);
        br.addItem("JSON File");
        br.addElement(resource.getResourceFile());
        br.addItem("keyPath");
        br.addElement(keyPath);
        br.addItem("RootMap Key");
        br.addElement(rootMapKey);
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    protected void throwKeyPathExpectedMapButNotMapException(String requestName, DfFreeGenResource resource, String keyPath,
            String targetPath, Object current) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The key path expects map type but not map. (FreeGen)");
        br.addItem("Request Name");
        br.addElement(requestName);
        br.addItem("JSON File");
        br.addElement(resource.getResourceFile());
        br.addItem("keyPath");
        br.addElement(keyPath);
        br.addItem("Target Path Element");
        br.addElement(targetPath);
        br.addItem("Actual Object");
        br.addElement(current != null ? current.getClass().getName() : null);
        br.addElement(current);
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    protected void throwKeyPathExpectedListButNotListException(String requestName, DfFreeGenResource resource, String keyPath,
            String targetPath, Object current) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The key path expects list type but not list. (FreeGen)");
        br.addItem("Request Name");
        br.addElement(requestName);
        br.addItem("JSON File");
        br.addElement(resource.getResourceFile());
        br.addItem("keyPath");
        br.addElement(keyPath);
        br.addItem("Target Path Element");
        br.addElement(targetPath);
        br.addItem("Actual Object");
        br.addElement(current != null ? current.getClass().getName() : null);
        br.addElement(current);
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    protected void throwKeyPathExpectedStringListButNotStringException(String requestName, DfFreeGenResource resource, String keyPath,
            String targetPath, List<Object> currentList, Object element) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The key path expects string type in list but not string. (FreeGen)");
        br.addItem("Request Name");
        br.addElement(requestName);
        br.addItem("JSON File");
        br.addElement(resource.getResourceFile());
        br.addItem("keyPath");
        br.addElement(keyPath);
        br.addItem("Target Path Element");
        br.addElement(targetPath);
        br.addItem("List Object");
        br.addElement(currentList != null ? currentList.getClass().getName() : null);
        br.addElement(currentList);
        br.addItem("Actual Element");
        br.addElement(element != null ? element.getClass().getName() : null);
        br.addElement(element);
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    protected void throwKeyPathExpectedMapListButNotMapException(String requestName, DfFreeGenResource resource, String keyPath,
            String targetPath, List<Object> currentList, Object element) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The key path expects string type in list but not string. (FreeGen)");
        br.addItem("Request Name");
        br.addElement(requestName);
        br.addItem("JSON File");
        br.addElement(resource.getResourceFile());
        br.addItem("keyPath");
        br.addElement(keyPath);
        br.addItem("Target Path Element");
        br.addElement(targetPath);
        br.addItem("List Object");
        br.addElement(currentList != null ? currentList.getClass().getName() : null);
        br.addElement(currentList);
        br.addItem("Actual Element");
        br.addElement(element != null ? element.getClass().getName() : null);
        br.addElement(element);
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    protected void throwIllegalKeyPathElementException(String requestName, DfFreeGenResource resource, String keyPath,
            String illegalPathElement) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Illegal key path was found. (FreeGen)");
        br.addItem("Request Name");
        br.addElement(requestName);
        br.addItem("JSON File");
        br.addElement(resource.getResourceFile());
        br.addItem("keyPath");
        br.addElement(keyPath);
        br.addItem("Illegal Path Element");
        br.addElement(illegalPathElement);
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    // ===================================================================================
    //                                                                           Trace Map
    //                                                                           =========
    public Map<String, Object> traceMap(String requestName, DfFreeGenResource resource, Map<String, Object> rootMap, String tracePath) {
        // e.g.
        //  jsonPath = map
        //  jsonPath = tables -> map
        //  jsonPath = schema -> tables -> map
        final List<String> pathList = Srl.splitListTrimmed(tracePath, "->");
        Map<String, Object> currentMap = rootMap;
        for (String pathElement : pathList) {
            if ("map".equals(pathElement)) {
                break;
            }
            final Object obj = currentMap.get(pathElement);
            if (obj == null) {
                throwJsonMapKeyNotFoundException(requestName, resource, tracePath, currentMap, pathElement);
            }
            if (!(obj instanceof Map<?, ?>)) {
                throwJsonTracePathNotMapException(requestName, resource, tracePath, pathElement, obj);
            }
            @SuppressWarnings("unchecked")
            final Map<String, Object> nextMap = (Map<String, Object>) obj;
            currentMap = nextMap;
        }
        return currentMap;
    }

    protected void throwJsonMapKeyNotFoundException(String requestName, DfFreeGenResource resource, String tracePath,
            Map<String, Object> currentMap, String pathElement) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the key in the map. (FreeGen)");
        br.addItem("Request Name");
        br.addElement(requestName);
        br.addItem("JSON File");
        br.addElement(resource.getResourceFile());
        br.addItem("Trace Path");
        br.addElement(tracePath);
        br.addItem("Current Map keySet()");
        br.addElement(currentMap.keySet());
        br.addItem("Path Element");
        br.addElement(pathElement);
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    protected void throwJsonTracePathNotMapException(String requestName, DfFreeGenResource resource, String tracePath, String pathElement,
            Object current) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The trace path expects map type but not map. (FreeGen)");
        br.addItem("Request Name");
        br.addElement(requestName);
        br.addItem("JSON File");
        br.addElement(resource.getResourceFile());
        br.addItem("Trace Path");
        br.addElement(tracePath);
        br.addItem("Path Element");
        br.addElement(pathElement);
        br.addItem("Actual Object");
        br.addElement(current != null ? current.getClass().getName() : null);
        br.addElement(current);
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }
}
