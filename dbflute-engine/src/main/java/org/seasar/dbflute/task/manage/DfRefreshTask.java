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
package org.seasar.dbflute.task.manage;

import org.seasar.dbflute.logic.DfDBFluteTaskUtil;
import org.seasar.dbflute.logic.manage.DfRefreshMan;
import org.seasar.dbflute.task.DfDBFluteTaskStatus;
import org.seasar.dbflute.task.DfDBFluteTaskStatus.TaskType;
import org.seasar.dbflute.task.bs.DfAbstractTask;
import org.seasar.dbflute.util.DfTraceViewUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.9.8.5 (2011/06/09 Thursday)
 */
public class DfRefreshTask extends DfAbstractTask {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String _refreshProject;

    // ===================================================================================
    //                                                                           Beginning
    //                                                                           =========
    @Override
    protected boolean begin() {
        // Refresh task is for utility so it's to be quietly as it can
        //_log.info("+------------------------------------------+");
        //_log.info("|                                          |");
        //_log.info("|                 Refresh                  |");
        //_log.info("|                                          |");
        //_log.info("+------------------------------------------+");
        DfDBFluteTaskStatus.getInstance().setTaskType(TaskType.Refresh);
        return true;
    }

    // ===================================================================================
    //                                                                          DataSource
    //                                                                          ==========
    @Override
    protected boolean isUseDataSource() {
        return false;
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Override
    protected void doExecute() {
        final DfRefreshMan refreshMan = prepareRefreshMap();
        refreshMan.refresh();
    }

    protected DfRefreshMan prepareRefreshMap() {
        return new DfRefreshMan().specifyRefreshProject(_refreshProject);
    }

    // ===================================================================================
    //                                                                       Final Message
    //                                                                       =============
    @Override
    protected void showFinalMessage(long before, long after, boolean abort) {
        final StringBuilder sb = new StringBuilder();
        sb.append("[Final Message]: ").append(getPerformanceView(after - before));
        if (abort) {
            sb.append(" *Abort");
        }
        DfDBFluteTaskUtil.logFinalMessage(sb.toString());
    }

    protected String getPerformanceView(long mil) {
        return DfTraceViewUtil.convertToPerformanceView(mil);
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setRefreshProject(String refreshProject) {
        if (Srl.is_Null_or_TrimmedEmpty(refreshProject)) {
            return;
        }
        if (refreshProject.equals("${dfprj}")) {
            return;
        }
        _refreshProject = refreshProject;
    }
}
