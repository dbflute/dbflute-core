/*
 * Copyright 2014-2020 the original author or authors.
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
package org.dbflute.mock;

import java.util.List;
import java.util.Map;

import org.dbflute.Entity;
import org.dbflute.dbmeta.AbstractDBMeta;
import org.dbflute.dbmeta.DBMeta;
import org.dbflute.dbmeta.info.ColumnInfo;
import org.dbflute.dbmeta.info.ForeignInfo;
import org.dbflute.dbmeta.info.ReferrerInfo;
import org.dbflute.dbmeta.info.RelationInfo;
import org.dbflute.dbmeta.info.UniqueInfo;
import org.dbflute.dbmeta.name.TableSqlName;
import org.dbflute.dbmeta.property.PropertyGateway;
import org.dbflute.dbway.DBDef;

/**
 * @author jflute
 */
public class MockDBMeta extends AbstractDBMeta {

    public String getProjectName() {
        return null;
    }

    public String getProjectPrefix() {
        return null;
    }

    public String getGenerationGapBasePrefix() {
        return null;
    }

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

    public String getTableDispName() {
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
