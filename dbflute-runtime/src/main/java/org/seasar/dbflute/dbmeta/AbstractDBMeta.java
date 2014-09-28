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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.seasar.dbflute.Entity;
import org.seasar.dbflute.dbmeta.info.ColumnInfo;
import org.seasar.dbflute.dbmeta.info.ForeignInfo;
import org.seasar.dbflute.dbmeta.info.ReferrerInfo;
import org.seasar.dbflute.dbmeta.info.RelationInfo;
import org.seasar.dbflute.dbmeta.info.UniqueInfo;
import org.seasar.dbflute.exception.DBMetaNotFoundException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.jdbc.Classification;
import org.seasar.dbflute.jdbc.ClassificationMeta;
import org.seasar.dbflute.jdbc.ClassificationUndefinedHandlingType;
import org.seasar.dbflute.resource.DBFluteSystem;
import org.seasar.dbflute.util.DfAssertUtil;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.DfReflectionUtil;
import org.seasar.dbflute.util.DfTypeUtil;
import org.seasar.dbflute.util.Srl;

/**
 * The abstract class of DB meta.
 * @author jflute
 */
public abstract class AbstractDBMeta implements DBMeta {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The dummy value for internal map value. */
    protected static final Object DUMMY_VALUE = new Object();

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                  Information Resource
    //                                  --------------------
    // lazy-initialized at corresponding getters
    private volatile StringKeyMap<String> _tableDbNameFlexibleMap;
    private volatile StringKeyMap<String> _tablePropertyNameFlexibleMap;
    private volatile List<ColumnInfo> _columnInfoList;
    private volatile StringKeyMap<ColumnInfo> _columnInfoFlexibleMap;
    private volatile UniqueInfo _primaryUniqueInfo;
    private volatile List<ForeignInfo> _foreignInfoList;
    private volatile StringKeyMap<ForeignInfo> _foreignInfoFlexibleMap;
    private volatile Map<Integer, ForeignInfo> _foreignInfoRelationNoKeyMap;
    private volatile List<ReferrerInfo> _referrerInfoList;
    private volatile StringKeyMap<ReferrerInfo> _referrerInfoFlexibleMap;

    // ===================================================================================
    //                                                             Resource Initialization
    //                                                             =======================
    protected void initializeInformationResource() { // for instance initializer of subclass.
        // initialize the flexible map of table DB name
        getTableDbNameFlexibleMap();

        // initialize the flexible map of table property name
        getTablePropertyNameFlexibleMap();

        // initialize the list of column information
        getColumnInfoList();

        // initialize the flexible map of column information 
        getColumnInfoFlexibleMap();

        // initialize the primary unique information
        if (hasPrimaryKey()) {
            getPrimaryUniqueInfo();
        }

        // these should not be initialized here
        // because the problem 'cyclic reference' occurred 
        // so these are initialized as lazy
        //getForeignInfoList();
        //getForeignInfoFlexibleMap();
        //getReferrerInfoList();
        //getReferrerInfoFlexibleMap();
    }

    // ===================================================================================
    //                                                                    Property Gateway
    //                                                                    ================
    // -----------------------------------------------------
    //                                       Column Property
    //                                       ---------------
    protected void setupEpg(Map<String, PropertyGateway> propertyGatewayMap, PropertyGateway gateway,
            String propertyName) {
        propertyGatewayMap.put(propertyName, gateway); // the map should be plain map for performance
    }

    public PropertyGateway findPropertyGateway(String propertyName) {
        return null; // should be overridden
    }

    protected <ENTITY extends Entity> PropertyGateway doFindEpg(Map<String, PropertyGateway> propertyGatewayMap,
            String propertyName) {
        return propertyGatewayMap.get(propertyName);
    }

    // -----------------------------------------------------
    //                                      Foreign Property
    //                                      ----------------
    protected void setupEfpg(Map<String, PropertyGateway> propertyGatewayMap, PropertyGateway gateway,
            String foreignPropertyName) {
        propertyGatewayMap.put(foreignPropertyName, gateway); // the map should be plain map for performance
    }

    public PropertyGateway findForeignPropertyGateway(String propertyName) {
        return null; // might be overridden
    }

    protected <ENTITY extends Entity> PropertyGateway doFindEfpg(Map<String, PropertyGateway> propertyGatewayMap,
            String foreignPropertyName) {
        return propertyGatewayMap.get(foreignPropertyName);
    }

    // -----------------------------------------------------
    //                                       Write Converter
    //                                       ---------------
    // these are static to avoid the FindBugs headache
    // (implementations of PropertyGateway can be static class)
    protected static void ccls(ColumnInfo columnInfo, Object code) { // checkClassification
        // old style, for compatibility, check only on entity after Java8
        if (code == null) {
            return; // no check null value which means no existence on DB
        }
        final ClassificationMeta meta = columnInfo.getClassificationMeta();
        if (meta == null) { // no way (just in case)
            return;
        }
        final ClassificationUndefinedHandlingType undefinedHandlingType = meta.undefinedHandlingType();
        if (!undefinedHandlingType.isChecked()) { // basically no way (not called if no check)
            return;
        }
        final Classification classification = gcls(columnInfo, code);
        if (classification == null) {
            final String tableDbName = columnInfo.getDBMeta().getTableDbName();
            final String columnDbName = columnInfo.getColumnDbName();
            Entity.FunCustodial.handleUndefinedClassificationCode(tableDbName, columnDbName, meta, code);
        }
    }

    protected static Classification gcls(ColumnInfo columnInfo, Object code) { // getClassification
        if (code == null) {
            return null;
        }
        final ClassificationMeta meta = columnInfo.getClassificationMeta();
        if (meta == null) { // no way (just in case)
            return null;
        }
        return meta.codeOf(code);
    }

