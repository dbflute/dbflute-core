/*
 * Copyright 2014-2016 the original author or authors.
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
package org.dbflute.logic.manage.freegen;

import java.util.List;
import java.util.Map;

import org.dbflute.logic.generate.packagepath.DfPackagePathHandler;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 */
public class DfFreeGenRequest {

    private static final Logger logger = LoggerFactory.getLogger(DfFreeGenRequest.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfFreeGenManager _manager;
    protected final String _requestName;
    protected final DfFreeGenResource _resource;
    protected final DfFreeGenOutput _output;
    protected DfFreeGenTable _table;
    protected DfPackagePathHandler _packagePathHandler;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfFreeGenRequest(DfFreeGenManager manager, String requestName, DfFreeGenResource resource, DfFreeGenOutput output) {
        _manager = manager;
        _requestName = requestName;
        _resource = resource;
        _output = output;
    }

    // ===================================================================================
    //                                                                        ResourceType
    //                                                                        ============
    public boolean isResourceTypeProp() {
        return DfFreeGenResourceType.PROP.equals(_resource.getResourceType());
    }

    public boolean isResourceTypeXls() {
        return DfFreeGenResourceType.XLS.equals(_resource.getResourceType());
    }

    public boolean isResourceTypeFilePath() {
        return DfFreeGenResourceType.FILE_PATH.equals(_resource.getResourceType());
    }

    public boolean isResourceTypeJsonKey() {
        return DfFreeGenResourceType.JSON_KEY.equals(_resource.getResourceType());
    }

    public boolean isResourceTypeJsonSchema() {
        return DfFreeGenResourceType.JSON_SCHEMA.equals(_resource.getResourceType());
    }

    public boolean isResourceTypeSolr() {
        return DfFreeGenResourceType.SOLR.equals(_resource.getResourceType());
    }

    public boolean isResourceTypeElasticsearch() {
        return DfFreeGenResourceType.ELASTICSEARCH.equals(_resource.getResourceType());
    }

    public boolean isResourceTypeMailFlute() {
        return DfFreeGenResourceType.MAIL_FLUTE.equals(_resource.getResourceType());
    }

    // ===================================================================================
    //                                                                                Path
    //                                                                                ====
    public void enableOutputDirectory() {
        _manager.setOutputDirectory(_output.getOutputDirectory());
    }

    public String getGenerateDirPath() { // contains rear slash '/'
        return getPackageAsPath(_output.getPackage());
    }

    public String buildGenerateDirHierarchyPath(Map<String, Object> tableMap) { // contains rear slash '/'
        return getPackageAsPath(buildHierarchyPackage(tableMap));
    }

    public String getGenerateFilePath() {
        return getGenerateDirPath() + _output.getClassName() + "." + _output.getFileExt();
    }

    protected String getPackageAsPath(String pkg) {
        return _packagePathHandler.getPackageAsPath(pkg);
    }

    public String getTemplatePath() {
        return _output.getTemplateFile();
    }

    // ===================================================================================
    //                                                                             Logging
    //                                                                             =======
    public void info(String msg) {
        logger.info(msg);
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "{" + _requestName + ", " + _resource + ", " + _output + ", " + _table + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getRequestName() {
        return _requestName;
    }

    // -----------------------------------------------------
    //                                              Resource
    //                                              --------
    public DfFreeGenResource getResource() {
        return _resource;
    }

    public DfFreeGenResourceType getResourceType() {
        return _resource.getResourceType();
    }

    public String getResourceFile() {
        return _resource.getResourceFile();
    }

    public String getResourceFilePureName() {
        return _resource.getResourceFilePureName();
    }

    // -----------------------------------------------------
    //                                                Output
    //                                                ------
    public DfFreeGenOutput getOutput() {
        return _output;
    }

    public String getTemplateFile() {
        return _output.getTemplateFile();
    }

    public String getOutputDirectory() {
        return _output.getOutputDirectory();
    }

    public String getPackage() {
        return _output.getPackage();
    }

    public String buildHierarchyPackage(Map<String, Object> tableMap) {
        final String additionalPkg = (String) tableMap.get("additionalPackage");
        final String added = Srl.is_NotNull_and_NotEmpty(additionalPkg) ? "." + additionalPkg : "";
        return _output.getPackage() + added;
    }

    public String getClassName() {
        return _output.getClassName();
    }

    public String getFileExt() {
        return _output.getFileExt();
    }

    // -----------------------------------------------------
    //                                                 Table
    //                                                 -----
    public DfFreeGenTable getTable() { // fixed info of table
        return _table;
    }

    public Map<String, Object> getTableMap() { // flexible table configuration
        return _table.getTableMap();
    }

    public boolean isOnlyOneTable() { // only-one or multiple table?
        return _table.isOnlyOneTable();
    }

    public String getTableName() { // when only-one table
        return _table.getTableName();
    }

    public List<Map<String, Object>> getColumnList() { // when only-one table
        return _table.getColumnList();
    }

    public List<Map<String, Object>> getTableList() { // when multiple table
        return _table.getTableList();
    }

    // -----------------------------------------------------
    //                                                Setter
    //                                                ------
    public void setTable(DfFreeGenTable table) {
        _table = table;
    }

    public void setPackagePathHandler(DfPackagePathHandler packagePathHandler) {
        _packagePathHandler = packagePathHandler;
    }
}
