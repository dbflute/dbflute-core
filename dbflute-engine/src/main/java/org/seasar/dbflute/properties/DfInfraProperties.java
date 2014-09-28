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
package org.seasar.dbflute.properties;

import java.util.Map;
import java.util.Properties;

import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.0.5K (2014/08/15 Friday)
 */
public final class DfInfraProperties extends DfAbstractHelperProperties {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfInfraProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                                      Definition Map
    //                                                                      ==============
    protected Map<String, Object> infraDefinitionMap;

    protected Map<String, Object> getInfraDefinitionMap() {
        if (infraDefinitionMap == null) {
            final Map<String, Object> map = mapProp("torque.infraDefinitionMap", DEFAULT_EMPTY_MAP);
            infraDefinitionMap = newLinkedHashMap();
            infraDefinitionMap.putAll(map);
        }
        return infraDefinitionMap;
    }

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    public boolean hasInfraDefinition() {
        return !getInfraDefinitionMap().isEmpty();
    }

    // ===================================================================================
    //                                                                     Detail Property
    //                                                                     ===============
    public String getPublicMapUrl() {
        final String prop = getInfraProperty("publicMapUrl");
        return Srl.is_NotNull_and_NotTrimmedEmpty(prop) ? prop : null;
    }

    public String getDBFluteIntroDownloadUrl() {
        final String prop = getInfraProperty("dbfluteIntroDownloadUrl");
        return Srl.is_NotNull_and_NotTrimmedEmpty(prop) ? prop : null;
    }

    public String getDBFluteIntroLocationPath() {
        final String prop = getInfraProperty("dbfluteIntroLocationPath");
        return Srl.is_NotNull_and_NotTrimmedEmpty(prop) ? prop : null;
    }

    public String getDBFluteModuleDownloadUrl() {
        final String prop = getInfraProperty("dbfluteModuleDownloadUrl");
        return Srl.is_NotNull_and_NotTrimmedEmpty(prop) ? prop : null;
    }

    protected String getInfraProperty(String key) {
        return (String) getInfraDefinitionMap().get(key);
    }
}