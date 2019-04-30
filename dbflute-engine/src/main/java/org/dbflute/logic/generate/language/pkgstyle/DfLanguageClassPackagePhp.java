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
package org.dbflute.logic.generate.language.pkgstyle;

/**
 * @author jflute
 */
public class DfLanguageClassPackagePhp implements DfLanguageClassPackage {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfLanguageClassPackageJava classPackageJava = new DfLanguageClassPackageJava();

    // ===================================================================================
    //                                                                         Basic Class
    //                                                                         ===========
    @Override
    public String buildCDefPureClassName(String projectPrefix, String allcommonPrefix) {
        return classPackageJava.buildCDefPureClassName(projectPrefix, allcommonPrefix);
    }

    // ===================================================================================
    //                                                                               Basic
    //                                                                               =====
    public String getBaseCommonPackage() {
        return "allcommon";
    }

    public String getBaseBehaviorPackage() {
        return "bsbhv";
    }

    public String getBaseDaoPackage() {
        return "bsdao";
    }

    public String getCursorSimplePackage() {
        return "cursor";
    }

    public String getReferrerLoaderSimplePackage() {
        return "loader";
    }

    public String getParameterBeanSimplePackage() {
        return "pmbean";
    }

    public String getBaseEntityPackage() {
        return "bsentity";
    }

    public String getCustomizeEntitySimplePackage() {
        return "customize";
    }

    public String getDBMetaSimplePackage() {
        return "dbmeta";
    }

    public String getConditionBeanPackage() {
        return "cbean";
    }

    public String getConditionQueryPackage() {
        return getConditionBeanPackage() + ".cq";
    }

    public String getExtendedBehaviorPackage() {
        return "exbhv";
    }

    public String getExtendedDaoPackage() {
        return "exdao";
    }

    public String getExtendedEntityPackage() {
        return "exentity";
    }

    // ===================================================================================
    //                                                                             Various
    //                                                                             =======
    public String buildExtendedBehaviorPackageMark(String sqlPackage, String exbhvName) {
        return "/" + exbhvName + "/"; // unknown same as CSharp for now
    }

    public String buildAccessContextFqcn(String baseCommonPackage, String projectPrefix) {
        throw new UnsupportedOperationException("Unsupported at Php");
    }
}