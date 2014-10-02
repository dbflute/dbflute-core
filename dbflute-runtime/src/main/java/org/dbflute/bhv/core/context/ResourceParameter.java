/*
 * Copyright 2014-2014 the original author or authors.
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
package org.dbflute.bhv.core.context;

import org.dbflute.bhv.core.context.mapping.MappingDateTimeZoneProvider;
import org.dbflute.twowaysql.style.BoundDateDisplayTimeZoneProvider;

/**
 * The parameters as internal resource.
 * @author jflute
 */
public class ResourceParameter {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                           Outside SQL
    //                                           -----------
    protected String _outsideSqlPackage; // null allowed

    // -----------------------------------------------------
    //                                          Mapping Date
    //                                          ------------
    protected MappingDateTimeZoneProvider _mappingDateTimeZoneProvider;

    // -----------------------------------------------------
    //                                     Log Display Style
    //                                     -----------------
    // every one null allowed
    protected String _logDatePattern;
    protected String _logTimestampPattern;
    protected String _logTimePattern;
    protected BoundDateDisplayTimeZoneProvider _logTimeZoneProvider;

    // -----------------------------------------------------
    //                                        Internal Debug
    //                                        --------------
    protected boolean _internalDebug;

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    // -----------------------------------------------------
    //                                           Outside SQL
    //                                           -----------
    public String getOutsideSqlPackage() {
        return _outsideSqlPackage;
    }

    public void setOutsideSqlPackage(String outsideSqlPackage) {
        _outsideSqlPackage = outsideSqlPackage;
    }

    // -----------------------------------------------------
    //                                          Mapping Date
    //                                          ------------
    public MappingDateTimeZoneProvider getMappingDateTimeZoneProvider() {
        return _mappingDateTimeZoneProvider;
    }

    public void setMappingDateTimeZoneProvider(MappingDateTimeZoneProvider mappingDateTimeZoneProvider) {
        _mappingDateTimeZoneProvider = mappingDateTimeZoneProvider;
    }

    // -----------------------------------------------------
    //                                     Log Display Style
    //                                     -----------------
    public String getLogDatePattern() {
        return _logDatePattern;
    }

    public void setLogDatePattern(String logDatePattern) {
        _logDatePattern = logDatePattern;
    }

    public String getLogTimestampPattern() {
        return _logTimestampPattern;
    }

    public void setLogTimestampPattern(String logTimestampPattern) {
        _logTimestampPattern = logTimestampPattern;
    }

    public String getLogTimePattern() {
        return _logTimePattern;
    }

    public void setLogTimePattern(String logTimePattern) {
        _logTimePattern = logTimePattern;
    }

    public BoundDateDisplayTimeZoneProvider getLogTimeZoneProvider() {
        return _logTimeZoneProvider;
    }

    public void setLogTimeZoneProvider(BoundDateDisplayTimeZoneProvider logTimeZoneProvider) {
        _logTimeZoneProvider = logTimeZoneProvider;
    }

    // -----------------------------------------------------
    //                                        Internal Debug
    //                                        --------------
    public boolean isInternalDebug() {
        return _internalDebug;
    }

    public void setInternalDebug(boolean internalDebug) {
        _internalDebug = internalDebug;
    }
}
