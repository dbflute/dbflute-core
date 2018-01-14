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
package org.dbflute.logic.sql2entity.analyzer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.torque.engine.database.model.AppData;
import org.dbflute.DfBuildProperties;
import org.dbflute.dbway.DBDef;
import org.dbflute.logic.generate.language.DfLanguageDependency;
import org.dbflute.logic.generate.language.grammar.DfLanguageGrammar;
import org.dbflute.logic.generate.language.pkgstyle.DfLanguagePropertyPackageResolver;
import org.dbflute.logic.generate.language.typemapping.DfLanguageTypeMapping;
import org.dbflute.logic.sql2entity.bqp.DfBehaviorQueryPathSetupper;
import org.dbflute.logic.sql2entity.pmbean.DfPmbMetaData;
import org.dbflute.logic.sql2entity.pmbean.DfPmbMetaData.DfPagingType;
import org.dbflute.logic.sql2entity.pmbean.DfPmbPropertyOptionComment;
import org.dbflute.outsidesql.ProcedurePmb;
import org.dbflute.outsidesql.paging.SimplePagingBean;
import org.dbflute.properties.DfBasicProperties;
import org.dbflute.properties.DfDatabaseProperties;
import org.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfParameterBeanResolver {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final List<String> _reservBooleanMethodList = new ArrayList<String>();
    static {
        for (Method method : SimplePagingBean.class.getMethods()) {
            if (method.getReturnType().equals(boolean.class)) {
                _reservBooleanMethodList.add(method.getName());
            }
        }
        for (Method method : ProcedurePmb.class.getMethods()) {
            if (method.getReturnType().equals(boolean.class)) {
                _reservBooleanMethodList.add(method.getName());
            }
        }
    }

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfOutsideSqlFile _outsideSqlFile;
    protected final AppData _schemaData; // to resolve option, null allowed if no option
    protected final DfSql2EntityMarkAnalyzer _outsideSqlMarkAnalyzer = new DfSql2EntityMarkAnalyzer();
    protected final DfOutsideSqlNameResolver _sqlFileNameResolver = new DfOutsideSqlNameResolver();
    protected final DfBehaviorQueryPathSetupper _bqpSetupper = new DfBehaviorQueryPathSetupper();

    // temporary collection resolved by auto-detect
    protected final Set<String> _alternateBooleanMethodNameSet = new LinkedHashSet<String>();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfParameterBeanResolver(DfOutsideSqlFile outsideSqlFile, AppData schemaData) {
        _outsideSqlFile = outsideSqlFile;
        _schemaData = schemaData;
    }

    // ===================================================================================
    //                                                                             Extract
    //                                                                             =======
    /**
     * Extract the meta data of parameter bean.
     * @param sql Target SQL. (NotNull and NotEmpty)
     * @return the meta data of parameter bean. (NullAllowed: If it returns null, it means 'not found'.)
     */
    public DfPmbMetaData extractPmbMetaData(String sql) {
        final String parameterBeanName = getParameterBeanName(sql);
        if (parameterBeanName == null) {
            return null;
        }
        final DfPmbMetaData pmbMetaData = new DfPmbMetaData();
        processClassHeader(sql, parameterBeanName, pmbMetaData);
        processParameterProperty(sql, parameterBeanName, pmbMetaData);
        pmbMetaData.setOutsideSqlFile(_outsideSqlFile);
        pmbMetaData.adjustPropertyMetaFinally(_schemaData);

        filterAlternateBooleanMethod(pmbMetaData);
        if (!_alternateBooleanMethodNameSet.isEmpty()) {
            // copy and clear the collection just in case
            final Set<String> set = new LinkedHashSet<String>(_alternateBooleanMethodNameSet);
            pmbMetaData.setAlternateMethodBooleanNameSet(set);
            _alternateBooleanMethodNameSet.clear();
        }
        return pmbMetaData;
    }

    protected void filterAlternateBooleanMethod(DfPmbMetaData pmbMetaData) {
        if (_alternateBooleanMethodNameSet.isEmpty()) {
            return;
        }
        for (String reservBooleanMethod : _reservBooleanMethodList) {
            if (_alternateBooleanMethodNameSet.contains(reservBooleanMethod)) {
                _alternateBooleanMethodNameSet.remove(reservBooleanMethod);
            }
        }
        final Map<String, String> propertyNameTypeMap = pmbMetaData.getPropertyNameTypeMap();
        for (String propertyName : propertyNameTypeMap.keySet()) {
            final String getterName = "get" + Srl.initCap(propertyName);
            if (_alternateBooleanMethodNameSet.contains(getterName)) {
                _alternateBooleanMethodNameSet.remove(getterName);
            }
            final String isName = "is" + Srl.initCap(propertyName);
            if (_alternateBooleanMethodNameSet.contains(isName)) {
                _alternateBooleanMethodNameSet.remove(isName);
            }
        }
    }

    // ===================================================================================
    //                                                                        Class Header
    //                                                                        ============
    protected void processClassHeader(String sql, String parameterBeanName, DfPmbMetaData pmbMetaData) {
        final String delimiter = "extends";
        final int idx = parameterBeanName.indexOf(delimiter);
        {
            String className = (idx >= 0) ? parameterBeanName.substring(0, idx) : parameterBeanName;
            className = className.trim();
            className = resolvePmbNameIfNeeds(className, _outsideSqlFile);
            pmbMetaData.setClassName(className);
        }
        if (idx >= 0) {
            final String superClassName = parameterBeanName.substring(idx + delimiter.length()).trim();
            pmbMetaData.setSuperClassName(superClassName);
            resolveSuperClassSimplePagingBean(pmbMetaData);
        }
    }

    protected void resolveSuperClassSimplePagingBean(DfPmbMetaData pmbMetaData) {
        final String superExp = pmbMetaData.getSuperClassName();
        if (Srl.endsWithIgnoreCase(superExp, "Paging") // main
                || Srl.equalsIgnoreCase(superExp, "SPB")) { // an old style for compatibility before 0.9.7.5
            final String superClassName = SimplePagingBean.class.getSimpleName();
            pmbMetaData.setSuperClassName(superClassName);
            if (Srl.equalsIgnoreCase(superClassName, "Paging", "SqlSkipPaging", "ManualPaging")) {
                pmbMetaData.setPagingType(DfPagingType.MANUAL);
            } else if (Srl.equalsIgnoreCase(superClassName, "CursorSkipPaging", "AutoPaging")) {
                pmbMetaData.setPagingType(DfPagingType.AUTO);
            } else {
                pmbMetaData.setPagingType(DfPagingType.UNKNOWN);
            }
        }
    }

    // ===================================================================================
    //                                                                  Parameter Property
    //                                                                  ==================
    protected void processParameterProperty(String sql, String parameterBeanName, DfPmbMetaData pmbMetaData) {
        final Map<String, String> propertyNameTypeMap = new LinkedHashMap<String, String>();
        final Map<String, String> propertyNameOptionMap = new LinkedHashMap<String, String>();
        final Set<String> autoDetectedPropertyNameSet = new LinkedHashSet<String>();
        pmbMetaData.setPropertyNameTypeMap(propertyNameTypeMap);
        pmbMetaData.setPropertyNameOptionMap(propertyNameOptionMap);
        pmbMetaData.setAutoDetectedPropertyNameSet(autoDetectedPropertyNameSet);
        final List<DfSql2EntityMark> parameterBeanElement = getParameterBeanPropertyTypeList(sql);
        final String autoDetectMark = "AutoDetect";
        for (DfSql2EntityMark mark : parameterBeanElement) {
            final String element = mark.getContent().trim();
            if (element.equalsIgnoreCase(autoDetectMark)) {
                processAutoDetect(sql, propertyNameTypeMap, propertyNameOptionMap, autoDetectedPropertyNameSet);
                break;
            }
        }
        for (DfSql2EntityMark mark : parameterBeanElement) {
            final String element = mark.getContent().trim();
            final String nameDelimiter = " ";
            final String optionDelimiter = ":";
            if (autoDetectMark.equals(element)) {
                continue; // because of already resolved
            }
            final int delimiterIndex = element.indexOf(optionDelimiter);
            final String slaslaOption;
            {
                final String optionPrefix = DfPmbPropertyOptionComment.OPTION_PREFIX;
                final String optionSuffix = DfPmbPropertyOptionComment.OPTION_SUFFIX;
                final String comment = mark.getComment();
                if (Srl.is_NotNull_and_NotTrimmedEmpty(comment)) {
                    final String filtered = Srl.replace(comment, "|", "/").trim();
                    slaslaOption = optionPrefix + filtered + optionSuffix;
                } else {
                    slaslaOption = null;
                }
            }
            final String propertyDef;
            final String optionDef;
            if (delimiterIndex > 0) {
                propertyDef = element.substring(0, delimiterIndex).trim();
                final int optionIndex = delimiterIndex + optionDelimiter.length();
                final String basicOption = element.substring(optionIndex).trim();
                optionDef = basicOption + (slaslaOption != null ? "|" + slaslaOption : "");
            } else {
                propertyDef = element;
                optionDef = (slaslaOption != null ? slaslaOption : null);
            }
            final int nameIndex = propertyDef.lastIndexOf(nameDelimiter);
            if (nameIndex <= 0) {
                String msg = "The parameter bean element should be [typeName propertyName].";
                msg = msg + " But: element=" + element + " srcFile=" + _outsideSqlFile;
                throw new IllegalStateException(msg);
            }
            // ParameterBean has the "import" clause of language-embedded utility
            final String typeName = prepareDefinedPropertyType(propertyDef, nameIndex);
            final String propertyName = propertyDef.substring(nameIndex + nameDelimiter.length()).trim();
            if (propertyNameTypeMap.containsKey(propertyName)) {
                // means the auto-detected property is found,
                // and it should be overridden
                propertyNameTypeMap.remove(propertyName);
                propertyNameOptionMap.remove(propertyName);
            }
            propertyNameTypeMap.put(propertyName, typeName);
            if (optionDef != null) {
                propertyNameOptionMap.put(propertyName, optionDef);
            }
        }
        processFloatingParameterComment(sql, propertyNameTypeMap, propertyNameOptionMap);
        final Map<String, Map<String, String>> bqpMap = _bqpSetupper.extractBasicBqpMap(createOutsideSqlPackAsOne());
        if (!bqpMap.isEmpty()) {
            final Map<String, String> bqpElementMap = bqpMap.values().iterator().next();
            pmbMetaData.setBqpElementMap(bqpElementMap);
        }
    }

    protected String prepareDefinedPropertyType(String propertyDef, int nameIndex) {
        return switchPlainTypeName(resolvePackageNameExceptUtil(propertyDef.substring(0, nameIndex).trim()));
    }

    protected String resolvePackageNameExceptUtil(String typeName) {
        final DfLanguageDependency lang = getBasicProperties().getLanguageDependency();
        final DfLanguagePropertyPackageResolver resolver = lang.getLanguagePropertyPackageResolver();
        return resolver.resolvePackageNameExceptUtil(typeName);
    }

    protected String switchPlainTypeName(String plainTypeName) {
        final DfLanguageTypeMapping typeMapping = getBasicProperties().getLanguageDependency().getLanguageTypeMapping();
        return typeMapping.switchParameterBeanTestValueType(plainTypeName);
    }

    protected void processFloatingParameterComment(String sql, Map<String, String> propertyNameTypeMap,
            Map<String, String> propertyNameOptionMap) {
        final Map<String, String> commentMap = _outsideSqlMarkAnalyzer.getFloatingParameterCommentMap(sql);
        final Map<String, String> renewalOptionMap = new LinkedHashMap<String, String>();
        final String commentOptionPrefix = "comment(";
        final String commentOptionSuffix = ")";
        for (String propertyName : propertyNameTypeMap.keySet()) {
            final String option = propertyNameOptionMap.get(propertyName);
            if (option != null && option.contains(commentOptionPrefix)) { // already exists
                continue;
            }
            final String comment = commentMap.get(propertyName);
            if (Srl.is_NotNull_and_NotTrimmedEmpty(comment)) {
                if (Srl.is_NotNull_and_NotTrimmedEmpty(option)) {
                    final String filteredOption = option + "|" + commentOptionPrefix + comment + commentOptionSuffix;
                    renewalOptionMap.put(propertyName, filteredOption);
                } else {
                    renewalOptionMap.put(propertyName, commentOptionPrefix + comment + commentOptionSuffix);
                }
            }
        }
        propertyNameOptionMap.putAll(renewalOptionMap);
    }

    protected DfOutsideSqlPack createOutsideSqlPackAsOne() {
        final DfOutsideSqlPack pack = new DfOutsideSqlPack();
        pack.add(_outsideSqlFile);
        return pack;
    }

    // ===================================================================================
    //                                                                          AutoDetect
    //                                                                          ==========
    protected void processAutoDetect(String sql, Map<String, String> propertyNameTypeMap, Map<String, String> propertyNameOptionMap,
            Set<String> autoDetectedPropertyNameSet) {
        final DfParameterAutoDetectProcess process = new DfParameterAutoDetectProcess();
        process.processAutoDetect(sql, propertyNameTypeMap, propertyNameOptionMap, autoDetectedPropertyNameSet);
        _alternateBooleanMethodNameSet.addAll(process.getAlternateBooleanMethodNameSet());
    }

    // ===================================================================================
    //                                                                           Analyzing
    //                                                                           =========
    protected String getParameterBeanName(final String sql) {
        return _outsideSqlMarkAnalyzer.getParameterBeanName(sql);
    }

    protected List<DfSql2EntityMark> getParameterBeanPropertyTypeList(final String sql) {
        return _outsideSqlMarkAnalyzer.getParameterBeanPropertyTypeList(sql);
    }

    protected String resolvePmbNameIfNeeds(String className, DfOutsideSqlFile file) {
        return _sqlFileNameResolver.resolvePmbNameIfNeeds(className, file.getPhysicalFile().getName());
    }

    // ===================================================================================
    //                                                                          SQL Helper
    //                                                                          ==========
    protected String removeBlockComment(final String sql) {
        return Srl.removeBlockComment(sql);
    }

    protected String removeLineComment(final String sql) {
        return Srl.removeLineComment(sql); // with removing CR
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DBDef currentDBDef() {
        return getBasicProperties().getCurrentDBDef();
    }

    protected DfBasicProperties getBasicProperties() {
        return getProperties().getBasicProperties();
    }

    protected DfLanguageGrammar getLanguageGrammar() {
        return getBasicProperties().getLanguageDependency().getLanguageGrammar();
    }

    protected DfDatabaseProperties getDatabaseProperties() {
        return getProperties().getDatabaseProperties();
    }
}
