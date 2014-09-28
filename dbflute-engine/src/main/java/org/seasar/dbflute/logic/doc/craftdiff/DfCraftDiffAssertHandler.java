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
package org.seasar.dbflute.logic.doc.craftdiff;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.exception.DfCraftDiffIllegalCraftKeyNameException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.helper.token.file.FileMakingCallback;
import org.seasar.dbflute.helper.token.file.FileMakingOption;
import org.seasar.dbflute.helper.token.file.FileMakingRowWriter;
import org.seasar.dbflute.helper.token.file.FileToken;
import org.seasar.dbflute.properties.DfDocumentProperties;
import org.seasar.dbflute.resource.DBFluteSystem;
import org.seasar.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 * @since 0.9.9.8 (2012/09/04 Tuesday)
 */
public class DfCraftDiffAssertHandler {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _craftMetaDir;
    protected final DfCraftDiffAssertDirection _assertDirection;
    protected final String _craftTitle;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfCraftDiffAssertHandler(String craftMetaDir, DfCraftDiffAssertDirection nextDirection, String craftTitle) {
        _craftMetaDir = craftMetaDir;
        _assertDirection = nextDirection;
        _craftTitle = craftTitle;
    }

    // ===================================================================================
    //                                                                       Handle Assert
    //                                                                       =============
    /**
     * Handle the assertion.
     * @param sqlFile The SQL file that contains the assert SQL. (NotNull)
     * @param st The statement for the SQL. (NotNull)
     * @param sql The SQL string to assert. (NotNull)
     * @throws SQLException
     */
    public void handle(File sqlFile, Statement st, String sql) throws SQLException {
        prepareCraftMetaDir();
        final List<Map<String, String>> diffDataList = selectDiffDataList(sqlFile, st, sql);
        final String targetFilePath = calculateTargetFilePath(sqlFile);
        dumpCraftMetaToDataFile(diffDataList, new File(targetFilePath));
    }

