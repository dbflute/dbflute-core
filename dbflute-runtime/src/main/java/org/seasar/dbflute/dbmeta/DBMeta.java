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
package org.seasar.dbflute.dbmeta;

import java.util.List;
import java.util.Map;

import org.seasar.dbflute.DBDef;
import org.seasar.dbflute.Entity;
import org.seasar.dbflute.dbmeta.info.ColumnInfo;
import org.seasar.dbflute.dbmeta.info.ForeignInfo;
import org.seasar.dbflute.dbmeta.info.ReferrerInfo;
import org.seasar.dbflute.dbmeta.info.RelationInfo;
import org.seasar.dbflute.dbmeta.info.UniqueInfo;
import org.seasar.dbflute.dbmeta.name.TableSqlName;

/**
 * The interface of DB meta for one table.
 * @author jflute
 */
public interface DBMeta {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The mark of map string. */
    public static final String MAP_STRING_MAP_MARK = "map:";

    /** The mark of list string. */
    public static final String MAP_STRING_LIST_MARK = "list:";

    /** The mark of start brace. */
    public static final String MAP_STRING_START_BRACE = "@{";

    /** The mark of end brace. */
    public static final String MAP_STRING_END_BRACE = "@}";

    /** The mark of delimiter. */
    public static final String MAP_STRING_DELIMITER = "@;";

    /** The mark of equal. */
    public static final String MAP_STRING_EQUAL = "@=";

    // ===================================================================================
    //                                                                               DBDef
    //                                                                               =====
    /**
     * Get the current DB definition.
     * @return The current DB definition. (NotNull)
     */
    DBDef getCurrentDBDef();

    // ===================================================================================
    //                                                                    Property Gateway
    //                                                                    ================
    // these fields and methods should be defined before definitions of column info at implementation classes
    /**
     * Find the property gateway of the entity for the column. <br />
     * @param propertyName The property name of the column as case insensitive for performance. (NotNull)
     * @return The instance of the property gateway. (NullAllowed: if not found, returns null)
     */
    PropertyGateway findPropertyGateway(String propertyName);

    /**
     * Find the foreign property gateway of the entity for the relation. <br />
     * @param foreignPropertyName The property name of the relation as case insensitive for performance. (NotNull)
     * @return The instance of the property gateway. (NullAllowed: if not found, returns null)
     */
    PropertyGateway findForeignPropertyGateway(String foreignPropertyName);

    // ===================================================================================
    //                                                                          Table Info
    //                                                                          ==========
    /**
     * Get the DB name of the table.
     * @return The DB name of the table. (NotNull)
     */
    String getTableDbName();

    /**
     * Get the property name(JavaBeansRule) of table.
     * @return The property name(JavaBeansRule) of table. (NotNull)
     */
    String getTablePropertyName();

    /**
     * Get the SQL name of table.
     * @return The SQL name of table. (NotNull)
     */
    TableSqlName getTableSqlName();

    /**
     * Get the alias of the table.
     * @return The alias of the table. (NullAllowed: when it cannot get an alias from meta)
     */
    String getTableAlias();

    /**
     * Get the comment of the table. <br />
     * If the real comment contains the alias,
     * this result does NOT contain it and its delimiter.  
     * @return The comment of the table. (NullAllowed: when it cannot get a comment from meta)
     */
    String getTableComment();

    // ===================================================================================
    //                                                                         Column Info
    //                                                                         ===========
    /**
     * Does this table have the corresponding column?
     * @param columnFlexibleName The flexible name of the column. (NotNull)
     * @return The determination, true or false.
     */
    boolean hasColumn(String columnFlexibleName);

    /**
     * Find the information of the column by the flexible name of the column.
     * <pre>
     * If the table name is 'BOOK_ID', you can find the dbmeta by ...(as follows)
     *     'BOOK_ID', 'BOok_iD', 'book_id'
     *     , 'BookId', 'bookid', 'bOoKiD'
     * </pre>
     * @param columnFlexibleName The flexible name of the column. (NotNull)
     * @return The information of the column. (NotNull)
     */
    ColumnInfo findColumnInfo(String columnFlexibleName);

    /**
     * Get the list of column information.
     * @return The list of column information. (NotNull, NotEmpty)
     */
    List<ColumnInfo> getColumnInfoList();

    // ===================================================================================
    //                                                                         Unique Info
    //                                                                         ===========
    /**
     * Get primary unique info that means unique info for primary key. <br />
     * If this table does not have primary-key, this method throws UnsupportedOperationException.
     * @return Primary unique info. (NotNull)
     */
    UniqueInfo getPrimaryUniqueInfo();

    /**
     * Does this table have primary-key?
     * @return The determination, true or false.
     */
    boolean hasPrimaryKey();

    /**
     * Does this table have compound primary-key? <br />
     * If this table does not have primary-key in the first place,
     * this method returns false. 
     * @return The determination, true or false.
     */
    boolean hasCompoundPrimaryKey();

