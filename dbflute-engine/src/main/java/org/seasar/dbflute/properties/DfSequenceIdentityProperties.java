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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.torque.engine.database.model.UnifiedSchema;
import org.seasar.dbflute.exception.DfIllegalPropertySettingException;
import org.seasar.dbflute.exception.DfIllegalPropertyTypeException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.helper.jdbc.context.DfSchemaSource;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfSequenceMeta;
import org.seasar.dbflute.logic.jdbc.metadata.sequence.DfSequenceExtractor;
import org.seasar.dbflute.logic.jdbc.metadata.sequence.factory.DfSequenceExtractorFactory;
import org.seasar.dbflute.properties.assistant.DfTableDeterminer;
import org.seasar.dbflute.properties.facade.DfDatabaseTypeFacadeProp;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public final class DfSequenceIdentityProperties extends DfAbstractHelperProperties {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSequenceIdentityProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                             Sequence Definition Map
    //                                                             =======================
    protected static final String KEY_sequenceDefinitionMap = "sequenceDefinitionMap";
    protected Map<String, String> _sequenceDefinitionMap;
    protected Map<String, String> _subColumnSequenceDefinitionMap;

    protected Map<String, String> getSequenceDefinitionMap() {
        if (_sequenceDefinitionMap != null) {
            return _sequenceDefinitionMap;
        }
        final Map<String, String> flexibleMap = StringKeyMap.createAsFlexibleOrdered();
        final Map<String, String> flexibleSubMap = StringKeyMap.createAsFlexibleOrdered();
        final Map<String, Object> originalMap = mapProp("torque." + KEY_sequenceDefinitionMap, DEFAULT_EMPTY_MAP);
        final Set<Entry<String, Object>> entrySet = originalMap.entrySet();
        for (Entry<String, Object> entry : entrySet) {
            final String tableName = entry.getKey();
            final Object sequenceName = entry.getValue();
            if (!(sequenceName instanceof String)) {
                String msg = "The value of sequence map should be string:";
                msg = msg + " sequenceName=" + sequenceName + " map=" + originalMap;
                throw new DfIllegalPropertyTypeException(msg);
            }
            if (!tableName.contains(".")) { // tableName only means primary sequence
                flexibleMap.put(tableName, (String) sequenceName);
            } else { // means sub sequence for normal columns
                flexibleSubMap.put(tableName, (String) sequenceName);
            }
        }
        _sequenceDefinitionMap = flexibleMap;
        _subColumnSequenceDefinitionMap = flexibleSubMap;
        return _sequenceDefinitionMap;
    }

    public Map<String, String> getTableSequenceMap() {
        final Map<String, String> sequenceDefinitionMap = getSequenceDefinitionMap();
        final Map<String, String> resultMap = new LinkedHashMap<String, String>();
        final Set<String> keySet = sequenceDefinitionMap.keySet();
        for (String tableName : keySet) {
            resultMap.put(tableName, getSequenceName(tableName));
        }
        return resultMap;
    }

    public String getSequenceName(String tableName) {
        final String sequenceProp = getSequenceDefinitionMap().get(tableName);
        return extractSequenceNameFromProp(sequenceProp);
    }

    protected String extractSequenceNameFromProp(String sequenceProp) {
        if (sequenceProp == null || sequenceProp.trim().length() == 0) {
            return null;
        }
        final String hintMark = getSequenceHintMark();
        final int hintMarkIndex = sequenceProp.lastIndexOf(hintMark);
        if (hintMarkIndex < 0) {
            return sequenceProp;
        }
        final String sequenceName = sequenceProp.substring(0, hintMarkIndex);
        if (sequenceName == null || sequenceName.trim().length() == 0) {
            return null;
        }
        return sequenceName;
    }

    protected String getSequenceHintMark() {
        return ":";
    }

    protected String getSequenceProp(String tableName) {
        final String sequenceProp = getSequenceDefinitionMap().get(tableName);
        if (sequenceProp == null || sequenceProp.trim().length() == 0) {
            return null;
        }
        return sequenceProp;
    }

    // -----------------------------------------------------
    //                                          Sub Sequence
    //                                          ------------
    protected Map<String, String> getSubColumnSequenceDefinitionMap() {
        if (_subColumnSequenceDefinitionMap != null) {
            return _subColumnSequenceDefinitionMap;
        }
        getSequenceDefinitionMap(); // initialize
        return _subColumnSequenceDefinitionMap;
    }

    public boolean hasSubColumnSequence() {
        return !getSubColumnSequenceDefinitionMap().isEmpty();
    }

    public String getSubColumnSequenceName(String tableName, String columnName) {
        final String sequenceProp = getSubColumnSequenceProp(tableName, columnName);
        if (Srl.is_Null_or_TrimmedEmpty(sequenceProp)) {
            return null;
        }
        final String hintMark = getSequenceHintMark();
        if (sequenceProp.contains(hintMark)) { // unsupported
            String msg = "Sequence hint for sub-column sequence is unsupported: " + sequenceProp;
            throw new DfIllegalPropertySettingException(msg);
        }
        return extractSequenceNameFromProp(sequenceProp);
    }

    protected String getSubColumnSequenceProp(String tableName, String columnName) {
        final String key = generateSubColumnSequenceKey(tableName, columnName);
        return getSubColumnSequenceDefinitionMap().get(key);
    }

    protected String generateSubColumnSequenceKey(String tableName, String columnName) {
        return tableName + "." + columnName;
    }

    // -----------------------------------------------------
    //                                      Check Definition
    //                                      ----------------
    /**
     * @param determiner The checker for call-back. (NotNull)
     */
    public void checkDefinition(DfTableDeterminer determiner) {
        final List<String> notFoundTableNameList = new ArrayList<String>();
        {
            final Map<String, String> sequenceDefinitionMap = getSequenceDefinitionMap();
            final Set<Entry<String, String>> entrySet = sequenceDefinitionMap.entrySet();
            for (Entry<String, String> entry : entrySet) {
                final String tableName = entry.getKey();
                if (!determiner.hasTable(tableName)) {
                    notFoundTableNameList.add(tableName);
                }
            }
        }
        {
            final Map<String, String> sequenceSubDefinitionMap = getSubColumnSequenceDefinitionMap();
            final Set<Entry<String, String>> entrySet = sequenceSubDefinitionMap.entrySet();
            for (Entry<String, String> entry : entrySet) {
                final String key = entry.getKey();
                final String tableName = Srl.substringFirstFront(key, ".");
                final String columnName = Srl.substringFirstRear(key, ".");
                if (!determiner.hasTableColumn(tableName, columnName)) {
                    notFoundTableNameList.add(key);
                }
            }
        }
        if (!notFoundTableNameList.isEmpty()) {
            throwSequenceDefinitionMapNotFoundTableException(notFoundTableNameList);
        }
    }

    protected void throwSequenceDefinitionMapNotFoundTableException(List<String> notFoundTableNameList) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The table name on the sequence definition was not found.");
        br.addItem("NotFound Table (or Column)");
        for (String tableName : notFoundTableNameList) {
            br.addElement(tableName);
        }
        br.addItem("Sequence Definition");
        br.addElement(_sequenceDefinitionMap);
        final String msg = br.buildExceptionMessage();
        throw new DfSequenceDefinitionMapTableNotFoundException(msg);
    }

    public static class DfSequenceDefinitionMapTableNotFoundException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public DfSequenceDefinitionMapTableNotFoundException(String msg) {
            super(msg);
        }
    }

    // ===================================================================================
    //                                                                Sequence Return Type
    //                                                                ====================
    public String getSequenceReturnType() { // not property
        return getBasicProperties().getLanguageDependency().getLanguageTypeMapping().getSequenceJavaNativeType();
    }

    // ===================================================================================
    //                                                            Sequence (Meta Info) Map
    //                                                            ========================
    protected Map<String, DfSequenceMeta> _sequenceMap;

    public Map<String, DfSequenceMeta> getSequenceMap(DfSchemaSource dataSource) {
        if (_sequenceMap != null) {
            return _sequenceMap;
        }
        final DfSequenceExtractorFactory factory = createSequenceExtractorFactory(dataSource);
        final DfSequenceExtractor sequenceExtractor = factory.createSequenceExtractor();
        Map<String, DfSequenceMeta> sequenceMap = null;
        if (sequenceExtractor != null) {
            sequenceMap = sequenceExtractor.extractSequenceMap();
        }
        if (sequenceMap != null) {
            _sequenceMap = sequenceMap;
        } else {
            _sequenceMap = DfCollectionUtil.emptyMap();
        }
        return _sequenceMap;
    }

    protected DfSequenceExtractorFactory createSequenceExtractorFactory(DfSchemaSource dataSource) {
        final DfDatabaseTypeFacadeProp facadeProp = getBasicProperties().getDatabaseTypeFacadeProp();
        return new DfSequenceExtractorFactory(dataSource, facadeProp, getDatabaseProperties());
    }

    protected DfSequenceMeta getSequenceElement(UnifiedSchema unifiedSchema, String sequenceName,
            Map<String, DfSequenceMeta> sequenceMap) {
        DfSequenceMeta info = sequenceMap.get(sequenceName);
        if (info != null) {
            return info;
        }
        if (sequenceName.contains(".")) {
            sequenceName = Srl.substringLastRear(sequenceName, "."); // pure sequence name
        }
        info = sequenceMap.get(unifiedSchema.buildFullQualifiedName(sequenceName));
        if (info != null) {
            return info;
        }
        info = sequenceMap.get(unifiedSchema.buildSchemaQualifiedName(sequenceName));
        if (info != null) {
            return info;
        }
        return info;
    }

    // -----------------------------------------------------
    //                                         Minimum Value
    //                                         -------------
    public BigDecimal getSequenceMinimumValueByTableName(DfSchemaSource dataSource, UnifiedSchema unifiedSchema,
            String tableName) {
        final String sequenceName = getSequenceName(tableName);
        if (sequenceName == null) {
            return null;
        }
        return getSequenceMinimumValueBySequenceName(dataSource, unifiedSchema, sequenceName);
    }

    public BigDecimal getSequenceMinimumValueBySequenceName(DfSchemaSource dataSource, UnifiedSchema unifiedSchema,
            String sequenceName) {
        final Map<String, DfSequenceMeta> sequenceMap = getSequenceMap(dataSource);
        return getSequenceMinimumValue(unifiedSchema, sequenceName, sequenceMap);
    }

    protected BigDecimal getSequenceMinimumValue(UnifiedSchema unifiedSchema, String sequenceName,
            Map<String, DfSequenceMeta> sequenceMap) {
        final DfSequenceMeta info = getSequenceElement(unifiedSchema, sequenceName, sequenceMap);
        if (info != null) {
            final BigDecimal minimumValue = info.getMinimumValue();
            if (minimumValue != null) {
                return minimumValue;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    // -----------------------------------------------------
    //                                         Maximum Value
    //                                         -------------
    public BigDecimal getSequenceMaximumValueByTableName(DfSchemaSource dataSource, UnifiedSchema unifiedSchema,
            String tableName) {
        final String sequenceName = getSequenceName(tableName);
        if (sequenceName == null) {
            return null;
        }
        return getSequenceMaximumValueBySequenceName(dataSource, unifiedSchema, sequenceName);
    }

    public BigDecimal getSequenceMaximumValueBySequenceName(DfSchemaSource dataSource, UnifiedSchema unifiedSchema,
            String sequenceName) {
        final Map<String, DfSequenceMeta> sequenceMap = getSequenceMap(dataSource);
        return getSequenceMaximumValue(unifiedSchema, sequenceName, sequenceMap);
    }

    protected BigDecimal getSequenceMaximumValue(UnifiedSchema unifiedSchema, String sequenceName,
            Map<String, DfSequenceMeta> sequenceMap) {
        final DfSequenceMeta info = getSequenceElement(unifiedSchema, sequenceName, sequenceMap);
        if (info != null) {
            final BigDecimal maximumValue = info.getMaximumValue();
            if (maximumValue != null) {
                return maximumValue;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    // -----------------------------------------------------
    //                                        Increment Size
    //                                        --------------
    public Integer getSequenceIncrementSizeByTableName(DfSchemaSource dataSource, UnifiedSchema unifiedSchema,
            String tableName) {
        final String sequenceName = getSequenceName(tableName);
        if (sequenceName == null) {
            return null;
        }
        return getSequenceIncrementSizeBySequenceName(dataSource, unifiedSchema, sequenceName);
    }

    public Integer getSequenceIncrementSizeBySequenceName(DfSchemaSource dataSource, UnifiedSchema unifiedSchema,
            String sequenceName) {
        final Map<String, DfSequenceMeta> sequenceMap = getSequenceMap(dataSource);
        return getSequenceIncrementSize(unifiedSchema, sequenceName, sequenceMap);
    }

    protected Integer getSequenceIncrementSize(UnifiedSchema unifiedSchema, String sequenceName,
            Map<String, DfSequenceMeta> sequenceMap) {
        final DfSequenceMeta info = getSequenceElement(unifiedSchema, sequenceName, sequenceMap);
        if (info != null) {
            final Integer incrementSize = info.getIncrementSize();
            if (incrementSize != null) {
                return incrementSize;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    // -----------------------------------------------------
    //                                  (DBFlute) Cache Size
    //                                  --------------------
    public Integer getSequenceCacheSize(DfSchemaSource dataSource, UnifiedSchema unifiedSchema, String tableName) {
        final String sequenceProp = getSequenceProp(tableName);
        if (sequenceProp == null) {
            return null;
        }
        final String hintMark = getSequenceHintMark();
        final int hintMarkIndex = sequenceProp.lastIndexOf(hintMark);
        if (hintMarkIndex < 0) {
            return null;
        }
        final String hint = sequenceProp.substring(hintMarkIndex + hintMark.length()).trim();
        final String cacheMark = "dfcache(";
        final int cacheMarkIndex = hint.indexOf(cacheMark);
        if (cacheMarkIndex < 0) {
            return null;
        }
        final String cacheValue = hint.substring(cacheMarkIndex + cacheMark.length()).trim();
        final String endMark = ")";
        final int endMarkIndex = cacheValue.indexOf(endMark);
        if (endMarkIndex < 0) {
            String msg = "The increment size setting needs end mark ')':";
            msg = msg + " sequence=" + sequenceProp;
            throw new IllegalStateException(msg);
        }
        final String cacheSizeProp = cacheValue.substring(0, endMarkIndex).trim();
        final String sequenceName = getSequenceName(tableName);
        final Map<String, DfSequenceMeta> sequenceMap = getSequenceMap(dataSource);
        final Integer incrementSize = getSequenceIncrementSize(unifiedSchema, sequenceName, sequenceMap);
        if (cacheSizeProp != null && cacheSizeProp.trim().length() > 0) { // cacheSize is specified
            final Integer cacheSize = castCacheSize(cacheSizeProp, tableName, sequenceProp, sequenceName);
            assertCacheSizeOverOne(cacheSize, unifiedSchema, tableName, sequenceProp, sequenceName, incrementSize);
            if (incrementSize != null) { // can get it from meta
                assertIncrementSizeNotDecrement(incrementSize, unifiedSchema, tableName, sequenceProp, sequenceName);
                assertExtraValueZero(cacheSize, incrementSize, unifiedSchema, tableName, sequenceProp, sequenceName);

                // *no limit because of self-responsibility
                //assertCacheSizeOfBatchWayNotTooLarge(cacheSize, schemaName, tableName, sequenceProp, sequenceName, incrementSize);

                return cacheSize; // batch way
            } else {
                // the specified cacheSize should be same as actual increment size
                // (DBFlute cannot know it)
                return cacheSize; // increment way
            }
        } else { // cacheSize is omitted
            assertIncrementSizeExistsIfNoCacheSize(incrementSize, unifiedSchema, tableName, sequenceProp, sequenceName);
            assertIncrementSizeNotDecrement(incrementSize, unifiedSchema, tableName, sequenceProp, sequenceName);
            // the cacheSize is same as actual increment size
            assertCacheSizeOverOne(incrementSize, unifiedSchema, tableName, sequenceProp, sequenceName, incrementSize);
            return incrementSize; // increment way
        }
    }

    protected Integer castCacheSize(String cacheSize, String tableName, String sequenceProp, String sequenceName) {
        try {
            return Integer.valueOf(cacheSize);
        } catch (NumberFormatException e) {
            String msg = "Look! Read the message below." + ln();
            msg = msg + "/- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -" + ln();
            msg = msg + "Failed to cast the cache size to integer:" + ln();
            msg = msg + ln();
            msg = msg + "table = " + tableName + ln();
            msg = msg + "sequenceProp = " + sequenceProp + ln();
            msg = msg + "sequenceName = " + sequenceName + ln();
            msg = msg + "- - - - - - - - - -/";
            throw new DfIllegalPropertySettingException(msg);
        }
    }

    protected void assertCacheSizeOverOne(Integer cacheSize, UnifiedSchema unifiedSchema, String tableName,
            String sequenceProp, String sequenceName, Integer incrementSize) {
        if (cacheSize <= 1) {
            String msg = "Look! Read the message below." + ln();
            msg = msg + "/- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -" + ln();
            msg = msg + "The cacheSize should be over 1. (not minus, not zero, not one)" + ln();
            msg = msg + ln();
            msg = msg + "schema = " + unifiedSchema + ln() + "table = " + tableName + ln();
            msg = msg + "sequenceProp = " + sequenceProp + ln();
            msg = msg + "sequenceName = " + sequenceName + ln();
            msg = msg + "cacheSize = " + cacheSize + ln();
            msg = msg + "incrementSize = " + incrementSize + ln();
            msg = msg + "- - - - - - - - - -/";
            throw new DfIllegalPropertySettingException(msg);
        }
    }

    // *no limit because of self-responsibility
    //protected void assertCacheSizeOfBatchWayNotTooLarge(Integer cacheSize, String schemaName, String tableName,
    //        String sequenceProp, String sequenceName, Integer incrementSize) {
    //    if (cacheSize > 2000) {
    //        String msg = "Look! Read the message below." + ln();
    //        msg = msg + "/- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -" + ln();
    //        msg = msg + "The cacheSize of batch way should be under 2000. (not 2001 or more)" + ln();
    //        msg = msg + ln();
    //        msg = msg + "schema = " + schemaName + ln() + "table = " + tableName + ln();
    //        msg = msg + "sequenceProp = " + sequenceProp + ln();
    //        msg = msg + "sequenceName = " + sequenceName + ln();
    //        msg = msg + "cacheSize = " + cacheSize + ln();
    //        msg = msg + "incrementSize = " + incrementSize + ln();
    //        msg = msg + "- - - - - - - - - -/";
    //        throw new DfIllegalPropertySettingException(msg);
    //    }
    //}

    protected void assertIncrementSizeNotDecrement(Integer incrementSize, UnifiedSchema unifiedSchema,
            String tableName, String sequenceProp, String sequenceName) {
        if (incrementSize <= 0) { // contains zero just in case
            String msg = "Look! Read the message below." + ln();
            msg = msg + "/- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -" + ln();
            msg = msg + "The incrementSize should be increment! (NOT decrement)" + ln();
            msg = msg + ln();
            msg = msg + "schema = " + unifiedSchema + ln() + "table = " + tableName + ln();
            msg = msg + "sequenceProp = " + sequenceProp + ln();
            msg = msg + "sequenceName = " + sequenceName + ln();
            msg = msg + "incrementSize = " + incrementSize + ln();
            msg = msg + "- - - - - - - - - -/";
            throw new DfIllegalPropertySettingException(msg);
        }
    }

    protected void assertExtraValueZero(Integer cacheSize, Integer incrementSize, UnifiedSchema unifiedSchema,
            String tableName, String sequenceProp, String sequenceName) {
        final Integer extraValue = cacheSize % incrementSize;
        if (extraValue != 0) {
            String msg = "Look! Read the message below." + ln();
            msg = msg + "/- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -" + ln();
            msg = msg + "The cacheSize cannot be divided by incrementSize!" + ln();
            msg = msg + ln();
            msg = msg + "schema = " + unifiedSchema + ln() + "table = " + tableName + ln();
            msg = msg + "sequenceProp = " + sequenceProp + ln();
            msg = msg + "sequenceName = " + sequenceName + ln();
            msg = msg + "cacheSize = " + cacheSize + ln();
            msg = msg + "incrementSize = " + incrementSize + ln();
            msg = msg + "extraValue = " + extraValue + ln();
            msg = msg + "- - - - - - - - - -/";
            throw new DfIllegalPropertySettingException(msg);
        }
    }

    protected void assertIncrementSizeExistsIfNoCacheSize(Integer incrementSize, UnifiedSchema unifiedSchema,
            String tableName, String sequenceProp, String sequenceName) {
        if (incrementSize == null) {
            String msg = "Look! Read the message below." + ln();
            msg = msg + "/- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -" + ln();
            msg = msg + "Failed to get the cache size of sequence(by no increment size):" + ln();
            msg = msg + ln();
            msg = msg + "schema = " + unifiedSchema + ln() + "table = " + tableName + ln();
            msg = msg + "sequenceProp = " + sequenceProp + ln();
            msg = msg + "sequenceName = " + sequenceName + ln();
            msg = msg + "incrementSize = " + incrementSize + ln();
            msg = msg + "- - - - - - - - - -/";
            throw new DfIllegalPropertySettingException(msg);
        }
    }

    // ===================================================================================
    //                                                             Identity Definition Map
    //                                                             =======================
    protected static final String KEY_identityDefinitionMap = "identityDefinitionMap";
    protected Map<String, Object> _identityDefinitionMap;

    // # /---------------------------------------------------------------------------
    // # identityDefinitionMap: (Default 'map:{}')
    // # 
    // # The relation mappings between identity and column of table.
    // # Basically you don't need this property because DBFlute
    // # can get the information about identity from JDBC automatically.
    // # The table names and column names are treated as case insensitive.
    // # 
    // # Example:
    // # map:{
    // #     ; PURCHASE     = PURCHASE_ID
    // #     ; MEMBER       = MEMBER_ID
    // #     ; MEMBER_LOGIN = MEMBER_LOGIN_ID
    // #     ; PRODUCT      = PRODUCT_ID
    // # }
    // #
    // # *The line that starts with '#' means comment-out.
    // #
    // map:{
    //     #; PURCHASE     = PURCHASE_ID
    //     #; MEMBER       = MEMBER_ID
    //     #; MEMBER_LOGIN = MEMBER_LOGIN_ID
    //     #; PRODUCT      = PRODUCT_ID
    // }
    // # ----------------/

    protected Map<String, Object> getIdentityDefinitionMap() {
        if (_identityDefinitionMap == null) {
            final Map<String, Object> map = mapProp("torque." + KEY_identityDefinitionMap, DEFAULT_EMPTY_MAP);
            _identityDefinitionMap = newLinkedHashMap();
            _identityDefinitionMap.putAll(map);
        }
        return _identityDefinitionMap;
    }

    public String getIdentityColumnName(String flexibleTableName) {
        final Map<String, Object> flexibleMap = StringKeyMap.createAsFlexibleOrdered();
        flexibleMap.putAll(getIdentityDefinitionMap());
        return (String) flexibleMap.get(flexibleTableName);
    }
}