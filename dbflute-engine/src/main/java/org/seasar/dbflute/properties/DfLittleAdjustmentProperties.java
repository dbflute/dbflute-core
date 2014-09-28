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
package org.seasar.dbflute.properties;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.seasar.dbflute.exception.DfIllegalPropertySettingException;
import org.seasar.dbflute.exception.DfIllegalPropertyTypeException;
import org.seasar.dbflute.exception.DfTableColumnNameNonCompilableConnectorException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.helper.StringSet;
import org.seasar.dbflute.jdbc.ClassificationUndefinedHandlingType;
import org.seasar.dbflute.logic.generate.language.DfLanguageDependency;
import org.seasar.dbflute.logic.generate.language.framework.DfLanguageFramework;
import org.seasar.dbflute.logic.generate.language.implstyle.DfLanguageImplStyle;
import org.seasar.dbflute.optional.OptionalEntity;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public final class DfLittleAdjustmentProperties extends DfAbstractHelperProperties {

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
        return getPropertyIfNotBuildProp(key, defaultValue, getLittleAdjustmentMap());
    }

    public boolean isProperty(String key, boolean defaultValue) {
        return isPropertyIfNotExistsFromBuildProp(key, defaultValue, getLittleAdjustmentMap());
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
    public boolean isAvailableNonPrimaryKeyWritable() {
        return isProperty("isAvailableNonPrimaryKeyWritable", false);
    }

    public boolean isAvailableSelectEntityPlainReturn() { // closet
        return isProperty("isAvailableSelectEntityPlainReturn", isCompatibleBeforeJava8());
    }

    public boolean isAvailableSelectEntityWithDeletedCheck() { // closet
        // selectEntityWithDeletedCheck() can coexist with optional entity
        //final boolean defaultValue = isCompatibleUnderJava8();
        return isProperty("isAvailableSelectEntityWithDeletedCheck", true);
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
        final ClassificationUndefinedHandlingType handlingType = ClassificationUndefinedHandlingType.codeOf(code);
        if (handlingType == null) {
            throwUnknownClassificationUndefinedHandlingTypeException(code);
        }
        return handlingType;
    }

    protected String doGetClassificationUndefinedHandlingType(String defaultValue) {
        return getProperty("classificationUndefinedHandlingType", defaultValue);
    }

    protected void throwUnknownClassificationUndefinedHandlingTypeException(String code) {
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
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }

    public boolean isPlainCheckClassificationCode() { // for e.g. classificationResource
        final DfLittleAdjustmentProperties prop = getLittleAdjustmentProperties();
        final ClassificationUndefinedHandlingType undefinedHandlingType = prop.getClassificationUndefinedHandlingType();
        if (prop.hasClassificationUndefinedHandlingTypeProperty() && undefinedHandlingType.isChecked()) {
            return true;
        }
        if (prop.isSuppressDefaultCheckClassificationCode()) {
            return false;
        }
        return undefinedHandlingType.isChecked();
    }

    public boolean isSuppressDefaultCheckClassificationCode() { // closet
        return isProperty("isSuppressDefaultCheckClassificationCode", isCompatibleBeforeJava8());
    }

    // old style
    protected static final String KEY_isCheckSelectedClassification = "isCheckSelectedClassification";

    public boolean hasCheckSelectedClassificationProperty() {
        return getProperty(KEY_isCheckSelectedClassification, null) != null;
    }

    public boolean isCheckSelectedClassification() {
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

    public boolean isMakeClassificationNativeTypeSetter() {
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
    //                                          Small Option
    //                                          ------------
    public boolean isCDefToStringReturnsName() { // closet
        return isProperty("isCDefToStringReturnsName", false);
    }

    public boolean isMakeEntityOldStyleClassify() { // closet
        return isProperty("isMakeEntityOldStyleClassify", false);
    }

    public boolean isSuppressTableClassificationDBAccessClass() { // closet
        return isProperty("isSuppressTableClassificationDBAccessClass", false);
    }

    // ===================================================================================
    //                                                                              Entity
    //                                                                              ======
    public boolean isMakeEntityChaseRelation() {
        return isProperty("isMakeEntityChaseRelation", false);
    }

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
    // Java8-Time and JodaTime support
    // DfTypeMappingProperties.java, Column.java
    // DBFluteConfig.vm, AbstractBsConditionQuery.vm, BsParameterBean.vm
    public boolean isAvailableJava8OrJodaTimeEntity() {
        return isAvailableJava8TimeEntity() || isAvailableJodaTimeEntity();
    }

    public boolean isAvailableJava8TimeEntity() { // closet
        return isAvailableJava8TimeLocalDateEntity() || isAvailableJava8TimeZonedDateEntity();
    }

    public boolean isAvailableJava8TimeLocalDateEntity() { // closet
        return isProperty("isAvailableJava8TimeLocalDateEntity", false);
    }

    public boolean isAvailableJava8TimeZonedDateEntity() { // closet
        return isProperty("isAvailableJava8TimeZonedDateEntity", false); // unsupported for now
    }

    public boolean isAvailableJodaTimeEntity() {
        return isAvailableJodaTimeLocalDateEntity() || isAvailableJodaTimeZonedDateEntity();
    }

    public boolean isAvailableJodaTimeLocalDateEntity() { // closet
        return isProperty("isAvailableJodaTimeLocalDateEntity", false);
    }

    public boolean isAvailableJodaTimeZonedDateEntity() { // closet
        return isProperty("isAvailableJodaTimeZonedDateEntity", false); // unsupported for now
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
        // you should also override TnRelationOptionalFactory if you change this
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

    public boolean isNullOrEmptyQueryAllowed() { // closet
        final boolean defaultValue = isProperty("isInvalidQueryChecked", !isCompatibleBeforeJava8());
        return isProperty("isNullOrEmptyQueryAllowed", !defaultValue);
    }

    public boolean isOverridingQueryAllowed() { // closet
        return isProperty("isOverridingQueryAllowed", isCompatibleBeforeJava8());
    }

    // ===================================================================================
    //                                                                      ConditionQuery
    //                                                                      ==============
    public boolean isMakeConditionQueryEqualEmptyString() {
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
        final boolean defaultValue = isCompatibleBeforeJava8();
        return isProperty("isMakeConditionQueryExistsReferrerToOne", defaultValue);
    }

    public boolean isMakeConditionQueryInScopeRelationToOne() { // closet
        final boolean defaultValue = isCompatibleBeforeJava8();
        return isProperty("isMakeConditionQueryInScopeRelationToOne", defaultValue);
    }

    public boolean isMakeConditionQueryPlainListManualOrder() { // closet
        final boolean defaultValue = isCompatibleBeforeJava8();
        return isProperty("isMakeConditionQueryPlainListManualOrder", defaultValue);
    }

    // ===================================================================================
    //                                                                       Paging Select
    //                                                                       =============
    public boolean isPagingCountLater() {
        return isProperty("isPagingCountLater", true); // default true @since 0.9.9.0A
    }

    public boolean isPagingCountLeastJoin() {
        return isProperty("isPagingCountLeastJoin", true); // default true @since 0.9.9.0A
    }

    // ===================================================================================
    //                                                                          Inner Join
    //                                                                          ==========
    public boolean isInnerJoinAutoDetect() {
        return isProperty("isInnerJoinAutoDetect", true); // default true @since 1.0.3
    }

    // ===================================================================================
    //                                                                   That's Bad Timing
    //                                                                   =================
    public boolean isThatsBadTimingDetect() { // closet
        final boolean defaultValue = !isCompatibleBeforeJava8();
        return isProperty("isThatsBadTimingDetect", defaultValue);
    }

    // ===================================================================================
    //                                                              Display Name UpperCase
    //                                                              ======================
    public boolean isTableDispNameUpperCase() {
        return isProperty("isTableDispNameUpperCase", false);
    }

    public String filterTableDispNameIfNeeds(String tableDbName) {
        return isTableDispNameUpperCase() ? tableDbName.toUpperCase() : tableDbName;
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
    public boolean isMakeDeprecated() {
        return isProperty("isMakeDeprecated", false);
    }

    public boolean isMakeRecentlyDeprecated() {
        return isProperty("isMakeRecentlyDeprecated", true);
    }

    // ===================================================================================
    //                                                                  Extended Component
    //                                                                  ==================
    public String getDBFluteInitializerClass() { // Java only
        return getExtensionClass("DBFluteInitializer");
    }

    public String getImplementedInvokerAssistantClass() { // Java only
        return getExtensionClass("ImplementedInvokerAssistant");
    }

    public String getImplementedCommonColumnAutoSetupperClass() { // Java only
        return getExtensionClass("ImplementedCommonColumnAutoSetupper");
    }

    public String getS2DaoSettingClass() { // CSharp only
        final String className = "S2DaoSetting";
        if (hasExtensionClass(className)) {
            return getExtendedExtensionClass(className);
        } else {
            return getBasicProperties().getProjectPrefix() + className;
        }
    }

    protected String getExtensionClass(String className) {
        if (hasExtensionClass(className)) {
            return getExtendedExtensionClass(className);
        } else {
            final String commonPackage = getBasicProperties().getBaseCommonPackage();
            final String projectPrefix = getBasicProperties().getProjectPrefix();
            return commonPackage + "." + projectPrefix + className;
        }
    }

    protected boolean hasExtensionClass(String className) {
        String str = getExtendedExtensionClass(className);
        return str != null && str.trim().length() > 0 && !str.trim().equals("null");
    }

    protected String getExtendedExtensionClass(String className) {
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
    //                                                                               Quote
    //                                                                               =====
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
    //                                                              Relational Null Object
    //                                                              ======================
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
        return getRelationalNullObjectProviderForeignMap().get(tableName) != null;
    }

    public String getNullObjectProviderPackage() {
        final String pkg = (String) getRelationalNullObjectMap().get("providerPackage");
        if (pkg == null) {
            return null;
        }
        final String packageBase = getBasicProperties().getPackageBase();
        return Srl.replace(pkg, "$$packageBase$$", packageBase);
    }

    protected Map<String, String> _relationalNullObjectProviderForeignMap;

    protected Map<String, String> getRelationalNullObjectProviderForeignMap() {
        if (_relationalNullObjectProviderForeignMap != null) {
            return _relationalNullObjectProviderForeignMap;
        }
        final Map<String, Object> nullObjectMap = getRelationalNullObjectMap();
        final Object obj = nullObjectMap.get("foreignMap");
        final Map<String, String> plainMap;
        if (obj != null) {
            plainMap = castToMap(obj, "littleAdjustmentMap.relationalNullObjectMap.foreignMap");
        } else {
            plainMap = newLinkedHashMap();
        }
        _relationalNullObjectProviderForeignMap = StringKeyMap.createAsFlexibleOrdered();
        _relationalNullObjectProviderForeignMap.putAll(plainMap);
        return _relationalNullObjectProviderForeignMap;
    }

    public String getRelationalNullObjectProviderForeignExp(String tableName) {
        return getRelationalNullObjectProviderForeignMap().get(tableName);
    }

    // ===================================================================================
    //                                                                        CursorSelect
    //                                                                        ============
    public boolean isCursorSelectFetchSizeValid() {
        return getCursorSelectFetchSize() != null;
    }

    public String getCursorSelectFetchSize() {
        return getProperty("cursorSelectFetchSize", null);
    }

    public boolean isCursorSelectOptionAllowed() {
        // because this option is patch for MySQL's poor cursor select
        return getBasicProperties().isDatabaseMySQL() && isCursorSelectFetchSizeIntegerMinValue();
    }

    protected boolean isCursorSelectFetchSizeIntegerMinValue() {
        if (!isCursorSelectFetchSizeValid()) {
            return false;
        }
        return "Integer.MIN_VALUE".equals(getCursorSelectFetchSize());
    }

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
        final boolean defaultValue = isProperty("isCheckCountBeforeQueryUpdate", false); // for compatible
        return isProperty("isQueryUpdateCountPreCheck", defaultValue);
    }

    // *stop support because of incomplete, not look much like DBFlute policy
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
                    String msg = "The element of suppressReferrerRelationMap should be list but: " + typeExp + " key="
                            + key;
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
    //                                                                          Value Type
    //                                                                          ==========
    // S2Dao.NET does not implement ValueType attribute,
    // so this property is INVALID now. At the future,
    // DBFlute may implement ValueType Framework. 
    public boolean isUseAnsiStringTypeToNotUnicode() { // closet, CSharp only
        return isProperty("isUseAnsiStringTypeToNotUnicode", false);
    }

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
    //                                                                       Stop Generate
    //                                                                       =============
    public boolean isStopGenerateExtendedBhv() { // closet
        return isProperty("isStopGenerateExtendedBhv", false);
    }

    public boolean isStopGenerateExtendedDao() { // closet
        return isProperty("isStopGenerateExtendedDao", false);
    }

    public boolean isStopGenerateExtendedEntity() { // closet
        return isProperty("isStopGenerateExtendedEntity", false);
    }

    // ===================================================================================
    //                                                              Delete Old Table Class
    //                                                              ======================
    public boolean isDeleteOldTableClass() { // closet
        // The default value is true since 0.8.8.1.
        return isProperty("isDeleteOldTableClass", true);
    }

    // ===================================================================================
    //                                                          Skip Generate If Same File
    //                                                          ==========================
    public boolean isSkipGenerateIfSameFile() { // closet
        // The default value is true since 0.7.8.
        return isProperty("isSkipGenerateIfSameFile", true);
    }

    // ===================================================================================
    //                                              ToLower in Generator Underscore Method
    //                                              ======================================
    public boolean isAvailableToLowerInGeneratorUnderscoreMethod() { // closet
        return isProperty("isAvailableToLowerInGeneratorUnderscoreMethod", true);
    }

    // ===================================================================================
    //                                                                      Flat Expansion
    //                                                                      ==============
    // unsupported completely @since 1.0.5K
    //public boolean isMakeFlatExpansion() { // closet, closed function permanently
    //    return isProperty("isMakeFlatExpansion", false);
    //}

    // ===================================================================================
    //                                                                               S2Dao
    //                                                                               =====
    public boolean isMakeDaoInterface() { // closet, basically CSharp only
        final DfLanguageFramework framework = getLanguageDependency().getLanguageFramework();
        return booleanProp("torque.isMakeDaoInterface", framework.isMakeDaoInterface());
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

    public boolean isCompatibleBeforeJava8() { // closet
        final boolean defaultValue = getLanguageDependency().getLanguageImplStyle().isCompatibleBeforeJava8();
        return isProperty("isCompatibleBeforeJava8", defaultValue);
    }

    // ===================================================================================
    //                                                                            Language
    //                                                                            ========
    protected DfLanguageDependency getLanguageDependency() {
        return getBasicProperties().getLanguageDependency();
    }
}