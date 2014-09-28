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
package org.seasar.dbflute.s2dao.metadata.impl;

import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.List;

import org.seasar.dbflute.helper.beans.DfBeanDesc;
import org.seasar.dbflute.helper.beans.DfPropertyDesc;
import org.seasar.dbflute.helper.beans.factory.DfBeanDescFactory;
import org.seasar.dbflute.s2dao.metadata.TnBeanAnnotationReader;
import org.seasar.dbflute.s2dao.metadata.TnBeanMetaData;
import org.seasar.dbflute.s2dao.metadata.TnBeanMetaDataFactory;
import org.seasar.dbflute.s2dao.metadata.TnRelationPropertyType;
import org.seasar.dbflute.s2dao.metadata.TnRelationPropertyTypeFactory;
import org.seasar.dbflute.util.Srl;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public class TnRelationPropertyTypeFactoryImpl implements TnRelationPropertyTypeFactory {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The default capacity of relation size for relation property list. */
    protected static final int RELATION_SIZE_CAPACITY = 8; // on feel, almost less 8

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Class<?> _localBeanClass;
    protected final TnBeanMetaData _localBeanMetaData;
    protected final TnBeanAnnotationReader _beanAnnotationReader;
    protected final TnBeanMetaDataFactory _beanMetaDataFactory;
    protected final DatabaseMetaData _dbMetaData;
    protected final int _relationNestLevel;
    protected final boolean _stopRelationCreation;
    protected final Class<?> _optionalEntityType;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TnRelationPropertyTypeFactoryImpl(Class<?> localBeanClass, TnBeanMetaData localBeanMetaData,
            TnBeanAnnotationReader beanAnnotationReader, TnBeanMetaDataFactory beanMetaDataFactory,
            DatabaseMetaData dbMetaData, int relationNestLevel, boolean stopRelationCreation,
            Class<?> optionalEntityType) {
        _localBeanClass = localBeanClass;
        _localBeanMetaData = localBeanMetaData;
        _beanAnnotationReader = beanAnnotationReader;
        _beanMetaDataFactory = beanMetaDataFactory;
        _dbMetaData = dbMetaData;
        _relationNestLevel = relationNestLevel;
        _stopRelationCreation = stopRelationCreation;
        _optionalEntityType = optionalEntityType;
    }

    // ===================================================================================
    //                                                                     Create Relation
    //                                                                     ===============
    public TnRelationPropertyType[] createRelationPropertyTypes() {
        final List<TnRelationPropertyType> relList = new ArrayList<TnRelationPropertyType>(RELATION_SIZE_CAPACITY);
        final DfBeanDesc localBeanDesc = getLocalBeanDesc();
        final List<String> proppertyNameList = localBeanDesc.getProppertyNameList();
        for (String proppertyName : proppertyNameList) {
            final DfPropertyDesc propertyDesc = localBeanDesc.getPropertyDesc(proppertyName);
            if (_stopRelationCreation || !isRelationProperty(propertyDesc)) {
                continue;
            }
            relList.add(createRelationPropertyType(propertyDesc));
        }
        return relList.toArray(new TnRelationPropertyType[relList.size()]);
    }

    protected DfBeanDesc getLocalBeanDesc() {
        return DfBeanDescFactory.getBeanDesc(_localBeanClass);
    }

    protected boolean isRelationProperty(DfPropertyDesc propertyDesc) {
        return _beanAnnotationReader.hasRelationNo(propertyDesc);
    }

    protected TnRelationPropertyType createRelationPropertyType(DfPropertyDesc propertyDesc) {
        final String[] myKeys;
        final String[] yourKeys;
        final int relno = _beanAnnotationReader.getRelationNo(propertyDesc);
        final String relkeys = _beanAnnotationReader.getRelationKey(propertyDesc);
        if (relkeys != null) {
            final List<String> tokenList = Srl.splitListTrimmed(relkeys, ",");
            final List<String> myKeyList = new ArrayList<String>(tokenList.size());
            final List<String> yourKeyList = new ArrayList<String>(tokenList.size());
            for (String token : tokenList) {
                final int index = token.indexOf(':');
                if (index > 0) {
                    myKeyList.add(token.substring(0, index));
                    yourKeyList.add(token.substring(index + 1));
                } else {
                    myKeyList.add(token);
                    yourKeyList.add(token);
                }
            }
            myKeys = (String[]) myKeyList.toArray(new String[myKeyList.size()]);
            yourKeys = (String[]) yourKeyList.toArray(new String[yourKeyList.size()]);
        } else { // basically no way at least on DBFlute
            myKeys = new String[0];
            yourKeys = new String[0];
        }
        final Class<?> propertyType = chooseAnalyzedPropertyType(propertyDesc);
        final TnBeanMetaData relationBeanMetaData = createRelationBeanMetaData(propertyType);
        return createRelationPropertyType(propertyDesc, myKeys, yourKeys, relno, relationBeanMetaData);
    }

    protected Class<?> chooseAnalyzedPropertyType(DfPropertyDesc propertyDesc) {
        final Class<?> propertyType = propertyDesc.getPropertyType();
        if (_optionalEntityType.isAssignableFrom(propertyType)) {
            final Class<?> genericType = propertyDesc.getGenericType();
            if (genericType != null) {
                return genericType;
            }
        }
        return propertyType;
    }

    protected TnRelationPropertyType createRelationPropertyType(DfPropertyDesc propertyDesc, String[] myKeys,
            String[] yourKeys, int relno, TnBeanMetaData relationBeanMetaData) {
        return new TnRelationPropertyTypeImpl(propertyDesc, relno, myKeys, yourKeys, _localBeanMetaData,
                relationBeanMetaData);
    }

    protected TnBeanMetaData createRelationBeanMetaData(Class<?> relationBeanClass) {
        final int nextRelationNestLevel = _relationNestLevel + 1;
        return _beanMetaDataFactory.createBeanMetaData(_dbMetaData, relationBeanClass, nextRelationNestLevel);
    }
}
