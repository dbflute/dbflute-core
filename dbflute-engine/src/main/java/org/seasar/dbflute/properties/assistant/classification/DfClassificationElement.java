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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.exception.DfClassificationRequiredAttributeNotFoundException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.properties.DfDocumentProperties;
import org.seasar.dbflute.util.Srl;

/**
 * Temporary DTO when classification initializing.
 * @author jflute
 * @since 0.8.2 (2008/10/22 Wednesday)
 */
public class DfClassificationElement {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String KEY_TABLE = "table";
    public static final String KEY_CODE = "code";
    public static final String KEY_NAME = "name";
    public static final String KEY_ALIAS = "alias";
    public static final String KEY_SISTER_CODE = "sisterCode";
    public static final String KEY_COMMENT = "comment";
    public static final String KEY_SUB_ITEM_MAP = "subItemMap";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String _classificationName; // required
    protected String _table; // table classification only
    protected DfClassificationTop _classificationTop; // after registered to top

    // basic items
    protected String _code;
    protected String _name;
    protected String _alias;
    protected String _comment;
    protected String[] _sisters = new String[] {}; // as default
    protected Map<String, Object> _subItemMap; // not-null after accept

    // ===================================================================================
    //                                                                              Accept
    //                                                                              ======
    public void acceptBasicItemMap(Map<?, ?> elementMap) {
        doAcceptBasicItemMap(elementMap, KEY_CODE, KEY_NAME, KEY_ALIAS, KEY_COMMENT, KEY_SISTER_CODE, KEY_SUB_ITEM_MAP);
    }

    protected void doAcceptBasicItemMap(Map<?, ?> elementMap, String codeKey, String nameKey, String aliasKey,
            String commentKey, String sisterCodeKey, String subItemMapKey) {
        final String code = (String) elementMap.get(codeKey);
        if (code == null) {
            throwClassificationRequiredAttributeNotFoundException(elementMap);
        }
        this._code = code;

        final String name = (String) elementMap.get(nameKey);
        this._name = (name != null ? name : code); // same as code if null

        final String alias = (String) elementMap.get(aliasKey);
        this._alias = (alias != null ? alias : name); // same as name if null

        this._comment = (String) elementMap.get(commentKey);

        final Object sisterCodeObj = elementMap.get(sisterCodeKey);
        if (sisterCodeObj != null) {
            if (sisterCodeObj instanceof List<?>) {
                @SuppressWarnings("unchecked")
                final List<String> sisterCodeList = (List<String>) sisterCodeObj;
                this._sisters = sisterCodeList.toArray(new String[sisterCodeList.size()]);
            } else {
                this._sisters = new String[] { (String) sisterCodeObj };
            }
        } else {
            this._sisters = new String[] {};
        }

        // initialize by dummy when no definition for velocity trap
        // (if null, variable in foreach is not overridden so previous loop's value is used)
        @SuppressWarnings("unchecked")
        final Map<String, Object> subItemMap = (Map<String, Object>) elementMap.get(subItemMapKey);
        this._subItemMap = subItemMap != null ? subItemMap : new HashMap<String, Object>(2);
    }

