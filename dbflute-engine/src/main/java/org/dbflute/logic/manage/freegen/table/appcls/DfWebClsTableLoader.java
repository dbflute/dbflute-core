/*
 * Copyright 2014-2021 the original author or authors.
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
package org.dbflute.logic.manage.freegen.table.appcls;

import org.dbflute.logic.manage.freegen.DfFreeGenMapProp;
import org.dbflute.logic.manage.freegen.DfFreeGenMetaData;
import org.dbflute.logic.manage.freegen.DfFreeGenResource;
import org.dbflute.logic.manage.freegen.DfFreeGenTableLoader;

/**
 * Very similar to AppCls, this WebCls is web-only CDef. <br>
 * Historically WebCls first, but it needs more general classification. <br>
 * This class is almost only for compatibility, so silent maintenance.
 * @author jflute
 */
public class DfWebClsTableLoader implements DfFreeGenTableLoader {

    // ===================================================================================
    //                                                                          Load Table
    //                                                                          ==========
    // ; resourceMap = map:{
    //     ; baseDir = ../src/main
    //     ; resourceType = WEB_CLS
    //     ; resourceFile = ../../../dockside_webcls.properties
    // }
    // ; outputMap = map:{
    //     ; templateFile = LaAppCDef.vm
    //     ; outputDirectory = $$baseDir$$/java
    //     ; package = org.dbflute...
    //     ; className = unused
    // }
    // ; optionMap = map:{
    // }
    @Override
    public DfFreeGenMetaData loadTable(String requestName, DfFreeGenResource resource, DfFreeGenMapProp mapProp) {
        // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
        // old comment: very similar to AppCls but no recycle because of silent maintenance
        // _/_/_/_/_/_/_/_/_/_/
        // done jflute let's recycle now (2021/07/05)
        return new DfAppClsTableLoader() {
            protected String getDefaultClsTheme() {
                return "webcls"; // for LastaDoc (2021/07/10)
            };
        }.loadTable(requestName, resource, mapProp);
    }
}
