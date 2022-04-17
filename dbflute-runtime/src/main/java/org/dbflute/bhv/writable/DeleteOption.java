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
package org.dbflute.bhv.writable;

import org.dbflute.cbean.ConditionBean;
import org.dbflute.cbean.coption.StatementConfigCall;
import org.dbflute.dbmeta.info.UniqueInfo;
import org.dbflute.exception.IllegalConditionBeanOperationException;
import org.dbflute.jdbc.StatementConfig;
import org.dbflute.util.DfTypeUtil;

/**
 * The option of delete for varying-delete.
 * @param <CB> The type of condition-bean for specification.
 * @author jflute
 * @since 0.9.7.8 (2010/12/16 Thursday)
 */
public class DeleteOption<CB extends ConditionBean> implements WritableOption<CB> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected UniqueInfo _uniqueByUniqueInfo;
    protected boolean _nonQueryDeleteAllowed;
    protected boolean _queryDeleteForcedDirectAllowed;
    protected Integer _batchLoggingDeleteLimit;
    protected StatementConfig _deleteStatementConfig;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Constructor.
     */
    public DeleteOption() {
    }

    // ===================================================================================
    //                                                                           Unique By
    //                                                                           =========
    /**
     * To be unique by the unique columns of the unique info without values. <br>
     * The values of the unique columns should be in your entity. <br>
     * Usually you can use entity's uniqueOf() so this is basically for interface dispatch world. <br>
     * You can delete the entity by the key when entity delete (NOT batch delete).
     * @param uniqueInfo The unique info of DB meta for natural unique. (NotNull, NotPrimary)
     */
    public void uniqueBy(UniqueInfo uniqueInfo) {
        if (uniqueInfo == null) {
            String msg = "The argument 'uniqueInfo' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (uniqueInfo.isPrimary()) {
            String msg = "The unique info should be natural unique (not primary): " + uniqueInfo;
            throw new IllegalArgumentException(msg);
        }
        _uniqueByUniqueInfo = uniqueInfo;
    }

    public boolean hasUniqueByUniqueInfo() {
        return _uniqueByUniqueInfo != null;
    }

    public UniqueInfo getUniqueByUniqueInfo() {
        return _uniqueByUniqueInfo;
    }

    // ===================================================================================
    //                                                                        Query Delete
    //                                                                        ============
    /**
     * Allow you to non-query-delete (means query-delete without a query condition). <br>
     * Normally it is not allowed, so you can do it by this option if you want.
     * @return The option of delete. (NotNull: returns this)
     */
    public DeleteOption<CB> allowNonQueryDelete() {
        _nonQueryDeleteAllowed = true;
        return this;
    }

    public boolean isNonQueryDeleteAllowed() {
        return _nonQueryDeleteAllowed;
    }

    /**
     * Allow you to use direct clause in query delete forcedly.
     * @return The option of update. (NotNull: returns this)
     */
    public DeleteOption<CB> allowQueryDeleteForcedDirect() {
        _queryDeleteForcedDirectAllowed = true;
        return this;
    }

    public boolean isQueryDeleteForcedDirectAllowed() {
        return _queryDeleteForcedDirectAllowed;
    }

    // ===================================================================================
    //                                                                       Batch Logging
    //                                                                       =============
    /**
     * Limit batch-delete logging by logging size. <br>
     * For example, if you set 3, only 3 records are logged. <br>
     * This also works to SqlLogHandler's call-back and SqlResultInfo's displaySql.
     * @param batchDeleteLoggingLimit The limit size of batch-delete logging. (NullAllowed: if null and minus, means no limit)
     */
    public void limitBatchDeleteLogging(Integer batchDeleteLoggingLimit) {
        this._batchLoggingDeleteLimit = batchDeleteLoggingLimit;
    }

    public Integer getBatchLoggingDeleteLimit() {
        return _batchLoggingDeleteLimit;
    }

    // ===================================================================================
    //                                                                           Configure
    //                                                                           =========
    /**
     * Configure statement JDBC options. e.g. queryTimeout, fetchSize, ... (only one-time call)
     * <pre>
     * <span style="color: #0000C0">memberBhv</span>.varyingDelete(member, <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">op</span>.<span style="color: #CC4747">configure</span>(<span style="color: #553000">conf</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">conf</span>.<span style="color: #994747">queryTimeout</span>(<span style="color: #2A00FF">3</span>)));
     * </pre>
     * @param confLambda The callback for configuration of statement for delete. (NotNull)
     */
    public void configure(StatementConfigCall<StatementConfig> confLambda) {
        assertStatementConfigNotDuplicated(confLambda);
        _deleteStatementConfig = createStatementConfig(confLambda);
    }

    protected void assertStatementConfigNotDuplicated(StatementConfigCall<StatementConfig> configCall) {
        if (_deleteStatementConfig != null) {
            String msg = "Already registered the configuration: existing=" + _deleteStatementConfig + ", new=" + configCall;
            throw new IllegalConditionBeanOperationException(msg);
        }
    }

    protected StatementConfig createStatementConfig(StatementConfigCall<StatementConfig> configCall) {
        if (configCall == null) {
            throw new IllegalArgumentException("The argument 'confLambda' should not be null.");
        }
        final StatementConfig config = newStatementConfig();
        configCall.callback(config);
        return config;
    }

    protected StatementConfig newStatementConfig() {
        return new StatementConfig();
    }

    public StatementConfig getDeleteStatementConfig() {
        return _deleteStatementConfig;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (_nonQueryDeleteAllowed) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("NonQueryDeleteAllowed");
        }
        if (sb.length() == 0) {
            sb.append("default");
        }
        return DfTypeUtil.toClassTitle(this) + ":{" + sb.toString() + "}";
    }
}