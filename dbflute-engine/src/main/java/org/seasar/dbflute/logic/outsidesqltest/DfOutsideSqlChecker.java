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
package org.seasar.dbflute.logic.outsidesqltest;

import java.util.List;

import org.seasar.dbflute.exception.DfCustomizeEntityMarkInvalidException;
import org.seasar.dbflute.exception.DfParameterBeanMarkInvalidException;
import org.seasar.dbflute.exception.DfRequiredOutsideSqlDescriptionNotFoundException;
import org.seasar.dbflute.exception.DfRequiredOutsideSqlTitleNotFoundException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.logic.sql2entity.analyzer.DfSql2EntityMarkAnalyzer;
import org.seasar.dbflute.twowaysql.SqlAnalyzer;
import org.seasar.dbflute.twowaysql.node.IfCommentEvaluator;
import org.seasar.dbflute.twowaysql.node.ParameterFinder;
import org.seasar.dbflute.util.DfStringUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.9.5 (2009/04/10 Friday)
 */
public class DfOutsideSqlChecker {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final String EMECHA_DEFAULT_TITLE = "SQL title here.";
    private static final String EMECHA_DEFAULT_DESCRIPTION = "SQL Description here.";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected boolean _ifCommentExpressionCheck;
    protected boolean _requiredTitleCheck;
    protected boolean _requiredDescriptionCheck;

    // ===================================================================================
    //                                                                             Checker
    //                                                                             =======
    public void check(String fileName, String sql) {
        // check Sql2Entity mark
        final List<String> splitList = splitList(sql, "\n");
        checkSql2EntityMark(splitList, fileName, sql);

        // check parameter comment easily
        final SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);
        final List<String> ifCommentList = analyzer.researchIfComment();
        analyzer.analyze(); // should throw an exception

        // check IF comment expression (option)
        checkIfCommentExpression(ifCommentList, sql);

