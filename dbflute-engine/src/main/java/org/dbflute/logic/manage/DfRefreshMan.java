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
package org.dbflute.logic.manage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dbflute.DfBuildProperties;
import org.dbflute.helper.filesystem.FileTextIO;
import org.dbflute.helper.filesystem.FileTextLineFilter;
import org.dbflute.infra.manage.refresh.DfRefreshResourceRequest;
import org.dbflute.logic.generate.refresh.DfRefreshResourceProcess;
import org.dbflute.properties.DfRefreshProperties;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfStringUtil;
import org.dbflute.util.Srl;
import org.dbflute.util.Srl.ScopeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 1.0.5K (2014/08/15 Friday)
 */
public class DfRefreshMan {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(DfRefreshMan.class);
    private static final String AUTO_DETECT_MARK = DfRefreshResourceRequest.AUTO_DETECT_MARK;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected boolean _specified;
    protected String _specifiedRefreshProject; // e.g. from command line argument

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    public void refresh() {
        final List<String> refreshList = getRefreshProjectList();
        if (refreshList.isEmpty()) {
            if (_specified) { // e.g. refresh task
                _log.info("*No refresh project");
            }
            return; // no settings
        }
        final String requestUrl = getRefreshRequestUrl();
        if (requestUrl == null || requestUrl.trim().length() == 0) {
            String msg = "No refresh request URL specified.";
            throw new IllegalStateException(msg);
        }
        createResourceProcess(refreshList, requestUrl).refreshResources();
    }

    protected DfRefreshResourceProcess createResourceProcess(List<String> refreshList, String requestUrl) {
        return new DfRefreshResourceProcess(refreshList, requestUrl);
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
        return resolveProjectAutoDetect(refreshList);
    }

    protected List<String> resolveProjectAutoDetect(List<String> refreshList) {
        if (refreshList.isEmpty()) {
            return refreshList;
        }
        final List<String> filteredList = new ArrayList<String>(refreshList.size());
        for (String projectName : refreshList) {
            if (AUTO_DETECT_MARK.equalsIgnoreCase(projectName)) {
                final String eclipseProjectName = detectEclipseProjectName();
                if (eclipseProjectName != null) {
                    filteredList.add(0, eclipseProjectName); // first refresh
                    continue;
                }
                _log.info("*Cannot auto-detect your refresh Eclipse proejct.");
                // no continue so added plainly as dummy to avoid non-specified exception
                // cannot auto-detect has no exception
            }
            filteredList.add(projectName);
        }
        return filteredList;
    }

    protected String detectEclipseProjectName() {
        // e.g.
        //  PROJECT_ROOT
        //   |-dbflute_maihamadb
        //   |-mydbflute
        //   |-...
        //   |-.project
        final File projectFile = new File("../.project");
        if (!projectFile.exists()) {
            return null;
        }
        final Set<String> resultSet = new HashSet<String>();
        try {
            new FileTextIO().encodeAsUTF8().readFilteringLine(new FileInputStream(projectFile), new FileTextLineFilter() {
                boolean _found = false;

                public String filter(String line) {
                    if (_found || !line.contains("<name>")) {
                        return null;
                    }
                    ScopeInfo scopeInfo = Srl.extractScopeFirst(line, "<name>", "</name>");
                    if (scopeInfo == null) { // basically no way, just in case
                        return null;
                    }
                    final String content = scopeInfo.getContent().trim();
                    resultSet.add(content);
                    _found = true;
                    return null;
                }
            });
        } catch (FileNotFoundException ignored) { // no way because of already checked, just in case
            return null;
        }
        if (resultSet.isEmpty()) {
            _log.info("*The .project file exists but not found project name: " + projectFile);
            return null;
        }
        return resultSet.iterator().next();
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
        _specified = true;
        _specifiedRefreshProject = refreshProject;
        return this;
    }
}
