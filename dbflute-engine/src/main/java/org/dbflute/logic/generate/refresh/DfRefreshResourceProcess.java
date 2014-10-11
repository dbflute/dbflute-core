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
package org.dbflute.logic.generate.refresh;

import java.io.IOException;
import java.util.List;

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
        _log.info("...Refreshing: " + _projectNameList);
        try {
            new DfRefreshResourceRequest(_projectNameList, _requestUrl).refreshResources();
        } catch (IOException e) {
            final String msg = buildIOExceptionMessage(e);
            _log.info(msg);
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
