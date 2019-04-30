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
package org.dbflute.properties.propreader;

import java.util.List;

import org.dbflute.infra.dfprop.DfPropFile;

/**
 * @author jflute
 * @since 0.6.8 (2008/03/31 Monday)
 */
public class DfOutsideListPropReader {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfPropFile _dfpropFile = createDfPropFile();
    protected boolean _returnsNullIfNotFound;
    protected boolean _skipLineSeparator;

    // ===================================================================================
    //                                                                                Read
    //                                                                                ====
    public List<Object> readList(String path, String environmentType) {
        return _dfpropFile.readList(path, environmentType);
    }

    // ===================================================================================
    //                                                                         DfProp File
    //                                                                         ===========
    protected DfPropFile createDfPropFile() {
        final DfPropFile file = newDfPropFile();
        if (_returnsNullIfNotFound) {
            file.returnsNullIfNotFound();
        }
        if (_skipLineSeparator) {
            file.skipLineSeparator();
        }
        file.checkDuplicateEntry();
        return file;
    }

    protected DfPropFile newDfPropFile() {
        return new DfPropFile();
    }

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    public DfOutsideListPropReader returnsNullIfNotFound() {
        _returnsNullIfNotFound = true;
        return this;
    }

    public DfOutsideListPropReader skipLineSeparator() {
        _skipLineSeparator = true;
        return this;
    }
}