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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.seasar.dbflute.cbean.chelper.HpFixedConditionQueryResolver;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public final class DfAdditionalForeignKeyProperties extends DfAbstractHelperProperties {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String KEY_LOCAL_TABLE_NAME = "localTableName";
    public static final String KEY_FOREIGN_TABLE_NAME = "foreignTableName";
    public static final String KEY_LOCAL_COLUMN_NAME = "localColumnName";
    public static final String KEY_FOREIGN_COLUMN_NAME = "foreignColumnName";
    public static final String KEY_FIXED_CONDITION = "fixedCondition";
    public static final String KEY_FIXED_SUFFIX = "fixedSuffix";
    public static final String KEY_FIXED_INLINE = "fixedInline";
    public static final String KEY_FIXED_REFERRER = "fixedReferrer";
    public static final String KEY_SUPPRESS_JOIN = "suppressJoin";
    public static final String KEY_SUPPRESS_SUBQUERY = "suppressSubQuery";
    public static final String KEY_COMMENT = "comment";
    public static final String KEY_DEPRECATED = "deprecated";

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Constructor.
     * @param prop Properties. (NotNull)
     */
    public DfAdditionalForeignKeyProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                             additionalForeignKeyMap
    //                                                             =======================
    public static final String KEY_additionalForeignKeyMap = "additionalForeignKeyMap";
    protected Map<String, Map<String, String>> _additionalForeignKeyMap;

    public Map<String, Map<String, String>> getAdditionalForeignKeyMap() {
        if (_additionalForeignKeyMap != null) {
            return _additionalForeignKeyMap;
        }
        _additionalForeignKeyMap = newLinkedHashMap();
        final String mapName = KEY_additionalForeignKeyMap;
        final String propKey = "torque." + mapName;
        final Map<String, Object> generatedMap = resolveSplit(mapName, mapProp(propKey, DEFAULT_EMPTY_MAP));
        for (Entry<String, Object> entry : generatedMap.entrySet()) { // FK Loop!
            final String foreignName = entry.getKey();
            final Object foreignDefObj = entry.getValue();
            checkForeignDefMap(foreignName, foreignDefObj);
            final Map<?, ?> fkDefMap = (Map<?, ?>) foreignDefObj;
            registerForeignKeyElement(foreignName, fkDefMap, null);
        }
        return _additionalForeignKeyMap;
    }

    protected void checkForeignDefMap(final String foreignName, final Object foreignDefObj) {
        if (!(foreignDefObj instanceof Map<?, ?>)) {
            String msg = "The value type should be Map:";
            msg = msg + " tableName=" + foreignName + " property=" + KEY_additionalForeignKeyMap;
            msg = msg + " actualType=" + foreignDefObj.getClass() + " actualValue=" + foreignDefObj;
            throw new IllegalStateException(msg);
        }
    }

    protected void registerForeignKeyElement(String foreignName, Map<?, ?> fkDefMap, String splitKeyword) {
        final Map<String, String> genericMap = prepareFKDefGenericMap(foreignName, fkDefMap, fkDefMap.keySet());
        _additionalForeignKeyMap.put(foreignName, genericMap);
    }

    protected Map<String, String> prepareFKDefGenericMap(Object foreignName, Map<?, ?> fkDefMap, Set<?> secondKeySet) {
        final Map<String, String> genericFKDefMap = newLinkedHashMap();
        for (Object componentName : secondKeySet) { // FK component loop!
            final Object secondValue = fkDefMap.get(componentName);
            if (secondValue == null) {
                continue;
            }
            if (!(componentName instanceof String)) {
                String msg = "The key type should be String: foreignName=" + foreignName;
                msg = msg + " property=AdditionalForeignKey";
                msg = msg + " actualType=" + componentName.getClass() + " actualKey=" + componentName;
                throw new IllegalStateException(msg);
            }
            if (!(secondValue instanceof String)) {
                String msg = "The value type should be String: foreignName=" + foreignName;
                msg = msg + " property=AdditionalForeignKey";
                msg = msg + " actualType=" + secondValue.getClass() + " actualValue=" + secondValue;
                throw new IllegalStateException(msg);
            }
            genericFKDefMap.put((String) componentName, (String) secondValue);
        }
        return genericFKDefMap;
    }

    // ===================================================================================
    //                                                                      Finding Helper
    //                                                                      ==============
    public String findLocalTableName(String foreignKeyName) {
        return doFindAttributeValue(foreignKeyName, KEY_LOCAL_TABLE_NAME);
    }

    public String findForeignTableName(String foreignKeyName) {
        return doFindAttributeValue(foreignKeyName, KEY_FOREIGN_TABLE_NAME);
    }

    protected String findLocalColumnName(String foreignKeyName) {
        return doFindAttributeValue(foreignKeyName, KEY_LOCAL_COLUMN_NAME);
    }

    protected String findForeignColumnName(String foreignKeyName) {
        return doFindAttributeValue(foreignKeyName, KEY_FOREIGN_COLUMN_NAME);
    }

    public String findFixedCondition(String foreignKeyName) {
        String fixedCondition = doFindAttributeValue(foreignKeyName, KEY_FIXED_CONDITION);
        if (fixedCondition != null && fixedCondition.trim().length() > 0) {
            fixedCondition = fixedCondition.trim(); // already trimmed but just in case

            // adjust a little about camel case
            final String foreignAliasMark = HpFixedConditionQueryResolver.FOREIGN_ALIAS_MARK;
            final String localAliasMark = HpFixedConditionQueryResolver.LOCAL_ALIAS_MARK;
            fixedCondition = Srl.replace(fixedCondition, "$$ALIAS$$", "$$alias$$");
            fixedCondition = Srl.replace(fixedCondition, "$$ForeignAlias$$", foreignAliasMark);
            fixedCondition = Srl.replace(fixedCondition, "$$LocalAlias$$", localAliasMark);

            // adjust line separator
            fixedCondition = Srl.replace(fixedCondition, "\r\n", "\n"); // remove CR
            fixedCondition = Srl.replace(fixedCondition, "\n", "\\n"); // LF to "LF on Java"

            // adjust formatting
            fixedCondition = adjustFixedConditionFormat(fixedCondition);
        }
        return fixedCondition;
    }

    protected String adjustFixedConditionFormat(String fixedCondition) { // called in setter
        if (fixedCondition == null) {
            return null;
        }
        final String lineMark = "\\n"; // already replaced to line mark
        if (!fixedCondition.contains(lineMark)) { // no need to adjust
            return fixedCondition;
        }
        if (fixedCondition.contains(HpFixedConditionQueryResolver.SQ_BEGIN_MARK)) { // no need to adjust
            return fixedCondition;
        }
        if (mightBeSubQueryOrOrScope(fixedCondition)) {
            return fixedCondition;
        }
        final List<String> splitList = Srl.splitList(fixedCondition, lineMark); // not trim
        final StringBuilder sb = new StringBuilder();
        final String andMark = "and ";
        final int fitSize = 5;
        int index = 0;
        for (String element : splitList) {
            if (index > 0) {
                sb.append(lineMark);
            }
            if (isFixedConditionIndentFittingTarget(andMark, fitSize, element)) {
                sb.append(Srl.indent(fitSize)).append(Srl.ltrim(element));
            } else {
                sb.append(element);
            }
            ++index;
        }
        return sb.toString();
    }

    protected boolean mightBeSubQueryOrOrScope(String fixedCondition) {
        final String clause = Srl.removeScope(Srl.removeBlockComment(fixedCondition), "$$over(", ")$$");
        return clause.contains("(");
    }

    protected boolean isFixedConditionIndentFittingTarget(String andMark, int fitSize, String element) {
        // e.g. "and ...", "    and ...", "        and ..."
        return element.trim().startsWith(andMark);
    }

    public String findFixedSuffix(String foreignKeyName) {
        return doFindAttributeValue(foreignKeyName, KEY_FIXED_SUFFIX);
    }

    public String findFixedInline(String foreignKeyName) {
        return doFindAttributeValue(foreignKeyName, KEY_FIXED_INLINE);
    }

    public String findFixedReferrer(String foreignKeyName) {
        return doFindAttributeValue(foreignKeyName, KEY_FIXED_REFERRER);
    }

    public String findComment(String foreignKeyName) {
        return doFindAttributeValue(foreignKeyName, KEY_COMMENT);
    }

    public String findSuppressJoin(String foreignKeyName) {
        return doFindAttributeValue(foreignKeyName, KEY_SUPPRESS_JOIN);
    }

    public String findSuppressSubQuery(String foreignKeyName) {
        return doFindAttributeValue(foreignKeyName, KEY_SUPPRESS_SUBQUERY);
    }

    public String findDeprecated(String foreignKeyName) {
        return doFindAttributeValue(foreignKeyName, KEY_DEPRECATED);
    }

    protected String doFindAttributeValue(String foreignKeyName, String optionKey) {
        final Map<String, String> attributeMap = getAdditionalForeignKeyMap().get(foreignKeyName);
        if (attributeMap == null) {
            String msg = "Unknown name of additional foreign key: " + foreignKeyName + " option=" + optionKey;
            throw new IllegalStateException(msg);
        }
        return attributeMap.get(optionKey);
    }

    public boolean isSuppressImplicitReverseFK(String foreignKeyName) { // closet (for emergency)
        final Map<String, String> componentMap = getAdditionalForeignKeyMap().get(foreignKeyName);
        String value = componentMap.get("isSuppressImplicitReverseFK");
        return value != null && value.equalsIgnoreCase("true");
    }

    public List<String> findLocalColumnNameList(String foreignKeyName) {
        final String property = findLocalColumnName(foreignKeyName);
        if (property == null || property.trim().length() == 0) {
            return null;
        }
        final List<String> localColumnNameList = new ArrayList<String>();
        final StringTokenizer st = new StringTokenizer(property, "/");
        while (st.hasMoreElements()) {
            localColumnNameList.add(st.nextToken());
        }
        return localColumnNameList;
    }

    public List<String> findForeignColumnNameList(String foreignKeyName) {
        final String property = findForeignColumnName(foreignKeyName);
        if (property == null || property.trim().length() == 0) {
            return null;
        }
        final List<String> foreignColumnNameList = new ArrayList<String>();
        final StringTokenizer st = new StringTokenizer(property, "/");
        while (st.hasMoreElements()) {
            foreignColumnNameList.add(st.nextToken());
        }
        return foreignColumnNameList;
    }
}