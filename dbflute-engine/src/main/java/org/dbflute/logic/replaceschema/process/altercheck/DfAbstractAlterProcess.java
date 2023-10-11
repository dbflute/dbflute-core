/*
 * Copyright 2014-2023 the original author or authors.
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
package org.dbflute.logic.replaceschema.process.altercheck;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.dbflute.exception.DfAlterCheckDataSourceNotFoundException;
import org.dbflute.helper.jdbc.context.DfSchemaSource;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.infra.core.logic.DfSchemaResourceFinder;
import org.dbflute.logic.replaceschema.process.DfAbstractRepsProcess;
import org.dbflute.logic.replaceschema.process.altercheck.agent.DfAlterControlAgent;
import org.dbflute.logic.replaceschema.process.altercheck.agent.DfHistoryZipAgent;
import org.dbflute.logic.replaceschema.process.altercheck.agent.DfPreviousDBAgent;
import org.dbflute.logic.replaceschema.process.altercheck.agent.DfUnreleasedAlterAgent;
import org.dbflute.logic.replaceschema.process.altercheck.player.DfAlterCoreProcessPlayer;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.9.8.3 (2011/04/29 Friday)
 */
public abstract class DfAbstractAlterProcess extends DfAbstractRepsProcess {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                        Basic Resource
    //                                        --------------
    protected final DfSchemaSource _dataSource;
    protected final DfAlterCoreProcessPlayer _coreProcessPlayer;

    // -----------------------------------------------------
    //                                             Supporter
    //                                             ---------
    protected final DfAlterControlAgent _alterControlAgent;
    protected final DfHistoryZipAgent _historyZipAgent;
    protected final DfPreviousDBAgent _previousDBAgent;
    protected final DfUnreleasedAlterAgent _unreleasedAlterAgent;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    protected DfAbstractAlterProcess(DfSchemaSource dataSource, DfAlterCoreProcessPlayer coreProcessPlayer) {
        if (dataSource == null) { // for example, ReplaceSchema may have lazy connection
            throwAlterCheckDataSourceNotFoundException();
        }
        _dataSource = dataSource;
        _coreProcessPlayer = coreProcessPlayer;

        _alterControlAgent = new DfAlterControlAgent();
        _historyZipAgent = new DfHistoryZipAgent();
        _previousDBAgent = new DfPreviousDBAgent(coreProcessPlayer);
        _unreleasedAlterAgent = new DfUnreleasedAlterAgent(coreProcessPlayer);
    }

    protected void throwAlterCheckDataSourceNotFoundException() {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the data source for AlterCheck.");
        br.addItem("Advice");
        br.addElement("Make sure your database process works");
        br.addElement("or your connection settings are correct.");
        String msg = br.buildExceptionMessage();
        throw new DfAlterCheckDataSourceNotFoundException(msg);
    }

    // ===================================================================================
    //                                                                  PreviousDB Command
    //                                                                  ==================
    // -----------------------------------------------------
    //                                       Previous Schema
    //                                       ---------------
    protected void playPreviousSchema() {
        _previousDBAgent.playPreviousSchema();
    }

    // -----------------------------------------------------
    //                                       PreviousNG Mark
    //                                       ---------------
    protected void markPreviousNG(String notice) {
        _previousDBAgent.markPreviousNG(notice);
    }

    // ===================================================================================
    //                                                                 PreviousDB Resource
    //                                                                 ===================
    // -----------------------------------------------------
    //                                      Extract Resource
    //                                      ----------------
    protected boolean extractPreviousResource() {
        return _previousDBAgent.extractPreviousResource();
    }

    // -----------------------------------------------------
    //                                       Delete Resource
    //                                       ---------------
    protected void deleteExtractedPreviousResource() {
        _previousDBAgent.deleteExtractedPreviousResource();
    }

    // ===================================================================================
    //                                                                    Control Resource
    //                                                                    ================
    // -----------------------------------------------------
    //                                           Delete Mark
    //                                           -----------
    protected void deleteAllNGMark() {
        _alterControlAgent.deleteAllNGMark();
    }

    // -----------------------------------------------------
    //                                        File Operation
    //                                        --------------
    protected void deleteControlFile(File file, String msg) {
        _alterControlAgent.deleteMarkFile(file, msg);
    }

    protected void writeControlLogRoad(File file, String notice) throws IOException {
        _alterControlAgent.writeMarkLogRoad(file, notice);
    }

    protected void writeControlLogRoad(File file, String notice, Map<String, Object> metaMap) throws IOException {
        _alterControlAgent.writeMarkLogRoad(file, notice, metaMap);
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String resolvePath(File file) {
        return Srl.replace(file.getPath(), "\\", "/");
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    // -----------------------------------------------------
    //                                         ReplaceSchema
    //                                         -------------
    protected String getPlaySqlDir() {
        return getReplaceSchemaProperties().getPlaySqlDir();
    }

    // -----------------------------------------------------
    //                                        Alter Resource
    //                                        --------------
    protected String getMigrationAlterDirectory() {
        return getReplaceSchemaProperties().getMigrationAlterDirectory();
    }

    protected DfSchemaResourceFinder createBasicAlterSqlFileFinder() {
        return getReplaceSchemaProperties().createMigrationBasicAlterSqlFileFinder();
    }
}
