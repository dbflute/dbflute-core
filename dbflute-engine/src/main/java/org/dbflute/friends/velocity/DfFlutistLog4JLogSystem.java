/*
 * Copyright 2014-2024 the original author or authors.
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
package org.dbflute.friends.velocity;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Log4jLogChute implementation removed to suppress velocity.log completely by jflute (2022/04/17)
// (actually this class is e.g. EmptyLogChute)
/**
 * The log system using Log4j for DBFlute.
 * @author jflute
 * @since 0.9.5.1 (2009/06/23 Tuesday)
 */
public class DfFlutistLog4JLogSystem implements LogChute {

    private static final Logger _log = LoggerFactory.getLogger(DfFlutistLog4JLogSystem.class);

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfFlutistLog4JLogSystem() {
    }

    // ===================================================================================
    //                                                                 Initialize Override
    //                                                                 ===================
    @Override
    public void init(RuntimeServices rs) {
        _log.info("...Suppressing velocity.log, which is unneeded for also debug");
        // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
        // velocity.log is unneeded for debug so remove it (also for removing log4j dependencies) by jflute (2022/04/17)
        // _/_/_/_/_/_/_/_/_/_/
        //final String logfile = "./log/velocity.log";
        //try {
        //    logger = Logger.getLogger(getClass().getName());
        //    logger.setAdditivity(false);
        //    logger.setLevel(Level.DEBUG);
        //
        //    final DfFlutistRollingFileAppender appender = createOriginalRollingFileAppender(logfile);
        //    appender.setMaxBackupIndex(2);
        //    appender.setMaximumFileSize(300000);
        //    logger.addAppender(appender);
        //
        //    log(0, ""); // as begin mark.
        //    log(0, DfTypeUtil.toClassTitle(this) + " initialized using logfile '" + logfile + "'");
        //} catch (Exception e) {
        //    System.out.println("PANIC : error configuring " + DfTypeUtil.toClassTitle(this));
        //    e.printStackTrace();
        //}
    }

    @Override
    public void log(int level, String message) {
        // do nothinng
    }

    @Override
    public void log(int level, String message, Throwable t) {
        // do nothinng
    }

    @Override
    public boolean isLevelEnabled(int level) {
        return false;
    }

    // already unneeded
    //protected DfFlutistRollingFileAppender createOriginalRollingFileAppender(String logfile) throws Exception {
    //    return new DfFlutistRollingFileAppender(new PatternLayout("%d - %m%n"), logfile, true);
    //}
}
