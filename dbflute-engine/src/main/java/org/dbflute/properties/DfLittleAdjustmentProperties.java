/*
 * Copyright 2014-2025 the original author or authors.
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
package org.dbflute.properties;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.dbflute.bhv.AbstractBehaviorReadable;
import org.dbflute.bhv.AbstractBehaviorWritable;
import org.dbflute.bhv.core.BehaviorCommandInvoker;
import org.dbflute.exception.DfColumnNotFoundException;
import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.exception.DfIllegalPropertyTypeException;
import org.dbflute.exception.DfTableColumnNameNonCompilableConnectorException;
import org.dbflute.exception.DfTableNotFoundException;
import org.dbflute.helper.StringKeyMap;
import org.dbflute.helper.StringSet;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.jdbc.ClassificationUndefinedHandlingType;
import org.dbflute.logic.generate.language.DfLanguageDependency;
import org.dbflute.logic.generate.language.framework.DfLanguageFramework;
import org.dbflute.logic.generate.language.implstyle.DfLanguageImplStyle;
import org.dbflute.optional.OptionalEntity;
import org.dbflute.properties.assistant.base.DfTableDeterminer;
import org.dbflute.properties.assistant.littleadjust.DfDeprecatedSelectByPKUQMap;
import org.dbflute.properties.assistant.littleadjust.DfDeprecatedSpecifyBatchColumnMap;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;

/**
 * @author jflute
 */
public final class DfLittleAdjustmentProperties extends DfAbstractDBFluteProperties {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String MYSQL_DYNAMIC_ROW_MAGIC_FETCH_SIZE_EXP = "Integer.MIN_VALUE";

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfLittleAdjustmentProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                               Little Adjustment Map
    //                                                               =====================
    public static final String KEY_littleAdjustmentMap = "littleAdjustmentMap";
    protected Map<String, Object> _littleAdjustmentMap;

    public Map<String, Object> getLittleAdjustmentMap() {
        if (_littleAdjustmentMap == null) {
            final Map<String, Object> map = mapProp("torque." + KEY_littleAdjustmentMap, DEFAULT_EMPTY_MAP);
            _littleAdjustmentMap = newLinkedHashMap();
            _littleAdjustmentMap.putAll(map);
        }
        return _littleAdjustmentMap;
    }

    public String getProperty(String key, String defaultValue) {
        return _propertyValueHandler.getPropertyIfNotBuildProp(key, defaultValue, getLittleAdjustmentMap());
    }

    public boolean isProperty(String key, boolean defaultValue) {
        return _propertyValueHandler.isPropertyIfNotExistsFromBuildProp(key, defaultValue, getLittleAdjustmentMap());
    }

    // -----------------------------------------------------
    //                                      Check Definition
    //                                      ----------------
    public void checkDefinition(DfTableDeterminer determiner) {
        checkColumnNullObjectTableColumn(determiner);
        checkRelationalNullObjectTableColumn(determiner);
    }

    // ===================================================================================
    //                                                                       Schema Driven
    //                                                                       =============
    public boolean isAvailableAddingSchemaToTableSqlName() {
        if (isAvailableSchemaDrivenTable()) {
            return true; // forcedly true because schema-driven needs adding schema
        }
        return isProperty("isAvailableAddingSchemaToTableSqlName", false);
    }

    public boolean isAvailableAddingCatalogToTableSqlName() {
        return isProperty("isAvailableAddingCatalogToTableSqlName", false);
    }

    public boolean isSuppressOtherSchemaSameNameTableLimiter() { // closet
        return isProperty("isSuppressOtherSchemaSameNameTableLimiter", false);
    }

    public boolean isAvailableSchemaDrivenTable() { // closet
        return isProperty("isAvailableSchemaDrivenTable", false);
    }

    // ===================================================================================
    //                                                                 Database Dependency
    //                                                                 ===================
    public boolean isAvailableDatabaseDependency() {
        return isProperty("isAvailableDatabaseDependency", false);
    }

    public boolean isUseFullTextSearchPGroongaBasic() { // closet, since 1.2.7
        return isProperty("isUseFullTextSearchPGroongaBasic", false);
    }

    // ===================================================================================
    //                                                                         Native JDBC
    //                                                                         ===========
    public boolean isAvailableDatabaseNativeJDBC() {
        // for example, using oracle.sql.DATE on Oracle gives us best performances
        return isProperty("isAvailableDatabaseNativeJDBC", false);
    }

    // ===================================================================================
    //                                                                            Behavior
    //                                                                            ========
    public boolean isAvailableNonPrimaryKeyWritable() { // closet
        return isProperty("isAvailableNonPrimaryKeyWritable", false);
    }

    public boolean isAvailableSelectEntityPlainReturn() { // closet
        return isProperty("isAvailableSelectEntityPlainReturn", isCompatibleBeforeJava8());
    }

    public boolean isAvailableLambdaSelectEntityForcedlyOptional() { // closet
        return isProperty("isAvailableLambdaSelectEntityForcedlyOptional", true);
    }

    public boolean isAvailableSelectEntityPlainReturnTable(String tableName) { // closet
        if (!isAvailableSelectEntityPlainReturn()) {
            return false;
        }
        final Map<String, Object> littleAdjustmentMap = getLittleAdjustmentMap();
        final String key = "availableSelectEntityPlainReturnTableList";
        @SuppressWarnings("unchecked")
        final List<String> tableList = (List<String>) littleAdjustmentMap.get(key);
        if (tableList == null || tableList.isEmpty()) { // no name specification
            return true; // all table make
        }
        return isTargetByHint(tableName, tableList, DfCollectionUtil.emptyList());
    }

    public boolean isAvailableSelectEntityWithDeletedCheck() { // closet
        // selectEntityWithDeletedCheck() can coexist with optional entity
        //final boolean defaultValue = isCompatibleUnderJava8();
        return isProperty("isAvailableSelectEntityWithDeletedCheck", true);
    }

    public boolean isMakeCallbackConditionBeanSetup() { // closet (main way since 1.1)
        return isProperty("isMakeCallbackConditionBeanSetup", !isCompatibleBeforeJava8());
    }

    protected boolean isMakeDirectConditionBeanSetup() { // closet
        return isProperty("isMakeDirectConditionBeanSetup", isCompatibleBeforeJava8());
    }

    public boolean isMakeDirectConditionBeanSetupTable(String tableName) { // closet
        if (!isMakeDirectConditionBeanSetup()) {
            return false;
        }
        final Map<String, Object> littleAdjustmentMap = getLittleAdjustmentMap();
        final String key = "makeDirectConditionBeanSetupTableList";
        @SuppressWarnings("unchecked")
        final List<String> tableList = (List<String>) littleAdjustmentMap.get(key);
        if (tableList == null || tableList.isEmpty()) { // no name specification
            return true; // all table make
        }
        return isTargetByHint(tableName, tableList, DfCollectionUtil.emptyList());
    }

    public boolean isMakeBatchUpdateSpecifyColumn() { // closet
        return isProperty("isMakeBatchUpdateSpecifyColumn", isCompatibleBeforeJava8());
    }

    // -----------------------------------------------------
    //                                     Abstract Extended
    //                                     -----------------
    public boolean hasExtendedAbstractBehaviorReadableClass() {
        return getExtendedAbstractBehaviorReadableClass() != null;
    }

    public String getExtendedAbstractBehaviorReadableClass() { // closet
        final String readableName = AbstractBehaviorReadable.class.getSimpleName();
        return hasExtensionClass(readableName) ? findExtendedExtensionClassProperty(readableName) : null;
    }

    public boolean hasExtendedAbstractBehaviorWritableClass() {
        return getExtendedAbstractBehaviorWritableClass() != null;
    }

    public String getExtendedAbstractBehaviorWritableClass() { // closet
        final String writableName = AbstractBehaviorWritable.class.getSimpleName();
        return hasExtensionClass(writableName) ? findExtendedExtensionClassProperty(writableName) : null;
    }

    // -----------------------------------------------------
    //                                   Pinpoint Deprecated
    //                                   -------------------
    protected DfDeprecatedSelectByPKUQMap _deprecatedSelectByPKUQMap;

    public DfDeprecatedSelectByPKUQMap getDeprecatedSelectByPKUQMap() {
        if (_deprecatedSelectByPKUQMap != null) {
            return _deprecatedSelectByPKUQMap;
        }
        _deprecatedSelectByPKUQMap = createDeprecatedSelectByPKUQMap();
        return _deprecatedSelectByPKUQMap;
    }

    protected DfDeprecatedSelectByPKUQMap createDeprecatedSelectByPKUQMap() {
        return new DfDeprecatedSelectByPKUQMap(getLittleAdjustmentMap(), _propertyValueHandler);
    }

    // ===================================================================================
    //                                                                              Entity
    //                                                                              ======
    public boolean isEntityConvertEmptyStringToNull() {
        return isProperty("isEntityConvertEmptyStringToNull", false);
    }

    public boolean isEntityDerivedMappable() { // closet
        boolean defaultValue = getLanguageDependency().getLanguageImplStyle().isEntityDerivedMappable();
        if (!defaultValue) {
            defaultValue = !isCompatibleBeforeJava8();
        }
        return isProperty("isEntityDerivedMappable", defaultValue);
    }

