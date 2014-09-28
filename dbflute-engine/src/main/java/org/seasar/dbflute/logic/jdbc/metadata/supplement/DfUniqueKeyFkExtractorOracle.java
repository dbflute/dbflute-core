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
package org.seasar.dbflute.logic.jdbc.metadata.supplement;

import java.sql.Connection;
import java.util.List;

/**
 * @author jflute
 * @since 1.0.5A (2013/11/04 Monday)
 */
public class DfUniqueKeyFkExtractorOracle extends DfUniqueKeyFkExtractorBase {

    // ===================================================================================
    //                                                                    Select Meta Data
    //                                                                    ================
    protected List<UserUniqueFkColumn> selectUserUniqueFkList(Connection conn) {
        if (!_unifiedSchema.existsPureSchema()) {
            String msg = "Extracting unique-key FK from Oracle requires pure schema in unified schema:";
            msg = msg + " unifiedSchema=" + _unifiedSchema;
            throw new IllegalStateException(msg);
        }
        final String pureSchema = _unifiedSchema.getPureSchema();
        final StringBuilder sb = new StringBuilder();
        sb.append("select cons.CONSTRAINT_NAME as FOREIGN_KEY_NAME");
        sb.append(", cons.TABLE_NAME as LOCAL_TABLE_NAME, cols.COLUMN_NAME as LOCAL_COLUMN_NAME");
        sb.append(", yourCons.TABLE_NAME as FOREIGN_TABLE_NAME, yourCols.COLUMN_NAME as FOREIGN_COLUMN_NAME");
        sb.append(" from ALL_CONSTRAINTS cons");
        sb.append(" left outer join ALL_CONSTRAINTS yourCons");
        sb.append(" on cons.R_CONSTRAINT_NAME = yourCons.CONSTRAINT_NAME");
        sb.append(" left outer join ALL_CONS_COLUMNS cols");
        sb.append(" on cons.OWNER = cols.OWNER and cons.CONSTRAINT_NAME = cols.CONSTRAINT_NAME");
        sb.append(" left outer join ALL_CONS_COLUMNS yourCols");
        sb.append(" on cons.R_OWNER = yourCols.OWNER and cons.R_CONSTRAINT_NAME = yourCols.CONSTRAINT_NAME");
        sb.append(" and cols.POSITION = yourCols.POSITION");
        sb.append(" where cons.OWNER = '").append(pureSchema).append("'");
        sb.append(" and cons.R_OWNER = '").append(pureSchema).append("'"); // same schema only
        sb.append(" and cons.CONSTRAINT_TYPE = 'R'"); // foreign key
        sb.append(" and yourCons.CONSTRAINT_TYPE = 'U'"); // unique key
        sb.append(" order by cons.TABLE_NAME, cons.CONSTRAINT_NAME, cols.POSITION nulls last");
        final String sql = sb.toString();
        return doSelectUserUniqueFkList(sql, conn);
    }
}
