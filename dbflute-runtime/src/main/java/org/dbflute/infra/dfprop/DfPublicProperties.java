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
package org.dbflute.infra.dfprop;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * The handling class for public.properties.
 * <pre>
 * e.g. use default URL
 *  DfPublicProperties publicProp = new DfPublicProperties();
 *  publicProp.load(); // connecting to network
 *  String latestVersion = publicProp.getDBFluteLatestVersion();
 * </pre>
 * @author jflute
 * @since 1.1.0 (2014/11/08 Saturday)
 */
public class DfPublicProperties {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    // -----------------------------------------------------
    //                                     Public Properties
    //                                     -----------------
    public static final String PUBLIC_PROP_URL = "http://dbflute.org/meta/public.properties";
    public static final String VERSION_VARIABLE = "$$version$$";

    // -----------------------------------------------------
    //                                        Infrastructure
    //                                        --------------
    public static final String DBFLUTE_LATEST_RELEASE_VERSION = "dbflute.latest.release.version";
    public static final String DBFLUTE_LATEST_SNAPSHOT_VERSION = "dbflute.latest.snapshot.version";
    public static final String DBFLUTE_ENGINE_DOWNLOAD_URL = "dbflute.engine.download.url";
    public static final String INTRO_LATEST_VERSION = "intro.latest.version";
    public static final String INTRO_DOWNLOAD_URL = "intro.download.url";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected Properties _publicProp;
    protected String _specifiedUrl;

    // ===================================================================================
    //                                                                           Load Meta
    //                                                                           =========
    public void load() {
        final String siteUrl = getPublicPropertiesUrl();
        InputStream ins = null;
        try {
            final URL url = new URL(siteUrl);
            ins = url.openStream();
            final Properties prop = new Properties();
            prop.load(ins);
            _publicProp = prop;
        } catch (IOException e) {
            String msg = "The url threw the IO exception: url=" + siteUrl;
            throw new IllegalStateException(msg, e);
        } finally {
            if (ins != null) {
                try {
                    ins.close();
                } catch (IOException ignored) {}
            }
        }
    }

    public String getPublicPropertiesUrl() {
        return _specifiedUrl != null ? _specifiedUrl : PUBLIC_PROP_URL;
    }

    // ===================================================================================
    //                                                                            Property
    //                                                                            ========
    // -----------------------------------------------------
    //                                          DBFlute Core
    //                                          ------------
    public String getDBFluteLatestReleaseVersion() {
        return getProperty(DBFLUTE_LATEST_RELEASE_VERSION);
    }

    public String getDBFluteLatestSnapshotVersion() {
        return getProperty(DBFLUTE_LATEST_SNAPSHOT_VERSION);
    }

    public String getDBFluteDownloadUrl(String downloadVersion) {
        final String key = DBFLUTE_ENGINE_DOWNLOAD_URL;
        return buildDownloadUrl(downloadVersion, key);
    }

    // -----------------------------------------------------
    //                                         DBFlute Intro
    //                                         -------------
    public String getIntroLatestVersion() {
        return getProperty(INTRO_LATEST_VERSION);
    }

    public String getIntroDownloadUrl(String downloadVersion) {
        final String key = INTRO_DOWNLOAD_URL;
        return buildDownloadUrl(downloadVersion, key);
    }

    protected String buildDownloadUrl(String downloadVersion, String key) {
        final String downloadUrl = getProperty(key);
        if (downloadUrl == null) {
            String msg = "Not found the property: key=" + key + ", map=" + _publicProp;
            throw new IllegalStateException(msg);
        }
        return replace(downloadUrl, VERSION_VARIABLE, downloadVersion);
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected String getProperty(String key) {
        if (_publicProp == null) {
            String msg = "Not loaded public properties: " + getPublicPropertiesUrl();
            throw new IllegalStateException(msg);
        }
        return _publicProp.getProperty(key);
    }

    protected String replace(String str, String fromStr, String toStr) { // not to depends on other classes
        if (str == null) {
            throw new IllegalArgumentException("The argument 'str' should not be null.");
        }
        if (fromStr == null) {
            throw new IllegalArgumentException("The argument 'fromStr' should not be null.");
        }
        if (toStr == null) {
            throw new IllegalArgumentException("The argument 'toStr' should not be null.");
        }
        StringBuilder sb = null; // lazy load
        int pos = 0;
        int pos2 = 0;
        do {
            pos = str.indexOf(fromStr, pos2);
            if (pos2 == 0 && pos < 0) { // first loop and not found
                return str; // without creating StringBuilder 
            }
            if (sb == null) {
                sb = new StringBuilder();
            }
            if (pos == 0) {
                sb.append(toStr);
                pos2 = fromStr.length();
            } else if (pos > 0) {
                sb.append(str.substring(pos2, pos));
                sb.append(toStr);
                pos2 = pos + fromStr.length();
            } else { // (pos < 0) second or after loop only
                sb.append(str.substring(pos2));
                return sb.toString();
            }
        } while (true);
    }

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    public DfPublicProperties specifyUrl(String url) {
        _specifiedUrl = url;
        return this;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "publicProp:{" + _publicProp + "}";
    }
}
