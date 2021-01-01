/*
 * Copyright 2014-2021 the original author or authors.
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
package org.dbflute.dbmeta;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.dbflute.Entity;
import org.dbflute.FunCustodial;
import org.dbflute.dbmeta.info.ColumnInfo;
import org.dbflute.dbmeta.info.ForeignInfo;
import org.dbflute.dbmeta.info.PrimaryInfo;
import org.dbflute.dbmeta.info.ReferrerInfo;
import org.dbflute.dbmeta.info.RelationInfo;
import org.dbflute.dbmeta.info.UniqueInfo;
import org.dbflute.dbmeta.property.DelegatingPropertyGateway;
import org.dbflute.dbmeta.property.PropertyGateway;
import org.dbflute.dbmeta.property.PropertyMethodFinder;
import org.dbflute.dbmeta.property.PropertyReader;
import org.dbflute.dbmeta.property.PropertyWriter;
import org.dbflute.dbmeta.valuemap.MetaHandlingEntityToMapMapper;
import org.dbflute.dbmeta.valuemap.MetaHandlingMapToEntityMapper;
import org.dbflute.exception.DBMetaNotFoundException;
import org.dbflute.helper.StringKeyMap;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.jdbc.Classification;
import org.dbflute.jdbc.ClassificationMeta;
import org.dbflute.jdbc.ClassificationUndefinedHandlingType;
import org.dbflute.optional.OptionalObject;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.util.DfAssertUtil;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfReflectionUtil;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.Srl;

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
    private volatile List<ColumnInfo> _columnInfoList;
    private volatile StringKeyMap<ColumnInfo> _columnInfoFlexibleMap;
    private volatile PrimaryInfo _primaryInfo;
    private volatile List<UniqueInfo> _uniqueInfoList;
    private volatile List<ForeignInfo> _foreignInfoList;
    private volatile Map<String, ForeignInfo> _foreignInfoFlexibleMap;
    private volatile Map<Integer, ForeignInfo> _foreignInfoRelationNoKeyMap;
    private volatile List<ReferrerInfo> _referrerInfoList;
    private volatile Map<String, ReferrerInfo> _referrerInfoFlexibleMap;

    // ===================================================================================
    //                                                             Resource Initialization
    //                                                             =======================
    protected void initializeInformationResource() { // for instance initializer of subclass.
        getColumnInfoList(); // initialize the list of column information
        getColumnInfoFlexibleMap(); // initialize the flexible map of column information
        if (hasPrimaryKey()) {
            getPrimaryInfo(); // initialize the primary unique information
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
    // old style for 1.0.x
    //protected void setupEpg(Map<String, PropertyGateway> propertyGatewayMap, PropertyGateway gateway, String propertyName) {
    //    propertyGatewayMap.put(propertyName, gateway); // the map should be plain map for performance
    //}

    protected void setupEpg(Map<String, PropertyGateway> propertyGatewayMap, PropertyReader reader, PropertyWriter writer,
            String propertyName) {
        final DelegatingPropertyGateway gateway = new DelegatingPropertyGateway(reader, writer);
        propertyGatewayMap.put(propertyName, gateway); // the map should be plain map for performance
    }

    public PropertyGateway findPropertyGateway(String propertyName) {
        return null; // should be overridden
    }

    protected <ENTITY extends Entity> PropertyGateway doFindEpg(Map<String, PropertyGateway> propertyGatewayMap, String propertyName) {
        return propertyGatewayMap.get(propertyName);
    }

    // -----------------------------------------------------
    //                                      Foreign Property
    //                                      ----------------
    // old style for 1.0.x
    //protected void setupEfpg(Map<String, PropertyGateway> propertyGatewayMap, PropertyGateway gateway, String foreignPropertyName) {
    //    propertyGatewayMap.put(foreignPropertyName, gateway); // the map should be plain map for performance
    //}

    protected void setupEfpg(Map<String, PropertyGateway> propertyGatewayMap, PropertyReader reader, PropertyWriter writer,
            String foreignPropertyName) {
        final DelegatingPropertyGateway gateway = new DelegatingPropertyGateway(reader, writer);
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
    protected static void ccls(Entity entity, ColumnInfo columnInfo, Object code) { // checkClassification
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
        final Classification classification = gcls(entity, columnInfo, code);
        if (classification == null) {
            final String tableDbName = columnInfo.getDBMeta().getTableDbName();
            final String columnDbName = columnInfo.getColumnDbName();
            final boolean allowedByOption = entity.myundefinedClassificationAccessAllowed();
            FunCustodial.handleUndefinedClassificationCode(tableDbName, columnDbName, meta, code, allowedByOption);
        }
    }

    protected static Classification gcls(Entity entity, ColumnInfo columnInfo, Object code) { // getClassification
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

    protected static LocalDate ctld(Object value) { // convertToLocalDate
        return DfTypeUtil.toLocalDate(value);
    }

    protected static LocalDateTime ctldt(Object value) { // convertToLocalDateTime
        return DfTypeUtil.toLocalDateTime(value);
    }

    protected static LocalTime ctlt(Object value) { // convertToLocalTime
        return DfTypeUtil.toLocalTime(value);
    }

    protected static Date ctdt(Object value) { // convertToDate
        return DfTypeUtil.toDate(value);
    }

    protected static Timestamp cttp(Object value) { // convertToTimestamp
        return DfTypeUtil.toTimestamp(value);
    }

    protected static Time cttm(Object value) { // convertToTime
        return DfTypeUtil.toTime(value);
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

    // ===================================================================================
    //                                                                         Column Info
    //                                                                         ===========
    /** {@inheritDoc} */
    public boolean hasColumn(String columnFlexibleName) {
        assertStringNotNullAndNotTrimmedEmpty("columnFlexibleName", columnFlexibleName);
        return getColumnInfoFlexibleMap().containsKey(columnFlexibleName);
    }

    /** {@inheritDoc} */
    public ColumnInfo findColumnInfo(String columnFlexibleName) {
        assertStringNotNullAndNotTrimmedEmpty("columnFlexibleName", columnFlexibleName);
        final Map<String, ColumnInfo> flexibleMap = getColumnInfoFlexibleMap();
        final ColumnInfo columnInfo = flexibleMap.get(columnFlexibleName);
        if (columnInfo == null) {
            final String notice = "The column info was not found.";
            final String keyName = "Column";
            throwDBMetaNotFoundException(notice, keyName, columnFlexibleName, flexibleMap.keySet());
        }
        return columnInfo;
    }

    protected ColumnInfo cci(String columnDbName, String columnSqlName, String columnSynonym, String columnAlias // column name
            , Class<?> objectNativeType, String propertyName, Class<?> propertyAccessType // property info
            , boolean primary, boolean autoIncrement, boolean notNull // column basic check
            , String columnDbType, Integer columnSize, Integer decimalDigits, Integer datetimePrecision, String defaultValue // column type
            , boolean commonColumn, OptimisticLockType optimisticLockType, String columnComment // column others
            , String foreignListExp, String referrerListExp // relation property
            , ClassificationMeta classificationMeta, boolean canBeNullObject // various info
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
        return new ColumnInfo(this, columnDbName, columnSqlName, columnSynonym, columnAlias, objectNativeType, propertyName, realPt,
                primary, autoIncrement, notNull, columnDbType, columnSize, decimalDigits, datetimePrecision, defaultValue, commonColumn,
                optimisticLockType, columnComment, foreignPropList, referrerPropList, classificationMeta, canBeNullObject,
                propertyMethodFinder);
    }

    protected Class<?> chooseColumnPropertyAccessType(Class<?> objectNativeType, String propertyName, Class<?> propertyAccessType) {
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

    /** {@inheritDoc} */
    public List<ColumnInfo> getColumnInfoList() {
        if (_columnInfoList != null) {
            return _columnInfoList;
        }
        synchronized (this) {
            if (_columnInfoList != null) {
                return _columnInfoList;
            }
            _columnInfoList = Collections.unmodifiableList(ccil());
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
    // -----------------------------------------------------
    //                                           Primary Key
    //                                           -----------
    /** {@inheritDoc} */
    public PrimaryInfo getPrimaryInfo() {
        if (_primaryInfo != null) {
            return _primaryInfo;
        }
        synchronized (this) {
            if (_primaryInfo != null) {
                return _primaryInfo;
            }
            _primaryInfo = new PrimaryInfo(cpui());
            return _primaryInfo;
        }
    }

    protected abstract UniqueInfo cpui(); // createPrimaryUniqueInfo()

    /** {@inheritDoc} */
    @SuppressWarnings("deprecation")
    public UniqueInfo getPrimaryUniqueInfo() { // old style
        return getPrimaryInfo().getUniqueInfo();
    }

    protected UniqueInfo hpcpui(ColumnInfo uniqueColumnInfo) { // helpCreatePrimaryUniqueInfo()
        return hpcpui(Arrays.asList(uniqueColumnInfo));
    }

    protected UniqueInfo hpcpui(List<ColumnInfo> uniqueColumnInfoList) { // helpCreatePrimaryUniqueInfo()
        return new UniqueInfo(this, uniqueColumnInfoList, true);
    }

    /** {@inheritDoc} */
    public OptionalObject<PrimaryInfo> searchPrimaryInfo(Collection<ColumnInfo> columnInfoList) {
        final PrimaryInfo primaryInfo = getPrimaryInfo(); // exception if no PK
        final Set<ColumnInfo> colSet = new HashSet<ColumnInfo>(columnInfoList);
        final List<ColumnInfo> pkList = primaryInfo.getPrimaryColumnList();
        for (ColumnInfo pk : pkList) {
            if (!colSet.contains(pk)) {
                return OptionalObject.ofNullable(null, () -> {
                    throwSpecifiedColumnNotPrimaryException(columnInfoList, pkList);
                });
            }
        }
        return OptionalObject.of(primaryInfo);
    }

    protected void throwSpecifiedColumnNotPrimaryException(Collection<ColumnInfo> columnInfoList, List<ColumnInfo> pkList) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the primary key by the columns");
        br.addItem("Table");
        br.addElement(getTableDbName());
        br.addItem("Specified Column List");
        br.addElement(columnInfoList);
        br.addItem("Existing PrimaryKey");
        br.addElement(pkList);
        final String msg = br.buildExceptionMessage();
        throw new DBMetaNotFoundException(msg); // for compatible and uniformity
    }

    // -----------------------------------------------------
    //                                        Natural Unique
    //                                        --------------
    /** {@inheritDoc} */
    public List<UniqueInfo> getUniqueInfoList() {
        if (_uniqueInfoList != null) {
            return _uniqueInfoList;
        }
        synchronized (this) {
            if (_uniqueInfoList != null) {
                return _uniqueInfoList;
            }
            final Method[] methods = this.getClass().getMethods();
            final List<UniqueInfo> workingList = newArrayListSized(4);
            final String prefix = "uniqueOf";
            final Class<UniqueInfo> returnType = UniqueInfo.class;
            for (Method method : methods) {
                if (method.getName().startsWith(prefix) && returnType.equals(method.getReturnType())) {
                    workingList.add((UniqueInfo) DfReflectionUtil.invoke(method, this, null));
                }
            }
            _uniqueInfoList = Collections.unmodifiableList(workingList);
            return _uniqueInfoList;
        }
    }

    protected UniqueInfo hpcui(ColumnInfo uniqueColumnInfo) { // helpCreateUniqueInfo()
        return hpcui(Arrays.asList(uniqueColumnInfo));
    }

    protected UniqueInfo hpcui(java.util.List<ColumnInfo> uniqueColumnInfoList) { // helpCreateUniqueInfo()
        return new UniqueInfo(this, uniqueColumnInfoList, false);
    }

    /** {@inheritDoc} */
    public List<UniqueInfo> searchUniqueInfoList(Collection<ColumnInfo> columnInfoList) {
        return doSearchMetaInfoList(columnInfoList, getUniqueInfoList(), info -> {
            return info.getUniqueColumnList();
        });
    }

    protected <INFO> List<INFO> doSearchMetaInfoList(Collection<ColumnInfo> columnInfoList, List<INFO> infoList,
            Function<INFO, Collection<ColumnInfo>> oneArgLambda) {
        if (infoList.isEmpty()) {
            return DfCollectionUtil.emptyList();
        }
        final Set<ColumnInfo> specifiedColSet = new HashSet<ColumnInfo>(columnInfoList);
        final List<INFO> foundInfoList = newArrayListSized(infoList.size());
        for (INFO info : infoList) {
            final Collection<ColumnInfo> columnList = oneArgLambda.apply(info);
            boolean notFound = false;
            for (ColumnInfo metaCol : columnList) {
                if (!specifiedColSet.contains(metaCol)) {
                    notFound = true;
                    break;
                }
            }
            if (!notFound) {
                foundInfoList.add(info);
            }
        }
        return Collections.unmodifiableList(foundInfoList);
    }

    // ===================================================================================
    //                                                                       Relation Info
    //                                                                       =============
    /**
     * {@inheritDoc}
     */
    public RelationInfo findRelationInfo(String relationPropertyName) {
        assertStringNotNullAndNotTrimmedEmpty("relationPropertyName", relationPropertyName);
        return hasForeign(relationPropertyName) ? findForeignInfo(relationPropertyName) : findReferrerInfo(relationPropertyName);
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

    /** {@inheritDoc} */
    public DBMeta findForeignDBMeta(String foreignPropertyName) {
        return findForeignInfo(foreignPropertyName).getForeignDBMeta();
    }

    /** {@inheritDoc} */
    public ForeignInfo findForeignInfo(String foreignPropertyName) {
        assertStringNotNullAndNotTrimmedEmpty("foreignPropertyName", foreignPropertyName);
        final Map<String, ForeignInfo> flexibleMap = getForeignInfoFlexibleMap();
        final ForeignInfo foreignInfo = flexibleMap.get(foreignPropertyName);
        if (foreignInfo == null) {
            final String notice = "The foreign info was not found.";
            final String keyName = "Foreign Property";
            throwDBMetaNotFoundException(notice, keyName, foreignPropertyName, flexibleMap.keySet());
        }
        return foreignInfo;
    }

    /** {@inheritDoc} */
    public ForeignInfo findForeignInfo(int relationNo) {
        final Map<Integer, ForeignInfo> relationNoKeyMap = getForeignInfoRelationNoKeyMap();
        final ForeignInfo foreignInfo = relationNoKeyMap.get(relationNo);
        if (foreignInfo == null) {
            final String notice = "The foreign info was not found.";
            final String keyName = "Relation No";
            throwDBMetaNotFoundException(notice, keyName, relationNo, relationNoKeyMap.keySet());
        }
        return foreignInfo;
    }

    protected ForeignInfo cfi(String constraintName, String foreignPropertyName // relation name
            , DBMeta localDbm, DBMeta foreignDbm // DB meta
            , Map<ColumnInfo, ColumnInfo> localForeignColumnInfoMap, int relationNo, Class<?> propertyAccessType // relation attribute
            , boolean oneToOne, boolean bizOneToOne, boolean referrerAsOne, boolean additionalFK // relation type
            , String fixedCondition, List<String> dynamicParameterList, boolean fixedInline // fixed condition
            , String reversePropertyName, boolean canBeNullObject // various info
    ) { // createForeignInfo()
        final Class<?> realPt = chooseForeignPropertyAccessType(foreignDbm, propertyAccessType);
        final PropertyMethodFinder propertyMethodFinder = createForeignPropertyMethodFinder();
        return new ForeignInfo(constraintName, foreignPropertyName, localDbm, foreignDbm, localForeignColumnInfoMap, relationNo, realPt,
                oneToOne, bizOneToOne, referrerAsOne, additionalFK, fixedCondition, dynamicParameterList, fixedInline, reversePropertyName,
                canBeNullObject, propertyMethodFinder);
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

    /** {@inheritDoc} */
    public List<ForeignInfo> getForeignInfoList() {
        if (_foreignInfoList != null) {
            return _foreignInfoList;
        }
        synchronized (this) {
            if (_foreignInfoList != null) {
                return _foreignInfoList;
            }
            final Method[] methods = this.getClass().getMethods();
            final List<ForeignInfo> workingList = newArrayList();
            final String prefix = "foreign";
            final Class<ForeignInfo> returnType = ForeignInfo.class;
            for (Method method : methods) {
                if (method.getName().startsWith(prefix) && returnType.equals(method.getReturnType())) {
                    workingList.add((ForeignInfo) DfReflectionUtil.invoke(method, this, null));
                }
            }
            _foreignInfoList = Collections.unmodifiableList(workingList);
            return _foreignInfoList;
        }
    }

    /**
     * Get the flexible map of foreign information.
     * @return The flexible map of foreign information. (NotNull, EmptyAllowed, ReadOnly)
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
            final StringKeyMap<ForeignInfo> map = createFlexibleConcurrentMap();
            for (ForeignInfo foreignInfo : foreignInfoList) {
                map.put(foreignInfo.getForeignPropertyName(), foreignInfo);
            }
            _foreignInfoFlexibleMap = Collections.unmodifiableMap(map);
            return _foreignInfoFlexibleMap;
        }
    }

    /**
     * Get the relation-no key map of foreign information.
     * @return The unordered map of foreign information. (NotNull, EmptyAllowed, ReadOnly)
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
            final Map<Integer, ForeignInfo> map = newConcurrentHashMap();
            for (ForeignInfo foreignInfo : foreignInfoList) {
                map.put(foreignInfo.getRelationNo(), foreignInfo);
            }
            _foreignInfoRelationNoKeyMap = Collections.unmodifiableMap(map);
            return _foreignInfoRelationNoKeyMap;
        }
    }

    /** {@inheritDoc} */
    public List<ForeignInfo> searchForeignInfoList(Collection<ColumnInfo> columnInfoList) {
        return doSearchMetaInfoList(columnInfoList, getForeignInfoList(), info -> {
            return info.getLocalForeignColumnInfoMap().keySet();
        });
    }

    // -----------------------------------------------------
    //                                      Referrer Element
    //                                      ----------------
    /** {@inheritDoc} */
    public boolean hasReferrer(String referrerPropertyName) {
        assertStringNotNullAndNotTrimmedEmpty("referrerPropertyName", referrerPropertyName);
        return getReferrerInfoFlexibleMap().containsKey(referrerPropertyName);
    }

    /** {@inheritDoc} */
    public DBMeta findReferrerDBMeta(String referrerPropertyName) {
        assertStringNotNullAndNotTrimmedEmpty("referrerPropertyName", referrerPropertyName);
        return findReferrerInfo(referrerPropertyName).getReferrerDBMeta();
    }

    /** {@inheritDoc} */
    public ReferrerInfo findReferrerInfo(String referrerPropertyName) {
        assertStringNotNullAndNotTrimmedEmpty("referrerPropertyName", referrerPropertyName);
        final Map<String, ReferrerInfo> flexibleMap = getReferrerInfoFlexibleMap();
        final ReferrerInfo referrerInfo = flexibleMap.get(referrerPropertyName);
        if (referrerInfo == null) {
            final String notice = "The referrer info was not found.";
            final String keyName = "Referrer Property";
            throwDBMetaNotFoundException(notice, keyName, referrerPropertyName, flexibleMap.keySet());
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
        return new ReferrerInfo(constraintName, referrerPropertyName, localDbm, referrerDbm, localReferrerColumnInfoMap, propertyAccessType,
                oneToOne, reversePropertyName, propertyMethodFinder);
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

    /** {@inheritDoc} */
    public List<ReferrerInfo> getReferrerInfoList() {
        if (_referrerInfoList != null) {
            return _referrerInfoList;
        }
        synchronized (this) {
            if (_referrerInfoList != null) {
                return _referrerInfoList;
            }
            final Method[] methods = this.getClass().getMethods();
            final List<ReferrerInfo> workingList = newArrayList();
            final String prefix = "referrer";
            final Class<ReferrerInfo> returnType = ReferrerInfo.class;
            for (Method method : methods) {
                if (method.getName().startsWith(prefix) && returnType.equals(method.getReturnType())) {
                    workingList.add((ReferrerInfo) DfReflectionUtil.invoke(method, this, null));
                }
            }
            _referrerInfoList = Collections.unmodifiableList(workingList);
            return _referrerInfoList;
        }
    }

    /**
     * Get the flexible map of referrer information.
     * @return The flexible map of referrer information. (NotNull, EmptyAllowed, ReadOnly)
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
            final StringKeyMap<ReferrerInfo> map = createFlexibleConcurrentMap();
            for (ReferrerInfo referrerInfo : referrerInfoList) {
                map.put(referrerInfo.getReferrerPropertyName(), referrerInfo);
            }
            _referrerInfoFlexibleMap = Collections.unmodifiableMap(map);
            return _referrerInfoFlexibleMap;
        }
    }

    /** {@inheritDoc} */
    public List<ReferrerInfo> searchReferrerInfoList(Collection<ColumnInfo> columnInfoList) {
        return doSearchMetaInfoList(columnInfoList, getReferrerInfoList(), info -> {
            return info.getLocalReferrerColumnInfoMap().keySet();
        });
    }

    // -----------------------------------------------------
    //                                          Common Logic
    //                                          ------------
    protected String buildRelationInfoGetterMethodNameInitCap(String targetName, String relationPropertyName) {
        return targetName + relationPropertyName.substring(0, 1).toUpperCase() + relationPropertyName.substring(1);
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
    //                                                                   Map Communication
    //                                                                   =================
    // -----------------------------------------------------
    //                                                Accept
    //                                                ------
    protected <ENTITY extends Entity> void doAcceptPrimaryKeyMap(ENTITY entity, Map<String, ? extends Object> primaryKeyMap) {
        assertObjectNotNull("entity", entity);
        if (primaryKeyMap == null || primaryKeyMap.isEmpty()) {
            String msg = "The argument 'primaryKeyMap' should not be null or empty: primaryKeyMap=" + primaryKeyMap;
            throw new IllegalArgumentException(msg);
        }
        doConvertToEntity(entity, primaryKeyMap, true);
    }

    protected <ENTITY extends Entity> void doAcceptAllColumnMap(ENTITY entity, Map<String, ? extends Object> allColumnMap) {
        assertObjectNotNull("entity", entity);
        if (allColumnMap == null || allColumnMap.isEmpty()) {
            String msg = "The argument 'allColumnMap' should not be null or empty: allColumnMap=" + allColumnMap;
            throw new IllegalArgumentException(msg);
        }
        doConvertToEntity(entity, allColumnMap, false);
    }

    protected <ENTITY extends Entity> void doConvertToEntity(ENTITY entity, Map<String, ? extends Object> columnMap, boolean pkOnly) {
        final List<ColumnInfo> columnInfoList = pkOnly ? getPrimaryInfo().getPrimaryColumnList() : getColumnInfoList();
        final MetaHandlingMapToEntityMapper mapper = createMetaHandlingMapToEntityMapper(columnMap);
        mapper.mappingToEntity(entity, columnMap, columnInfoList);
    }

    protected MetaHandlingMapToEntityMapper createMetaHandlingMapToEntityMapper(Map<String, ? extends Object> columnMap) {
        return new MetaHandlingMapToEntityMapper(columnMap);
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
        final List<ColumnInfo> columnInfoList = pkOnly ? getPrimaryInfo().getPrimaryColumnList() : getColumnInfoList();
        final MetaHandlingEntityToMapMapper mapper = createMetaHandlingEntityToMapMapper(entity);
        return mapper.mappingToColumnValueMap(columnInfoList);
    }

    protected MetaHandlingEntityToMapMapper createMetaHandlingEntityToMapMapper(Entity entity) {
        return new MetaHandlingEntityToMapMapper(entity);
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

    protected void throwDBMetaNotFoundException(String notice, String keyName, Object value, Set<? extends Object> keySet) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice(notice);
        br.addItem("Table");
        br.addElement(getTableDbName());
        br.addItem(keyName);
        br.addElement(value);
        br.addItem("Existing KeySet");
        br.addElement(keySet);
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
        return DBFluteSystem.ln();
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

    @SafeVarargs
    protected final <ELEMENT> List<ELEMENT> newArrayList(ELEMENT... elements) {
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
     * @param variableName The check name of variable for message. (NotNull)
     * @param value The checked value. (NotNull)
     * @throws IllegalArgumentException When the argument is null.
     */
    protected void assertObjectNotNull(String variableName, Object value) {
        DfAssertUtil.assertObjectNotNull(variableName, value);
    }

    // -----------------------------------------------------
    //                                         Assert String
    //                                         -------------
    /**
     * Assert that the string is not null and not trimmed empty.
     * @param variableName The check name of variable for message. (NotNull)
     * @param value The checked value. (NotNull)
     * @throws IllegalArgumentException When the argument is null or empty.
     */
    protected void assertStringNotNullAndNotTrimmedEmpty(String variableName, String value) {
        DfAssertUtil.assertStringNotNullAndNotTrimmedEmpty(variableName, value);
    }
}
