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
package org.dbflute.properties;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

import org.apache.torque.engine.database.model.Database;
import org.dbflute.logic.doc.spolicy.DfSPolicyChecker;
import org.dbflute.logic.jdbc.schemadiff.DfSchemaDiff;

/**
 * @author jflute
 * @since 1.1.1 (2015/12/31 Thursday)
 */
public final class DfSchemaPolicyProperties extends DfAbstractDBFluteProperties {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSchemaPolicyProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                                      Definition Map
    //                                                                      ==============
    // *see policy checker
    protected Map<String, Object> _schemaPolicyMap;

    protected Map<String, Object> getSchemaPolicyMap() {
        if (_schemaPolicyMap == null) {
            final Map<String, Object> map = mapProp("torque.schemaPolicyMap", DEFAULT_EMPTY_MAP);
            _schemaPolicyMap = newLinkedHashMap();
            _schemaPolicyMap.putAll(map);
        }
        return _schemaPolicyMap;
    }

    public boolean hasPolicy() {
        return !getSchemaPolicyMap().isEmpty();
    }

    public DfSPolicyChecker createChecker(Database database, Supplier<List<DfSchemaDiff>> schemaDiffListSupplier) {
        return new DfSPolicyChecker(database, schemaDiffListSupplier, getSchemaPolicyMap());
    }
}