    protected static Integer cti(Object value) { // convertToInteger
        return DfTypeUtil.toInteger(value);
    }

    protected static Long ctl(Object value) { // convertToLong
        return DfTypeUtil.toLong(value);
    }

    protected static BigDecimal ctb(Object value) { // convertToBigDecimal
        return DfTypeUtil.toBigDecimal(value);
    }

    @SuppressWarnings("unchecked")
    protected static <NUMBER extends Number> NUMBER ctn(Object value, Class<NUMBER> type) { // convertToNumber
        return (NUMBER) DfTypeUtil.toNumber(value, type);
    }

    // ===================================================================================
    //                                                                          Table Info
    //                                                                          ==========
    // these methods is expected to override if it needs
    public String getTableAlias() {
        return null;
    }

    public String getTableComment() {
        return null;
    }

    // -----------------------------------------------------
    //                                          Flexible Map
    //                                          ------------
    /**
     * Get the flexible map of table DB name.
     * @return The flexible map of table DB name. (NotNull, NotEmpty)
     */
    protected Map<String, String> getTableDbNameFlexibleMap() {
        if (_tableDbNameFlexibleMap != null) {
            return _tableDbNameFlexibleMap;
        }
        synchronized (this) {
            if (_tableDbNameFlexibleMap != null) {
                return _tableDbNameFlexibleMap;
            }
            _tableDbNameFlexibleMap = createFlexibleConcurrentMap();
            _tableDbNameFlexibleMap.put(getTableDbName(), getTableDbName());
            return _tableDbNameFlexibleMap;
        }
    }

    /**
     * Get the flexible map of table property name.
     * @return The flexible map of table property name. (NotNull, NotEmpty)
     */
    protected Map<String, String> getTablePropertyNameFlexibleMap() {
        if (_tablePropertyNameFlexibleMap != null) {
            return _tablePropertyNameFlexibleMap;
        }
        synchronized (this) {
            if (_tablePropertyNameFlexibleMap != null) {
                return _tablePropertyNameFlexibleMap;
            }
            _tablePropertyNameFlexibleMap = createFlexibleConcurrentMap();
            _tablePropertyNameFlexibleMap.put(getTableDbName(), getTablePropertyName());
            return _tableDbNameFlexibleMap;
        }
    }

    // ===================================================================================
    //                                                                         Column Info
    //                                                                         ===========
    /**
     * {@inheritDoc}
     */
    public boolean hasColumn(String columnFlexibleName) {
        assertStringNotNullAndNotTrimmedEmpty("columnFlexibleName", columnFlexibleName);
        return getColumnInfoFlexibleMap().containsKey(columnFlexibleName);
    }

    /**
     * {@inheritDoc}
     */
    public ColumnInfo findColumnInfo(String columnFlexibleName) {
        assertStringNotNullAndNotTrimmedEmpty("columnFlexibleName", columnFlexibleName);
        final ColumnInfo columnInfo = getColumnInfoFlexibleMap().get(columnFlexibleName);
        if (columnInfo == null) {
            throwDBMetaNotFoundException("The column info was not found.", "Column", columnFlexibleName);
        }
        return columnInfo;
    }

    protected ColumnInfo cci(String columnDbName, String columnSqlName, String columnSynonym, String columnAlias // column name
            , Class<?> objectNativeType, String propertyName, Class<?> propertyAccessType // property info
            , boolean primary, boolean autoIncrement, boolean notNull // column basic check
            , String columnDbType, Integer columnSize, Integer decimalDigits, String defaultValue // column type
            , boolean commonColumn, OptimisticLockType optimisticLockType, String columnComment // column others
            , String foreignListExp, String referrerListExp // relation property
            , ClassificationMeta classificationMeta // various info
    ) { // createColumnInfo()
        final Class<?> realPt = chooseColumnPropertyAccessType(objectNativeType, propertyName, propertyAccessType);
        final String delimiter = ",";
        List<String> foreignPropList = null;
        if (foreignListExp != null && foreignListExp.trim().length() > 0) {
            foreignPropList = splitListTrimmed(foreignListExp, delimiter);
        }
        List<String> referrerPropList = null;
        if (referrerListExp != null && referrerListExp.trim().length() > 0) {
            referrerPropList = splitListTrimmed(referrerListExp, delimiter);
        }
        final PropertyMethodFinder propertyMethodFinder = createColumnPropertyMethodFinder();
        return new ColumnInfo(this, columnDbName, columnSqlName, columnSynonym, columnAlias, objectNativeType,
                propertyName, realPt, primary, autoIncrement, notNull, columnDbType, columnSize, decimalDigits,
                defaultValue, commonColumn, optimisticLockType, columnComment, foreignPropList, referrerPropList,
                classificationMeta, propertyMethodFinder);
    }

    protected Class<?> chooseColumnPropertyAccessType(Class<?> objectNativeType, String propertyName,
            Class<?> propertyAccessType) {
        return propertyAccessType != null ? propertyAccessType : objectNativeType;
    }

    protected PropertyMethodFinder createColumnPropertyMethodFinder() {
        return new PropertyMethodFinder() {
            public Method findReadMethod(Class<?> beanType, String propertyName, Class<?> propertyAccessType) {
                return findPropertyReadMethod(beanType, propertyName, propertyAccessType);
            }

            public Method findWriteMethod(Class<?> beanType, String propertyName, Class<?> propertyAccessType) {
                return findPropertyWriteMethod(beanType, propertyName, propertyAccessType);
            }
        };
    }

