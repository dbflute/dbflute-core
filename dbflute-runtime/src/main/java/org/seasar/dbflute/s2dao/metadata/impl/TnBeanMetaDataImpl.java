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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.dbmeta.name.ColumnSqlName;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.helper.beans.DfBeanDesc;
import org.seasar.dbflute.helper.beans.DfPropertyDesc;
import org.seasar.dbflute.helper.beans.exception.DfBeanPropertyNotFoundException;
import org.seasar.dbflute.helper.beans.factory.DfBeanDescFactory;
import org.seasar.dbflute.s2dao.extension.TnBeanMetaDataFactoryExtension;
import org.seasar.dbflute.s2dao.identity.TnIdentifierGenerator;
import org.seasar.dbflute.s2dao.identity.TnIdentifierGeneratorFactory;
import org.seasar.dbflute.s2dao.metadata.TnBeanAnnotationReader;
import org.seasar.dbflute.s2dao.metadata.TnBeanMetaData;
import org.seasar.dbflute.s2dao.metadata.TnModifiedPropertySupport;
import org.seasar.dbflute.s2dao.metadata.TnPropertyType;
import org.seasar.dbflute.s2dao.metadata.TnPropertyTypeFactory;
import org.seasar.dbflute.s2dao.metadata.TnRelationPropertyType;
import org.seasar.dbflute.s2dao.metadata.TnRelationPropertyTypeFactory;

/**
 * The implementation as S2Dao of bean meta data. <br />
 * This class has sub-class extended by DBFlute.
 * <pre>
 * {@link TnBeanMetaDataImpl} is close to S2Dao logic
 * The extension in {@link TnBeanMetaDataFactoryExtension} has DBFlute logic
 * </pre>
 * DBFlute depended on S2Dao before 0.9.0. <br />
 * It saves these structure to be easy to know what DBFlute extends it.
 * @author modified by jflute (originated in S2Dao)
 */
public class TnBeanMetaDataImpl implements TnBeanMetaData {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The type of bean. (NotNull) */
    protected final Class<?> _beanClass;

    /** The DB meta of the bean. (NotNull: if DBFlute entity) */
    protected final DBMeta _dbmeta;

    /** The name of table. (NotNull: after initialized, if it's not entity, this value is 'df:Unknown') */
    protected String _tableName;

    protected final StringKeyMap<TnPropertyType> _propertyTypeMap = StringKeyMap.createAsCaseInsensitive();
    protected final List<TnPropertyType> _propertyTypeList = new ArrayList<TnPropertyType>();
    protected TnBeanAnnotationReader _beanAnnotationReader;
    protected TnPropertyTypeFactory _propertyTypeFactory;

    /** The array of property type for primary key. */
    protected TnPropertyType[] _primaryKeys;

    // should be initialized in a process synchronized
    protected final Map<String, TnPropertyType> _columnPropertyTypeMap = StringKeyMap.createAsCaseInsensitive();
    protected final List<TnRelationPropertyType> _relationPropertyTypes = new ArrayList<TnRelationPropertyType>();
    protected final List<TnIdentifierGenerator> _identifierGeneratorList = new ArrayList<TnIdentifierGenerator>();
    protected final Map<String, TnIdentifierGenerator> _identifierGeneratorsByPropertyName = StringKeyMap
            .createAsCaseInsensitive();

    protected String _versionNoPropertyName;
    protected String _timestampPropertyName;
    protected TnModifiedPropertySupport _modifiedPropertySupport;
    protected TnRelationPropertyTypeFactory _relationPropertyTypeFactory;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TnBeanMetaDataImpl(Class<?> beanClass, DBMeta dbmeta) {
        _beanClass = beanClass;
        _dbmeta = dbmeta;
    }

    // ===================================================================================
    //                                                                          Initialize
    //                                                                          ==========
    public void initialize() { // non thread safe so this is called immediately after creation
        final DfBeanDesc beanDesc = DfBeanDescFactory.getBeanDesc(getBeanClass());
        setupTableName(beanDesc);
        setupProperty();
        setupPrimaryKey();
    }

    protected void setupTableName(DfBeanDesc beanDesc) { // only called in the initialize() process 
        final String ta = _beanAnnotationReader.getTableAnnotation();
        if (ta != null) {
            _tableName = ta;
        } else {
            _tableName = "df:Unknown";
        }
    }

    protected void setupProperty() { // only called in the initialize() process
        final TnPropertyType[] propertyTypes = _propertyTypeFactory.createBeanPropertyTypes();
        for (int i = 0; i < propertyTypes.length; i++) {
            TnPropertyType pt = propertyTypes[i];
            addPropertyType(pt);
            _columnPropertyTypeMap.put(pt.getColumnDbName(), pt);
        }

        final TnRelationPropertyType[] rptTypes = _relationPropertyTypeFactory.createRelationPropertyTypes();
        for (int i = 0; i < rptTypes.length; i++) {
            TnRelationPropertyType rpt = rptTypes[i];
            addRelationPropertyType(rpt);
        }
    }

