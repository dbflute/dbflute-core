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
import org.seasar.dbflute.logic.generate.language.grammar.DfLanguageGrammar;
import org.seasar.dbflute.logic.generate.language.implstyle.DfLanguageImplStyle;
import org.seasar.dbflute.logic.generate.language.pkgstyle.DfLanguageClassPackage;
import org.seasar.dbflute.logic.generate.language.pkgstyle.DfLanguagePropertyPackageResolver;
import org.seasar.dbflute.logic.generate.language.typemapping.DfLanguageTypeMapping;

/**
 * @author jflute
 */
public interface DfLanguageDependency {

    // ===================================================================================
    //                                                                               Basic
    //                                                                               =====
    /**
     * @return The title expression for the language. (NotNull)
     */
    String getLanguageTitle();

    // ===================================================================================
    //                                                                    Program Handling
    //                                                                    ================
    /**
     * @return The information of target language grammar. (NotNull)
     */
    DfLanguageGrammar getLanguageGrammar();

    /**
     * @return The information of type mapping. (NotNull)
     */
    DfLanguageTypeMapping getLanguageTypeMapping();

    /**
     * @return The information object of framework. (NotNull)
     */
    DfLanguageFramework getLanguageFramework();

    /**
     * @return The information object of implementation style. (NotNull)
     */
    DfLanguageImplStyle getLanguageImplStyle();

    // ===================================================================================
    //                                                                 Compile Environment
    //                                                                 ===================
    /**
     * @return The directory for main program. (NotNull)
     */
    String getMainProgramDirectory();

    /**
     * @return The directory for main resources. (NotNull: might be same as program directory)
     */
    String getMainResourceDirectory();

    /**
     * @param file The file. (NotNull)
     * @return Is the file compile target?
     */
    boolean isCompileTargetFile(File file); // basically for CSharp

    /**
     * @return Is the flat or omit directory supported?
     */
    boolean isFlatOrOmitDirectorySupported(); // basically for CSharp

    // ===================================================================================
    //                                                                Generate Environment
    //                                                                ====================
    /**
     * @return The path of velocity control file for generate. (NotNull) 
     */
    String getGenerateControl();

    /**
     * @return The path of velocity control file for application behavior generate. (NotNull) 
     */
    String getGenerateControlBhvAp();

    /**
     * @return The path of velocity control file for sql2entity. (NotNull) 
     */
    String getSql2EntityControl();

    /**
     * @return The directory for generate output. (NotNull)
     */
    String getGenerateOutputDirectory();

    /**
     * @return The relative path (from generate output directory) of directory for resource output. (NotNull)
     */
    String getResourceOutputDirectory();

    /**
     * @return The directory for outside SQL. (NotNull)
     */
    String getOutsideSqlDirectory();

    /**
     * @param sqlDirectory The primary SQL directory. (NotNull)
     * @return The converted SQL directory for secondary. (NullAllowed: when no secondary)
     */
    String convertToSecondaryOutsideSqlDirectory(String sqlDirectory);

    /**
     * @return The file extension of template. (NotNull)
     */
    String getTemplateFileExtension();

    /**
     * @return The string of line separator. (NotNull)
     */
    String getSourceCodeLineSeparator();

    /**
     * @return The information object of generated class package. (NotNull)
     */
    DfLanguageClassPackage getLanguageClassPackage();

    /**
     * @return The resolver of property type's package. (NotNull)
     */
    DfLanguagePropertyPackageResolver getLanguagePropertyPackageResolver();
}
