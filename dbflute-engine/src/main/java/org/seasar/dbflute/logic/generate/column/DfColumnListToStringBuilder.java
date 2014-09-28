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
package org.seasar.dbflute.logic.generate.column;

import java.util.Iterator;
import java.util.List;

import org.apache.torque.engine.database.model.Column;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.logic.generate.language.DfLanguageDependency;
import org.seasar.dbflute.logic.generate.language.grammar.DfLanguageGrammar;
import org.seasar.dbflute.logic.generate.language.implstyle.DfLanguageImplStyle;
import org.seasar.dbflute.properties.DfBasicProperties;

/**
 * @author jflute
 */
public class DfColumnListToStringBuilder {

    public static String getColumnArgsString(List<Column> columnList, DfLanguageGrammar grammar) {
        validateColumnList(columnList);
        final StringBuilder sb = new StringBuilder();
        for (Column column : columnList) {
            final String javaNative;
            if (column.isForceClassificationSetting()) {
                final DfBasicProperties prop = getBasicProperties();
                final String projectPrefix = prop.getProjectPrefix();
                final String classificationName = column.getClassificationName();
                javaNative = projectPrefix + "CDef." + classificationName;
            } else {
                javaNative = column.getJavaNative();
            }
            final String uncapitalisedJavaName = column.getUncapitalisedJavaName();
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(grammar.buildVariableSimpleDefinition(javaNative, uncapitalisedJavaName));
        }
        return sb.toString();
    }

    public static String getColumnArgsJavaDocString(List<Column> columnList, String title, String ln) {
        validateColumnList(columnList);
        final StringBuilder sb = new StringBuilder();
        for (Column column : columnList) {
            final String uncapitalisedJavaName = column.getUncapitalisedJavaName();
            if (sb.length() > 0) {
                sb.append(ln).append("     * ");
            }
            sb.append("@param ").append(uncapitalisedJavaName).append(" ");
            sb.append(column.getAliasExpression()).append(": ");
            sb.append(column.getColumnDefinitionLineDisp()).append(". (NotNull)");
        }
        return sb.toString();
    }

    public static String getColumnArgsAssertString(List<Column> columnList) {
        return doGetColumnArgsAssertString(columnList, false);
    }

    public static String getColumnArgsAssertStringCSharp(List<Column> columnList) {
        return doGetColumnArgsAssertString(columnList, true);
    }

    private static String doGetColumnArgsAssertString(List<Column> columnList, boolean initCap) {
        validateColumnList(columnList);

        final StringBuilder sb = new StringBuilder();
        for (Iterator<Column> ite = columnList.iterator(); ite.hasNext();) {
            final Column pk = (Column) ite.next();
            final String uncapitalisedJavaName = pk.getUncapitalisedJavaName();
            sb.append(initCap ? "A" : "a").append("ssertObjectNotNull(\"");
            sb.append(uncapitalisedJavaName).append("\", ");
            sb.append(uncapitalisedJavaName).append(");");
        }
        return sb.toString();
    }

    public static String getColumnArgsSetupString(String beanName, List<Column> columnList) {
        validateColumnList(columnList);
        final boolean hasPrefix = beanName != null;
        final String beanPrefix = (hasPrefix ? beanName + "." : "");
        final DfLanguageDependency lang = getBasicProperties().getLanguageDependency();
        final DfLanguageImplStyle implStyle = lang.getLanguageImplStyle();
        String result = "";
        for (Iterator<Column> ite = columnList.iterator(); ite.hasNext();) {
            final Column column = (Column) ite.next();
            final String javaName = column.getJavaName();
            final String variable = column.getUncapitalisedJavaName();
            final String cls = column.getClassificationName();
            final String basic;
            if (column.isForceClassificationSetting()) {
                basic = "set" + javaName + "As" + cls + "(" + variable + ")";
            } else {
                basic = "set" + javaName + "(" + variable + ")";
            }
            final String adjusted = implStyle.adjustEntitySetMethodCall(basic, !hasPrefix);
            final String setter = beanPrefix + adjusted + ";";
            if ("".equals(result)) {
                result = setter;
            } else {
                result = result + setter;
            }
        }
        return result;
    }

    public static String getColumnArgsSetupPropertyString(String beanName, List<Column> columnList) {
        validateColumnList(columnList);
        final boolean hasPrefix = beanName != null;
        final String beanPrefix = (hasPrefix ? beanName + "." : "");
        final DfLanguageDependency lang = getBasicProperties().getLanguageDependency();
        final DfLanguageImplStyle implStyle = lang.getLanguageImplStyle();
        String result = "";
        for (Iterator<Column> ite = columnList.iterator(); ite.hasNext();) {
            final Column column = (Column) ite.next();
            final String javaName = column.getJavaName();
            final String variable = column.getUncapitalisedJavaName();
            final String cls = column.getClassificationName();
            final String basic;
            if (column.isForceClassificationSetting()) {
                basic = "set" + javaName + "As" + cls + "(" + variable + ")";
            } else {
                basic = "set" + javaName + "(" + variable + ")";
            }
            final String adjusted = implStyle.adjustEntitySetPropertyCall(basic, !hasPrefix);
            final String setter = beanPrefix + adjusted + ";";
            if ("".equals(result)) {
                result = setter;
            } else {
                result = result + setter;
            }
        }
        return result;
    }

