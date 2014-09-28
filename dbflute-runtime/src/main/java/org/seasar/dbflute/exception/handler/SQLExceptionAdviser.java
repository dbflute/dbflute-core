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
package org.seasar.dbflute.exception.handler;

import java.sql.SQLException;

import org.seasar.dbflute.DBDef;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.9.9.1G (2011/11/17 Thursday)
 */
public class SQLExceptionAdviser {

    public String askAdvice(SQLException sqlEx, DBDef dbdef) {
        if (sqlEx == null || dbdef == null) { // just in case
            return null;
        }
        if (DBDef.MySQL.equals(dbdef)) {
            if (hasMessageHint(sqlEx, "Communications link failure")) {
                return "And also check the MySQL bootstrap and network.";
            } else if (hasMessageHint(sqlEx, "Field", "doesn't have a default value")) {
                return "And also check the insert values to not-null columns.";
            } else if (hasMessageHint(sqlEx, "Column", "cannot be null")) {
                return "And also check the update values to not-null columns.";
            }
        } else if (DBDef.DB2.equals(dbdef)) {
            if (hasMessageHint(sqlEx, "SQLCODE=-302")) {
                return "Is it column size-over?";
            } else if (hasMessageHint(sqlEx, "SQLCODE=-407")) {
                return "Is it not-null constraint?";
            } else if (hasMessageHint(sqlEx, "SQLCODE=-530")) {
                return "Is it FK constraint?";
            } else if (hasMessageHint(sqlEx, "SQLCODE=-952")) {
                return "Is it timeout?";
            }
        }
        return null;
    }

    protected boolean hasMessageHint(SQLException sqlEx, String... hints) {
        String msg = sqlEx.getMessage();
        if (msg != null && Srl.containsAll(msg, hints)) {
            return true;
        }
        final SQLException nextEx = sqlEx.getNextException();
        if (nextEx == null) {
            return false;
        }
        msg = nextEx.getMessage();
        if (msg != null && Srl.containsAll(msg, hints)) {
            return true;
        }
        final SQLException nextNextEx = nextEx.getNextException();
        if (nextNextEx == null) {
            return false;
        }
        msg = nextNextEx.getMessage();
        if (msg != null && Srl.containsAll(msg, hints)) {
            return true;
        }

        // It doesn't use recursive call by design because JDBC is unpredictable fellow.
        return false;
    }
}
