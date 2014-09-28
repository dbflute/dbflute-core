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

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.torque.engine.database.model.Table;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.properties.assistant.DfTableListProvider;
import org.seasar.dbflute.util.DfTypeUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.8.8.1 (2009/01/09 Friday)
 */
public final class DfOptimisticLockProperties extends DfAbstractHelperProperties {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Constructor.
     * @param prop Properties. (NotNull)
     */
    public DfOptimisticLockProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                      Optimistic Lock Definition Map
    //                                                      ==============================
    public static final String KEY_optimisticLockDefinitionMap = "optimisticLockDefinitionMap";
    protected Map<String, Object> _optimisticLockDefinitionMap;

    public Map<String, Object> getOptimisticLockDefinitionMap() {
        if (_optimisticLockDefinitionMap == null) {
            final Map<String, Object> map = mapProp("torque." + KEY_optimisticLockDefinitionMap, DEFAULT_EMPTY_MAP);
            _optimisticLockDefinitionMap = newLinkedHashMap();
            _optimisticLockDefinitionMap.putAll(map);
        }
        return _optimisticLockDefinitionMap;
    }

    // -----------------------------------------------------
    //                                      Check Definition
    //                                      ----------------
    public void checkDefinition(DfTableListProvider provider) {
        if (!hasExplicitOptimisticLockColumn()) {
            return;
        }
        final List<Table> tableList = provider.provideTableList();
        boolean exists = false;
        for (Table table : tableList) {
            if (table.hasOptimisticLock()) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            throwOptimisticLockRelatedTableNotFoundException();
        }
    }

    protected void throwOptimisticLockRelatedTableNotFoundException() {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The table related to optimistic lock was not found.");
        br.addItem("Advice");
        br.addElement("At least one table should be related to optimistic lock");
        br.addElement("when the optimistic lock column is specified explicitly.");
        br.addElement("Make sure your definition is correct.");
        br.addItem("Specified Column");
        br.addElement(getExplicitOptimisticLockColumn());
        final String msg = br.buildExceptionMessage();
        throw new DfOptimisticLockRelatedTableNotFoundException(msg);
    }

    public static class DfOptimisticLockRelatedTableNotFoundException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public DfOptimisticLockRelatedTableNotFoundException(String msg) {
            super(msg);
        }
    }

    protected boolean hasExplicitOptimisticLockColumn() {
        return getExplicitOptimisticLockColumn() != null;
    }

    protected String getExplicitOptimisticLockColumn() {
        final String updateDateFieldName = doGetUpdateDateFieldName(""); // as no default
        if (Srl.is_NotNull_and_NotTrimmedEmpty(updateDateFieldName)) {
            return updateDateFieldName;
        }
        final String versionNoFieldName = doGetVersionNoFieldName(""); // as no default
        if (Srl.is_NotNull_and_NotTrimmedEmpty(versionNoFieldName)) {
            return versionNoFieldName;
        }
        return null;
    }

    // ===================================================================================
    //                                                                          Field Name
    //                                                                          ==========
    public String getUpdateDateFieldName() {
        return doGetUpdateDateFieldName("");
    }

    protected String doGetUpdateDateFieldName(String defaultValue) {
        return getProperty("updateDateFieldName", defaultValue);
    }

    public String getVersionNoFieldName() {
        return doGetVersionNoFieldName("version_no");
    }

    protected String doGetVersionNoFieldName(String defaultValue) {
        return getProperty("versionNoFieldName", defaultValue);
    }

    public boolean isOptimisticLockColumn(String columnName) {
        final String updateDate = getUpdateDateFieldName();
        if (Srl.is_NotNull_and_NotTrimmedEmpty(updateDate) && updateDate.equalsIgnoreCase(columnName)) {
            return true;
        }
        final String versionNo = getVersionNoFieldName();
        if (Srl.is_NotNull_and_NotTrimmedEmpty(versionNo) && versionNo.equalsIgnoreCase(columnName)) {
            return true;
        }
        return false;
    }

    // ===================================================================================
    //                                                                     Property Helper
    //                                                                     ===============
    protected String getProperty(String key, String defaultValue) {
        Map<String, Object> map = getOptimisticLockDefinitionMap();
        Object obj = map.get(key);
        if (obj != null) {
            if (!(obj instanceof String)) {
                String msg = "The key's value should be string:";
                msg = msg + " " + DfTypeUtil.toClassTitle(obj) + "=" + obj;
                throw new IllegalStateException(msg);
            }
            String value = (String) obj;
            if (value.trim().length() > 0) {
                return value;
            } else {
                return defaultValue;
            }
        }
        return stringProp("torque." + key, defaultValue);
    }

    protected boolean isProperty(String key, boolean defaultValue) {
        Map<String, Object> map = getOptimisticLockDefinitionMap();
        Object obj = map.get(key);
        if (obj != null) {
            if (!(obj instanceof String)) {
                String msg = "The key's value should be boolean:";
                msg = msg + " " + DfTypeUtil.toClassTitle(obj) + "=" + obj;
                throw new IllegalStateException(msg);
            }
            String value = (String) obj;
            if (value.trim().length() > 0) {
                return value.trim().equalsIgnoreCase("true");
            } else {
                return defaultValue;
            }
        }
        return booleanProp("torque." + key, defaultValue);
    }
}