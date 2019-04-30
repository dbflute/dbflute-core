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
public interface DfLanguageClassPackage {

    // ===================================================================================
    //                                                                         Basic Class
    //                                                                         ===========
    String buildCDefPureClassName(String projectPrefix, String allcommonPrefix);

    // ===================================================================================
    //                                                                       Basic Package
    //                                                                       =============
    String getBaseCommonPackage();

    String getBaseBehaviorPackage();

    String getBaseDaoPackage();

    String getCursorSimplePackage();

    String getReferrerLoaderSimplePackage();

    String getParameterBeanSimplePackage();

    String getBaseEntityPackage();

    String getCustomizeEntitySimplePackage();

    String getDBMetaSimplePackage();

    String getConditionBeanPackage();

    String getConditionQueryPackage();

    String getExtendedBehaviorPackage();

    String getExtendedDaoPackage();

    String getExtendedEntityPackage();

    // ===================================================================================
    //                                                                             Various
    //                                                                             =======
    String buildExtendedBehaviorPackageMark(String sqlPackage, String exbhvName);

    String buildAccessContextFqcn(String baseCommonPackage, String projectPrefix);
}