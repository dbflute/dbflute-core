/*
 * Copyright 2014-2022 the original author or authors.
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
package org.dbflute.properties.assistant.classification.resource;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dbflute.exception.DfIllegalPropertySettingException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.properties.assistant.classification.DfClassificationTop;

/**
 * @author jflute
 * @since 1.2.5 split from DfClassificationProperties (2021/07/11 Sunday at roppongi japanese)
 */
public class DfClassificationResourceDefinitionReflector {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final List<DfClassificationTop> _resourcePresentsTopList;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfClassificationResourceDefinitionReflector(List<DfClassificationTop> resourcePresentsTopList) {
        _resourcePresentsTopList = resourcePresentsTopList;
    }

    // ===================================================================================
    //                                                               Reflect to Definition
    //                                                               =====================
    public void reflectClassificationResourceToDefinition(Map<String, DfClassificationTop> classificationTopMap) {
        final Set<String> alreadySet = new HashSet<String>(classificationTopMap.keySet());
        for (DfClassificationTop resourcePresentsTop : _resourcePresentsTopList) {
            final String classificationName = resourcePresentsTop.getClassificationName();
            if (alreadySet.contains(classificationName)) {
                throwClassificationAlreadyExistsInDfPropException(classificationName, "ClassificationResource");
            }
            // reflect to classification top definition
            classificationTopMap.put(classificationName, resourcePresentsTop);
        }
    }

    protected void throwClassificationAlreadyExistsInDfPropException(String classificationName, String settingName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The classification already exists in dfprop settings.");
        br.addItem("Advice");
        br.addElement("Check the classification names in '" + settingName + "' settings.");
        br.addElement("The settings may contain classifications existing in dfprop.");
        br.addItem("Classification");
        br.addElement(classificationName);
        final String msg = br.buildExceptionMessage();
        throw new DfIllegalPropertySettingException(msg);
    }
}
