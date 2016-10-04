/*
 * Copyright 2014-2016 the original author or authors.
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
package org.dbflute.task.manage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.dbflute.friends.velocity.DfVelocityContextFactory;
import org.dbflute.helper.function.IndependentProcessor;
import org.dbflute.logic.generate.language.DfLanguageDependency;
import org.dbflute.logic.manage.freegen.DfFreeGenInitializer;
import org.dbflute.logic.manage.freegen.DfFreeGenManager;
import org.dbflute.logic.manage.freegen.DfFreeGenRequest;
import org.dbflute.properties.DfFreeGenProperties;
import org.dbflute.task.DfDBFluteTaskStatus;
import org.dbflute.task.DfDBFluteTaskStatus.TaskType;
import org.dbflute.task.bs.DfAbstractTexenTask;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 * @since 0.9.9.4C (2012/10/06 Thursday)
 */
public class DfFreeGenTask extends DfAbstractTexenTask {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(DfFreeGenTask.class);
    protected static final String CONTROL_FREEGEN_VM = "ControlFreeGen.vm";
    protected static List<IndependentProcessor> lazyCallList = new ArrayList<IndependentProcessor>();

    public static void regsiterLazyCall(IndependentProcessor processor) {
        lazyCallList.add(processor);
    }

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final List<DfFreeGenRequest> _freeGenRequestList = new ArrayList<DfFreeGenRequest>();
    protected String _genTarget;

    // ===================================================================================
    //                                                                           Beginning
    //                                                                           =========
    @Override
    protected boolean begin() {
        _log.info("+------------------------------------------+");
        _log.info("|                                          |");
        _log.info("|                 Free Gen                 |");
        _log.info("|                                          |");
        _log.info("+------------------------------------------+");
        DfDBFluteTaskStatus.getInstance().setTaskType(TaskType.FreeGen);
        return true;
    }

