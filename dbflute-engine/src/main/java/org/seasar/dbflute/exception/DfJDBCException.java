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
package org.seasar.dbflute.exception;

import java.sql.SQLException;

/**
 * @author jflute
 */
public class DfJDBCException extends SQLException {

    private static final long serialVersionUID = 1L;

    public static final int NULL_ERROR_CODE = Integer.MIN_VALUE;

    public DfJDBCException(String msg) {
        super(msg, null, NULL_ERROR_CODE);
    }

    public DfJDBCException(String msg, SQLException e) {
        super(msg, e.getSQLState(), e.getErrorCode());
        setNextException(e);
    }

    public static String extractMessage(SQLException e) {
        String message = e.getMessage();

        // Because a message of Oracle contains a line separator.
        return message != null ? message.trim() : message;
    }

    public static String extractSQLState(SQLException e) {
        String sqlState = e.getSQLState();
        if (sqlState != null) {
            return sqlState;
        }

        // Next
        SQLException nextEx = e.getNextException();
        if (nextEx == null) {
            return null;
        }
        sqlState = nextEx.getSQLState();
        if (sqlState != null) {
            return sqlState;
        }

        // Next Next
        SQLException nextNextEx = nextEx.getNextException();
        if (nextNextEx == null) {
            return null;
        }
        sqlState = nextNextEx.getSQLState();
        if (sqlState != null) {
            return sqlState;
        }

        // Next Next Next
        SQLException nextNextNextEx = nextNextEx.getNextException();
        if (nextNextNextEx == null) {
            return null;
        }
        sqlState = nextNextNextEx.getSQLState();
        if (sqlState != null) {
            return sqlState;
        }

        // It doesn't use recursive call by design because JDBC is unpredictable fellow.
        return null;
    }

    public static Integer extractErrorCode(SQLException e) {
        // this SQLException may be DBFlute's original exception
        final int nullErrorCode = DfJDBCException.NULL_ERROR_CODE;
        int errorCode = e.getErrorCode();
        if (errorCode != nullErrorCode) {
            return errorCode;
        }

        // Next
        SQLException nextEx = e.getNextException();
        if (nextEx == null) {
            return null;
        }
        errorCode = nextEx.getErrorCode();
        if (errorCode != nullErrorCode) {
            return errorCode;
        }

        // Next Next
        SQLException nextNextEx = nextEx.getNextException();
        if (nextNextEx == null) {
            return null;
        }
        errorCode = nextNextEx.getErrorCode();
        if (errorCode != nullErrorCode) {
            return errorCode;
        }

        // Next Next Next
        SQLException nextNextNextEx = nextNextEx.getNextException();
        if (nextNextNextEx == null) {
            return null;
        }
        errorCode = nextNextNextEx.getErrorCode();
        if (errorCode != nullErrorCode) {
            return errorCode;
        }

        // It doesn't use recursive call by design because JDBC is unpredictable fellow.
        return null;
    }
}
