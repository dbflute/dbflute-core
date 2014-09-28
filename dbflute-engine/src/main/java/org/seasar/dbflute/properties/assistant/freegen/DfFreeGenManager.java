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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.texen.util.FileUtil;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.friends.velocity.DfGenerator;
import org.seasar.dbflute.properties.DfDocumentProperties;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfFreeGenManager {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log _log = LogFactory.getLog(DfFreeGenManager.class);

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfFreeGenManager() {
    }

    // ===================================================================================
    //                                                                           Generator
    //                                                                           =========
    public DfGenerator getGeneratorInstance() {
        return DfGenerator.getInstance();
    }

    // ===================================================================================
    //                                                                           Directory
    //                                                                           =========
    public void setOutputDirectory(String outputDirectory) {
        _log.info("...Setting up generateOutputDirectory: " + outputDirectory);
        getGeneratorInstance().setOutputPath(outputDirectory);
    }

    public void makeDirectory(String filePath) {
        final String basePath = Srl.substringLastFront(filePath, "/");
        FileUtil.mkdir(getGeneratorInstance().getOutputPath() + "/" + basePath);
    }

    // ===================================================================================
    //                                                                      Resolve Helper
    //                                                                      ==============
    public String resolveTextForSchemaHtml(String text) {
        final DfDocumentProperties prop = DfBuildProperties.getInstance().getDocumentProperties();
        return prop.resolveTextForSchemaHtml(text);
    }

    public String resolveTextForJavaDoc(String text, String indent) {
        final DfDocumentProperties prop = DfBuildProperties.getInstance().getDocumentProperties();
        return prop.resolveTextForJavaDoc(text, indent);
    }

    // ===================================================================================
    //                                                                      Convert Helper
    //                                                                      ==============
    public String initCap(String decamelName) {
        return Srl.initCap(decamelName);
    }

    public String initUncap(String decamelName) {
        return Srl.initUncap(decamelName);
    }

    public String camelize(String decamelName) {
        return Srl.camelize(decamelName);
    }

    public String decamelize(String camelName) {
        return Srl.decamelize(camelName);
    }

    // ===================================================================================
    //                                                                             Logging
    //                                                                             =======
    public void info(String msg) {
        _log.info(msg);
    }
}
