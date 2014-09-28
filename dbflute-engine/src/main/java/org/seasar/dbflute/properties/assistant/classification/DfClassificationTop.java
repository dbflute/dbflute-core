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
package org.seasar.dbflute.properties.assistant.classification;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.exception.DfClassificationRequiredAttributeNotFoundException;
import org.seasar.dbflute.exception.DfIllegalPropertySettingException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.jdbc.ClassificationUndefinedHandlingType;
import org.seasar.dbflute.properties.DfDocumentProperties;
import org.seasar.dbflute.task.DfDBFluteTaskStatus;
import org.seasar.dbflute.util.Srl;

/**
 * Temporary DTO when classification initializing.
 * @author jflute
 * @since 0.8.2 (2008/10/22 Wednesday)
 */
public class DfClassificationTop {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String KEY_TOP_COMMENT = "topComment";

    // code type
    public static final String KEY_CODE_TYPE = "codeType";
    public static final String KEY_DATA_TYPE = "dataType"; // old style
    public static final String CODE_TYPE_STRING = "String";
    public static final String CODE_TYPE_NUMBER = "Number";
    public static final String CODE_TYPE_BOOLEAN = "Boolean";
    public static final String DEFAULT_CODE_TYPE = CODE_TYPE_STRING;

    // primitive control, closet
    public static final String KEY_CHECK_IMPLICIT_SET = "isCheckImplicitSet"; // old style

    // document default, basically true
    public static final String KEY_UNDEFINED_HANDLING_TYPE = "undefinedHandlingType";
    public static final String KEY_MAKE_NATIVE_TYPE_SETTER = "isMakeNativeTypeSetter";

    // small options
    public static final String KEY_USE_DOCUMENT_ONLY = "isUseDocumentOnly";
    public static final String KEY_SUPPRESS_AUTO_DEPLOY = "isSuppressAutoDeploy";
    public static final String KEY_SUPPRESS_DBACCESS_CLASS = "isSuppressDBAccessClass";
    public static final String KEY_DEPRECATED = "isDeprecated";

    // mapping settings
    public static final String KEY_GROUPING_MAP = "groupingMap";
    public static final String KEY_DEPRECATED_MAP = "deprecatedMap";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String _classificationName;
    protected String _topComment;
    protected String _codeType = DfClassificationTop.CODE_TYPE_STRING; // as default
    protected boolean _checkClassificationCode;
    protected ClassificationUndefinedHandlingType _undefinedHandlingType = ClassificationUndefinedHandlingType.LOGGING; // as default
    protected String _relatedColumnName;
    protected final List<DfClassificationElement> _elementList = new ArrayList<DfClassificationElement>();
    protected boolean _tableClassification;
    protected boolean _checkImplicitSet; // old style
    protected boolean _checkSelectedClassification; // old style
    protected boolean _forceClassificationSetting;
    protected boolean _useDocumentOnly;
    protected boolean _suppressAutoDeploy; // no automatic classification deployment
    protected boolean _suppressDBAccessClass; // no DB-access class (e.g. behavior) for table classification
    protected boolean _deprecated;
    protected final Map<String, Map<String, Object>> _groupingMap = new LinkedHashMap<String, Map<String, Object>>();
    protected final Map<String, String> _deprecatedMap = new LinkedHashMap<String, String>();

    // ===================================================================================
    //                                                                              Accept
    //                                                                              ======
    public void acceptClassificationTopBasicItemMap(Map<?, ?> elementMap) {
        acceptTopMap(elementMap, KEY_TOP_COMMENT, KEY_CODE_TYPE, KEY_DATA_TYPE);
    }

    protected void acceptTopMap(Map<?, ?> elementMap, String commentKey, String codeTypeKey, String dataTypeKey) {
        // topComment
        final String topComment = (String) elementMap.get(commentKey);
        if (topComment == null) {
            throwClassificationLiteralCommentNotFoundException(_classificationName, elementMap);
        }
        _topComment = topComment;

        // codeType
        final String codeType;
        {
            String tmpType = (String) elementMap.get(codeTypeKey);
            if (Srl.is_Null_or_TrimmedEmpty(tmpType)) {
                // for compatibility
                tmpType = (String) elementMap.get(dataTypeKey);
            }
            codeType = tmpType;
        }
        if (codeType != null) {
            _codeType = codeType;
        }
    }

