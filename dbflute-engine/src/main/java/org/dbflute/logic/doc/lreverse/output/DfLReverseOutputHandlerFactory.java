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
package org.dbflute.logic.doc.lreverse.output;

import org.dbflute.DfBuildProperties;
import org.dbflute.helper.jdbc.context.DfSchemaSource;
import org.dbflute.properties.DfDocumentProperties;

/**
 * @author jflute
 * @since 1.2.5 as split (2021/01/04 Monday at roppongi japanese)
 */
public class DfLReverseOutputHandlerFactory {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfSchemaSource _dataSource;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfLReverseOutputHandlerFactory(DfSchemaSource dataSource) {
        _dataSource = dataSource;
    }

    // ===================================================================================
    //                                                                              Create
    //                                                                              ======
    public DfLReverseOutputHandler createOutputHandler() {
        final DfLReverseOutputHandler handler = new DfLReverseOutputHandler(_dataSource);
        handler.setContainsCommonColumn(isContainsCommonColumn());

        // option of Delimiter Data
        handler.setLargeDataDir(getDelimiterDataDir());
        handler.setDelimiterDataBasis(isDelimiterDataBasis());
        handler.setDelimiterMinimallyQuoted(isDelimiterDataMinimallyQuoted());
        // changes to TSV for compatibility of copy and paste to excel @since 0.9.8.3
        //handler.setDelimiterDataTypeCsv(true);

        // option of Xls Data
        final Integer xlsLimit = getXlsLimit(); // if null, default limit
        if (xlsLimit != null) {
            handler.setXlsLimit(xlsLimit);
        }
        if (isSuppressLargeDataHandling()) {
            handler.setSuppressLargeDataHandling(true);
        }
        if (isSuppressQuoteEmptyString()) {
            handler.setSuppressQuoteEmptyString(true);
        }
        final Integer cellLengthLimit = getCellLengthLimit();
        if (cellLengthLimit != null) {
            handler.setCellLengthLimit(cellLengthLimit);
        }
        return handler;
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfDocumentProperties getDocumentProperties() {
        return getProperties().getDocumentProperties();
    }

    // -----------------------------------------------------
    //                                        Output Handler
    //                                        --------------
    protected boolean isContainsCommonColumn() {
        return getDocumentProperties().isLoadDataReverseContainsCommonColumn();
    }

    protected Integer getXlsLimit() {
        return getDocumentProperties().getLoadDataReverseXlsLimit();
    }

    protected boolean isDelimiterDataBasis() {
        return getDocumentProperties().isLoadDataReverseDelimiterDataBasis();
    }

    protected boolean isDelimiterDataMinimallyQuoted() {
        return getDocumentProperties().isLoadDataReverseDelimiterDataMinimallyQuoted();
    }

    protected boolean isSuppressLargeDataHandling() {
        return getDocumentProperties().isLoadDataReverseSuppressLargeDataHandling();
    }

    protected boolean isSuppressQuoteEmptyString() {
        return getDocumentProperties().isLoadDataReverseSuppressQuoteEmptyString();
    }

    protected Integer getCellLengthLimit() {
        return getDocumentProperties().getLoadDataReverseCellLengthLimit();
    }

    protected String getDelimiterDataDir() {
        return getDocumentProperties().getLoadDataReverseDelimiterDataDir();
    }
}
