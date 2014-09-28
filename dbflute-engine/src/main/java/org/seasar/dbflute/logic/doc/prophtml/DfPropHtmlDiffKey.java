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
package org.seasar.dbflute.logic.doc.prophtml;

/**
 * @author jflute
 * @since 1.0.1 (2012/12/21 Friday)
 */
public class DfPropHtmlDiffKey {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _propertyKey;
    protected final boolean _override;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfPropHtmlDiffKey(String propertyKey, boolean override) {
        _propertyKey = propertyKey;
        _override = override;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getPropertyKey() {
        return _propertyKey;
    }

    public boolean isOverride() {
        return _override;
    }
}
