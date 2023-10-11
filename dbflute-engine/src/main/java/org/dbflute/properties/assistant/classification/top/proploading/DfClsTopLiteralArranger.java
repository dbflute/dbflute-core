/*
 * Copyright 2014-2023 the original author or authors.
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
package org.dbflute.properties.assistant.classification.top.proploading;

import java.util.Map;

import org.dbflute.DfBuildProperties;
import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.jdbc.ClassificationUndefinedHandlingType;
import org.dbflute.properties.DfLittleAdjustmentProperties;
import org.dbflute.properties.assistant.base.DfPropertyValueHandler;
import org.dbflute.properties.assistant.classification.DfClassificationTop;
import org.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 * @since 1.2.5 split from DfClassificationProperties (2021/07/04 Sunday at roppongi japanese)
 */
public class DfClsTopLiteralArranger {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfPropertyValueHandler _propertyValueHandler;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfClsTopLiteralArranger(DfPropertyValueHandler propertyValueHandler) {
        _propertyValueHandler = propertyValueHandler;
    }

    // ===================================================================================
    //                                                                Arrange from Literal
    //                                                                ====================
    // -----------------------------------------------------
    //                                        Process Method
    //                                        --------------
    public void arrangeClassificationTopFromLiteral(DfClassificationTop classificationTop, Map<?, ?> elementMap) {
        // #thinking jflute while analyzing literal, we don't know table or implict classification (2023/08/17)
        // so you cannot determine e.g. isCheckImplicitSet correctly (but small problem so keep it for now)
        classificationTop.acceptBasicItem(elementMap);
        classificationTop.setCheckClassificationCode(isElementMapCheckClassificationCode(elementMap));
        classificationTop.setUndefinedHandlingType(getElementMapUndefinedHandlingType(elementMap));
        classificationTop.setCheckImplicitSet(isElementMapCheckImplicitSet(elementMap));
        classificationTop.setCheckSelectedClassification(isElementMapCheckSelectedClassification(elementMap));
        classificationTop.setForceClassificationSetting(isElementMapForceClassificationSetting(elementMap));
        classificationTop.setUseDocumentOnly(isElementMapUseDocumentOnly(elementMap));
        classificationTop.setSuppressAutoDeploy(isElementMapSuppressAutoDeploy(elementMap));
        classificationTop.setSuppressDBAccessClass(isElementMapSuppressDBAccessClass(elementMap));
        classificationTop.setSuppressNameCamelizing(isElementMapSuppressNameCamelizing(elementMap));
        classificationTop.setDeprecated(isElementMapDeprecated(elementMap));
        classificationTop.putGroupingAll(getElementMapGroupingMap(elementMap));
        classificationTop.putDeprecatedAll(getElementMapDeprecatedMap(elementMap));
    }

    // -----------------------------------------------------
    //                                    Undefined Handling
    //                                    ------------------
    // user public since 1.1 (implemented when 1.0.5K)
    protected boolean isElementMapCheckClassificationCode(Map<?, ?> elementMap) {
        final boolean checked = getElementMapUndefinedHandlingType(elementMap).isChecked();
        if (hasElementMapUndefinedHandlingTypeProperty(elementMap)) {
            return checked; // e.g. explicitly undefinedHandlingType=[something]
        }
        if (hasElementMapCheckImplicitSetProperty(elementMap) && !isElementMapCheckImplicitSet(elementMap)) {
            // #for_now jflute isCheckImplicitSet, no determination whether table classification or not (2023/08/17)
            return false; // explicitly isCheckImplicitSet=false
        }
        final DfLittleAdjustmentProperties prop = getLittleAdjustmentProperties();
        if (prop.isSuppressDefaultCheckClassificationCode()) {
            return false;
        }
        return checked;
    }

