/*
 * Copyright 2014-2018 the original author or authors.
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
package org.dbflute.logic.generate.refresh;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.dbflute.helper.mapstring.MapListString;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.infra.manage.refresh.DfRefreshResourceRequest;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 */
public class DfRefreshResourceProcess {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(DfRefreshResourceProcess.class);

    protected static final String DEFAULT_PRIMARY_REQUEST_URL = "http://localhost:8386/"; // setting default
    protected static final String DEFAULT_SECONDARY_REQUEST_URL = "http://localhost:8387/";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final List<String> _projectNameList;
    protected final String _requestUrl;
    protected Integer successRetryPort;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * @param projectNameList The list of project name for refresh. (NullAllowed, EmptyAllowed: no refresh)
     * @param requestUrl The request URL for refresh to synchronizer. (NullAllowed: no refresh)
     */
    public DfRefreshResourceProcess(List<String> projectNameList, String requestUrl) {
        _projectNameList = projectNameList;
        _requestUrl = requestUrl;
    }

    // ===================================================================================
    //                                                                             Refresh
    //                                                                             =======
    public void refreshResources() {
        if (!isRefresh()) {
            return;
        }
        show("/===========================================================================");
        show("...Refreshing " + _projectNameList + " by " + _requestUrl); // not null here
        final DfRefreshResourceRequest request = createRefreshResourceRequest(_requestUrl);
        try {
            final Map<String, Map<String, Object>> resultMap = request.refreshResources();
            handleResultMap(resultMap);
        } catch (IOException e) {
            handleRefreshIOException(e);
        }
        show("==========/");
    }

    protected void handleResultMap(Map<String, Map<String, Object>> resultMap) {
        for (Entry<String, Map<String, Object>> entry : resultMap.entrySet()) {
            final String projectName = entry.getKey();
            final Map<String, Object> responseMap = entry.getValue();
            final String body = (String) responseMap.get(DfRefreshResourceRequest.KEY_BODY);
            if (Srl.is_NotNull_and_NotTrimmedEmpty(body)) {
                final Properties props = toResultProperties(body);
                if (props != null) {
                    handlePropsResult(projectName, body, props);
                } else { // might be success of Seasar's resource synchronizer
                    handleNonPropsResult(projectName, body);
                }
            } else { // might be failure of Seasar's resource synchronizer
                handleEmptyResult(projectName);
            }
        }
    }

    protected Properties toResultProperties(String body) {
        try {
            final Properties props = new Properties();
            props.load(new ByteArrayInputStream(body.getBytes("UTF-8")));
            return props;
        } catch (IOException ignored) {
            final String firstLine = Srl.substringFirstFront(body, "\n");
            show("*Cannot read the result body from synchronizer as properties: " + firstLine);
            return null; // might be success of Seasar's resource synchronizer
        }
    }

    protected void handlePropsResult(String projectName, String body, Properties props) {
        try {
            final String refreshResult = props.getProperty("refresh.result");
            if (!Srl.equalsPlain(refreshResult, "allNotFound", "hasNotFound")) { // means success
                return;
            }
            final String ports = props.getProperty("retry.port");
            final List<Object> portList = new MapListString().generateList(ports);
            int refreshLevel = 2;
            boolean retrySuccess = false;
            for (Object portObj : portList) {
                final int port = Integer.parseInt(portObj.toString());
                if (retrySpecifiedPort(projectName, port, refreshLevel)) {
                    retrySuccess = true;
                    break;
                }
                ++refreshLevel;
            }
            if (!retrySuccess) {
                show("*Not found the projects in the Eclipse: " + projectName);
            }
        } catch (RuntimeException continued) {
            show("*Cannot retry by response port for the project: " + projectName + " " + continued.getMessage());
        }
    }

    protected void handleNonPropsResult(String projectName, String body) {
        final String firstLine = body.contains("\n") ? (Srl.substringFirstFront(body, "\n") + "...") : body;
        show("*Not properties response body (success?): " + projectName + " firstLine=" + firstLine);
    }

    protected void handleEmptyResult(String projectName) {
        show("*Empty result so treat it as default not found: " + projectName);
        handleDefaultNotFoundProject(projectName);
    }

    protected void handleDefaultNotFoundProject(String projectName) {
        final boolean retrySuccess = retryDefaultSecondary();
        if (!retrySuccess) {
            show("*Not found the projects in the Eclipse: " + projectName);
        }
    }

    // -----------------------------------------------------
    //                                   Refresh IOExpcetion
    //                                   -------------------
    protected void handleRefreshIOException(IOException e) {
        final boolean retrySuccess = retryDefaultSecondary();
        if (!retrySuccess) {
            show(buildIOExceptionMessage(e));
        }
    }

