/*
 * Copyright 2014-2025 the original author or authors.
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
package org.dbflute.properties.assistant.classification;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dbflute.jdbc.ClassificationUndefinedHandlingType;
import org.dbflute.properties.assistant.classification.refcls.DfRefClsElement;
import org.dbflute.properties.assistant.classification.top.acceptor.DfClsTopBasicItemAcceptor;
import org.dbflute.properties.assistant.classification.top.comment.DfClsTopCommentDisp;
import org.dbflute.properties.assistant.classification.top.deprecated.DfClsTopDeprecatedExistenceVerifier;
import org.dbflute.properties.assistant.classification.top.elementoption.sistercode.DfClsTopSisterCodeHandler;
import org.dbflute.properties.assistant.classification.top.elementoption.subitem.DfClsTopRegularSubItem;
import org.dbflute.properties.assistant.classification.top.elementoption.subitem.DfClsTopSubItemHandler;
import org.dbflute.properties.assistant.classification.top.grouping.DfClsTopGroupInheritanceArranger;
import org.dbflute.properties.assistant.classification.top.grouping.DfClsTopGroupListArranger;
import org.dbflute.util.Srl;

/**
 * Temporary DTO when classification initializing.
 * @author jflute
 * @since 0.8.2 (2008/10/22 Wednesday)
 */
public class DfClassificationTop { // directly used in template

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String KEY_TOP_COMMENT = "topComment";

    // code type
    public static final String KEY_CODE_TYPE = "codeType";
    public static final String KEY_DATA_TYPE = "dataType"; // old style, for compatibility
    public static final String CODE_TYPE_STRING = "String";
    public static final String CODE_TYPE_NUMBER = "Number";
    public static final String CODE_TYPE_BOOLEAN = "Boolean";
    public static final String DEFAULT_CODE_TYPE = CODE_TYPE_STRING;

    // document default, basically true
    public static final String KEY_UNDEFINED_HANDLING_TYPE = "undefinedHandlingType"; // related to checkClassificationCode
    public static final String KEY_MAKE_NATIVE_TYPE_SETTER = "isMakeNativeTypeSetter"; // related to forceClassificationSetting

    // (elements) mapping settings
    public static final String KEY_GROUPING_MAP = "groupingMap"; // grouping elements
    public static final String KEY_DEPRECATED_MAP = "deprecatedMap"; // deprecated elements

    // primitive control, closet
    public static final String KEY_CHECK_IMPLICIT_SET = "isCheckImplicitSet"; // old style, for compatibility

    // small options
    public static final String KEY_USE_DOCUMENT_ONLY = "isUseDocumentOnly";
    public static final String KEY_SUPPRESS_AUTO_DEPLOY = "isSuppressAutoDeploy";
    public static final String KEY_SUPPRESS_DBACCESS_CLASS = "isSuppressDBAccessClass";
    public static final String KEY_SUPPRESS_NAME_CAMELIZING = "isSuppressNameCamelizing";
    public static final String KEY_DEPRECATED = "isDeprecated"; // deprecated option for classification itsself

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                               Classification Identity
    //                               -----------------------
    protected final String _classificationName; // not null (required)

    // -----------------------------------------------------
    //                                            Basic Item
    //                                            ----------
    // basically not null after accept (required)
    // but null allowed only when no topElementMap (then cannot use e.g. groupingMap)
    protected String _topComment;

    protected String _codeType = DEFAULT_CODE_TYPE; // not null with default

    // -----------------------------------------------------
    //                                    Undefined Handling
    //                                    ------------------
    // basically check determination is related to the handling type
    // however for example, no check when check=false even if handling=EXCEPTION
    // littleAdjustment's force option and old style isCheckImplicitSet are also related
    protected boolean _checkClassificationCode; // derived by e.g. undefined handling, checkImplicitSet
    protected ClassificationUndefinedHandlingType _undefinedHandlingType = ClassificationUndefinedHandlingType.LOGGING; // as default

    // -----------------------------------------------------
    //                                Classification Element
    //                                ----------------------
    protected final List<DfClassificationElement> _elementList = new ArrayList<DfClassificationElement>();
    protected boolean _tableClassification; // derived by classification elements

