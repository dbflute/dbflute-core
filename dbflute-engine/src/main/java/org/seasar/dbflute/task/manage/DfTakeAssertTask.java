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
package org.seasar.dbflute.task.manage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.exception.DfTakeAssertAssertionFailureException;
import org.seasar.dbflute.exception.DfTakeAssertFailureException;
import org.seasar.dbflute.exception.DfTakeFinallyAssertionFailureException;
import org.seasar.dbflute.exception.SQLFailureException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.logic.replaceschema.finalinfo.DfTakeFinallyFinalInfo;
import org.seasar.dbflute.logic.replaceschema.process.DfTakeFinallyProcess;
import org.seasar.dbflute.task.DfDBFluteTaskStatus;
import org.seasar.dbflute.task.DfDBFluteTaskStatus.TaskType;
import org.seasar.dbflute.task.bs.DfAbstractTask;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.9.9.1A (2011/10/06 Thursday)
 */
public class DfTakeAssertTask extends DfAbstractTask {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log _log = LogFactory.getLog(DfTakeAssertTask.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String _sqlRootDir; // is set by its property (option)
    protected DfTakeFinallyFinalInfo _finalInfo; // is set after execution
    protected List<DfTakeFinallyAssertionFailureException> _takeAssertExList; // is set after execution

    // ===================================================================================
    //                                                                           Beginning
    //                                                                           =========
    @Override
    protected boolean begin() {
        _log.info("+------------------------------------------+");
        _log.info("|                                          |");
        _log.info("|               Take Assert                |");
        _log.info("|                                          |");
        _log.info("+------------------------------------------+");
        DfDBFluteTaskStatus.getInstance().setTaskType(TaskType.TakeAssert);
        return true;
    }

    // ===================================================================================
    //                                                                          DataSource
    //                                                                          ==========
    @Override
    protected boolean isUseDataSource() {
        return true;
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Override
    protected void doExecute() {
        final String sqlRootDir = Srl.is_NotNull_and_NotTrimmedEmpty(_sqlRootDir) ? _sqlRootDir : "./playsql";
        final DfTakeFinallyProcess process = DfTakeFinallyProcess.createAsTakeAssert(sqlRootDir, getDataSource());
        _finalInfo = process.execute();
        final SQLFailureException breakCause = _finalInfo.getBreakCause();
        if (breakCause != null) { // high priority exception
            throw breakCause;
        }

        // get exceptions from this method when take-assert
        // (then the finalInfo does not have an exception)
        _takeAssertExList = process.getTakeAssertExList();

        handleSQLFailure();
        handleAssertionFailure(sqlRootDir);
    }

    protected void handleSQLFailure() {
        if (_takeAssertExList.isEmpty() && _finalInfo.isFailure()) { // means SQL failure
            String msg = "Failed to take assert (Look at the final info)";
            throw new DfTakeAssertFailureException(msg);
        }
    }

    protected void handleAssertionFailure(String sqlRootDir) {
        if (_takeAssertExList.isEmpty()) {
            _log.info("*All assertions are successful");
            return;
        }
        dumpAssertionFailure(_takeAssertExList);
        throwTakeAssertAssertionFailureException(_takeAssertExList);
    }

    protected void throwTakeAssertAssertionFailureException(
            List<DfTakeFinallyAssertionFailureException> takeAssertExList) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Assertion failures were found.");
        br.addItem("Advice");
        br.addElement("Look at the take-assert.log in the log directory of the DBFlute client.");
        br.addElement("The log file has detail messages about the failures.");
        br.addItem("Failure Count");
        br.addElement(takeAssertExList.size());
        final String msg = br.buildExceptionMessage();
        throw new DfTakeAssertAssertionFailureException(msg);
    }

    protected void dumpAssertionFailure(List<DfTakeFinallyAssertionFailureException> takeAssertExList) {
        final File dumpFile = new File("./log/take-assert.log");
        if (dumpFile.exists()) {
            dumpFile.delete();
        }
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dumpFile), "UTF-8"));
            for (DfTakeFinallyAssertionFailureException assertionEx : takeAssertExList) {
                bw.write(assertionEx.getMessage());
                bw.write(ln() + ln());
            }
            bw.flush();
        } catch (IOException e) {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    // ===================================================================================
    //                                                                          Final Info
    //                                                                          ==========
    @Override
    protected String getFinalInformation() {
        return buildFinalMessage();
    }

    protected String buildFinalMessage() {
        final DfTakeFinallyFinalInfo finalInfo = _finalInfo; // might be null
        final List<DfTakeFinallyAssertionFailureException> takeAssertExList = _takeAssertExList;
        final StringBuilder sb = new StringBuilder();

        if (finalInfo != null) {
            if (finalInfo.isValidInfo()) {
                buildSchemaTaskContents(sb, finalInfo);
            }
            if (takeAssertExList != null && !takeAssertExList.isEmpty()) {
                sb.append(ln()).append("    * * * * * * * * * * *");
                sb.append(ln()).append("    * Assertion Failure *");
                sb.append(ln()).append("    * * * * * * * * * * *");
            }
        }
        return sb.toString();
    }

    protected void buildSchemaTaskContents(StringBuilder sb, DfTakeFinallyFinalInfo finalInfo) {
        sb.append(" ").append(finalInfo.getResultMessage());
        final List<String> detailMessageList = finalInfo.getDetailMessageList();
        for (String detailMessage : detailMessageList) {
            sb.append(ln()).append("  ").append(detailMessage);
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setSqlRootDir(String sqlRootDir) {
        if (Srl.is_Null_or_TrimmedEmpty(sqlRootDir)) {
            return;
        }
        if (sqlRootDir.equals("${dfdir}")) {
            return;
        }
        _sqlRootDir = sqlRootDir;
    }
}