    protected String buildIOExceptionMessage(IOException ioEx) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to refresh the resources.");
        br.addItem("Project List");
        br.addElement(_projectNameList);
        br.addItem("Request URL");
        br.addElement(_requestUrl);
        br.addItem("IOExpception");
        final String ioMsg = ioEx.getMessage();
        br.addElement(ioEx.getClass().getSimpleName());
        br.addElement(ioMsg != null ? ioMsg.trim() : null);
        return br.buildExceptionMessage();
    }

    // -----------------------------------------------------
    //                                       Retry Secondary
    //                                       ---------------
    protected boolean retryDefaultSecondary() {
        if (isDefaultPrimaryRequestUrl()) {
            try {
                final String secondaryRequestUrl = DEFAULT_SECONDARY_REQUEST_URL;
                show("...Retrying refreshing by default secondary URL " + secondaryRequestUrl);
                final DfRefreshResourceRequest request = createRefreshResourceRequest(secondaryRequestUrl);
                final Map<String, Map<String, Object>> resultMap = request.refreshResources();
                if (!hasNotFoundFromTextBody(resultMap)) {
                    show("*Success of the retry refreshing");
                    return true;
                }
                return false;
            } catch (IOException ignored) {
                return false;
            }
        }
        return false;
    }

    protected boolean isDefaultPrimaryRequestUrl() {
        return DEFAULT_PRIMARY_REQUEST_URL.equals(_requestUrl);
    }

    protected boolean retrySpecifiedPort(String projectName, int port, int refreshLevel) {
        if (!_requestUrl.contains("http://localhost:")) {
            return false;
        }
        if (successRetryPort != null && !successRetryPort.equals(port)) {
            return false;
        }
        final String levelExp = buildRefreshLevelExp(refreshLevel);
        final String retryUrl = buildRefreshRetryUrl(port);
        show("...Retrying refreshing by " + levelExp + " URL " + retryUrl + " for " + projectName);
        try {
            final DfRefreshResourceRequest request = createRefreshResourceRequest(projectName, retryUrl);
            final Map<String, Map<String, Object>> resultMap = request.refreshResources();
            if (!hasNotFoundFromTextBody(resultMap)) {
                show(" => success of the retry refreshing: " + port + " for " + projectName);
                if (successRetryPort == null) {
                    successRetryPort = port;
                }
                return true;
            }
            return false;
        } catch (IOException ignored) {
            return false;
        }
    }

    protected String buildRefreshLevelExp(int refreshLevel) {
        final String refreshExp;
        if (refreshLevel == 1) {
            refreshExp = "primary";
        } else if (refreshLevel == 2) {
            refreshExp = "secondary";
        } else if (refreshLevel == 3) {
            refreshExp = "tertiary";
        } else if (refreshLevel == 4) {
            refreshExp = "quaternary";
        } else {
            refreshExp = "quinary or more...";
        }
        return refreshExp;
    }

    protected String buildRefreshRetryUrl(int port) {
        final String portBegin = Srl.substringFirstRear(_requestUrl, "http://localhost:");
        final String rearPath = portBegin.contains("/") ? Srl.substringFirstRear(portBegin, "/") : "";
        return "http://localhost:" + port + "/" + rearPath;
    }

    // -----------------------------------------------------
    //                                        Analyze Result
    //                                        --------------
    protected boolean hasNotFoundFromTextBody(final Map<String, Map<String, Object>> resultMap) {
        for (Entry<String, Map<String, Object>> entry : resultMap.entrySet()) {
            final Map<String, Object> elementMap = entry.getValue();
            final String body = (String) elementMap.get(DfRefreshResourceRequest.KEY_BODY);
            if (body != null && Srl.containsAny(body, "allNotFound", "hasNotFound")) {
                return true;
            }
        }
        return false;
    }

    // -----------------------------------------------------
    //                                        Create Request
    //                                        --------------
    protected DfRefreshResourceRequest createRefreshResourceRequest(String requestUrl) {
        return newRefreshResourceRequest(_projectNameList, requestUrl);
    }

    protected DfRefreshResourceRequest createRefreshResourceRequest(String projectName, String requestUrl) {
        return newRefreshResourceRequest(Arrays.asList(projectName), requestUrl);
    }

    protected DfRefreshResourceRequest newRefreshResourceRequest(List<String> projectNameList, String requestUrl) {
        return new DfRefreshResourceRequest(projectNameList, requestUrl);
    }

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    protected boolean isRefresh() {
        if (_projectNameList == null || _projectNameList.isEmpty()) {
            return false;
        }
        if (Srl.is_Null_or_TrimmedEmpty(_requestUrl)) {
            return false;
        }
        return true;
    }

    protected void show(String msg) {
        _log.info(msg);
    }
}
