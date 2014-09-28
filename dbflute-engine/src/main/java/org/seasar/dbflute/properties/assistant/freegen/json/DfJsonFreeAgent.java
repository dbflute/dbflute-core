/*
 * Copyright 2004-2014 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.properties.assistant.freegen.json;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.seasar.dbflute.exception.DfIllegalPropertySettingException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.properties.assistant.freegen.DfFreeGenResource;
import org.seasar.dbflute.util.DfReflectionUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfJsonFreeAgent {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String JSON_DECODER_NAME = "net.arnx.jsonic.JSON";

    // ===================================================================================
    //                                                                         Decode JSON
    //                                                                         ===========
    public Map<String, Object> decodeJsonMap(String requestName, String resourceFile) {
        final String decoderName = JSON_DECODER_NAME;
        final Class<?> jsonType;
        try {
            jsonType = Class.forName(decoderName);
        } catch (ClassNotFoundException e) {
            throwJsonDecoderNotFoundException(requestName, resourceFile, decoderName, e);
            return null; // unreachable
        }
        final String decodeMethodName = "decode";
        final Class<?>[] argTypes = new Class<?>[] { InputStream.class };
        final Method decodeMethod = DfReflectionUtil.getPublicMethod(jsonType, decodeMethodName, argTypes);
        FileInputStream ins = null;
        final Object decodedObj;
        try {
            ins = new FileInputStream(new File(resourceFile));
            decodedObj = DfReflectionUtil.invokeStatic(decodeMethod, new Object[] { ins });
        } catch (FileNotFoundException e) {
            throwJsonFileNotFoundException(e, resourceFile);
            return null; // unreachable
        } finally {
            if (ins != null) {
                try {
                    ins.close();
                } catch (IOException ignored) {
                }
            }
        }
        @SuppressWarnings("unchecked")
        final Map<String, Object> rootMap = (Map<String, Object>) decodedObj;
        return rootMap;
    }

    protected void throwJsonDecoderNotFoundException(String requestName, String resourceFile, String decoderName,
            ClassNotFoundException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the JSON decoder for FreeGen.");
        br.addItem("Advice");
        br.addElement("You should put the JSONIC jar file to the 'extlib' directory");
        br.addElement("on DBFlute client when you use JSON handling of FreeGen.");
        br.addElement("For example:");
        br.addElement("  {DBFluteClient}");
        br.addElement("    |-dfprop");
        br.addElement("    |-extlib");
        br.addElement("    |  |-jsonic-1.2.5.jar");
        br.addElement("    |-...");
        br.addItem("Request Name");
        br.addElement(requestName);
        br.addItem("Resource File");
        br.addElement(resourceFile);
        br.addItem("Decoder Name");
        br.addElement(decoderName);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg, e);
    }

    protected void throwJsonFileNotFoundException(FileNotFoundException e, String resourceFile) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the JSON file for FreeGen.");
        br.addItem("JSON File");
        br.addElement(resourceFile);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg, e);
    }

    // ===================================================================================
    //                                                                       Trace KeyList
    //                                                                       =============
    public List<String> traceKeyList(String requestName, DfFreeGenResource resource, Map<String, Object> rootMap,
            String keyPath, List<String> pathList) {
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
                    throwKeyPathExpectedMapButNotMapException(requestName, resource, keyPath, pathElement, current);
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
                            throwKeyPathExpectedStringListButNotStringException(requestName, resource, keyPath,
                                    pathElement, currentList, element);
                        }
                        keyList.add((String) element);
                    }
                    break;
                } else if (pathElement.startsWith("list.map.")) { // found
                    final String elementKey = Srl.substringFirstRear(pathElement, "list.map.");
                    keyList = new ArrayList<String>();
                    for (Object element : currentList) {
                        if (!(element instanceof Map<?, ?>)) {
                            throwKeyPathExpectedMapListButNotMapException(requestName, resource, keyPath, pathElement,
                                    currentList, element);
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

    protected void throwRootMapKeyNotFoundException(String requestName, DfFreeGenResource resource, String keyPath,
            String rootMapKey) {
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

    protected void throwKeyPathExpectedMapButNotMapException(String requestName, DfFreeGenResource resource,
            String keyPath, String targetPath, Object current) {
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

    protected void throwKeyPathExpectedListButNotListException(String requestName, DfFreeGenResource resource,
            String keyPath, String targetPath, Object current) {
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

    protected void throwKeyPathExpectedStringListButNotStringException(String requestName, DfFreeGenResource resource,
            String keyPath, String targetPath, List<Object> currentList, Object element) {
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

    protected void throwKeyPathExpectedMapListButNotMapException(String requestName, DfFreeGenResource resource,
            String keyPath, String targetPath, List<Object> currentList, Object element) {
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
    public Map<String, Object> traceMap(String requestName, DfFreeGenResource resource, Map<String, Object> rootMap,
            String tracePath) {
        // e.g.
        //  jsonPath = map
        //  jsonPath = tables -> map
        //  jsonPath = schema -> map.tables -> map
        final List<String> pathList = Srl.splitListTrimmed(tracePath, "->");
        Map<String, Object> currentMap = rootMap;
        for (String pathElement : pathList) {
            if ("map".equals(pathElement)) {
                break;
            }
            final Object obj = currentMap.get(pathElement);
            if (!(obj instanceof Map<?, ?>)) {
                throwJsonTracePathNotMapException(requestName, resource, tracePath, pathElement, obj);
            }
            @SuppressWarnings("unchecked")
            final Map<String, Object> nextMap = (Map<String, Object>) obj;
            if (nextMap == null) {
                throwJsonMapKeyNotFoundException(requestName, resource, tracePath, pathElement);
            }
            currentMap = nextMap;
        }
        return currentMap;
    }

    protected void throwJsonMapKeyNotFoundException(String requestName, DfFreeGenResource resource, String tracePath,
            String pathElement) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the key in the map. (FreeGen)");
        br.addItem("Request Name");
        br.addElement(requestName);
        br.addItem("JSON File");
        br.addElement(resource.getResourceFile());
        br.addItem("Trace Path");
        br.addElement(tracePath);
        br.addItem("Path Element");
        br.addElement(pathElement);
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    protected void throwJsonTracePathNotMapException(String requestName, DfFreeGenResource resource, String tracePath,
            String pathElement, Object current) {
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