    // -----------------------------------------------------
    //                                        Mapping Option
    //                                        --------------
    // means grouping elements, getGroupList() is used in template instead of this plain map
    protected final Map<String, Map<String, Object>> _groupingMap = new LinkedHashMap<String, Map<String, Object>>();

    // means deprecated elements, used in classification element object, this is plain map
    protected final Map<String, String> _deprecatedMap = new LinkedHashMap<String, String>();

    // -----------------------------------------------------
    //                                      Reference Option
    //                                      ----------------
    protected final List<DfRefClsElement> _refClsElementList = new ArrayList<DfRefClsElement>(); // for appcls

    // -----------------------------------------------------
    //                                     Adjustment Option
    //                                     -----------------
    // #hope jflute delete two old style options (from java6) to be simple (2021/07/04)
    protected boolean _checkImplicitSet; // old style, use undefinedHandlingType
    protected boolean _checkSelectedClassification; // old style, use undefinedHandlingType
    protected boolean _forceClassificationSetting; // suppress native-type setter, already true as defalut since java8
    protected boolean _useDocumentOnly; // suppress generating at application code
    protected boolean _suppressAutoDeploy; // no automatic classification deployment
    protected boolean _suppressDBAccessClass; // no DB-access class (e.g. behavior) for table classification
    protected boolean _suppressNameCamelizing; // use plain name, to avoid Japanese and English headache 
    protected boolean _deprecated; // deprecated option for classification itsself

    // -----------------------------------------------------
    //                               Manually-Related Column
    //                               -----------------------
    // normally unused, only for e.g. classification resource
    protected String _relatedColumnName;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfClassificationTop(String classificationName) {
        _classificationName = classificationName;
    }

    // ===================================================================================
    //                                                                              Accept
    //                                                                              ======
    public void acceptBasicItem(Map<?, ?> topElementMap) {
        final DfClsTopBasicItemAcceptor acceptor = new DfClsTopBasicItemAcceptor(_classificationName, topElementMap);
        _topComment = acceptor.acceptTopComment(); // not null
        _codeType = acceptor.acceptCodeType(_codeType); // not null with default
    }

    // ===================================================================================
    //                                                                          Basic Item
    //                                                                          ==========
    // -----------------------------------------------------
    //                                           Top Comment
    //                                           -----------
    public boolean hasTopComment() { // almost true but may be false in rare case (no topElementMap)
        return Srl.is_NotNull_and_NotTrimmedEmpty(_topComment);
    }

    public String getTopCommentDisp() {
        return createClsTopCommentDisp().buildTopCommentDisp();
    }

    public String getTopCommentForJavaDoc() {
        return createClsTopCommentDisp().buildTopCommentForJavaDoc();
    }

    public String getTopCommentForJavaDocNest() {
        return createClsTopCommentDisp().buildTopCommentForJavaDocNest();
    }

    public String getTopCommentForSchemaHtml() { // used by just SchemaHTML
        return createClsTopCommentDisp().buildTopCommentForSchemaHtml();
    }

    public String getTopCommentPlainlyForDfpropMap() { // used by e.g. Client NamedCls
        return createClsTopCommentDisp().buildTopCommentPlainlyForDfpropMap();
    }

    protected DfClsTopCommentDisp createClsTopCommentDisp() {
        return new DfClsTopCommentDisp(_topComment, _useDocumentOnly);
    }

    // -----------------------------------------------------
    //                                             Code Type
    //                                             ---------
    public boolean hasCodeType() {
        return Srl.is_NotNull_and_NotTrimmedEmpty(_codeType);
    }

    public boolean isCodeTypeNeedsQuoted() {
        // basically codeType is not null but just in case
        return _codeType == null || _codeType.equalsIgnoreCase(CODE_TYPE_STRING); // quoted if unknown
    }

    // ===================================================================================
    //                                                                  Undefined Handling
    //                                                                  ==================
    public boolean isUndefinedHandlingTypeChecked() {
        return _undefinedHandlingType != null && _undefinedHandlingType.isChecked();
    }

