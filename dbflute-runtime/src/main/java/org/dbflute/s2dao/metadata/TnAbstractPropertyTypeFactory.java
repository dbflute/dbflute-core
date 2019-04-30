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
package org.dbflute.s2dao.metadata;

import org.dbflute.dbmeta.info.ColumnInfo;
import org.dbflute.dbmeta.name.ColumnSqlName;
import org.dbflute.exception.PluginValueTypeNotFoundException;
import org.dbflute.helper.beans.DfBeanDesc;
import org.dbflute.helper.beans.DfPropertyDesc;
import org.dbflute.helper.beans.factory.DfBeanDescFactory;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.jdbc.ValueType;
import org.dbflute.s2dao.metadata.impl.TnPropertyTypeImpl;
import org.dbflute.s2dao.valuetype.TnValueTypes;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public abstract class TnAbstractPropertyTypeFactory implements TnPropertyTypeFactory {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Class<?> _beanClass;
    protected final TnBeanAnnotationReader _beanAnnotationReader;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TnAbstractPropertyTypeFactory(Class<?> beanClass, TnBeanAnnotationReader beanAnnotationReader) {
        _beanClass = beanClass;
        _beanAnnotationReader = beanAnnotationReader;
    }

    // ===================================================================================
    //                                                                     Property Helper
    //                                                                     ===============
    protected DfBeanDesc getBeanDesc() {
        return DfBeanDescFactory.getBeanDesc(_beanClass);
    }

    protected TnPropertyType createPropertyType(DfPropertyDesc propertyDesc) {
        final ValueType valueType = getValueType(propertyDesc);
        final String columnDbName = getColumnDbName(propertyDesc);
        final ColumnSqlName columnSqlName = getColumnSqlName(columnDbName);
        final ColumnInfo entityColumnInfo = getEntityColumnInfo(columnDbName);
        return new TnPropertyTypeImpl(propertyDesc, valueType, columnDbName, columnSqlName, entityColumnInfo);
    }

    protected ValueType getValueType(DfPropertyDesc propertyDesc) {
        final String propertyName = propertyDesc.getPropertyName();
        final Class<?> propertyType = propertyDesc.getPropertyType();
        final String keyName = _beanAnnotationReader.getValueType(propertyDesc);
        if (keyName != null) {
            return findValueTypeByName(propertyName, propertyType, keyName);
        }
        return TnValueTypes.getValueType(propertyType);
    }

    protected String getColumnDbName(DfPropertyDesc propertyDesc) {
        final String propertyName = propertyDesc.getPropertyName();
        final String name = _beanAnnotationReader.getColumnAnnotation(propertyDesc);
        return name != null ? name : propertyName;
    }

    protected abstract ColumnSqlName getColumnSqlName(String columnDbName);

    protected abstract ColumnInfo getEntityColumnInfo(String columnDbName);

    protected ValueType findValueTypeByName(String propertyName, Class<?> propertyType, String keyName) {
        final ValueType valueType = TnValueTypes.getPluginValueType(keyName);
        if (valueType != null) {
            return valueType;
        }
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the plug-in value type by the name.");
        br.addItem("Bean Type");
        br.addElement(_beanClass.getName());
        br.addItem("Property");
        br.addElement(propertyName);
        br.addElement(propertyType.getName());
        br.addItem("Key Name");
        br.addElement(keyName);
        final String msg = br.buildExceptionMessage();
        throw new PluginValueTypeNotFoundException(msg);
    }
}