    @SuppressWarnings("unchecked")
    protected boolean hasElementMapUndefinedHandlingTypeProperty(Map<?, ?> elementMap) {
        final String key = DfClassificationTop.KEY_UNDEFINED_HANDLING_TYPE;
        if (getProperty(key, null, (Map<String, ? extends Object>) elementMap) != null) {
            return true;
        }
        if (getLittleAdjustmentProperties().hasClassificationUndefinedHandlingTypeProperty()) {
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    protected ClassificationUndefinedHandlingType getElementMapUndefinedHandlingType(Map<?, ?> elementMap) {
        if (isElementMapCheckImplicitSet(elementMap)) {
            // #for_now jflute isCheckImplicitSet, no determination whether table classification or not (2023/08/17)
            // so here even if isCheckImplicitSet=true in table classification
            return ClassificationUndefinedHandlingType.EXCEPTION;
        }
        final String key = DfClassificationTop.KEY_UNDEFINED_HANDLING_TYPE;
        final DfLittleAdjustmentProperties prop = getLittleAdjustmentProperties();
        final ClassificationUndefinedHandlingType defaultType = prop.getClassificationUndefinedHandlingType();
        final String defaultValue = defaultType.code();
        final String code = getProperty(key, defaultValue, (Map<String, ? extends Object>) elementMap);
        final ClassificationUndefinedHandlingType handlingType =
                ClassificationUndefinedHandlingType.of(code).orElseTranslatingThrow(cause -> {
                    throwUnknownClassificationUndefinedCodeHandlingTypeException(code, cause);
                    return null; // unreachable
                });
        return handlingType;
    }

    protected void throwUnknownClassificationUndefinedCodeHandlingTypeException(String code, Throwable cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Unknown handling type of classification undefined code.");
        br.addItem("Advice");
        br.addElement("You can specify following types:");
        for (ClassificationUndefinedHandlingType handlingType : ClassificationUndefinedHandlingType.values()) {
            br.addElement(" " + handlingType.code());
        }
        final String exampleCode = ClassificationUndefinedHandlingType.EXCEPTION.code();
        br.addElement("");
        br.addElement("For example: (classificationDefinitionMap.dfprop)");
        br.addElement("; [classification-name] = list:{");
        br.addElement("    ; map:{");
        br.addElement("        ; topComment=...");
        br.addElement("        ; codeType=String");
        br.addElement("        ; classificationUndefinedCodeHandlingType = " + exampleCode);
        br.addElement("        ...");
        br.addElement("    }");
        br.addElement("}");
        br.addItem("Specified Unknown Type");
        br.addElement(code);
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg, cause);
    }

    // user closet, old style
    @SuppressWarnings("unchecked")
    protected boolean hasElementMapCheckImplicitSetProperty(Map<?, ?> elementMap) { // has been existed since old
        final String key = DfClassificationTop.KEY_CHECK_IMPLICIT_SET;
        return getProperty(key, null, (Map<String, ? extends Object>) elementMap) != null;
    }

    @SuppressWarnings("unchecked")
    protected boolean isElementMapCheckImplicitSet(Map<?, ?> elementMap) { // has been existed since old
        // table classification determination is set at DfClassificationTop
        // #hope jflute isCheckImplicitSet, should determine table classification here (2023/08/17)
        final String key = DfClassificationTop.KEY_CHECK_IMPLICIT_SET;
        return isProperty(key, false, (Map<String, ? extends Object>) elementMap);
    }

    protected boolean isElementMapCheckSelectedClassification(Map<?, ?> elementMap) { // has been existed since old
        return getLittleAdjustmentProperties().isCheckSelectedClassification();
    }

    // -----------------------------------------------------
    //                                  Force Classification
    //                                  --------------------
    // user public since 1.1 (implemented when 1.0.5K)
    @SuppressWarnings("unchecked")
    protected boolean hasClassificationMakeNativeTypeProperty(Map<?, ?> elementMap) {
        final String key = DfClassificationTop.KEY_MAKE_NATIVE_TYPE_SETTER;
        return getProperty(key, null, (Map<String, ? extends Object>) elementMap) != null;
    }

    @SuppressWarnings("unchecked")
    protected boolean isClassificationMakeNativeTypeSetter(Map<?, ?> elementMap) {
        final String key = DfClassificationTop.KEY_MAKE_NATIVE_TYPE_SETTER;
        return isProperty(key, false, (Map<String, ? extends Object>) elementMap);
    }

    // user closet, but primitive control
    protected boolean isElementMapForceClassificationSetting(Map<?, ?> elementMap) {
        if (hasClassificationMakeNativeTypeProperty(elementMap)) {
            return !isClassificationMakeNativeTypeSetter(elementMap);
        }
        return getLittleAdjustmentProperties().isForceClassificationSetting();
    }

    // -----------------------------------------------------
    //                                          Small Option
    //                                          ------------
    @SuppressWarnings("unchecked")
    protected boolean isElementMapUseDocumentOnly(Map<?, ?> elementMap) {
        final String key = DfClassificationTop.KEY_USE_DOCUMENT_ONLY;
        return isProperty(key, false, (Map<String, ? extends Object>) elementMap);
    }

    @SuppressWarnings("unchecked")
    protected boolean isElementMapSuppressAutoDeploy(Map<?, ?> elementMap) {
        final String key = DfClassificationTop.KEY_SUPPRESS_AUTO_DEPLOY;
        return isProperty(key, false, (Map<String, ? extends Object>) elementMap);
    }

    @SuppressWarnings("unchecked")
    protected boolean isElementMapSuppressDBAccessClass(Map<?, ?> elementMap) {
        final String key = DfClassificationTop.KEY_SUPPRESS_DBACCESS_CLASS;
        final DfLittleAdjustmentProperties prop = getLittleAdjustmentProperties();
        final boolean defaultValue = prop.isSuppressTableClassificationDBAccessClass();
        return isProperty(key, defaultValue, (Map<String, ? extends Object>) elementMap);
    }

    @SuppressWarnings("unchecked")
    protected boolean isElementMapSuppressNameCamelizing(Map<?, ?> elementMap) {
        final String key = DfClassificationTop.KEY_SUPPRESS_NAME_CAMELIZING;
        final DfLittleAdjustmentProperties prop = getLittleAdjustmentProperties();
        final boolean defaultValue = prop.isSuppressTableClassificationNameCamelizing();
        return isProperty(key, defaultValue, (Map<String, ? extends Object>) elementMap);
    }

    @SuppressWarnings("unchecked")
    protected boolean isElementMapDeprecated(Map<?, ?> elementMap) {
        final String key = DfClassificationTop.KEY_DEPRECATED;
        return isProperty(key, false, (Map<String, ? extends Object>) elementMap);
    }

    // ===================================================================================
    //                                                                    Mapping Settings
    //                                                                    ================
    // also called by e.g. appcls loader so public
    public Map<String, Map<String, Object>> getElementMapGroupingMap(Map<?, ?> elementMap) {
        final Object obj = elementMap.get(DfClassificationTop.KEY_GROUPING_MAP);
        if (obj == null) {
            return DfCollectionUtil.emptyMap();
        }
        @SuppressWarnings("unchecked")
        final Map<String, Map<String, Object>> groupingMap = (Map<String, Map<String, Object>>) obj;
        return groupingMap;
    }

    public Map<String, String> getElementMapDeprecatedMap(Map<?, ?> elementMap) {
        final Object obj = elementMap.get(DfClassificationTop.KEY_DEPRECATED_MAP);
        if (obj == null) {
            return DfCollectionUtil.emptyMap();
        }
        @SuppressWarnings("unchecked")
        final Map<String, String> deprecatedMap = (Map<String, String>) obj;
        return deprecatedMap;
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfLittleAdjustmentProperties getLittleAdjustmentProperties() {
        return DfBuildProperties.getInstance().getLittleAdjustmentProperties();
    }

    // ===================================================================================
    //                                                                    Flexible Handler
    //                                                                    ================
    protected String getProperty(String key, String defaultValue, Map<String, ? extends Object> map) {
        return _propertyValueHandler.getProperty(key, defaultValue, map);
    }

    protected boolean isProperty(String key, boolean defaultValue, Map<String, ? extends Object> map) {
        return _propertyValueHandler.isProperty(key, defaultValue, map);
    }
}