    public boolean isUndefinedHandlingTypeCheckedAbort() {
        return _undefinedHandlingType != null && _undefinedHandlingType.isCheckedAbort();
    }

    public boolean isUndefinedHandlingTypeCheckedContinue() {
        return _undefinedHandlingType != null && _undefinedHandlingType.isCheckedContinue();
    }

    public boolean isUndefinedHandlingTypeContinued() {
        return _undefinedHandlingType != null && _undefinedHandlingType.isContinued();
    }

    // ===================================================================================
    //                                                              Classification Element
    //                                                              ======================
    // -----------------------------------------------------
    //                                      Element Handling
    //                                      ----------------
    public int getElementSize() {
        return _elementList.size();
    }

    public void addClassificationElement(DfClassificationElement classificationElement) {
        classificationElement.setClassificationTop(this);
        _elementList.add(classificationElement);
    }

    public void addClassificationElementAll(List<DfClassificationElement> classificationElementList) {
        classificationElementList.forEach(el -> el.setClassificationTop(this));
        _elementList.addAll(classificationElementList);
    }

    public DfClassificationElement findClassificationElementByName(String name) { // null if not found
        return _elementList.stream().filter(el -> el.getName().equals(name)).findFirst().orElse(null);
    }

    // -----------------------------------------------------
    //                                           Sister Code
    //                                           -----------
    public boolean hasSisterCode() {
        return new DfClsTopSisterCodeHandler(_elementList).hasSisterCode();
    }

    public boolean hasSisterEmpty() {
        return new DfClsTopSisterCodeHandler(_elementList).hasSisterEmpty();
    }

    public boolean isSisterBooleanHandling() {
        return new DfClsTopSisterCodeHandler(_elementList).isSisterBooleanHandling();
    }

    // -----------------------------------------------------
    //                                           SubItem Map
    //                                           -----------
    public boolean hasSubItem() {
        return new DfClsTopSubItemHandler(this).hasSubItem();
    }

    public List<DfClsTopRegularSubItem> getRegularSubItemList() {
        return new DfClsTopSubItemHandler(this).arrangeRegularSubItemList();
    }

    // ===================================================================================
    //                                                                      Mapping Option
    //                                                                      ==============
    // -----------------------------------------------------
    //                                          Grouping Map
    //                                          ------------
    public void inheritRefClsGroup(DfClassificationTop referredClsTop) { // for e.g. appcls
        new DfClsTopGroupInheritanceArranger(this, referredClsTop).inheritRefClsGroup(_groupingMap);
    }

    public boolean isAlreadyGroupArranged() { // for e.g. inheritance process
        return _cachedGroupList != null; // means already arranged
    }

    protected List<DfClassificationGroup> _cachedGroupList; // null means before arrangement

    public List<DfClassificationGroup> getGroupList() {
        if (_cachedGroupList != null) {
            return _cachedGroupList;
        }
        _cachedGroupList = new DfClsTopGroupListArranger(this, _groupingMap).arrangeGroupList();
        return _cachedGroupList;
    }

    public boolean hasGroup() {
        return !getGroupList().isEmpty();
    }

    public DfClassificationGroup findGroup(String groupName) { // null allowed, case insensitive
        return getGroupList().stream().filter(group -> group.getGroupName().equalsIgnoreCase(groupName)).findFirst().orElse(null);
    }

