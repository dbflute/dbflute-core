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
package org.dbflute.logic.generate.table;

import java.util.function.Supplier;

import org.apache.torque.engine.database.model.Column;
import org.apache.torque.engine.database.model.UnifiedSchema;
import org.dbflute.DfBuildProperties;
import org.dbflute.properties.DfBasicProperties;

/**
 * @author jflute
 * @since 1.2.7 (2023/01/11 Wednesday at roppongi japanese) (split it from Table class)
 */
public class DfSerialSequenceExtractor {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final UnifiedSchema _unifiedSchema; // null allowed

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSerialSequenceExtractor(UnifiedSchema unifiedSchema) {
        _unifiedSchema = unifiedSchema;
    }

    // ===================================================================================
    //                                                               Extract Sequence Name
    //                                                               =====================
    /**
     * Extract sequence name of postgreSQL serial type column (from default value).
     * @param hasAutoIncrementColumnDeterminer The determiner whether the table has auto-increment column? (if false, returns null)
     * @param autoIncrementColumnProvider The provider of column to be extracted to sequence name. (NullAllowed: if null, returns null)
     * @return The name of sequence containing schema prefix if needed. (NullAllowed: if null, not found)
     */
    public String extractPostgreSQLSerialSequenceName(Supplier<Boolean> hasAutoIncrementColumnDeterminer,
            Supplier<Column> autoIncrementColumnProvider) {
        if (!getBasicProperties().isDatabasePostgreSQL() || !hasAutoIncrementColumnDeterminer.get()) {
            return null;
        }
        final Column autoIncrementColumn = autoIncrementColumnProvider.get();
        if (autoIncrementColumn == null) {
            return null;
        }
        final String defaultValue = autoIncrementColumn.getDefaultValue();
        if (defaultValue == null) {
            return null;
        }
        final String prefix = "nextval('";
        if (!defaultValue.startsWith(prefix)) {
            return null;
        }
        final String excludedPrefixString = defaultValue.substring(prefix.length());
        final int endIndex = excludedPrefixString.indexOf("'");
        if (endIndex < 0) {
            return null;
        }
        final String extractedSequenceName = excludedPrefixString.substring(0, endIndex);
        return filterExtractedSequenceName(extractedSequenceName);
    }

    protected String filterExtractedSequenceName(String extractedSequenceName) {
        if (_unifiedSchema != null && needsSchemaPrefixFilter(extractedSequenceName)) {
            return _unifiedSchema.buildSqlName(extractedSequenceName);
        } else {
            return extractedSequenceName;
        }
    }

    protected boolean needsSchemaPrefixFilter(String extractedSequenceName) {
        // #for_now jflute catalog prefix for next schema is unsupported yet (2023/01/15)
        return !extractedSequenceName.contains(".");
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
}