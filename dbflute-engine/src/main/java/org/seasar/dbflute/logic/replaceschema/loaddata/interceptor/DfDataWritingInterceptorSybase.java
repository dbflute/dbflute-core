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
package org.seasar.dbflute.logic.replaceschema.loaddata.interceptor;

import java.util.Map;

import javax.sql.DataSource;

import org.seasar.dbflute.dbway.WayOfSybase;
import org.seasar.dbflute.logic.jdbc.metadata.info.DfColumnMeta;

/**
 * @author jflute
 */
public class DfDataWritingInterceptorSybase extends DfDataWritingInterceptorSQLServer {

    public DfDataWritingInterceptorSybase(DataSource dataSource, boolean loggingSql) {
        super(dataSource, loggingSql);
    }

    @Override
    protected boolean hasIdentityColumn(DataSource dataSource, String tableSqlName, Map<String, DfColumnMeta> columnMap) {
        for (DfColumnMeta info : columnMap.values()) {
            if (info.isSybaseAutoIncrement()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected String buildIdentityInsertSettingSql(String tableSqlName, boolean insertOn) {
        final WayOfSybase wayOfSybase = new WayOfSybase();
        if (insertOn) {
            return wayOfSybase.buildIdentityDisableSql(tableSqlName);
        } else {
            return wayOfSybase.buildIdentityEnableSql(tableSqlName);
        }
    }
}
