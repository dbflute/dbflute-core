/*
 * Copyright 2014-2017 the original author or authors.
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

import java.util.LinkedHashMap;
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
    protected DfFreeGenMetaData _metaData;
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

    public boolean isResourceTypeJsonGeneral() {
        return DfFreeGenResourceType.JSON_GENERAL.equals(_resource.getResourceType());
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

    public boolean isResourceTypeSwagger() {
        return DfFreeGenResourceType.SWAGGER.equals(_resource.getResourceType());
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

    public String buildGenerateDirHierarchyPath(Map<String, Object> map) { // contains rear slash '/'
        return getPackageAsPath(buildHierarchyPackage(map));
    }

    public String buildHierarchyPackage(Map<String, Object> map) { // public for compatible
        final String additionalPkg = (String) map.get("additionalPackage");
        final String added = Srl.is_NotNull_and_NotEmpty(additionalPkg) ? "." + additionalPkg : "";
        return _output.getPackage() + added;
    }

    protected String getPackageAsPath(String pkg) {
        return _packagePathHandler.getPackageAsPath(pkg);
    }

    public String getGenerateFilePath() { // for only-one table
        return getGenerateDirPath() + _output.getClassName() + "." + _output.getFileExt();
    }

    public String getTemplatePath() { // for only-one table or multiple table (not for one-to-free)
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
        return "{" + _requestName + ", " + _resource + ", " + _output + ", " + _metaData + "}";
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
    public DfFreeGenResource getResource() { // not null
        // basically unneeded to be called from template but it may be used for resolveBaseDir()
        return _resource;
    }

    public DfFreeGenResourceType getResourceType() { // not null
        return _resource.getResourceType();
    }

    public String getResourceFile() { // not null
        return _resource.getResourceFile();
    }

    public String getResourceFilePureName() { // not null
        return _resource.getResourceFilePureName();
    }

    // -----------------------------------------------------
    //                                                Output
    //                                                ------
    public DfFreeGenOutput getOutput() { // not null (basically unneeded to be called from template)
        return _output;
    }

    public String getOutputDirectory() { // not null
        return _output.getOutputDirectory();
    }

    public String getPackage() { // not null
        return _output.getPackage();
    }

    public String getTemplateFile() { // for one-to-one, one-to-many classes, null allowed when one-to-free classes
        return _output.getTemplateFile();
    }

    public String getClassName() { // for one-to-one class, null allowed when one-to-many or one-to-free class
        return _output.getClassName();
    }

    public String getFileExt() { // not null
        return _output.getFileExt();
    }

    // -----------------------------------------------------
    //                                            Option Map
    //                                            ----------
    public Map<String, Object> getOptionMap() { // not null
        return _metaData.getOptionMap();
    }

    @Deprecated
    public Map<String, Object> getTableMap() { // for compatible
        return _metaData.getOptionMap();
    }

    // -----------------------------------------------------
    //                                             Meta Data
    //                                             ---------
    public DfFreeGenMetaData getMetaData() { // not null (basically unneeded to be called from template)
        return _metaData;
    }

    public boolean isOnlyOneTable() { // only-one table? (or multiple?)
        return _metaData.isOnlyOneTable();
    }

    public Map<String, Object> getTable() { // for only-one table
        final Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("tableName", _metaData.getTableName());
        map.put("columnList", _metaData.getColumnList());
        map.put("isOnlyOneTable", _metaData.isOnlyOneTable()); // for compatible
        map.put("tableMap", _metaData.getOptionMap()); // for compatible
        map.put("schemaMap", _metaData.getSchemaMap()); // for compatible
        map.put("tableList", _metaData.getTableList()); // for compatible
        return map;
    }

    public List<Map<String, Object>> getTableList() { // for multiple table
        return _metaData.getTableList();
    }

    // -----------------------------------------------------
    //                                        for compatible
    //                                        --------------
    @Deprecated
    public String getTableName() { // for only-one table
        return _metaData.getTableName();
    }

    @Deprecated
    public List<Map<String, Object>> getColumnList() { // for compatible, for only-one table
        return _metaData.getColumnList();
    }

    // -----------------------------------------------------
    //                                                Setter
    //                                                ------
    public void setMetaData(DfFreeGenMetaData metaData) {
        _metaData = metaData;
    }

    public void setPackagePathHandler(DfPackagePathHandler packagePathHandler) {
        _packagePathHandler = packagePathHandler;
    }
}
