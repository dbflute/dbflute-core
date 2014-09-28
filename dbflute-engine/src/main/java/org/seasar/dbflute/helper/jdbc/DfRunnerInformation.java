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
package org.seasar.dbflute.helper.jdbc;

/**
 * @author jflute
 */
public class DfRunnerInformation {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String _driver;
    protected String _url;
    protected String _user;
    protected String _password;
    protected String _delimiter = ";";
    protected boolean _breakCauseThrow;
    protected boolean _errorContinue;
    protected boolean _autoCommit;
    protected boolean _rollbackOnly;
    protected boolean _ignoreTxError;
    protected boolean _suppressLoggingSql;
    protected String _encoding;

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getDriver() {
        return _driver;
    }

    public void setDriver(String driver) {
        this._driver = driver;
    }

    public String getUrl() {
        return _url;
    }

    public void setUrl(String url) {
        this._url = url;
    }

    public String getUser() {
        return _user;
    }

    public void setUser(String user) {
        this._user = user;
    }

    public String getPassword() {
        return _password;
    }

    public void setPassword(String password) {
        this._password = password;
    }

    public boolean isEncodingNull() {
        return (_encoding == null);
    }

    public String getDelimiter() {
        return _delimiter;
    }

    public void setDelimiter(String delimiter) {
        this._delimiter = delimiter;
    }

    public boolean isBreakCauseThrow() {
        return _breakCauseThrow;
    }

    public void setBreakCauseThrow(boolean breakCauseThrow) {
        this._breakCauseThrow = breakCauseThrow;
    }

    public boolean isErrorContinue() {
        return _errorContinue;
    }

    public void setErrorContinue(boolean errorContinue) {
        this._errorContinue = errorContinue;
    }

    public boolean isAutoCommit() {
        return _autoCommit;
    }

    public void setAutoCommit(boolean autoCommit) {
        this._autoCommit = autoCommit;
    }

    public boolean isRollbackOnly() {
        return _rollbackOnly;
    }

    public void setRollbackOnly(boolean rollbackOnly) {
        this._rollbackOnly = rollbackOnly;
    }

    public boolean isIgnoreTxError() {
        return _ignoreTxError;
    }

    public void setIgnoreTxError(boolean ignoreTxError) {
        this._ignoreTxError = ignoreTxError;
    }

    public boolean isSuppressLoggingSql() {
        return _suppressLoggingSql;
    }

    public void setSuppressLoggingSql(boolean suppressLoggingSql) {
        this._suppressLoggingSql = suppressLoggingSql;
    }

    public String getEncoding() {
        return _encoding;
    }

    public void setEncoding(String encoding) {
        this._encoding = encoding;
    }
}
