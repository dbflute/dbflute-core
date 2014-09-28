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
package org.seasar.dbflute.logic.generate.packagepath;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.engine.database.model.Database;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.util.DfStringUtil;

/**
 * @author jflute
 * @since 0.7.8 (2008/08/23 Saturday)
 */
public class DfPackagePathHandler {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    private static final Log _log = LogFactory.getLog(Database.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfBasicProperties _basicProperties; // not required
    protected boolean _fileSeparatorSlash;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfPackagePathHandler(DfBasicProperties basicProperties) {
        _basicProperties = basicProperties;
    }

    // ===================================================================================
    //                                                                                Main
    //                                                                                ====
    public String getPackageAsPath(String pckge) {
        if (pckge == null) {
            String msg = "The argument 'pckge' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        final String omitDirectoryPackage = _basicProperties != null ? _basicProperties.getOmitDirectoryPackage()
                : null;
        if (omitDirectoryPackage != null && omitDirectoryPackage.trim().length() > 0) {
            pckge = removeOmitPackage(pckge, omitDirectoryPackage);
        }
        final String flatDirectoryPackage = _basicProperties != null ? _basicProperties.getFlatDirectoryPackage()
                : null;
        if (flatDirectoryPackage == null || flatDirectoryPackage.trim().length() == 0) {
            return resolvePackageAsPath(pckge);
        }
        if (!pckge.contains(flatDirectoryPackage)) {
            return resolvePackageAsPath(pckge);
        }
        final String flatMark = "$$df:flatMark$$";
        pckge = DfStringUtil.replace(pckge, flatDirectoryPackage, flatMark);
        pckge = resolvePackageAsPath(pckge);
        pckge = DfStringUtil.replace(pckge, flatMark, flatDirectoryPackage);
        return pckge;
    }

    protected String removeOmitPackage(String pckge, String omitName) {
        if (pckge.startsWith(omitName)) {
            return replaceString(pckge, omitName + ".", "");
        } else if (pckge.endsWith(omitName)) {
            return replaceString(pckge, "." + omitName, "");
        } else {
            return replaceString(pckge, "." + omitName + ".", ".");
        }
    }

    protected String resolvePackageAsPath(String pckge) {
        if (_fileSeparatorSlash) {
            return pckge.replace('.', '/') + "/";
        } else {
            return pckge.replace('.', File.separator.charAt(0)) + File.separator;
        }
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    // -----------------------------------------------------
    //                                               Logging
    //                                               -------
    public void info(String msg) {
        _log.info(msg);
    }

    public void debug(String msg) {
        _log.debug(msg);
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    public String replaceString(String text, String fromText, String toText) {
        return DfStringUtil.replace(text, fromText, toText);
    }

    public boolean isFileSeparatorSlash() {
        return _fileSeparatorSlash;
    }

    public void setFileSeparatorSlash(boolean fileSeparatorSlash) {
        this._fileSeparatorSlash = fileSeparatorSlash;
    }
}
