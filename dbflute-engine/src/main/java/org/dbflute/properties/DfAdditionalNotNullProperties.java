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

import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.helper.StringKeyMap;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.2.9 (2024/10/13 Sunday at nakameguro)
 */
public final class DfAdditionalNotNullProperties extends DfAbstractDBFluteProperties {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String KEY_ALL = "$$ALL$$";
    public static final String KEY_NUANCE = "nuance";
    public static final String KEY_NUANCE_BUSINESS = "business";
    public static final String KEY_NUANCE_MAYBE = "maybe";

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * @param prop Properties. (NotNull)
     */
    public DfAdditionalNotNullProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                                additionalNotNullMap
    //                                                                ====================
    // # map: {
    // #     $$ALL$$ = map:{
    // #         ; columnMap = map:{
    // #             [column-name] = map:{ notnull = true ; maybe = false }
    // #         }
    // #     }
    // #     [table-name] = map:{
    // #         ; columnMap = map:{
    // #             [column-name] = map:{ notnull = true ; maybe = true }
    // #             [column-name] = map:{ notnull = false }
    // #         }
    // #     }
    // # }
    public static final String KEY_additionalNotNullMap = "additionalNotNullMap";
    protected Map<String, Object> _additionalNotNullMap; // lazy loaded

    public Map<String, Object> getAdditionalNotNullMap() {
        if (_additionalNotNullMap == null) {
            final Map<String, Object> resolvedMap = resolveSplit(KEY_additionalNotNullMap, preparePlainMap());
            final StringKeyMap<Object> flexibleMap = StringKeyMap.createAsFlexibleOrdered(); // root has table names
            flexibleMap.putAll(resolvedMap);
            _additionalNotNullMap = flexibleMap;
        }
        return _additionalNotNullMap;
    }

    protected Map<String, Object> preparePlainMap() {
        return mapProp("torque." + KEY_additionalNotNullMap, DEFAULT_EMPTY_MAP);
    }

    // ===================================================================================
    //                                                               NotNull Determination
    //                                                               =====================
    public boolean isColumnNotNullBusiness(String tableName, String columnName) {
        final Map<String, String> elementMap = searchColumnElementMap(tableName, columnName);
        assertNotNullNuance(tableName, columnName, elementMap);
        if (determineColumnBusiness(elementMap)) {
            return true;
        }
        final Map<String, String> elementMapFromAll = searchColumnElementMapFromAll(columnName);
        assertNotNullNuance(KEY_ALL, columnName, elementMap);
        if (determineColumnBusiness(elementMapFromAll)) {
            return true;
        }
        return false;
    }

    public boolean isColumnNotNullMaybe(String tableName, String columnName) {
        final Map<String, String> elementMap = searchColumnElementMap(tableName, columnName);
        assertNotNullNuance(tableName, columnName, elementMap);
        if (determineColumnMaybe(elementMap)) {
            return true;
        }
        final Map<String, String> elementMapFromAll = searchColumnElementMapFromAll(columnName);
        assertNotNullNuance(KEY_ALL, columnName, elementMap);
        if (determineColumnMaybe(elementMapFromAll)) {
            return true;
        }
        return false;
    }

    // -----------------------------------------------------
    //                                         Determination
    //                                         -------------
    protected boolean determineColumnBusiness(Map<String, String> elementMap) {
        return judgeLevel(elementMap, KEY_NUANCE_BUSINESS);
    }

    protected boolean determineColumnMaybe(Map<String, String> elementMap) {
        return judgeLevel(elementMap, KEY_NUANCE_MAYBE);
    }

    protected boolean judgeLevel(Map<String, String> elementMap, String target) { // elementMap is null allowed
        if (elementMap == null) { // means undefined for the column
            return false; // basically not called
        }
        final String nuance = getProperty(KEY_NUANCE, null, elementMap); // not null here
        return Srl.equalsIgnoreCase(nuance, target);
    }

