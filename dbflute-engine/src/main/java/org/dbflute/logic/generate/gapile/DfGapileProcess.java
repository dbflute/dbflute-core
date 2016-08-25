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
import org.dbflute.logic.generate.language.pkgstyle.DfLanguageClassPackage;
import org.dbflute.properties.DfBasicProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.1.2 (2016/08/25 Thursday)
 */
public class DfGapileProcess {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(DfGapileProcess.class);

    // ===================================================================================
    //                                                                             Reflect
    //                                                                             =======
    public void reflectIfNeeds() {
        final String gapileDirectory = getBasicProperties().getGenerationGapileDirectory();
        if (gapileDirectory == null) {
            return; // normally here
        }
        _log.info("...Reflecting generation-gapile classes: " + gapileDirectory);
        final DfGapileClassReflector reflector = createReflector(gapileDirectory);
        reflector.reflect();
    }

    protected DfGapileClassReflector createReflector(String gapileDirectory) {
        final DfBasicProperties prop = getBasicProperties();
        final String outputDirectory = prop.getGenerateOutputDirectory();
        final String packageBase = prop.getPackageBase();
        final DfLanguageClassPackage classPackage = prop.getLanguageDependency().getLanguageClassPackage();
        return new DfGapileClassReflector(outputDirectory, packageBase, classPackage, gapileDirectory);
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