    // -----------------------------------------------------
    //                                Java8Time and JodaTime
    //                                ----------------------
    // Java8-Time support:
    // DfTypeMappingProperties.java, Column.java
    // DBFluteConfig.vm, AbstractBsConditionQuery.vm, BsParameterBean.vm and so on...
    public boolean isAvailableJava8TimeEntity() { // closet
        return isAvailableJava8TimeLocalDateEntity() || isAvailableJava8TimeZonedDateEntity();
    }

    public boolean isAvailableJava8TimeLocalDateEntity() { // closet
        if (isAvailableJava8TimeZonedDateEntity()) {
            return false;
        }
        final DfLanguageImplStyle implStyle = getLanguageDependency().getLanguageImplStyle();
        if (implStyle.canUseJava8TimeLocalDate()) {
            final boolean defaultValue = !isCompatibleBeforeJava8(); // as default since 1.1
            return isProperty("isAvailableJava8TimeLocalDateEntity", defaultValue);
        }
        return false;
    }

    // protected for now because of not supported yet
    protected boolean isAvailableJava8TimeZonedDateEntity() { // closet
        final String key = "isAvailableJava8TimeZonedDateEntity";
        final boolean property = isProperty(key, false);
        if (property) {
            throw new IllegalStateException("Unsupported for now: " + key);
        }
        return property;
    }

    // basically use Java8-Time instead of JodaTime since 1.1
    //  => *unsupported since 1.1
    //public boolean isAvailableJodaTimeEntity() {
    //    return isAvailableJodaTimeLocalDateEntity() || isAvailableJodaTimeZonedDateEntity();
    //}
    //
    //public boolean isAvailableJodaTimeLocalDateEntity() { // closet
    //    return isProperty("isAvailableJodaTimeLocalDateEntity", false);
    //}
    //
    //public boolean isAvailableJodaTimeZonedDateEntity() { // closet
    //    final String key = "isAvailableJodaTimeZonedDateEntity";
    //    final boolean property = isProperty(key, false);
    //    if (property) {
    //        throw new IllegalStateException("Unsupported for now: " + key);
    //    }
    //    return property;
    //}

    public boolean needsDateTreatedAsLocalDateTime() {
        // basically for Oracle, actually supported Oracle only for now (2014/11/04)
        if (!isAvailableJava8TimeLocalDateEntity()) {
            return false;
        }
        final DfBasicProperties basicProp = getBasicProperties();
        if (basicProp.isDatabaseOracle()) {
            return !isOracleDateTreatedAsDate();
        }
        return false;
    }

    protected boolean isOracleDateTreatedAsDate() { // closet
        return isProperty("isOracleDateTreatedAsDate", false);
    }

    // -----------------------------------------------------
    //                                        Basic Optional
    //                                        --------------
    public String getBasicOptionalEntityClass() { // closet
        final DfLanguageImplStyle implStyle = getLanguageDependency().getLanguageImplStyle();
        final String langClass = implStyle.getBasicOptionalEntityClass();
        final String embedded = getOptionalEntityDBFluteEmbeddedClassName();
        return getProperty("basicOptionalEntityClass", langClass != null ? langClass : embedded);
    }

    public String getBasicOptionalEntitySimpleName() { // closet
        final String className = getBasicOptionalEntityClass();
        return className != null ? Srl.substringLastRear(className, ".") : null;
    }

    public boolean isBasicOptionalEntityDBFluteEmbeddedClass() {
        final String className = getBasicOptionalEntityClass();
        return className != null && className.equals(getOptionalEntityDBFluteEmbeddedClassName());
    }

    public boolean needsBasicOptionalEntityImport() {
        if (isBasicOptionalEntityScalaOption()) {
            return false;
        }
        return true;
    }

    protected boolean isBasicOptionalEntityScalaOption() {
        final String className = getBasicOptionalEntitySimpleName();
        return className != null && className.equals("Option");
    }

    // -----------------------------------------------------
    //                                     Relation Optional
    //                                     -----------------
    public boolean isAvailableRelationPlainEntity() {
        return isProperty("isAvailableRelationPlainEntity", isCompatibleBeforeJava8());
    }

    public String getRelationOptionalEntityClass() { // closet
        // you should also override RelationOptionalFactory if you change this
        final DfLanguageImplStyle implStyle = getLanguageDependency().getLanguageImplStyle();
        final String langClass = implStyle.getRelationOptionalEntityClass();
        final String embedded = getOptionalEntityDBFluteEmbeddedClassName();
        return getProperty("relationOptionalEntityClass", langClass != null ? langClass : embedded);
    }

    public String getRelationOptionalEntitySimpleName() {
        final String className = getRelationOptionalEntityClass();
        return className != null ? Srl.substringLastRear(className, ".") : null;
    }

    public boolean isRelationOptionalEntityDBFluteEmbeddedClass() {
        final String className = getRelationOptionalEntityClass();
        return className != null && className.equals(getOptionalEntityDBFluteEmbeddedClassName());
    }

    public boolean needsRelationOptionalEntityImport() {
        if (isRelationOptionalEntityScalaOption()) {
            return false;
        }
        return true;
    }

    public boolean needsRelationOptionalEntityNextImport() {
        if (isRelationOptionalEntityScalaOption()) {
            return false;
        }
        final String relationOptionalEntityClassName = getRelationOptionalEntityClass();
        if (relationOptionalEntityClassName.equals(getBasicOptionalEntityClass())) {
            return false;
        }
        return true;
    }

    protected boolean isRelationOptionalEntityScalaOption() {
        final String className = getRelationOptionalEntitySimpleName();
        return className != null && className.equals("Option");
    }

    // -----------------------------------------------------
    //                             DBFlute Embedded Optional
    //                             -------------------------
    protected String getOptionalEntityDBFluteEmbeddedClassName() {
        return getOptionalEntityDBFluteEmbeddedType().getName();
    }

    protected Class<?> getOptionalEntityDBFluteEmbeddedType() {
        return OptionalEntity.class;
    }

    // -----------------------------------------------------
    //                              Entity Mutable/Immutable
    //                              ------------------------
    public boolean isMakeImmutableEntity() { // closet, basically for Scala
        DfLanguageDependency lang = getLanguageDependency();
        return isProperty("isMakeImmutableEntity", lang.getLanguageImplStyle().isMakeImmutableEntity());
    }

    public String getEntityDBablePrefix() { // closet, basically for Scala
        DfLanguageDependency lang = getLanguageDependency();
        return getProperty("entityDBablePrefix", lang.getLanguageImplStyle().getEntityDBablePrefix());
    }

    public String getEntityMutablePrefix() { // closet, basically for Scala
        DfLanguageDependency lang = getLanguageDependency();
        return getProperty("entityMutablePrefix", lang.getLanguageImplStyle().getEntityMutablePrefix());
    }

    // -----------------------------------------------------
    //                                            Unique Key
    //                                            ----------
    public Integer getKeyableUniqueColumnLimit() { // closet
        final String defaultValue = "9"; // too many columns may be inconvenient as key
        return Integer.valueOf(getProperty("keyableUniqueColumnLimit", defaultValue)); // if minus, no limit
    }

    // -----------------------------------------------------
    //                                               JavaDoc
    //                                               -------
    public boolean isGenerateTableDetailJavaDoc() { // closet, since 1.2.7
        // there are "get/set template" users so add option here (2023/03/17)
        // thanks pair programing, yukiyoko san! (good naming for this property)
        return isProperty("isGenerateTableDetailJavaDoc", false);
    }

    // ===================================================================================
    //                                                                       ConditionBean
    //                                                                       =============
    // it's phantom
    // cb.loadPurchaseList(purchaseCB -> {
    //     purchaseCB.setupSelect_Product();
    //     purchaseCB.query()...
    //     purchaseCB.loadPurchaseDetailList(detailCB -> {
    //         detailCB.setupSelect_Foo();
    //         detailCB.setupSelect_BarAsOne();
    //         detailCB.query()...
    //         detailCB.pulloutBarAsOne().loadPurchase()...
    //     });
    // });
    // DBFlute gives importance to easy-to-trace DB access by jflute (2014/05/26)
    //public boolean isMakeConditionBeanCBDrivenLoadReferrer() {
    //    return isProperty("isMakeConditionBeanCBDrivenLoadReferrer", false);
    //}
    public boolean isSpecifyColumnRequired() { // @since 1.1.2
        return isProperty("isSpecifyColumnRequired", false);
    }

    public boolean isNullOrEmptyQueryAllowed() { // closet
        // invalid-query means abstract concept, so rename it
        final boolean defaultValue = isProperty("isInvalidQueryChecked", !isCompatibleBeforeJava8());
        return isProperty("isNullOrEmptyQueryAllowed", !defaultValue);
    }

    public boolean isEmptyStringQueryAllowed() { // closet
        return isProperty("isEmptyStringQueryAllowed", false);
    }

    public boolean isEmptyStringParameterAllowed() { // closet
        return isProperty("isEmptyStringParameterAllowed", false);
    }

    public boolean isOverridingQueryAllowed() { // closet
        return isProperty("isOverridingQueryAllowed", isCompatibleBeforeJava8());
    }

    public boolean isInvalidQueryAllowedWarning() { // closet
        return isProperty("isInvalidQueryAllowedWarning", false);
    }

    public boolean isNonSpecifiedColumnAccessAllowed() { // closet
        return isProperty("isNonSpecifiedColumnAccessAllowed", isCompatibleBeforeJava8());
    }

    public boolean isNonSpecifiedColumnAccessWarningOnly() { // closet, since 1.2.7
        return isProperty("isNonSpecifiedColumnAccessWarningOnly", false);
    }

