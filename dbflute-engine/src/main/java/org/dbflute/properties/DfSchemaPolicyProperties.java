/*
 * Copyright 2014-2017 the original author or authors.
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

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

import org.apache.torque.engine.database.model.Table;
import org.dbflute.logic.doc.policycheck.DfSchemaPolicyChecker;

/**
 * @author jflute
 * @since 1.1.1 (2015/12/31 Thursday)
 */
public final class DfSchemaPolicyProperties extends DfAbstractHelperProperties {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSchemaPolicyProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                                      Definition Map
    //                                                                      ==============
    // *see SchemaPolicyChecker
    protected Map<String, Object> _schemaPolicyMap;

    protected Map<String, Object> getSchemaPolicyMap() {
        if (_schemaPolicyMap == null) {
            final Map<String, Object> map = mapProp("torque.schemaPolicyMap", DEFAULT_EMPTY_MAP);
            _schemaPolicyMap = newLinkedHashMap();
            _schemaPolicyMap.putAll(map);
        }
        return _schemaPolicyMap;
    }

    public DfSchemaPolicyChecker createChecker(Supplier<List<Table>> tableListSupplier) {
        return new DfSchemaPolicyChecker(tableListSupplier, getSchemaPolicyMap());
    }
}