    protected Method findPropertyReadMethod(Class<?> beanType, String propertyName, Class<?> propertyAccessType) {
        final String methodName = buildPropertyGetterMethodName(propertyName);
        final Method method = doFindPropertyMethod(beanType, methodName, new Class<?>[] {});
        if (method == null) {
            String msg = "Not found the read method by the name:";
            msg = msg + " " + beanType.getName() + "#" + methodName + "()";
            throw new IllegalStateException(msg);
        }
        return method;
    }

    protected Method findPropertyWriteMethod(Class<?> beanType, String propertyName, Class<?> propertyAccessType) {
        final String methodName = buildPropertySetterMethodName(propertyName);
        final Method method = doFindPropertyMethod(beanType, methodName, new Class<?>[] { propertyAccessType });
        if (method == null) {
            String msg = "Not found the write method by the name and type:";
            msg = msg + " " + beanType.getName() + "#" + methodName + "(" + propertyAccessType.getName() + ")";
            throw new IllegalStateException(msg);
        }
        return method;
    }

    protected String buildPropertyGetterMethodName(String propertyName) {
        return "get" + initCap(propertyName);
    }

    protected String buildPropertySetterMethodName(String propertyName) {
        return "set" + initCap(propertyName);
    }

    protected Method doFindPropertyMethod(Class<?> clazz, String methodName, Class<?>[] argTypes) {
        return DfReflectionUtil.getAccessibleMethod(clazz, methodName, argTypes);
    }

    /**
     * {@inheritDoc}
     */
    public List<ColumnInfo> getColumnInfoList() {
        if (_columnInfoList != null) {
            return _columnInfoList;
        }
        synchronized (this) {
            if (_columnInfoList != null) {
                return _columnInfoList;
            }
            _columnInfoList = ccil();
            return _columnInfoList;
        }
    }

    protected abstract List<ColumnInfo> ccil(); // createColumnInfoList()

    /**
     * Get the flexible map of column information.
     * @return The flexible map of column information. (NotNull, NotEmpty)
     */
    protected Map<String, ColumnInfo> getColumnInfoFlexibleMap() {
        if (_columnInfoFlexibleMap != null) {
            return _columnInfoFlexibleMap;
        }
        final List<ColumnInfo> columnInfoList = getColumnInfoList();
        synchronized (this) {
            if (_columnInfoFlexibleMap != null) {
                return _columnInfoFlexibleMap;
            }
            _columnInfoFlexibleMap = createFlexibleConcurrentMap();
            for (ColumnInfo columnInfo : columnInfoList) {
                columnInfo.diveIntoFlexibleMap(_columnInfoFlexibleMap);
            }
            return _columnInfoFlexibleMap;
        }
    }

    // ===================================================================================
    //                                                                         Unique Info
    //                                                                         ===========
    /**
     * {@inheritDoc}
     */
    public UniqueInfo getPrimaryUniqueInfo() {
        if (_primaryUniqueInfo != null) {
            return _primaryUniqueInfo;
        }
        synchronized (this) {
            if (_primaryUniqueInfo != null) {
                return _primaryUniqueInfo;
            }
            _primaryUniqueInfo = cpui();
            return _primaryUniqueInfo;
        }
    }

    protected abstract UniqueInfo cpui(); // createPrimaryUniqueInfo()

    protected UniqueInfo hpcpui(ColumnInfo uniqueColumnInfo) { // helpCreatePrimaryUniqueInfo()
        return hpcpui(Arrays.asList(uniqueColumnInfo));
    }

    protected UniqueInfo hpcpui(List<ColumnInfo> uniqueColumnInfoList) { // helpCreatePrimaryUniqueInfo()
        return new UniqueInfo(this, uniqueColumnInfoList, true);
    }

    // ===================================================================================
    //                                                                       Relation Info
    //                                                                       =============
    /**
     * {@inheritDoc}
     */
    public RelationInfo findRelationInfo(String relationPropertyName) {
        assertStringNotNullAndNotTrimmedEmpty("relationPropertyName", relationPropertyName);
        return hasForeign(relationPropertyName) ? findForeignInfo(relationPropertyName)
                : findReferrerInfo(relationPropertyName);
    }

    // -----------------------------------------------------
    //                                       Foreign Element
    //                                       ---------------
    /**
     * {@inheritDoc}
     */
    public boolean hasForeign(String foreignPropertyName) {
        assertStringNotNullAndNotTrimmedEmpty("foreignPropertyName", foreignPropertyName);
        return getForeignInfoFlexibleMap().containsKey(foreignPropertyName);
    }

    /**
     * {@inheritDoc}
     */
    public DBMeta findForeignDBMeta(String foreignPropertyName) {
        return findForeignInfo(foreignPropertyName).getForeignDBMeta();
    }

    /**
     * {@inheritDoc}
     */
    public ForeignInfo findForeignInfo(String foreignPropertyName) {
        assertStringNotNullAndNotTrimmedEmpty("foreignPropertyName", foreignPropertyName);
        final ForeignInfo foreignInfo = getForeignInfoFlexibleMap().get(foreignPropertyName);
        if (foreignInfo == null) {
            throwDBMetaNotFoundException("The foreign info was not found.", "Foreign Property", foreignPropertyName);
        }
        return foreignInfo;
    }

    /**
     * {@inheritDoc}
     */
    public ForeignInfo findForeignInfo(int relationNo) {
        final ForeignInfo foreignInfo = getForeignInfoRelationNoKeyMap().get(relationNo);
        if (foreignInfo == null) {
            throwDBMetaNotFoundException("The foreign info was not found.", "Relation No", relationNo);
        }
        return foreignInfo;
    }

