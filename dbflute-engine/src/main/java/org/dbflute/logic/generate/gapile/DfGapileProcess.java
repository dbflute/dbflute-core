/*
 * Copyright 2014-2016 the original author or authors.
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
package org.dbflute.logic.generate.gapile;

import org.dbflute.DfBuildProperties;
import org.dbflute.logic.generate.language.DfLanguageDependency;
import org.dbflute.logic.generate.language.grammar.DfLanguageGrammar;
import org.dbflute.logic.generate.language.pkgstyle.DfLanguageClassPackage;
import org.dbflute.properties.DfBasicProperties;

/**
 * @author jflute
 * @since 1.1.2 (2016/08/25 Thursday)
 */
public class DfGapileProcess {

    // ===================================================================================
    //                                                                             Reflect
    //                                                                             =======
    public void reflectIfNeeds() {
        if (!getBasicProperties().isGenerationGapileValid()) {
            return;
        }
        final String gapileDirectory = getBasicProperties().getGenerationGapileDirectory(); // not null here
        final DfGapileClassReflector reflector = createReflector(gapileDirectory);
        reflector.reflect();
    }

    protected DfGapileClassReflector createReflector(String gapileDirectory) {
        final DfBasicProperties prop = getBasicProperties();
        final String outputDirectory = prop.getGenerateOutputDirectory();
        final String packageBase = prop.getPackageBase();
        final DfLanguageDependency lang = prop.getLanguageDependency();
        final DfLanguageClassPackage classPackage = lang.getLanguageClassPackage();
        final DfLanguageGrammar grammar = lang.getLanguageGrammar();
        return new DfGapileClassReflector(outputDirectory, packageBase, classPackage, grammar, gapileDirectory);
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfBasicProperties getBasicProperties() {
        return getProperties().getBasicProperties();
    }
}
