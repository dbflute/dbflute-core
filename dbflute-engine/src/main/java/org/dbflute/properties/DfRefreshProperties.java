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
package org.dbflute.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfStringUtil;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.6.9 (2008/04/11 Friday)
 */
public final class DfRefreshProperties extends DfAbstractDBFluteProperties {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfRefreshProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                                      Definition Map
    //                                                                      ==============
    public static final String KEY_refreshMap = "refreshMap";
    protected static final String KEY_oldRefreshMap = "refreshDefinitionMap";

    protected Map<String, Object> _refreshMap;

    protected Map<String, Object> getRefreshMap() {
        if (_refreshMap == null) {
            Map<String, Object> map = mapProp("torque." + KEY_refreshMap, null);
            if (map == null) {
                map = getLittleAdjustmentProperties().getRefreshFacadeMap();
                if (map == null) {
                    map = mapProp("torque." + KEY_oldRefreshMap, null); // for compatible
                    if (map == null) {
                        map = prepareDefaultRefreshMap();
                    }
                }
            }
            reflectEmbeddedProjectName(map);
            _refreshMap = newLinkedHashMap();
            _refreshMap.putAll(map);
        }
        return _refreshMap;
    }

    protected Map<String, Object> prepareDefaultRefreshMap() {
        final Map<String, Object> map = newLinkedHashMap();
        map.put("projectName", "$$AutoDetect$$");
        map.put("requestUrl", "http://localhost:8386/");
        return map;
    }

    protected void reflectEmbeddedProjectName(Map<String, Object> map) {
        final List<String> projectNameList = new ArrayList<String>();
        getLastaFluteProperties().reflectRefreshProjectList(projectNameList);
        String projectName = (String) map.get("projectName");
        projectName = Srl.is_NotNull_and_NotTrimmedEmpty(projectName) ? projectName : "$$AutoDetect$$";
        for (String element : projectNameList) {
            projectName = projectName + "/" + element;
        }
        map.put("projectName", projectName);
    }

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    public boolean hasRefreshDefinition() {
        return !getRefreshMap().isEmpty();
    }

    // ===================================================================================
    //                                                                     Detail Property
    //                                                                     ===============
    public List<String> getProjectNameList() {
        final String prop = getRefreshProperty("projectName");
        if (Srl.is_NotNull_and_NotTrimmedEmpty(prop)) {
            return DfStringUtil.splitListTrimmed(prop, "/");
        } else {
            return DfCollectionUtil.emptyList();
        }
    }

    public String getRequestUrl() {
        final String prop = getRefreshProperty("requestUrl");
        if (Srl.is_NotNull_and_NotTrimmedEmpty(prop)) {
            return prop;
        } else {
            return null;
        }
    }

    protected String getRefreshPropertyIfNullEmpty(String key) {
        final String value = getRefreshProperty(key);
        if (value == null) {
            return "";
        }
        return value;
    }

    protected String getRefreshProperty(String key) {
        return (String) getRefreshMap().get(key);
    }
}