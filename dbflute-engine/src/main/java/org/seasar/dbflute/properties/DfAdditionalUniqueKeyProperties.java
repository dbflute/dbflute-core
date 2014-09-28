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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * @author jflute
 * @since 0.9.5.3 (2009/08/01 Saturday)
 */
public final class DfAdditionalUniqueKeyProperties extends DfAbstractHelperProperties {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Constructor.
     * @param prop Properties. (NotNull)
     */
    public DfAdditionalUniqueKeyProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                             additionalUniqueKeyMap
    //                                                             =======================
    public static final String KEY_additionalUniqueKeyMap = "additionalUniqueKeyMap";
    protected Map<String, Map<String, String>> _additionalUniqueKeyMap;

    public Map<String, Map<String, String>> getAdditionalUniqueKeyMap() {
        if (_additionalUniqueKeyMap == null) {
            _additionalUniqueKeyMap = new LinkedHashMap<String, Map<String, String>>();
            final Map<String, Object> generatedMap = mapProp("torque." + KEY_additionalUniqueKeyMap, DEFAULT_EMPTY_MAP);
            final Set<String> fisrtKeySet = generatedMap.keySet();
            for (Object primaryName : fisrtKeySet) {// PK Loop!
                final Object firstValue = generatedMap.get(primaryName);
                if (!(firstValue instanceof Map<?, ?>)) {
                    String msg = "The value type should be Map: tableName=" + primaryName + " property=CustomizeDao";
                    msg = msg + " actualType=" + firstValue.getClass() + " actualValue=" + firstValue;
                    throw new IllegalStateException(msg);
                }
                final Map<?, ?> foreignDefinitionMap = (Map<?, ?>) firstValue;
                final Set<?> secondKeySet = foreignDefinitionMap.keySet();
                final Map<String, String> genericForeignDefinitiontMap = new LinkedHashMap<String, String>();
                for (Object componentName : secondKeySet) { // PK Component Loop!
                    final Object secondValue = foreignDefinitionMap.get(componentName);
                    if (secondValue == null) {
                        continue;
                    }
                    if (!(componentName instanceof String)) {
                        String msg = "The key type should be String: foreignName=" + primaryName;
                        msg = msg + " property=additionalUniqueKey";
                        msg = msg + " actualType=" + componentName.getClass() + " actualKey=" + componentName;
                        throw new IllegalStateException(msg);
                    }
                    if (!(secondValue instanceof String)) {
                        String msg = "The value type should be String: foreignName=" + primaryName;
                        msg = msg + " property=additionalUniqueKey";
                        msg = msg + " actualType=" + secondValue.getClass() + " actualValue=" + secondValue;
                        throw new IllegalStateException(msg);
                    }
                    genericForeignDefinitiontMap.put((String) componentName, (String) secondValue);
                }
                _additionalUniqueKeyMap.put((String) primaryName, genericForeignDefinitiontMap);
            }
        }
        return _additionalUniqueKeyMap;
    }

    // ===================================================================================
    //                                                                      Finding Helper
    //                                                                      ==============
    public String findTableName(String uniqueName) {
        final Map<String, String> componentMap = getAdditionalUniqueKeyMap().get(uniqueName);
        return componentMap.get("tableName");
    }

    protected String findColumnName(String uniqueName) {
        final Map<String, String> componentMap = getAdditionalUniqueKeyMap().get(uniqueName);
        return componentMap.get("columnName");
    }

    public List<String> findColumnNameList(String uniqueName) {
        final String property = findColumnName(uniqueName);
        if (property == null || property.trim().length() == 0) {
            return null;
        }
        final List<String> columnNameList = new ArrayList<String>();
        final StringTokenizer st = new StringTokenizer(property, "/");
        while (st.hasMoreElements()) {
            columnNameList.add(st.nextToken());
        }
        return columnNameList;
    }
}