    // ===================================================================================
    //                                                                          DataSource
    //                                                                          ==========
    @Override
    protected boolean isUseDataSource() {
        return true;
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Override
    protected void doExecute() {
        prepareFreeGenRequestList();
        processESFluteFreeGen();
        processLastaFluteFreeGen();
        processApplicationFreeGen();
        refreshResources();
        callLazily();
    }

    protected void prepareFreeGenRequestList() {
        final DfFreeGenInitializer initializer = new DfFreeGenInitializer();
        final List<DfFreeGenRequest> requestList = initializer.initialize(requestName -> {
            return isTargetRequest(requestName);
        });
        _freeGenRequestList.addAll(requestList);
    }

    protected boolean isTargetRequest(String requestName) {
        return _genTarget == null || _genTarget.equalsIgnoreCase(requestName);
    }

    // ===================================================================================
    //                                                                             ESFlute
    //                                                                             =======
    protected void processESFluteFreeGen() {
        if (!hasESFluteRequest()) {
            return;
        }
        final DfLanguageDependency lang = getBasicProperties().getLanguageDependency();
        final String control = lang.getESFluteFreeGenControl();
        _log.info("");
        _log.info("* * * * * * * * * * * * * *");
        _log.info("* Process ESFlute FreeGen *");
        _log.info("* * * * * * * * * * * * * *");
        _log.info("...Using control: " + control);
        setControlTemplate(control);
        fireVelocityProcess();
        removeESFluteDoneRequest();
    }

    protected boolean hasESFluteRequest() {
        for (DfFreeGenRequest request : _freeGenRequestList) {
            final Object es = request.getOptionMap().get("isESFlute");
            if (es != null && (boolean) es) {
                return true;
            }
        }
        return false;
    }

    protected void removeESFluteDoneRequest() {
        final List<DfFreeGenRequest> doneList = new ArrayList<DfFreeGenRequest>();
        for (DfFreeGenRequest request : _freeGenRequestList) {
            final Object la = request.getOptionMap().get("isESFlute");
            if (la != null && (boolean) la) {
                doneList.add(request);
            }
        }
        _log.info("...Removing esflute requests: " + doneList.size());
        for (DfFreeGenRequest request : doneList) {
            _freeGenRequestList.remove(request);
        }
    }

    // ===================================================================================
    //                                                                          LastaFlute
    //                                                                          ==========
    protected void processLastaFluteFreeGen() {
        if (!hasLastaFluteRequest()) {
            return;
        }
        final DfLanguageDependency lang = getBasicProperties().getLanguageDependency();
        final String control = lang.getLastaFluteFreeGenControl();
        _log.info("");
        _log.info("* * * * * * * * * * * * * * * *");
        _log.info("* Process LastaFlute FreeGen  *");
        _log.info("* * * * * * * * * * * * * * * *");
        _log.info("...Using control: " + control);
        setControlTemplate(control);
        fireVelocityProcess();
        removeLastaFluteDoneRequest();
    }

    protected boolean hasLastaFluteRequest() {
        for (DfFreeGenRequest request : _freeGenRequestList) {
            final Object la = request.getOptionMap().get("isLastaFlute");
            if (la != null && (boolean) la) {
                return true;
            }
        }
        return false;
    }

    protected void removeLastaFluteDoneRequest() {
        final List<DfFreeGenRequest> doneList = new ArrayList<DfFreeGenRequest>();
        for (DfFreeGenRequest request : _freeGenRequestList) {
            final Object la = request.getOptionMap().get("isLastaFlute");
            if (la != null && (boolean) la) {
                doneList.add(request);
            }
        }
        _log.info("...Removing lastaflute requests: " + doneList.size());
        for (DfFreeGenRequest request : doneList) {
            _freeGenRequestList.remove(request);
        }
    }

    // ===================================================================================
    //                                                                 Application FreeGen
    //                                                                 ===================
    protected void processApplicationFreeGen() {
        final String freeSpace = "./freegen";
        try {
            final File freeGenDir = new File(freeSpace);
            if (!freeGenDir.exists()) {
                _log.info("*No freeGen space so skip application freeGen: path=" + freeSpace);
                return;
            }
            templatePath = freeGenDir.getCanonicalPath();
        } catch (IOException e) {
            String msg = "Failed to set template path: " + freeSpace;
            throw new IllegalStateException(msg, e);
        }
        final String control = CONTROL_FREEGEN_VM;
        final String pathOfControl = freeSpace + "/" + control;
        if (!new File(pathOfControl).exists()) {
            _log.info("*No freeGen control vm so skip application freeGen: path=" + pathOfControl);
            return;
        }
        _log.info("");
        _log.info("* * * * * * * * * * * * * * * *");
        _log.info("* Process Application FreeGen *");
        _log.info("* * * * * * * * * * * * * * * *");
        _log.info("...Using control: " + pathOfControl);
        setControlTemplate(control); // from templatePath, so pure name here
        fireVelocityProcess();
    }

    // ===================================================================================
    //                                                                           Lazy Call
    //                                                                           =========
    public void callLazily() {
        if (lazyCallList.isEmpty()) {
            return;
        }
        _log.info("...Calling processors lazily: " + lazyCallList.size());
        for (IndependentProcessor processor : lazyCallList) {
            processor.process();
        }
    }

    // ===================================================================================
    //                                                                  Prepare Generation
    //                                                                  ==================
    @Override
    public Context initControlContext() throws Exception {
        _log.info("");
        _log.info("...Preparing generation of free generate");
        final StringBuilder sb = new StringBuilder();
        for (DfFreeGenRequest request : _freeGenRequestList) {
            sb.append(ln()).append("[").append(request.getRequestName()).append("]");
            sb.append(ln()).append(" resource : ").append(request.getResource());
            sb.append(ln()).append(" output   : ").append(request.getOutput());
            final String tableMapExp = Srl.cut(request.getOptionMap().toString(), 3000, "...");
            sb.append(ln()).append(" tableMap : ").append(tableMapExp); // possible too big
        }
        _log.info(sb.toString());
        return createVelocityContext();
    }

    protected VelocityContext createVelocityContext() {
        final DfVelocityContextFactory factory = createVelocityContextFactory();
        final DfFreeGenManager manager = DfFreeGenInitializer.getManager();
        return factory.createAsFreeGen(manager, _freeGenRequestList);
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected DfFreeGenProperties getFreeGenProperties() {
        return getProperties().getFreeGenProperties();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setGenTarget(String genTarget) {
        if (Srl.is_Null_or_TrimmedEmpty(genTarget)) {
            return;
        }
        if (genTarget.equals("${gentgt}")) {
            return;
        }
        _genTarget = genTarget;
    }
}