    // ===================================================================================
    //                                                                       Relation Info
    //                                                                       =============
    // -----------------------------------------------------
    //                                      Relation Element
    //                                      ----------------
    /**
     * Find the information of relation.
     * @param relationPropertyName The flexible name of the relation property. (NotNull)
     * @return The information object of relation. (NotNull)
     * @exception org.seasar.dbflute.exception.DBMetaNotFoundException When the corresponding relation info was not found.
     */
    RelationInfo findRelationInfo(String relationPropertyName);

    // -----------------------------------------------------
    //                                       Foreign Element
    //                                       ---------------
    /**
     * Does this table have the corresponding foreign relation?
     * @param foreignPropertyName The flexible name of the foreign property. (NotNull)
     * @return The determination, true or false. (NotNull)
     */
    boolean hasForeign(String foreignPropertyName);

    /**
     * Find the DB meta of foreign relation.
     * @param foreignPropertyName The flexible name of the foreign property. (NotNull)
     * @return The DB meta of foreign relation. (NotNull)
     * @exception org.seasar.dbflute.exception.DBMetaNotFoundException When the corresponding foreign info was not found.
     */
    DBMeta findForeignDBMeta(String foreignPropertyName);

    /**
     * Find the information of foreign relation by property name.
     * @param foreignPropertyName The flexible name of the foreign property. (NotNull)
     * @return The information object of foreign relation. (NotNull)
     * @exception org.seasar.dbflute.exception.DBMetaNotFoundException When the corresponding foreign info was not found.
     */
    ForeignInfo findForeignInfo(String foreignPropertyName);

    /**
     * Find the information of foreign relation by relation number.
     * @param relationNo The relation number of the foreign property. (NotNull)
     * @return The information object of foreign relation. (NotNull)
     * @exception org.seasar.dbflute.exception.DBMetaNotFoundException When the corresponding foreign info was not found.
     */
    ForeignInfo findForeignInfo(int relationNo);

    /**
     * Get the list of foreign info.
     * @return The list of foreign info. (NotNull)
     */
    List<ForeignInfo> getForeignInfoList();

    // -----------------------------------------------------
    //                                      Referrer Element
    //                                      ----------------
    /**
     * Does this table have the corresponding referrer relation?
     * @param referrerPropertyName The flexible name of the referrer property. (NotNull)
     * @return The determination, true or false. (NotNull)
     */
    boolean hasReferrer(String referrerPropertyName);

    /**
     * Find the DB meta of referrer relation.
     * @param referrerPropertyName The flexible name of the referrer property. (NotNull)
     * @return The DB meta of referrer relation. (NotNull)
     */
    DBMeta findReferrerDBMeta(String referrerPropertyName);

    /**
     * Find the information of referrer relation.
     * @param referrerPropertyName The flexible name of the referrer property. (NotNull)
     * @return The information object of referrer relation. (NotNull)
     * @exception org.seasar.dbflute.exception.DBMetaNotFoundException When the corresponding referrer info was not found.
     */
    ReferrerInfo findReferrerInfo(String referrerPropertyName);

    /**
     * Get the list of referrer info.
     * @return The list of referrer info. (NotNull)
     */
    List<ReferrerInfo> getReferrerInfoList();

    // -----------------------------------------------------
    //                                        Relation Trace
    //                                        --------------
    /**
     * Relation trace.
     */
    public static interface RelationTrace { // #later remove this since Java8

        /**
         * Get the trace of relation.
         * @return The trace of relation as the list of relation info. (NotNull)
         */
        List<RelationInfo> getTraceRelation();

        /**
         * Get the trace of column.
         * @return The trace of column as column info. (NullAllowed)
         */
        ColumnInfo getTraceColumn();
    }

    public static interface RelationTraceFixHandler {
        void handleFixedTrace(RelationTrace relationTrace);
    }

    // ===================================================================================
    //                                                                       Identity Info
    //                                                                       =============
    /**
     * Does this table have identity?
     * @return The determination, true or false.
     */
    boolean hasIdentity();

    // ===================================================================================
    //                                                                       Sequence Info
    //                                                                       =============
    /**
     * Does this table have sequence?
     * @return The determination, true or false.
     */
    boolean hasSequence();

    /**
     * Get the sequence name.
     * @return The sequence name. (NullAllowed: if no sequence, returns null.)
     */
    String getSequenceName();

    /**
     * Get the SQL for next value of sequence.
     * @return The SQL for next value of sequence. (NullAllowed: if no sequence, returns null.)
     */
    String getSequenceNextValSql();

    /**
     * Get the increment size of sequence.
     * @return The increment size of sequence. (NullAllowed: if unknown, returns null.)
     */
    Integer getSequenceIncrementSize();

    /**
     * Get the cache size of sequence. (The cache means sequence cache on DBFlute)
     * @return The cache size of sequence. (NullAllowed: if no cache, returns null.)
     */
    Integer getSequenceCacheSize();

    // ===================================================================================
    //                                                                 OptimisticLock Info
    //                                                                 ===================
    /**
     * Does the table have optimistic lock? (basically has version no or update date?)
     * @return The determination, true or false.
     */
    boolean hasOptimisticLock();

    /**
     * Does the table have a column for version no?
     * @return The determination, true or false.
     */
    boolean hasVersionNo();

