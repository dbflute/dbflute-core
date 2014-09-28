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
package org.seasar.dbflute.logic.replaceschema.process;

import java.util.ArrayList;
import java.util.List;

import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.helper.jdbc.DfRunnerInformation;
import org.seasar.dbflute.helper.jdbc.sqlfile.DfSqlFileFireResult;
import org.seasar.dbflute.helper.token.line.LineToken;
import org.seasar.dbflute.helper.token.line.LineTokenizingOption;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.properties.DfDatabaseProperties;
import org.seasar.dbflute.properties.DfDocumentProperties;
import org.seasar.dbflute.properties.DfReplaceSchemaProperties;
import org.seasar.dbflute.properties.facade.DfDatabaseTypeFacadeProp;

/**
 * @author jflute
 * @since 0.9.8.3 (2011/04/29 Friday)
 */
public class DfAbstractReplaceSchemaProcess {

    // ===================================================================================
    //                                                                     SQL File Runner
    //                                                                     ===============
    protected DfRunnerInformation createRunnerInformation() {
        final DfRunnerInformation runInfo = new DfRunnerInformation();
        final DfDatabaseProperties prop = getDatabaseProperties();
        runInfo.setDriver(prop.getDatabaseDriver());
        runInfo.setUrl(prop.getDatabaseUrl());
        runInfo.setUser(prop.getDatabaseUser());
        runInfo.setPassword(prop.getDatabasePassword());
        runInfo.setEncoding(getSqlFileEncoding());
        runInfo.setBreakCauseThrow(false); // save break cause to prepare messages
        runInfo.setErrorContinue(isErrorContinue());
        if (isRollbackTransaction()) { // basically take-assert
            runInfo.setAutoCommit(false);
            runInfo.setRollbackOnly(true);
        } else { // mainly here
            runInfo.setAutoCommit(true);
            runInfo.setRollbackOnly(false);
        }
        runInfo.setSuppressLoggingSql(isSuppressLoggingReplaceSql());
        return runInfo;
    }

    protected String getSqlFileEncoding() {
        return getReplaceSchemaProperties().getSqlFileEncoding();
    }

    protected boolean isErrorContinue() {
        return getReplaceSchemaProperties().isErrorSqlContinue();
    }

    protected boolean isRollbackTransaction() {
        return false; // as default
    }

    protected boolean isSuppressLoggingReplaceSql() {
        return getReplaceSchemaProperties().isSuppressLoggingReplaceSql();
    }

    protected String resolveTerminator4Tool() {
        return getReplaceSchemaProperties().resolveTerminator4Tool();
    }

    protected boolean isDbCommentLineForIrregularPattern(String line) {
        // for irregular pattern
        line = line.trim().toLowerCase();
        if (getDatabaseTypeFacadeProp().isDatabaseMySQL()) {
            if (line.contains("comment='") || line.contains("comment = '") || line.contains(" comment '")) {
                return true;
            }
        }
        if (getDatabaseTypeFacadeProp().isDatabaseSQLServer()) {
            if (line.startsWith("exec sys.sp_addextendedproperty @name=n'ms_description'")) {
                return true;
            }
        }
        return false;
    }

    // ===================================================================================
    //                                                                          Final Info
    //                                                                          ==========
    protected List<String> extractDetailMessageList(DfSqlFileFireResult fireResult) {
        final List<String> detailMessageList = new ArrayList<String>();
        final String detailMessage = fireResult.getDetailMessage();
        if (detailMessage != null && detailMessage.trim().length() > 0) {
            final LineToken lineToken = new LineToken();
            final LineTokenizingOption lineTokenizingOption = new LineTokenizingOption();
            lineTokenizingOption.setDelimiter(ln());
            final List<String> tokenizedList = lineToken.tokenize(detailMessage, lineTokenizingOption);
            for (String tokenizedElement : tokenizedList) {
                detailMessageList.add(tokenizedElement);
            }
        }
        return detailMessageList;
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected static DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected static DfReplaceSchemaProperties getReplaceSchemaProperties() {
        return getProperties().getReplaceSchemaProperties();
    }

    protected static DfBasicProperties getBasicProperties() {
        return getProperties().getBasicProperties();
    }

    protected static DfDatabaseProperties getDatabaseProperties() {
        return getProperties().getDatabaseProperties();
    }

    protected static DfDatabaseTypeFacadeProp getDatabaseTypeFacadeProp() {
        return getBasicProperties().getDatabaseTypeFacadeProp();
    }

    protected static DfDocumentProperties getDocumentProperties() {
        return getProperties().getDocumentProperties();
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return "\n";
    }
}
