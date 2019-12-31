/*
 * Copyright 2014-2020 the original author or authors.
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
package org.dbflute.bhv.core.command;

import java.util.List;

import org.dbflute.Entity;
import org.dbflute.s2dao.jdbc.TnResultSetHandler;
import org.dbflute.s2dao.metadata.TnBeanMetaData;

/**
 * @author jflute
 * @param <ENTITY> The type of entity.
 */
public class SelectListCBCommand<ENTITY extends Entity> extends AbstractSelectCBReturnEntityCommand<List<ENTITY>> {

    // ===================================================================================
    //                                                                   Basic Information
    //                                                                   =================
    public String getCommandName() {
        return "selectList";
    }

    public Class<?> getCommandReturnType() {
        return List.class;
    }

    // ===================================================================================
    //                                                               SqlExecution Handling
    //                                                               =====================
    protected TnResultSetHandler createReturnEntityResultSetHandler(TnBeanMetaData bmd) {
        return super.createBeanListResultSetHandler(bmd);
    }
}
