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
package org.seasar.dbflute.logic.generate.language.pkgstyle;

/**
 * @author jflute
 */
public class DfLanguageClassPackageCSharp implements DfLanguageClassPackage {

    // ===================================================================================
    //                                                                               Basic
    //                                                                               =====
    public String getBaseCommonPackage() {
        return "AllCommon";
    }

    public String getBaseBehaviorPackage() {
        return "BsBhv";
    }

    public String getBaseDaoPackage() {
        return "BsDao";
    }

    public String getCursorSimplePackage() {
        return "Cursor";
    }

    public String getReferrerLoaderSimplePackage() {
        return "Loader";
    }

    public String getParameterBeanSimplePackage() {
        return "PmBean";
    }

    public String getBaseEntityPackage() {
        return "BsEntity";
    }

    public String getCustomizeEntitySimplePackage() {
        return "Customize";
    }

    public String getDBMetaSimplePackage() {
        return "Dbm";
    }

    public String getConditionBeanPackage() {
        return "CBean";
    }

    public String getExtendedBehaviorPackage() {
        return "ExBhv";
    }

    public String getExtendedDaoPackage() {
        return "ExDao";
    }

    public String getExtendedEntityPackage() {
        return "ExEntity";
    }

    // ===================================================================================
    //                                                                             Various
    //                                                                             =======
    public String buildExtendedBehaviorPackageMark(String sqlPackage, String exbhvName) {
        // because CSharp is allowed to have free directory structure
        return "/" + exbhvName + "/";
    }

    public String buildAccessContextFqcn(String baseCommonPackage, String projectPrefix) {
        return baseCommonPackage + "." + projectPrefix + "AccessContext";
    }
}