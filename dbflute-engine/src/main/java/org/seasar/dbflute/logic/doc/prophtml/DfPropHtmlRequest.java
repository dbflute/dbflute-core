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
package org.seasar.dbflute.logic.doc.prophtml;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.seasar.dbflute.exception.DfIllegalPropertySettingException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.jprop.JavaPropertiesProperty;
import org.seasar.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 * @since 1.0.1 (2012/12/21 Friday)
 */
public class DfPropHtmlRequest {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final String MASKING_VALUE = "********";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _requestName;
    protected final Map<String, DfPropHtmlFileAttribute> _fileAttributeMap = DfCollectionUtil.newLinkedHashMap();
    protected final Map<String, DfPropHtmlProperty> _propertyMap = DfCollectionUtil.newLinkedHashMap();
    protected final Set<String> _diffIgnoredKeySet = DfCollectionUtil.newLinkedHashSet();
    protected final Set<String> _maskedKeySet = DfCollectionUtil.newLinkedHashSet();
    protected final boolean _envOnlyFloatLeft;
    protected final String _extendsPropRequest;
    protected final boolean _checkImplicitOverride;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfPropHtmlRequest(String requestName, List<String> diffIgnoredKeyList, List<String> maskedKeyList,
            boolean envOnlyFloatLeft, String extendsPropRequest, boolean checkImplicitOverride) {
        _requestName = requestName;
        addDiffIgnoredKeyAll(diffIgnoredKeyList);
        addMaskedKeyAll(maskedKeyList);
        _envOnlyFloatLeft = envOnlyFloatLeft;
        _extendsPropRequest = extendsPropRequest;
        _checkImplicitOverride = checkImplicitOverride;
    }

    // ===================================================================================
    //                                                                        Request Name
    //                                                                        ============
    public String getRequestName() {
        return _requestName;
    }

    public String getRequestLowerName() {
        return _requestName.toLowerCase();
    }

    // ===================================================================================
    //                                                                      File Attribute
    //                                                                      ==============
    public DfPropHtmlFileAttribute getFileAttribute(String envType, String langType) {
        return _fileAttributeMap.get(generateFileAttributeKey(envType, langType));
    }

    public List<DfPropHtmlFileAttribute> getFileAttributeList() {
        return DfCollectionUtil.newArrayList(_fileAttributeMap.values());
    }

    public void addFileAttribute(DfPropHtmlFileAttribute attribute) {
        final String envType = attribute.getEnvType();
        final String langType = attribute.getLangType();
        _fileAttributeMap.put(generateFileAttributeKey(envType, langType), attribute);
    }

    protected String generateFileAttributeKey(final String envType, final String langType) {
        return envType + ":" + langType;
    }

    // ===================================================================================
    //                                                                   Property Handling
    //                                                                   =================
    public DfPropHtmlProperty getProperty(String propertyKey) {
        return _propertyMap.get(propertyKey);
    }

    public List<DfPropHtmlProperty> getPropertyList() {
        return DfCollectionUtil.newArrayList(_propertyMap.values());
    }

    public void addProperty(String envType, String langType, JavaPropertiesProperty jprop) {
        final String propertyKey = jprop.getPropertyKey();
        final String propertyValue = jprop.getPropertyValue();
        final String comment = jprop.getComment();
        final boolean override = jprop.isOverride();
        final boolean secure = jprop.isSecure();
        doAddProperty(propertyKey, envType, langType, propertyValue, comment, override, secure);
    }

    protected void doAddProperty(String propertyKey, String envType, String langType, String propertyValue,
            String comment, boolean override, boolean secure) {
        final DfPropHtmlProperty property = prepareManagedProperty(propertyKey);
        final String registeredValue = filterRegisteredValue(propertyKey, propertyValue, secure);
        property.setPropertyValue(envType, langType, registeredValue, comment, override, secure);
        checkEnvOnlyNoLang(propertyKey, property);
    }

    protected DfPropHtmlProperty prepareManagedProperty(String propertyKey) {
        DfPropHtmlProperty property = _propertyMap.get(propertyKey);
        if (property == null) {
            property = new DfPropHtmlProperty(propertyKey);
            _propertyMap.put(propertyKey, property);
        }
        return property;
    }

    protected String filterRegisteredValue(String propertyKey, String propertyValue, boolean secure) {
        final String registeredValue;
        if (secure || _maskedKeySet.contains(propertyKey)) { // maskedKeySet should be set before
            registeredValue = MASKING_VALUE; // masked here
        } else {
            registeredValue = propertyValue;
        }
        return registeredValue;
    }

    // ===================================================================================
    //                                                                     Option Handling
    //                                                                     ===============
    public Set<String> getDiffIgnoredKeySet() {
        return _diffIgnoredKeySet;
    }

    protected void addDiffIgnoredKeyAll(List<String> diffIgnoredKeyList) {
        _diffIgnoredKeySet.addAll(diffIgnoredKeyList);
    }

    public Set<String> getMaskedKeySet() {
        return _maskedKeySet;
    }

    protected void addMaskedKeyAll(List<String> maskedKeyList) {
        _maskedKeySet.addAll(maskedKeyList);
    }

    public boolean isEnvOnlyFloatLeft() {
        return _envOnlyFloatLeft;
    }

    protected void checkEnvOnlyNoLang(String propertyKey, DfPropHtmlProperty property) {
        if (_envOnlyFloatLeft && property.getLangTypeSet().size() >= 2) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("Env only mode but two lang types exist.");
            br.addItem("Advice");
            br.addElement("You cannot use isEnvOnlyFloatLeft if lang type is plural");
            br.addElement("in request of PropertiesHtml.");
            br.addItem("Request");
            br.addElement(_requestName);
            br.addItem("Property");
            br.addElement(propertyKey);
            br.addItem("Lang Types");
            br.addElement(property.getLangTypeSet());
            final String msg = br.buildExceptionMessage();
            throw new DfIllegalPropertySettingException(msg);
        }
    }

    public boolean hasExtendsPropRequest() {
        return _extendsPropRequest != null;
    }

    public String getExtendsPropRequest() {
        return _extendsPropRequest;
    }

    public boolean isCheckImplicitOverride() {
        return _checkImplicitOverride;
    }
}