    protected void throwClassificationLiteralCommentNotFoundException(String classificationName, Map<?, ?> elementMap) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The comment attribute of the classification was not found.");
        br.addItem("Advice");
        br.addElement("The classification should have the comment attribute.");
        br.addElement("See the document for the DBFlute property.");
        br.addItem("Classification");
        br.addElement(classificationName);
        br.addItem("Element Map");
        br.addElement(elementMap);
        final String msg = br.buildExceptionMessage();
        throw new DfClassificationRequiredAttributeNotFoundException(msg);
    }

    // ===================================================================================
    //                                                                           Top Basic
    //                                                                           =========
    public boolean hasTopComment() {
        return Srl.is_NotNull_and_NotTrimmedEmpty(_topComment);
    }

    public boolean hasCodeType() {
        return Srl.is_NotNull_and_NotTrimmedEmpty(_codeType);
    }

    // ===================================================================================
    //                                                                         Sister Code
    //                                                                         ===========
    public boolean isSisterBooleanHandling() {
        if (_elementList.size() != 2) {
            return false;
        }
        final Set<String> firstSet = new HashSet<String>();
        {
            final String[] firstSisters = _elementList.get(0).getSisters();
            for (String sister : firstSisters) {
                firstSet.add(sister.toLowerCase());
            }
        }
        final Set<String> secondSet = new HashSet<String>();
        {
            final String[] secondSisters = _elementList.get(1).getSisters();
            for (String sister : secondSisters) {
                secondSet.add(sister.toLowerCase());
            }
        }
        return (firstSet.contains("true") && secondSet.contains("false") // first true
        || firstSet.contains("false") && secondSet.contains("true")); // first false
    }

    public boolean hasSisterCode() {
        final List<DfClassificationElement> elementList = getClassificationElementList();
        for (DfClassificationElement element : elementList) {
            final String[] sisters = element.getSisters();
            if (sisters != null && sisters.length > 0) {
                return true;
            }
        }
        return false;
    }

    // ===================================================================================
    //                                                                         SubItem Map
    //                                                                         ===========
    public boolean hasSubItem() {
        final List<DfClassificationElement> elementList = getClassificationElementList();
        for (DfClassificationElement element : elementList) {
            Map<String, Object> subItemMap = element.getSubItemMap();
            if (subItemMap != null && !subItemMap.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public List<DfClassificationRegularSubItem> getRegularSubItemList() {
        final List<DfClassificationElement> elementList = getClassificationElementList();
        final Map<String, List<Object>> subItemListMap = new LinkedHashMap<String, List<Object>>();
        for (DfClassificationElement element : elementList) {
            final Map<String, Object> subItemMap = element.getSubItemMap();
            if (subItemMap == null || subItemMap.isEmpty()) {
                continue;
            }
            for (Entry<String, Object> entry : subItemMap.entrySet()) {
                final String subItemKey = entry.getKey();
                final Object subItemValue = entry.getValue();
                List<Object> subItemList = subItemListMap.get(subItemKey);
                if (subItemList == null) {
                    subItemList = new ArrayList<Object>();
                    subItemListMap.put(subItemKey, subItemList);
                }
                subItemList.add(subItemValue);
            }
        }
        final String typeObject = DfClassificationRegularSubItem.TYPE_OBJECT;
        final String typeString = DfClassificationRegularSubItem.TYPE_STRING;
        final List<DfClassificationRegularSubItem> regularSubItemList = new ArrayList<DfClassificationRegularSubItem>();
        final int elementSize = elementList.size();
        for (Entry<String, List<Object>> entry : subItemListMap.entrySet()) {
            final String subItemKey = entry.getKey();
            final List<Object> subItemList = entry.getValue();
            if (subItemList != null && subItemList.size() == elementSize) {
                String subItemType = null;
                for (Object value : subItemList) {
                    if (value == null) {
                        continue;
                    }
                    if (!(value instanceof String)) {
                        subItemType = typeObject;
                        break;
                    } else if (Srl.startsWith((String) value, "map:", "list:")) {
                        subItemType = typeObject;
                        break;
                    }
                }
                if (subItemType == null) {
                    subItemType = typeString;
                }
                regularSubItemList.add(new DfClassificationRegularSubItem(subItemKey, subItemType));
            }
        }
        return regularSubItemList;
    }

    public static class DfClassificationRegularSubItem {
        // Object or String only supported
        public static final String TYPE_OBJECT = "Object";
        public static final String TYPE_STRING = "String";

        protected final String _subItemName;
        protected final String _subItemType;

        public DfClassificationRegularSubItem(String subItemName, String subItemType) {
            _subItemName = subItemName;
            _subItemType = subItemType;
        }

        public boolean isSubItemTypeObject() {
            return _subItemType.equals(TYPE_OBJECT);
        }

        public boolean isSubItemTypeString() {
            return _subItemType.equals(TYPE_STRING);
        }

        public String getSubItemName() {
            return _subItemName;
        }

        public String getSubItemType() {
            return _subItemType;
        }
    }

    // ===================================================================================
    //                                                                        Grouping Map
    //                                                                        ============
    protected List<DfClassificationGroup> _cachedGroupList;

    public List<DfClassificationGroup> getGroupList() {
        if (_cachedGroupList != null) {
            return _cachedGroupList;
        }
        final List<DfClassificationGroup> groupList = new ArrayList<DfClassificationGroup>();
        for (Entry<String, Map<String, Object>> entry : _groupingMap.entrySet()) {
            final String groupName = entry.getKey();
            final Map<String, Object> attrMap = entry.getValue();
            final String groupComment = (String) attrMap.get("groupComment");
            @SuppressWarnings("unchecked")
            final List<String> elementList = (List<String>) attrMap.get("elementList");
            if (elementList == null) {
                String msg = "The elementList in grouping map is required: " + getClassificationName();
                throw new DfClassificationRequiredAttributeNotFoundException(msg);
            }
            final String docOnly = (String) attrMap.get("isUseDocumentOnly");
            final DfClassificationGroup group = new DfClassificationGroup(this, groupName);
            group.setGroupComment(groupComment);
            group.setElementNameList(elementList);
            group.setUseDocumentOnly(docOnly != null && docOnly.trim().equalsIgnoreCase("true"));
            groupList.add(group);
        }
        resolveGroupVariable(groupList);
        _cachedGroupList = prepareGroupRealList(groupList);
        return _cachedGroupList;
    }

    protected void resolveGroupVariable(List<DfClassificationGroup> groupList) {
        final Map<String, DfClassificationGroup> groupMap = new LinkedHashMap<String, DfClassificationGroup>();
        for (DfClassificationGroup group : groupList) {
            groupMap.put(group.getGroupName(), group);
        }
        // e.g.
        // ; servicePlus = map:{
        //     ; elementList = list:{ $$ref$$.serviceAvailable ; Withdrawal }
        // }
        final String refPrefix = "$$ref$$.";
        for (DfClassificationGroup group : groupList) {
            final List<String> elementNameList = group.getElementNameList();
            final Set<String> resolvedNameSet = new LinkedHashSet<String>();
            for (String elementName : elementNameList) {
                if (Srl.startsWith(elementName, refPrefix)) {
                    final String refName = Srl.substringFirstRear(elementName, refPrefix).trim();
                    final DfClassificationGroup refGroup = groupMap.get(refName);
                    if (refGroup == null) {
                        throwClassificationGroupingMapReferenceNotFoundException(groupList, group, refName);
                    }
                    resolvedNameSet.addAll(refGroup.getElementNameList());
                } else {
                    resolvedNameSet.add(elementName);
                }
            }
            if (elementNameList.size() < resolvedNameSet.size()) {
                group.setElementNameList(new ArrayList<String>(resolvedNameSet));
            }
        }
    }

    protected void throwClassificationGroupingMapReferenceNotFoundException(List<DfClassificationGroup> groupList,
            DfClassificationGroup group, String refName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the refenrece in the grouping map.");
        br.addItem("Classification Name");
        br.addElement(group.getClassificationName());
        br.addItem("Group Name");
        br.addElement(group.getGroupName());
        br.addItem("NotFound Name");
        br.addElement(refName);
        br.addItem("Defined Group");
        for (DfClassificationGroup defined : groupList) {
            br.addElement(defined);
        }
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    protected List<DfClassificationGroup> prepareGroupRealList(final List<DfClassificationGroup> groupList) {
        final List<DfClassificationGroup> realList = new ArrayList<DfClassificationGroup>();
        final boolean docOnly = isDocOnlyTask();
        for (DfClassificationGroup group : groupList) {
            if (!docOnly && group.isUseDocumentOnly()) {
                continue;
            }
            realList.add(group);
        }
        return realList;
    }

    public boolean hasGroup() {
        return !getGroupList().isEmpty();
    }

    // ===================================================================================
    //                                                                     Deprecated List
    //                                                                     ===============
    public void checkDeprecatedElementExistence() {
        for (String deprecated : _deprecatedMap.keySet()) {
            boolean found = false;
            for (DfClassificationElement element : _elementList) {
                final String name = element.getName();
                if (name.equals(deprecated)) {
                    found = true;
                }
            }
            if (!found) {
                throwDeprecatedClassificationElementNotFoundException(deprecated);
            }
        }
    }

    protected void throwDeprecatedClassificationElementNotFoundException(String deprecated) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the specified element in deprecated list.");
        br.addItem("Classification");
        br.addElement(_classificationName);
        br.addItem("Existing Element");
        final StringBuilder sb = new StringBuilder();
        for (DfClassificationElement element : _elementList) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(element.getName());
        }
        br.addElement(sb.toString());
        br.addItem("NotFound Element");
        br.addElement(deprecated);
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    // ===================================================================================
    //                                                              Classification Element
    //                                                              ======================
    public int getElementSize() {
        return _elementList.size();
    }

    // ===================================================================================
    //                                                                         Escape Text
    //                                                                         ===========
    protected String resolveTextForJavaDoc(String comment, String indent) {
        final DfDocumentProperties prop = DfBuildProperties.getInstance().getDocumentProperties();
        return prop.resolveTextForJavaDoc(comment, indent);
    }

    protected String resolveTextForSchemaHtml(String comment) {
        final DfDocumentProperties prop = DfBuildProperties.getInstance().getDocumentProperties();
        return prop.resolveTextForSchemaHtml(comment);
    }

    // ===============================================================================
    //                                                                     Task Status
    //                                                                     ===========
    protected boolean isDocOnlyTask() {
        final DfDBFluteTaskStatus instance = DfDBFluteTaskStatus.getInstance();
        return instance.isDocTask() || instance.isReplaceSchema();
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "{" + _classificationName + ", " + _topComment + ", " + _codeType + ", " + _relatedColumnName + ", "
                + _elementList + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    // -----------------------------------------------------
    //                                            Basic Item
    //                                            ----------
    public String getClassificationName() {
        return _classificationName;
    }

    public void setClassificationName(String classificationName) {
        _classificationName = classificationName;
    }

    public String getTopComment() {
        return _topComment;
    }

    public String getTopCommentDisp() {
        return buildTopCommentDisp();
    }

    protected String buildTopCommentDisp() {
        if (_topComment == null) {
            return "";
        }
        final String comment;
        if (_useDocumentOnly) {
            comment = _topComment + " (document only)";
        } else {
            comment = _topComment;
        }
        return Srl.replace(comment, "\n", ""); // basically one line
    }

    public String getTopCommentForJavaDoc() {
        return buildTopCommentForJavaDoc("    "); // basically indent unused
    }

    public String getTopCommentForJavaDocNest() {
        return buildTopCommentForJavaDoc("        "); // basically indent unused
    }

    protected String buildTopCommentForJavaDoc(String indent) {
        return resolveTextForJavaDoc(getTopCommentDisp(), indent);
    }

    public String getTopCommentForSchemaHtml() {
        return resolveTextForSchemaHtml(getTopCommentDisp());
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
    //                                 UndefinedHandlingType
    //                                 ---------------------
    public boolean isCheckClassificationCode() {
        return _checkClassificationCode;
    }

    public void setCheckClassificationCode(boolean checkClassificationCode) {
        _checkClassificationCode = checkClassificationCode;
    }

    public ClassificationUndefinedHandlingType getUndefinedHandlingType() {
        return _undefinedHandlingType;
    }

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

    public void setUndefinedHandlingType(ClassificationUndefinedHandlingType undefinedHandlingType) {
        _undefinedHandlingType = undefinedHandlingType;
    }

    // -----------------------------------------------------
    //                                     RelatedColumnName
    //                                     -----------------
    public String getRelatedColumnName() {
        return _relatedColumnName;
    }

    public void setRelatedColumnName(String relatedColumnName) {
        _relatedColumnName = relatedColumnName;
    }

    // -----------------------------------------------------
    //                             ClassificationElementList
    //                             -------------------------
    public List<DfClassificationElement> getClassificationElementList() {
        return _elementList;
    }

    public void addClassificationElement(DfClassificationElement classificationElement) {
        classificationElement.setClassificationTop(this);
        _elementList.add(classificationElement);
    }

    public void addClassificationElementAll(List<DfClassificationElement> classificationElementList) {
        for (DfClassificationElement element : classificationElementList) {
            element.setClassificationTop(this);
        }
        _elementList.addAll(classificationElementList);
    }

    public DfClassificationElement findClassificationElementByName(String name) {
        for (DfClassificationElement element : _elementList) {
            if (element.getName().equals(name)) {
                return element;
            }
        }
        return null;
    }

    // -----------------------------------------------------
    //                                 Various Determination
    //                                 ---------------------
    public boolean isTableClassification() {
        return _tableClassification;
    }

    public void setTableClassification(boolean tableClassification) {
        _tableClassification = tableClassification;
    }

    public boolean isCheckImplicitSet() {
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

    public boolean isDeprecated() {
        return _deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        _deprecated = deprecated;
    }

    // -----------------------------------------------------
    //                                        Mapping Option
    //                                        --------------
    protected Map<String, Map<String, Object>> getGroupingMap() { // not public to use resolved group list
        return _groupingMap;
    }

    public void putGroupingAll(Map<String, Map<String, Object>> groupingMap) {
        _groupingMap.putAll(groupingMap);
    }

    public Map<String, String> getDeprecatedMap() {
        return _deprecatedMap;
    }

    public void putDeprecatedAll(Map<String, String> deprecatedMap) {
        _deprecatedMap.putAll(deprecatedMap);
    }
}
