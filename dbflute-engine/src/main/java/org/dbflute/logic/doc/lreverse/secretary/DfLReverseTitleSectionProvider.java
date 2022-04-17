/*
 * Copyright 2014-2022 the original author or authors.
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
package org.dbflute.logic.doc.lreverse.secretary;

import java.util.ArrayList;
import java.util.List;

import org.apache.torque.engine.database.model.Table;
import org.dbflute.DfBuildProperties;
import org.dbflute.properties.DfDocumentProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.2.5 as split (2021/01/04 Monday at roppongi japanese)
 */
public class DfLReverseTitleSectionProvider {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(DfLReverseTitleSectionProvider.class);

    // ===================================================================================
    //                                                                             Prepare
    //                                                                             =======
    public List<String> prepareTitleSection(List<Table> tableList) {
        final List<String> sectionInfoList = new ArrayList<String>();
        sectionInfoList.add("...Outputting load data: tables=" + tableList.size());
        final Integer recordLimit = getRecordLimit();
        if (recordLimit != null) {
            sectionInfoList.add("  recordLimit = " + recordLimit);
        }
        sectionInfoList.add("  isReplaceSchemaDirectUse = " + isReplaceSchemaDirectUse());
        sectionInfoList.add("  isOverrideExistingDataFile = " + isOverrideExistingDataFile());
        sectionInfoList.add("  isSynchronizeOriginDate = " + isSynchronizeOriginDate());
        final Integer xlsLimit = getXlsLimit();
        if (xlsLimit != null) { // e.g. xlsLimit = 0 (all TSV mode)
            sectionInfoList.add("  xlsLimit = " + xlsLimit);
        }
        for (String sectionInfo : sectionInfoList) {
            _log.info(sectionInfo);
        }
        return sectionInfoList;
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
    //                                         File Resource
    //                                         -------------
    protected Integer getRecordLimit() {
        return getDocumentProperties().getLoadDataReverseRecordLimit();
    }

    // -----------------------------------------------------
    //                                          Basic Option
    //                                          ------------
    protected boolean isReplaceSchemaDirectUse() {
        return getDocumentProperties().isLoadDataReverseReplaceSchemaDirectUse();
    }

    protected boolean isOverrideExistingDataFile() {
        return getDocumentProperties().isLoadDataReverseOverrideExistingDataFile();
    }

    protected boolean isSynchronizeOriginDate() {
        return getDocumentProperties().isLoadDataReverseSynchronizeOriginDate();
    }

    // -----------------------------------------------------
    //                                        Output Handler
    //                                        --------------
    protected Integer getXlsLimit() {
        return getDocumentProperties().getLoadDataReverseXlsLimit();
    }
}
