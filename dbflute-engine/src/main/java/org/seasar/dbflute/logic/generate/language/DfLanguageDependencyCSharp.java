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
import org.seasar.dbflute.logic.generate.language.framework.DfLanguageFrameworkCSharp;
import org.seasar.dbflute.logic.generate.language.grammar.DfLanguageGrammar;
import org.seasar.dbflute.logic.generate.language.grammar.DfLanguageGrammarCSharp;
import org.seasar.dbflute.logic.generate.language.implstyle.DfLanguageImplStyle;
import org.seasar.dbflute.logic.generate.language.implstyle.DfLanguageImplStyleCSharp;
import org.seasar.dbflute.logic.generate.language.pkgstyle.DfLanguageClassPackage;
import org.seasar.dbflute.logic.generate.language.pkgstyle.DfLanguageClassPackageCSharp;
import org.seasar.dbflute.logic.generate.language.pkgstyle.DfLanguagePropertyPackageResolver;
import org.seasar.dbflute.logic.generate.language.pkgstyle.DfLanguagePropertyPackageResolverCSharp;
import org.seasar.dbflute.logic.generate.language.typemapping.DfLanguageTypeMapping;
import org.seasar.dbflute.logic.generate.language.typemapping.DfLanguageTypeMappingCSharp;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfLanguageDependencyCSharp implements DfLanguageDependency {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfLanguageGrammar _grammar = new DfLanguageGrammarCSharp();
    protected final DfLanguageTypeMapping _typeMapping = new DfLanguageTypeMappingCSharp();
    protected final DfLanguageFramework _framework = new DfLanguageFrameworkCSharp();
    protected final DfLanguageImplStyle _implStyle = new DfLanguageImplStyleCSharp();
    protected final DfLanguageClassPackage _classPackage = new DfLanguageClassPackageCSharp();
    protected final DfLanguagePropertyPackageResolver _packageResolver = new DfLanguagePropertyPackageResolverCSharp();

    // ===================================================================================
    //                                                                               Basic
    //                                                                               =====
    public String getLanguageTitle() {
        return "CSharp";
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
        return "source";
    }

    public String getMainResourceDirectory() {
        return getMainProgramDirectory();
    }

    public boolean isCompileTargetFile(File file) {
        final String absolutePath = Srl.replace(file.getAbsolutePath(), "\\", "/");
        if (absolutePath.contains("/bin/") || absolutePath.contains("/obj/")) {
            return false;
        }
        return true;
    }

    public boolean isFlatOrOmitDirectorySupported() {
        return true;
    }

    // ===================================================================================
    //                                                                Generate Environment
    //                                                                ====================
    public String getGenerateControl() {
        return "om/ControlGenerateCSharp.vm";
    }

    public String getGenerateControlBhvAp() {
        return "om/csharp/plugin/bhvap/ControlBhvApCSharp.vm";
    }

    public String getSql2EntityControl() {
        return "om/ControlSql2EntityCSharp.vm";
    }

    public String getGenerateOutputDirectory() {
        return "../" + getMainProgramDirectory();
    }

    public String getResourceOutputDirectory() {
        return "../source/${topNamespace}/Resources"; // basically unused
    }

    public String getOutsideSqlDirectory() {
        return getMainProgramDirectory();
    }

    public String convertToSecondaryOutsideSqlDirectory(String sqlDirectory) {
        return null; // no secondary
    }

    public String getTemplateFileExtension() {
        return "vmnet";
    }

    public String getSourceCodeLineSeparator() {
        return "\r\n";
    }

    public DfLanguageClassPackage getLanguageClassPackage() {
        return _classPackage;
    }

    public DfLanguagePropertyPackageResolver getLanguagePropertyPackageResolver() {
        return _packageResolver;
    }
}