    protected ForeignInfo cfi(String constraintName, String foreignPropertyName // relation name
            , DBMeta localDbm, DBMeta foreignDbm // DB meta
            , Map<ColumnInfo, ColumnInfo> localForeignColumnInfoMap, int relationNo, Class<?> propertyAccessType // relation attribute
            , boolean oneToOne, boolean bizOneToOne, boolean referrerAsOne, boolean additionalFK // relation type
            , String fixedCondition, List<String> dynamicParameterList, boolean fixedInline // fixed condition
            , String reversePropertyName // various info
    ) { // createForeignInfo()
        final Class<?> realPt = chooseForeignPropertyAccessType(foreignDbm, propertyAccessType);
        final PropertyMethodFinder propertyMethodFinder = createForeignPropertyMethodFinder();
        return new ForeignInfo(constraintName, foreignPropertyName, localDbm, foreignDbm, localForeignColumnInfoMap,
                relationNo, realPt, oneToOne, bizOneToOne, referrerAsOne, additionalFK, fixedCondition,
                dynamicParameterList, fixedInline, reversePropertyName, propertyMethodFinder);
    }

    protected Class<?> chooseForeignPropertyAccessType(DBMeta foreignDbm, Class<?> specifiedType) {
        return specifiedType != null ? specifiedType : foreignDbm.getEntityType(); // basically default, or specified Optional
    }