    /**
     * Get the column information of version no.
     * @return The column information of version no. (NullAllowed: if no column, return null)
     */
    ColumnInfo getVersionNoColumnInfo();

    /**
     * Does the table have a column for update date?
     * @return The determination, true or false.
     */
    boolean hasUpdateDate();

    /**
     * Get the column information of update date.
     * @return The column information of update date. (NullAllowed: if no column, return null)
     */
    ColumnInfo getUpdateDateColumnInfo();

    // ===================================================================================
    //                                                                   CommonColumn Info
    //                                                                   =================
    /**
     * Does the table have common columns?
     * @return The determination, true or false.
     */
    boolean hasCommonColumn();

    /**
     * Get the list of common column.
     * @return The list of column info. (NotNull)
     */
    List<ColumnInfo> getCommonColumnInfoList();

    /**
     * Get the list of common column auto-setup before insert.
     * @return The list of column info. (NotNull)
     */
    List<ColumnInfo> getCommonColumnInfoBeforeInsertList();

    /**
     * Get the list of common column auto-setup before update.
     * @return The list of column info. (NotNull)
     */
    List<ColumnInfo> getCommonColumnInfoBeforeUpdateList();

    // ===================================================================================
    //                                                                       Name Handling
    //                                                                       =============
    /**
     * Does the table have an object for the flexible name? {Target objects are TABLE and COLUMN}
     * @param flexibleName The flexible name of the object. (NotNull)
     * @return The determination, true or false.
     */
    boolean hasFlexibleName(String flexibleName); // #later remove this since Java8

    /**
     * Find DB name by flexible name. {Target objects are TABLE and COLUMN}
     * @param flexibleName The flexible name of the object. (NotNull)
     * @return The DB name of anything. (NotNull)
     * @exception org.seasar.dbflute.exception.DBMetaNotFoundException When the corresponding name was not found.
     */
    String findDbName(String flexibleName); // #later remove this since Java8

    /**
     * Find property name(JavaBeansRule) by flexible name. {Target objects are TABLE and COLUMN}
     * @param flexibleName The flexible name of the property. (NotNull)
     * @return The DB name of anything. (NotNull)
     * @exception org.seasar.dbflute.exception.DBMetaNotFoundException When the corresponding name was not found.
     */
    String findPropertyName(String flexibleName); // #later remove this since Java8

    // ===================================================================================
    //                                                                           Type Name
    //                                                                           =========
    /**
     * Get the type name of entity.
     * @return The type name of entity. (NotNull)
     */
    String getEntityTypeName();

    /**
     * Get the type name of condition-bean.
     * @return The type name of condition-bean. (NullAllowed: if the condition-bean does not exist)
     */
    String getConditionBeanTypeName();

    /**
     * Get the type name of behavior.
     * @return The type name of behavior. (NullAllowed: if the behavior does not exist)
     */
    String getBehaviorTypeName();

    // ===================================================================================
    //                                                                         Object Type
    //                                                                         ===========
    /**
     * Get the type of entity.
     * @return The type of entity. (NotNull)
     */
    Class<? extends Entity> getEntityType();

    // ===================================================================================
    //                                                                     Object Instance
    //                                                                     ===============
    /**
     * New the instance of entity.
     * @return The instance of entity. (NotNull)
     */
    Entity newEntity();

    // ===================================================================================
    //                                                                   Map Communication
    //                                                                   =================
    // -----------------------------------------------------
    //                                                Accept
    //                                                ------
    /**
     * Accept the map of primary-keys. map:{[column-name] = [value]}
     * @param entity The instance of entity to accept the map data. (NotNull)
     * @param primaryKeyMap The value map of primary-keys. (NotNull, NotEmpty)
     */
    void acceptPrimaryKeyMap(Entity entity, Map<String, ? extends Object> primaryKeyMap);

    /**
     * Accept the map of all columns. map:{[column-name] = [value]}<br />
     * Derived columns are not accepted, physical columns only.
     * @param entity The instance of entity to accept the map data. (NotNull)
     * @param allColumnMap The value map of all columns. (NotNull, NotEmpty)
     */
    void acceptAllColumnMap(Entity entity, Map<String, ? extends Object> allColumnMap);

    // -----------------------------------------------------
    //                                               Extract
    //                                               -------
    /**
     * Extract the map of primary-keys. map:{[column-name] = [value]}
     * @param entity The instance of entity to extract the data. (NotNull)
     * @return The value map of primary-keys. (NotNull)
     */
    Map<String, Object> extractPrimaryKeyMap(Entity entity);

    /**
     * Extract The map of all columns. map:{[column-name] = [value]}<br />
     * Derived columns are not extracted, physical columns only.
     * @param entity The instance of entity to extract the data. (NotNull)
     * @return The map of all columns. (NotNull)
     */
    Map<String, Object> extractAllColumnMap(Entity entity);

    // ===================================================================================
    //                                                                Optimistic Lock Type
    //                                                                ====================
    public static enum OptimisticLockType {
        NONE, VERSION_NO, UPDATE_DATE
    }
}
