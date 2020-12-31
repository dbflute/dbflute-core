/*
 * Copyright 2014-2020 the original author or authors.
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
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.properties.DfAllClassCopyrightProperties;
import org.dbflute.properties.DfBasicProperties;
import org.dbflute.properties.DfDocumentProperties;
import org.dbflute.properties.DfLastaFluteProperties;
import org.dbflute.util.DfTypeUtil;
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

    public boolean isTargetContainerMicronaut() {
        return getBasicProperties().isTargetContainerMicronaut();
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
    // -----------------------------------------------------
    //                                                String
    //                                                ------
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

    // -----------------------------------------------------
    //                                                Number
    //                                                ------
    public Integer toInteger(Object numberObj) { // e.g. 12.0 to 12 (for Gson headache)
        return DfTypeUtil.toInteger(numberObj);
    }

    // ===================================================================================
    //                                                                       Script Helper
    //                                                                       =============
    // #hope switch to simple JSON interface for FreeGen frameworks (e.g. KVSFlute) by jflute (2020/12/31)
    public ScriptEngine createJavaScriptEngine() { // as public engine e.g. remote-api generate
        // should use Ant class loader to find 'sai' engine in 'extlib' directory
        // because default constructor of the manager uses only System class loader
        // (also DfFrgJavaScriptJsonEngine)
        final ScriptEngineManager manager = new ScriptEngineManager(getClass().getClassLoader());
        ScriptEngine engine = manager.getEngineByName("sai");
        if (engine == null) {
            engine = manager.getEngineByName("JavaScript"); // original code until 1.2.3
        }
        if (engine == null) {
            throwJsonScriptPublicEngineNotFoundException();
        }
        return engine;
    }

    protected void throwJsonScriptPublicEngineNotFoundException() {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the public engine of JSON script for FreeGen template.");
        br.addItem("Advice");
        br.addElement("Nashorn (JavaScript engine) is removed since Java15.");
        br.addElement("");
        br.addElement("You can use 'sai' instead of Nashorn.");
        br.addElement(" https://github.com/codelibs/sai");
        br.addElement("");
        br.addElement("Put the jar files (including dependencies)");
        br.addElement("on 'extlib' directory of your DBFlute client.");
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
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