    public static String getColumnArgsSetupStringCSharp(String beanName, List<Column> columnList) {
        validateColumnList(columnList);
        final String beanPrefix = (beanName != null ? beanName + "." : "");

        String result = "";
        for (Iterator<Column> ite = columnList.iterator(); ite.hasNext();) {
            final Column column = (Column) ite.next();
            final String javaName = column.getJavaName();
            final String variable = column.getUncapitalisedJavaName();
            final String setter = beanPrefix + javaName + " = " + variable + ";";
            if ("".equals(result)) {
                result = setter;
            } else {
                result = result + setter;
            }
        }
        return result;
    }

    public static String getColumnArgsConditionSetupString(List<Column> columnList) {
        return doGetColumnArgsConditionSetupString(columnList);
    }

    private static String doGetColumnArgsConditionSetupString(List<Column> columnList) {
        validateColumnList(columnList);
        final DfLanguageDependency lang = getBasicProperties().getLanguageDependency();
        final DfLanguageImplStyle implStyle = lang.getLanguageImplStyle();
        final String query = implStyle.adjustConditionBeanLocalCQCall("cb");
        final StringBuilder sb = new StringBuilder();
        for (Column column : columnList) {
            final String javaName = column.getJavaName();
            final String variable = column.getUncapitalisedJavaName();
            final String setter;
            if (column.isForceClassificationSetting()) {
                final String cls = column.getClassificationName();
                final String basic = "set" + javaName + "_Equal_As" + cls + "(" + variable + ")";
                final String adjusted = implStyle.adjustConditionQuerySetMethodCall(basic);
                setter = query + "." + adjusted + ";";
            } else {
                final String basic = "set" + javaName + "_Equal(" + variable + ")";
                final String adjusted = implStyle.adjustConditionQuerySetMethodCall(basic);
                setter = query + "." + adjusted + ";";
            }
            sb.append(setter);
        }
        return sb.toString();
    }

    public static String getColumnNameCommaString(List<Column> columnList) {
        validateColumnList(columnList);
        String result = "";
        for (Column column : columnList) {
            final String name = column.getName();
            if ("".equals(result)) {
                result = name;
            } else {
                result = result + ", " + name;
            }
        }
        return result;
    }

    public static String getColumnJavaNameCommaString(List<Column> columnList) {
        validateColumnList(columnList);
        String result = "";
        for (Column column : columnList) {
            final String name = column.getJavaName();
            if ("".equals(result)) {
                result = name;
            } else {
                result = result + ", " + name;
            }
        }
        return result;
    }

    public static String getColumnUncapitalisedJavaNameCommaString(List<Column> columnList) {
        validateColumnList(columnList);
        String result = "";
        for (Column column : columnList) {
            final String name = column.getUncapitalisedJavaName();
            if ("".equals(result)) {
                result = name;
            } else {
                result = result + ", " + name;
            }
        }
        return result;
    }

    public static String getColumnGetterCommaString(List<Column> columnList) {
        validateColumnList(columnList);
        String result = "";
        for (Column column : columnList) {
            final String javaName = column.getJavaName();
            final String getterString = "get" + javaName + "()";
            if ("".equals(result)) {
                result = getterString;
            } else {
                result = result + ", " + getterString;
            }
        }
        return result;
    }

    public static String getColumnOrderByString(List<Column> columnList, String sortString) {
        validateColumnList(columnList);
        final StringBuilder sb = new StringBuilder();
        for (Column column : columnList) {
            final String name = column.getName();
            if ("".equals(sb.toString())) {
                sb.append(name).append(" ").append(sortString);
            } else {
                sb.append(", ").append(name).append(" ").append(sortString);
            }
        }
        return sb.toString();
    }

    public static String getColumnDispValueString(List<Column> columnList, String getterPrefix) {
        validateColumnList(columnList);
        String result = "";
        for (Column column : columnList) {
            final String javaName = column.getJavaName();
            final String getterString = getterPrefix + javaName + "()";
            if ("".equals(result)) {
                result = getterString;
            } else {
                result = result + " + \"-\" + " + getterString;
            }
        }
        return result;
    }

    protected static DfBasicProperties getBasicProperties() {
        return DfBuildProperties.getInstance().getBasicProperties();
    }

    protected static void validateColumnList(List<Column> columnList) {
        if (columnList == null) {
            String msg = "The columnList is null.";
            throw new IllegalStateException(msg);
        }
    }
}