    public boolean isDatetimePrecisionTruncationOfCondition() { // closet
        return isProperty("isDatetimePrecisionTruncationOfCondition", false);
    }

    public boolean isDatetimePrecisionTruncationOfUpdatedEntity() { // closet
        return isProperty("isDatetimePrecisionTruncationOfUpdatedEntity", false);
    }

    public boolean isMakeConditionQueryEqualEmptyString() { // closet
        return isProperty("isMakeConditionQueryEqualEmptyString", false);
    }

    public boolean isMakeConditionQueryNotEqualAsStandard() { // closet
        // DBFlute had used tradition for a long time
        // but default value is true (uses standard) since 0.9.7.2
        return isProperty("isMakeConditionQueryNotEqualAsStandard", true);
    }

    public String getConditionQueryNotEqualDefinitionName() {
        // for AbstractConditionQuery's definition name
        return isMakeConditionQueryNotEqualAsStandard() ? "CK_NES" : "CK_NET";
    }

    public boolean isMakeConditionQueryExistsReferrerToOne() { // closet
        return isProperty("isMakeConditionQueryExistsReferrerToOne", isCompatibleBeforeJava8());
    }

    public boolean isMakeConditionQueryInScopeRelationToOne() { // closet
        return isProperty("isMakeConditionQueryInScopeRelationToOne", isCompatibleBeforeJava8());
    }

    public boolean isMakeConditionQueryPlainListManualOrder() { // closet
        return isProperty("isMakeConditionQueryPlainListManualOrder", isCompatibleBeforeJava8());
    }

    public boolean isMakeConditionQueryPrefixSearch() { // closet
        return isProperty("isMakeConditionQueryPrefixSearch", isCompatibleBeforeJava8());
    }

    public boolean isMakeConditionQueryDateFromTo() { // closet
        return isProperty("isMakeConditionQueryDateFromTo", isCompatibleBeforeJava8());
    }

    public boolean isMakeCallbackConditionOptionSetup() { // closet
        return isProperty("isMakeCallbackConditionOptionSetup", !isCompatibleBeforeJava8());
    }

    public boolean isMakeDirectConditionOptionSetup() { // closet
        return isProperty("isMakeDirectConditionOptionSetup", isCompatibleBeforeJava8());
    }

    public boolean isMakeCallbackConditionManualOrder() { // closet
        return isProperty("isMakeCallbackConditionManualOrder", !isCompatibleBeforeJava8());
    }

    public boolean isMakeDirectConditionManualOrder() { // closet
        return isProperty("isMakeDirectConditionManualOrder", isCompatibleBeforeJava8());
    }

    // -----------------------------------------------------
    //                                   Pinpoint Deprecated
    //                                   -------------------
    protected DfDeprecatedSpecifyBatchColumnMap _deprecatedSpecifyBatchColumnMap;

    public DfDeprecatedSpecifyBatchColumnMap getDeprecatedSpecifyBatchColumnMap() {
        if (_deprecatedSpecifyBatchColumnMap != null) {
            return _deprecatedSpecifyBatchColumnMap;
        }
        _deprecatedSpecifyBatchColumnMap = createDeprecatedSpecifyBatchColumnMap();
        return _deprecatedSpecifyBatchColumnMap;
    }

    protected DfDeprecatedSpecifyBatchColumnMap createDeprecatedSpecifyBatchColumnMap() {
        return new DfDeprecatedSpecifyBatchColumnMap(getLittleAdjustmentMap(), _propertyValueHandler);
    }

    // ===================================================================================
    //                                                                      Classification
    //                                                                      ==============
    // -----------------------------------------------------
    //                                    Undefined Handling
    //                                    ------------------
    // user public since 1.1 (implemented when 1.0.5K)
    public boolean hasClassificationUndefinedHandlingTypeProperty() {
        return doGetClassificationUndefinedHandlingType(null) != null;
    }

    public ClassificationUndefinedHandlingType getClassificationUndefinedHandlingType() {
        final String defaultValue;
        if (isCompatibleBeforeJava8()) {
            defaultValue = ClassificationUndefinedHandlingType.EXCEPTION.code();
        } else {
            if (hasCheckSelectedClassificationProperty() && isCheckSelectedClassification()) {
                defaultValue = ClassificationUndefinedHandlingType.EXCEPTION.code();
            } else { // default after Java8
                defaultValue = ClassificationUndefinedHandlingType.LOGGING.code();
            }
        }
        final String code = doGetClassificationUndefinedHandlingType(defaultValue);
        final ClassificationUndefinedHandlingType handlingType =
                ClassificationUndefinedHandlingType.of(code).orElseTranslatingThrow(cause -> {
                    throwUnknownClassificationUndefinedHandlingTypeException(code, KEY_littleAdjustmentMap + ".dfprop", cause);
                    return null; // unreachable
                });
        return handlingType;
    }

    protected String doGetClassificationUndefinedHandlingType(String defaultValue) {
        return getProperty("classificationUndefinedHandlingType", defaultValue);
    }

