/*
 * Copyright 2014-2014 the original author or authors.
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
package org.dbflute.outsidesql;

import org.dbflute.jdbc.StatementConfig;

/**
 * The option of outside-SQL. It contains various information about execution.
 * @author jflute
 */
public class OutsideSqlOption {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                                Option
    //                                                ------
    /** The request type of paging. */
    protected String _pagingRequestType = "non";

    protected boolean _removeBlockComment;
    protected boolean _removeLineComment;
    protected boolean _formatSql;
    protected boolean _nonSpecifiedColumnAccessAllowed;

    /** The configuration of statement specified by configure(). (NullAllowed) */
    protected StatementConfig _statementConfig;

    protected String _sourcePagingRequestType = "non";

    // -----------------------------------------------------
    //                                           Information
    //                                           -----------
    /** The DB name of table. It is not related with the options of outside-SQL. */
    protected String _tableDbName;

    // ===================================================================================
    //                                                                         Easy-to-Use
    //                                                                         ===========
    public OutsideSqlOption autoPaging() {
        _pagingRequestType = "auto";
        return this;
    }

    public OutsideSqlOption manualPaging() {
        _pagingRequestType = "manual";
        return this;
    }

    public OutsideSqlOption removeBlockComment() {
        _removeBlockComment = true;
        return this;
    }

    public OutsideSqlOption removeLineComment() {
        _removeLineComment = true;
        return this;
    }

    public OutsideSqlOption formatSql() {
        _formatSql = true;
        return this;
    }

    public OutsideSqlOption enableNonSpecifiedColumnAccess() {
        _nonSpecifiedColumnAccessAllowed = true;
        return this;
    }

    // ===================================================================================
    //                                                                          Unique Key
    //                                                                          ==========
    public String generateUniqueKey() {
        // these options are used only when outside-SQL initialization
        // for example, statementConfig is used after initialization
        // so the instance is not needed to be contained in this unique key
        return "{" + _pagingRequestType + "/" + _removeBlockComment + "/" + _removeLineComment + "/" + _formatSql + "}";
    }

    // ===================================================================================
    //                                                                                Copy
    //                                                                                ====
    public OutsideSqlOption copyOptionForPagingCount() {
        final OutsideSqlOption copyOption = new OutsideSqlOption();
        copyOption.setPagingSourceRequestType(_pagingRequestType);
        copyOption.setTableDbName(_tableDbName);
        if (_removeBlockComment) {
            copyOption.removeBlockComment();
        }
        if (_removeLineComment) {
            copyOption.removeLineComment();
        }
        if (_formatSql) {
            copyOption.formatSql();
        }
        if (_nonSpecifiedColumnAccessAllowed) {
            copyOption.enableNonSpecifiedColumnAccess();
        }
        // inherit only queryTimeout (others are basically not related to count select)
        if (_statementConfig != null && _statementConfig.hasQueryTimeout()) {
            final Integer queryTimeout = _statementConfig.getQueryTimeout();
            copyOption.setStatementConfig(new StatementConfig().queryTimeout(queryTimeout));
        }
        return copyOption;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{").append("paging=").append(_pagingRequestType);
        if (_statementConfig != null) {
            if (_statementConfig.hasResultSetType()) {
                sb.append(", resultSet=").append(_statementConfig.buildResultSetTypeDisp());
            }
            if (_statementConfig.hasQueryTimeout()) {
                sb.append(", timeout=").append(_statementConfig.getQueryTimeout());
            }
            if (_statementConfig.hasFetchSize()) {
                sb.append(", fetchSize=").append(_statementConfig.getFetchSize());
            }
            if (_statementConfig.hasMaxRows()) {
                sb.append(", maxRows=").append(_statementConfig.getMaxRows());
            }
        } else {
            sb.append(", config=default");
        }
        if (_tableDbName != null) {
            sb.append(", related to table");
        }
        sb.append("}");
        return sb.toString();
        // not show formatSql and other comment adjustments because of not important
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    // -----------------------------------------------------
    //                                                Option
    //                                                ------
    public boolean isAutoPaging() {
        return "auto".equals(_pagingRequestType);
    }

    public boolean isManualPaging() {
        return "manual".equals(_pagingRequestType);
    }

    public boolean isRemoveBlockComment() {
        return _removeBlockComment;
    }

    public boolean isRemoveLineComment() {
        return _removeLineComment;
    }

    public boolean isFormatSql() {
        return _formatSql;
    }

    public boolean isNonSpecifiedColumnAccessAllowed() {
        return _nonSpecifiedColumnAccessAllowed;
    }

    public StatementConfig getStatementConfig() {
        return _statementConfig;
    }

    public void setStatementConfig(StatementConfig statementConfig) {
        _statementConfig = statementConfig;
    }

    protected void setPagingSourceRequestType(String sourcePagingRequestType) { // very internal
        _sourcePagingRequestType = sourcePagingRequestType;
    }

    public boolean isSourcePagingRequestTypeAuto() { // very internal
        return "auto".equals(_sourcePagingRequestType);
    }

    // -----------------------------------------------------
    //                                           Information
    //                                           -----------
    public String getTableDbName() {
        return _tableDbName;
    }

    public void setTableDbName(String tableDbName) {
        _tableDbName = tableDbName;
    }
}
