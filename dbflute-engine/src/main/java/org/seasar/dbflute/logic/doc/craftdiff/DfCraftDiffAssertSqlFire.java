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
package org.seasar.dbflute.logic.doc.craftdiff;

import java.io.File;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.exception.DfCraftDiffNonAssertionSqlFoundException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.jdbc.DfRunnerInformation;
import org.seasar.dbflute.helper.jdbc.context.DfSchemaSource;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileFireMan;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileRunner;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileRunnerDispatcher;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileRunnerExecute;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileRunnerExecute.DfRunnerDispatchResult;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfTableMeta;
import org.seasar.dbflute.properties.DfDatabaseProperties;
import org.seasar.dbflute.properties.DfDocumentProperties;
import org.seasar.dbflute.properties.DfReplaceSchemaProperties;

/**
 * @author jflute
 * @since 0.9.9.8 (2012/09/04 Tuesday)
 */
public class DfCraftDiffAssertSqlFire {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                        Basic Resource
    //                                        --------------
    protected final DfSchemaSource _dataSource;
    protected final String _craftMetaDir;
    protected final DfCraftDiffAssertDirection _assertDirection;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfCraftDiffAssertSqlFire(DfSchemaSource dataSource, String craftMetaDir,
            DfCraftDiffAssertDirection assertDirection) {
        _dataSource = dataSource;
        _craftMetaDir = craftMetaDir;
        _assertDirection = assertDirection;
    }

    // ===================================================================================
    //                                                                            SQL Fire
    //                                                                            ========
    /**
     * Fire the SQLs for assertion.
     * @param tableList The list of table meta, e.g. to auto-generate SQL for table-equals. (NotNull)
     */
    public void fire(List<DfTableMeta> tableList) {
        final List<File> craftSqlFileList = getCraftSqlFileList();
        if (craftSqlFileList.isEmpty()) {
            return;
        }
        final DfRunnerInformation runInfo = createRunnerInformation();
        final DfSqlFileFireMan fireMan = new DfSqlFileFireMan();
        fireMan.setExecutorName("Craft Diff");

        // result file is ignored because of break cause
        final DfCraftDiffAssertProvider provider = createAssertProvider(tableList);
        fireMan.fire(getSqlFileRunner4CraftDiff(runInfo, provider), craftSqlFileList);
    }

    protected DfCraftDiffAssertProvider createAssertProvider(List<DfTableMeta> tableList) {
        return new DfCraftDiffAssertProvider(_craftMetaDir, _assertDirection, tableList);
    }

    protected DfSqlFileRunner getSqlFileRunner4CraftDiff(final DfRunnerInformation runInfo,
            final DfCraftDiffAssertProvider provider) {
        final DfSqlFileRunnerExecute runnerExecute = new DfSqlFileRunnerExecute(runInfo, _dataSource) {
            @Override
            protected String getTerminator4Tool() {
                return resolveTerminator4Tool();
            }

            @Override
            protected boolean isTargetFile(String sql) {
                return isTargetEnvTypeFile(sql);
            }
        };
        runnerExecute.setDispatcher(new DfSqlFileRunnerDispatcher() {
            public DfRunnerDispatchResult dispatch(File sqlFile, Statement st, String sql) throws SQLException {
                final DfCraftDiffAssertHandler handler = provider.provideCraftDiffAssertHandler(sqlFile, sql);
                if (handler == null) {
                    throwCraftDiffNonAssertionSqlFoundException(sqlFile, sql);
                }
                handler.handle(sqlFile, st, sql);
                return DfRunnerDispatchResult.DISPATCHED;
            }
        });
        return runnerExecute;
    }

    protected void throwCraftDiffNonAssertionSqlFoundException(File sqlFile, String sql) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Found the non-assertion SQL.");
        br.addItem("Advice");
        br.addElement("Confirm your SQL files for CraftDiff.");
        br.addElement("For example, non-assertion SQL on CraftDiff is restricted.");
        br.addItem("SQL File");
        br.addElement(sqlFile.getPath());
        br.addItem("non-assertion SQL");
        br.addElement(sql);
        final String msg = br.buildExceptionMessage();
        throw new DfCraftDiffNonAssertionSqlFoundException(msg);
    }

    // ===================================================================================
    //                                                                  Runner Information
    //                                                                  ==================
    protected DfRunnerInformation createRunnerInformation() {
        final DfRunnerInformation runInfo = new DfRunnerInformation();
        final DfDatabaseProperties prop = getDatabaseProperties();
        runInfo.setDriver(prop.getDatabaseDriver());
        runInfo.setUrl(prop.getDatabaseUrl());
        runInfo.setUser(prop.getDatabaseUser());
        runInfo.setPassword(prop.getDatabasePassword());
        runInfo.setEncoding(getSqlFileEncoding());
        runInfo.setBreakCauseThrow(true);
        runInfo.setErrorContinue(false);
        runInfo.setAutoCommit(false);
        runInfo.setRollbackOnly(true);
        runInfo.setSuppressLoggingSql(false);
        return runInfo;
    }

    protected String getSqlFileEncoding() {
        return getReplaceSchemaProperties().getSqlFileEncoding(); // same as ReplaceSchema
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfDatabaseProperties getDatabaseProperties() {
        return getProperties().getDatabaseProperties();
    }

    protected DfDocumentProperties getDocumentProperties() {
        return getProperties().getDocumentProperties();
    }

    protected List<File> getCraftSqlFileList() {
        return getDocumentProperties().getCraftSqlFileList();
    }

    protected DfReplaceSchemaProperties getReplaceSchemaProperties() {
        return getProperties().getReplaceSchemaProperties();
    }

    protected String resolveTerminator4Tool() {
        return getReplaceSchemaProperties().resolveTerminator4Tool();
    }

    protected boolean isTargetEnvTypeFile(String sql) {
        return getReplaceSchemaProperties().isTargetEnvTypeFile(sql);
    }
}