    // -----------------------------------------------------
    //                                        Deprecated Map
    //                                        --------------
    public void verifyDeprecatedElementExistence() {
        new DfClsTopDeprecatedExistenceVerifier(this, _deprecatedMap).verifyDeprecatedElementExistence();
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "{" + _classificationName + ", " + _topComment + ", " + _codeType + ", elements=" + _elementList.size() + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    // -----------------------------------------------------
    //                               Classification Identity
    //                               -----------------------
    public String getClassificationName() {
        return _classificationName;
    }

    // -----------------------------------------------------
    //                                            Basic Item
    //                                            ----------
    public String getTopComment() {
        return _topComment;
    }

    public void setTopComment(String topComment) {
        _topComment = topComment;
    }

    public String getCodeType() {
        return _codeType;
    }

    public void setCodeType(String codeType) {
        _codeType = codeType;
    }

    // -----------------------------------------------------
    //                                    Undefined Handling
    //                                    ------------------
    public boolean isCheckClassificationCode() {
        return _checkClassificationCode;
    }

    public void setCheckClassificationCode(boolean checkClassificationCode) {
        _checkClassificationCode = checkClassificationCode;
    }

    public ClassificationUndefinedHandlingType getUndefinedHandlingType() {
        return _undefinedHandlingType;
    }

    public void setUndefinedHandlingType(ClassificationUndefinedHandlingType undefinedHandlingType) {
        _undefinedHandlingType = undefinedHandlingType;
    }

    // -----------------------------------------------------
    //                                Classification Element
    //                                ----------------------
    public List<DfClassificationElement> getClassificationElementList() {
        return _elementList;
    }

    public boolean isTableClassification() {
        return _tableClassification;
    }

    public void setTableClassification(boolean tableClassification) {
        _tableClassification = tableClassification;
    }

    // -----------------------------------------------------
    //                                        Mapping Option
    //                                        --------------
    protected Map<String, Map<String, Object>> getGroupingMap() { // not public to use resolved group list
        return _groupingMap; // not null
    }

    public void putGroupingAll(Map<String, Map<String, Object>> groupingMap) {
        _groupingMap.putAll(groupingMap);
    }

    public Map<String, String> getDeprecatedMap() {
        return _deprecatedMap; // not null
    }

    public void putDeprecatedAll(Map<String, String> deprecatedMap) {
        _deprecatedMap.putAll(deprecatedMap);
    }

    // -----------------------------------------------------
    //                                     RefClsElementList
    //                                     -----------------
    public List<DfRefClsElement> getRefClsElementList() { // for webCls
        return _refClsElementList;
    }

    public void addRefClsElement(DfRefClsElement classificationElement) {
        _refClsElementList.add(classificationElement);
    }

    // -----------------------------------------------------
    //                                     Adjustment Option
    //                                     -----------------
    public boolean isCheckImplicitSet() { // contains table classification determination
        return !_tableClassification && _checkImplicitSet;
    }

    public void setCheckImplicitSet(boolean checkImplicitSet) {
        _checkImplicitSet = checkImplicitSet;
    }

    public boolean isCheckSelectedClassification() {
        return _checkSelectedClassification;
    }

    public void setCheckSelectedClassification(boolean checkSelectedClassification) {
        _checkSelectedClassification = checkSelectedClassification;
    }

    public boolean isForceClassificationSetting() {
        return _forceClassificationSetting;
    }

    public void setForceClassificationSetting(boolean forceClassificationSetting) {
        _forceClassificationSetting = forceClassificationSetting;
    }

    public boolean isUseDocumentOnly() {
        return _useDocumentOnly;
    }

    public void setUseDocumentOnly(boolean useDocumentOnly) {
        _useDocumentOnly = useDocumentOnly;
    }

    public boolean isSuppressAutoDeploy() {
        return _suppressAutoDeploy;
    }

    public void setSuppressAutoDeploy(boolean suppressAutoDeploy) {
        _suppressAutoDeploy = suppressAutoDeploy;
    }

    public boolean isSuppressDBAccessClass() {
        return _suppressDBAccessClass;
    }

    public void setSuppressDBAccessClass(boolean suppressDBAccessClass) {
        _suppressDBAccessClass = suppressDBAccessClass;
    }

    public boolean isSuppressNameCamelizing() {
        return _suppressNameCamelizing;
    }

    public void setSuppressNameCamelizing(boolean suppressNameCamelizing) {
        _suppressNameCamelizing = suppressNameCamelizing;
    }

    public boolean isDeprecated() {
        return _deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        _deprecated = deprecated;
    }

    // -----------------------------------------------------
    //                               Manually-Related Column
    //                               -----------------------
    public String getRelatedColumnName() { // not for template, for deployment
        return _relatedColumnName;
    }

    public void setRelatedColumnName(String relatedColumnName) {
        _relatedColumnName = relatedColumnName;
    }
}
