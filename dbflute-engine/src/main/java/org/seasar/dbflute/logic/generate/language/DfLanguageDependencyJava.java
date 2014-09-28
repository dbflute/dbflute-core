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
package org.seasar.dbflute.logic.generate.language;

import java.io.File;

import org.seasar.dbflute.logic.generate.language.framework.DfLanguageFramework;
import org.seasar.dbflute.logic.generate.language.framework.DfLanguageFrameworkJava;
import org.seasar.dbflute.logic.generate.language.grammar.DfLanguageGrammar;
import org.seasar.dbflute.logic.generate.language.grammar.DfLanguageGrammarJava;
import org.seasar.dbflute.logic.generate.language.implstyle.DfLanguageImplStyle;
import org.seasar.dbflute.logic.generate.language.implstyle.DfLanguageImplStyleJava;
import org.seasar.dbflute.logic.generate.language.pkgstyle.DfLanguageClassPackage;
import org.seasar.dbflute.logic.generate.language.pkgstyle.DfLanguageClassPackageJava;
import org.seasar.dbflute.logic.generate.language.pkgstyle.DfLanguagePropertyPackageResolver;
import org.seasar.dbflute.logic.generate.language.pkgstyle.DfLanguagePropertyPackageResolverJava;
import org.seasar.dbflute.logic.generate.language.typemapping.DfLanguageTypeMapping;
import org.seasar.dbflute.logic.generate.language.typemapping.DfLanguageTypeMappingJava;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfLanguageDependencyJava implements DfLanguageDependency {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String PATH_MAVEN_SRC_MAIN_JAVA = "src/main/java";
    protected static final String PATH_MAVEN_SRC_MAIN_RESOURCES = "src/main/resources";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfLanguageGrammar _grammar = new DfLanguageGrammarJava();
    protected final DfLanguageTypeMapping _typeMapping = new DfLanguageTypeMappingJava();
    protected final DfLanguageFramework _framework = new DfLanguageFrameworkJava();
    protected final DfLanguageImplStyle _implStyle = new DfLanguageImplStyleJava();
    protected final DfLanguageClassPackage _classPackage = new DfLanguageClassPackageJava();
    protected final DfLanguagePropertyPackageResolver _packageResolver = new DfLanguagePropertyPackageResolverJava();

    // ===================================================================================
    //                                                                               Basic
    //                                                                               =====
    public String getLanguageTitle() {
        return "Java";
    }

    // ===================================================================================
    //                                                                    Program Handling
    //                                                                    ================
    public DfLanguageGrammar getLanguageGrammar() {
        return _grammar;
    }

    public DfLanguageTypeMapping getLanguageTypeMapping() {
        return _typeMapping;
    }

    public DfLanguageFramework getLanguageFramework() {
        return _framework;
    }

    public DfLanguageImplStyle getLanguageImplStyle() {
        return _implStyle;
    }

    // ===================================================================================
    //                                                                 Compile Environment
    //                                                                 ===================
    public String getMainProgramDirectory() {
        return PATH_MAVEN_SRC_MAIN_JAVA;
    }

    public String getMainResourceDirectory() {
        return PATH_MAVEN_SRC_MAIN_RESOURCES;
    }

    public boolean isCompileTargetFile(File file) {
        return true;
    }

    public boolean isFlatOrOmitDirectorySupported() {
        return false;
    }

    // ===================================================================================
    //                                                                Generate Environment
    //                                                                ====================
    public String getGenerateControl() {
        return "om/ControlGenerateJava.vm";
    }

    public String getGenerateControlBhvAp() {
        return "om/java/plugin/bhvap/ControlBhvApJava.vm";
    }

    public String getSql2EntityControl() {
        return "om/ControlSql2EntityJava.vm";
    }

    public String getGenerateOutputDirectory() {
        return "../" + getMainProgramDirectory();
    }

    public String getResourceOutputDirectory() {
        return "../resources";
    }

    public String getOutsideSqlDirectory() {
        // returns program directory
        // because it is possible that resources directory does not prepared
        // and resources directory is resolved later
        return getMainProgramDirectory();
    }

    public String convertToSecondaryOutsideSqlDirectory(String sqlDirectory) {
        final String mainProgramDirectory = getMainProgramDirectory();
        if (!sqlDirectory.contains(mainProgramDirectory)) {
            return null; // no secondary
        }
        return Srl.replace(sqlDirectory, mainProgramDirectory, getMainResourceDirectory());
    }

    public String getTemplateFileExtension() {
        return "vm";
    }

    public String getSourceCodeLineSeparator() {
        return "\n";
    }

    public DfLanguageClassPackage getLanguageClassPackage() {
        return _classPackage;
    }

    public DfLanguagePropertyPackageResolver getLanguagePropertyPackageResolver() {
        return _packageResolver;
    }
}
