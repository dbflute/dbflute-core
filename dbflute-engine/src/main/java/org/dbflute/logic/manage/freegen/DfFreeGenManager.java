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
package org.dbflute.logic.manage.freegen;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.velocity.texen.util.FileUtil;
import org.dbflute.DfBuildProperties;
import org.dbflute.friends.velocity.DfGenerator;
import org.dbflute.properties.DfAllClassCopyrightProperties;
import org.dbflute.properties.DfBasicProperties;
import org.dbflute.properties.DfDocumentProperties;
import org.dbflute.properties.DfLastaFluteProperties;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 */
public class DfFreeGenManager {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(DfFreeGenManager.class);

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfFreeGenManager() {
    }

    // ===================================================================================
    //                                                                           Generator
    //                                                                           =========
    public DfGenerator getGenerator() {
        return DfGenerator.getInstance();
    }

    // ===================================================================================
    //                                                                           Directory
    //                                                                           =========
    public void setOutputDirectory(String outputDirectory) {
        final DfGenerator generator = getGenerator();
        final String existingPath = generator.getOutputPath();
        if (existingPath != null && existingPath.equals(outputDirectory)) {
            return;
        }
        _log.info("...Setting up generateOutputDirectory: " + outputDirectory);
        generator.setOutputPath(outputDirectory);
    }

    public void makeDirectory(String filePath) { // may be slash or back-slash see DfPackagePathHandler
        final String fileName = Srl.substringLastRear(filePath, "/", "\\");
        final String realPath;
        if (fileName.contains(".")) { // may be file
            realPath = Srl.substringLastFront(filePath, "/", "\\");
        } else {
            realPath = filePath;
        }
        FileUtil.mkdir(getGenerator().getOutputPath() + "/" + realPath);
    }

    // ===================================================================================
    //                                                                          Basic Info
    //                                                                          ==========
    public boolean isTargetContainerSeasar() {
        return getBasicProperties().isTargetContainerSeasar();
    }

    public boolean isTargetContainerSpring() {
        return getBasicProperties().isTargetContainerSpring();
    }

    public boolean isTargetContainerGuice() {
        return getBasicProperties().isTargetContainerGuice();
    }

    public boolean isTargetContainerCDI() {
        return getBasicProperties().isTargetContainerCDI();
    }

    public boolean isTargetContainerLastaDi() {
        return getBasicProperties().isTargetContainerLastaDi();
    }

    // ===================================================================================
    //                                                                           Copyright
    //                                                                           =========
    public String getAllClassCopyright() {
        return getAllClassCopyrightProperties().getAllClassCopyright();
    }

    // ===================================================================================
    //                                                                            LastaDoc
    //                                                                            ========
    public List<String> getLastaDocHtmlPathList() {
        return getLastaFluteProperties().getLastaDocHtmlPathList();
    }

    public String getLastaDocHtmlMarkFreeGenDocNaviLink() {
        return getLastaFluteProperties().getLastaDocHtmlMarkFreeGenDocNaviLink();
    }

    public String getLastaDocHtmlMarkFreeGenDocBody() {
        return getLastaFluteProperties().getLastaDocHtmlMarkFreeGenDocBody();
    }

    // ===================================================================================
    //                                                                      Resolve Helper
    //                                                                      ==============
    public String htmlEscape(String text) { // made from lasta-doc
        return resolveTextForSchemaHtml(text); // borrow it
    }

    public String htmlEscapeAsId(String text) { // made from lasta-doc
        final Map<String, String> fromToMap = new LinkedHashMap<String, String>();
        fromToMap.put("/", "."); // e.g. sea/land => sea.land
        fromToMap.put("{", "_b_"); // e.g. sea/{land} => sea._b_land_e_ (begin/end)
        fromToMap.put("}", "_e_");
        fromToMap.put("$", "_d_"); // e.g. get$index => get_d_index  (dollar)
        return Srl.replaceBy(htmlEscape(text), fromToMap);
    }

    public String resolveTextForSchemaHtml(String text) { // public for compatible
        final DfDocumentProperties prop = getDocumentProperties();
        return prop.resolveSchemaHtmlContent(text);
    }

    public String resolveTextForJavaDoc(String text, String indent) {
        final DfDocumentProperties prop = getDocumentProperties();
        return prop.resolveJavaDocContent(text, indent);
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
    //                                                                       Script Helper
    //                                                                       =============
    public ScriptEngine createJavaScriptEngine() { // e.g. remote-api generate
        return new ScriptEngineManager().getEngineByName("JavaScript");
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBasicProperties getBasicProperties() {
        return getProperties().getBasicProperties();
    }

    protected DfDocumentProperties getDocumentProperties() {
        return getProperties().getDocumentProperties();
    }

    protected DfAllClassCopyrightProperties getAllClassCopyrightProperties() {
        return getProperties().getAllClassCopyrightProperties();
    }

    protected DfLastaFluteProperties getLastaFluteProperties() {
        return getProperties().getLastaFluteProperties();
    }

    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    // ===================================================================================
    //                                                                             Logging
    //                                                                             =======
    public void info(String msg) {
        _log.info(msg);
    }
}