    protected void throwUnknownClassificationUndefinedHandlingTypeException(String code, String dfpropFile, Throwable cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Unknown handling type of classification undefined code.");
        br.addItem("Advice");
        br.addElement("You can specify following types:");
        for (ClassificationUndefinedHandlingType handlingType : ClassificationUndefinedHandlingType.values()) {
            br.addElement(" " + handlingType.code());
        }
        final String exampleCode = ClassificationUndefinedHandlingType.EXCEPTION.code();
        br.addElement("");
        br.addElement("For example: (littleAdjustmentMap.dfprop)");
        br.addElement("map:{");
        br.addElement("    ...");
        br.addElement("    ");
        br.addElement("    ; classificationUndefinedCodeHandlingType = " + exampleCode);
        br.addElement("    ...");
        br.addElement("}");
        br.addItem("Specified Unknown Type");
        br.addElement(code);
        br.addItem("dfprop File");
        br.addElement(dfpropFile);
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg, cause);
    }

    public boolean isPlainCheckClassificationCode() { // for e.g. classificationResource
        final DfLittleAdjustmentProperties prop = getLittleAdjustmentProperties();
        final ClassificationUndefinedHandlingType undefinedHandlingType = prop.getClassificationUndefinedHandlingType();
        if (prop.hasClassificationUndefinedHandlingTypeProperty() && undefinedHandlingType.isChecked()) {
            return true; // explicitly check setting in dfprop
        }
        if (prop.isSuppressDefaultCheckClassificationCode()) {
            return false; // compatible option
        }
        return undefinedHandlingType.isChecked(); // as default of dfprop (logging after java8)
    }

    public boolean isSuppressDefaultCheckClassificationCode() { // closet
        // for new classification code check (existing code or not)
        // the check is on entity if undefinedHandlingType is LOGGING or EXCEPTION
        // but you can suppress the check if implicit setting (means default) in dfprop
        return isProperty("isSuppressDefaultCheckClassificationCode", isCompatibleBeforeJava8());
    }

    // old style, checking on dbmeta
    protected static final String KEY_isCheckSelectedClassification = "isCheckSelectedClassification";

    public boolean hasCheckSelectedClassificationProperty() {
        return getProperty(KEY_isCheckSelectedClassification, null) != null;
    }

    public boolean isCheckSelectedClassification() { // closet
        return isProperty(KEY_isCheckSelectedClassification, false);
    }

    // -----------------------------------------------------
    //                                  Force Classification
    //                                  --------------------
    // user public since 1.1 (implemented when 1.0.5K)
    protected static final String PROP_isMakeClassificationNativeTypeSetter = "isMakeClassificationNativeTypeSetter";

    public boolean hasMakeClassificationNativeTypeSetterProperty() {
        return getProperty(PROP_isMakeClassificationNativeTypeSetter, null) != null;
    }

    public boolean isMakeClassificationNativeTypeSetter() { // closet
        // not make setter as default (but closet classification can be selected with logging) since 1.1
        // http://d.hatena.ne.jp/jflute/20140823/forcecls
        return isProperty(PROP_isMakeClassificationNativeTypeSetter, false);
    }

    // old style, but primitive control
    public boolean isForceClassificationSetting() {
        if (hasMakeClassificationNativeTypeSetterProperty()) {
            return !isMakeClassificationNativeTypeSetter();
        }
        return isProperty("isForceClassificationSetting", !isCompatibleBeforeJava8());
    }

    // -----------------------------------------------------
    //                                           CDef Option
    //                                           -----------
    public boolean isMakeCDefOldStyleCodeOfMethod() { // closet
        // default true until removing old style dependencies
        return isProperty("isMakeCDefOldStyleCodeOfMethod", true);
    }

    public boolean isMakeCDefOldStyleNameOfMethod() { // closet
        // default true until deprecated version
        return isProperty("isMakeCDefOldStyleNameOfMethod", true);
    }

    public boolean isMakeCDefOldStyleListOfMethod() { // closet
        // default true until deprecated version
        return isProperty("isMakeCDefOldStyleListOfMethod", true);
    }

    public boolean isMakeCDefOldStyleGroupOfMethod() { // closet
        // default true until deprecated version
        return isProperty("isMakeCDefOldStyleGroupOfMethod", true);
    }

    // already unused
    //public boolean isCDefToStringReturnsName() { // closet
    //    return isProperty("isCDefToStringReturnsName", false);
    //}

    // unsupported since 1.1
    //public boolean isMakeEntityOldStyleClassify() { // closet
    //    return isProperty("isMakeEntityOldStyleClassify", false);
    //}

    public boolean isSuppressTableClassificationDBAccessClass() { // closet
        return isProperty("isSuppressTableClassificationDBAccessClass", false);
    }

    public boolean isSuppressTableClassificationNameCamelizing() { // closet
        return isProperty("isSuppressTableClassificationNameCamelizing", false);
    }

    // ===================================================================================
    //                                                                       Paging Select
    //                                                                       =============
    public boolean isPagingCountLater() { // closet since 1.1
        return isProperty("isPagingCountLater", true); // default true @since 0.9.9.0A
    }

    public boolean isPagingCountLeastJoin() { // closet since 1.1
        return isProperty("isPagingCountLeastJoin", true); // default true @since 0.9.9.0A
    }

    public boolean isInnerJoinAutoDetect() { // closet since 1.1
        return isProperty("isInnerJoinAutoDetect", true); // default true @since 1.0.3
    }

    // ===================================================================================
    //                                                                          CB Purpose
    //                                                                          ==========
    public boolean isOrScopeQueryPurposeCheckWarningOnly() { // closet since 1.2.7
        return isProperty("isOrScopeQueryPurposeCheckWarningOnly", false);
    }

    public boolean isThatsBadTimingDetect() { // closet
        final boolean defaultValue = !isCompatibleBeforeJava8();
        return isProperty("isThatsBadTimingDetect", defaultValue);
    }

    public boolean isThatsBadTimingWarningOnly() { // closet since 1.2.7
        return isProperty("isThatsBadTimingWarningOnly", false); // precondition: detect=true
    }

    // ===================================================================================
    //                                                              Display Name UpperCase
    //                                                              ======================
    public boolean isTableDispNameUpperCase() {
        return isProperty("isTableDispNameUpperCase", false);
    }

    public String filterTableDispNameIfNeeds(String tableDbName) {
        if (isTableDispNameUpperCase()) {
            if (!tableDbName.endsWith("\"") && tableDbName.contains(".")) { // e.g. maihamadb.MEMBER
                final String schema = Srl.substringLastFront(tableDbName, ".");
                final String lowerName = Srl.substringLastRear(tableDbName, ".").toLowerCase();
                return schema + "." + lowerName;
            } else { // e.g. MEMBER or "MEM.BAR"
                return tableDbName.toUpperCase();
            }
        } else {
            return tableDbName;
        }
    }

    // ===================================================================================
    //                                                                  SQL Name UpperCase
    //                                                                  ==================
    public boolean isTableSqlNameUpperCase() {
        return isProperty("isTableSqlNameUpperCase", false);
    }

    public boolean isColumnSqlNameUpperCase() {
        return isProperty("isColumnSqlNameUpperCase", false);
    }

    // ===================================================================================
    //                                                                     Make Deprecated
    //                                                                     ===============
    public boolean isMakeDeprecated() { // closet
        return isProperty("isMakeDeprecated", false);
    }

    public boolean isMakeRecentlyDeprecated() { // closet
        return isProperty("isMakeRecentlyDeprecated", true);
    }

    // ===================================================================================
    //                                                                  Extended Component
    //                                                                  ==================
    public String getDBFluteInitializerClass() { // Java only
        return prepareExtensionClassAllcommon("DBFluteInitializer");
    }

    public String getImplementedInvokerAssistantClass() { // Java only, closet
        return prepareExtensionClassAllcommon("ImplementedInvokerAssistant");
    }

    public String getImplementedCommonColumnAutoSetupperClass() { // Java only, closet
        return prepareExtensionClassAllcommon("ImplementedCommonColumnAutoSetupper");
    }

    public String getBehaviorCommandInvokerClass() { // Java only
        return prepareExtensionClassRuntime(BehaviorCommandInvoker.class.getName());
    }

    public String getBehaviorCommandInvokerSimpleIfPlainClass() { // Java only
        return prepareExtensionClassRuntime(BehaviorCommandInvoker.class.getSimpleName());
    }

    public String getS2DaoSettingClass() { // CSharp only, closet
        final String className = "S2DaoSetting";
        if (hasExtensionClass(className)) {
            return findExtendedExtensionClassProperty(className);
        } else {
            return getBasicProperties().getProjectPrefix() + className;
        }
    }

    protected String prepareExtensionClassAllcommon(String className) {
        return doPrepareExtensionClass(className, false);
    }

    protected String prepareExtensionClassRuntime(String className) {
        return doPrepareExtensionClass(className, true);
    }

    protected String doPrepareExtensionClass(String className, boolean runtime) { // not null
        final String plainName = Srl.substringLastRear(className, ".");
        if (hasExtensionClass(plainName)) {
            return findExtendedExtensionClassProperty(plainName);
        } else {
            if (runtime) { // e.g. BehaviorCommandInvoker
                return className;
            } else { // allcommon
                final DfBasicProperties prop = getBasicProperties();
                final String commonPackage = prop.getBaseCommonPackage();
                final String projectPrefix = prop.getProjectPrefix();
                final String allcommonPrefix = prop.getAllcommonPrefix();
                return commonPackage + "." + projectPrefix + allcommonPrefix + className;
            }
        }
    }

    protected boolean hasExtensionClass(String className) {
        String str = findExtendedExtensionClassProperty(className);
        return str != null && str.trim().length() > 0 && !str.trim().equals("null");
    }

    protected String findExtendedExtensionClassProperty(String className) { // null allowed
        return getProperty("extended" + className + "Class", null);
    }

    // ===================================================================================
    //                                                                          Short Char
    //                                                                          ==========
    public boolean isShortCharHandlingValid() {
        return !getShortCharHandlingMode().equalsIgnoreCase("NONE");
    }

    public String getShortCharHandlingMode() {
        String property = getProperty("shortCharHandlingMode", "NONE");
        return property.toUpperCase();
    }

    public String getShortCharHandlingModeCode() {
        return getShortCharHandlingMode().substring(0, 1);
    }

    // ===================================================================================
    //                                                                          Quote Name
    //                                                                          ==========
    // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
    // #for_now jflute too difficult to quote catalog and schema names so unsupported for now (2023/10/30)
    // _/_/_/_/_/_/_/_/_/_/
    // -----------------------------------------------------
    //                                                 Table
    //                                                 -----
    protected Set<String> _quoteTableNameSet;
    protected Boolean _quoteTableNameAll;

    protected Set<String> getQuoteTableNameSet() {
        if (_quoteTableNameSet != null) {
            return _quoteTableNameSet;
        }
        final Map<String, Object> littleAdjustmentMap = getLittleAdjustmentMap();
        final Object obj = littleAdjustmentMap.get("quoteTableNameList");
        if (obj != null) {
            final List<String> list = castToList(obj, "littleAdjustmentMap.quoteTableNameList");
            _quoteTableNameSet = StringSet.createAsFlexible();
            _quoteTableNameSet.addAll(list);
        } else {
            _quoteTableNameSet = new HashSet<String>();
        }
        _quoteTableNameAll = _quoteTableNameSet.contains("$$ALL$$");
        return _quoteTableNameSet;
    }

    public boolean isQuoteTable(String tableName) {
        final Set<String> quoteTableNameSet = getQuoteTableNameSet(); // also initialize
        if (_quoteTableNameAll != null && _quoteTableNameAll) { // after initialization
            return true;
        }
        return quoteTableNameSet.contains(tableName);
    }

    public String quoteTableNameIfNeeds(String tableName) {
        return doQuoteTableNameIfNeeds(tableName, false);
    }

    public String quoteTableNameIfNeedsDirectUse(String tableName) {
        return doQuoteTableNameIfNeeds(tableName, true);
    }

    protected String doQuoteTableNameIfNeeds(String tableName, boolean directUse) {
        if (tableName == null) {
            return null;
        }
        if (!isQuoteTable(tableName) && !containsNonCompilableConnector(tableName)) {
            return tableName;
        }
        return doQuoteName(tableName, directUse);
    }

    // -----------------------------------------------------
    //                                                Column
    //                                                ------
    protected Set<String> _quoteColumnNameSet;
    protected Boolean _quoteColumnNameAll;

    protected Set<String> getQuoteColumnNameSet() {
        if (_quoteColumnNameSet != null) {
            return _quoteColumnNameSet;
        }
        final Map<String, Object> littleAdjustmentMap = getLittleAdjustmentMap();
        final Object obj = littleAdjustmentMap.get("quoteColumnNameList");
        if (obj != null) {
            final List<String> list = castToList(obj, "littleAdjustmentMap.quoteColumnNameList");
            _quoteColumnNameSet = StringSet.createAsFlexible();
            _quoteColumnNameSet.addAll(list);
        } else {
            _quoteColumnNameSet = new HashSet<String>();
        }
        _quoteColumnNameAll = _quoteColumnNameSet.contains("$$ALL$$");
        return _quoteColumnNameSet;
    }

    public boolean isQuoteColumn(String columnName) {
        final Set<String> quoteColumnNameSet = getQuoteColumnNameSet(); // also initialize
        if (_quoteColumnNameAll != null && _quoteColumnNameAll) { // after initialization
            return true;
        }
        return quoteColumnNameSet.contains(columnName);
    }

    public String quoteColumnNameIfNeeds(String columnName) {
        return doQuoteColumnNameIfNeeds(columnName, false);
    }

    public String quoteColumnNameIfNeedsDirectUse(String columnName) {
        return doQuoteColumnNameIfNeeds(columnName, true);
    }

    protected String doQuoteColumnNameIfNeeds(String columnName, boolean directUse) {
        if (columnName == null) {
            return null;
        }
        if (!isQuoteColumn(columnName) && !containsNonCompilableConnector(columnName)) {
            return columnName;
        }
        return doQuoteName(columnName, directUse);
    }

    // -----------------------------------------------------
    //                                                 Quote
    //                                                 -----
    protected String doQuoteName(String name, boolean directUse) {
        final String beginQuote;
        final String endQuote;
        if (getBasicProperties().isDatabaseMySQL()) {
            // it works in spite of ANSI_QUOTES
            beginQuote = "`";
            endQuote = beginQuote;
        } else if (getBasicProperties().isDatabaseSQLServer()) {
            beginQuote = "[";
            endQuote = "]";
        } else {
            beginQuote = directUse ? "\"" : "\\\"";
            endQuote = beginQuote;
        }
        return beginQuote + name + endQuote;
    }

    // ===================================================================================
    //                                                                   Column NullObject
    //                                                                   =================
    // you can get the handling of geared to specify by isGearedToSpecify
    protected Map<String, Object> _columnNullObjectMap;

    protected Map<String, Object> getColumnNullObjectMap() {
        if (_columnNullObjectMap != null) {
            return _columnNullObjectMap;
        }
        final Map<String, Object> littleAdjustmentMap = getLittleAdjustmentMap();
        final Object obj = littleAdjustmentMap.get("columnNullObjectMap");
        if (obj != null) {
            _columnNullObjectMap = castToMap(obj, "littleAdjustmentMap.columnNullObjectMap");
        } else {
            _columnNullObjectMap = newLinkedHashMap();
        }
        return _columnNullObjectMap;
    }

    public boolean isColumnNullObjectAllowed() {
        return !getColumnNullObjectColumnMap().isEmpty();
    }

    public boolean hasColumnNullObject(String tableName) {
        return getColumnNullObjectColumnMap().get(tableName) != null;
    }

    public boolean hasColumnNullObject(String tableName, String columnName) {
        final Map<String, String> providerMap = getColumnNullObjectColumnMap().get(tableName);
        return providerMap != null && providerMap.get(columnName) != null;
    }

    public String getColumnNullObjectProviderPackage() {
        final String pkg = (String) getColumnNullObjectMap().get("providerPackage");
        if (pkg == null) {
            return null;
        }
        final String packageBase = getBasicProperties().getPackageBase();
        return Srl.replace(pkg, "$$packageBase$$", packageBase);
    }

    public boolean isColumnNullObjectGearedToSpecify() {
        return isProperty("isGearedToSpecify", false, getColumnNullObjectMap());
    }

    public String buildColumnNullObjectProviderExp(String tableName, String columnName, String pkExp) {
        String exp = getColumnNullObjectProviderExp(tableName, columnName);
        exp = replace(exp, "$$columnName$$", columnName);
        exp = replace(exp, "$$primaryKey$$", pkExp);
        return exp;
    }

    protected String getColumnNullObjectProviderExp(String tableName, String columnName) {
        final Map<String, Map<String, String>> columnMap = getColumnNullObjectColumnMap();
        final Map<String, String> providerMap = columnMap.get(tableName);
        return providerMap != null ? providerMap.get(columnName) : null;
    }

    protected Map<String, Map<String, String>> _columnNullObjectColumnMap;

    public Map<String, Map<String, String>> getColumnNullObjectColumnMap() {
        if (_columnNullObjectColumnMap != null) {
            return _columnNullObjectColumnMap;
        }
        final Map<String, Object> nullObjectMap = getColumnNullObjectMap();
        final Object obj = nullObjectMap.get("columnMap");
        final Map<String, Map<String, String>> plainMap;
        if (obj != null) {
            plainMap = castToMap(obj, "littleAdjustmentMap.columnNullObjectMap.columnMap");
        } else {
            plainMap = newLinkedHashMap();
        }
        _columnNullObjectColumnMap = StringKeyMap.createAsFlexibleOrdered();
        for (Entry<String, Map<String, String>> entry : plainMap.entrySet()) {
            final String tableName = entry.getKey();
            final Map<String, String> providerMap = entry.getValue();
            final Map<String, String> flexibleMap = StringKeyMap.createAsFlexibleOrdered();
            flexibleMap.putAll(providerMap);
            _columnNullObjectColumnMap.put(tableName, flexibleMap);
        }
        return _columnNullObjectColumnMap;
    }

    protected void checkColumnNullObjectTableColumn(DfTableDeterminer determiner) {
        final Map<String, Map<String, String>> nullObjectColumnMap = getColumnNullObjectColumnMap();
        final String location = "littleAdjustmentMap.columnNullObjectMap.columnMap";
        for (Entry<String, Map<String, String>> entry : nullObjectColumnMap.entrySet()) {
            final String tableName = entry.getKey();
            if (!determiner.hasTable(tableName)) {
                String msg = "The table was not found in the " + location + ": " + tableName;
                throw new DfTableNotFoundException(msg);
            }
            final Map<String, String> providerMap = entry.getValue();
            for (String columnName : providerMap.keySet()) {
                if (!determiner.hasTableColumn(tableName, columnName)) {
                    String msg = "The column was not found in the " + location + ": " + tableName + "." + columnName;
                    throw new DfColumnNotFoundException(msg);
                }
            }
        }
    }

    // ===================================================================================
    //                                                               Relational NullObject
    //                                                               =====================
    // no geared to setupSelect because too complex
    // (e.g. needs to think nested relation tables)
    protected Map<String, Object> _relationalNullObjectMap;

    protected Map<String, Object> getRelationalNullObjectMap() {
        if (_relationalNullObjectMap != null) {
            return _relationalNullObjectMap;
        }
        final Map<String, Object> littleAdjustmentMap = getLittleAdjustmentMap();
        final Object obj = littleAdjustmentMap.get("relationalNullObjectMap");
        if (obj != null) {
            _relationalNullObjectMap = castToMap(obj, "littleAdjustmentMap.relationalNullObjectMap");
        } else {
            _relationalNullObjectMap = newLinkedHashMap();
        }
        return _relationalNullObjectMap;
    }

    // foreignMap is only supported now (2011/11/13)

    public boolean hasRelationalNullObjectForeign(String tableName) {
        return getRelationalNullObjectForeignMap().get(tableName) != null;
    }

    public String getRelationalNullObjectProviderPackage() {
        final String pkg = (String) getRelationalNullObjectMap().get("providerPackage");
        if (pkg == null) {
            return null;
        }
        final String packageBase = getBasicProperties().getPackageBase();
        return Srl.replace(pkg, "$$packageBase$$", packageBase);
    }

    public String getRelationalNullObjectOptionalEmptyExp() {
        final String defaultValue = "orElse(null) == null"; // fixedly java style for now
        return getProperty("optionalEmptyExp", defaultValue, getRelationalNullObjectMap());
    }

    public String buildRelationalNullObjectProviderForeignExp(String tableName, String foreignProperty, String beansRuleProperty,
            String pkExp) {
        String exp = getRelationalNullObjectForeignMap().get(tableName);
        exp = replace(exp, "$$foreignPropertyName$$", beansRuleProperty);
        exp = replace(exp, "$$foreignVariable$$", "_" + foreignProperty);
        exp = replace(exp, "$$PrimaryKey$$", pkExp); // for compatible (1.0.x)
        exp = replace(exp, "$$primaryKey$$", pkExp);
        return exp;
    }

    protected Map<String, String> _relationalNullObjectForeignMap;

    protected Map<String, String> getRelationalNullObjectForeignMap() {
        if (_relationalNullObjectForeignMap != null) {
            return _relationalNullObjectForeignMap;
        }
        final Map<String, Object> nullObjectMap = getRelationalNullObjectMap();
        final Object obj = nullObjectMap.get("foreignMap");
        final Map<String, String> plainMap;
        if (obj != null) {
            plainMap = castToMap(obj, "littleAdjustmentMap.relationalNullObjectMap.foreignMap");
        } else {
            plainMap = newLinkedHashMap();
        }
        _relationalNullObjectForeignMap = StringKeyMap.createAsFlexibleOrdered();
        _relationalNullObjectForeignMap.putAll(plainMap);
        return _relationalNullObjectForeignMap;
    }

    protected void checkRelationalNullObjectTableColumn(DfTableDeterminer determiner) {
        final String foreignLocation = "littleAdjustmentMap.relationalNullObjectMap.foreignMap";
        final Map<String, String> foreignMap = getRelationalNullObjectForeignMap();
        for (Entry<String, String> entry : foreignMap.entrySet()) {
            final String tableName = entry.getKey();
            if (!determiner.hasTable(tableName)) {
                String msg = "The table was not found in the " + foreignLocation + ": " + tableName;
                throw new DfTableNotFoundException(msg);
            }
        }
    }

    // ===================================================================================
    //                                                                     JDBC Fetch Size
    //                                                                     ===============
    // -----------------------------------------------------
    //                                    CursorSelect Fetch
    //                                    ------------------
    public boolean isCursorSelectFetchSizeValid() {
        return getCursorSelectFetchSize() != null;
    }

    public String getCursorSelectFetchSize() {
        return getProperty("cursorSelectFetchSize", getDefaultCursorSelectFetchSize());
    }

    protected String getDefaultCursorSelectFetchSize() {
        final String defaultValue;
        final DfBasicProperties prop = getBasicProperties();
        // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
        // also MySQL is all data fetching, but MIN_VALUE has the adverse effect
        // e.g. you cannot select in cursor callback when MIN_VALUE
        // _/_/_/_/_/_/_/_/_/_/
        if (prop.isDatabasePostgreSQL()) { // is all data fetching as default
            defaultValue = "100";
        } else {
            defaultValue = null;
        }
        return defaultValue;
    }

    public boolean isCursorSelectOptionAllowed() {
        // because this option is patch for MySQL's poor cursor select
        return getBasicProperties().isDatabaseMySQL() && isCursorSelectFetchSizeIntegerMinValue();
    }

    protected boolean isCursorSelectFetchSizeIntegerMinValue() {
        if (!isCursorSelectFetchSizeValid()) {
            return false;
        }
        return MYSQL_DYNAMIC_ROW_MAGIC_FETCH_SIZE_EXP.equals(getCursorSelectFetchSize());
    }

    // -----------------------------------------------------
    //                                    EntitySelect Fetch
    //                                    ------------------
    public boolean isEntitySelectFetchSizeValid() {
        return getEntitySelectFetchSize() != null;
    }

    public String getEntitySelectFetchSize() { // closet
        final String defaultValue = getDefaultEntitySelectFetchSize();
        return getProperty("entitySelectFetchSize", defaultValue);
    }

    protected String getDefaultEntitySelectFetchSize() {
        // several databases fetch all records when the result set is created
        // and the check of fetching the illegal second record does not work
        // so enable real fetching when entity select
        final String defaultValue;
        final DfBasicProperties prop = getBasicProperties();
        if (prop.isDatabaseMySQL()) {
            defaultValue = MYSQL_DYNAMIC_ROW_MAGIC_FETCH_SIZE_EXP;
        } else if (prop.isDatabasePostgreSQL()) {
            defaultValue = "1";
        } else {
            defaultValue = null;
        }
        return defaultValue;
    }

    // -----------------------------------------------------
    //                                    PagingSelect Fetch
    //                                    ------------------
    // cursor-skip only (because manual paging is no memory problem)
    public boolean isUsePagingByCursorSkipSynchronizedFetchSize() { // closet
        final boolean defaultValue = determineDefaultUsePagingByCursorSkipSynchronizedFetchSize();
        return isProperty("isUsePagingByCursorSkipSynchronizedFetchSize", defaultValue);
    }

    protected boolean determineDefaultUsePagingByCursorSkipSynchronizedFetchSize() {
        final DfBasicProperties prop = getBasicProperties();
        return prop.isDatabaseMySQL() || prop.isDatabasePostgreSQL(); // are all data fetching as default
    }

    public boolean isFixedPagingByCursorSkipSynchronizedFetchSizeValid() {
        return getFixedPagingByCursorSkipSynchronizedFetchSize() != null;
    }

    public String getFixedPagingByCursorSkipSynchronizedFetchSize() { // closet
        // this size is used when isUsePagingByCursorSkipSynchronizedFetchSize is true
        final String defaultValue = getDefaultFixedPagingByCursorSkipSynchronizedFetchSize();
        return getProperty("fixedPagingSynchronizedFetchSize", defaultValue);
    }

    protected String getDefaultFixedPagingByCursorSkipSynchronizedFetchSize() {
        final String defaultValue;
        final DfBasicProperties prop = getBasicProperties();
        if (prop.isDatabaseMySQL()) { // MySQL cannot set normal fetch size e.g. 1, 20 so magic size
            defaultValue = MYSQL_DYNAMIC_ROW_MAGIC_FETCH_SIZE_EXP;
        } else {
            defaultValue = null;
        }
        return defaultValue;
    }

    // ===================================================================================
    //                                                                      Refresh Facade
    //                                                                      ==============
    public Map<String, Object> getRefreshFacadeMap() { // null allowed
        final String key = DfRefreshProperties.KEY_refreshMap;
        final Object obj = getLittleAdjustmentMap().get(key);
        return obj != null ? castToMap(obj, key) : null;
    }

    // ===================================================================================
    //                                                              Optimistic Lock Facade
    //                                                              ======================
    public Map<String, Object> getOptimisticLockFacadeMap() { // null allowed
        final String key = DfOptimisticLockProperties.KEY_optimisticLockMap;
        final Object obj = getLittleAdjustmentMap().get(key);
        return obj != null ? castToMap(obj, key) : null;
    }

    // ===================================================================================
    //                                                                     Sequence Facade
    //                                                                     ===============
    // quit because of fundamental properties
    //public Map<String, Object> getSequenceFacadeMap() { // null allowed
    //    final String key = DfSequenceIdentityProperties.KEY_sequenceMap;
    //    return castToMap(getLittleAdjustmentMap().get(key), key);
    //}

    // ===================================================================================
    //                                                                        Batch Update
    //                                                                        ============
    public boolean isBatchInsertColumnModifiedPropertiesFragmentedDisallowed() { // closet
        // BatchInsert can allow fragmented properties (least common multiple)
        final boolean defaultValue = !isProperty("isBatchInsertColumnModifiedPropertiesFragmentedAllowed", true);
        return isProperty("isBatchInsertColumnModifiedPropertiesFragmentedDisallowed", defaultValue);
    }

    public boolean isBatchUpdateColumnModifiedPropertiesFragmentedAllowed() { // closet
        return isProperty("isBatchUpdateColumnModifiedPropertiesFragmentedAllowed", false);
    }

    // ===================================================================================
    //                                                                        Query Update
    //                                                                        ============
    public boolean isQueryUpdateCountPreCheck() { // closet
        // to avoid Otegaru Deadlock of MySQL RepeatableRead
        final boolean defaultValue = isProperty("isCheckCountBeforeQueryUpdate", false); // for compatible
        return isProperty("isQueryUpdateCountPreCheck", defaultValue);
    }

    // ===================================================================================
    //                                                                    Insert or Update
    //                                                                    ================
    public boolean isInsertOrUpdateCountPreCheck() { // closet
        // to avoid Otegaru Deadlock of MySQL RepeatableRead
        return isProperty("isInsertOrUpdateCountPreCheck", false);
    }

    // ===================================================================================
    //                                                                         Zero Update
    //                                                                         ===========
    public boolean isZeroUpdateCheckExistenceForPassing() { // closet, for e.g. Atlas
        // e.g. Atlas (MySQL proxy) returns changed, not matched, so needs to check after zero update
        return isProperty("isZeroUpdateCheckExistenceForPassing", false);
    }

    // ===================================================================================
    //                                                                   ReadOnly Generate
    //                                                                   =================
    public boolean isReadOnlySchema() { // closet, for compile speed, safety
        return isProperty("isReadOnlySchema", false);
    }

    // e.g. littleAdjustmentMap.dfprop
    //  ; readOnlyTableNameList = list:{
    //      ; prefix:PURCHASE
    //  
    //      ; !PURCHASE_PAYMENT
    //  }
    // => PURCHASE is readable, PURCHASE_PAYMENT is writable
    protected List<String> _readOnlyTableNameList;
    protected Set<String> _exceptReadOnlyTableNameSet;

    protected List<String> getReadOnlyTableNameList() {
        if (_readOnlyTableNameList != null) {
            return _readOnlyTableNameList;
        }
        final String key = "readOnlyTableNameList";
        final Object obj = getLittleAdjustmentMap().get(key);
        final List<String> plainList = obj != null ? castToList(obj, key) : new ArrayList<String>();
        _readOnlyTableNameList = new ArrayList<String>();
        _exceptReadOnlyTableNameSet = StringSet.createAsFlexible(); // fit with name-hint logic
        for (String element : plainList) {
            if (element.startsWith("!")) {
                _exceptReadOnlyTableNameSet.add(Srl.ltrim(element, "!"));
            } else {
                _readOnlyTableNameList.add(element);
            }
        }
        return _readOnlyTableNameList;
    }

    public boolean isReadOnlyTable(String tableDbName) {
        final List<String> targetList = getReadOnlyTableNameList(); // also initialize
        if (_exceptReadOnlyTableNameSet.contains(tableDbName)) { // after initialization
            return false;
        }
        return !targetList.isEmpty() && isTargetByHint(tableDbName, targetList, DfCollectionUtil.emptyList());
    }

    // *quit support because of incomplete, not look much like DBFlute policy
    //// ===================================================================================
    ////                                                         SetupSelect Forced Relation
    ////                                                         ===========================
    //// /= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
    //// e.g. MEMBER_SERVICE to SERVICE_RANK
    //// <supported>
    //// MemberCB cb = new MemberCB();
    //// cb.setupSelect_MemberService();
    ////
    //// PurchaseCB cb = new PurchaseCB();
    //// cb.setupSelect_Member().withMemberService();
    ////
    //// <unsupported>
    //// MemberServiceCB cb = new MemberServiceCB();
    //// *no timing for auto relation
    //// = = = = = = = = = =/
    //public static final String KEY_setupSelectForcedRelationMap = "setupSelectForcedRelationMap";
    //protected Map<String, Set<String>> _setupSelectForcedRelationMap;
    //
    //public Map<String, Set<String>> getSetupSelectForcedRelationMap() { // closet
    //    if (_setupSelectForcedRelationMap != null) {
    //        return _setupSelectForcedRelationMap;
    //    }
    //    final Map<String, Object> littleAdjustmentMap = getLittleAdjustmentMap();
    //    final Object obj = littleAdjustmentMap.get(KEY_setupSelectForcedRelationMap);
    //    final Map<String, Set<String>> resultMap = StringKeyMap.createAsFlexibleOrdered();
    //    if (obj != null) {
    //        @SuppressWarnings("unchecked")
    //        Map<String, Object> propMap = (Map<String, Object>) obj;
    //        for (Entry<String, Object> entry : propMap.entrySet()) {
    //            final String key = entry.getKey();
    //            final Object value = entry.getValue();
    //            if (!(value instanceof List<?>)) {
    //                final String typeExp = value != null ? value.getClass().getName() : null;
    //                String msg = "The element of forcedNextRelationMap should be list but: " + typeExp + " key=" + key;
    //                throw new DfIllegalPropertyTypeException(msg);
    //            }
    //            @SuppressWarnings("unchecked")
    //            final List<String> valueList = (List<String>) value;
    //            final Set<String> relationSet = StringSet.createAsCaseInsensitive(); // not flexible for relation name
    //            relationSet.addAll(valueList);
    //            resultMap.put(key, relationSet);
    //        }
    //    }
    //    _setupSelectForcedRelationMap = resultMap;
    //    return _setupSelectForcedRelationMap;
    //}
    //
    //public Set<String> getSetupSelectForcedRelationSet(String tableName) {
    //    return getSetupSelectForcedRelationMap().get(tableName);
    //}
    //
    //public void checkSetupSelectForcedRelation(DfTableFinder tableFinder) {
    //    final String propKey = KEY_setupSelectForcedRelationMap;
    //    final Map<String, Set<String>> relationMap = getSetupSelectForcedRelationMap();
    //    for (Entry<String, Set<String>> entry : relationMap.entrySet()) {
    //        final String tableName = entry.getKey();
    //        final Table table = tableFinder.findTable(tableName);
    //        if (table == null) {
    //            String msg = "Not found the table: " + tableName + " in " + propKey;
    //            throw new DfPropertySettingTableNotFoundException(msg);
    //        }
    //        final Set<String> relationSet = entry.getValue();
    //        for (String relation : relationSet) {
    //            boolean found = false;
    //            final List<ForeignKey> foreignKeyList = table.getForeignKeyList();
    //            for (ForeignKey fk : foreignKeyList) {
    //                if (fk.getForeignPropertyName().equalsIgnoreCase(relation)) {
    //                    found = true;
    //                }
    //            }
    //            List<ForeignKey> referrerAsOneList = table.getReferrerAsOneList();
    //            for (ForeignKey fk : referrerAsOneList) {
    //                if (fk.getReferrerPropertyNameAsOne().equalsIgnoreCase(relation)) {
    //                    found = true;
    //                }
    //            }
    //            if (!found) {
    //                String msg = "Not found the relation: " + relation + " of " + tableName + " in " + propKey;
    //                throw new DfPropertySettingTableNotFoundException(msg);
    //            }
    //        }
    //    }
    //}

    // ===================================================================================
    //                                                          Suppress Referrer Relation
    //                                                          ==========================
    // to suppress referrer relation that are not used in application (not doc-only task)
    // so basically don't use this, however just in case
    public static final String KEY_suppressReferrerRelationMap = "suppressReferrerRelationMap";
    protected Map<String, Set<String>> _suppressReferrerRelationMap;

    public Map<String, Set<String>> getSuppressReferrerRelationMap() { // closet
        if (_suppressReferrerRelationMap != null) {
            return _suppressReferrerRelationMap;
        }
        final Map<String, Object> littleAdjustmentMap = getLittleAdjustmentMap();
        final Object obj = littleAdjustmentMap.get(KEY_suppressReferrerRelationMap);
        final Map<String, Set<String>> resultMap = StringKeyMap.createAsFlexibleOrdered();
        if (obj != null) {
            final boolean generateTask = !isDocOnlyTask(); // not doc-only task means generate (contains sql2entity)
            @SuppressWarnings("unchecked")
            final Map<String, Object> propMap = (Map<String, Object>) obj;
            for (Entry<String, Object> entry : propMap.entrySet()) {
                final String key = entry.getKey();
                final Object value = entry.getValue();
                if (!(value instanceof List<?>)) {
                    final String typeExp = value != null ? value.getClass().getName() : null;
                    String msg = "The element of suppressReferrerRelationMap should be list but: " + typeExp + " key=" + key;
                    throw new DfIllegalPropertyTypeException(msg);
                }
                @SuppressWarnings("unchecked")
                final List<String> valueList = (List<String>) value;
                final Set<String> relationSet = StringSet.createAsCaseInsensitive(); // not flexible for relation name
                for (String relation : valueList) {
                    final String genSuffix = "@gen";
                    if (relation.endsWith(genSuffix)) {
                        if (generateTask) {
                            relationSet.add(Srl.substringLastFront(relation, genSuffix));
                        }
                    } else {
                        relationSet.add(relation);
                    }
                }
                resultMap.put(key, relationSet);
            }
        }
        _suppressReferrerRelationMap = resultMap;
        return _suppressReferrerRelationMap;
    }

    // ===================================================================================
    //                                                                 PG Reservation Word
    //                                                                 ===================
    protected Set<String> _pgReservColumnSet;

    protected Set<String> getPgReservColumnSet() { // closet
        if (_pgReservColumnSet != null) {
            return _pgReservColumnSet;
        }
        _pgReservColumnSet = StringSet.createAsFlexible();
        final Map<String, Object> littleAdjustmentMap = getLittleAdjustmentMap();
        final Object obj = littleAdjustmentMap.get("pgReservColumnList");
        final List<String> pgReservColumnList;
        if (obj != null) {
            pgReservColumnList = castToList(obj, "littleAdjustmentMap.pgReservColumnList");
        } else {
            pgReservColumnList = DfCollectionUtil.emptyList();
        }
        _pgReservColumnSet.addAll(pgReservColumnList);
        return _pgReservColumnSet;
    }

    public boolean isPgReservColumn(String columnName) {
        final Set<String> pgReservColumnList = getPgReservColumnSet();
        if (pgReservColumnList.isEmpty()) {
            return getLanguageDependency().getLanguageGrammar().isPgReservColumn(columnName);
        } else {
            return pgReservColumnList.contains(columnName);
        }
    }

    public String resolvePgReservColumn(String columnName) {
        if (isPgReservColumn(columnName)) {
            return columnName + (getBasicProperties().isColumnNameCamelCase() ? "Synonym" : "_SYNONYM");
        }
        return columnName;
    }

    // ===================================================================================
    //                                                            Non Compilable Connector
    //                                                            ========================
    public boolean isSuppressNonCompilableConnectorLimiter() { // closet
        return isProperty("isSuppressNonCompilableConnectorLimiter", false);
    }

    public String filterJavaNameNonCompilableConnector(String javaName, NonCompilableChecker checker) {
        checkNonCompilableConnector(checker.name(), checker.disp());
        final List<String> connectorList = getNonCompilableConnectorList();
        for (String connector : connectorList) {
            javaName = Srl.replace(javaName, connector, "_");
        }
        return javaName;
    }

    public static interface NonCompilableChecker {
        String name();

        String disp();
    }

    public void checkNonCompilableConnector(String name, String disp) {
        if (isSuppressNonCompilableConnectorLimiter()) {
            return;
        }
        if (containsNonCompilableConnector(name)) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("Non-compilable connectors in a table/column name were found.");
            br.addItem("Advice");
            br.addElement("Non-compilable connectors are unsupported.");
            br.addElement("For example, 'HYPHEN-TABLE' and 'SPACE COLUMN' and so on...");
            br.addElement("You should change the names like this:");
            br.addElement("  HYPHEN-TABLE -> HYPHEN_TABLE");
            br.addElement("  SPACE COLUMN -> SPACE_COLUMN");
            br.addElement("");
            br.addElement("If you cannot change by any possibility, you can suppress its limiter.");
            br.addElement(" -> isSuppressNonCompilableConnectorLimiter in littleAdjustmentMap.dfprop.");
            br.addElement("However several functions may not work. It's a restriction.");
            br.addItem("Target Object");
            br.addElement(disp);
            final String msg = br.buildExceptionMessage();
            throw new DfTableColumnNameNonCompilableConnectorException(msg);
        }
    }

    protected boolean containsNonCompilableConnector(String tableName) {
        final List<String> connectorList = getNonCompilableConnectorList();
        return Srl.containsAny(tableName, connectorList.toArray(new String[] {}));
    }

    protected List<String> getNonCompilableConnectorList() {
        return DfCollectionUtil.newArrayList("-", " "); // non property
    }

    // ===================================================================================
    //                                                                      Flat Expansion
    //                                                                      ==============
    // unsupported completely @since 1.0.5K
    //public boolean isMakeFlatExpansion() { // closet, closed function permanently
    //    return isProperty("isMakeFlatExpansion", false);
    //}

    // ===================================================================================
    //                                                                   Alternate Control
    //                                                                   =================
    public boolean isAlternateGenerateControlValid() {
        final String str = getAlternateGenerateControl();
        return str != null && str.trim().length() > 0 && !str.trim().equals("null");
    }

    public String getAlternateGenerateControl() { // closet
        return getProperty("alternateGenerateControl", null);
    }

    public boolean isAlternateSql2EntityControlValid() {
        final String str = getAlternateSql2EntityControl();
        return str != null && str.trim().length() > 0 && !str.trim().equals("null");
    }

    public String getAlternateSql2EntityControl() { // closet
        return getProperty("alternateSql2EntityControl", null);
    }

    // ===================================================================================
    //                                                                    Internal Options
    //                                                                    ================
    // -----------------------------------------------------
    //                                         Stop Generate
    //                                         -------------
    public boolean isStopGenerateExtendedBhv() { // closet
        return isProperty("isStopGenerateExtendedBhv", false);
    }

    public boolean isStopGenerateExtendedDao() { // closet
        return isProperty("isStopGenerateExtendedDao", false);
    }

    public boolean isStopGenerateExtendedEntity() { // closet
        return isProperty("isStopGenerateExtendedEntity", false);
    }

    // -----------------------------------------------------
    //                                Delete Old Table Class
    //                                ----------------------
    public boolean isDeleteOldTableClass() { // closet
        // The default value is true since 0.8.8.1.
        return isProperty("isDeleteOldTableClass", true);
    }

    // -----------------------------------------------------
    //                            Skip Generate If Same File
    //                            --------------------------
    public boolean isSkipGenerateIfSameFile() { // closet
        // The default value is true since 0.7.8.
        return isProperty("isSkipGenerateIfSameFile", true);
    }

    // -----------------------------------------------------
    //                ToLower in Generator Underscore Method
    //                --------------------------------------
    public boolean isAvailableToLowerInGeneratorUnderscoreMethod() { // closet
        return isProperty("isAvailableToLowerInGeneratorUnderscoreMethod", true);
    }

    // -----------------------------------------------------
    //                              System Random Management
    //                              ------------------------
    public boolean isFilterSystemRandomDefaultValue() { // closet, for H2 database first
        return isProperty("isFilterSystemRandomDefaultValue", false);
    }

    public boolean isSuppressSystemRandomSequence() { // closet, for H2 database first
        return isProperty("isSuppressSystemRandomSequence", false);
    }

    // ===================================================================================
    //                                                                               S2Dao
    //                                                                               =====
    public boolean isMakeDaoInterface() { // closet, basically CSharp only
        final DfLanguageFramework framework = getLanguageDependency().getLanguageFramework();
        return booleanProp("torque.isMakeDaoInterface", framework.isMakeDaoInterface());
    }

    // S2Dao.NET does not implement ValueType attribute,
    // so this property is INVALID now. At the future,
    // DBFlute may implement ValueType Framework. 
    public boolean isUseAnsiStringTypeToNotUnicode() { // closet, CSharp only
        return isProperty("isUseAnsiStringTypeToNotUnicode", false);
    }

    // ===================================================================================
    //                                                                             FreeGen
    //                                                                             =======
    public boolean isFreeGenJavaScriptEngineNashorn() { // closet, for emergency debug
        return isProperty("isFreeGenJavaScriptEngineNashorn", false);
    }

    // ===================================================================================
    //                                                                          LastaFlute
    //                                                                          ==========
    public boolean isLastaMigrationAlsoGenearteDiXml() { // closet
        // generate Di xml even if other DI container to gradually migrate to LastaFlute
        return isProperty("isLastaMigrationAlsoGenearteDiXml", false);
    }

    // ===================================================================================
    //                                                                     Jakarta Package
    //                                                                     ===============
    public String getCurrentJakartaPackage() {
        return isMigrateOldJavaxToJakarta() ? /*new*/"jakarta" : /*old*/"javax";
    }

    protected boolean isMigrateOldJavaxToJakarta() { // closet
        // many javax packages are moved to jakarta packages from since Tomcat10
        // this property is for compatible velocity templates
        return isProperty("isMigrateOldJavaxToJakarta", false); // will be true for the future
    }

    // ===================================================================================
    //                                                                          Compatible
    //                                                                          ==========
    public boolean isCompatibleAutoMappingOldStyle() { // closet
        return isProperty("isCompatibleAutoMappingOldStyle", false);
    }

    public boolean isCompatibleInsertColumnNotNullOnly() { // closet
        return isProperty("isCompatibleInsertColumnNotNullOnly", false);
    }

    public boolean isCompatibleBatchInsertDefaultEveryColumn() { // closet
        return isProperty("isCompatibleBatchInsertDefaultEveryColumn", false);
    }

    public boolean isCompatibleBatchUpdateDefaultEveryColumn() { // closet
        return isProperty("isCompatibleBatchUpdateDefaultEveryColumn", false);
    }

    public boolean isCompatibleConditionInlineQueryAlwaysGenerate() { // closet
        return isProperty("isCompatibleConditionInlineQueryAlwaysGenerate", false);
    }

    public boolean isCompatibleNestSelectSetupperAlwaysGenerate() { // closet
        return isProperty("isCompatibleNestSelectSetupperAlwaysGenerate", false);
    }

    public boolean isCompatibleFreeGenPropVariableNotOrdered() { // closet
        return isProperty("isCompatibleFreeGenPropVariableNotOrdered", false);
    }

    public boolean isCompatibleUserMessagesVariableNotOrdered() { // closet
        return isProperty("isCompatibleUserMessagesVariableNotOrdered", false);
    }

    public boolean isCompatibleNativeInScopePublicForcedly() { // closet
        return isProperty("isCompatibleNativeInScopePublicForcedly", false);
    }

    public boolean isCompatibleMonolithicDiffMapHistory() { // closet, for emergency so will be deleted future
        return isProperty("isCompatibleMonolithicDiffMapHistory", false);
    }

    public boolean isCompatibleProcedureReturnValueNameAsArg() { // closet, "returnValue" back to "arg1"
        return isProperty("isCompatibleProcedureReturnValueNameAsArg", false); // since 1.2.9
    }

    // -----------------------------------------------------
    //                                      Java8 Compatible
    //                                      ----------------
    public boolean isCompatibleSelectByPKOldStyle() { // closet
        return isProperty("isCompatibleSelectByPKOldStyle", isCompatibleBeforeJava8());
    }

    public boolean isCompatibleSelectByPKPlainReturn() { // closet
        return isProperty("isCompatibleSelectByPKPlainReturn", isAvailableSelectEntityPlainReturn());
    }

    public boolean isCompatibleSelectByPKWithDeletedCheck() { // closet
        return isProperty("isCompatibleSelectByPKWithDeletedCheck", isCompatibleBeforeJava8());
    }

    public boolean isCompatibleOrScopeQueryPurposeNoCheck() { // closet
        return isProperty("isCompatibleOrScopeQueryPurposeNoCheck", isCompatibleBeforeJava8());
    }

    public boolean isCompatibleNewMyEntityConditionBean() { // closet
        return isProperty("isCompatibleNewMyEntityConditionBean", isCompatibleBeforeJava8());
    }

    public boolean isCompatibleDeleteNonstrictIgnoreDeleted() { // closet
        return isProperty("isCompatibleDeleteNonstrictIgnoreDeleted", isCompatibleBeforeJava8());
    }

    public boolean isCompatibleLoadReferrerConditionBeanSetupper() { // closet
        return isProperty("isCompatibleLoadReferrerConditionBeanSetupper", isCompatibleLoadReferrerOldOption());
    }

    public boolean isCompatibleLoadReferrerOldOption() { // closet
        return isProperty("isCompatibleLoadReferrerOldOption", isCompatibleBeforeJava8());
    }

    public boolean isCompatibleConditionBeanAcceptPKOldStyle() { // closet
        return isProperty("isCompatibleConditionBeanAcceptPKOldStyle", isCompatibleBeforeJava8());
    }

    public boolean isCompatibleConditionBeanOldNamingCheckInvalid() { // closet
        return isProperty("isCompatibleConditionBeanOldNamingCheckInvalid", isCompatibleBeforeJava8());
    }

    public boolean isCompatibleConditionBeanOldNamingOption() { // closet
        return isProperty("isCompatibleConditionBeanOldNamingOption", isCompatibleBeforeJava8());
    }

    public boolean isCompatibleConditionBeanFromToOneSideAllowed() { // closet
        return isProperty("isCompatibleConditionBeanFromToOneSideAllowed", isCompatibleBeforeJava8());
    }

    public boolean isCompatibleBizOneToOneImplicitReverseFkAllowed() { // closet
        return isProperty("isCompatibleBizOneToOneImplicitReverseFkAllowed", isCompatibleBeforeJava8());
    }

    public boolean isCompatibleDfPropDuplicateEntryIgnored() { // closet
        return isProperty("isCompatibleDfPropDuplicateEntryIgnored", isCompatibleBeforeJava8());
    }

    public boolean isCompatibleReferrerCBMethodIdentityNameListSuffix() { // closet
        return isProperty("isCompatibleReferrerCBMethodIdentityNameListSuffix", isCompatibleBeforeJava8());
    }

    public boolean isCompatibleOutsideSqlFacadeChainOldStyle() { // closet
        return isProperty("isCompatibleOutsideSqlFacadeChainOldStyle", isCompatibleBeforeJava8());
    }

    public boolean isCompatibleOutsideSqlSqlCommentCheckDefault() { // closet
        return isProperty("isCompatibleOutsideSqlSqlCommentCheckDefault", isCompatibleBeforeJava8());
    }

    public boolean isCompatibleSelectScalarOldName() { // closet
        return isProperty("isCompatibleSelectScalarOldName", isCompatibleBeforeJava8());
    }

    public boolean isCompatibleBeforeJava8() { // closet
        final boolean defaultValue = getLanguageDependency().getLanguageImplStyle().isCompatibleBeforeJava8();
        return isProperty("isCompatibleBeforeJava8", defaultValue);
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfLanguageDependency getLanguageDependency() {
        return getBasicProperties().getLanguageDependency();
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String replace(String text, String fromText, String toText) {
        return Srl.replace(text, fromText, toText);
    }
}