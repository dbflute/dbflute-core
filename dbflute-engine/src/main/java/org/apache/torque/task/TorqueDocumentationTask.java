/*
 * Copyright 2014-2019 the original author or authors.
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
 *
 * And the following license definition is for Apache Torque.
 * DBFlute modified this source code and redistribute as same license 'Apache'.
 * /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 *
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 * 
 * - - - - - - - - - -/
 */
package org.apache.torque.task;

/* ====================================================================
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import org.apache.torque.engine.database.model.AppData;
import org.apache.torque.engine.database.model.Database;
import org.apache.velocity.anakia.Escape;
import org.apache.velocity.context.Context;
import org.dbflute.exception.DfRequiredPropertyNotFoundException;
import org.dbflute.exception.DfSchemaSyncCheckGhastlyTragedyException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.infra.doc.decomment.DfDecoMapPickup;
import org.dbflute.infra.doc.hacomment.DfHacoMapPickup;
import org.dbflute.logic.doc.decomment.DfDecommentPickupProcess;
import org.dbflute.logic.doc.hacomment.DfHacommentPickupProcess;
import org.dbflute.logic.doc.lreverse.DfLReverseProcess;
import org.dbflute.logic.doc.spolicy.DfSPolicyChecker;
import org.dbflute.logic.doc.spolicy.display.DfSPolicyDisplay;
import org.dbflute.logic.doc.spolicy.result.DfSPolicyResult;
import org.dbflute.logic.doc.supplement.firstdate.DfFirstDateAgent;
import org.dbflute.logic.doc.synccheck.DfSchemaSyncChecker;
import org.dbflute.logic.jdbc.schemaxml.DfSchemaXmlReader;
import org.dbflute.properties.DfDocumentProperties;
import org.dbflute.properties.DfSchemaPolicyProperties;
import org.dbflute.task.DfDBFluteTaskStatus;
import org.dbflute.task.DfDBFluteTaskStatus.TaskType;
import org.dbflute.task.bs.DfAbstractDbMetaTexenTask;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The DBFlute task generating documentations, SchemaHTML, HistoryHTML and so on.
 * @author modified by jflute (originated in Apache Torque)
 * @author contributed by cabos at maihama sheraton hotel
 * @author contributed by hakiba
 */
