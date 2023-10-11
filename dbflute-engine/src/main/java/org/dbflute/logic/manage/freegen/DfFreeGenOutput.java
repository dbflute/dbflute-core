/*
 * Copyright 2014-2023 the original author or authors.
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

/**
 * @author jflute
 */
public class DfFreeGenOutput {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _outputDirectory; // not null
    protected final String _package; // not null
    protected final String _templateFile; // for one-to-one, one-to-many, null allowed: when one-to-free
    protected final String _className; // for one-to-one, null allowed: when one-to-many, one-to-free
    protected final String _fileExt; // not null (has default)

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfFreeGenOutput(String outputDirectory, String pkg, String templateFile, String className, String fileExt) {
        _outputDirectory = outputDirectory;
        _package = pkg;
        _templateFile = templateFile;
        _className = className;
        _fileExt = fileExt;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "{output=" + _outputDirectory + ", package=" + _package + ", template=" + _templateFile + ", class=" + _className + ", "
                + _fileExt + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getOutputDirectory() { // not null
        return _outputDirectory;
    }

    public String getPackage() { // not null
        return _package;
    }

    public String getTemplateFile() { // for one-to-one, one-to-many, null allowed when one-to-free
        return _templateFile;
    }

    public String getClassName() { // for one-to-one, null allowed when one-to-many, one-to-free
        return _className;
    }

    public String getFileExt() { // not null
        return _fileExt;
    }
}
