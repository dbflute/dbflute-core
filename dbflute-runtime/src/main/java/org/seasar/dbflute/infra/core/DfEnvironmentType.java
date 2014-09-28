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
package org.seasar.dbflute.infra.core;

/**
 * @author jflute
 * @since 0.7.9 (2008/08/26 Tuesday)
 */
public class DfEnvironmentType {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The mark for default control */
    public static final String DEFAULT_CONTROL_MARK = "df:default";

    /** The singleton instance of this. */
    private static final DfEnvironmentType _instance = new DfEnvironmentType();

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The type of environment. (NullAllowed: if null, means non-specified type) */
    protected String _environmentType;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    private DfEnvironmentType() {
    }

    // ===================================================================================
    //                                                                           Singleton
    //                                                                           =========
    public static DfEnvironmentType getInstance() {
        return _instance;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    /**
     * Is the environment type specified?
     * @return The determination, true or false.
     */
    public boolean isSpecifiedType() {
        return _environmentType != null;
    }

    /**
     * Get the type of environment.
     * @return The string for environment type. (NullAllowed: if null, means non-specified type)
     */
    public String getEnvironmentType() {
        return _environmentType;
    }

    /**
     * Get the type of environment it might be default expression {@link #DEFAULT_CONTROL_MARK}.
     * @return The string for environment type. (NotNull: if no specified environment type, returns default control mark)
     */
    public String getEnvironmentTypeMightBeDefault() {
        return _environmentType != null ? _environmentType : DEFAULT_CONTROL_MARK;
    }

    public void setEnvironmentType(String environmentType) {
        if (environmentType == null || environmentType.trim().length() == 0) {
            return;
        }
        _environmentType = environmentType;
    }
}
