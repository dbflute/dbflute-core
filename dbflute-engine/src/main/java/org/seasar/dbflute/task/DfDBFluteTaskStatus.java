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
package org.seasar.dbflute.task;

/**
 * @author jflute
 * @since 0.9.9.1B (2011/10/17 Monday)
 */
public class DfDBFluteTaskStatus {

    // ===================================================================================
    //                                                                           Singleton
    //                                                                           =========
    private static final DfDBFluteTaskStatus _instance = new DfDBFluteTaskStatus();

    public static final DfDBFluteTaskStatus getInstance() {
        return _instance;
    }

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected TaskType _taskType;

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    public boolean isDocTask() {
        return TaskType.Doc.equals(_taskType);
    }

    public boolean isReplaceSchema() {
        return TaskType.ReplaceSchema.equals(_taskType);
    }

    // ===================================================================================
    //                                                                           Task Type
    //                                                                           =========
    public enum TaskType {
        JDBC, Doc, Generate, Sql2Entity, OutsideSqlTest, ReplaceSchema, Refresh, TakeAssert, FreeGen, Intro, Upgrade
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public TaskType getTaskType() {
        return _taskType;
    }

    public void setTaskType(TaskType taskType) {
        this._taskType = taskType;
    }
}