        // check title and description (option)
        checkRequiredTitle(fileName, sql);
        checkRequiredDescription(fileName, sql);
    }

    // ===================================================================================
    //                                                                     Sql2Entity Mark
    //                                                                     ===============
    protected void checkSql2EntityMark(List<String> splitList, String fileName, String sql) {
        for (String line : splitList) {
            line = line.trim();
            if (!line.contains("--")) {
                continue;
            }
            if (line.contains("#df;entity#") || line.contains("#df:pmb#") || line.contains("#df:emtity#")) {
                throwCustomizeEntityMarkInvalidException(line, fileName, sql);
            } else if (line.contains("!df;pmb!") || line.contains("!df:entity!") || line.contains("!df:pnb!")) {
                throwParameterBeanMarkInvalidException(line, fileName, sql);
            }
        }
    }

    // ===================================================================================
    //                                                                IfComment Expression
    //                                                                ====================
    protected void checkIfCommentExpression(List<String> ifCommentList, String sql) {
        if (!_ifCommentExpressionCheck) {
            return;
        }
        for (String expr : ifCommentList) {
            final IfCommentEvaluator evaluator = new IfCommentEvaluator(new ParameterFinder() {
                public Object find(String name) {
                    return null;
                }
            }, expr, sql, null);
            evaluator.assertExpression();
        }
    }

    protected void throwCustomizeEntityMarkInvalidException(String line, String fileName, String sql) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The CustomizeEntity mark was invalid.");
        br.addItem("Advice");
        br.addElement("Please confirm your CustomizeEntity mark.");
        br.addElement("For example:");
        br.addElement("  (x): -- #df;entity#  *NOT semicolun");
        br.addElement("  (x): -- #df:pmb#     *NOT parameter bean");
        br.addElement("  (x): -- #df:emtity#  *NOT emtity ('entity' is right)");
        br.addElement("  (o): -- #df:entity#");
        br.addItem("CustomizeEntity Mark");
        br.addElement(line);
        br.addItem("SQL File");
        br.addElement(fileName);
        br.addItem("Your SQL");
        br.addElement(sql);
        final String msg = br.buildExceptionMessage();
        throw new DfCustomizeEntityMarkInvalidException(msg);
    }

    protected void throwParameterBeanMarkInvalidException(String line, String fileName, String sql) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The ParameterBean mark was invalid.");
        br.addItem("Advice");
        br.addElement("Please confirm your ParameterBean mark.");
        br.addElement("For example:");
        br.addElement("  (x): -- !df;pmb!     *NOT semicolun");
        br.addElement("  (x): -- !df:entity!  *NOT customize entity");
        br.addElement("  (x): -- !df:pnb!     *NOT pnb ('pmb' is right)");
        br.addElement("  (o): -- !df:pmb!");
        br.addItem("ParameterBean Mark");
        br.addElement(line);
        br.addItem("SQL File");
        br.addElement(fileName);
        br.addItem("Your SQL");
        br.addElement(sql);
        final String msg = br.buildExceptionMessage();
        throw new DfParameterBeanMarkInvalidException(msg);
    }

    // ===================================================================================
    //                                                                Required SQL Comment
    //                                                                ====================
    public void checkRequiredSqlComment(String fileName, String sql) { // basically for Sql2Entity task
        checkRequiredTitle(fileName, sql);
        checkRequiredDescription(fileName, sql);
    }

    protected void checkRequiredTitle(String fileName, String sql) {
        if (!_requiredTitleCheck) {
            return;
        }
        final DfSql2EntityMarkAnalyzer analyzer = new DfSql2EntityMarkAnalyzer();
        final String title = analyzer.getTitle(sql);
        if (isInvalidTitle(title)) {
            throwRequiredOutsideSqlTitleNotFoundException(title, fileName, sql);
        }
    }

    protected boolean isInvalidTitle(String title) {
        return Srl.is_Null_or_TrimmedEmpty(title) || EMECHA_DEFAULT_TITLE.equalsIgnoreCase(title.trim());
    }

    protected void throwRequiredOutsideSqlTitleNotFoundException(String title, String fileName, String sql) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The outsideSql title was NOT found!");
        br.addItem("Advice");
        br.addElement("OutsideSql title is required in this project.");
        br.addElement("The property 'isRequiredSqlTitle' of outsideSqlDefinition is true.");
        br.addElement("So you should add title comment, [df:title] and title,");
        br.addElement("in your outside-SQL like this:");
        br.addElement("  /= = = = = = = = = = = = = = = = = = =");
        br.addElement("  /*");
        br.addElement("   [df:title]");
        br.addElement("   Simple Member Select");
        br.addElement("");
        br.addElement("   [df:description]");
        br.addElement("   This SQL is ...");
        br.addElement("  */");
        br.addElement("  -- #df:entity#");
        br.addElement("");
        br.addElement("  -- !df:pmb!");
        br.addElement("  -- !!AutoDetect!!");
        br.addElement("");
        br.addElement("  select ...");
        br.addElement("    from ...");
        br.addElement("   where ...");
        br.addElement("  = = = = = = = = = = /");
        br.addElement("");
        br.addElement("If you need to remove the check,");
        br.addElement("change the property in your outsideSqlDefinitionMap.dfprop.");
        br.addElement("Fro example:");
        br.addElement("    ; isRequiredSqlTitle = false");
        br.addItem("Title");
        br.addElement(title);
        br.addItem("SQL File");
        br.addElement(fileName);
        br.addItem("Your SQL");
        br.addElement(sql);
        final String msg = br.buildExceptionMessage();
        throw new DfRequiredOutsideSqlTitleNotFoundException(msg);
    }

    protected void checkRequiredDescription(String fileName, String sql) {
        if (!_requiredDescriptionCheck) {
            return;
        }
        final DfSql2EntityMarkAnalyzer analyzer = new DfSql2EntityMarkAnalyzer();
        final String desc = analyzer.getDescription(sql);
        if (isInvalidDescription(desc)) {
            throwRequiredOutsideSqlDescriptionNotFoundException(desc, fileName, sql);
        }
    }

    protected boolean isInvalidDescription(String desc) {
        return Srl.is_Null_or_TrimmedEmpty(desc) || EMECHA_DEFAULT_DESCRIPTION.equalsIgnoreCase(desc.trim());
    }

    protected void throwRequiredOutsideSqlDescriptionNotFoundException(String desc, String fileName, String sql) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The outsideSql description was NOT found.");
        br.addItem("Advice");
        br.addElement("OutsideSql description is required in this project.");
        br.addElement("The property 'isRequiredSqlDescription' of outsideSqlDefinition is true.");
        br.addElement("So you should add description comment, [df:description] and description,");
        br.addElement("in your outside-SQL like this:");
        br.addElement("  /= = = = = = = = = = = = = = = = = = =");
        br.addElement("  /*");
        br.addElement("   [df:title]");
        br.addElement("   Simple Member Select");
        br.addElement("");
        br.addElement("   [df:description]");
        br.addElement("   This SQL is ...");
        br.addElement("  */");
        br.addElement("  -- #df:entity#");
        br.addElement("  -- !df:pmb!");
        br.addElement("  -- !!AutoDetect!!");
        br.addElement("");
        br.addElement("  select ...");
        br.addElement("    from ...");
        br.addElement("   where ...");
        br.addElement("  = = = = = = = = = = /");
        br.addElement("");
        br.addElement("If you need to remove the check,");
        br.addElement("change the property in your outsideSqlDefinitionMap.dfprop.");
        br.addElement("Fro example:");
        br.addElement("    ; isRequiredSqlDescription = false");
        br.addItem("SQL File");
        br.addElement(fileName);
        br.addItem("Description");
        br.addElement(desc);
        br.addItem("Your SQL");
        br.addElement(sql);
        final String msg = br.buildExceptionMessage();
        throw new DfRequiredOutsideSqlDescriptionNotFoundException(msg);
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected List<String> splitList(String str, String delimiter) {
        return DfStringUtil.splitList(str, delimiter);
    }

    protected String ln() {
        return "\n";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void enableIfCommentExpressionCheck() {
        _ifCommentExpressionCheck = true;
    }

    public void enableRequiredTitleCheck() {
        _requiredTitleCheck = true;
    }

    public void enableRequiredDescriptionCheck() {
        _requiredDescriptionCheck = true;
    }
}
