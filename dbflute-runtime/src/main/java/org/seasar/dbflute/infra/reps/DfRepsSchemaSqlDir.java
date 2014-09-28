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
package org.seasar.dbflute.infra.reps;

import java.io.File;
import java.util.List;

import org.seasar.dbflute.infra.core.logic.DfSchemaResourceFinder;

/**
 * @author jflute
 * @since 1.0.4G (2013/07/13 Saturday)
 */
public class DfRepsSchemaSqlDir {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String REPLACE_SCHEMA_SQL_TITLE = "replace-schema";
    public static final String REPLACE_SCHEMA_FILE_EXT = ".sql";
    public static final String TAKE_FINALLY_SQL_TITLE = "take-finally";
    public static final String TAKE_FINALLY_FILE_EXT = ".sql";
    public static final String ALTER_TAKE_FINALLY_SQL_TITLE = "alter-take-finally";
    public static final String ALTER_TAKE_FINALLY_FILE_EXT = ".sql";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _sqlRootDir;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfRepsSchemaSqlDir(String sqlRootDir) {
        _sqlRootDir = sqlRootDir;
    }

    // ===================================================================================
    //                                                                        Collect File
    //                                                                        ============
    public List<File> collectReplaceSchemaSqlFileList() {
        return findSchemaResourceFileList(getReplaceSchemaSqlTitle(), getReplaceSchemaFileExt());
    }

    protected String getReplaceSchemaSqlTitle() {
        return REPLACE_SCHEMA_SQL_TITLE;
    }

    protected String getReplaceSchemaFileExt() {
        return REPLACE_SCHEMA_FILE_EXT;
    }

    public List<File> collectTakeFinallySqlFileList() {
        return findSchemaResourceFileList(getTakeFinallySqlTitle(), getTakeFinallyFileExt());
    }

    protected String getTakeFinallySqlTitle() {
        return TAKE_FINALLY_SQL_TITLE;
    }

    protected String getTakeFinallyFileExt() {
        return TAKE_FINALLY_FILE_EXT;
    }

    public List<File> collectAlterTakeFinallySqlFileList() {
        return findSchemaResourceFileList(getAlterTakeFinallySqlTitle(), getAlterTakeFinallyFileExt());
    }

    protected String getAlterTakeFinallySqlTitle() {
        return ALTER_TAKE_FINALLY_SQL_TITLE;
    }

    protected String getAlterTakeFinallyFileExt() {
        return ALTER_TAKE_FINALLY_FILE_EXT;
    }

    protected List<File> findSchemaResourceFileList(String prefix, String suffix) {
        final DfSchemaResourceFinder finder = createSchemaResourceFinder();
        finder.addPrefix(prefix);
        finder.addSuffix(suffix);
        return finder.findResourceFileList(_sqlRootDir);
    }

    protected DfSchemaResourceFinder createSchemaResourceFinder() {
        return new DfSchemaResourceFinder();
    }
}