    // -----------------------------------------------------
    //                                             Exception
    //                                             ---------
    protected void assertNotNullNuance(String tableName, String columnName, Map<String, String> elementMap) {
        if (elementMap == null) { // originally undefined
            return;
        }
        final String nuance = elementMap.get(KEY_NUANCE);
        if (nuance == null) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("Not found the nuance property of additionalNotNull.");
            br.addItem("[Advice]");
            br.addElement("The nuance property of additionalNotNullMap.dfprop");
            br.addElement("is required as 'business' or 'maybe'.");
            br.addElement("For example:");
            br.addElement("  (x):");
            br.addElement("    BIRTHDATE = map:{}");
            br.addElement("    BIRTHDATE = map:{ nuaaaaaance = business }");
            br.addElement("    BIRTHDATE = map:{ nuance = }");
            br.addElement("  (o):");
            br.addElement("    BIRTHDATE = map:{ nuance = business }");
            br.addElement("    BIRTHDATE = map:{ nuance = maybe }");
            br.addElement("");
            br.addItem("Table");
            br.addElement(tableName);
            br.addItem("Column");
            br.addElement(columnName);
            br.addItem("Wrong Map");
            br.addElement(elementMap);
            final String msg = br.buildExceptionMessage();
            throw new DfIllegalPropertySettingException(msg);
        }
        if (!isSupportedNotNullNuance(nuance)) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("Wrong expression for additionalNotNull nuance.");
            br.addItem("[Advice]");
            br.addElement("The nuance property of additionalNotNullMap.dfprop");
            br.addElement("must be 'business' or 'maybe'.");
            br.addElement("For example:");
            br.addElement("  (x):");
            br.addElement("    BIRTHDATE = map:{ nuance = sea }");
            br.addElement("    BIRTHDATE = map:{ nuance = land }");
            br.addElement("  (o):");
            br.addElement("    BIRTHDATE = map:{ nuance = business }");
            br.addElement("    BIRTHDATE = map:{ nuance = maybe }");
            br.addElement("");
            br.addItem("Table");
            br.addElement(tableName);
            br.addItem("Column");
            br.addElement(columnName);
            br.addItem("Wrong Map");
            br.addElement(elementMap);
            final String msg = br.buildExceptionMessage();
            throw new DfIllegalPropertySettingException(msg);
        }
    }

    protected boolean isSupportedNotNullNuance(String nuance) {
        // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
        // #for_now jflute virtual is unsupported yet, almost unneeded so pending (2024/10/14)
        //  virtual :: reflected to dbmeta, same as additional FK
        //  business :: document only but actually not null in database (except bugs)
        //  maybe :: document only, possibility only
        // _/_/_/_/_/_/_/_/_/_/
        return Srl.equalsIgnoreCase(nuance, KEY_NUANCE_BUSINESS, KEY_NUANCE_MAYBE);
    }

    // ===================================================================================
    //                                                                          Search Map
    //                                                                          ==========
    // -----------------------------------------------------
    //                                    Column Element Map
    //                                    ------------------
    protected Map<String, String> searchColumnElementMap(String tableName, String columnName) { // null allowed when undefined
        final Map<String, Map<String, String>> columnMap = searchColumnMap(tableName);
        if (columnMap == null) {
            return null;
        }
        final Map<String, String> elementMap = columnMap.get(columnName);
        if (elementMap == null) {
            return null;
        }
        return elementMap;
    }

    protected Map<String, String> searchColumnElementMapFromAll(String columnName) { // null allowed when undefined
        final Map<String, Map<String, String>> allMap = searchColumnMap(KEY_ALL);
        if (allMap == null) {
            return null;
        }
        final Map<String, String> allElement = allMap.get(columnName);
        if (allElement == null) {
            return null;
        }
        return allElement;
    }

    // -----------------------------------------------------
    //                                            Column Map
    //                                            ----------
    @SuppressWarnings("unchecked")
    protected Map<String, Map<String, String>> searchColumnMap(String tableName) { // null allowed when undefined
        final Map<String, Object> componentMap = (Map<String, Object>) getAdditionalNotNullMap().get(tableName);
        if (componentMap == null) {
            return null;
        }
        final Map<String, Map<String, String>> columnMap = (Map<String, Map<String, String>>) componentMap.get("columnMap");
        if (columnMap == null) {
            return null;
        }
        final StringKeyMap<Map<String, String>> flexibleMap = StringKeyMap.createAsFlexibleOrdered();
        flexibleMap.putAll(columnMap);
        return flexibleMap;
    }
}