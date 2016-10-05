/*
 * Copyright 2014-2016 the original author or authors.
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
package org.dbflute.logic.manage.freegen.table.prop;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dbflute.DfBuildProperties;
import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.helper.jprop.JavaPropertiesProperty;
import org.dbflute.helper.jprop.JavaPropertiesReader;
import org.dbflute.helper.jprop.JavaPropertiesResult;
import org.dbflute.helper.jprop.JavaPropertiesStreamProvider;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.manage.freegen.DfFreeGenMapProp;
import org.dbflute.logic.manage.freegen.DfFreeGenRequest;
import org.dbflute.logic.manage.freegen.DfFreeGenResource;
import org.dbflute.logic.manage.freegen.DfFreeGenMetaData;
import org.dbflute.logic.manage.freegen.DfFreeGenTableLoader;
import org.dbflute.properties.DfDocumentProperties;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfNameHintUtil;
import org.dbflute.util.DfPropertyUtil;
import org.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfPropTableLoader implements DfFreeGenTableLoader {

    // ===================================================================================
    //                                                                          Load Table
    //                                                                          ==========
    // ; resourceMap = map:{
    //     ; resourceType = PROP
    //     ; resourceFile = ../../../sea.properties
    // }
    // ; outputMap = map:{
    //     ; templateFile = MessageDef.vm
    //     ; outputDirectory = ../src/main/java
    //     ; package = org.dbflute...
    //     ; className = MessageDef
    // }
    // ; tableMap = map:{
    //     ; exceptKeyList = list:{ prefix:config. }
    //     ; groupingKeyMap = map:{ label = prefix:label. }
    //     ; extendsPropRequest = FooProp
    //     ; extendsPropFileList = list:{ ../../../bar.properties }
    //     ; isCheckImplicitOverride = false
    // }
    public DfFreeGenMetaData loadTable(String requestName, DfFreeGenResource resource, DfFreeGenMapProp mapProp) {
        final Map<String, Object> tableMap = mapProp.getOptionMap();
        final Map<String, DfFreeGenRequest> requestMap = mapProp.getRequestMap();
        final JavaPropertiesReader reader = createReader(requestName, resource, tableMap, requestMap);
        final JavaPropertiesResult result;
        try {
            result = reader.read();
        } catch (RuntimeException e) {
            final Map<String, Map<String, String>> mappingMap = mapProp.getMappingMap();
            throwFreeGenPropReadFailureException(requestName, resource, tableMap, mappingMap, e);
            return null; // unreachable
        }
        final String resourceFile = resource.getResourceFile();
        final String tableName = buildTableName(resourceFile);
        final List<Map<String, Object>> columnList = toMapList(result, tableMap);
        return new DfFreeGenMetaData(tableMap, tableName, columnList);
    }

    protected void throwFreeGenPropReadFailureException(String requestName, DfFreeGenResource resource, Map<String, Object> tableMap,
            Map<String, Map<String, String>> mappingMap, RuntimeException cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to read the property for free-gen.");
        br.addItem("Current Request");
        br.addElement(requestName);
        br.addItem("Resource");
        br.addElement(resource);
        br.addItem("tableMap");
        if (!tableMap.isEmpty()) {
            for (Entry<String, Object> entry : tableMap.entrySet()) {
                br.addElement(entry.getKey() + " = " + entry.getValue());
            }
        } else {
            br.addElement("*empty");
        }
        br.addItem("mappingMap");
        if (!mappingMap.isEmpty()) {
            for (Entry<String, Map<String, String>> entry : mappingMap.entrySet()) {
                br.addElement(entry.getKey() + " = " + entry.getValue());
            }
        } else {
            br.addElement("*empty");
        }
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg, cause);
    }

    protected String buildTableName(final String resourceFile) {
        return Srl.substringLastFront((Srl.substringLastRear(resourceFile, "/")), "."); // sea
    }

    // ===================================================================================
    //                                                                   Properties Reader
    //                                                                   =================
    protected JavaPropertiesReader createReader(String requestName, DfFreeGenResource resource, Map<String, Object> tableMap,
            Map<String, DfFreeGenRequest> requestMap) {
        final String resourceFile = resource.getResourceFile();
        final String title = requestName + ":" + resourceFile;
        final JavaPropertiesReader reader = new JavaPropertiesReader(title, new JavaPropertiesStreamProvider() {
            public InputStream provideStream() throws IOException {
                return new FileInputStream(new File(resourceFile));
            }
        });
        final String extendsPropRequestKey = "extendsPropRequest";
        String extendsPropRequest = (String) tableMap.get(extendsPropRequestKey);
        final List<String> extendsPropFileList;
        if (extendsPropRequest != null) {
            extendsPropFileList = new ArrayList<String>();
            while (true) {
                final DfFreeGenRequest extendsRequest = requestMap.get(extendsPropRequest);
                if (extendsRequest == null) {
                    throwFreeGenPropExtendsRequestNotFoundException(requestName, extendsPropRequest, requestMap);
                }
                if (!extendsRequest.isResourceTypeProp()) {
                    throwFreeGenPropExtendsRequestNotPropException(requestName, extendsPropRequest, extendsRequest);
                }
                final String extendsFile = extendsRequest.getResource().getResourceFile();
                extendsPropFileList.add(extendsFile);
                final Map<String, Object> extendsTableMap = extendsRequest.getOptionMap();
                extendsPropRequest = (String) extendsTableMap.get(extendsPropRequestKey);
                if (extendsPropRequest == null) {
                    break;
                }
            }
        } else { // for compatible
            @SuppressWarnings("unchecked")
            final List<String> castList = (List<String>) tableMap.get("extendsPropFileList");
            extendsPropFileList = castList;
        }
        if (extendsPropFileList != null && !extendsPropFileList.isEmpty()) {
            for (String extendsPropFile : extendsPropFileList) {
                final String resolvedFile = resource.resolveBaseDir(extendsPropFile);
                final String extendsTitle = requestName + ":" + extendsPropFile;
                reader.extendsProperties(extendsTitle, new JavaPropertiesStreamProvider() {
                    public InputStream provideStream() throws IOException {
                        return new FileInputStream(new File(resolvedFile));
                    }
                });
            }
        }
        if (isProperty("isCheckImplicitOverride", tableMap)) {
            reader.checkImplicitOverride();
        }
        if (isProperty("isUseNonNumberVariable", tableMap)) {
            reader.useNonNumberVariable();
        }
        return reader;
    }

    protected void throwFreeGenPropExtendsRequestNotFoundException(String requestName, String extendsPropRequest,
            Map<String, DfFreeGenRequest> requestMap) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the request to be extended for properties.");
        br.addItem("Advice");
        br.addElement("Make sure your request name in freeGenDefinition.dfprop.");
        br.addElement("And also check definition order of requests.");
        br.addElement("Extends requests should be defined before the request.");
        br.addItem("Current Request");
        br.addElement(requestName);
        br.addItem("Extended Request");
        br.addElement(extendsPropRequest);
        br.addItem("Existing Requests");
        br.addElement(requestMap.keySet());
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    protected void throwFreeGenPropExtendsRequestNotPropException(String requestName, String extendsPropRequest,
            DfFreeGenRequest extendsRequest) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not PROP request to be extended for properties.");
        br.addItem("Advice");
        br.addElement("Make sure your request name in freeGenDefinition.dfprop.");
        br.addElement("The extends request should be PROP type.");
        br.addItem("Current Request");
        br.addElement(requestName);
        br.addItem("Not PROP Extended Request");
        br.addElement(extendsPropRequest);
        br.addElement(extendsRequest.getResource().getResourceType());
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    protected boolean isProperty(String key, Map<String, Object> tableMap) {
        String value = (String) tableMap.get(key);
        if (value == null) {
            final String derived = deriveBooleanAnotherKey(key);
            if (derived != null) {
                value = (String) tableMap.get(derived);
            }
        }
        return value != null && value.trim().equalsIgnoreCase("true");
    }

    protected String deriveBooleanAnotherKey(String key) {
        return DfPropertyUtil.deriveBooleanAnotherKey(key);
    }

    // ===================================================================================
    //                                                                           Converter
    //                                                                           =========
    public List<Map<String, Object>> toMapList(JavaPropertiesResult result, Map<String, Object> tableMap) {
        final List<JavaPropertiesProperty> propertyList = result.getPropertyList();
        return doConvertToMapList(propertyList, tableMap);
    }

    protected List<Map<String, Object>> doConvertToMapList(final List<JavaPropertiesProperty> propertyList, Map<String, Object> tableMap) {
        final List<String> exceptKeyList = extractExceptKeyList(tableMap);
        final Map<String, String> groupingKeyMap = extractDeterminationMap(tableMap);
        final DfDocumentProperties prop = getDocumentProperties();
        final List<Map<String, Object>> mapList = DfCollectionUtil.newArrayList();
        for (JavaPropertiesProperty property : propertyList) {
            final Map<String, Object> columnMap = DfCollectionUtil.newLinkedHashMap();
            final String propertyKey = property.getPropertyKey();
            if (!isTargetKey(propertyKey, exceptKeyList)) {
                continue;
            }
            columnMap.put("propertyKey", propertyKey);
            final String propertyValue = property.getPropertyValue();
            columnMap.put("propertyValue", propertyValue != null ? propertyValue : "");
            final String valueHtmlEncoded = prop.resolveTextForSimpleLineHtml(propertyValue);
            columnMap.put("propertyValueHtmlEncoded", valueHtmlEncoded != null ? valueHtmlEncoded : "");
            columnMap.put("hasPropertyValue", Srl.is_NotNull_and_NotTrimmedEmpty(propertyValue));

            final String defName = convertToDefName(propertyKey);
            columnMap.put("defName", defName);

            final String camelizedName = Srl.camelize(defName);
            columnMap.put("camelizedName", camelizedName);
            columnMap.put("capCamelName", Srl.initCap(camelizedName));
            columnMap.put("uncapCamelName", Srl.initUncap(camelizedName));
            final List<Integer> variableNumberList = property.getVariableNumberList();
            columnMap.put("variableCount", variableNumberList.size()); // old style
            columnMap.put("variableNumberCount", variableNumberList.size());
            columnMap.put("variableNumberList", variableNumberList);
            final List<String> variableStringList = property.getVariableStringList();
            columnMap.put("variableStringCount", variableStringList.size());
            columnMap.put("variableStringList", variableStringList);
            columnMap.put("variableArgNameList", property.getVariableArgNameList());
            columnMap.put("variableArgDef", property.getVariableArgDef());
            columnMap.put("variableArgSet", property.getVariableArgSet());
            columnMap.put("hasVariable", !variableStringList.isEmpty());

            final String comment = property.getComment();
            columnMap.put("comment", comment != null ? comment : "");
            final String commentHtmlEncoded = prop.resolveTextForSimpleLineHtml(comment);
            columnMap.put("commentHtmlEncoded", commentHtmlEncoded != null ? commentHtmlEncoded : "");
            columnMap.put("hasComment", Srl.is_NotNull_and_NotTrimmedEmpty(comment));
            columnMap.put("isExtends", property.isExtends());
            columnMap.put("isOverride", property.isOverride());
            columnMap.put("mayBeIntegerProperty", mayBeIntegerProperty(property, comment));
            columnMap.put("mayBeLongProperty", mayBeLongProperty(property, comment));
            columnMap.put("mayBeDecimalProperty", mayBeDecimalProperty(property, comment));
            columnMap.put("mayBeDateProperty", mayBeDateProperty(property, comment));
            columnMap.put("mayBeBooleanProperty", mayBeBooleanProperty(property, comment));

            for (Entry<String, String> entry : groupingKeyMap.entrySet()) {
                final String groupingName = entry.getKey();
                final String keyHint = entry.getValue();
                final String deternationKey = "is" + Srl.initCap(groupingName);
                columnMap.put(deternationKey, isGroupingTarget(propertyKey, keyHint));
            }

            mapList.add(columnMap);
        }
        return mapList;
    }

    protected List<String> extractExceptKeyList(Map<String, Object> tableMap) {
        @SuppressWarnings("unchecked")
        final List<String> exceptKeyList = (List<String>) tableMap.get("exceptKeyList");
        if (exceptKeyList != null) {
            return exceptKeyList;
        }
        return DfCollectionUtil.emptyList();
    }

    protected Map<String, String> extractDeterminationMap(Map<String, Object> tableMap) {
        @SuppressWarnings("unchecked")
        final Map<String, String> groupingKeyMap = (Map<String, String>) tableMap.get("groupingKeyMap");
        if (groupingKeyMap != null) {
            return groupingKeyMap;
        }
        return DfCollectionUtil.emptyMap();
    }

    protected boolean isTargetKey(String propertyKey, List<String> exceptKeyList) {
        final List<String> targetDummyList = DfCollectionUtil.emptyList();
        return DfNameHintUtil.isTargetByHint(propertyKey, targetDummyList, exceptKeyList);
    }

    protected String convertToDefName(String propertyKey) {
        final List<String> splitList = Srl.splitList(propertyKey, ".");
        final StringBuilder sb = new StringBuilder();
        for (String element : splitList) {
            if (sb.length() > 0) {
                sb.append("_");
            }
            if (mightBeCamelCase(element)) {
                sb.append(Srl.decamelize(element));
            } else {
                sb.append(element);
            }
        }
        return sb.toString();
    }

    protected boolean mightBeCamelCase(String element) { // e.g. fooBar, foo
        return Srl.isInitLowerCase(element) && !element.contains("_");
    }

    protected boolean isGroupingTarget(String propertyKey, String keyHint) {
        return DfNameHintUtil.isHitByTheHint(propertyKey, keyHint);
    }

    // -----------------------------------------------------
    //                                         Property Type
    //                                         -------------
    protected boolean mayBeIntegerProperty(JavaPropertiesProperty property, String comment) {
        if (mayBeLongProperty(property, comment) || mayBeDecimalProperty(property, comment)) {
            return false;
        }
        return property.mayBeIntegerProperty() || containsPropertyTypeAnnotation(comment, "@IntegerType");
    }

    protected boolean mayBeLongProperty(JavaPropertiesProperty property, String comment) {
        if (mayBeDecimalProperty(property, comment)) {
            return false;
        }
        return property.mayBeLongProperty() || containsPropertyTypeAnnotation(comment, "@LongType");
    }

    protected boolean mayBeDecimalProperty(JavaPropertiesProperty property, String comment) {
        return property.mayBeDecimalProperty() || containsPropertyTypeAnnotation(comment, "@DecimalType");
    }

    protected boolean mayBeDateProperty(JavaPropertiesProperty property, String comment) {
        return property.mayBeDateProperty() || containsPropertyTypeAnnotation(comment, "@DateType");
    }

    protected boolean mayBeBooleanProperty(JavaPropertiesProperty property, String comment) {
        return property.mayBeBooleanProperty() || containsPropertyTypeAnnotation(comment, "@BooleanType");
    }

    protected boolean containsPropertyTypeAnnotation(String comment, String annotation) {
        return comment != null && comment.contains(annotation);
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfDocumentProperties getDocumentProperties() {
        return getProperties().getDocumentProperties();
    }
}
