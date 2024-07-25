/*
 * Copyright 2014-2024 the original author or authors.
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
package org.dbflute.helper.jdbc.connection;

import org.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfConnectionMetaInfo {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String _productName;
    protected String _productVersion;
    protected String _driverName;
    protected String _driverVersion;
    protected String _jdbcVersion;

    // ===================================================================================
    //                                                                             Display
    //                                                                             =======
    // -----------------------------------------------------
    //                                      Database Product
    //                                      ----------------
    public String getProductDisp() {
        return _productName + " " + _productVersion;
    }

    // -----------------------------------------------------
    //                                           JDBC Driver
    //                                           -----------
    public String getDriverDisp() {
        return _driverName + " " + _driverVersion + " for " + getJdbcDisp();
    }

    public String getDriverDispMySQL() {
        // driverVersion actually contains driver name so remove it
        // and driverVersion has unneeded revision information so remove it
        final String base;
        if (_driverVersion != null) {
            base = Srl.substringFirstFront(_driverVersion, "(Revision", "( Revision").trim();
        } else {
            base = _driverName;
        }
        return base + " for " + getJdbcDisp();
    }

    // -----------------------------------------------------
    //                                          JDBC Version
    //                                          ------------
    public String getJdbcDisp() {
        return "JDBC " + _jdbcVersion;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getProductName() {
        return _productName;
    }

    public void setProductName(String productName) {
        this._productName = productName;
    }

    public String getProductVersion() {
        return _productVersion;
    }

    public void setProductVersion(String productVersion) {
        this._productVersion = productVersion;
    }

    public String getDriverName() {
        return _driverName;
    }

    public void setDriverName(String driverName) {
        this._driverName = driverName;
    }

    public String getDriverVersion() {
        return _driverVersion;
    }

    public void setDriverVersion(String driverVersion) {
        this._driverVersion = driverVersion;
    }

    public String getJdbcVersion() {
        return _jdbcVersion;
    }

    public void setJdbcVersion(String jdbcVersion) {
        this._jdbcVersion = jdbcVersion;
    }
}
