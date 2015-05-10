/*
 * Copyright 2014-2015 the original author or authors.
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
    protected final String _templateFile;
    protected final String _outputDirectory;
    protected final String _package;
    protected final String _className; // null allowed: when table list
    protected final String _fileExt;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfFreeGenOutput(String templateFile, String outputDirectory, String pkg, String className, String fileExt) {
        _templateFile = templateFile;
        _outputDirectory = outputDirectory;
        _package = pkg;
        _className = className;
        _fileExt = fileExt;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "{templateFile=" + _templateFile + ", outputDirectory=" + _outputDirectory + ", package=" + _package + ", className="
                + _className + ", " + _fileExt + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getTemplateFile() {
        return _templateFile;
    }

    public String getOutputDirectory() {
        return _outputDirectory;
    }

    public String getPackage() {
        return _package;
    }

    public String getClassName() {
        return _className;
    }

    public String getFileExt() {
        return _fileExt;
    }
}
