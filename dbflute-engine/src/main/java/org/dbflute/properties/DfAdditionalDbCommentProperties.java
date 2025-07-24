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

import java.util.Map;
import java.util.Properties;

import org.dbflute.helper.StringKeyMap;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.2.9 (2024/10/10 Thursday at nakameguro)
 */
public final class DfAdditionalDbCommentProperties extends DfAbstractDBFluteProperties {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String KEY_SCHEMA = "$$schema$$";
    public static final String KEY_ALL = "$$ALL$$";

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * @param prop Properties. (NotNull)
     */
    public DfAdditionalDbCommentProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                              additionalDbCommentMap
    //                                                              ======================
    // # map: {
    // #     $$schema$$ = map:{
    // #         ; alias = [schema alias]
    // #         ; description = [schema description]
    // #     }
    // #     $$ALL$$ = map:{
    // #         ; columnMap = map:{
    // #             [column-name] = map:{
    // #                 alias = [column alias]
    // #                 description = [column description]
    // #             }
    // #         }
    // #     }
    // #     [table-name] = map:{
    // #         ; alias = [table alias]
    // #         ; description = [table description]
    // #         ; columnMap = map:{
    // #             [column-name] = map:{
    // #                 alias = [column alias]
    // #                 description = [column description]
    // #             }
    // #         }
    // #     }
    // # }
    public static final String KEY_additionalDbCommentMap = "additionalDbCommentMap";
    protected Map<String, Object> _additionalDbCommentMap; // lazy loaded

    public Map<String, Object> getAdditionalDbCommentMap() {
        if (_additionalDbCommentMap == null) {
            final Map<String, Object> resolvedMap = resolveSplit(KEY_additionalDbCommentMap, preparePlainMap());
            final StringKeyMap<Object> flexibleMap = StringKeyMap.createAsFlexibleOrdered(); // root has table names
            flexibleMap.putAll(resolvedMap);
            _additionalDbCommentMap = flexibleMap;
        }
        return _additionalDbCommentMap;
    }

    protected Map<String, Object> preparePlainMap() {
        return mapProp("torque." + KEY_additionalDbCommentMap, DEFAULT_EMPTY_MAP);
    }

    // ===================================================================================
    //                                                                        Handle Plain
    //                                                                        ============
    // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
    // basically dfprop alias and description are prior
    // because dfprop is more easier modifiable than DB's comment
    // so dfprop is not supplement but is patch
    // _/_/_/_/_/_/_/_/_/_/
    // -----------------------------------------------------
    //                                                 Alias
    //                                                 -----
    public String chooseTablePlainAlias(String tableName, String plainAlias) { // null allowed
        return doChoosePlainAlias(findTableAlias(tableName), plainAlias);
    }

    public String chooseColumnPlainAlias(String tableName, String columnName, String plainAlias) { // null allowed
        return doChoosePlainAlias(findColumnAlias(tableName, columnName), plainAlias);
    }

    protected String doChoosePlainAlias(String dfpropAlias, String plainAlias) {
        if (Srl.is_Null_or_TrimmedEmpty(dfpropAlias)) { // no dfprop
            return plainAlias; // null allowed
        }
        if (Srl.is_Null_or_TrimmedEmpty(plainAlias)) { // dfprop only
            return dfpropAlias;
        } else { // both
            return dfpropAlias; // prior as default
        }
    }

    public boolean hasTableDfpropAlias(String tableName) {
        return Srl.is_NotNull_and_NotTrimmedEmpty(findTableAlias(tableName));
    }

    public boolean hasColumnDfpropAlias(String tableName, String columnName) {
        return Srl.is_NotNull_and_NotTrimmedEmpty(findColumnAlias(tableName, columnName));
    }

    // -----------------------------------------------------
    //                                           Description
    //                                           -----------
    public String unifyTablePlainDescription(String tableName, String plainDescription) { // null allowed
        return doUnifyPlainDescription(findTableDescription(tableName), plainDescription);
    }

    public String unifyColumnPlainDescription(String tableName, String columnName, String plainDescription) { // null allowed
        return doUnifyPlainDescription(findColumnDescription(tableName, columnName), plainDescription);
    }

    protected String doUnifyPlainDescription(String dfpropDescription, String plainDescription) {
        if (Srl.is_Null_or_TrimmedEmpty(dfpropDescription)) { // no dfprop
            return plainDescription; // null allowed
        }
        return connectPlainDescription(dfpropDescription, plainDescription);
    }

