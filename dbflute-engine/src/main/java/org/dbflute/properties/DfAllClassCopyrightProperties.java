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

import java.util.Properties;

import org.dbflute.util.DfStringUtil;

/**
 * @author jflute
 */
public final class DfAllClassCopyrightProperties extends DfAbstractHelperProperties {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String KEY_sourceCopyright = "sourceCopyright";
    protected static final String KEY_oldAllClassCopyright = "allClassCopyright";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String _copyright;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfAllClassCopyrightProperties(Properties prop) {
        super(prop);
    }

    // ===================================================================================
    //                                                                           Copyright
    //                                                                           =========
    public String getAllClassCopyright() {
        if (_copyright != null) {
            return _copyright;
        }
        String prop = stringProp("torque." + KEY_sourceCopyright, null);
        if (prop == null) {
            prop = stringProp("torque." + KEY_oldAllClassCopyright, ""); // for compatible
        }

        final String sourceCodeLn = getBasicProperties().getSourceCodeLineSeparator();
        prop = DfStringUtil.replace(prop, "\r\n", "\n");
        prop = DfStringUtil.replace(prop, "\n", sourceCodeLn);

        _copyright = prop;
        return _copyright;
    }
}