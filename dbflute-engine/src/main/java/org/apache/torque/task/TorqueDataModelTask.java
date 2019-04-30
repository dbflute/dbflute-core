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

import org.dbflute.logic.generate.gapile.DfGapileProcess;
import org.dbflute.logic.generate.language.DfLanguageDependency;
import org.dbflute.logic.jdbc.schemaxml.DfSchemaXmlReader;
import org.dbflute.logic.sql2entity.analyzer.DfOutsideSqlPack;
import org.dbflute.logic.sql2entity.bqp.DfBehaviorQueryPathSetupper;
import org.dbflute.properties.DfBasicProperties;
import org.dbflute.properties.DfLittleAdjustmentProperties;
import org.dbflute.task.DfDBFluteTaskStatus;
import org.dbflute.task.DfDBFluteTaskStatus.TaskType;
import org.dbflute.task.bs.DfAbstractDbMetaTexenTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The DBFlute task generating classes from schema meta data.
 * @author modified by jflute (originated in Apache Torque)
 */
public class TorqueDataModelTask extends DfAbstractDbMetaTexenTask {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger _log = LoggerFactory.getLogger(TorqueDataModelTask.class);

    // ===================================================================================
    //                                                                           Beginning
    //                                                                           =========
    @Override
    protected boolean begin() {
        if (getBasicProperties().isSuppressGenerateTask()) {
            _log.info("...Suppressing Generate task as basicInfoMap.dfprop");
            return false;
        }
        _log.info("+------------------------------------------+");
        _log.info("|                                          |");
        _log.info("|                 Generate                 |");
        _log.info("|                                          |");
        _log.info("+------------------------------------------+");
        DfDBFluteTaskStatus.getInstance().setTaskType(TaskType.Generate);
        return true;
    }

    // ===================================================================================
    //                                                                         Data Source
    //                                                                         ===========
    @Override
    protected boolean isUseDataSource() {
        return true;
    }

    // ===================================================================================
    //                                                                          Schema XML
    //                                                                          ==========
    @Override
    protected DfSchemaXmlReader createSchemaXmlReader() {
        return createSchemaXmlReaderAsCoreToGenerate();
    }

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Override
    protected void doExecute() {
        setupControlTemplate();
        fireVelocityProcess();
        setupBehaviorQueryPath();
        reflectGapileClassIfNeeds();
        showSkippedFileInformation();
        refreshResources();
    }

    protected void setupControlTemplate() {
        final DfLittleAdjustmentProperties littleProp = getLittleAdjustmentProperties();
        final String title;
        final String control;
        final DfBasicProperties basicProp = getBasicProperties();
        if (littleProp.isAlternateGenerateControlValid()) {
            title = "alternate control";
            control = littleProp.getAlternateGenerateControl();
        } else {
            final DfLanguageDependency lang = getBasicProperties().getLanguageDependency();
            if (basicProp.isApplicationBehaviorProject()) {
                title = lang.getLanguageTitle() + " BhvAp";
                control = lang.getGenerateControlBhvAp();
            } else {
                title = lang.getLanguageTitle();
                control = lang.getGenerateControl();
            }
        }
        _log.info("");
        _log.info("...Using " + title + " control: " + control);
        setControlTemplate(control);
    }

    // ===================================================================================
    //                                                                 Behavior Query Path
    //                                                                 ===================
    protected void setupBehaviorQueryPath() {
        final DfOutsideSqlPack outsideSqlPack = collectOutsideSqlChecked();
        final DfBehaviorQueryPathSetupper setupper = new DfBehaviorQueryPathSetupper();
        setupper.setupBehaviorQueryPath(outsideSqlPack);
    }

    // ===================================================================================
    //                                                                        Gapile Class
    //                                                                        ============
    protected void reflectGapileClassIfNeeds() {
        final DfGapileProcess process = new DfGapileProcess();
        process.reflectIfNeeds();
    }
}