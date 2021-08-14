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
package org.dbflute.logic.manage.freegen.table.appcls.refcls;

import java.util.Collections;
import java.util.Map;

import org.dbflute.DfBuildProperties;
import org.dbflute.helper.StringKeyMap;
import org.dbflute.properties.DfBasicProperties;
import org.dbflute.properties.DfClassificationProperties;
import org.dbflute.properties.assistant.classification.DfClassificationTop;
import org.dbflute.properties.assistant.classification.refcls.DfRefClsReferredCDef;

/**
 * @author jflute
 * @since 1.2.5 (2021/07/08 Thursday at roppongi japanese)
 */
public class DfRefClsReferenceRegistry {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final DfRefClsReferenceRegistry _instance = new DfRefClsReferenceRegistry();

    public static DfRefClsReferenceRegistry getInstance() {
        return _instance;
    }

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // except DB cls (which is lazy-loaded in finder)
    protected final Map<String, DfRefClsReference> _themeClsTopMap = StringKeyMap.createAsCaseInsensitive();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    protected DfRefClsReferenceRegistry() {
    }

    // ===================================================================================
    //                                                                  Reference Handling
    //                                                                  ==================
    public void registerReference(String clsTheme, Map<String, DfClassificationTop> clsTopMap, DfRefClsReferredCDef refCDef) {
        if (clsTheme == null) {
            throw new IllegalArgumentException("The argument 'clsTheme' should not be null.");
        }
        if (clsTopMap == null) {
            throw new IllegalArgumentException("The argument 'clsTopMap' should not be null.");
        }
        if (refCDef == null) {
            throw new IllegalArgumentException("The argument 'refCDef' should not be null.");
        }
        _themeClsTopMap.put(clsTheme, new DfRefClsReference(clsTheme, clsTopMap, refCDef));
    }

    public DfRefClsReference findReference(String clsTheme) { // contains DB reference, null allowed
        if (clsTheme == null) {
            throw new IllegalArgumentException("The argument 'clsTheme' should not be null.");
        }
        if (isDBReference(clsTheme)) {
            return createDBClsReference(clsTheme); // lazy-load here
        } else {
            return _themeClsTopMap.get(clsTheme);
        }
    }

    protected boolean isDBReference(String clsTheme) {
        return clsTheme.equals(getBasicProperties().getProjectName());
    }

    protected DfRefClsReference createDBClsReference(String clsTheme) {
        final Map<String, DfClassificationTop> topMap = getClassificationProperties().getClassificationTopMap();
        final String cdefPackage = getBasicProperties().getBaseCommonPackage();
        final String cdefInterfaceName = getBasicProperties().getCDefPureName();
        final DfRefClsReferredCDef refCDef = new DfRefClsReferredCDef(cdefPackage, cdefInterfaceName);
        return new DfRefClsReference(clsTheme, topMap, refCDef);
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBasicProperties getBasicProperties() {
        return DfBuildProperties.getInstance().getBasicProperties();
    }

    protected DfClassificationProperties getClassificationProperties() {
        return DfBuildProperties.getInstance().getClassificationProperties();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public Map<String, DfRefClsReference> getThemeClsTopMap() {
        return Collections.unmodifiableMap(_themeClsTopMap);
    }
}
