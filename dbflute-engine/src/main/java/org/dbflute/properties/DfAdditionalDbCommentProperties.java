/*
 * Copyright 2014-2024 the original author or authors.
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
    protected Map<String, Object> _additionalDbCommentMap;

    public Map<String, Object> getAdditionalDbCommentMap() {
        if (_additionalDbCommentMap == null) {
            final Map<String, Object> map = mapProp("torque." + KEY_additionalDbCommentMap, DEFAULT_EMPTY_MAP);
            _additionalDbCommentMap = newLinkedHashMap();
            _additionalDbCommentMap.putAll(map);
        }
        return _additionalDbCommentMap;
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
        if (plainDescription != null) { // both
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
    //                                                                      Finding Helper
    //                                                                      ==============
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
        return (String) componentMap.get("alias");
    }

    public String findColumnAlias(String tableName, String columnName) { // null allowed: null means not defined
        final Map<String, Map<String, String>> columnMap = findColumnMap(tableName);
        if (columnMap == null) {
            return null;
        }
        final Map<String, String> elementMap = columnMap.get(columnName);
        if (elementMap == null) {
            return null;
        }
        return elementMap.get("alias");
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
        final Map<String, Map<String, String>> columnMap = findColumnMap(tableName);
        if (columnMap == null) {
            return null;
        }
        final Map<String, String> elementMap = columnMap.get(columnName);
        if (elementMap == null) {
            return null;
        }
        return elementMap.get("description");
    }

    // -----------------------------------------------------
    //                                            Column Map
    //                                            ----------
    @SuppressWarnings("unchecked")
    protected Map<String, Map<String, String>> findColumnMap(String tableName) { // null allowed: null means not defined
        final Map<String, Object> componentMap = (Map<String, Object>) getAdditionalDbCommentMap().get(tableName);
        if (componentMap == null) {
            return DfCollectionUtil.emptyMap();
        }
        return (Map<String, Map<String, String>>) componentMap.get("columnMap");
    }
}