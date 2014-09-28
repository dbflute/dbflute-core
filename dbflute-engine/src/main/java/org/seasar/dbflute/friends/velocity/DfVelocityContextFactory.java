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
package org.seasar.dbflute.friends.velocity;

import java.util.ArrayList;
import java.util.List;

import org.apache.torque.engine.database.model.AppData;
import org.apache.velocity.VelocityContext;
import org.seasar.dbflute.properties.assistant.freegen.DfFreeGenManager;
import org.seasar.dbflute.properties.assistant.freegen.DfFreeGenRequest;
import org.seasar.dbflute.task.bs.assistant.DfDocumentSelector;

/**
 * @author jflute
 * @since 0.9.5.1 (2009/06/26 Friday)
 */
public class DfVelocityContextFactory {

    public VelocityContext createAsCore(AppData appData, DfDocumentSelector selector) {
        final VelocityContext context = new VelocityContext();
        final List<AppData> dataModels = new ArrayList<AppData>();
        dataModels.add(appData);
        context.put("dataModels", dataModels); // for compatibility
        context.put("schemaData", appData);
        context.put("selector", selector); // basically for Doc task
        return context;
    }

    public VelocityContext createAsFreeGen(DfFreeGenManager freeGenManager, List<DfFreeGenRequest> freeGenRequestList) {
        final VelocityContext context = new VelocityContext();
        context.put("manager", freeGenManager);
        context.put("requestList", freeGenRequestList);
        return context;
    }
}