    protected void throwClassificationRequiredAttributeNotFoundException(Map<?, ?> elementMap) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The element map did not have a 'code' attribute.");
        br.addItem("Advice");
        br.addElement("An element map requires 'code' attribute like this:");
        br.addElement("  (o): map:{table=MEMBER_STATUS; code=MEMBER_STATUS_CODE; ...}");
        br.addItem("Classification");
        br.addElement(_classificationName);
        if (_table != null) {
            br.addItem("Table");
            br.addElement(_table);
        }
        br.addItem("ElementMap");
        br.addElement(elementMap);
        final String msg = br.buildExceptionMessage();
        throw new DfClassificationRequiredAttributeNotFoundException(msg);
    }

    // ===================================================================================
    //                                                                       Alias/Comment
    //                                                                       =============
    public boolean hasAlias() {
        return Srl.is_NotNull_and_NotTrimmedEmpty(_alias);
    }

    public boolean hasComment() {
        return Srl.is_NotNull_and_NotTrimmedEmpty(_comment);
    }

    public boolean hasCommentDisp() {
        return Srl.is_NotNull_and_NotTrimmedEmpty(getCommentDisp());
    }

    // ===================================================================================
    //                                                                         Sister Code
    //                                                                         ===========
    public String buildSisterCodeExpForSchemaHtml() {
        if (_sisters == null || _sisters.length == 0) {
            return "&nbsp;";
        }
        final StringBuilder sb = new StringBuilder();
        int index = 0;
        for (String sister : _sisters) {
            if (index > 0) {
                sb.append(", ");
            }
            sb.append(sister);
            ++index;
        }
        return sb.toString();
    }

    // ===================================================================================
    //                                                                         SubItem Map
    //                                                                         ===========
    public String buildSubItemExpForSchemaHtml() {
        if (_subItemMap == null || _subItemMap.isEmpty()) {
            return "&nbsp;";
        }
        final StringBuilder sb = new StringBuilder();
        int index = 0;
        for (Entry<String, Object> entry : _subItemMap.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            if (index > 0) {
                sb.append("\n, ");
            }
            sb.append(key).append("=").append(value);
            ++index;
        }
        return filterSubItemForSchemaHtml(sb.toString());
    }

    protected String filterSubItemForSchemaHtml(String str) {
        final DfDocumentProperties prop = DfBuildProperties.getInstance().getDocumentProperties();
        return prop.resolveTextForSchemaHtml(Srl.replace(str, "\\n", "\n"));
    }

    // ===================================================================================
    //                                                                        Grouping Map
    //                                                                        ============
    public boolean isGroup(String groupName) {
        if (_classificationTop == null) {
            return false;
        }
        final List<DfClassificationGroup> groupList = _classificationTop.getGroupList();
        for (DfClassificationGroup group : groupList) {
            if (groupName.equals(group.getGroupName())) {
                final List<String> elementNameList = group.getElementNameList();
                if (elementNameList.contains(_name)) {
                    return true;
                }
                break;
            }
        }
        return false;
    }

    // ===================================================================================
    //                                                                      Deprecated Map
    //                                                                      ==============
    public boolean isDeprecated() {
        if (_classificationTop == null) { // just in case
            return false;
        }
        return _classificationTop.getDeprecatedMap().containsKey(_name);
    }

    public String getDeprecatedComment() {
        if (!isDeprecated()) {
            return "";
        }
        final String comment = _classificationTop.getDeprecatedMap().get(_name);
        return comment != null ? removeLineSeparator(comment) : "";
    }

    protected String removeLineSeparator(String str) {
        return Srl.replace(Srl.replace(str, "\r\n", "\n"), "\n", "");
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

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return _classificationName + ":{" + _table + ", " + _code + ", " + _name + ", " + _alias + ", " + _comment
                + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getClassificationName() {
        return _classificationName;
    }

    public void setClassificationName(String classificationName) {
        this._classificationName = classificationName;
    }

    public String getTable() {
        return _table;
    }

    public void setTable(String table) {
        this._table = table;
    }

    public DfClassificationTop getClassificationTop() {
        return _classificationTop;
    }

    public void setClassificationTop(DfClassificationTop classificationTop) {
        this._classificationTop = classificationTop;
    }

    public String getCode() {
        return _code;
    }

    public void setCode(String code) {
        this._code = code;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getAlias() {
        return _alias;
    }

    public void setAlias(String alias) {
        _alias = alias;
    }

    public String getComment() {
        return _comment;
    }

    public String getCommentDisp() {
        return buildCommentDisp();
    }

    protected String buildCommentDisp() {
        final StringBuilder sb = new StringBuilder();
        sb.append(_comment != null ? _comment : "");
        if (isDeprecated()) {
            final String deprecatedComment = getDeprecatedComment();
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append("(deprecated: ").append(deprecatedComment).append(")");
        }
        final String disp = sb.toString();
        return Srl.replace(disp, "\n", ""); // basically one line
    }

    public String getCommentForJavaDoc() {
        return buildCommentForJavaDoc("    "); // basically indent unused
    }

    public String getCommentForJavaDocNest() {
        return buildCommentForJavaDoc("        "); // basically indent unused
    }

    protected String buildCommentForJavaDoc(String indent) {
        return resolveTextForJavaDoc(getCommentDisp(), indent);
    }

    public String getCommentForSchemaHtml() {
        return resolveTextForSchemaHtml(getCommentDisp());
    }

    public void setComment(String comment) {
        _comment = comment;
    }

    public String[] getSisters() {
        return _sisters;
    }

    public void setSisters(String[] sisters) {
        _sisters = sisters;
    }

    public Map<String, Object> getSubItemMap() {
        return _subItemMap;
    }

    public void setSubItemMap(Map<String, Object> subItemMap) {
        _subItemMap = subItemMap;
    }
}