    protected void addPropertyType(TnPropertyType propertyType) { // only called in the initialize() process
        _propertyTypeMap.put(propertyType.getPropertyName(), propertyType);
        _propertyTypeList.add(propertyType);
    }

    protected void setupPrimaryKey() { // only called in the initialize() process
        final List<TnPropertyType> keys = new ArrayList<TnPropertyType>();
        for (TnPropertyType pt : _propertyTypeList) {
            if (pt.isPrimaryKey()) {
                keys.add(pt);
                setupIdentifierGenerator(pt);
            }
        }
        _primaryKeys = (TnPropertyType[]) keys.toArray(new TnPropertyType[keys.size()]);
    }

    protected void setupIdentifierGenerator(TnPropertyType pt) { // only called in the initialize() process
        final DfPropertyDesc pd = pt.getPropertyDesc();
        final String propertyName = pt.getPropertyName();
        final String idType = _beanAnnotationReader.getId(pd);
        final TnIdentifierGenerator generator = TnIdentifierGeneratorFactory.createIdentifierGenerator(pt, idType);
        _identifierGeneratorList.add(generator);
        _identifierGeneratorsByPropertyName.put(propertyName, generator);
    }

    protected void addRelationPropertyType(TnRelationPropertyType rpt) { // only called in the initialize() process
        for (int i = _relationPropertyTypes.size(); i <= rpt.getRelationNo(); ++i) {
            _relationPropertyTypes.add(null);
        }
        _relationPropertyTypes.set(rpt.getRelationNo(), rpt);
    }

    // ===================================================================================
    //                                                                          Basic Info
    //                                                                          ==========
    public Class<?> getBeanClass() {
        return _beanClass;
    }

    public DBMeta getDBMeta() {
        return _dbmeta;
    }

    public String getTableName() {
        return _tableName;
    }

    // ===================================================================================
    //                                                                       Property Type
    //                                                                       =============
    public List<TnPropertyType> getPropertyTypeList() {
        return _propertyTypeList;
    }

    public TnPropertyType getPropertyType(String propertyName) {
        final TnPropertyType propertyType = (TnPropertyType) _propertyTypeMap.get(propertyName);
        if (propertyType == null) {
            String msg = "The propertyName was not found in the map:";
            msg = msg + " propertyName=" + propertyName + " propertyTypeMap=" + _propertyTypeMap;
            throw new IllegalStateException(msg);
        }
        return propertyType;
    }

    public boolean hasPropertyType(String propertyName) {
        return _propertyTypeMap.get(propertyName) != null;
    }

    public TnPropertyType getPropertyTypeByColumnName(String columnName) {
        final TnPropertyType propertyType = _columnPropertyTypeMap.get(columnName);
        if (propertyType == null) {
            throwBeanMetaPropertyTypeByColumnNameNotFoundException(columnName);
        }
        return propertyType;
    }

    protected void throwBeanMetaPropertyTypeByColumnNameNotFoundException(String columnName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The column was not found in the table.");
        br.addItem("Bean Class");
        br.addElement(_beanClass);
        br.addItem("Column");
        br.addElement(_tableName + "." + columnName);
        br.addItem("DBMeta");
        br.addElement(_dbmeta);
        br.addItem("Mapping");
        final Set<Entry<String, TnPropertyType>> entrySet = _columnPropertyTypeMap.entrySet();
        for (Entry<String, TnPropertyType> entry : entrySet) {
            br.addElement(entry.getKey() + ": " + entry.getValue());
        }
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }

    public TnPropertyType getPropertyTypeByAliasName(String alias) {
        if (hasPropertyTypeByColumnName(alias)) {
            return getPropertyTypeByColumnName(alias);
        }
        final int index = alias.lastIndexOf('_');
        if (index < 0) {
            String msg = "The alias was not found in the table: table=" + _tableName + " alias=" + alias;
            throw new IllegalStateException(msg);
        }
        final String columnName = alias.substring(0, index);
        final String relnoStr = alias.substring(index + 1);
        int relno = -1;
        try {
            relno = Integer.parseInt(relnoStr);
        } catch (Throwable t) {
            String msg = "The alias was not found in the table: table=" + _tableName + " alias=" + alias;
            throw new IllegalStateException(msg, t);
        }
        final TnRelationPropertyType rpt = getRelationPropertyType(relno);
        if (!rpt.getYourBeanMetaData().hasPropertyTypeByColumnName(columnName)) {
            String msg = "The alias was not found in the table: table=" + _tableName + " alias=" + alias;
            throw new IllegalStateException(msg);
        }
        return rpt.getYourBeanMetaData().getPropertyTypeByColumnName(columnName);
    }

    public boolean hasPropertyTypeByColumnName(String columnName) {
        return _columnPropertyTypeMap.get(columnName) != null;
    }

    public boolean hasPropertyTypeByAliasName(String alias) {
        if (hasPropertyTypeByColumnName(alias)) {
            return true;
        }
        final int index = alias.lastIndexOf('_');
        if (index < 0) {
            return false;
        }
        final String columnName = alias.substring(0, index);
        final String relnoStr = alias.substring(index + 1);
        int relno = -1;
        try {
            relno = Integer.parseInt(relnoStr);
        } catch (Throwable t) {
            return false;
        }
        if (relno >= getRelationPropertyTypeSize()) {
            return false;
        }
        final TnRelationPropertyType rpt = getRelationPropertyType(relno);
        return rpt.getYourBeanMetaData().hasPropertyTypeByColumnName(columnName);
    }

