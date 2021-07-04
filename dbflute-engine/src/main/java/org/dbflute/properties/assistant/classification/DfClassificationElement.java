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

import java.util.Map;

import org.dbflute.properties.assistant.classification.element.acceptor.DfClsElementBasicItemAcceptor;
import org.dbflute.properties.assistant.classification.element.attribute.DfClsElementApplicationComment;
import org.dbflute.properties.assistant.classification.element.attribute.DfClsElementCodeAliasVariables;
import org.dbflute.properties.assistant.classification.element.attribute.DfClsElementCommentDisp;
import org.dbflute.properties.assistant.classification.element.attribute.DfClsElementSisterCodeExp;
import org.dbflute.properties.assistant.classification.element.attribute.DfClsElementSubItemExp;
import org.dbflute.properties.assistant.classification.element.topoption.DfClsElementDeprecatedHandling;
import org.dbflute.properties.assistant.classification.element.topoption.DfClsElementGroupHandling;
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
        final DfClsElementBasicItemAcceptor acceptor = new DfClsElementBasicItemAcceptor(_classificationName, _table, elementMap);
        _code = acceptor.acceptCode();
        _name = acceptor.acceptName(_code); // same as code if null (not required)
        _alias = acceptor.acceptAlias(_name); // same as name if null (not required)
        _comment = acceptor.acceptComment(); // null allowed (not required)
        _sisters = acceptor.acceptSisters(); // not null, empty allowed (not required)
        _subItemMap = acceptor.acceptSubItemMap(); // not null, empty allowed (not required)
    }

    // ===================================================================================
    //                                                                       Alias/Comment
    //                                                                       =============
    public boolean hasAlias() {
        return Srl.is_NotNull_and_NotTrimmedEmpty(_alias);
    }

    public boolean hasComment() { // means plain comment
        return Srl.is_NotNull_and_NotTrimmedEmpty(_comment);
    }

    public boolean hasCommentDisp() { // e.g. with deprecated comment
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