    protected void prepareCraftMetaDir() {
        final File dir = new File(_craftMetaDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    protected String calculateTargetFilePath(File sqlFile) {
        final String nextDataFilePath = buildNextDataFile(sqlFile);
        final String previousDataFilePath = buildPreviousDataFile(sqlFile);
        final String targetFilePath;
        if (DfCraftDiffAssertDirection.ROLLING_NEXT.equals(_assertDirection)) {
            rollingPreviousDataFile(nextDataFilePath, previousDataFilePath);
            targetFilePath = nextDataFilePath;
        } else if (DfCraftDiffAssertDirection.DIRECT_NEXT.equals(_assertDirection)) {
            targetFilePath = nextDataFilePath;
        } else if (DfCraftDiffAssertDirection.DIRECT_PREVIOUS.equals(_assertDirection)) {
            targetFilePath = previousDataFilePath;
        } else {
            String msg = "Unknown assert direction: " + _assertDirection;
            throw new IllegalStateException(msg);
        }
        return targetFilePath;
    }

    protected String buildNextDataFile(File sqlFile) {
        return doCreateNextDataFile(sqlFile, true);
    }

    protected String buildPreviousDataFile(File sqlFile) {
        return doCreateNextDataFile(sqlFile, false);
    }

    protected String doCreateNextDataFile(File sqlFile, boolean next) {
        final String fileName = buildCraftMetaFileName(_craftTitle, next);
        return _craftMetaDir + "/" + fileName;
    }

    // ===================================================================================
    //                                                                     Select DiffData
    //                                                                     ===============
    protected List<Map<String, String>> selectDiffDataList(File sqlFile, Statement st, String sql) throws SQLException {
        if (st == null) {
            String msg = "The argument 'st' should not be null: sqlFile=" + sqlFile;
            throw new IllegalStateException(msg);
        }
        final List<Map<String, String>> resultList = DfCollectionUtil.newArrayList();
        ResultSet rs = null;
        try {
            rs = st.executeQuery(sql);
            final ResultSetMetaData metaData = rs.getMetaData();
            final int columnCount = metaData.getColumnCount();
            final Set<String> craftKeyNameSet = DfCollectionUtil.newHashSet();
            while (rs.next()) {
                final Map<String, String> recordMap = StringKeyMap.createAsFlexibleOrdered();
                final int firstIndex = 1; // 1 origin in JDBC
                for (int i = firstIndex; i <= columnCount; i++) {
                    final String value = rs.getString(i);
                    if (i == firstIndex) { // first loop
                        assertCraftKeyExists(value, sqlFile, sql);
                        assertUniqueCraftKey(value, sqlFile, sql, craftKeyNameSet);
                    }
                    recordMap.put(metaData.getColumnLabel(i), value);
                }
                resultList.add(recordMap);
            }
            return resultList;
        } catch (SQLException e) {
            handleSQLException(e, sql);
            return null; // reachable
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
    }

    protected void handleSQLException(SQLException e, String sql) throws SQLException {
        throw e; // directly throw it as default (you can override this)
    }

    protected void assertCraftKeyExists(String craftKeyName, File sqlFile, String sql) {
        if (craftKeyName == null) {
            throwCraftDiffCraftKeyNameHasNullException(sqlFile, sql);
        }
    }

    protected void throwCraftDiffCraftKeyNameHasNullException(File sqlFile, String sql) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The craft key name has null.");
        br.addItem("SQL File");
        br.addElement(sqlFile.getPath());
        br.addItem("Craft SQL");
        br.addElement(sql);
        final String msg = br.buildExceptionMessage();
        throw new DfCraftDiffIllegalCraftKeyNameException(msg);
    }

    protected void assertUniqueCraftKey(String craftKeyName, File sqlFile, String sql, Set<String> craftKeyNameSet) {
        if (craftKeyNameSet.contains(craftKeyName)) {
            throwCraftDiffCraftKeyNameDuplicateException(craftKeyName, sqlFile, sql);
        }
        craftKeyNameSet.add(craftKeyName);
    }

    protected void throwCraftDiffCraftKeyNameDuplicateException(String craftKeyName, File sqlFile, String sql) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The craft key name has duplicate entry.");
        br.addItem("Duplicate Key");
        br.addElement(craftKeyName);
        br.addItem("SQL File");
        br.addElement(sqlFile.getPath());
        br.addItem("Craft SQL");
        br.addElement(sql);
        final String msg = br.buildExceptionMessage();
        throw new DfCraftDiffIllegalCraftKeyNameException(msg);
    }

    // ===================================================================================
    //                                                                    Rolling Previous
    //                                                                    ================
    protected void rollingPreviousDataFile(final String nextDataFilePath, final String previousDataFilePath) {
        final File previousDataFile = new File(previousDataFilePath);
        if (previousDataFile.exists()) {
            previousDataFile.delete();
        }
        final File nextDataFile = new File(nextDataFilePath);
        if (nextDataFile.exists()) {
            nextDataFile.renameTo(previousDataFile);
        }
    }

    // ===================================================================================
    //                                                                      Dump CraftMeta
    //                                                                      ==============
    protected void dumpCraftMetaToDataFile(final List<Map<String, String>> diffDataList, File nextDataFile) {
        final FileToken fileToken = new FileToken();
        try {
            fileToken.make(new FileOutputStream(nextDataFile), new FileMakingCallback() {
                public void write(FileMakingRowWriter writer) throws IOException {
                    for (Map<String, String> map : diffDataList) {
                        writer.writeRow(map);
                    }
                }
            }, new FileMakingOption().delimitateByTab().encodeAsUTF8());
        } catch (IOException e) {
            String msg = "Failed to make file: " + nextDataFile.getPath();
            throw new IllegalStateException(msg, e);
        }
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfDocumentProperties getDocumentProperties() {
        return getProperties().getDocumentProperties();
    }

    protected String buildCraftMetaFileName(String craftTitle, boolean next) {
        return getDocumentProperties().buildCraftMetaFileName(craftTitle, next);
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return DBFluteSystem.getBasicLn();
    }
}
