/*
 * Copyright 2014-2022 the original author or authors.
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

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

import org.dbflute.DfBuildProperties;
import org.dbflute.logic.generate.language.DfLanguageDependency;
import org.dbflute.logic.generate.language.grammar.DfLanguageGrammar;
import org.dbflute.logic.generate.language.pkgstyle.DfLanguagePropertyPackageResolver;
import org.dbflute.logic.generate.language.typemapping.DfLanguageTypeMapping;
import org.dbflute.properties.DfBasicProperties;
import org.dbflute.twowaysql.node.ForNode;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.DfTypeUtil.ParseTimeException;
import org.dbflute.util.DfTypeUtil.ParseTimestampException;
import org.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfParameterAutoDetectAssist {

    // ===================================================================================
    //                                                                       Property Type
    //                                                                       =============
    public String derivePropertyTypeFromTestValue(String testValue) {
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
                    } catch (ParseTimeException andIgnored) {}
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
            } catch (NumberFormatException ignored) {}
            if (decimalValue != null) {
                plainTypeName = "BigDecimal";
            } else { // means unknown type
                plainTypeName = "String";
            }
        } else {
            Long longValue = null;
            try {
                longValue = DfTypeUtil.toLong(testValue);
            } catch (NumberFormatException ignored) {}
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

    protected String resolvePackageNameExceptUtil(String typeName) {
        final DfLanguageDependency lang = getBasicProperties().getLanguageDependency();
        final DfLanguagePropertyPackageResolver resolver = lang.getLanguagePropertyPackageResolver();
        return resolver.resolvePackageNameExceptUtil(typeName);
    }

    public String switchPlainTypeName(String plainTypeName) {
        final DfLanguageTypeMapping typeMapping = getBasicProperties().getLanguageDependency().getLanguageTypeMapping();
        return typeMapping.switchParameterBeanTestValueType(plainTypeName);
    }

    public boolean isRevervedProperty(String propertyName) {
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

    // ===================================================================================
    //                                                                     Property Option
    //                                                                     ===============
    public String derivePropertyOptionFromTestValue(String testValue) { // test point
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
    
    // ===================================================================================
    //                                                                    PmComment Helper
    //                                                                    ================
    public boolean isPmCommentStartsWithPmb(String expression) {
        return Srl.startsWith(expression, "pmb."); // e.g. "pmb.foo"
    }

    public String substringPmCommentPmbRear(String expression) {
        return Srl.substringFirstRear(expression, "pmb.").trim();
    }

    public boolean isPmCommentEqualsCurrent(String expression) {
        return Srl.equalsPlain(expression, ForNode.CURRENT_VARIABLE); // e.g. "#current"
    }

    public boolean isPmCommentStartsWithCurrent(String expression) {
        return Srl.startsWith(expression, ForNode.CURRENT_VARIABLE + "."); // e.g. "#current.foo"
    }

    public String substringPmCommentCurrentRear(String expression) {
        return Srl.substringFirstRear(expression, ForNode.CURRENT_VARIABLE + ".").trim();
    }

    public boolean isPmCommentNestedProperty(String expression) {
        return Srl.count(expression, ".") > 1; // e.g. "pmb.foo.bar"
    }

    public boolean isPmCommentMethodCall(String expression) {
        return Srl.endsWith(expression, "()"); // e.g. "pmb.isPaging()"
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfBasicProperties getBasicProperties() {
        return getProperties().getBasicProperties();
    }

    protected DfLanguageGrammar getLanguageGrammar() {
        return getBasicProperties().getLanguageDependency().getLanguageGrammar();
    }
}
