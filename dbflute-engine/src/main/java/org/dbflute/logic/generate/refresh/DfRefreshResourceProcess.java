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
package org.dbflute.logic.generate.refresh;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

    protected static final String PRIMARY_REQUEST_URL = "http://localhost:8386/"; // setting default
    protected static final String SECONDARY_REQUEST_URL = "http://localhost:8387/";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final List<String> _projectNameList;
    protected final String _requestUrl;

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
        _log.info("...Refreshing " + _projectNameList + " by " + _requestUrl); // not null here
        final DfRefreshResourceRequest request = createRefreshResourceRequest(_requestUrl);
        try {
            final Map<String, Map<String, Object>> resultMap = request.refreshResources();
            if (existsNotFound(resultMap)) {
                handleNotFoundProject();
            }
        } catch (IOException e) {
            handleRefreshIOException(e);
        }
    }

    // -----------------------------------------------------
    //                                      NotFound Project
    //                                      ----------------
    protected void handleNotFoundProject() {
        final boolean retrySuccess = retrySecondary();
        if (!retrySuccess) {
            _log.info(buildNotFoundProjectMessage());
        }
    }

    protected String buildNotFoundProjectMessage() {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the projects in the Eclipse.");
        br.addItem("Project List");
        br.addElement(_projectNameList);
        br.addItem("Request URL");
        br.addElement(_requestUrl);
        return br.buildExceptionMessage();
    }

    // -----------------------------------------------------
    //                                   Refresh IOExpcetion
    //                                   -------------------
    protected void handleRefreshIOException(IOException e) {
        final boolean retrySuccess = retrySecondary();
        if (!retrySuccess) {
            _log.info(buildIOExceptionMessage(e));
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
    protected boolean retrySecondary() {
        if (isPrimaryRequestUrl()) {
            try {
                _log.info("...Retrying refreshing by secondary URL " + SECONDARY_REQUEST_URL);
                final DfRefreshResourceRequest request = createRefreshResourceRequest(SECONDARY_REQUEST_URL);
                final Map<String, Map<String, Object>> resultMap = request.refreshResources();
                if (!existsNotFound(resultMap)) {
                    _log.info("*Success of the retry refreshing");
                    return true;
                }
                return false;
            } catch (IOException ignored) {
                return false;
            }
        }
        return false;
    }

    protected boolean isPrimaryRequestUrl() {
        return PRIMARY_REQUEST_URL.equals(_requestUrl);
    }

    // -----------------------------------------------------
    //                                        Analyze Result
    //                                        --------------
    protected boolean existsNotFound(final Map<String, Map<String, Object>> resultMap) {
        for (Entry<String, Map<String, Object>> entry : resultMap.entrySet()) {
            final Map<String, Object> elementMap = entry.getValue();
            final String body = (String) elementMap.get(DfRefreshResourceRequest.KEY_BODY);
            if (body != null && body.contains("*NotFound")) {
                return true;
            }
        }
        return false;
    }

    // -----------------------------------------------------
    //                                        Create Request
    //                                        --------------
    protected DfRefreshResourceRequest createRefreshResourceRequest(String requestUrl) {
        return new DfRefreshResourceRequest(_projectNameList, requestUrl);
    }

    // ===================================================================================
    //                                                                    Refresh Resource
    //                                                                    ================
    protected boolean isRefresh() {
        if (_projectNameList == null || _projectNameList.isEmpty()) {
            return false;
        }
        if (Srl.is_Null_or_TrimmedEmpty(_requestUrl)) {
            return false;
        }
        return true;
    }
}
