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
package org.seasar.dbflute.s2dao.extension;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.Entity;
import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.cbean.ConditionBeanContext;
import org.seasar.dbflute.cbean.chelper.HpDerivingSubQueryInfo;
import org.seasar.dbflute.cbean.sqlclause.SqlClause;
import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.dbmeta.DerivedMappable;
import org.seasar.dbflute.dbmeta.DerivedTypeHandler;
import org.seasar.dbflute.dbmeta.info.ColumnInfo;
import org.seasar.dbflute.exception.MappingClassCastException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.jdbc.ValueType;
import org.seasar.dbflute.resource.DBFluteSystem;
import org.seasar.dbflute.resource.InternalMapContext;
import org.seasar.dbflute.resource.ResourceContext;
import org.seasar.dbflute.s2dao.metadata.TnBeanMetaData;
import org.seasar.dbflute.s2dao.metadata.TnPropertyMapping;
import org.seasar.dbflute.s2dao.rowcreator.impl.TnRowCreatorImpl;
import org.seasar.dbflute.s2dao.valuetype.TnValueTypes;
import org.seasar.dbflute.util.DfTypeUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class TnRowCreatorExtension extends TnRowCreatorImpl {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(TnRowCreatorExtension.class);

    /** The key of DBMeta cache. */
    protected static final String DBMETA_CACHE_KEY = "df:DBMetaCache";

    /** The prefix mark for derived mapping alias. */
    protected static final String DERIVED_MAPPABLE_ALIAS_PREFIX = DerivedMappable.MAPPING_ALIAS_PREFIX;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected DBMeta _fixedDBMeta;
    protected boolean _creatableByDBMeta;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    protected TnRowCreatorExtension() {
    }

    /**
     * @param beanClass The class of target bean to find DB-meta. (NullAllowed)
     * @return The instance of internal row creator. (NotNull)
     */
    public static TnRowCreatorExtension createRowCreator(Class<?> beanClass) {
        final TnRowCreatorExtension rowCreator = new TnRowCreatorExtension();
        if (beanClass != null) {
            final DBMeta dbmeta = findDBMetaByClass(beanClass);
            if (dbmeta != null) {
                rowCreator.setFixedDBMeta(dbmeta);
                rowCreator.setCreatableByDBMeta(isCreatableByDBMeta(beanClass, dbmeta.getEntityType()));
            }
        }
        return rowCreator;
    }

    protected static DBMeta findDBMetaByClass(Class<?> beanClass) {
        if (!Entity.class.isAssignableFrom(beanClass)) {
            return null;
        }
        // getting from entity because the bean may be customize entity.
        final Object instance = newInstance(beanClass); // only when initialization
        return ((Entity) instance).getDBMeta();
    }

    protected static Object newInstance(Class<?> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    protected static boolean isCreatableByDBMeta(Class<?> beanClass, Class<?> entityType) {
        // Returns false when the bean is not related to the entity or is a sub class of the entity.
        return beanClass.isAssignableFrom(entityType);
    }

    // ===================================================================================
    //                                                                                Main
    //                                                                                ====
    /**
     * {@inheritDoc}
     */
    public Object createRow(ResultSet rs, Map<String, Map<String, Integer>> selectIndexMap,
            Map<String, TnPropertyMapping> propertyCache, Class<?> beanClass) throws SQLException {
        if (propertyCache.isEmpty()) {
            String msg = "The propertyCache should not be empty: bean=" + beanClass.getName();
            throw new IllegalStateException(msg);
        }

        // temporary variable, for exception message, debug message
        String columnName = null;
        TnPropertyMapping mapping = null;
        String propertyName = null;
        Object selectedValue = null;
        ColumnInfo columnInfo = null;

        final Object row;
        final DBMeta dbmeta;
        if (_fixedDBMeta != null) {
            if (_creatableByDBMeta) { // mainly here
                row = _fixedDBMeta.newEntity();
            } else { // e.g. manual-extended entity
                row = newBean(beanClass);
            }
            dbmeta = _fixedDBMeta;
        } else { // e.g. manual-created bean of outsideSql
            row = newBean(beanClass);
            dbmeta = findCachedDBMeta(row); // find just in case
        }
        try {
            if (dbmeta != null) { // mainly here
                final boolean isEntity = row instanceof Entity; // almost always true
                final Entity entityRow = isEntity ? (Entity) row : null;
                for (Entry<String, TnPropertyMapping> entry : propertyCache.entrySet()) {
                    columnName = entry.getKey();
                    mapping = entry.getValue();
                    propertyName = mapping.getPropertyName();
                    selectedValue = getValue(rs, columnName, mapping.getValueType(), selectIndexMap);
                    columnInfo = mapping.getEntityColumnInfo();
                    if (columnInfo != null && isEntity) {
                        columnInfo.write(entityRow, selectedValue);
                    } else {
                        mapping.getPropertyAccessor().setValue(row, selectedValue);
                    }
                }
                if (canHandleDerivedMap(row)) {
                    processDerivedMap(rs, selectIndexMap, propertyCache, row);
                }
            } else { // not DBFlute entity
                for (Entry<String, TnPropertyMapping> entry : propertyCache.entrySet()) {
                    columnName = entry.getKey();
                    mapping = entry.getValue();
                    propertyName = mapping.getPropertyName();
                    selectedValue = getValue(rs, columnName, mapping.getValueType(), selectIndexMap);
                    mapping.getPropertyAccessor().setValue(row, selectedValue);
                }
            }
            return row;
        } catch (ClassCastException e) {
            throwMappingClassCastException(row, dbmeta, mapping, selectedValue, e);
            return null; // unreachable
        } catch (SQLException e) {
            if (_log.isDebugEnabled()) {
                String msg = "Failed to get selected values while resultSet handling:";
                msg = msg + " target=" + DfTypeUtil.toClassTitle(beanClass) + "." + propertyName;
                _log.debug(msg);
            }
            throw e;
        }
    }

    protected boolean canHandleDerivedMap(final Object row) {
        return row instanceof DerivedMappable && ConditionBeanContext.isExistConditionBeanOnThread();
    }

    protected void processDerivedMap(ResultSet rs, Map<String, Map<String, Integer>> selectIndexMap,
            Map<String, TnPropertyMapping> propertyCache, Object row) throws SQLException {
        final ConditionBean cb = ConditionBeanContext.getConditionBeanOnThread();
        final SqlClause sqlClause = cb.getSqlClause();
        if (!sqlClause.hasSpecifiedDerivingSubQuery()) {
            return;
        }
        final DerivedMappable mappable = (DerivedMappable) row;
        final List<String> derivingAliasList = sqlClause.getSpecifiedDerivingAliasList();
        DerivedTypeHandler typeHandler = null;
        for (String derivingAlias : derivingAliasList) {
            // propertyCache has alias name when derived-referrer as case-insensitive
            if (propertyCache.containsKey(derivingAlias)) { // already handled
                continue;
            }
            if (!derivingAlias.startsWith(DERIVED_MAPPABLE_ALIAS_PREFIX)) { // basically no way (just in case)
                continue; // might be exception but no need to be strict here
            }
            if (typeHandler == null) {
                typeHandler = cb.xgetDerivedTypeHandler(); // basically fixed instance returned
                if (typeHandler == null) { // no way, just in case
                    String msg = "Not found the type handler from condition-bean: " + cb.getTableDbName();
                    throw new IllegalStateException(msg);
                }
            }
            final HpDerivingSubQueryInfo derivingInfo = sqlClause.getSpecifiedDerivingInfo(derivingAlias);
            final ValueType valueType = TnValueTypes.getValueType(typeHandler.findMappingType(derivingInfo));
            final String onQueryAlias = Srl.substringFirstRear(derivingAlias, DERIVED_MAPPABLE_ALIAS_PREFIX);
            Object selectedValue = getValue(rs, onQueryAlias, valueType, selectIndexMap);
            selectedValue = typeHandler.convertToMapValue(derivingInfo, selectedValue);
            mappable.registerDerivedValue(derivingAlias, selectedValue);
        }
    }

    protected Object getValue(ResultSet rs, String columnName, ValueType valueType,
            Map<String, Map<String, Integer>> selectIndexMap) throws SQLException {
        final Object value;
        if (selectIndexMap != null) {
            value = ResourceContext.getLocalValue(rs, columnName, valueType, selectIndexMap);
        } else {
            value = valueType.getValue(rs, columnName);
        }
        return value;
    }

    protected void throwMappingClassCastException(Object entity, DBMeta dbmeta, TnPropertyMapping mapping,
            Object selectedValue, ClassCastException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to cast a class while data mapping.");
        br.addItem("Advice");
        br.addElement("If you use Seasar(S2Container), this exception may be");
        br.addElement("from ClassLoader Headache about HotDeploy.");
        br.addElement("Add the ignore-package setting to convention.dicon like this:");
        br.addElement("    <initMethod name=”addIgnorePackageName”>");
        br.addElement("        <arg>”com.example.xxx.dbflute”</arg>");
        br.addElement("    </initMethod>");
        br.addElement("If you use an other DI container, this exception may be");
        br.addElement("from illegal state about your settings of DBFlute.");
        br.addElement("Confirm your settings: for example, typeMappingMap.dfprop.");
        br.addItem("Exception Message");
        br.addElement(e.getMessage());
        br.addItem("Target Entity");
        br.addElement(entity);
        br.addElement("classLoader: " + entity.getClass().getClassLoader());
        br.addItem("Target DBMeta");
        br.addElement(dbmeta);
        br.addElement("classLoader: " + dbmeta.getClass().getClassLoader());
        br.addItem("Property Mapping");
        br.addElement(mapping);
        br.addElement("type: " + (mapping != null ? mapping.getClass() : null));
        br.addItem("Selected Value");
        br.addElement(selectedValue);
        br.addElement("type: " + (selectedValue != null ? selectedValue.getClass() : null));
        final String msg = br.buildExceptionMessage();
        throw new MappingClassCastException(msg, e);
    }

    // ===================================================================================
    //                                                                        DBMeta Cache
    //                                                                        ============
    // share with relation row
    /**
     * @param row The instance of row. (NotNull)
     * @return The interface of DBMeta. (NullAllowed: If it's null, it means NotFound.)
     */
    public static DBMeta findCachedDBMeta(Object row) {
        return DBMetaCacheHandler.findDBMeta(row);
    }

    /**
     * @param rowType The type of row. (NotNull)
     * @param tableName The name of table. (NotNull)
     * @return The interface of DBMeta. (NullAllowed: If it's null, it means NotFound.)
     */
    public static DBMeta findCachedDBMeta(Class<?> rowType, String tableName) {
        return DBMetaCacheHandler.findDBMeta(rowType, tableName);
    }

    protected static class DBMetaCacheHandler {

        /** The key of DBMeta cache. */
        protected static final String DBMETA_CACHE_KEY = "df:DBMetaCache";

        public static DBMeta findDBMeta(Object row) {
            if (!(row instanceof Entity)) {
                return null;
            }
            final Entity entity = (Entity) row;
            DBMeta dbmeta = getCachedDBMeta(entity.getClass());
            if (dbmeta != null) {
                return dbmeta;
            }
            dbmeta = entity.getDBMeta();
            cacheDBMeta(entity, dbmeta);
            return dbmeta;
        }

        public static DBMeta findDBMeta(Class<?> rowType, String tableName) {
            DBMeta dbmeta = getCachedDBMeta(rowType);
            if (dbmeta != null) {
                return dbmeta;
            }
            // No check because the table name is not always for domain.
            dbmeta = ResourceContext.provideDBMeta(tableName);
            cacheDBMeta(rowType, dbmeta);
            return dbmeta;
        }

        protected static DBMeta getCachedDBMeta(Class<?> rowType) {
            Map<Class<?>, DBMeta> contextCacheMap = getDBMetaContextCacheMap();
            if (contextCacheMap == null) {
                contextCacheMap = new HashMap<Class<?>, DBMeta>();
                InternalMapContext.setObject(DBMETA_CACHE_KEY, contextCacheMap);
            }
            return contextCacheMap.get(rowType);
        }

        protected static void cacheDBMeta(Entity entity, DBMeta dbmeta) {
            cacheDBMeta(entity.getClass(), dbmeta);
        }

        protected static void cacheDBMeta(Class<?> type, DBMeta dbmeta) {
            final Map<Class<?>, DBMeta> dbmetaCache = getDBMetaContextCacheMap();
            dbmetaCache.put(type, dbmeta);
        }

        @SuppressWarnings("unchecked")
        protected static Map<Class<?>, DBMeta> getDBMetaContextCacheMap() {
            return (Map<Class<?>, DBMeta>) InternalMapContext.getObject(DBMETA_CACHE_KEY);
        }
    }

    // ===================================================================================
    //                                                                             Fix Row
    //                                                                             =======
    // share with relation row
    /**
     * Adjust created row. (clearing modified info, ...)
     * @param row The row of result list. (NotNull)
     * @param bmd The bean meta data of the row. (NotNull)
     */
    public static void adjustCreatedRow(final Object row, TnBeanMetaData bmd) {
        if (row instanceof Entity) {
            final Entity entity = (Entity) row;
            entity.clearModifiedInfo();
            entity.markAsSelect();
        } else { // not DBFlute entity
            // actually any bean meta data can be accepted
            // because only it gets modified properties
            bmd.getModifiedPropertyNames(row).clear();
        }
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return DBFluteSystem.getBasicLn();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setFixedDBMeta(DBMeta fixedDBMeta) {
        this._fixedDBMeta = fixedDBMeta;
    }

    public void setCreatableByDBMeta(boolean creatableByDBMeta) {
        this._creatableByDBMeta = creatableByDBMeta;
    }
}