public class TorqueDocumentationTask extends DfAbstractDbMetaTexenTask {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The logger instance for this class. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(TorqueDocumentationTask.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String _varyingArg; // set by Ant process
    protected boolean _syncCheckGhastlyTragedy; // set by schema-sync process
    protected DfSPolicyResult _schemaPolicyResult; // null allowed (before policy check or no policy)
    protected DfDecoMapPickup _decoMapPickup; // null allowed (before decomment process)
    protected DfHacoMapPickup _hacoMapPickup; // null allowed (before hacomment process)

    // ===================================================================================
    //                                                                           Beginning
    //                                                                           =========
    @Override
    protected boolean begin() {
        if (getBasicProperties().isSuppressDocTask()) {
            _log.info("...Suppressing Doc task as basicInfoMap.dfprop");
            return false;
        }
        {
            _log.info("+------------------------------------------+");
            _log.info("|                                          |");
            _log.info("|                   Doc                    |");
        }
        if (isLoadDataReverseOnly()) {
            _log.info("|            (LoadDataReverse)             |");
        } else if (isSchemaSyncCheckOnly()) {
            _log.info("|            (SchemaSyncCheck)             |");
        }
        {
            _log.info("|                                          |");
            _log.info("+------------------------------------------+");
        }
        DfDBFluteTaskStatus.getInstance().setTaskType(TaskType.Doc);
        return true;
    }

    // ===================================================================================
    //                                                                         Data Source
    //                                                                         ===========
    @Override
    protected boolean isUseDataSource() {
        // at old age, this is false, but after all, classification needs a connection 
        return true;
    }

    // ===================================================================================
    //                                                                          Schema XML
    //                                                                          ==========
    @Override
    protected DfSchemaXmlReader createSchemaXmlReader() {
        return createSchemaXmlReaderAsCoreToManage();
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Override
    protected void doExecute() {
        if (processSubTask()) {
            return;
        }
        processSchemaHtml(); // making SchemaHTML by velocity process

        // these processes are independent since 0.9.9.7B
        //processLoadDataReverse();
        //processSchemaSyncCheck();
        refreshResources();
    }

    protected boolean processSubTask() {
        if (isLoadDataReverseOnly()) {
            if (!isLoadDataReverseValid()) {
                throwLoadDataReversePropertyNotFoundException();
            }
            initializeSchemaData(); // needed => #thinking needed? ...does not use basic schema data
            processLoadDataReverse();
            return true;
        } else if (isSchemaSyncCheckOnly()) {
            if (!isSchemaSyncCheckValid()) {
                throwSchemaSyncCheckPropertyNotFoundException();
            }
            processSchemaSyncCheck();
            return true;
        }
        return false;
    }

    // -----------------------------------------------------
    //                                            SchemaHtml
    //                                            ----------
    protected void processSchemaHtml() {
        _log.info("");
        _log.info("* * * * * * * * * * *");
        _log.info("*                   *");
        _log.info("*    Schema HTML    *");
        _log.info("*                   *");
        _log.info("* * * * * * * * * * *");
        _documentSelector.selectSchemaHtml(); // regular
        _documentSelector.selectHistoryHtml(); // regular
        _documentSelector.selectPropertiesHtml(); // option
        _documentSelector.selectLastaDocHtml(); // option
        processDecommentPickup();
        processHacommentPickup();
        purelyInitializeSchemaData(); // policy needs database meta data so initialize it here
        processSchemaPolicyCheck(); // only check and keep result here (not ending yet)
        fireVelocityProcess(); // making SchemaHTML
        endingSchemaPolicyResult(); // may throw, after SchemaHTML to show policy result in SchemaHTML since 1.2.0
    }

    // -----------------------------------------------------
    //                                       pickupDecomment
    //                                       ---------------
    protected void processDecommentPickup() {
        _decoMapPickup = createDecommentPickupProcess().pickupDecomment(".");
    }

    protected DfDecommentPickupProcess createDecommentPickupProcess() {
        return new DfDecommentPickupProcess();
    }

    // -----------------------------------------------------
    //                                       pickupHacomment
    //                                       ---------------
    protected void processHacommentPickup() {
        _hacoMapPickup = createHacommentPickupProcess().pickupHacomment(".");
    }

    protected DfHacommentPickupProcess createHacommentPickupProcess() {
        return new DfHacommentPickupProcess();
    }

    // -----------------------------------------------------
    //                                     SchemaPolicyCheck
    //                                     -----------------
    protected void processSchemaPolicyCheck() {
        if (_schemaData == null) {
            throw new IllegalStateException("SchemaPolicyCheck should be before making SchemaHTML because of schema data.");
        }
        final DfSchemaPolicyProperties prop = getSchemaPolicyCheckProperties();
        final DfSPolicyChecker checker = prop.createChecker(_schemaData.getDatabase(), () -> _documentSelector.getSchemaDiffList());
        _schemaPolicyResult = checker.checkPolicyIfNeeds(); // not ending yet (ending after SchemaHTML)
    }

    protected void endingSchemaPolicyResult() { // may throw
        if (_schemaPolicyResult != null) {
            _schemaPolicyResult.ending();
        }
    }

    // -----------------------------------------------------
    //                                       LoadDataReverse
    //                                       ---------------
    protected void processLoadDataReverse() {
        if (!isLoadDataReverseValid()) {
            return;
        }
        // this process is executed separately, 
        // so title logging is already existing at begin()
        //_log.info("");
        //_log.info("* * * * * * * * * * *");
        //_log.info("*                   *");
        //_log.info("* Load Data Reverse *");
        //_log.info("*                   *");
        //_log.info("* * * * * * * * * * *");
        outputLoadDataReverse();
        refreshResources();
    }

    protected void outputLoadDataReverse() {
        final DfLReverseProcess process = createLReverseProcess();
        process.execute();
    }

    protected DfLReverseProcess createLReverseProcess() {
        return new DfLReverseProcess(getDataSource());
    }

    protected void throwLoadDataReversePropertyNotFoundException() {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the property for LoadDataReverse.");
        br.addItem("Advice");
        br.addElement("You should set the property like this:");
        br.addElement("[documentDefinitionMap.dfprop]");
        br.addElement("  ; loadDataReverseMap = map:{");
        br.addElement("      ; recordLimit = -1");
        br.addElement("      ; isReplaceSchemaDirectUse = true");
        br.addElement("      ; isOverrideExistingDataFile = false");
        br.addElement("  }");
        final String msg = br.buildExceptionMessage();
        throw new DfRequiredPropertyNotFoundException(msg);
    }

    // -----------------------------------------------------
    //                                       SchemaSyncCheck
    //                                       ---------------
    protected void processSchemaSyncCheck() {
        try {
            doProcessSchemaSyncCheck();
        } catch (DfSchemaSyncCheckGhastlyTragedyException e) {
            _syncCheckGhastlyTragedy = true;
            throw e;
        } finally {
            refreshResources();
        }
    }

    protected void doProcessSchemaSyncCheck() {
        if (!isSchemaSyncCheckValid()) {
            return;
        }
        // this process is executed separately, 
        // so title logging is already existing at begin()
        //_log.info("");
        //_log.info("* * * * * * * * * * *");
        //_log.info("*                   *");
        //_log.info("* Schema Sync Check *");
        //_log.info("*                   *");
        //_log.info("* * * * * * * * * * *");
        final DfSchemaSyncChecker checker = new DfSchemaSyncChecker(getDataSource());
        try {
            checker.checkSync();
        } catch (DfSchemaSyncCheckGhastlyTragedyException e) {
            _documentSelector.selectSchemaSyncCheckResultHtml();
            fireVelocityProcess();
            throw e;
        }
    }

    protected void throwSchemaSyncCheckPropertyNotFoundException() {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the property for SchemaSyncCheck.");
        br.addItem("Advice");
        br.addElement("You should set the property like this:");
        br.addElement("[documentDefinitionMap.dfprop]");
        br.addElement("  ; schemaSyncCheckMap = map:{");
        br.addElement("      ; url = jdbc:...");
        br.addElement("      ; schema = EXAMPLEDB");
        br.addElement("      ; user = exampuser");
        br.addElement("      ; password = exampword");
        br.addElement("  }");
        final String msg = br.buildExceptionMessage();
        throw new DfRequiredPropertyNotFoundException(msg);
    }

    // ===================================================================================
    //                                                                  Prepare Generation
    //                                                                  ==================
    @Override
    protected void initializeSchemaData() { // basically called in fireVelocityProcess()
        if (isLoadDataReverseOnly() || isSchemaSyncCheckOnly()) { // don't use basic schema data
            _schemaData = AppData.createAsEmpty(); // not to depends on JDBC task
        } else { // normally here
            if (_schemaData == null) { // basically false, already initialized in SchemaHTML process so just in case
                purelyInitializeSchemaData();
            }
            final Database database = _schemaData.getDatabase();
            if (_schemaPolicyResult != null) { // null allowed when no policy
                database.setSchemaPolicyDisplay(new DfSPolicyDisplay(_schemaPolicyResult));
            }
            database.setEmbeddedPickup(_decoMapPickup);
            database.setFirstDateAgent(new DfFirstDateAgent(() -> _documentSelector.getSchemaDiffList()));
        }
    }

    protected void purelyInitializeSchemaData() {
        super.initializeSchemaData();
    }

    // ===================================================================================
    //                                                                          Final Info
    //                                                                          ==========
    @Override
    public String getFinalInformation() {
        return buildReplaceSchemaFinalMessage();
    }

    protected String buildReplaceSchemaFinalMessage() {
        if (_syncCheckGhastlyTragedy) {
            final StringBuilder sb = new StringBuilder();
            sb.append("    * * * * * * * * * *").append(ln());
            sb.append("    * Ghastly Tragedy *").append(ln());
            sb.append("    * * * * * * * * * *");
            return sb.toString();
        }
        return null;
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfDocumentProperties getDocumentProperties() {
        return getProperties().getDocumentProperties();
    }

    protected boolean isLoadDataReverseValid() {
        return getDocumentProperties().isLoadDataReverseValid();
    }

    protected boolean isSchemaSyncCheckValid() {
        return getDocumentProperties().isSchemaSyncCheckValid();
    }

    protected DfSchemaPolicyProperties getSchemaPolicyCheckProperties() {
        return getProperties().getSchemaPolicyProperties();
    }

    // ===================================================================================
    //                                                                      Varying Option
    //                                                                      ==============
    protected boolean isLoadDataReverseOnly() {
        return _varyingArg != null && _varyingArg.equals("load-data-reverse");
    }

    protected boolean isSchemaSyncCheckOnly() {
        return _varyingArg != null && _varyingArg.equals("schema-sync-check");
    }

    // ===================================================================================
    //                                                                  Prepare Generation
    //                                                                  ==================
    @Override
    public Context initControlContext() throws Exception {
        final Context context = super.initControlContext();
        context.put("escape", new Escape());
        context.put("selector", _documentSelector);
        return context;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setVaryingArg(String varyingArg) {
        if (Srl.is_Null_or_TrimmedEmpty(varyingArg)) {
            return;
        }
        _varyingArg = varyingArg;
    }
}
