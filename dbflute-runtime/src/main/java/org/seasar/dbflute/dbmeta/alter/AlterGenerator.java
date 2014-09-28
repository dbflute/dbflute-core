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

import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.dbmeta.info.ColumnInfo;
import org.seasar.dbflute.dbmeta.info.ForeignInfo;

/**
 * @author jflute
 */
public interface AlterGenerator {

    // ===================================================================================
    //                                                                               Table
    //                                                                               =====
    String generateTableRename(String oldTableName, DBMeta newMeta);

    String generateTableRename(DBMeta oldMeta, String newTableName);

    // ===================================================================================
    //                                                                              Column
    //                                                                              ======
    String generateColumnDefChange(ColumnInfo columnInfo);

    String generateColumnRename(String oldColumnName, ColumnInfo newColumnInfo);

    String generateColumnRename(ColumnInfo oldColumnInfo, String newColumnName);

    // ===================================================================================
    //                                                                         Primary Key
    //                                                                         ===========
    String generatePrimaryKeyAdd(ColumnInfo columnInfo);

    String generatePrimaryKeyDrop(DBMeta dbmeta);

    // ===================================================================================
    //                                                                         Foreign Key
    //                                                                         ===========
    String generateForeignKeyAdd(ForeignInfo foreignInfo);

    String generateForeignKeyDrop(ForeignInfo foreignInfo);
}
