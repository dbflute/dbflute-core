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
package org.seasar.dbflute.logic.sql2entity.analyzer;

import java.io.File;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.torque.engine.database.model.AppData;
import org.seasar.dbflute.DBDef;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.cbean.SimplePagingBean;
import org.seasar.dbflute.exception.DfCustomizeEntityDuplicateException;
import org.seasar.dbflute.exception.DfParameterBeanDuplicateException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.logic.generate.language.DfLanguageDependency;
import org.seasar.dbflute.logic.generate.language.grammar.DfLanguageGrammar;
import org.seasar.dbflute.logic.generate.language.pkgstyle.DfLanguagePropertyPackageResolver;
import org.seasar.dbflute.logic.generate.language.typemapping.DfLanguageTypeMapping;
import org.seasar.dbflute.logic.sql2entity.bqp.DfBehaviorQueryPathSetupper;
import org.seasar.dbflute.logic.sql2entity.cmentity.DfCustomizeEntityInfo;
import org.seasar.dbflute.logic.sql2entity.pmbean.DfPmbMetaData;
import org.seasar.dbflute.logic.sql2entity.pmbean.DfPmbMetaData.DfPagingType;
import org.seasar.dbflute.logic.sql2entity.pmbean.DfPmbPropertyOptionComment;
import org.seasar.dbflute.outsidesql.ProcedurePmb;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.properties.DfDatabaseProperties;
import org.seasar.dbflute.twowaysql.SqlAnalyzer;
import org.seasar.dbflute.twowaysql.node.BindVariableNode;
import org.seasar.dbflute.twowaysql.node.ForNode;
import org.seasar.dbflute.twowaysql.node.IfCommentEvaluator;
import org.seasar.dbflute.twowaysql.node.IfNode;
import org.seasar.dbflute.twowaysql.node.Node;
import org.seasar.dbflute.twowaysql.node.ScopeNode;
import org.seasar.dbflute.util.DfTypeUtil;
import org.seasar.dbflute.util.DfTypeUtil.ParseTimeException;
import org.seasar.dbflute.util.DfTypeUtil.ParseTimestampException;
import org.seasar.dbflute.util.Srl;

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
    protected final DfSql2EntityMeta _sql2entityMeta;
    protected final DfOutsideSqlFile _outsideSqlFile;
    protected final AppData _schemaData;
    protected final DfSql2EntityMarkAnalyzer _outsideSqlMarkAnalyzer = new DfSql2EntityMarkAnalyzer();
    protected final DfOutsideSqlNameResolver _sqlFileNameResolver = new DfOutsideSqlNameResolver();
    protected final DfBehaviorQueryPathSetupper _bqpSetupper = new DfBehaviorQueryPathSetupper();

    // temporary collection resolved by auto-detect
    protected final Set<String> _alternateBooleanMethodNameSet = new LinkedHashSet<String>();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfParameterBeanResolver(DfSql2EntityMeta sql2entityMeta, DfOutsideSqlFile outsideSqlFile, AppData schemaData) {
        _sql2entityMeta = sql2entityMeta;
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
        final String superClassName = pmbMetaData.getSuperClassName();
        if (Srl.endsWithIgnoreCase(superClassName, "Paging") // main
                || Srl.equalsIgnoreCase(superClassName, "SPB")) { // an old style for compatibility before 0.9.7.5
            pmbMetaData.setSuperClassName("SimplePagingBean");
            if (Srl.equalsIgnoreCase(superClassName, "ManualPaging")) {
                pmbMetaData.setPagingType(DfPagingType.MANUAL);
            } else if (Srl.equalsIgnoreCase(superClassName, "AutoPaging")) {
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

    protected DfOutsideSqlPack createOutsideSqlPackAsOne() {
        final DfOutsideSqlPack pack = new DfOutsideSqlPack();
        pack.add(_outsideSqlFile);
        return pack;
    }

    // ===================================================================================
    //                                                                          AutoDetect
    //                                                                          ==========
    // -----------------------------------------------------
    //                                                  Core
    //                                                  ----
    protected void processAutoDetect(String sql, Map<String, String> propertyNameTypeMap,
            Map<String, String> propertyNameOptionMap, Set<String> autoDetectedPropertyNameSet) {
        final SqlAnalyzer analyzer = new SqlAnalyzer(sql, false);
        final Node rootNode = analyzer.analyze();
        doProcessAutoDetect(sql, propertyNameTypeMap, propertyNameOptionMap, autoDetectedPropertyNameSet, rootNode);
    }

    protected void doProcessAutoDetect(String sql, Map<String, String> propertyNameTypeMap,
            Map<String, String> propertyNameOptionMap, Set<String> autoDetectedPropertyNameSet, Node node) {
        // only bind variable comment is supported
        // because simple specification is very important here
        if (node instanceof BindVariableNode) {
            final BindVariableNode bindNode = (BindVariableNode) node;
            processAutoDetectBindNode(sql, propertyNameTypeMap, propertyNameOptionMap, autoDetectedPropertyNameSet,
                    bindNode);
        } else if (node instanceof IfNode) {
            final IfNode ifNode = (IfNode) node;
            doProcessAutoDetectIfNode(sql, propertyNameTypeMap, propertyNameOptionMap, ifNode);

            // process alternate boolean methods
            // which is supported with auto-detect
            doProcessAlternateBooleanMethodIfNode(sql, ifNode);
        } else if (node instanceof ForNode) {
            final ForNode forNode = (ForNode) node;
            doProcessAutoDetectForNode(sql, propertyNameTypeMap, propertyNameOptionMap, forNode);
        }
        for (int i = 0; i < node.getChildSize(); i++) {
            final Node childNode = node.getChild(i);

            // recursive call
            doProcessAutoDetect(sql, propertyNameTypeMap, propertyNameOptionMap, autoDetectedPropertyNameSet, childNode);
        }
    }

    // -----------------------------------------------------
    //                                         Bind Variable
    //                                         -------------
    protected void processAutoDetectBindNode(String sql, Map<String, String> propertyNameTypeMap,
            Map<String, String> propertyNameOptionMap, Set<String> autoDetectedPropertyNameSet,
            BindVariableNode variableNode) {
        final String expression = variableNode.getExpression();
        final String testValue = variableNode.getTestValue();
        if (testValue == null) {
            return;
        }
        if (!isPmCommentStartsWithPmb(expression)) {
            return;
        }
        if (isPmCommentNestedProperty(expression) || isPmCommentMethodCall(expression)) {
            return;
        }
        final String propertyName = substringPmCommentPmbRear(expression);
        if (isRevervedProperty(propertyName)) {
            return;
        }
        final String typeName = derivePropertyTypeFromTestValue(testValue);
        propertyNameTypeMap.put(propertyName, typeName); // override if same one exists
        autoDetectedPropertyNameSet.add(propertyName);
        final String option = variableNode.getOptionDef();
        // add option if it exists
        // so it is enough to set an option to only one bind variable comment
        // if several bind variable comments for the same property exist
        final String derivedOption = derivePropertyOptionFromTestValue(testValue);
        if (Srl.is_NotNull_and_NotTrimmedEmpty(option)) {
            final String resolvedOption;
            if (Srl.is_NotNull_and_NotTrimmedEmpty(derivedOption)) {
                resolvedOption = option + "|" + derivedOption; // merged
            } else {
                resolvedOption = option;
            }
            propertyNameOptionMap.put(propertyName, resolvedOption);
        } else {
            if (Srl.is_NotNull_and_NotTrimmedEmpty(derivedOption)) {
                propertyNameOptionMap.put(propertyName, derivedOption);
            }
        }
    }

    protected String derivePropertyTypeFromTestValue(String testValue) {
        final String plainType = doDerivePropertyTypeFromTestValue(testValue);
        return resolvePackageNameExceptUtil(switchPlainTypeName(plainType));
    }

    protected String doDerivePropertyTypeFromTestValue(String testValue) { // test point
        if (testValue == null) {
            String msg = "The argument 'testValue' should be not null.";
            throw new IllegalArgumentException(msg);
        }
        final DfLanguageGrammar grammar = getLanguageGrammar();
        final String plainTypeName;
        if (Srl.startsWithIgnoreCase(testValue, "date '", "date'")) {
            plainTypeName = "Date";
        } else if (Srl.startsWithIgnoreCase(testValue, "timestamp '", "timestamp'")) {
            plainTypeName = "Timestamp";
        } else if (Srl.startsWithIgnoreCase(testValue, "time '", "time'")) {
            plainTypeName = "Time";
        } else {
            if (Srl.isQuotedSingle(testValue)) {
                final String unquoted = Srl.unquoteSingle(testValue);
                Timestamp timestamp = null;
                Time time = null;
                try {
                    timestamp = DfTypeUtil.toTimestamp(unquoted);
                } catch (ParseTimestampException ignored) {
                    try {
                        time = DfTypeUtil.toTime(unquoted);
                    } catch (ParseTimeException andIgnored) {
                    }
                }
                if (timestamp != null) {
                    final String timeParts = DfTypeUtil.toString(timestamp, "HH:mm:ss.SSS");
                    if (timeParts.equals("00:00:00.000")) {
                        plainTypeName = "Date";
                    } else {
                        plainTypeName = "Timestamp";
                    }
                } else if (time != null) {
                    plainTypeName = "Time";
                } else {
                    plainTypeName = "String";
                }
            } else if (Srl.isQuotedAnything(testValue, "(", ")")) {
                final String unquoted = Srl.unquoteAnything(testValue, "(", ")");
                final List<String> elementList = Srl.splitListTrimmed(unquoted, ",");
                if (elementList.size() > 0) {
                    final String firstElement = elementList.get(0);
                    // InScope for Date is unsupported at this analyzing
                    if (Srl.isQuotedSingle(firstElement)) {
                        plainTypeName = "List" + grammar.buildGenericOneClassHint("String");
                    } else {
                        final String elementType = doDeriveNonQuotedLiteralTypeFromTestValue(firstElement);
                        plainTypeName = "List" + grammar.buildGenericOneClassHint(elementType);
                    }
                } else {
                    plainTypeName = "List" + grammar.buildGenericOneClassHint("String");
                }
            } else {
                plainTypeName = doDeriveNonQuotedLiteralTypeFromTestValue(testValue);
            }
        }
        return plainTypeName;
    }

    protected String doDeriveNonQuotedLiteralTypeFromTestValue(String testValue) {
        final String plainTypeName;
        if (Srl.contains(testValue, ".")) {
            BigDecimal decimalValue = null;
            try {
                decimalValue = DfTypeUtil.toBigDecimal(testValue);
            } catch (NumberFormatException ignored) {
            }
            if (decimalValue != null) {
                plainTypeName = "BigDecimal";
            } else { // means unknown type
                plainTypeName = "String";
            }
        } else {
            Long longValue = null;
            try {
                longValue = DfTypeUtil.toLong(testValue);
            } catch (NumberFormatException ignored) {
            }
            if (longValue != null) {
                if (longValue > Long.valueOf(Integer.MAX_VALUE)) {
                    plainTypeName = "Long";
                } else {
                    plainTypeName = "Integer";
                }
            } else {
                if (testValue.equalsIgnoreCase("true") || testValue.equalsIgnoreCase("false")) {
                    plainTypeName = "Boolean";
                } else { // means unknown type
                    plainTypeName = "String";
                }
            }
        }
        return plainTypeName;
    }

    protected String switchPlainTypeName(String plainTypeName) {
        final DfLanguageTypeMapping typeMapping = getBasicProperties().getLanguageDependency().getLanguageTypeMapping();
        return typeMapping.switchParameterBeanTestValueType(plainTypeName);
    }

    protected String derivePropertyOptionFromTestValue(String testValue) { // test point
        if (Srl.isQuotedSingle(testValue)) {
            final String unquoted = Srl.unquoteSingle(testValue);
            final int count = Srl.count(unquoted, "%");
            if (Srl.endsWith(unquoted, "%") && count == 1) {
                return "likePrefix";
            } else if (Srl.startsWith(unquoted, "%") && count == 1) {
                return "likeSuffix";
            } else if (Srl.isQuotedAnything(unquoted, "%") && count == 2) {
                return "likeContain";
            } else if (count > 0) {
                return "like";
            }
        }
        return null;
    }

    // -----------------------------------------------------
    //                                            If Comment
    //                                            ----------
    protected void doProcessAutoDetectIfNode(String sql, Map<String, String> propertyNameTypeMap,
            Map<String, String> propertyNameOptionMap, IfNode ifNode) {
        final String ifCommentBooleanType = switchPlainTypeName("boolean");
        final String expression = ifNode.getExpression().trim(); // trim it just in case
        final List<String> elementList = Srl.splitList(expression, " ");
        for (int i = 0; i < elementList.size(); i++) {
            // boolean-not mark unused here so remove it at first
            final String element = substringBooleanNotRear(elementList.get(i));
            if (!isPmCommentStartsWithPmb(element)) {
                continue;
            }
            if (isPmCommentNestedProperty(element) || isPmCommentMethodCall(element)) {
                continue;
            }
            final String propertyName = substringPmCommentPmbRear(element);
            if (propertyNameTypeMap.containsKey(propertyName)) {
                // because of priority low (bind variable is given priority over if-comment)
                continue;
            }
            if (isRevervedProperty(propertyName)) {
                continue;
            }
            final int nextIndex = i + 1;
            if (elementList.size() <= nextIndex) { // last now
                propertyNameTypeMap.put(propertyName, ifCommentBooleanType);
                continue;
            }
            // next exists here
            final String nextElement = elementList.get(nextIndex);
            if (isIfCommentStatementConnector(nextElement)) { // e.g. '&&' or '||'
                propertyNameTypeMap.put(propertyName, ifCommentBooleanType);
                continue;
            }
            if (!isIfCommentStatementOperand(nextElement)) { // no way (wrong syntax)
                continue;
            }
            final int nextNextIndex = i + 2;
            if (elementList.size() <= nextNextIndex) { // no way (wrong syntax)
                continue;
            }
            // next next exists
            final String nextNextElement = elementList.get(nextNextIndex);
            if (isPmCommentStartsWithPmb(nextNextElement)) { // e.g. pmb.foo == pmb.bar
                continue;
            }
            // using-value statement here e.g. pmb.foo == 'foo'
            // condition value is treated as testValue to derive
            final String propertyType = derivePropertyTypeFromTestValue(nextNextElement);
            propertyNameTypeMap.put(propertyName, propertyType);
        }
    }

    protected void doProcessAlternateBooleanMethodIfNode(String sql, IfNode ifNode) {
        final String expression = ifNode.getExpression().trim(); // trim it just in case
        if (Srl.containsAny(expression, getIfCommentConnectors())
                || Srl.containsAny(expression, getIfCommentOperands())) {
            return; // unknown (type)
        }
        if (isPmCommentNestedProperty(expression) || !isPmCommentMethodCall(expression)) {
            return; // e.g. pmb.foo.bar
        }
        if (!isPmCommentStartsWithPmb(substringBooleanNotRear(expression))) {
            return; // e.g. #current.isFoo()
        }
        // pmb.foo() or !pmb.foo() here
        String methodName = substringPmCommentPmbRear(expression); // -> foo()
        methodName = Srl.substringLastFront(methodName, "()"); // -> foo
        _alternateBooleanMethodNameSet.add(methodName); // filter later
    }

    protected String[] getIfCommentConnectors() {
        return IfCommentEvaluator.getConnectors();
    }

    protected String[] getIfCommentOperands() {
        return IfCommentEvaluator.getOperands();
    }

    protected boolean isIfCommentStatementConnector(String target) {
        return IfCommentEvaluator.isConnector(target);
    }

    protected boolean isIfCommentStatementOperand(String target) {
        return IfCommentEvaluator.isOperand(target);
    }

    protected String substringBooleanNotRear(String expression) {
        return IfCommentEvaluator.substringBooleanNotRear(expression);
    }

    // -----------------------------------------------------
    //                                           For Comment
    //                                           -----------
    protected void doProcessAutoDetectForNode(String sql, Map<String, String> propertyNameTypeMap,
            Map<String, String> propertyNameOptionMap, ForNode forNode) {
        final String expression = forNode.getExpression();
        if (!isPmCommentStartsWithPmb(expression)) {
            return;
        }
        final String propertyName = substringPmCommentPmbRear(expression);
        if (propertyNameTypeMap.containsKey(propertyName)) {
            // because of priority low (bind variable is given priority over for-comment)
            return;
        }
        if (isRevervedProperty(propertyName)) {
            return;
        }
        final DetectedPropertyInfo detected = analyzeForNodeElementType(forNode, propertyName);
        if (detected != null) {
            final String propertyType = switchPlainTypeName(detected.getPropertyType());
            propertyNameTypeMap.put(propertyName, propertyType);
            final String propertyOption = detected.getPropertyOption();
            if (Srl.is_NotNull_and_NotTrimmedEmpty(propertyOption)) {
                propertyNameOptionMap.put(propertyName, propertyOption);
            }
        }
    }

    protected DetectedPropertyInfo analyzeForNodeElementType(Node node, String propertyName) {
        if (isPmCommentNestedProperty(propertyName) || isPmCommentMethodCall(propertyName)) {
            return null;
        }
        final DfLanguageGrammar grammar = getLanguageGrammar();
        DetectedPropertyInfo detected = null;
        for (int i = 0; i < node.getChildSize(); i++) {
            final Node childNode = node.getChild(i);
            if (childNode instanceof BindVariableNode) {
                final BindVariableNode bindNode = (BindVariableNode) childNode;
                final String expression = bindNode.getExpression();
                if (!isPmCommentEqualsCurrent(expression)) {
                    continue;
                }
                if (isPmCommentNestedProperty(expression) || isPmCommentMethodCall(expression)) {
                    continue;
                }
                // /*#current*/ here
                final String testValue = bindNode.getTestValue();
                if (testValue == null) {
                    continue;
                }
                final String propertyType = derivePropertyTypeFromTestValue(testValue);
                final String propertyOption = derivePropertyOptionFromTestValue(testValue);
                if (Srl.is_NotNull_and_NotTrimmedEmpty(propertyType)) {
                    detected = new DetectedPropertyInfo();
                    final String generic = grammar.buildGenericOneClassHint(propertyType);
                    detected.setPropertyType("List" + generic);
                    detected.setPropertyOption(propertyOption);
                }
            } else if (childNode instanceof ForNode) {
                final ForNode nestedNode = (ForNode) childNode;
                final String expression = nestedNode.getExpression();
                if (!isPmCommentStartsWithCurrent(expression)) {
                    continue;
                }
                // /*FOR #current.xxx*/ here
                final String nestedForPropName = substringPmCommentCurrentRear(expression);
                detected = analyzeForNodeElementType(nestedNode, nestedForPropName); // recursive call
                if (detected != null) {
                    final String generic = grammar.buildGenericOneClassHint(detected.getPropertyType());
                    detected.setPropertyType("List" + generic);
                }
            } else if (childNode instanceof ScopeNode) { // IF, Begin, First, ...
                detected = analyzeForNodeElementType(childNode, propertyName); // recursive call
            }
            if (detected != null) {
                break;
            }
        }
        if (detected == null) {
            return null;
        }
        return detected;
    }

    protected static class DetectedPropertyInfo {
        protected String _propertyType;
        protected String _propertyOption;

        public String getPropertyType() {
            return _propertyType;
        }

        public void setPropertyType(String propertyType) {
            this._propertyType = propertyType;
        }

        public String getPropertyOption() {
            return _propertyOption;
        }

        public void setPropertyOption(String propertyOption) {
            this._propertyOption = propertyOption;
        }
    }

    // -----------------------------------------------------
    //                                         Common Helper
    //                                         -------------
    protected boolean isRevervedProperty(String propertyName) {
        // properties for TypedParameterBean and SimplePagingBean and so on...
        return Srl.equalsIgnoreCase(propertyName, "OutsideSqlPath" // TypedParameterBean
                , "EntityType" // TypedSelectPmb
                , "ProcedureName", "EscapeStatement", "CalledBySelect" // ProcedurePmb
                , "IsEscapeStatement", "IsCalledBySelect" // ProcedurePmb (C#)
                , "FetchStartIndex", "FetchSize", "FetchPageNumber" // PagingBean
                , "PageStartIndex", "PageEndIndex" // PagingBean
                , "IsPaging" // PagingBean (C#)
                , "OrderByClause", "OrderByComponent" // OrderByBean
                , "SafetyMaxResultSize" // FetchBean
                , "ParameterMap" // MapParameterBean
        );
    }

    protected boolean isPmCommentStartsWithPmb(String expression) {
        return Srl.startsWith(expression, "pmb."); // e.g. "pmb.foo"
    }

    protected String substringPmCommentPmbRear(String expression) {
        return Srl.substringFirstRear(expression, "pmb.").trim();
    }

    protected boolean isPmCommentEqualsCurrent(String expression) {
        return Srl.equalsPlain(expression, ForNode.CURRENT_VARIABLE); // e.g. "#current"
    }

    protected boolean isPmCommentStartsWithCurrent(String expression) {
        return Srl.startsWith(expression, ForNode.CURRENT_VARIABLE + "."); // e.g. "#current.foo"
    }

    protected String substringPmCommentCurrentRear(String expression) {
        return Srl.substringFirstRear(expression, ForNode.CURRENT_VARIABLE + ".").trim();
    }

    protected boolean isPmCommentNestedProperty(String expression) {
        return Srl.count(expression, ".") > 1; // e.g. "pmb.foo.bar"
    }

    protected boolean isPmCommentMethodCall(String expression) {
        return Srl.endsWith(expression, "()"); // e.g. "pmb.isPaging()"
    }

    // ===================================================================================
    //                                                                   Assert Definition
    //                                                                   =================
    protected void assertDuplicateEntity(String entityName, File currentSqlFile) {
        final DfCustomizeEntityInfo entityInfo = _sql2entityMeta.getEntityInfoMap().get(entityName);
        if (entityInfo == null) {
            return;
        }
        final File sqlFile = entityInfo.getSqlFile();
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The same-name customize-entities were found.");
        br.addItem("CustomizeEntity");
        br.addElement(entityName);
        br.addItem("SQL Files");
        br.addElement(sqlFile);
        br.addElement(currentSqlFile);
        final String msg = br.buildExceptionMessage();
        throw new DfCustomizeEntityDuplicateException(msg);
    }

    protected void assertDuplicateParameterBean(String pmbName, File currentSqlFile) {
        final DfPmbMetaData metaData = _sql2entityMeta.getPmbMetaDataMap().get(pmbName);
        if (metaData == null) {
            return;
        }
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The same-name parameter-beans were found.");
        br.addItem("ParameterBean");
        br.addElement(pmbName);
        br.addItem("SQL Files");
        br.addElement(metaData.getOutsideSqlFile());
        br.addElement(currentSqlFile);
        final String msg = br.buildExceptionMessage();
        throw new DfParameterBeanDuplicateException(msg);
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
