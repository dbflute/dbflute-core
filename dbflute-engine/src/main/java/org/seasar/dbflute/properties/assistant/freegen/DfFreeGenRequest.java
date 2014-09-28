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
package org.seasar.dbflute.properties.assistant.freegen;

import java.util.List;
import java.util.Map;

import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.logic.generate.packagepath.DfPackagePathHandler;
import org.seasar.dbflute.properties.DfBasicProperties;

/**
 * @author jflute
 */
public class DfFreeGenRequest {

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
    public DfFreeGenRequest(DfFreeGenManager manager, String requestName, DfFreeGenResource resource,
            DfFreeGenOutput output) {
        _manager = manager;
        _requestName = requestName;
        _resource = resource;
        _output = output;
    }

    // ===================================================================================
    //                                                                        ResourceType
    //                                                                        ============
    public enum DfFreeGenerateResourceType {
        PROP, XLS, FILE_PATH, JSON_KEY, JSON_SCHEMA, SOLR
    }

    public boolean isResourceTypeProp() {
        return _resource.isResourceTypeProp();
    }

    public boolean isResourceTypeXls() {
        return _resource.isResourceTypeXls();
    }

    public boolean isResourceTypeFilePath() {
        return _resource.isResourceTypeFilePath();
    }

    public boolean isResourceTypeJsonKey() {
        return _resource.isResourceTypeJsonKey();
    }

    public boolean isResourceTypeJsonSchema() {
        return _resource.isResourceTypeJsonSchema();
    }

    public boolean isResourceTypeSolr() {
        return _resource.isResourceTypeSolr();
    }

    // ===================================================================================
    //                                                                                Path
    //                                                                                ====
    public void enableOutputDirectory() {
        _manager.setOutputDirectory(_output.getOutputDirectory());
    }

    public String getGenerateDirPath() {
        return getPackageAsPath(_output.getPackage());
    }

    public String getGenerateFilePath() {
        final DfBasicProperties basicProp = DfBuildProperties.getInstance().getBasicProperties();
        final String classExt = basicProp.getLanguageDependency().getLanguageGrammar().getClassFileExtension();
        return getGenerateDirPath() + "/" + _output.getClassName() + "." + classExt;
    }

    protected String getPackageAsPath(String pkg) {
        return _packagePathHandler.getPackageAsPath(pkg);
    }

    public String getTemplatePath() {
        return _output.getTemplateFile();
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

    public DfFreeGenResource getResource() {
        return _resource;
    }

    public DfFreeGenerateResourceType getResourceType() {
        return _resource.getResourceType();
    }

    public String getResourceFile() {
        return _resource.getResourceFile();
    }

    public String getResourceFilePureName() {
        return _resource.getResourceFilePureName();
    }

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

    public String getClassName() {
        return _output.getClassName();
    }

    public DfFreeGenTable getTable() {
        return _table;
    }

    public Map<String, Object> getTableMap() {
        return _table.getTableMap();
    }

    public boolean isOnlyOneTable() {
        return _table.isOnlyOneTable();
    }

    public String getTableName() {
        return _table.getTableName();
    }

    public List<Map<String, Object>> getColumnList() {
        return _table.getColumnList();
    }

    public List<Map<String, Object>> getTableList() {
        return _table.getTableList();
    }

    public void setTable(DfFreeGenTable table) {
        _table = table;
    }

    public void setPackagePathHandler(DfPackagePathHandler packagePathHandler) {
        _packagePathHandler = packagePathHandler;
    }
}
