/*
 * Copyright 2014-2015 the original author or authors.
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

import java.util.Map;
import java.util.Properties;

import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.properties.assistant.lastaflute.DfLastaFluteFreeGenReflector;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.1.0-sp3 (2015/04/26 Sunday)
 */
public final class DfLastaFluteProperties extends DfAbstractHelperProperties {

    private static final Logger logger = LoggerFactory.getLogger(DfLastaFluteProperties.class);

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfLastaFluteProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                                      Definition Map
    //                                                                      ==============
    // map:{
    //     ; serviceName = maihama
    //     ; domainPackage = org.docksidestage
    //     ; isMakeActionHtml = true
    //     ; commonMap = map:{
    //         ; path = ../
    //         ; freeGenList = list:{ env ; config ; label ; message ; jsp }
    //         ; propertiesHtmlList = list:{ env ; config ; label ; message }
    //     }
    //     ; appMap = map:{
    //         ; dockside = map:{
    //             ; path = ../../maihama-dockside
    //             ; freeGenList = list:{ env ; config ; label ; message ; jsp }
    //             ; propertiesHtmlList = list:{ env ; config ; label ; message }
    //         }
    //         ; hanger = map:{
    //             ; path = ../../maihama-hanger
    //             ; freeGenList = list:{ env ; config ; label ; message ; jsp }
    //             ; propertiesHtmlList = list:{ env ; config ; label ; message }
    //         }
    //     }
    // }
    protected Map<String, Object> _lastafluteMap;

    protected Map<String, Object> getLastafluteMap() { // closet
        if (_lastafluteMap == null) {
            final Map<String, Object> map = mapProp("torque.lastafluteMap", DEFAULT_EMPTY_MAP);
            _lastafluteMap = newLinkedHashMap();
            _lastafluteMap.putAll(map);
        }
        return _lastafluteMap;
    }

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    public boolean hasLastafluteDefinition() {
        return !getLastafluteMap().isEmpty();
    }

    // ===================================================================================
    //                                                                     Prepare FreeGen
    //                                                                     ===============
    public void prepareFreeGenProperties(Map<String, Object> freeGenMap) {
        final Map<String, Object> lastafluteMap = getLastafluteMap();
        final String serviceName = (String) lastafluteMap.get("serviceName");
        if (Srl.is_Null_or_TrimmedEmpty(serviceName)) { // no use
            return;
        }
        logger.info("...Loading freeGen settings from lastafluteMap: " + serviceName);
        final String domainPackage = (String) lastafluteMap.get("domainPackage");
        if (domainPackage == null) {
            throw new DfIllegalPropertySettingException("The property 'domainPackage' is required: " + lastafluteMap.keySet());
        }
        new DfLastaFluteFreeGenReflector(freeGenMap, serviceName, domainPackage).reflectFrom(getLastafluteMap());
    }
}