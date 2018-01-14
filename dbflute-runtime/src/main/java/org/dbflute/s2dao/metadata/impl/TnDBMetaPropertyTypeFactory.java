/*
 * Copyright 2014-2018 the original author or authors.
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
package org.dbflute.s2dao.metadata.impl;

import java.util.ArrayList;
import java.util.List;

import org.dbflute.Entity;
import org.dbflute.dbmeta.DBMeta;
import org.dbflute.dbmeta.info.ColumnInfo;
import org.dbflute.dbmeta.name.ColumnSqlName;
import org.dbflute.helper.beans.DfBeanDesc;
import org.dbflute.helper.beans.DfPropertyDesc;
import org.dbflute.jdbc.Classification;
import org.dbflute.s2dao.metadata.TnAbstractPropertyTypeFactory;
import org.dbflute.s2dao.metadata.TnBeanAnnotationReader;
import org.dbflute.s2dao.metadata.TnPropertyType;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public class TnDBMetaPropertyTypeFactory extends TnAbstractPropertyTypeFactory {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected DBMeta _dbmeta;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TnDBMetaPropertyTypeFactory(Class<?> beanClass, TnBeanAnnotationReader beanAnnotationReader) {
        super(beanClass, beanAnnotationReader);
        initializeResources();
    }

    // -----------------------------------------------------
    //                                Initialization Support
    //                                ----------------------
    protected void initializeResources() {
        if (isEntity()) {
            _dbmeta = findDBMeta();
        }
    }

    protected boolean isEntity() {
        return Entity.class.isAssignableFrom(_beanClass);
    }

    protected DBMeta findDBMeta() {
        try {
            final Entity entity = (Entity) _beanClass.newInstance();
            return entity.asDBMeta();
        } catch (Exception e) {
            String msg = "beanClass.newInstance() threw the exception: beanClass=" + _beanClass;
            throw new IllegalStateException(msg, e);
        }
    }

    // ===================================================================================
    //                                                                      Implementation
    //                                                                      ==============
    public TnPropertyType[] createBeanPropertyTypes() {
        final List<TnPropertyType> list = new ArrayList<TnPropertyType>();
        final DfBeanDesc beanDesc = getBeanDesc();
        final List<String> proppertyNameList = beanDesc.getProppertyNameList();
        for (String proppertyName : proppertyNameList) {
            final DfPropertyDesc pd = beanDesc.getPropertyDesc(proppertyName);

            // read-only property (that is NOT column) is unnecessary!
            if (!pd.isWritable()) {
                // If the property is treated as column, a writer method may be unnecessary.
                // For example, target column of classification setting forced
                // is set by classification writer method.
                if (!isColumn(pd)) {
                    continue;
                }
            }

            // classification property is unnecessary!
            // (because native type is valid)
            if (isClassification(pd)) {
                continue;
            }

            // relation property is unnecessary!
            // (because a relation mapping is other process)
            if (isRelation(pd)) {
                continue;
            }

            final TnPropertyType pt = createPropertyType(pd);
            pt.setPrimaryKey(isPrimaryKey(pd));
            pt.setPersistent(isPersistent(pt));
            list.add(pt);
        }
        return list.toArray(new TnPropertyType[list.size()]);
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected boolean isColumn(DfPropertyDesc propertyDesc) {
        // no DBMeta means the property is NOT column (for example, derived-column)
        return hasDBMeta() ? _dbmeta.hasColumn(propertyDesc.getPropertyName()) : false;
    }

    protected boolean isClassification(DfPropertyDesc propertyDesc) {
        return Classification.class.isAssignableFrom(propertyDesc.getPropertyType());
    }

    protected boolean isRelation(DfPropertyDesc propertyDesc) {
        final String propertyName = propertyDesc.getPropertyName();
        if (hasDBMeta() && (_dbmeta.hasForeign(propertyName) || _dbmeta.hasReferrer(propertyName))) {
            return true;
        }
        return hasRelationNoAnnotation(propertyDesc);
    }

    protected boolean hasRelationNoAnnotation(DfPropertyDesc propertyDesc) {
        return _beanAnnotationReader.hasRelationNo(propertyDesc);
    }

    protected boolean isPrimaryKey(DfPropertyDesc propertyDesc) {
        final String propertyName = propertyDesc.getPropertyName();
        if (hasDBMeta() && _dbmeta.hasPrimaryKey() && _dbmeta.hasColumn(propertyName)) {
            if (_dbmeta.findColumnInfo(propertyName).isPrimary()) {
                return true;
            }
        }
        return hasIdAnnotation(propertyDesc);
    }

    protected boolean hasIdAnnotation(DfPropertyDesc propertyDesc) {
        return _beanAnnotationReader.getId(propertyDesc) != null;
    }

    protected boolean isPersistent(TnPropertyType propertyType) {
        final String propertyName = propertyType.getPropertyName();
        final DfPropertyDesc propertyDesc = propertyType.getPropertyDesc();
        return (hasDBMeta() && _dbmeta.hasColumn(propertyName)) || hasColumnAnnotation(propertyDesc);
    }

    protected boolean hasColumnAnnotation(DfPropertyDesc propertyDesc) {
        return _beanAnnotationReader.getColumnAnnotation(propertyDesc) != null;
    }

    protected boolean hasDBMeta() {
        return _dbmeta != null;
    }

    @Override
    protected ColumnSqlName getColumnSqlName(String columnDbName) {
        if (hasDBMeta() && _dbmeta.hasColumn(columnDbName)) {
            return _dbmeta.findColumnInfo(columnDbName).getColumnSqlName();
        }
        return new ColumnSqlName(columnDbName);
    }

    @Override
    protected ColumnInfo getEntityColumnInfo(String columnDbName) {
        if (hasDBMeta() && _dbmeta.hasColumn(columnDbName)) {
            return _dbmeta.findColumnInfo(columnDbName);
        }
        return null;
    }
}