    protected String connectPlainDescription(String dfpropDescription, String plainDescription) { // dfprop exists here
        if (Srl.is_NotNull_and_NotTrimmedEmpty(plainDescription)) { // both
            final String delimiter = getPlainCommentDelimiter();
            return dfpropDescription + delimiter + plainDescription; // dfprop + DB
        } else { // dfprop only
            return dfpropDescription;
        }
    }

    protected String getPlainCommentDelimiter() {
        return ln();
    }

    // ===================================================================================
    //                                                                       Direct Finder
    //                                                                       =============
    // -----------------------------------------------------
    //                                                 Alias
    //                                                 -----
    @SuppressWarnings("unchecked")
    public String findSchemaAlias() { // null allowed: null means not defined 
        final Map<String, Object> componentMap = (Map<String, Object>) getAdditionalDbCommentMap().get(KEY_SCHEMA);
        if (componentMap == null) {
            return null;
        }
        return (String) componentMap.get("alias");
    }

    @SuppressWarnings("unchecked")
    public String findTableAlias(String tableName) { // null allowed: null means not defined 
        final Map<String, Object> componentMap = (Map<String, Object>) getAdditionalDbCommentMap().get(tableName);
        if (componentMap == null) {
            return null;
        }
        final String additionalAlias = (String) componentMap.get("alias");
        return additionalAlias;
    }

    public String findColumnAlias(String tableName, String columnName) { // null allowed: null means not defined
        final String columnAlias = searchColumnAlias(tableName, columnName);
        if (columnAlias != null) {
            return columnAlias;
        } else {
            return searchColumnAliasFromAll(columnName);
        }
    }

    protected String searchColumnAlias(String tableName, String columnName) {
        final Map<String, Map<String, String>> columnMap = searchColumnMap(tableName);
        if (columnMap.isEmpty()) {
            return null;
        }
        final Map<String, String> elementMap = columnMap.get(columnName);
        if (elementMap == null) {
            return null;
        }
        return elementMap.get("alias");
    }

    protected String searchColumnAliasFromAll(String columnName) {
        final Map<String, Map<String, String>> allMap = searchColumnMap(KEY_ALL);
        final Map<String, String> allElement = allMap.get(columnName);
        if (allElement != null) {
            return allElement.get("alias");
        }
        return null;
    }

    // -----------------------------------------------------
    //                                           Description
    //                                           -----------
    @SuppressWarnings("unchecked")
    public String findSchemaDescription() { // null allowed: null means not defined 
        final Map<String, Object> componentMap = (Map<String, Object>) getAdditionalDbCommentMap().get(KEY_SCHEMA);
        if (componentMap == null) {
            return null;
        }
        return (String) componentMap.get("description");
    }

    @SuppressWarnings("unchecked")
    public String findTableDescription(String tableName) { // null allowed: null means not defined 
        final Map<String, Object> componentMap = (Map<String, Object>) getAdditionalDbCommentMap().get(tableName);
        if (componentMap == null) {
            return null;
        }
        return (String) componentMap.get("description");
    }

    public String findColumnDescription(String tableName, String columnName) { // null allowed: null means not defined
        final String columnDescription = searchColumnDescription(tableName, columnName);
        if (columnDescription != null) {
            return columnDescription;
        } else {
            return searchColumnDescriptionFromAll(columnName);
        }
    }

    protected String searchColumnDescription(String tableName, String columnName) {
        final Map<String, Map<String, String>> columnMap = searchColumnMap(tableName);
        if (columnMap.isEmpty()) {
            return null;
        }
        final Map<String, String> elementMap = columnMap.get(columnName);
        if (elementMap == null) {
            return null;
        }
        return elementMap.get("description");
    }

    protected String searchColumnDescriptionFromAll(String columnName) {
        final Map<String, Map<String, String>> allMap = searchColumnMap(KEY_ALL);
        final Map<String, String> allElement = allMap.get(columnName);
        if (allElement != null) {
            return allElement.get("description");
        }
        return null;
    }

    // -----------------------------------------------------
    //                                            Column Map
    //                                            ----------
    @SuppressWarnings("unchecked")
    protected Map<String, Map<String, String>> searchColumnMap(String tableName) { // not null, empty allowed when undefined
        final Map<String, Object> componentMap = (Map<String, Object>) getAdditionalDbCommentMap().get(tableName);
        if (componentMap == null) {
            return DfCollectionUtil.emptyMap();
        }
        final Map<String, Map<String, String>> columnMap = (Map<String, Map<String, String>>) componentMap.get("columnMap");
        if (columnMap == null) {
            return DfCollectionUtil.emptyMap();
        }
        final StringKeyMap<Map<String, String>> flexibleMap = StringKeyMap.createAsFlexibleOrdered();
        flexibleMap.putAll(columnMap);
        return flexibleMap;
    }
}