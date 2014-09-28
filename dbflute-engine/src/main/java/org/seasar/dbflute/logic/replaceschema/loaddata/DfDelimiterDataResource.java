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
package org.seasar.dbflute.logic.replaceschema.loaddata;

/**
 * @author jflute
 */
public class DfDelimiterDataResource {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String _loadType;
    protected String _basePath;
    protected String _fileType;
    protected String _delimiter;

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getLoadType() {
        return _loadType;
    }

    public void setLoadType(String loadType) {
        this._loadType = loadType;
    }

    public String getBasePath() {
        return _basePath;
    }

    public void setBasePath(String basePath) {
        this._basePath = basePath;
    }

    public String getFileType() {
        return _fileType;
    }

    public void setFileType(String fileType) {
        this._fileType = fileType;
    }

    public String getDelimiter() {
        return _delimiter;
    }

    public void setDelimiter(String delimter) {
        this._delimiter = delimter;
    }
}
