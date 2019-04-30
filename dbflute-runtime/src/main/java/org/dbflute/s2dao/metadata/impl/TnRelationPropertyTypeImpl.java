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
package org.dbflute.s2dao.metadata.impl;

import java.util.ArrayList;
import java.util.List;

import org.dbflute.Entity;
import org.dbflute.cbean.sqlclause.SqlClause;
import org.dbflute.dbmeta.DBMeta;
import org.dbflute.dbmeta.info.ColumnInfo;
import org.dbflute.dbmeta.info.ForeignInfo;
import org.dbflute.dbmeta.info.PrimaryInfo;
import org.dbflute.exception.DBMetaNotFoundException;
import org.dbflute.helper.beans.DfPropertyAccessor;
import org.dbflute.helper.beans.DfPropertyDesc;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.s2dao.metadata.TnBeanMetaData;
import org.dbflute.s2dao.metadata.TnPropertyType;
import org.dbflute.s2dao.metadata.TnRelationPropertyType;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public class TnRelationPropertyTypeImpl extends TnPropertyTypeImpl implements TnRelationPropertyType {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final int _relationNo;
    protected final String _relationNoSuffixPart;
    protected final String[] _myKeys;
    protected final String[] _yourKeys;
    protected final TnBeanMetaData _myBeanMetaData;
    protected final TnBeanMetaData _yourBeanMetaData;
    protected final List<TnPropertyType> _uniquePropertyTypeList;
    protected final boolean _hasSimpleUniqueKey;
    protected final boolean _hasCompoundUniqueKey;
    protected final TnPropertyType _simpleUniquePropertyType;
    protected final DfPropertyAccessor _propertyAccessor;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TnRelationPropertyTypeImpl(final DfPropertyDesc propertyDesc, int relationNo, String[] myKeys, String[] yourKeys,
            TnBeanMetaData myBeanMetaData, TnBeanMetaData yourBeanMetaData) {
        super(propertyDesc);
        _relationNo = relationNo;
        _relationNoSuffixPart = buildRelationNoSuffixPart(relationNo);
        _myKeys = myKeys;
        _yourKeys = yourKeys;
        _myBeanMetaData = myBeanMetaData;
        _yourBeanMetaData = yourBeanMetaData;
        _uniquePropertyTypeList = deriveUniqueKeys(yourKeys, yourBeanMetaData);

        // save at this point for performance (suppose that many relation has the only one key)
        _hasSimpleUniqueKey = _uniquePropertyTypeList.size() == 1;
        _hasCompoundUniqueKey = _uniquePropertyTypeList.size() >= 2;
        _simpleUniquePropertyType = _hasSimpleUniqueKey ? _uniquePropertyTypeList.get(0) : null;
        _propertyAccessor = createPropertyAccessor(propertyDesc, myBeanMetaData);
    }

    protected String buildRelationNoSuffixPart(int relationNo) {
        return SqlClause.RELATION_PATH_DELIMITER + relationNo;
    }

    protected List<TnPropertyType> deriveUniqueKeys(String[] yourKeys, TnBeanMetaData yourBeanMetaData) {
        final DBMeta dbmeta = yourBeanMetaData.getDBMeta();
        final List<TnPropertyType> uniquePropertyTypeList;
        if (dbmeta != null && dbmeta.hasPrimaryKey()) {
            final PrimaryInfo primaryInfo = dbmeta.getPrimaryInfo();
            final List<ColumnInfo> primaryColumnList = primaryInfo.getPrimaryColumnList();
            uniquePropertyTypeList = new ArrayList<TnPropertyType>(primaryColumnList.size());
            for (ColumnInfo pk : primaryColumnList) {
                final TnPropertyType pt = yourBeanMetaData.getPropertyTypeByColumnName(pk.getColumnDbName());
                uniquePropertyTypeList.add(pt);
            }
        } else {
            uniquePropertyTypeList = new ArrayList<TnPropertyType>(yourKeys.length);
            for (String yourKey : yourKeys) {
                final TnPropertyType pt = yourBeanMetaData.getPropertyTypeByColumnName(yourKey);
                uniquePropertyTypeList.add(pt);
            }
        }
        return uniquePropertyTypeList;
    }

    protected DfPropertyAccessor createPropertyAccessor(final DfPropertyDesc propertyDesc, TnBeanMetaData myBeanMetaData) {
        final DBMeta dbmeta = myBeanMetaData.getDBMeta();
        assertDBMetaExists(dbmeta, myBeanMetaData);
        final String propertyName = propertyDesc.getPropertyName();
        final ForeignInfo foreignInfo = dbmeta.hasForeign(propertyName) ? dbmeta.findForeignInfo(propertyName) : null;
        return new DfPropertyAccessor() {

            public String getPropertyName() {
                return foreignInfo != null ? foreignInfo.getForeignPropertyName() : propertyName;
            }

            public Class<?> getPropertyType() {
                return foreignInfo != null ? foreignInfo.getPropertyAccessType() : propertyDesc.getPropertyType();
            }

            public Class<?> getGenericType() {
                return propertyDesc.getGenericType();
            }

            public Object getValue(Object target) {
                if (foreignInfo != null && target instanceof Entity) { // basically here
                    return foreignInfo.read((Entity) target);
                } else {
                    return propertyDesc.getValue(target);
                }
            }

            public void setValue(Object target, Object value) {
                if (foreignInfo != null && target instanceof Entity) { // basically here
                    foreignInfo.write((Entity) target, value);
                } else {
                    propertyDesc.setValue(target, value);
                }
            }

            public boolean isReadable() {
                return propertyDesc.isReadable();
            }

            public boolean isWritable() {
                return propertyDesc.isWritable();
            }
        };
    }

    protected void assertDBMetaExists(DBMeta dbmeta, TnBeanMetaData myBeanMetaData) {
        if (dbmeta == null) { // basically no way
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("Not found the DB meta for the entity");
            br.addItem("Advice");
            br.addElement("Basically no way!");
            br.addElement("So confirm your DBFlute environment.");
            br.addElement(" e.g. DBMetaInstanceHandler, table name's case");
            br.addItem("Corresponding Table");
            br.addElement(myBeanMetaData.getTableName());
            br.addElement(myBeanMetaData.getBeanClass());
            final String msg = br.buildExceptionMessage();
            throw new DBMetaNotFoundException(msg);
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public int getRelationNo() {
        return _relationNo;
    }

    public String getRelationNoSuffixPart() {
        return _relationNoSuffixPart;
    }

    public int getKeySize() {
        if (_myKeys.length > 0) {
            return _myKeys.length;
        } else {
            return _yourBeanMetaData.getPrimaryKeySize();
        }

    }

    public String getMyKey(int index) {
        if (_myKeys.length > 0) {
            return _myKeys[index];
        } else {
            return _yourBeanMetaData.getPrimaryKeyDbName(index);
        }
    }

    public String getYourKey(int index) {
        if (_yourKeys.length > 0) {
            return _yourKeys[index];
        } else {
            return _yourBeanMetaData.getPrimaryKeyDbName(index);
        }
    }

    public boolean isYourKey(String columnName) {
        for (int i = 0; i < getKeySize(); ++i) {
            if (columnName.equalsIgnoreCase(getYourKey(i))) {
                return true;
            }
        }
        return false;
    }

    public TnBeanMetaData getMyBeanMetaData() {
        return _myBeanMetaData;
    }

    public TnBeanMetaData getYourBeanMetaData() {
        return _yourBeanMetaData;
    }

    public List<TnPropertyType> getUniquePropertyTypeList() {
        return _uniquePropertyTypeList;
    }

    public boolean hasSimpleUniqueKey() {
        return _hasSimpleUniqueKey;
    }

    public boolean hasCompoundUniqueKey() {
        return _hasCompoundUniqueKey;
    }

    public TnPropertyType getSimpleUniquePropertyType() {
        return _simpleUniquePropertyType;
    }

    @Override
    public DfPropertyAccessor getPropertyAccessor() {
        return _propertyAccessor;
    }
}