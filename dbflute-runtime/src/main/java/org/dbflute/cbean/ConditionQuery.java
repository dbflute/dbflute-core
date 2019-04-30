/*
 * Copyright 2014-2019 the original author or authors.
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
package org.dbflute.cbean;

import org.dbflute.cbean.coption.ConditionOption;
import org.dbflute.cbean.coption.ParameterOption;
import org.dbflute.cbean.cvalue.ConditionValue;
import org.dbflute.cbean.sqlclause.SqlClause;
import org.dbflute.dbmeta.info.ColumnInfo;
import org.dbflute.dbmeta.name.ColumnRealName;
import org.dbflute.dbmeta.name.ColumnSqlName;
import org.dbflute.exception.ConditionInvokingFailureException;

/**
 * The condition-query as interface.
 * @author jflute
 */
public interface ConditionQuery {

    // ===================================================================================
    //                                                                  Important Accessor
    //                                                                  ==================
    /**
     * Handle the meta as table DB name, that can be identity of table..
     * @return The (fixed) DB name of the table. (NotNull)
     */
    String asTableDbName();

    // internal getter methods start with 'x'
    // not to be same as column names 

    /**
     * Convert to the column real name. (with real alias name) <br>
     * With finding DBMeta.
     * @param columnDbName The DB name of column. (NotNull)
     * @return the column real name. (NotNull)
     */
    ColumnRealName toColumnRealName(String columnDbName);

    /**
     * Convert to the column real name. (with real alias name) <br>
     * Without finding DBMeta.
     * @param columnInfo The information of column. (NotNull)
     * @return the column real name. (NotNull)
     */
    ColumnRealName toColumnRealName(ColumnInfo columnInfo);

    /**
     * Convert to the column SQL name. <br>
     * With finding DBMeta.
     * @param columnDbName The DB name of column. (NotNull)
     * @return the column SQL name. (NotNull)
     */
    ColumnSqlName toColumnSqlName(String columnDbName);

    /**
     * Get the base condition-bean.
     * @return The base condition-bean of this query. (NotNull)
     */
    ConditionBean xgetBaseCB();

    /**
     * Get the base query.
     * @return The condition-query of base table. (NotNull: if this is base query, returns this)
     */
    ConditionQuery xgetBaseQuery();

    /**
     * Get the referrer query.
     * @return The condition-query of referrer table. (NullAllowed: if null, this is base query)
     */
    ConditionQuery xgetReferrerQuery();

    /**
     * Get the SqlClause.
     * @return The instance of SqlClause. (NotNull)
     */
    SqlClause xgetSqlClause();

    /**
     * Get the alias name for this query.
     * @return The alias name for this query. (NotNull)
     */
    String xgetAliasName();

    // nest level is old style
    // so basically unused now 
    /**
     * Get the nest level of relation.
     * @return The nest level of relation.
     */
    int xgetNestLevel();

    /**
     * Get the nest level for next relation.
     * @return The nest level for next relation.
     */
    int xgetNextNestLevel();

    /**
     * Is this a base query?
     * @return The determination, true or false.
     */
    boolean isBaseQuery();

    /**
     * Get the property name of foreign relation.
     * @return The property name of foreign relation. (NotNull)
     */
    String xgetForeignPropertyName();

    /**
     * Get the path of foreign relation. e.g. _0_1
     * @return The path of foreign relation. (NullAllowed: if base query, returns null)
     */
    String xgetRelationPath();

    /**
     * Get the base location of this condition-query.
     * @return The base location of this condition-query. (NotNull)
     */
    String xgetLocationBase();

    // ===================================================================================
    //                                                                 Reflection Invoking
    //                                                                 ===================
    /**
     * Invoke getting value.
     * @param columnFlexibleName The flexible name of the column. (NotNull, NotEmpty)
     * @return The instance of condition-value object. (NotNull)
     * @throws ConditionInvokingFailureException When the method to the column is not found and the method is failed.
     */
    ConditionValue invokeValue(String columnFlexibleName);

