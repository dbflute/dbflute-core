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
package org.seasar.dbflute.infra.dfprop;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.seasar.dbflute.helper.mapstring.MapListFile;
import org.seasar.dbflute.util.Srl;

/**
 * The handling class for publicMap.dfprop.
 * <pre>
 * e.g. use default URL
 *  DfPropPublicMap publicMap = new DfPropPublicMap();
 *  publicMap.loadMap(); // connecting to network
 *  String latestVersion = publicMap.getDBFluteLatestVersion();
 * </pre>
 * @author jflute
 * @since 1.0.5K (2014/08/15 Friday)
 */
public class DfPropPublicMap {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String DBFLUTE_LATEST_VERSION = "dbflute.latest.version";
    protected static final String DBFLUTE_LATEST_SNAPSHOT_VERSION = "dbflute.latest.snapshot.version";
    protected static final String DBFLUTE_DOWNLOAD_URL = "dbflute.download.url";
    protected static final String INTRO_DOWNLOAD_URL = "intro.download.url";

    protected static final String DEFAULT_DFPROP_URL = "http://dbflute.seasar.org/meta/publicMap.dfprop";
    protected static final String VERSION_SUFFIX_VARIABLE = "$$versionSuffix$$";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected Map<String, Object> _map;
    protected String _specifiedUrl;

    // ===================================================================================
    //                                                                           Load Meta
    //                                                                           =========
    public void loadMap() {
        final String siteUrl = getPublicMapUrl();
        InputStream ins = null;
        try {
            final URL url = new URL(siteUrl);
            ins = url.openStream();
            final MapListFile mapListFile = new MapListFile();
            _map = mapListFile.readMap(ins);
        } catch (IOException e) {
            throw new IllegalStateException("The url threw the IO exception: url=" + siteUrl, e);
        } finally {
            if (ins != null) {
                try {
                    ins.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    // ===================================================================================
    //                                                                            Property
    //                                                                            ========
    public String getDBFluteLatestVersion() {
        return getMetaValue(DBFLUTE_LATEST_VERSION);
    }

    public String getDBFluteLatestSnapshotVersion() {
        return getMetaValue(DBFLUTE_LATEST_SNAPSHOT_VERSION);
    }

    public String getDBFluteDownloadUrl(String downloadVersion) {
        final String key = DBFLUTE_DOWNLOAD_URL;
        final String downloadUrl = getMetaValue(key);
        if (downloadUrl == null) {
            String msg = "Not found the property: key=" + key + ", map=" + _map;
            throw new IllegalStateException(msg);
        }
        return buildDBFluteDownloadUrl(downloadUrl, downloadVersion);
    }

    protected String buildDBFluteDownloadUrl(String downloadUrl, String downloadVersion) {
        final String toStr;
        if (Srl.is_NotNull_and_NotTrimmedEmpty(downloadVersion)) {
            toStr = "-" + downloadVersion;
        } else {
            toStr = ""; // to remove variable
        }
        return Srl.replace(downloadUrl, VERSION_SUFFIX_VARIABLE, toStr);
    }

    public String getIntroDownloadUrl() {
        return getMetaValue(INTRO_DOWNLOAD_URL);
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected String getMetaValue(String key) {
        return (String) _map.get(key);
    }

    protected String getPublicMapUrl() {
        return _specifiedUrl != null ? _specifiedUrl : DEFAULT_DFPROP_URL;
    }

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    public DfPropPublicMap specifyUrl(String url) {
        _specifiedUrl = url;
        return this;
    }
}
