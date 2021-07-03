/*
 * Copyright 2014-2021 the original author or authors.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dbflute.exception.DfClassificationRequiredAttributeNotFoundException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.properties.assistant.classification.element.attribute.DfClsElementApplicationComment;
import org.dbflute.properties.assistant.classification.element.attribute.DfClsElementCodeAliasVariables;
import org.dbflute.properties.assistant.classification.element.attribute.DfClsElementCommentDisp;
import org.dbflute.properties.assistant.classification.element.attribute.DfClsElementSisterCodeExp;
import org.dbflute.properties.assistant.classification.element.attribute.DfClsElementSubItemExp;
import org.dbflute.properties.assistant.classification.element.tophandling.DfClsElementDeprecatedHandling;
import org.dbflute.properties.assistant.classification.element.tophandling.DfClsElementGroupHandling;
import org.dbflute.util.Srl;

/**
 * Temporary DTO when classification initializing.
 * @author jflute
 * @since 0.8.2 (2008/10/22 Wednesday)
 */
public class DfClassificationElement { // directly used in template

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
    // -----------------------------------------------------
    //                                       Top Information
    //                                       ---------------
    protected String _classificationName; // not null (required)
    protected DfClassificationTop _classificationTop; // after registered to top

    // -----------------------------------------------------
    //                                     Element Attribute
    //                                     -----------------
    // almost items are not null after accept but null check in methods just in case
    protected String _table; // table classification only
    protected String _code; // not null after accept
    protected String _name; // not null after accept
    protected String _alias; // not null after accept
    protected String _comment; // null allowed
    protected String[] _sisters = new String[] {}; // not null with default
    protected Map<String, Object> _subItemMap; // not null after accept

    // ===================================================================================
    //                                                                              Accept
    //                                                                              ======
    public void acceptBasicItemMap(Map<?, ?> elementMap) {
        doAcceptBasicItemMap(elementMap, KEY_CODE, KEY_NAME, KEY_ALIAS, KEY_COMMENT, KEY_SISTER_CODE, KEY_SUB_ITEM_MAP);
    }

