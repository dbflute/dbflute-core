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

import java.util.List;
import java.util.function.Supplier;

import org.dbflute.properties.assistant.classification.DfClassificationTop;

/**
 * @author jflute
 * @since 1.2.5 split from DfClassificationProperties (2021/07/11 Sunday at roppongi japanese)
 */
public class DfClassificationResourceExtractor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String NAME_CLASSIFICATION_RESOURCE = "classificationResource";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Supplier<Boolean> _specifiedEnvironmentTypeDeterminer;
    protected final Supplier<String> _environmentTypeProvider;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfClassificationResourceExtractor(Supplier<Boolean> specifiedEnvironmentTypeDeterminer,
            Supplier<String> environmentTypeProvider) {
        _specifiedEnvironmentTypeDeterminer = specifiedEnvironmentTypeDeterminer;
        _environmentTypeProvider = environmentTypeProvider;
    }

    // ===================================================================================
    //                                                                    Extract Resource
    //                                                                    ================
    public List<DfClassificationTop> extractClassificationResource() {
        final DfClassificationResourceFileAnalyzer analyzer = new DfClassificationResourceFileAnalyzer();
        final String dirBaseName = "./dfprop";
        final String resource = NAME_CLASSIFICATION_RESOURCE;
        final String extension = "dfprop";
        if (_specifiedEnvironmentTypeDeterminer.get()) {
            final String dirEnvName = dirBaseName + "/" + _environmentTypeProvider.get();
            final List<DfClassificationTop> ls = analyzer.analyze(dirEnvName, resource, extension);
            if (!ls.isEmpty()) {
                return ls;
            }
            return analyzer.analyze(dirBaseName, resource, extension);
        } else {
            return analyzer.analyze(dirBaseName, resource, extension);
        }
    }
}
