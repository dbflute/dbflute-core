/*
 * Copyright 2014-2021 the original author or authors.
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

import org.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfFreeGenResource {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _baseDir; // null allowed
    protected final DfFreeGenResourceType _resourceType; // not null
    protected final String _resourceFile; // not null
    protected final String _encoding; // null allowed

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfFreeGenResource(String baseDir, DfFreeGenResourceType resourceType, String resourceFile, String encoding) {
        _baseDir = baseDir;
        _resourceType = resourceType;
        _resourceFile = resolveBaseDir(resourceFile);
        _encoding = encoding;
    }

    // ===================================================================================
    //                                                                  Directory Resolver
    //                                                                  ==================
    public String resolveBaseDir(String path) {
        if (path != null && _baseDir != null) {
            return Srl.replace(path, "$$baseDir$$", _baseDir);
        }
        return path;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "{resourceType=" + _resourceType + ", resourceFile=" + _resourceFile + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getBaseDir() {
        return _baseDir;
    }

    public DfFreeGenResourceType getResourceType() {
        return _resourceType;
    }

    public String getResourceFile() {
        return _resourceFile;
    }

    public String getResourceFilePureName() {
        return _resourceFile != null ? Srl.substringLastRear(_resourceFile, "/") : _resourceFile;
    }

    public boolean hasEncoding() {
        return _encoding != null;
    }

    public String getEncoding() {
        return _encoding;
    }
}