    protected PropertyMethodFinder createForeignPropertyMethodFinder() {
        return new PropertyMethodFinder() {
            public Method findReadMethod(Class<?> beanType, String propertyName, Class<?> propertyAccessType) {
                return findPropertyReadMethod(beanType, propertyName, propertyAccessType);
            }

            public Method findWriteMethod(Class<?> beanType, String propertyName, Class<?> propertyAccessType) {
                return findPropertyWriteMethod(beanType, propertyName, propertyAccessType);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    public List<ForeignInfo> getForeignInfoList() {
        if (_foreignInfoList != null) {
            return _foreignInfoList;
        }
        synchronized (this) {
            if (_foreignInfoList != null) {
                return _foreignInfoList;
            }
            final Method[] methods = this.getClass().getMethods();
            _foreignInfoList = newArrayList();
            final String prefix = "foreign";
            final Class<ForeignInfo> returnType = ForeignInfo.class;
            for (Method method : methods) {
                if (method.getName().startsWith(prefix) && returnType.equals(method.getReturnType())) {
                    _foreignInfoList.add((ForeignInfo) DfReflectionUtil.invoke(method, this, null));
                }
            }
            return _foreignInfoList;
        }
    }

    /**
     * Get the flexible map of foreign information.
     * @return The flexible map of foreign information. (NotNull)
     */
    protected Map<String, ForeignInfo> getForeignInfoFlexibleMap() {
        if (_foreignInfoFlexibleMap != null) {
            return _foreignInfoFlexibleMap;
        }
        final List<ForeignInfo> foreignInfoList = getForeignInfoList();
        synchronized (this) {
            if (_foreignInfoFlexibleMap != null) {
                return _foreignInfoFlexibleMap;
            }
            _foreignInfoFlexibleMap = createFlexibleConcurrentMap();
            for (ForeignInfo foreignInfo : foreignInfoList) {
                _foreignInfoFlexibleMap.put(foreignInfo.getForeignPropertyName(), foreignInfo);
            }
            return _foreignInfoFlexibleMap;
        }
    }

    /**
     * Get the relation-no key map of foreign information.
     * @return The flexible map of foreign information. (NotNull)
     */
    protected Map<Integer, ForeignInfo> getForeignInfoRelationNoKeyMap() {
        if (_foreignInfoRelationNoKeyMap != null) {
            return _foreignInfoRelationNoKeyMap;
        }
        final List<ForeignInfo> foreignInfoList = getForeignInfoList();
        synchronized (this) {
            if (_foreignInfoRelationNoKeyMap != null) {
                return _foreignInfoRelationNoKeyMap;
            }
            _foreignInfoRelationNoKeyMap = newLinkedHashMap();
            for (ForeignInfo foreignInfo : foreignInfoList) {
                _foreignInfoRelationNoKeyMap.put(foreignInfo.getRelationNo(), foreignInfo);
            }
            return _foreignInfoRelationNoKeyMap;
        }
    }

    // -----------------------------------------------------
    //                                      Referrer Element
    //                                      ----------------
    /**
     * {@inheritDoc}
     */
    public boolean hasReferrer(String referrerPropertyName) {
        assertStringNotNullAndNotTrimmedEmpty("referrerPropertyName", referrerPropertyName);
        return getReferrerInfoFlexibleMap().containsKey(referrerPropertyName);
    }

    /**
     * {@inheritDoc}
     */
    public DBMeta findReferrerDBMeta(String referrerPropertyName) {
        assertStringNotNullAndNotTrimmedEmpty("referrerPropertyName", referrerPropertyName);
        return findReferrerInfo(referrerPropertyName).getReferrerDBMeta();
    }

    /**
     * {@inheritDoc}
     */
    public ReferrerInfo findReferrerInfo(String referrerPropertyName) {
        assertStringNotNullAndNotTrimmedEmpty("referrerPropertyName", referrerPropertyName);
        final ReferrerInfo referrerInfo = getReferrerInfoFlexibleMap().get(referrerPropertyName);
        if (referrerInfo == null) {
            throwDBMetaNotFoundException("The referrer info was not found.", "Referrer Property", referrerPropertyName);
        }
        return referrerInfo;
    }

    protected ReferrerInfo cri(String constraintName, String referrerPropertyName // relation name
            , DBMeta localDbm, DBMeta referrerDbm // DB meta
            , Map<ColumnInfo, ColumnInfo> localReferrerColumnInfoMap // relation attribute
            , boolean oneToOne, String reversePropertyName // relation type and various info
    ) { // createReferrerInfo()
        final Class<?> propertyAccessType = chooseReferrerPropertyAccessType(referrerDbm, oneToOne);
        final PropertyMethodFinder propertyMethodFinder = createReferrerPropertyMethodFinder();
        return new ReferrerInfo(constraintName, referrerPropertyName, localDbm, referrerDbm,
                localReferrerColumnInfoMap, propertyAccessType, oneToOne, reversePropertyName, propertyMethodFinder);
    }

    protected Class<?> chooseReferrerPropertyAccessType(DBMeta referrerDbm, boolean oneToOne) {
        final Class<?> propertyType;
        if (oneToOne) { // basically no way
            propertyType = referrerDbm.getEntityType();
        } else {
            final Class<?> listType = getReferrerPropertyListType();
            propertyType = listType != null ? listType : List.class;
        }
        return propertyType;
    }

    /**
     * Get the list type of referrer property in entity.
     * @return The class instance of list type. (NullAllowed: if null, Java's List used as default)
     */
    protected Class<?> getReferrerPropertyListType() { // might be overridden
        return null; // as default (List)
    }

    protected PropertyMethodFinder createReferrerPropertyMethodFinder() {
        return new PropertyMethodFinder() {
            public Method findReadMethod(Class<?> beanType, String propertyName, Class<?> propertyAccessType) {
                return findPropertyReadMethod(beanType, propertyName, propertyAccessType);
            }

            public Method findWriteMethod(Class<?> beanType, String propertyName, Class<?> propertyAccessType) {
                return findPropertyWriteMethod(beanType, propertyName, propertyAccessType);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    public List<ReferrerInfo> getReferrerInfoList() {
        if (_referrerInfoList != null) {
            return _referrerInfoList;
        }
        synchronized (this) {
            if (_referrerInfoList != null) {
                return _referrerInfoList;
            }
            final Method[] methods = this.getClass().getMethods();
            _referrerInfoList = newArrayList();
            final String prefix = "referrer";
            final Class<ReferrerInfo> returnType = ReferrerInfo.class;
            for (Method method : methods) {
                if (method.getName().startsWith(prefix) && returnType.equals(method.getReturnType())) {
                    _referrerInfoList.add((ReferrerInfo) DfReflectionUtil.invoke(method, this, null));
                }
            }
            return _referrerInfoList;
        }
    }

    /**
     * Get the flexible map of referrer information.
     * @return The flexible map of referrer information. (NotNull)
     */
    protected Map<String, ReferrerInfo> getReferrerInfoFlexibleMap() {
        if (_referrerInfoFlexibleMap != null) {
            return _referrerInfoFlexibleMap;
        }
        final List<ReferrerInfo> referrerInfoList = getReferrerInfoList();
        synchronized (this) {
            if (_referrerInfoFlexibleMap != null) {
                return _referrerInfoFlexibleMap;
            }
            _referrerInfoFlexibleMap = createFlexibleConcurrentMap();
            for (ReferrerInfo referrerInfo : referrerInfoList) {
                _referrerInfoFlexibleMap.put(referrerInfo.getReferrerPropertyName(), referrerInfo);
            }
            return _referrerInfoFlexibleMap;
        }
    }

    // -----------------------------------------------------
    //                                          Common Logic
    //                                          ------------
    protected String buildRelationInfoGetterMethodNameInitCap(String targetName, String relationPropertyName) {
        return targetName + relationPropertyName.substring(0, 1).toUpperCase() + relationPropertyName.substring(1);
    }

    // -----------------------------------------------------
    //                                        Relation Trace
    //                                        --------------
    /**
     * Relation trace.
     */
    protected static abstract class AbstractRelationTrace implements RelationTrace { // #later remove this since Java8

        /** The list of relation. */
        protected List<RelationInfo> _relationList;

        /** The list of relation trace. */
        protected List<AbstractRelationTrace> _relationTraceList;

        /** The list of relation info as trace. */
        protected List<RelationInfo> _traceRelationInfoList;

        /** The column info as trace. */
        protected ColumnInfo _traceColumnInfo;

        /** The handler of fixed relation trace. */
        protected RelationTraceFixHandler _relationTraceFixHandler;

        /**
         * Constructor for first step.
         * @param relationTraceFixHandler The handler of fixed relation trace. (NullAllowed)
         */
        public AbstractRelationTrace(RelationTraceFixHandler relationTraceFixHandler) {
            this(new ArrayList<RelationInfo>(), new ArrayList<AbstractRelationTrace>());
            this._relationTraceFixHandler = relationTraceFixHandler;
        }

        /**
         * Constructor for relation step.
         * @param relationList The list of relation. (NotNull)
         * @param relationTraceList The list of relation trace. (NotNull)
         */
        public AbstractRelationTrace(List<RelationInfo> relationList, List<AbstractRelationTrace> relationTraceList) {
            this._relationList = relationList;
            this._relationTraceList = relationTraceList;
            this._relationTraceList.add(this);
        }

        /**
         * {@inheritDoc}
         */
        public List<RelationInfo> getTraceRelation() {
            return _traceRelationInfoList;
        }

        /**
         * {@inheritDoc}
         */
        public ColumnInfo getTraceColumn() {
            return _traceColumnInfo;
        }

        /**
         * Fix trace.
         * @param traceRelationInfoList The trace of relation as the list of relation info. (NotNull)
         * @param traceColumnInfo The trace of column as column info. (NullAllowed)
         * @return Relation trace(result). (NotNull)
         */
        protected RelationTrace fixTrace(List<RelationInfo> traceRelationInfoList, ColumnInfo traceColumnInfo) {
            final AbstractRelationTrace localRelationTrace = (AbstractRelationTrace) _relationTraceList.get(0);
            localRelationTrace.setTraceRelation(traceRelationInfoList);
            localRelationTrace.setTraceColumn(traceColumnInfo);
            localRelationTrace.recycle();
            localRelationTrace.handleFixedRelationTrace();
            return localRelationTrace;
        }

        protected void setTraceRelation(List<RelationInfo> traceRelationInfoList) {
            this._traceRelationInfoList = traceRelationInfoList;
        }

        protected void setTraceColumn(ColumnInfo traceColumn) {
            this._traceColumnInfo = traceColumn;
        }

        protected void recycle() {
            this._relationList = new ArrayList<RelationInfo>();
            this._relationTraceList = new ArrayList<AbstractRelationTrace>();
            this._relationTraceList.add(this);
        }

        protected void handleFixedRelationTrace() {
            if (_relationTraceFixHandler != null) {
                _relationTraceFixHandler.handleFixedTrace(this);
            }
        }
    }

    // ===================================================================================
    //                                                                        Various Info
    //                                                                        ============
    // These methods is expected to override if it needs.
    public boolean hasIdentity() {
        return false;
    }

    public boolean hasSequence() {
        return false;
    }

    public String getSequenceName() {
        return null;
    }

    public String getSequenceNextValSql() {
        if (!hasSequence()) {
            return null;
        }
        return getCurrentDBDef().dbway().buildSequenceNextValSql(getSequenceName());
    }

    public Integer getSequenceIncrementSize() {
        return null;
    }

    public Integer getSequenceCacheSize() {
        return null;
    }

    public boolean hasOptimisticLock() {
        return hasVersionNo() || hasUpdateDate();
    }

    public boolean hasVersionNo() {
        return false;
    }

    public ColumnInfo getVersionNoColumnInfo() {
        return null;
    }

    public boolean hasUpdateDate() {
        return false;
    }

    public ColumnInfo getUpdateDateColumnInfo() {
        return null;
    }

    public boolean hasCommonColumn() {
        return false;
    }

    public List<ColumnInfo> getCommonColumnInfoList() {
        return DfCollectionUtil.emptyList();
    }

    public List<ColumnInfo> getCommonColumnInfoBeforeInsertList() {
        return DfCollectionUtil.emptyList();
    }

    public List<ColumnInfo> getCommonColumnInfoBeforeUpdateList() {
        return DfCollectionUtil.emptyList();
    }

    // ===================================================================================
    //                                                                       Name Handling
    //                                                                       =============
    /**
     * {@inheritDoc}
     */
    public boolean hasFlexibleName(String flexibleName) {
        assertStringNotNullAndNotTrimmedEmpty("flexibleName", flexibleName);

        // It uses column before table because column is used much more than table.
        // This is the same consideration at other methods.
        return getColumnInfoFlexibleMap().containsKey(flexibleName)
                || getTableDbNameFlexibleMap().containsKey(flexibleName);
    }

    /**
     * {@inheritDoc}
     */
    public String findDbName(String flexibleName) {
        assertStringNotNullAndNotTrimmedEmpty("flexibleName", flexibleName);
        final ColumnInfo columnInfoMap = getColumnInfoFlexibleMap().get(flexibleName);
        if (columnInfoMap != null) {
            return columnInfoMap.getColumnDbName();
        }
        final String tableDbName = getTableDbNameFlexibleMap().get(flexibleName);
        if (tableDbName != null) {
            return tableDbName;
        }
        throwDBMetaNotFoundException("The DB name was not found.", "Flexible Name", flexibleName);
        return null; // unreachable
    }

    /**
     * {@inheritDoc}
     */
    public String findPropertyName(String flexibleName) {
        assertStringNotNullAndNotTrimmedEmpty("flexibleName", flexibleName);
        final ColumnInfo columnInfoMap = getColumnInfoFlexibleMap().get(flexibleName);
        if (columnInfoMap != null) {
            return columnInfoMap.getPropertyName();
        }
        final String tablePropertyName = getTablePropertyNameFlexibleMap().get(flexibleName);
        if (tablePropertyName != null) {
            return tablePropertyName;
        }
        throwDBMetaNotFoundException("The property name was not found.", "Flexible Name", flexibleName);
        return null; // unreachable
    }

    // ===================================================================================
    //                                                                   Map Communication
    //                                                                   =================
    // -----------------------------------------------------
    //                                                Accept
    //                                                ------
    protected <ENTITY extends Entity> void doAcceptPrimaryKeyMap(ENTITY entity,
            Map<String, ? extends Object> primaryKeyMap) {
        if (primaryKeyMap == null || primaryKeyMap.isEmpty()) {
            String msg = "The argument 'primaryKeyMap' should not be null or empty:";
            msg = msg + " primaryKeyMap=" + primaryKeyMap;
            throw new IllegalArgumentException(msg);
        }
        final List<ColumnInfo> uniqueColumnList = getPrimaryUniqueInfo().getUniqueColumnList();
        doConvertToEntity(entity, primaryKeyMap, uniqueColumnList);
    }

    protected <ENTITY extends Entity> void doAcceptAllColumnMap(ENTITY entity,
            Map<String, ? extends Object> allColumnMap) {
        if (allColumnMap == null || allColumnMap.isEmpty()) {
            String msg = "The argument 'allColumnMap' should not be null or empty:";
            msg = msg + " allColumnMap=" + allColumnMap;
            throw new IllegalArgumentException(msg);
        }
        final List<ColumnInfo> uniqueColumnList = getColumnInfoList();
        doConvertToEntity(entity, allColumnMap, uniqueColumnList);
    }

    protected <ENTITY extends Entity> void doConvertToEntity(ENTITY entity, Map<String, ? extends Object> columnMap,
            List<ColumnInfo> columnInfoList) {
        entity.clearModifiedInfo();
        final MapStringValueAnalyzer analyzer = new MapStringValueAnalyzer(columnMap);
        for (ColumnInfo columnInfo : columnInfoList) {
            final String columnName = columnInfo.getColumnDbName();
            final String propertyName = columnInfo.getPropertyName();
            final String uncapPropName = initUncap(propertyName);
            final Class<?> nativeType = columnInfo.getObjectNativeType();
            if (analyzer.init(columnName, uncapPropName, propertyName)) {
                final Object value;
                if (String.class.isAssignableFrom(nativeType)) {
                    value = analyzer.analyzeString(nativeType);
                } else if (Number.class.isAssignableFrom(nativeType)) {
                    value = analyzer.analyzeNumber(nativeType);
                } else if (Date.class.isAssignableFrom(nativeType)) {
                    value = analyzer.analyzeDate(nativeType);
                } else if (Boolean.class.isAssignableFrom(nativeType)) {
                    value = analyzer.analyzeBoolean(nativeType);
                } else if (byte[].class.isAssignableFrom(nativeType)) {
                    value = analyzer.analyzeBinary(nativeType);
                } else if (UUID.class.isAssignableFrom(nativeType)) {
                    value = analyzer.analyzeUUID(nativeType);
                } else {
                    value = analyzer.analyzeOther(nativeType);
                }
                columnInfo.write(entity, value);
            }
        }
    }

    // -----------------------------------------------------
    //                                               Extract
    //                                               -------
    protected Map<String, Object> doExtractPrimaryKeyMap(Entity entity) {
        assertObjectNotNull("entity", entity);
        return doConvertToColumnValueMap(entity, true);
    }

    protected Map<String, Object> doExtractAllColumnMap(Entity entity) {
        assertObjectNotNull("entity", entity);
        return doConvertToColumnValueMap(entity, false);
    }

    protected Map<String, Object> doConvertToColumnValueMap(Entity entity, boolean pkOnly) {
        final Map<String, Object> valueMap = newLinkedHashMap();
        final List<ColumnInfo> columnInfoList;
        if (pkOnly) {
            columnInfoList = getPrimaryUniqueInfo().getUniqueColumnList();
        } else {
            columnInfoList = getColumnInfoList();
        }
        for (ColumnInfo columnInfo : columnInfoList) {
            final String columnName = columnInfo.getColumnDbName();
            final Object value = columnInfo.read(entity);
            valueMap.put(columnName, value);
        }
        return valueMap;
    }

    // -----------------------------------------------------
    //                                              Analyzer
    //                                              --------
    /**
     * This class is for internal. Don't use this!
     */
    protected static class MapStringValueAnalyzer {
        protected final Map<String, ? extends Object> _valueMap;
        protected String _columnName;
        protected String _uncapPropName;
        protected String _propertyName;

        public MapStringValueAnalyzer(Map<String, ? extends Object> valueMap) {
            _valueMap = valueMap;
        }

        public boolean init(String columnName, String uncapPropName, String propertyName) {
            _columnName = columnName;
            _uncapPropName = uncapPropName;
            _propertyName = propertyName;
            return _valueMap.containsKey(_columnName);
        }

        @SuppressWarnings("unchecked")
        public <PROPERTY> PROPERTY analyzeString(Class<PROPERTY> javaType) {
            final Object obj = getColumnValue();
            return (PROPERTY) DfTypeUtil.toString(obj);
        }

        @SuppressWarnings("unchecked")
        public <PROPERTY> PROPERTY analyzeNumber(Class<PROPERTY> javaType) {
            final Object obj = getColumnValue();
            return (PROPERTY) DfTypeUtil.toNumber(obj, javaType);
        }

        @SuppressWarnings("unchecked")
        public <PROPERTY> PROPERTY analyzeDate(Class<PROPERTY> javaType) {
            final Object obj = getColumnValue();
            if (Time.class.isAssignableFrom(javaType)) {
                return (PROPERTY) DfTypeUtil.toTime(obj);
            } else if (Timestamp.class.isAssignableFrom(javaType)) {
                return (PROPERTY) DfTypeUtil.toTimestamp(obj);
            } else {
                return (PROPERTY) DfTypeUtil.toDate(obj);
            }
        }

        @SuppressWarnings("unchecked")
        public <PROPERTY> PROPERTY analyzeBoolean(Class<PROPERTY> javaType) {
            final Object obj = getColumnValue();
            return (PROPERTY) DfTypeUtil.toBoolean(obj);
        }

        @SuppressWarnings("unchecked")
        public <PROPERTY> PROPERTY analyzeBinary(Class<PROPERTY> javaType) {
            final Object obj = getColumnValue();
            if (obj == null) {
                return null;
            }
            if (obj instanceof Serializable) {
                return (PROPERTY) DfTypeUtil.toBinary((Serializable) obj);
            }
            throw new UnsupportedOperationException("unsupported binary type: " + obj.getClass());
        }

        @SuppressWarnings("unchecked")
        public <PROPERTY> PROPERTY analyzeUUID(Class<PROPERTY> javaType) {
            final Object obj = getColumnValue();
            return (PROPERTY) DfTypeUtil.toUUID(obj);
        }

        @SuppressWarnings("unchecked")
        public <PROPERTY> PROPERTY analyzeOther(Class<PROPERTY> javaType) {
            final Object obj = getColumnValue();
            if (obj == null) {
                return null;
            }
            if (Classification.class.isAssignableFrom(javaType)) {
                final Class<?>[] argTypes = new Class[] { Object.class };
                final Method method = DfReflectionUtil.getPublicMethod(javaType, "codeOf", argTypes);
                return (PROPERTY) DfReflectionUtil.invokeStatic(method, new Object[] { obj });
            }
            return (PROPERTY) obj;
        }

        protected Object getColumnValue() {
            final Object value = _valueMap.get(_columnName);
            return filterClassificationValue(value);
        }

        protected Object filterClassificationValue(Object value) {
            if (value != null && value instanceof Classification) {
                value = ((Classification) value).code();
            }
            return value;
        }
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    @SuppressWarnings("unchecked")
    protected <ENTITY> ENTITY downcast(Entity entity) {
        checkDowncast(entity);
        return (ENTITY) entity;
    }

    protected void checkDowncast(Entity entity) {
        assertObjectNotNull("entity", entity);
        final Class<?> entityType = getEntityType();
        final Class<?> targetType = entity.getClass();
        if (!entityType.isAssignableFrom(targetType)) {
            final String titleName = DfTypeUtil.toClassTitle(entityType);
            String msg = "The entity should be " + titleName + " but it was: " + targetType;
            throw new IllegalStateException(msg);
        }
    }

    protected Map<String, String> setupKeyToLowerMap(boolean dbNameKey) {
        final Map<String, String> map;
        if (dbNameKey) {
            map = newConcurrentHashMap(getTableDbName().toLowerCase(), getTablePropertyName());
        } else {
            map = newConcurrentHashMap(getTablePropertyName().toLowerCase(), getTableDbName());
        }
        final Method[] methods = this.getClass().getMethods();
        final String columnInfoMethodPrefix = "column";
        try {
            for (Method method : methods) {
                final String name = method.getName();
                if (!name.startsWith(columnInfoMethodPrefix)) {
                    continue;
                }
                final ColumnInfo columnInfo = (ColumnInfo) method.invoke(this);
                final String dbName = columnInfo.getColumnDbName();
                final String propertyName = columnInfo.getPropertyName();
                if (dbNameKey) {
                    map.put(dbName.toLowerCase(), propertyName);
                } else {
                    map.put(propertyName.toLowerCase(), dbName);
                }
            }
            return Collections.unmodifiableMap(map);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    protected void throwDBMetaNotFoundException(String notice, String keyName, Object value) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice(notice);
        br.addItem("Table");
        br.addElement(getTableDbName());
        br.addItem(keyName);
        br.addElement(value);
        final String msg = br.buildExceptionMessage();
        throw new DBMetaNotFoundException(msg);
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    // -----------------------------------------------------
    //                                       String Handling
    //                                       ---------------
    protected final String replaceString(String text, String fromText, String toText) {
        return Srl.replace(text, fromText, toText);
    }

    protected final List<String> splitListTrimmed(String str, String delimiter) {
        return Srl.splitListTrimmed(str, delimiter);
    }

    protected final String initCap(String str) {
        return Srl.initCap(str);
    }

    protected final String initUncap(String str) {
        return Srl.initUncap(str);
    }

    protected final String ln() {
        return DBFluteSystem.getBasicLn();
    }

    // -----------------------------------------------------
    //                                  Collection Generator
    //                                  --------------------
    protected <KEY, VALUE> HashMap<KEY, VALUE> newHashMap() {
        return DfCollectionUtil.newHashMap();
    }

    protected <KEY, VALUE> ConcurrentHashMap<KEY, VALUE> newConcurrentHashMap() {
        return DfCollectionUtil.newConcurrentHashMap();
    }

    protected <KEY, VALUE> ConcurrentHashMap<KEY, VALUE> newConcurrentHashMap(KEY key, VALUE value) {
        final ConcurrentHashMap<KEY, VALUE> map = newConcurrentHashMap();
        map.put(key, value);
        return map;
    }

    protected <KEY, VALUE> LinkedHashMap<KEY, VALUE> newLinkedHashMap() {
        return DfCollectionUtil.newLinkedHashMap();
    }

    protected <KEY, VALUE> LinkedHashMap<KEY, VALUE> newLinkedHashMap(KEY key, VALUE value) {
        final LinkedHashMap<KEY, VALUE> map = newLinkedHashMap();
        map.put(key, value);
        return map;
    }

    protected <KEY, VALUE> LinkedHashMap<KEY, VALUE> newLinkedHashMapSized(int size) {
        return DfCollectionUtil.newLinkedHashMapSized(size);
    }

    protected <ELEMENT> ArrayList<ELEMENT> newArrayList() {
        return DfCollectionUtil.newArrayList();
    }

    protected <ELEMENT> List<ELEMENT> newArrayList(ELEMENT... elements) {
        final List<ELEMENT> list = newArrayList();
        for (ELEMENT element : elements) {
            list.add(element);
        }
        return list;
    }

    protected <ELEMENT> ArrayList<ELEMENT> newArrayList(Collection<ELEMENT> collection) {
        return DfCollectionUtil.newArrayList(collection);
    }

    protected <ELEMENT> ArrayList<ELEMENT> newArrayListSized(int size) {
        return DfCollectionUtil.newArrayListSized(size);
    }

    protected <VALUE> StringKeyMap<VALUE> createFlexibleConcurrentMap() {
        return StringKeyMap.createAsFlexibleConcurrent();
    }

    // -----------------------------------------------------
    //                                         Assert Object
    //                                         -------------
    /**
     * Assert that the argument is not null.
     * @param variableName Variable name. (NotNull)
     * @param value Value. (NotNull)
     */
    protected void assertObjectNotNull(String variableName, Object value) {
        DfAssertUtil.assertObjectNotNull(variableName, value);
    }

    // -----------------------------------------------------
    //                                         Assert String
    //                                         -------------
    /**
     * Assert that the string is not null and not trimmed empty.
     * @param variableName Variable name. (NotNull)
     * @param value Value. (NotNull)
     */
    protected void assertStringNotNullAndNotTrimmedEmpty(String variableName, String value) {
        DfAssertUtil.assertStringNotNullAndNotTrimmedEmpty(variableName, value);
    }
}
