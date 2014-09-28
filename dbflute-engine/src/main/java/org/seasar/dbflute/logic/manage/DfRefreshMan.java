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
package org.seasar.dbflute.logic.manage;

import java.util.List;

import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.logic.generate.refresh.DfRefreshResourceProcess;
import org.seasar.dbflute.properties.DfRefreshProperties;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.DfStringUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.0.5K (2014/08/15 Friday)
 */
public class DfRefreshMan {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String _specifiedRefreshProject;

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    public void refresh() {
        final List<String> refreshList = getRefreshProjectList();
        if (refreshList.isEmpty()) {
            String msg = "No refresh project specified.";
            throw new IllegalStateException(msg);
        }
        final String requestUrl = getRefreshRequestUrl();
        if (requestUrl == null || requestUrl.trim().length() == 0) {
            String msg = "No refresh request URL specified.";
            throw new IllegalStateException(msg);
        }
        new DfRefreshResourceProcess(refreshList, requestUrl).refreshResources();
    }

    protected List<String> getRefreshProjectList() {
        final List<String> refreshList = DfCollectionUtil.newArrayList();
        final List<String> specifiedList = getSpecifiedProjectList();
        if (!specifiedList.isEmpty()) {
            refreshList.addAll(specifiedList);
        } else {
            if (getRefreshProperties().hasRefreshDefinition()) {
                refreshList.addAll(getRefreshProperties().getProjectNameList());
            }
        }
        return refreshList;
    }

    protected List<String> getSpecifiedProjectList() {
        if (Srl.is_NotNull_and_NotTrimmedEmpty(_specifiedRefreshProject)) {
            return DfStringUtil.splitListTrimmed(_specifiedRefreshProject, "/");
        }
        return DfCollectionUtil.emptyList();
    }

    protected String getRefreshRequestUrl() {
        return getRefreshProperties().getRequestUrl();
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfRefreshProperties getRefreshProperties() {
        return getProperties().getRefreshProperties();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public DfRefreshMan specifyRefreshProject(String refreshProject) {
        _specifiedRefreshProject = refreshProject;
        return this;
    }
}
