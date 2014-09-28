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
package org.seasar.dbflute.dbmeta.alter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.dbmeta.info.ForeignInfo;

/**
 * @author jflute
 */
public abstract class AbstractAlterGenerator implements AlterGenerator {

    // ===================================================================================
    //                                                                               Table
    //                                                                               =====
    protected void doBuildAlterTable(StringBuilder sb, DBMeta dbmeta) {
        doBuildAlterTable(sb, dbmeta.getTableSqlName().toString());
    }

    protected void doBuildAlterTable(StringBuilder sb, String tableName) {
        sb.append("alter table ").append(tableName);
    }

    // ===================================================================================
    //                                                                              Column
    //                                                                              ======

    // ===================================================================================
    //                                                                         Primary Key
    //                                                                         ===========

    // ===================================================================================
    //                                                                         Foreign Key
    //                                                                         ===========
    public List<String> packForeignAddList(Collection<DBMeta> dbmetaList) {
        final List<String> ddlList = new ArrayList<String>();
        for (DBMeta dbmeta : dbmetaList) {
            final List<ForeignInfo> foreignInfoList = dbmeta.getForeignInfoList();
            for (ForeignInfo info : foreignInfoList) {
                if (info.isPureFK()) {
                    ddlList.add(generateForeignKeyAdd(info));
                }
            }
        }
        return ddlList;
    }

    public List<String> packForeignDropList(Collection<DBMeta> dbmetaList) {
        final List<String> ddlList = new ArrayList<String>();
        for (DBMeta dbmeta : dbmetaList) {
            final List<ForeignInfo> foreignInfoList = dbmeta.getForeignInfoList();
            for (ForeignInfo info : foreignInfoList) {
                if (info.isPureFK()) {
                    ddlList.add(generateForeignKeyDrop(info));
                }
            }
        }
        return ddlList;
    }
}