    public String convertFullColumnName(String alias) {
        if (hasPropertyTypeByColumnName(alias)) {
            return _tableName + "." + alias;
        }
        final int index = alias.lastIndexOf('_');
        if (index < 0) {
            String msg = "The alias was not found in the table: table=" + _tableName + " alias=" + alias;
            throw new IllegalStateException(msg);
        }
        final String columnName = alias.substring(0, index);
        final String relnoStr = alias.substring(index + 1);
        int relno = -1;
        try {
            relno = Integer.parseInt(relnoStr);
        } catch (Throwable t) {
            String msg = "The alias was not found in the table: table=" + _tableName + " alias=" + alias;
            throw new IllegalStateException(msg, t);
        }
        final TnRelationPropertyType rpt = getRelationPropertyType(relno);
        if (!rpt.getYourBeanMetaData().hasPropertyTypeByColumnName(columnName)) {
            String msg = "The alias was not found in the table: table=" + _tableName + " alias=" + alias;
            throw new IllegalStateException(msg);
        }
        return rpt.getPropertyName() + "." + columnName;
    }

    // ===================================================================================
    //                                                                     Optimistic Lock
    //                                                                     ===============
    public TnPropertyType getVersionNoPropertyType() throws DfBeanPropertyNotFoundException {
        return getPropertyType(getVersionNoPropertyName());
    }

    public TnPropertyType getTimestampPropertyType() throws DfBeanPropertyNotFoundException {
        return getPropertyType(getTimestampPropertyName());
    }

    public String getVersionNoPropertyName() {
        return _versionNoPropertyName;
    }

    public String getTimestampPropertyName() {
        return _timestampPropertyName;
    }

    public boolean hasVersionNoPropertyType() {
        return hasPropertyType(getVersionNoPropertyName());
    }

    public boolean hasTimestampPropertyType() {
        return hasPropertyType(getTimestampPropertyName());
    }

    // ===================================================================================
    //                                                              Relation Property Type
    //                                                              ======================
    public List<TnRelationPropertyType> getRelationPropertyTypeList() {
        return _relationPropertyTypes;
    }

    public int getRelationPropertyTypeSize() {
        return _relationPropertyTypes.size();
    }

    public TnRelationPropertyType getRelationPropertyType(int index) {
        return _relationPropertyTypes.get(index);
    }

    public TnRelationPropertyType getRelationPropertyType(String propertyName) throws DfBeanPropertyNotFoundException {
        for (int i = 0; i < getRelationPropertyTypeSize(); i++) {
            final TnRelationPropertyType rpt = (TnRelationPropertyType) _relationPropertyTypes.get(i);
            if (rpt != null && rpt.getPropertyName().equalsIgnoreCase(propertyName)) {
                return rpt;
            }
        }
        throw new DfBeanPropertyNotFoundException(getBeanClass(), propertyName);
    }

    public int getPrimaryKeySize() {
        return _primaryKeys.length;
    }

    public String getPrimaryKeyDbName(int index) {
        return _primaryKeys[index].getColumnDbName();
    }

    public ColumnSqlName getPrimaryKeySqlName(int index) {
        return _primaryKeys[index].getColumnSqlName();
    }

    public int getIdentifierGeneratorSize() {
        return _identifierGeneratorList.size();
    }

    public TnIdentifierGenerator getIdentifierGenerator(int index) {
        return _identifierGeneratorList.get(index);
    }

    public TnIdentifierGenerator getIdentifierGenerator(String propertyName) {
        return _identifierGeneratorsByPropertyName.get(propertyName);
    }

    public Set<String> getModifiedPropertyNames(Object bean) {
        return getModifiedPropertySupport().getModifiedPropertyNames(bean);
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setVersionNoPropertyName(String versionNoPropertyName) {
        _versionNoPropertyName = versionNoPropertyName;
    }

    public void setTimestampPropertyName(String timestampPropertyName) {
        _timestampPropertyName = timestampPropertyName;
    }

    public void setBeanAnnotationReader(TnBeanAnnotationReader beanAnnotationReader) {
        _beanAnnotationReader = beanAnnotationReader;
    }

    public void setPropertyTypeFactory(TnPropertyTypeFactory propertyTypeFactory) {
        _propertyTypeFactory = propertyTypeFactory;
    }

    public void setRelationPropertyTypeFactory(TnRelationPropertyTypeFactory relationPropertyTypeFactory) {
        _relationPropertyTypeFactory = relationPropertyTypeFactory;
    }

    public TnModifiedPropertySupport getModifiedPropertySupport() {
        return _modifiedPropertySupport;
    }

    public void setModifiedPropertySupport(TnModifiedPropertySupport propertyModifiedSupport) {
        _modifiedPropertySupport = propertyModifiedSupport;
    }
}
