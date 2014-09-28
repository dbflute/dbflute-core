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
package org.seasar.dbflute.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;

/**
 * The wrapper of database meta data for lazy loading.
 * @author jflute
 * @since 1.0.3 (2012/02/16 Saturday)
 */
public class LazyDatabaseMetaDataWrapper implements DatabaseMetaData {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final MetaDataConnectionProvider _connectionProvider;
    protected Connection _lazyConnection;
    protected DatabaseMetaData _lazyMetaData;
    protected boolean _restrictedMetaData;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public LazyDatabaseMetaDataWrapper(MetaDataConnectionProvider connectionProvider) {
        _connectionProvider = connectionProvider;
    }

    // ===================================================================================
    //                                                                     Actual MetaData
    //                                                                     ===============
    protected DatabaseMetaData getActualMetaData() throws SQLException {
        if (_restrictedMetaData) {
            String msg = "The meta data is restriected.";
            throw new IllegalStateException(msg);
        }
        if (_lazyMetaData != null) {
            return _lazyMetaData;
        }
        _lazyConnection = _connectionProvider.getConnection();
        _lazyMetaData = _lazyConnection.getMetaData();
        return _lazyMetaData;
    }

    public void restrictMetaData() {
        _restrictedMetaData = true;
    }

    public void closeActualReally() throws SQLException {
        if (_lazyConnection != null) {
            _lazyConnection.close();
            _lazyConnection = null;
        }
        _lazyMetaData = null;
    }

    // ===================================================================================
    //                                                                      Implementation
    //                                                                      ==============
    public <T> T unwrap(Class<T> class1) throws SQLException {
        return getActualMetaData().unwrap(class1);
    }

    public boolean isWrapperFor(Class<?> class1) throws SQLException {
        return getActualMetaData().isWrapperFor(class1);
    }

    public boolean allProceduresAreCallable() throws SQLException {
        return getActualMetaData().allProceduresAreCallable();
    }

    public boolean allTablesAreSelectable() throws SQLException {
        return getActualMetaData().allTablesAreSelectable();
    }

    public String getURL() throws SQLException {
        return getActualMetaData().getURL();
    }

    public String getUserName() throws SQLException {
        return getActualMetaData().getUserName();
    }

    public boolean isReadOnly() throws SQLException {
        return getActualMetaData().isReadOnly();
    }

    public boolean nullsAreSortedHigh() throws SQLException {
        return getActualMetaData().nullsAreSortedHigh();
    }

    public boolean nullsAreSortedLow() throws SQLException {
        return getActualMetaData().nullsAreSortedLow();
    }

    public boolean nullsAreSortedAtStart() throws SQLException {
        return getActualMetaData().nullsAreSortedAtStart();
    }

    public boolean nullsAreSortedAtEnd() throws SQLException {
        return getActualMetaData().nullsAreSortedAtEnd();
    }

    public String getDatabaseProductName() throws SQLException {
        return getActualMetaData().getDatabaseProductName();
    }

    public String getDatabaseProductVersion() throws SQLException {
        return getActualMetaData().getDatabaseProductVersion();
    }

    public String getDriverName() throws SQLException {
        return getActualMetaData().getDriverName();
    }

    public String getDriverVersion() throws SQLException {
        return getActualMetaData().getDriverVersion();
    }

    public int getDriverMajorVersion() {
        try {
            return getActualMetaData().getDriverMajorVersion();
        } catch (SQLException e) {
            String msg = "Failed to get actual meta data.";
            throw new IllegalStateException(msg, e);
        }
    }

    public int getDriverMinorVersion() {
        try {
            return getActualMetaData().getDriverMinorVersion();
        } catch (SQLException e) {
            String msg = "Failed to get actual meta data.";
            throw new IllegalStateException(msg, e);
        }
    }

    public boolean usesLocalFiles() throws SQLException {
        return getActualMetaData().usesLocalFiles();
    }

    public boolean usesLocalFilePerTable() throws SQLException {
        return getActualMetaData().usesLocalFilePerTable();
    }

