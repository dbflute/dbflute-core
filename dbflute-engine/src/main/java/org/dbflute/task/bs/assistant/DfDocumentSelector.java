/*
 * Copyright 2014-2019 the original author or authors.
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
package org.dbflute.task.bs.assistant;

import java.util.List;

import org.dbflute.DfBuildProperties;
import org.dbflute.infra.doc.hacomment.DfHacoMapDiffPart;
import org.dbflute.logic.doc.historyhtml.DfSchemaHistory;
import org.dbflute.logic.doc.prophtml.DfPropHtmlManager;
import org.dbflute.logic.jdbc.schemadiff.DfSchemaDiff;
import org.dbflute.properties.DfBasicProperties;
import org.dbflute.properties.DfDocumentProperties;
import org.dbflute.properties.DfLastaFluteProperties;
import org.dbflute.properties.DfReplaceSchemaProperties;
import org.dbflute.util.DfCollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @author hakiba
 */
public class DfDocumentSelector { // also has document supplement resources e.g. history, schema policy

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /**
     * The logger instance for this class. (NotNull)
     */
    private static final Logger _log = LoggerFactory.getLogger(DfDocumentSelector.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected boolean _schemaHtml;
    protected boolean _historyHtml;
    protected boolean _schemaSyncCheckResultHtml;
    protected boolean _alterCheckResultHtml;
    protected boolean _propertiesHtml;
    protected boolean _lastaDocHtml;
    protected DocumentType _currentDocumentType;

    // -----------------------------------------------------
    //                                   Supplement Resource
    //                                   -------------------
    protected DfSchemaHistory _schemaHistory;
    protected DfPropHtmlManager _propHtmlManager;
    protected List<String> cachedLastaDocNameList;

    // ===================================================================================
    //                                                                         Coin Method
    //                                                                         ===========
    public boolean needsInitializeProperties() {
        return _schemaHtml || _historyHtml;
    }

    public String getDiffModelBigTitle() {
        final String title;
        if (isCurrentHistoryHtml()) {
            title = "History";
        } else if (isCurrentSchemaSyncCheckResultHtml()) {
            title = "SyncCheck";
        } else if (isCurrentAlterCheckResultHtml()) {
            title = "AlterCheck";
        } else { // no way
            title = "Unknown";
        }
        return title;
    }

    public String getDiffModelSmallTitle() {
        final String title;
        if (isCurrentHistoryHtml()) {
            title = "history";
        } else if (isCurrentSchemaSyncCheckResultHtml()) {
            title = "sync check";
        } else if (isCurrentAlterCheckResultHtml()) {
            title = "alter check";
        } else { // no way
            title = "unknown";
        }
        return title;
    }

    // ===================================================================================
    //                                                                    Current Document
    //                                                                    ================
    public boolean isCurrentHistoryHtml() {
        return _currentDocumentType != null && _currentDocumentType.equals(DocumentType.HistoryHtml);
    }

    public boolean isCurrentSchemaSyncCheckResultHtml() {
        return _currentDocumentType != null && _currentDocumentType.equals(DocumentType.SchemaSyncCheckResultHtml);
    }

    public boolean isCurrentAlterCheckResultHtml() {
        return _currentDocumentType != null && _currentDocumentType.equals(DocumentType.AlterCheckResultHtml);
    }

    public boolean isCurrentPropertiesHtml() {
        return _currentDocumentType != null && _currentDocumentType.equals(DocumentType.PropertiesHtml);
    }

    public void markSchemaHtml() {
        _currentDocumentType = DocumentType.SchemaHtml;
    }

    public void markHistoryHtml() {
        _currentDocumentType = DocumentType.HistoryHtml;
    }

    public void markSchemaSyncCheckResultHtml() {
        _currentDocumentType = DocumentType.SchemaSyncCheckResultHtml;
    }

    public void markAlterCheckResultHtml() {
        _currentDocumentType = DocumentType.AlterCheckResultHtml;
    }

    public void markPropertiesHtml() {
        _currentDocumentType = DocumentType.PropertiesHtml;
    }

    public enum DocumentType {
        SchemaHtml, HistoryHtml, SchemaSyncCheckResultHtml, AlterCheckResultHtml, PropertiesHtml
    }

    // ===================================================================================
    //                                                                         Schema HTML
    //                                                                         ===========
    // -----------------------------------------------------
    //                                           File System
    //                                           -----------
    public String getSchemaHtmlFileName() {
        final String projectName = getProjectName();
        return getDocumentProperties().getSchemaHtmlFileName(projectName);
    }

    // -----------------------------------------------------
    //                                           Sister Link
    //                                           -----------
    public boolean isSchemaHtmlToHistoryHtmlLink() {
        return !isSuppressSchemaHtmlToSisterLink() && isHistoryHtml() && existsSchemaHistory();
    }

    public boolean isSchemaHtmlToPropertiesHtmlLink() {
        return !isSuppressSchemaHtmlToSisterLink() && isPropertiesHtml() && existsPropertiesHtmlRequest();
    }

    public boolean isSchemaHtmlToLastaDocLink() {
        return !isSuppressSchemaHtmlToSisterLink() && isLastaDocHtml() && existsLastaDocHtml();
    }

    // -----------------------------------------------------
    //                               Neighborhood SchemaHtml
    //                               -----------------------
    public boolean hasNeighborhoodSchemaHtml() {
        return getDocumentProperties().hasNeighborhoodSchemaHtml();
    }

    public List<String> getNeighborhoodSchemaHtmlKeyList() {
        return getDocumentProperties().getNeighborhoodSchemaHtmlKeyList();
    }

    public String getNeighborhoodSchemaHtmlPath(String key) {
        return getDocumentProperties().getNeighborhoodSchemaHtmlPath(key);
    }

    // -----------------------------------------------------
    //                                        Schema Diagram
    //                                        --------------
    public boolean hasSchemaDiagram() {
        return getDocumentProperties().hasSchemaDiagram();
    }

    public List<String> getSchemaDiagramKeyList() {
        return getDocumentProperties().getSchemaDiagramKeyList();
    }

    public String getSchemaDiagramPath(String key) {
        return getDocumentProperties().getSchemaDiagramPath(key);
    }

    public boolean hasSchemaDiagramWidth(String key) {
        return getDocumentProperties().hasSchemaDiagramWidth(key);
    }

    public String getSchemaDiagramWidth(String key) {
        return getDocumentProperties().getSchemaDiagramWidth(key);
    }

    public boolean hasSchemaDiagramHeight(String key) {
        return getDocumentProperties().hasSchemaDiagramHeight(key);
    }

    public String getSchemaDiagramHeight(String key) {
        return getDocumentProperties().getSchemaDiagramHeight(key);
    }

    // ===================================================================================
    //                                                                      Schema History
    //                                                                      ==============
    // -----------------------------------------------------
    //                                         Basic Process
    //                                         -------------
    public void loadSchemaHistoryAsCore() { // for HistoryHtml
        final DfSchemaHistory schemaHistory = DfSchemaHistory.createAsCore();
        doLoadSchemaHistory(schemaHistory);
    }

    public void loadSchemaHistoryAsSchemaSyncCheck() { // for SchemaSyncCheck
        final DfSchemaHistory schemaHistory = DfSchemaHistory.createAsPlain(getSchemaSyncCheckDiffMapFile());
        doLoadSchemaHistory(schemaHistory);
    }

    public void loadSchemaHistoryAsAlterCheck() { // for AlterCheck
        final DfSchemaHistory schemaHistory = DfSchemaHistory.createAsPlain(getAlterCheckDiffMapFile());
        doLoadSchemaHistory(schemaHistory);
    }

    protected void doLoadSchemaHistory(DfSchemaHistory schemaHistory) {
        _log.info("...Loading schema history");
        _schemaHistory = schemaHistory; // also loaded mark
        _schemaHistory.loadHistory();
        if (existsSchemaHistory()) {
            _log.info(" -> found history: count=" + getSchemaDiffList().size());
        } else {
            _log.info(" -> no history");
        }
    }

    public boolean existsSchemaHistory() {
        return _schemaHistory != null && _schemaHistory.existsHistory();
    }

    public List<DfSchemaDiff> getSchemaDiffList() {
        return _schemaHistory != null ? _schemaHistory.getSchemaDiffList() : DfCollectionUtil.emptyList();
    }

    public List<DfSchemaDiff> lazyLoadIfNeedsCoreSchemaDiffList() { // for e,g, SchemaPolicy, ConventionalTakeAssert
        if (_schemaHistory == null) { // means not loaded yet
            loadSchemaHistoryAsCore();
        }
        return getSchemaDiffList();
    }

    // -----------------------------------------------------
    //                                             Hacomment
    //                                             ---------
    public void loadHacoMap() {
        _schemaHistory.loadHacoMap();
    }

    public List<DfHacoMapDiffPart> getHacoMapDiffList() {
        return _schemaHistory.getHacoMapDiffList();
    }

    public boolean existHacoMap() {
        return _schemaHistory.existsHacoMapPickup();
    }

    // -----------------------------------------------------
    //                                           File System
    //                                           -----------
    public String getHistoryHtmlFileName() {
        final String projectName = getProjectName();
        return getDocumentProperties().getHistoryHtmlFileName(projectName);
    }

    public String getSchemaSyncCheckResultFileName() {
        return getDocumentProperties().getSchemaSyncCheckResultFileName();
    }

    public String getAlterCheckResultFileName() {
        return getReplaceSchemaProperties().getMigrationAlterCheckResultFileName();
    }

    // -----------------------------------------------------
    //                                           Sister Link
    //                                           -----------
    public boolean isHistoryHtmlToSchemaHtmlLink() {
        return !isSuppressHistoryHtmlToSisterLink() && isSchemaHtml(); // SchemaHtml always exists
    }

    public boolean isHistoryHtmlToPropertiesHtmlLink() {
        return !isSuppressHistoryHtmlToSisterLink() && isPropertiesHtml() && existsPropertiesHtmlRequest();
    }

    // -----------------------------------------------------
    //                                            Supplement
    //                                            ----------
    public boolean isHistoryHtmlShowDiffAuthor() {
        return !isSuppressHistoryHtmlDiffAuthor();
    }

    public boolean isHistoryHtmlShowDiffGitBranch() {
        return !isSuppressHistoryHtmlDiffGitBranch();
    }

    // ===================================================================================
    //                                                                     Properties HTML
    //                                                                     ===============
    // -----------------------------------------------------
    //                                         Basic Process
    //                                         -------------
    /**
     * Load requests for properties HTML. <br>
     * If no property, do nothing.
     */
    public void loadPropertiesHtmlRequest() {
        _propHtmlManager = new DfPropHtmlManager();
        _propHtmlManager.loadRequest();
    }

    public boolean existsPropertiesHtmlRequest() {
        return _propHtmlManager != null && _propHtmlManager.existsRequest();
    }

    public DfPropHtmlManager getPropertiesHtmlManager() {
        return _propHtmlManager;
    }

    // -----------------------------------------------------
    //                                           File System
    //                                           -----------
    public String getPropertiesHtmlFileName() {
        final String projectName = getProjectName();
        return getDocumentProperties().getPropertiesHtmlFileName(projectName);
    }

    // -----------------------------------------------------
    //                                           Sister Link
    //                                           -----------
    public boolean isPropertiesHtmlToSchemaHtmlLink() {
        return !isSuppressPropertiesHtmlToSisterLink() && isSchemaHtml(); // SchemaHtml always exists
    }

    public boolean isPropertiesHtmlToHistoryHtmlLink() {
        return !isSuppressPropertiesHtmlToSisterLink() && isHistoryHtml() && existsSchemaHistory();
    }

    // ===================================================================================
    //                                                                            LastaDoc
    //                                                                            ========
    // -----------------------------------------------------
    //                                         Basic Process
    //                                         -------------
    public boolean existsLastaDocHtml() {
        return !getLastaDocHtmlNameList().isEmpty();
    }

    // -----------------------------------------------------
    //                                           File System
    //                                           -----------
    public List<String> getLastaDocHtmlNameList() {
        if (cachedLastaDocNameList != null) {
            return cachedLastaDocNameList;
        }
        cachedLastaDocNameList = getLastaFluteProperties().getLastaDocHtmlNameList();
        return cachedLastaDocNameList;
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfBasicProperties getBasicProperties() {
        return getProperties().getBasicProperties();
    }

    protected String getProjectName() {
        return getBasicProperties().getProjectName();
    }

    protected DfDocumentProperties getDocumentProperties() {
        return getProperties().getDocumentProperties();
    }

    protected boolean isSuppressSchemaHtmlToSisterLink() {
        return getDocumentProperties().isSuppressSchemaHtmlToSisterLink();
    }

    protected boolean isSuppressHistoryHtmlToSisterLink() {
        return getDocumentProperties().isSuppressHistoryHtmlToSisterLink();
    }

    protected boolean isSuppressHistoryHtmlDiffAuthor() {
        return getDocumentProperties().isSuppressHistoryHtmlDiffAuthor();
    }

    protected boolean isSuppressHistoryHtmlDiffGitBranch() {
        return getDocumentProperties().isSuppressHistoryHtmlDiffGitBranch();
    }

    protected boolean isSuppressPropertiesHtmlToSisterLink() {
        return getDocumentProperties().isSuppressPropertiesHtmlToSisterLink();
    }

    protected String getSchemaSyncCheckDiffMapFile() {
        return getDocumentProperties().getSchemaSyncCheckDiffMapFile();
    }

    protected DfReplaceSchemaProperties getReplaceSchemaProperties() {
        return getProperties().getReplaceSchemaProperties();
    }

    protected String getAlterCheckDiffMapFile() {
        return getReplaceSchemaProperties().getMigrationAlterCheckDiffMapFile();
    }

    protected DfLastaFluteProperties getLastaFluteProperties() {
        return getProperties().getLastaFluteProperties();
    }

    protected String getLastaDocOutputDirectory() {
        return getLastaFluteProperties().getLastaDocOutputDirectory();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public boolean isSchemaHtml() {
        return _schemaHtml;
    }

    public DfDocumentSelector selectSchemaHtml() {
        _schemaHtml = true;
        return this;
    }

    public boolean isHistoryHtml() {
        return _historyHtml;
    }

    public DfDocumentSelector selectHistoryHtml() {
        _historyHtml = true;
        return this;
    }

    public boolean isSchemaSyncCheckResultHtml() {
        return _schemaSyncCheckResultHtml;
    }

    public DfDocumentSelector selectSchemaSyncCheckResultHtml() {
        _schemaSyncCheckResultHtml = true;
        return this;
    }

    public boolean isAlterCheckResultHtml() {
        return _alterCheckResultHtml;
    }

    public DfDocumentSelector selectAlterCheckResultHtml() {
        _alterCheckResultHtml = true;
        return this;
    }

    public boolean isPropertiesHtml() {
        return _propertiesHtml;
    }

    public DfDocumentSelector selectPropertiesHtml() {
        _propertiesHtml = true;
        return this;
    }

    public boolean isLastaDocHtml() {
        return _lastaDocHtml;
    }

    public DfDocumentSelector selectLastaDocHtml() {
        _lastaDocHtml = true;
        return this;
    }
}
