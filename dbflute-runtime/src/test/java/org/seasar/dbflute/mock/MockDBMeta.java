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
package org.seasar.dbflute.mock;

import java.util.List;
import java.util.Map;

import org.seasar.dbflute.DBDef;
import org.seasar.dbflute.Entity;
import org.seasar.dbflute.dbmeta.AbstractDBMeta;
import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.dbmeta.PropertyGateway;
import org.seasar.dbflute.dbmeta.info.ColumnInfo;
import org.seasar.dbflute.dbmeta.info.ForeignInfo;
import org.seasar.dbflute.dbmeta.info.ReferrerInfo;
import org.seasar.dbflute.dbmeta.info.RelationInfo;
import org.seasar.dbflute.dbmeta.info.UniqueInfo;
import org.seasar.dbflute.dbmeta.name.TableSqlName;

/**
 * @author jflute
 */
public class MockDBMeta extends AbstractDBMeta {

    public DBDef getCurrentDBDef() {
        return null;
    }

    public String findDbName(String flexibleName) {
        return null;
    }

    public DBMeta findForeignDBMeta(String foreignPropName) {
        return null;
    }

    public ForeignInfo findForeignInfo(String foreignPropName) {
        return null;
    }

    public String findPropertyName(String flexibleName) {
        return null;
    }

    public DBMeta findReferrerDBMeta(String referrerPropertyName) {
        return null;
    }

    public ReferrerInfo findReferrerInfo(String referrerPropertyName) {
        return null;
    }

    protected Class<?> getReferrerPropertyListType() {
        return null;
    }

    public RelationInfo findRelationInfo(String relationPropertyName) {
        return null;
    }

    public String getBehaviorTypeName() {
        return null;
    }

    public List<ColumnInfo> getColumnInfoList() {
        return null;
    }

    public String getConditionBeanTypeName() {
        return null;
    }

    public String getDaoTypeName() {
        return null;
    }

    public Class<? extends Entity> getEntityType() {
        return null;
    }

    public String getEntityTypeName() {
        return null;
    }

    public UniqueInfo getPrimaryUniqueInfo() {
        return null;
    }

    public String getTableDbName() {
        return null;
    }

    public String getTablePropertyName() {
        return null;
    }

    public TableSqlName getTableSqlName() {
        return null;
    }

    public boolean hasPrimaryKey() {
        return false;
    }

    public boolean hasCompoundPrimaryKey() {
        return false;
    }

    public Entity newEntity() {
        return null;
    }

    @Override
    protected List<ColumnInfo> ccil() {
        return null;
    }

    @Override
    protected UniqueInfo cpui() {
        return null;
    }

    public void acceptPrimaryKeyMap(Entity entity, Map<String, ? extends Object> primaryKeyMap) {
    }

    public void acceptAllColumnMap(Entity entity, Map<String, ? extends Object> allColumnMap) {
    }

    public Map<String, Object> extractPrimaryKeyMap(Entity entity) {
        return null;
    }

    public Map<String, Object> extractAllColumnMap(Entity entity) {
        return null;
    }

    public PropertyGateway findPropertyGateway(String propertyName) {
        return null;
    }

    public PropertyGateway findForeignPropertyGateway(String foreignPropertyName) {
        return null;
    }
}