    public boolean supportsMixedCaseIdentifiers() throws SQLException {
        return getActualMetaData().supportsMixedCaseIdentifiers();
    }

    public boolean storesUpperCaseIdentifiers() throws SQLException {
        return getActualMetaData().storesUpperCaseIdentifiers();
    }

    public boolean storesLowerCaseIdentifiers() throws SQLException {
        return getActualMetaData().storesLowerCaseIdentifiers();
    }

    public boolean storesMixedCaseIdentifiers() throws SQLException {
        return getActualMetaData().storesMixedCaseIdentifiers();
    }

    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
        return getActualMetaData().supportsMixedCaseQuotedIdentifiers();
    }

    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
        return getActualMetaData().storesUpperCaseQuotedIdentifiers();
    }

    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
        return getActualMetaData().storesLowerCaseQuotedIdentifiers();
    }

    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
        return getActualMetaData().storesMixedCaseQuotedIdentifiers();
    }

    public String getIdentifierQuoteString() throws SQLException {
        return getActualMetaData().getIdentifierQuoteString();
    }

    public String getSQLKeywords() throws SQLException {
        return getActualMetaData().getSQLKeywords();
    }

    public String getNumericFunctions() throws SQLException {
        return getActualMetaData().getNumericFunctions();
    }

    public String getStringFunctions() throws SQLException {
        return getActualMetaData().getStringFunctions();
    }

    public String getSystemFunctions() throws SQLException {
        return getActualMetaData().getSystemFunctions();
    }

    public String getTimeDateFunctions() throws SQLException {
        return getActualMetaData().getTimeDateFunctions();
    }

    public String getSearchStringEscape() throws SQLException {
        return getActualMetaData().getSearchStringEscape();
    }

    public String getExtraNameCharacters() throws SQLException {
        return getActualMetaData().getExtraNameCharacters();
    }

    public boolean supportsAlterTableWithAddColumn() throws SQLException {
        return getActualMetaData().supportsAlterTableWithAddColumn();
    }

    public boolean supportsAlterTableWithDropColumn() throws SQLException {
        return getActualMetaData().supportsAlterTableWithDropColumn();
    }

    public boolean supportsColumnAliasing() throws SQLException {
        return getActualMetaData().supportsColumnAliasing();
    }

    public boolean nullPlusNonNullIsNull() throws SQLException {
        return getActualMetaData().nullPlusNonNullIsNull();
    }

    public boolean supportsConvert() throws SQLException {
        return getActualMetaData().supportsConvert();
    }

    public boolean supportsConvert(int i, int j) throws SQLException {
        return getActualMetaData().supportsConvert(i, j);
    }

    public boolean supportsTableCorrelationNames() throws SQLException {
        return getActualMetaData().supportsTableCorrelationNames();
    }

    public boolean supportsDifferentTableCorrelationNames() throws SQLException {
        return getActualMetaData().supportsDifferentTableCorrelationNames();
    }

    public boolean supportsExpressionsInOrderBy() throws SQLException {
        return getActualMetaData().supportsExpressionsInOrderBy();
    }

    public boolean supportsOrderByUnrelated() throws SQLException {
        return getActualMetaData().supportsOrderByUnrelated();
    }

    public boolean supportsGroupBy() throws SQLException {
        return getActualMetaData().supportsGroupBy();
    }

    public boolean supportsGroupByUnrelated() throws SQLException {
        return getActualMetaData().supportsGroupByUnrelated();
    }

    public boolean supportsGroupByBeyondSelect() throws SQLException {
        return getActualMetaData().supportsGroupByBeyondSelect();
    }

    public boolean supportsLikeEscapeClause() throws SQLException {
        return getActualMetaData().supportsLikeEscapeClause();
    }

    public boolean supportsMultipleResultSets() throws SQLException {
        return getActualMetaData().supportsMultipleResultSets();
    }

    public boolean supportsMultipleTransactions() throws SQLException {
        return getActualMetaData().supportsMultipleTransactions();
    }

    public boolean supportsNonNullableColumns() throws SQLException {
        return getActualMetaData().supportsNonNullableColumns();
    }

    public boolean supportsMinimumSQLGrammar() throws SQLException {
        return getActualMetaData().supportsMinimumSQLGrammar();
    }

    public boolean supportsCoreSQLGrammar() throws SQLException {
        return getActualMetaData().supportsCoreSQLGrammar();
    }

    public boolean supportsExtendedSQLGrammar() throws SQLException {
        return getActualMetaData().supportsExtendedSQLGrammar();
    }

    public boolean supportsANSI92EntryLevelSQL() throws SQLException {
        return getActualMetaData().supportsANSI92EntryLevelSQL();
    }

    public boolean supportsANSI92IntermediateSQL() throws SQLException {
        return getActualMetaData().supportsANSI92IntermediateSQL();
    }

    public boolean supportsANSI92FullSQL() throws SQLException {
        return getActualMetaData().supportsANSI92FullSQL();
    }

    public boolean supportsIntegrityEnhancementFacility() throws SQLException {
        return getActualMetaData().supportsIntegrityEnhancementFacility();
    }

    public boolean supportsOuterJoins() throws SQLException {
        return getActualMetaData().supportsOuterJoins();
    }

    public boolean supportsFullOuterJoins() throws SQLException {
        return getActualMetaData().supportsFullOuterJoins();
    }

    public boolean supportsLimitedOuterJoins() throws SQLException {
        return getActualMetaData().supportsLimitedOuterJoins();
    }

    public String getSchemaTerm() throws SQLException {
        return getActualMetaData().getSchemaTerm();
    }

    public String getProcedureTerm() throws SQLException {
        return getActualMetaData().getProcedureTerm();
    }

    public String getCatalogTerm() throws SQLException {
        return getActualMetaData().getCatalogTerm();
    }

    public boolean isCatalogAtStart() throws SQLException {
        return getActualMetaData().isCatalogAtStart();
    }

    public String getCatalogSeparator() throws SQLException {
        return getActualMetaData().getCatalogSeparator();
    }

    public boolean supportsSchemasInDataManipulation() throws SQLException {
        return getActualMetaData().supportsSchemasInDataManipulation();
    }

    public boolean supportsSchemasInProcedureCalls() throws SQLException {
        return getActualMetaData().supportsSchemasInProcedureCalls();
    }

    public boolean supportsSchemasInTableDefinitions() throws SQLException {
        return getActualMetaData().supportsSchemasInTableDefinitions();
    }

    public boolean supportsSchemasInIndexDefinitions() throws SQLException {
        return getActualMetaData().supportsSchemasInIndexDefinitions();
    }

    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
        return getActualMetaData().supportsSchemasInPrivilegeDefinitions();
    }

    public boolean supportsCatalogsInDataManipulation() throws SQLException {
        return getActualMetaData().supportsCatalogsInDataManipulation();
    }

    public boolean supportsCatalogsInProcedureCalls() throws SQLException {
        return getActualMetaData().supportsCatalogsInProcedureCalls();
    }

    public boolean supportsCatalogsInTableDefinitions() throws SQLException {
        return getActualMetaData().supportsCatalogsInTableDefinitions();
    }

    public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
        return getActualMetaData().supportsCatalogsInIndexDefinitions();
    }

    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
        return getActualMetaData().supportsCatalogsInPrivilegeDefinitions();
    }

    public boolean supportsPositionedDelete() throws SQLException {
        return getActualMetaData().supportsPositionedDelete();
    }

    public boolean supportsPositionedUpdate() throws SQLException {
        return getActualMetaData().supportsPositionedUpdate();
    }

    public boolean supportsSelectForUpdate() throws SQLException {
        return getActualMetaData().supportsSelectForUpdate();
    }

    public boolean supportsStoredProcedures() throws SQLException {
        return getActualMetaData().supportsStoredProcedures();
    }

    public boolean supportsSubqueriesInComparisons() throws SQLException {
        return getActualMetaData().supportsSubqueriesInComparisons();
    }

    public boolean supportsSubqueriesInExists() throws SQLException {
        return getActualMetaData().supportsSubqueriesInExists();
    }

    public boolean supportsSubqueriesInIns() throws SQLException {
        return getActualMetaData().supportsSubqueriesInIns();
    }

    public boolean supportsSubqueriesInQuantifieds() throws SQLException {
        return getActualMetaData().supportsSubqueriesInQuantifieds();
    }

    public boolean supportsCorrelatedSubqueries() throws SQLException {
        return getActualMetaData().supportsCorrelatedSubqueries();
    }

    public boolean supportsUnion() throws SQLException {
        return getActualMetaData().supportsUnion();
    }

    public boolean supportsUnionAll() throws SQLException {
        return getActualMetaData().supportsUnionAll();
    }

    public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
        return getActualMetaData().supportsOpenCursorsAcrossCommit();
    }

    public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
        return getActualMetaData().supportsOpenCursorsAcrossRollback();
    }

    public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
        return getActualMetaData().supportsOpenStatementsAcrossCommit();
    }

    public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
        return getActualMetaData().supportsOpenStatementsAcrossRollback();
    }

    public int getMaxBinaryLiteralLength() throws SQLException {
        return getActualMetaData().getMaxBinaryLiteralLength();
    }

    public int getMaxCharLiteralLength() throws SQLException {
        return getActualMetaData().getMaxCharLiteralLength();
    }

    public int getMaxColumnNameLength() throws SQLException {
        return getActualMetaData().getMaxColumnNameLength();
    }

    public int getMaxColumnsInGroupBy() throws SQLException {
        return getActualMetaData().getMaxColumnsInGroupBy();
    }

    public int getMaxColumnsInIndex() throws SQLException {
        return getActualMetaData().getMaxColumnsInIndex();
    }

    public int getMaxColumnsInOrderBy() throws SQLException {
        return getActualMetaData().getMaxColumnsInOrderBy();
    }

    public int getMaxColumnsInSelect() throws SQLException {
        return getActualMetaData().getMaxColumnsInSelect();
    }

    public int getMaxColumnsInTable() throws SQLException {
        return getActualMetaData().getMaxColumnsInTable();
    }

    public int getMaxConnections() throws SQLException {
        return getActualMetaData().getMaxConnections();
    }

    public int getMaxCursorNameLength() throws SQLException {
        return getActualMetaData().getMaxCursorNameLength();
    }

    public int getMaxIndexLength() throws SQLException {
        return getActualMetaData().getMaxIndexLength();
    }

    public int getMaxSchemaNameLength() throws SQLException {
        return getActualMetaData().getMaxSchemaNameLength();
    }

    public int getMaxProcedureNameLength() throws SQLException {
        return getActualMetaData().getMaxProcedureNameLength();
    }

    public int getMaxCatalogNameLength() throws SQLException {
        return getActualMetaData().getMaxCatalogNameLength();
    }

    public int getMaxRowSize() throws SQLException {
        return getActualMetaData().getMaxRowSize();
    }

    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
        return getActualMetaData().doesMaxRowSizeIncludeBlobs();
    }

    public int getMaxStatementLength() throws SQLException {
        return getActualMetaData().getMaxStatementLength();
    }

    public int getMaxStatements() throws SQLException {
        return getActualMetaData().getMaxStatements();
    }

    public int getMaxTableNameLength() throws SQLException {
        return getActualMetaData().getMaxTableNameLength();
    }

    public int getMaxTablesInSelect() throws SQLException {
        return getActualMetaData().getMaxTablesInSelect();
    }

    public int getMaxUserNameLength() throws SQLException {
        return getActualMetaData().getMaxUserNameLength();
    }

    public int getDefaultTransactionIsolation() throws SQLException {
        return getActualMetaData().getDefaultTransactionIsolation();
    }

    public boolean supportsTransactions() throws SQLException {
        return getActualMetaData().supportsTransactions();
    }

    public boolean supportsTransactionIsolationLevel(int i) throws SQLException {
        return getActualMetaData().supportsTransactionIsolationLevel(i);
    }

    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
        return getActualMetaData().supportsDataDefinitionAndDataManipulationTransactions();
    }

    public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
        return getActualMetaData().supportsDataManipulationTransactionsOnly();
    }

    public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
        return getActualMetaData().dataDefinitionCausesTransactionCommit();
    }

    public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
        return getActualMetaData().dataDefinitionIgnoredInTransactions();
    }

    public ResultSet getProcedures(String s, String s1, String s2) throws SQLException {
        return getActualMetaData().getProcedures(s, s1, s2);
    }

    public ResultSet getProcedureColumns(String s, String s1, String s2, String s3) throws SQLException {
        return getActualMetaData().getProcedureColumns(s, s1, s2, s3);
    }

    public ResultSet getTables(String s, String s1, String s2, String[] as) throws SQLException {
        return getActualMetaData().getTables(s, s1, s2, as);
    }

    public ResultSet getSchemas() throws SQLException {
        return getActualMetaData().getSchemas();
    }

    public ResultSet getCatalogs() throws SQLException {
        return getActualMetaData().getCatalogs();
    }

    public ResultSet getTableTypes() throws SQLException {
        return getActualMetaData().getTableTypes();
    }

    public ResultSet getColumns(String s, String s1, String s2, String s3) throws SQLException {
        return getActualMetaData().getColumns(s, s1, s2, s3);
    }

    public ResultSet getColumnPrivileges(String s, String s1, String s2, String s3) throws SQLException {
        return getActualMetaData().getColumnPrivileges(s, s1, s2, s3);
    }

    public ResultSet getTablePrivileges(String s, String s1, String s2) throws SQLException {
        return getActualMetaData().getTablePrivileges(s, s1, s2);
    }

    public ResultSet getBestRowIdentifier(String s, String s1, String s2, int i, boolean flag) throws SQLException {
        return getActualMetaData().getBestRowIdentifier(s, s1, s2, i, flag);
    }

    public ResultSet getVersionColumns(String s, String s1, String s2) throws SQLException {
        return getActualMetaData().getVersionColumns(s, s1, s2);
    }

    public ResultSet getPrimaryKeys(String s, String s1, String s2) throws SQLException {
        return getActualMetaData().getPrimaryKeys(s, s1, s2);
    }

    public ResultSet getImportedKeys(String s, String s1, String s2) throws SQLException {
        return getActualMetaData().getImportedKeys(s, s1, s2);
    }

    public ResultSet getExportedKeys(String s, String s1, String s2) throws SQLException {
        return getActualMetaData().getExportedKeys(s, s1, s2);
    }

    public ResultSet getCrossReference(String s, String s1, String s2, String s3, String s4, String s5)
            throws SQLException {
        return getActualMetaData().getCrossReference(s, s1, s2, s3, s4, s5);
    }

    public ResultSet getTypeInfo() throws SQLException {
        return getActualMetaData().getTypeInfo();
    }

    public ResultSet getIndexInfo(String s, String s1, String s2, boolean flag, boolean flag1) throws SQLException {
        return getActualMetaData().getIndexInfo(s, s1, s2, flag, flag1);
    }

    public boolean supportsResultSetType(int i) throws SQLException {
        return getActualMetaData().supportsResultSetType(i);
    }

    public boolean supportsResultSetConcurrency(int i, int j) throws SQLException {
        return getActualMetaData().supportsResultSetConcurrency(i, j);
    }

    public boolean ownUpdatesAreVisible(int i) throws SQLException {
        return getActualMetaData().ownUpdatesAreVisible(i);
    }

    public boolean ownDeletesAreVisible(int i) throws SQLException {
        return getActualMetaData().ownDeletesAreVisible(i);
    }

    public boolean ownInsertsAreVisible(int i) throws SQLException {
        return getActualMetaData().ownInsertsAreVisible(i);
    }

    public boolean othersUpdatesAreVisible(int i) throws SQLException {
        return getActualMetaData().othersUpdatesAreVisible(i);
    }

    public boolean othersDeletesAreVisible(int i) throws SQLException {
        return getActualMetaData().othersDeletesAreVisible(i);
    }

    public boolean othersInsertsAreVisible(int i) throws SQLException {
        return getActualMetaData().othersInsertsAreVisible(i);
    }

    public boolean updatesAreDetected(int i) throws SQLException {
        return getActualMetaData().updatesAreDetected(i);
    }

    public boolean deletesAreDetected(int i) throws SQLException {
        return getActualMetaData().deletesAreDetected(i);
    }

    public boolean insertsAreDetected(int i) throws SQLException {
        return getActualMetaData().insertsAreDetected(i);
    }

    public boolean supportsBatchUpdates() throws SQLException {
        return getActualMetaData().supportsBatchUpdates();
    }

    public ResultSet getUDTs(String s, String s1, String s2, int[] ai) throws SQLException {
        return getActualMetaData().getUDTs(s, s1, s2, ai);
    }

    public Connection getConnection() throws SQLException {
        return getActualMetaData().getConnection();
    }

    public boolean supportsSavepoints() throws SQLException {
        return getActualMetaData().supportsSavepoints();
    }

    public boolean supportsNamedParameters() throws SQLException {
        return getActualMetaData().supportsNamedParameters();
    }

    public boolean supportsMultipleOpenResults() throws SQLException {
        return getActualMetaData().supportsMultipleOpenResults();
    }

    public boolean supportsGetGeneratedKeys() throws SQLException {
        return getActualMetaData().supportsGetGeneratedKeys();
    }

    public ResultSet getSuperTypes(String s, String s1, String s2) throws SQLException {
        return getActualMetaData().getSuperTypes(s, s1, s2);
    }

    public ResultSet getSuperTables(String s, String s1, String s2) throws SQLException {
        return getActualMetaData().getSuperTables(s, s1, s2);
    }

    public ResultSet getAttributes(String s, String s1, String s2, String s3) throws SQLException {
        return getActualMetaData().getAttributes(s, s1, s2, s3);
    }

    public boolean supportsResultSetHoldability(int i) throws SQLException {
        return getActualMetaData().supportsResultSetHoldability(i);
    }

    public int getResultSetHoldability() throws SQLException {
        return getActualMetaData().getResultSetHoldability();
    }

    public int getDatabaseMajorVersion() throws SQLException {
        return getActualMetaData().getDatabaseMajorVersion();
    }

    public int getDatabaseMinorVersion() throws SQLException {
        return getActualMetaData().getDatabaseMinorVersion();
    }

    public int getJDBCMajorVersion() throws SQLException {
        return getActualMetaData().getJDBCMajorVersion();
    }

    public int getJDBCMinorVersion() throws SQLException {
        return getActualMetaData().getJDBCMinorVersion();
    }

    public int getSQLStateType() throws SQLException {
        return getActualMetaData().getSQLStateType();
    }

    public boolean locatorsUpdateCopy() throws SQLException {
        return getActualMetaData().locatorsUpdateCopy();
    }

    public boolean supportsStatementPooling() throws SQLException {
        return getActualMetaData().supportsStatementPooling();
    }

    public RowIdLifetime getRowIdLifetime() throws SQLException {
        return getActualMetaData().getRowIdLifetime();
    }

    public ResultSet getSchemas(String s, String s1) throws SQLException {
        return getActualMetaData().getSchemas(s, s1);
    }

    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
        return getActualMetaData().supportsStoredFunctionsUsingCallSyntax();
    }

    public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
        return getActualMetaData().autoCommitFailureClosesAllResultSets();
    }

    public ResultSet getClientInfoProperties() throws SQLException {
        return getActualMetaData().getClientInfoProperties();
    }

    public ResultSet getFunctions(String s, String s1, String s2) throws SQLException {
        return getActualMetaData().getFunctions(s, s1, s2);
    }

    public ResultSet getFunctionColumns(String s, String s1, String s2, String s3) throws SQLException {
        return getActualMetaData().getFunctionColumns(s, s1, s2, s3);
    }
}
