/*
 * Copyright 2014-2023 the original author or authors.
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

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.dbflute.helper.function.IndependentProcessor;
import org.dbflute.properties.assistant.classification.DfClassificationTop;

/**
 * @author jflute
 * @since 1.2.5 split from DfClassificationProperties (2021/07/11 Sunday at roppongi japanese)
 */
public class DfClassificationResourceDeploymentReflector {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final List<DfClassificationTop> _resourcePresentsTopList;
    protected final IndependentProcessor _allColumnClsSetupper;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfClassificationResourceDeploymentReflector(List<DfClassificationTop> resourcePresentsTopList,
            IndependentProcessor allColumnClsSetupper) {
        _resourcePresentsTopList = resourcePresentsTopList;
        _allColumnClsSetupper = allColumnClsSetupper;
    }

    // ===================================================================================
    //                                                               Reflect to Deployment
    //                                                               =====================
    public void reflectClassificationResourceToDeployment(Supplier<Map<String, String>> allColumnClassificationMapProvider) {
        final List<DfClassificationTop> classificationTopList = _resourcePresentsTopList;
        for (DfClassificationTop classificationTop : classificationTopList) {
            final String classificationName = classificationTop.getClassificationName();
            final String relatedColumnName = classificationTop.getRelatedColumnName();
            if (relatedColumnName == null) {
                continue;
            }
            // both callback to keep process order 
            _allColumnClsSetupper.process();
            final Map<String, String> allColumnClassificationMap = allColumnClassificationMapProvider.get();
            if (allColumnClassificationMap.containsKey(relatedColumnName)) {
                continue;
            }
            allColumnClassificationMap.put(relatedColumnName, classificationName);
        }
    }
}