    protected void doAcceptBasicItemMap(Map<?, ?> elementMap, String codeKey, String nameKey, String aliasKey, String commentKey,
            String sisterCodeKey, String subItemMapKey) {
        final String code = (String) elementMap.get(codeKey);
        if (code == null) {
            throwClassificationRequiredAttributeNotFoundException(elementMap);
        }
        _code = code;

        final String name = (String) elementMap.get(nameKey);
        _name = (name != null ? name : code); // same as code if null

        final String alias = (String) elementMap.get(aliasKey);
        _alias = (alias != null ? alias : name); // same as name if null

        _comment = (String) elementMap.get(commentKey);

        final Object sisterCodeObj = elementMap.get(sisterCodeKey);
        if (sisterCodeObj != null) {
            if (sisterCodeObj instanceof List<?>) {
                @SuppressWarnings("unchecked")
                final List<String> sisterCodeList = (List<String>) sisterCodeObj;
                _sisters = sisterCodeList.toArray(new String[sisterCodeList.size()]);
            } else {
                _sisters = new String[] { (String) sisterCodeObj };
            }
        } else {
            _sisters = new String[] {};
        }

        // initialize by dummy when no definition for velocity trap
        // (if null, variable in for-each is not overridden so previous loop's value is used)
        @SuppressWarnings("unchecked")
        final Map<String, Object> subItemMap = (Map<String, Object>) elementMap.get(subItemMapKey);
        _subItemMap = subItemMap != null ? subItemMap : new HashMap<String, Object>(2);
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
    //                                                                    Basic Expression
    //                                                                    ================
    // -----------------------------------------------------
    //                                           Sister Code
    //                                           -----------
    public String buildSisterCodeExpForSchemaHtml() { // e.g. "sea, land"
        return new DfClsElementSisterCodeExp(_sisters).buildSisterCodeExpForSchemaHtml();
    }

    // -----------------------------------------------------
    //                                           SubItem Map
    //                                           -----------
    public String buildSubItemExpForSchemaHtml() { // key-value expression
        return new DfClsElementSubItemExp(_subItemMap).buildSubItemExpForSchemaHtml();
    }

    // -----------------------------------------------------
    //                                          Grouping Map
    //                                          ------------
    public boolean isGroup(String groupName) {
        return new DfClsElementGroupHandling(_classificationTop, _name).isGroup(groupName);
    }

    // -----------------------------------------------------
    //                                        Deprecated Map
    //                                        --------------
    public boolean isDeprecated() {
        return new DfClsElementDeprecatedHandling(_classificationTop, _name).isDeprecated();
    }

    public String getDeprecatedComment() {
        return new DfClsElementDeprecatedHandling(_classificationTop, _name).getDeprecatedComment();
    }

    // ===================================================================================
    //                                                                Code Alias Variables
    //                                                                ====================
    // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
    // Code Alias Variables means e.g. "FML", "Formalized", new String[]{"a", "b"}
    // (too long name but give up renaming because of too many fix) by jflute (2021/07/03)
    // _/_/_/_/_/_/_/_/_/_/
    public String buildClassificationCodeAliasVariables() {
        return createClsElementCodeAliasVariables().buildCodeAliasVariables();
    }

    public String buildClassificationCodeAliasSisterCodeVariables() {
        return createClsElementCodeAliasVariables().buildCodeAliasSisterCodeVariables();
    }

    protected DfClsElementCodeAliasVariables createClsElementCodeAliasVariables() {
        return new DfClsElementCodeAliasVariables(getCode(), getAlias(), getSisters());
    }

    // ===================================================================================
    //                                                                 Application Comment
    //                                                                 ===================
    // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
    // Application Comment means alias + commentDisp
    // (too long name but give up renaming because of too many fix) by jflute (2021/07/03)
    // _/_/_/_/_/_/_/_/_/_/
    public String buildClassificationApplicationCommentForJavaDoc() { // has many user e.g. CDef template
        return createClsElementApplicationComment().buildApplicationCommentForJavaDoc();
    }

    public String buildClassificationApplicationCommentForSchemaHtml() { // unused now (commentDisp used instead) (2021/07/03)
        return createClsElementApplicationComment().buildApplicationCommentForSchemaHtml();
    }

    protected DfClsElementApplicationComment createClsElementApplicationComment() {
        return new DfClsElementApplicationComment(getAlias(), getCommentDisp());
    }

    // ===================================================================================
    //                                                                     Comment Display
    //                                                                     ===============
    // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
    // Comment Display (commentDisp) means comment + deprecated comment (if exists)
    // _/_/_/_/_/_/_/_/_/_/
    protected String getCommentDisp() { // not null, empty allowed if no comment
        return createClsElementCommentDisp().buildCommentDisp();
    }

    protected DfClsElementCommentDisp createClsElementCommentDisp() {
        return new DfClsElementCommentDisp(_comment, getDeprecatedComment());
    }

    // forJavaDoc methods are unused now, application comment is used in JavaDoc instead (2021/06/22)
    public String getCommentForJavaDoc() {
        return createClsElementCommentDisp().buildCommentForJavaDoc();
    }

    public String getCommentForJavaDocNest() {
        return createClsElementCommentDisp().buildCommentForJavaDocNest();
    }

    public String getCommentForSchemaHtml() { // used by just SchemaHTML
        return createClsElementCommentDisp().buildCommentForSchemaHtml();
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final String clsType = _table != null ? "table(" + _table + ")" : "implicit";
        final StringBuilder sb = new StringBuilder();
        sb.append(_classificationName);
        sb.append(":{").append(clsType);
        sb.append(", code=").append(_code).append(", name=").append(_name);
        sb.append(", alias=").append(_alias).append(", comment=").append(_comment);
        sb.append("}");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    // -----------------------------------------------------
    //                                       Top Information
    //                                       ---------------
    public String getClassificationName() {
        return _classificationName;
    }

    public void setClassificationName(String classificationName) {
        _classificationName = classificationName;
    }

    public DfClassificationTop getClassificationTop() {
        return _classificationTop;
    }

    public void setClassificationTop(DfClassificationTop classificationTop) {
        _classificationTop = classificationTop;
    }

    // -----------------------------------------------------
    //                                     Element Attribute
    //                                     -----------------
    public String getTable() {
        return _table; // exists if table classification
    }

    public void setTable(String table) {
        _table = table;
    }

    public String getCode() {
        return _code; // not null after accept
    }

    public void setCode(String code) {
        _code = code;
    }

    public String getName() {
        return _name; // not null after accept
    }

    public void setName(String name) {
        _name = name;
    }

    public String getAlias() {
        return _alias; // not null after accept
    }

    public void setAlias(String alias) {
        _alias = alias;
    }

    // other comment expression methods exists e.g. getCommentDisp()
    // use them if it needs instead of this
    public String getComment() { // so basically unused (directly) in templates
        return _comment; // null allowed
    }

    public void setComment(String comment) {
        _comment = comment;
    }

    // string array is hard to be handled in templates
    public String[] getSisters() { // so basically unused (directly) in templates
        return _sisters; // not null (has default)
    }

    public void setSisters(String[] sisters) {
        _sisters = sisters;
    }

    public Map<String, Object> getSubItemMap() {
        return _subItemMap; // not null after accept
    }

    public void setSubItemMap(Map<String, Object> subItemMap) {
        _subItemMap = subItemMap;
    }
}
