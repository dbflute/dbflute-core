/*
 * Copyright 2014-2024 the original author or authors.
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
package org.dbflute.logic.jdbc.metadata.info;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.dbflute.logic.jdbc.metadata.comment.DfDbCommentExtractor.UserColComments;
import org.dbflute.system.DBFluteSystem;

/**
 * @author jflute
 */
public class DfSynonymMeta {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected UnifiedSchema _synonymOwner;
    protected String _synonymName;
    protected UnifiedSchema _tableOwner;
    protected String _tableName;
    protected DfPrimaryKeyMeta _primaryKey;
    protected boolean _autoIncrement;
    protected Map<String, Map<Integer, String>> _uniqueKeyMap;
    protected Map<String, DfForeignKeyMeta> _foreignKeyMap;
    protected Map<String, Map<Integer, String>> _indexMap;
    protected String _dbLinkName;
    protected List<DfColumnMeta> _columnMetaList;
    protected boolean _selectable;
    protected String _tableComment;
    protected Map<String, UserColComments> _columnCommentMap;

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    public boolean isDBLink() {
        return _dbLinkName != null;
    }

    public boolean hasTableComment() {
        return _tableComment != null && _tableComment.trim().length() > 0;
    }

    public boolean hasColumnCommentMap() {
        return _columnCommentMap != null && !_columnCommentMap.isEmpty();
    }

    // ===================================================================================
    //                                                                       Name Building
    //                                                                       =============
    public String buildSynonymFullQualifiedName() {
        return _synonymOwner.buildFullQualifiedName(_synonymName);
    }

    public String buildSynonymSchemaQualifiedName() {
        return _synonymOwner.buildSchemaQualifiedName(_synonymName);
    }

    public String buildSynonymSqlName() {
        return _synonymOwner.buildSqlName(_synonymName);
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        String comment = "";
        if (_tableComment != null) {
            final String ln = DBFluteSystem.ln();
            final int indexOf = _tableComment.indexOf(ln);
            if (indexOf > 0) { // not contain 0 because ignore first line separator
                comment = _tableComment.substring(0, indexOf) + "...";
            } else {
                comment = _tableComment;
            }
        }
        String columns = "";
        if (_columnMetaList != null) {
            columns = "(" + _columnMetaList.size() + " columns for DB link)";
        }
        final String synonymSchema = _synonymOwner != null ? _synonymOwner.getPureSchema() : "";
        final String tableSchema = _tableOwner != null ? _tableOwner.getPureSchema() : "";
        return synonymSchema + "." + _synonymName + ":{" + (_dbLinkName != null ? _dbLinkName : tableSchema) + "." + _tableName + columns
                + ", PK=" + _primaryKey + (_autoIncrement ? ", ID" : "") + ", "
                + (_uniqueKeyMap != null ? "UQ=" + _uniqueKeyMap.size() : null) + ", "
                + (_foreignKeyMap != null ? "FK=" + _foreignKeyMap.size() : null) + ", " + (_selectable ? "selectable" : "unselectable")
                + "}" + ((comment != null && comment.trim().length() > 0) ? " // " + comment : "");
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public UnifiedSchema getSynonymOwner() {
        return _synonymOwner;
    }

    public void setSynonymOwner(UnifiedSchema synonymOwner) {
        _synonymOwner = synonymOwner;
    }

    public String getSynonymName() {
        return _synonymName;
    }

    public void setSynonymName(String synonymName) {
        _synonymName = synonymName;
    }

    public UnifiedSchema getTableOwner() {
        return _tableOwner;
    }

    public void setTableOwner(UnifiedSchema tableOwner) {
        _tableOwner = tableOwner;
    }

    public String getTableName() {
        return _tableName;
    }

    public void setTableName(String tableName) {
        _tableName = tableName;
    }

    public DfPrimaryKeyMeta getPrimaryKey() {
        return _primaryKey;
    }

    public void setPrimaryKey(DfPrimaryKeyMeta primaryKey) {
        _primaryKey = primaryKey;
    }

    public boolean isAutoIncrement() {
        return _autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        _autoIncrement = autoIncrement;
    }

    public Map<String, Map<Integer, String>> getUniqueKeyMap() {
        return _uniqueKeyMap != null ? _uniqueKeyMap : new HashMap<String, Map<Integer, String>>();
    }

    public void setUniqueKeyMap(Map<String, Map<Integer, String>> uniqueKeyMap) {
        _uniqueKeyMap = uniqueKeyMap;
    }

    public Map<String, DfForeignKeyMeta> getForeignKeyMap() {
        return _foreignKeyMap != null ? _foreignKeyMap : new HashMap<String, DfForeignKeyMeta>();
    }

    public void setForeignKeyMap(Map<String, DfForeignKeyMeta> foreignKeyMap) {
        _foreignKeyMap = foreignKeyMap;
    }

    public Map<String, Map<Integer, String>> getIndexMap() {
        return _indexMap != null ? _indexMap : new HashMap<String, Map<Integer, String>>();
    }

    public void setIndexMap(Map<String, Map<Integer, String>> indexMap) {
        _indexMap = indexMap;
    }

    public String getDBLinkName() {
        return _dbLinkName;
    }

    public void setDBLinkName(String dbLinkName) {
        _dbLinkName = dbLinkName;
    }

    public List<DfColumnMeta> getColumnMetaList() {
        return _columnMetaList != null ? _columnMetaList : new ArrayList<DfColumnMeta>();
    }

    public void setColumnMetaList(List<DfColumnMeta> columnMetaList4DBLink) {
        _columnMetaList = columnMetaList4DBLink;
    }

    public boolean isSelectable() {
        return _selectable;
    }

    public void setSelectable(boolean selectable) {
        _selectable = selectable;
    }

    public String getTableComment() {
        return _tableComment;
    }

    public void setTableComment(String tableComment) {
        _tableComment = tableComment;
    }

    public Map<String, UserColComments> getColumnCommentMap() {
        return _columnCommentMap;
    }

    public void setColumnCommentMap(Map<String, UserColComments> columnCommentMap) {
        _columnCommentMap = columnCommentMap;
    }
}