    /**
     * Invoke setting query. {RelationResolved} <br>
     * Basically for keys that does not need a condition option. <br>
     * And you should set a list that has fromDate and toDate to conditionValue when DateFromTo,
     * and set null value to it when keys that does not have an argument, e.g. IsNull, then the value is ignored. 
     * @param columnFlexibleName The flexible name of the column allowed to contain relations. (NotNull, NotEmpty)
     * @param conditionKeyName The name of the condition-key. (NotNull)
     * @param conditionValue The value of the condition. (NotNull: as default, NullAllowed: as optional)
     * @throws ConditionInvokingFailureException When the method to the column is not found and the method is failed.
     */
    void invokeQuery(String columnFlexibleName, String conditionKeyName, Object conditionValue);

    /**
     * Invoke setting query with option. {RelationResolved} <br>
     * Basically for LikeSearch, NotLikeSearch, FromTo. <br>
     * And you should set a list that has fromDate and toDate to conditionValue when FromTo,
     * and set null value to it when keys that does not have an argument, e.g. IsNull, then the value is ignored.
     * @param columnFlexibleName The flexible name of the column allowed to contain relations. (NotNull, NotEmpty)
     * @param conditionKeyName The name of the condition-key. (NotNull)
     * @param conditionValue The value of the condition. (NotNull: as default, NullAllowed: as optional)
     * @param conditionOption The option of the condition. (NotNull)
     * @throws ConditionInvokingFailureException When the method to the column is not found and the method is failed.
     */
    void invokeQuery(String columnFlexibleName, String conditionKeyName, Object conditionValue, ConditionOption conditionOption);

    /**
     * Invoke setting query of equal. {RelationResolved}
     * @param columnFlexibleName The flexible name of the column allowed to contain relations. (NotNull, NotEmpty)
     * @param conditionValue The value of the condition 'equal'. (NotNull: as default, NullAllowed: as optional)
     * @throws ConditionInvokingFailureException When the method to the column is not found and the method is failed.
     */
    void invokeQueryEqual(String columnFlexibleName, Object conditionValue);

    /**
     * Invoke setting query of not-equal. {RelationResolved}
     * @param columnFlexibleName The flexible name of the column allowed to contain relations. (NotNull, NotEmpty)
     * @param conditionValue The value of the condition 'notEqual'. (NotNull: as default, NullAllowed: as optional)
     * @throws ConditionInvokingFailureException When the method to the column is not found and the method is failed.
     */
    void invokeQueryNotEqual(String columnFlexibleName, Object conditionValue);

    /**
     * Invoke adding orderBy. {RelationResolved}
     * @param columnFlexibleName The flexible name of the column allowed to contain relations. (NotNull, NotEmpty)
     * @param isAsc Is it ascend?
     * @throws ConditionInvokingFailureException When the method to the column is not found and the method is failed.
     */
    void invokeOrderBy(String columnFlexibleName, boolean isAsc);

    /**
     * Invoke getting foreign condition-query. <br>
     * A method with parameters (using fixed condition) is unsupported.
     * @param foreignPropertyName The property name(s), can contain '.' , of the foreign relation. (NotNull, NotEmpty)
     * @return The conditionQuery of the foreign relation as interface. (NotNull)
     * @throws ConditionInvokingFailureException When the method to the property is not found and the method is failed.
     */
    ConditionQuery invokeForeignCQ(String foreignPropertyName);

    /**
     * Invoke determining foreign condition-query existence?
     * A method with parameters (using fixed condition) is unsupported.
     * @param foreignPropertyName The property name(s), can contain '.' , of the foreign relation. (NotNull, NotEmpty)
     * @return The conditionQuery of the foreign relation as interface. (NotNull)
     * @throws ConditionInvokingFailureException When the method to the property is not found and the method is failed.
     */
    boolean invokeHasForeignCQ(String foreignPropertyName);

    // ===================================================================================
    //                                                                    Option Parameter
    //                                                                    ================
    /**
     * Register the parameter option.
     * @param option The option of parameter. (NullAllowed)
     */
    void xregisterParameterOption(ParameterOption option);
}
