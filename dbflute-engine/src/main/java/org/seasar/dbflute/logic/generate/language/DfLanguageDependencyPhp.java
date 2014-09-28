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
import org.seasar.dbflute.logic.generate.language.framework.DfLanguageFrameworkPhp;
import org.seasar.dbflute.logic.generate.language.grammar.DfLanguageGrammar;
import org.seasar.dbflute.logic.generate.language.grammar.DfLanguageGrammarPhp;
import org.seasar.dbflute.logic.generate.language.implstyle.DfLanguageImplStyle;
import org.seasar.dbflute.logic.generate.language.implstyle.DfLanguageImplStylePhp;
import org.seasar.dbflute.logic.generate.language.pkgstyle.DfLanguageClassPackage;
import org.seasar.dbflute.logic.generate.language.pkgstyle.DfLanguageClassPackagePhp;
import org.seasar.dbflute.logic.generate.language.pkgstyle.DfLanguagePropertyPackageResolver;
import org.seasar.dbflute.logic.generate.language.pkgstyle.DfLanguagePropertyPackageResolverPhp;
import org.seasar.dbflute.logic.generate.language.typemapping.DfLanguageTypeMapping;
import org.seasar.dbflute.logic.generate.language.typemapping.DfLanguageTypeMappingPhp;

/**
 * @author jflute
 */
public class DfLanguageDependencyPhp implements DfLanguageDependency {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String PATH_MAVEN_SRC_MAIN_PHP = "src/main/php";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfLanguageGrammar _grammar = new DfLanguageGrammarPhp();
    protected final DfLanguageTypeMapping _typeMapping = new DfLanguageTypeMappingPhp();
    protected final DfLanguageFramework _framework = new DfLanguageFrameworkPhp();
    protected final DfLanguageImplStyle _implStyle = new DfLanguageImplStylePhp();
    protected final DfLanguageClassPackage _classPackage = new DfLanguageClassPackagePhp();
    protected final DfLanguagePropertyPackageResolver _packageResolver = new DfLanguagePropertyPackageResolverPhp();

    // ===================================================================================
    //                                                                               Basic
    //                                                                               =====
    public String getLanguageTitle() {
        return "Php";
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
        return PATH_MAVEN_SRC_MAIN_PHP;
    }

    public String getMainResourceDirectory() {
        return getMainProgramDirectory();
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
        throw new UnsupportedOperationException("Unsupported language Php");
    }

    public String getGenerateControlBhvAp() {
        throw new UnsupportedOperationException("Unsupported language Php");
    }

    public String getSql2EntityControl() {
        throw new UnsupportedOperationException("Unsupported language Php");
    }

    public String getOutsideSqlDirectory() {
        return getMainProgramDirectory();
    }

    public String convertToSecondaryOutsideSqlDirectory(String sqlDirectory) {
        return null; // no secondary
    }

    public String getGenerateOutputDirectory() {
        return "../" + getMainProgramDirectory();
    }

    public String getResourceOutputDirectory() {
        return "";
    }

    public String getTemplateFileExtension() {
        return "vmphp";
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
