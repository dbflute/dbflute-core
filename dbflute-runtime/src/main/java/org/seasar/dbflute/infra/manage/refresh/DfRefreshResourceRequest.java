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
package org.seasar.dbflute.infra.manage.refresh;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.seasar.dbflute.util.Srl;

/**
 * The request of (Eclipse's) refresh resource. <br />
 * You can refresh automatically by this.
 * <pre>
 * DfRefreshResourceRequest request
 *     = new DfRefreshResourceRequest(projectNameList, requestUrl);
 * request.refreshResources();
 * </pre>
 * @author jflute
 */
public class DfRefreshResourceRequest {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final List<String> _projectNameList;
    protected final String _requestUrl;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * @param projectNameList The list of project name for refresh. (NotNull)
     * @param requestUrl The request URL for refresh to synchronizer. (NotNull)
     */
    public DfRefreshResourceRequest(List<String> projectNameList, String requestUrl) {
        if (projectNameList == null || projectNameList.isEmpty()) {
            String msg = "The argument '_projectNameList' should not be null or empty: " + projectNameList;
            throw new IllegalArgumentException(msg);
        }
        if (Srl.is_Null_or_TrimmedEmpty(requestUrl)) {
            String msg = "The argument 'requestUrl' should not be null or empty: " + requestUrl;
            throw new IllegalArgumentException(msg);
        }
        _projectNameList = projectNameList;
        _requestUrl = requestUrl;
    }

    // ===================================================================================
    //                                                                             Refresh
    //                                                                             =======
    /**
     * Refresh resources. (request to synchronizer)
     * @throws IOException When the refresh failed.
     */
    public void refreshResources() throws IOException {
        for (String projectName : _projectNameList) {
            doRefreshResources(projectName);
        }
    }

    protected void doRefreshResources(String projectName) throws IOException {
        final StringBuilder sb = new StringBuilder();
        sb.append("refresh?").append(projectName).append("=INFINITE");

        final URL url = createRefreshRequestURL(sb.toString());
        if (url == null) {
            return;
        }

        InputStream ins = null;
        try {
            final URLConnection conn = url.openConnection();
            conn.setReadTimeout(getRefreshRequestReadTimeout());
            conn.connect();
            ins = conn.getInputStream();
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
    //                                                                    Refresh Resource
    //                                                                    ================
    protected URL createRefreshRequestURL(String path) throws MalformedURLException {
        String requestUrl = _requestUrl;
        if (!requestUrl.endsWith("/")) {
            requestUrl = requestUrl + "/";
        }
        return new URL(requestUrl + path);
    }

    protected int getRefreshRequestReadTimeout() {
        return 3 * 1000;
    }
}
