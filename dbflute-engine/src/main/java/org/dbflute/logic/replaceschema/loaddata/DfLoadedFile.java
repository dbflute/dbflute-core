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
package org.dbflute.logic.replaceschema.loaddata;

/**
 * @author jflute
 */
public class DfLoadedFile {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _envType;
    protected final String _fileType;
    protected final String _encoding;
    protected final String _fileName;
    protected final boolean _warned;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    public DfLoadedFile(String envType, String fileType, String encoding, String fileName, boolean warned) {
        _envType = envType;
        _fileType = fileType;
        _encoding = encoding;
        _fileName = fileName;
        _warned = warned;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getEnvType() {
        return _envType;
    }

    public String getFileType() {
        return _fileType;
    }

    public String getEncoding() {
        return _encoding;
    }

    public String getFileName() {
        return _fileName;
    }

    public boolean isWarned() {
        return _warned;
    }
}
