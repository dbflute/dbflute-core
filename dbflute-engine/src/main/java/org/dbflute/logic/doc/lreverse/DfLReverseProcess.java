/*
 * Copyright 2014-2021 the original author or authors.
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
package org.dbflute.logic.doc.lreverse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.torque.engine.database.model.Database;
import org.apache.torque.engine.database.model.Table;
import org.dbflute.DfBuildProperties;
import org.dbflute.exception.DfLReverseProcessFailureException;
import org.dbflute.helper.jdbc.context.DfSchemaSource;
import org.dbflute.logic.doc.lreverse.order.DfLReverseFileOrder;
import org.dbflute.logic.doc.lreverse.origindate.DfLReverseOriginDateSynchronizer;
import org.dbflute.logic.doc.lreverse.output.DfLReverseOutputHandler;
import org.dbflute.logic.doc.lreverse.output.DfLReverseOutputHandlerFactory;
import org.dbflute.logic.doc.lreverse.reverse.DfLReverseTableData;
import org.dbflute.logic.doc.lreverse.schema.DfLReverseSchemaMetaProvider;
import org.dbflute.logic.doc.lreverse.schema.DfLReverseSchemaTableFilter;
import org.dbflute.logic.doc.lreverse.secretary.DfLReverseTitleSectionProvider;
import org.dbflute.logic.replaceschema.loaddata.base.dataprop.DfTableNameProp;
import org.dbflute.properties.DfDocumentProperties;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 0.9.8.3 (2011/04/23 Saturday)
 */
public class DfLReverseProcess {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(DfLReverseProcess.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfSchemaSource _dataSource;
    protected final DfLReverseOutputHandler _outputHandler; // also provides tableNameMap

    // shared data between components
    protected final DfTableNameProp _tableNameProp = new DfTableNameProp();
    protected final List<Table> _skippedTableList = DfCollectionUtil.newArrayList(); // initialize in filter

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfLReverseProcess(DfSchemaSource dataSource) {
        _dataSource = dataSource;
        _outputHandler = createOutputHandler();
    }

    protected DfLReverseOutputHandler createOutputHandler() {
        return new DfLReverseOutputHandlerFactory(_dataSource).createOutputHandler();
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    public void execute() {
        final Database database = prepareDatabase();
        final List<Table> tableList = filterTableList(database);
        final List<String> sectionInfoList = prepareTitleSection(tableList);
        final File baseDir = prepareBaseDir();
        final Map<File, DfLReverseOutputResource> orderedMap = prepareOrderedMap(tableList, baseDir);

        reverseTableData(orderedMap, baseDir, sectionInfoList);
        outputTableNameMap();

        synchronizeOriginDateIfNeeds(sectionInfoList);
        outputResultMark(sectionInfoList);
    }

    // -----------------------------------------------------
    //                                               Prepare
    //                                               -------
    protected Database prepareDatabase() {
        return new DfLReverseSchemaMetaProvider(_dataSource).prepareDatabase();
    }

    protected List<Table> filterTableList(Database database) {
        return new DfLReverseSchemaTableFilter(_dataSource, _tableNameProp, _skippedTableList).filterTableList(database);
    }

    protected List<String> prepareTitleSection(List<Table> tableList) {
        return new DfLReverseTitleSectionProvider().prepareTitleSection(tableList);
    }

    protected File prepareBaseDir() {
        final File baseDir = new File(getReverseXlsDataDir());
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        return baseDir;
    }

    protected Map<File, DfLReverseOutputResource> prepareOrderedMap(List<Table> tableList, File baseDir) {
        return new DfLReverseFileOrder(_dataSource, _tableNameProp, _skippedTableList).prepareOrderedMap(tableList, baseDir);
    }

    // ===================================================================================
    //                                                                       Reverse Table
    //                                                                       =============
    protected void reverseTableData(Map<File, DfLReverseOutputResource> orderedMap, File baseDir, List<String> sectionInfoList) {
        new DfLReverseTableData(_dataSource, _outputHandler).reverseTableData(orderedMap, baseDir, sectionInfoList);
    }

    // ===================================================================================
    //                                                                      Table Name Map
    //                                                                      ==============
    protected void outputTableNameMap() {
        final Map<String, Table> tableNameMap = _outputHandler.getTableNameMap();
        if (tableNameMap.isEmpty()) {
            return;
        }
        _log.info("...Outputting table name map for reversed tables");
        _tableNameProp.outputTableNameMap(getReverseXlsDataDir(), tableNameMap);
    }

    // ===================================================================================
    //                                                              Synchronize OriginDate
    //                                                              ======================
    protected void synchronizeOriginDateIfNeeds(List<String> sectionInfoList) {
        if (!isSynchronizeOriginDate()) {
            return;
        }
        _log.info("...Synchronizing origin date for date adjustment");
        final String dataDir = getReverseXlsDataDir();
        final DfLReverseOriginDateSynchronizer synchronizer = new DfLReverseOriginDateSynchronizer();
        final String syncResult = synchronizer.synchronizeOriginDate(dataDir);
        _log.info("  df:originDate: " + syncResult);
        sectionInfoList.add("");
        sectionInfoList.add("[loadingControlMap.dataprop]");
        sectionInfoList.add("df:originDate: " + syncResult);
    }

    // ===================================================================================
    //                                                                         Result Mark
    //                                                                         ===========
    protected void outputResultMark(List<String> sectionInfoList) {
        _log.info("...Outputting result mark for reversed data");
        final StringBuilder sb = new StringBuilder();
        sb.append(ln()).append("* * * * * * * * * * *");
        sb.append(ln()).append("*                   *");
        sb.append(ln()).append("* Load Data Reverse *");
        sb.append(ln()).append("*                   *");
        sb.append(ln()).append("* * * * * * * * * * *");
        for (String sectionInfo : sectionInfoList) {
            sb.append(ln()).append(sectionInfo);
        }
        final Date currentDate = DfTypeUtil.toDate(DBFluteSystem.currentTimeMillis());
        final String currentExp = DfTypeUtil.toString(currentDate, "yyyy/MM/dd HH:mm:ss");
        sb.append(ln()).append(ln()).append("Output Date: ").append(currentExp);
        final File dataPropFile = new File(getReverseXlsDataDir() + "/reverse-data-result.dfmark");
        if (dataPropFile.exists()) {
            dataPropFile.delete();
        }
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dataPropFile), "UTF-8"));
            bw.write(sb.toString());
            bw.flush();
        } catch (IOException e) {
            String msg = "Failed to write reverse-data-result.dfmark: " + dataPropFile;
            throw new DfLReverseProcessFailureException(msg, e);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ignored) {}
            }
        }
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
    protected String getReverseXlsDataDir() {
        return getDocumentProperties().getLoadDataReverseXlsDataDir();
    }

    protected String getDelimiterDataDir() {
        return getDocumentProperties().getLoadDataReverseDelimiterDataDir();
    }

    // -----------------------------------------------------
    //                                          Basic Option
    //                                          ------------
    protected boolean isSynchronizeOriginDate() {
        return getDocumentProperties().isLoadDataReverseSynchronizeOriginDate();
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return DBFluteSystem.ln();
    }

    protected String resolvePath(File file) {
        return Srl.replace(file.getPath(), "\\", "/");
    }
}
