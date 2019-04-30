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
 */
package org.dbflute.friends.velocity;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.Log4JLogChute;
import org.dbflute.friends.log4j.DfFlutistRollingFileAppender;
import org.dbflute.util.DfTypeUtil;

/**
 * The log system using Log4j for DBFlute.
 * @author jflute
 * @since 0.9.5.1 (2009/06/23 Tuesday)
 */
public class DfFlutistLog4JLogSystem extends Log4JLogChute {

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
        final String logfile = "./log/velocity.log";
        try {
            logger = Logger.getLogger(getClass().getName());
            logger.setAdditivity(false);
            logger.setLevel(Level.DEBUG);

            final DfFlutistRollingFileAppender appender = createOriginalRollingFileAppender(logfile);
            appender.setMaxBackupIndex(2);
            appender.setMaximumFileSize(300000);
            logger.addAppender(appender);

            log(0, ""); // as begin mark.
            log(0, DfTypeUtil.toClassTitle(this) + " initialized using logfile '" + logfile + "'");
        } catch (Exception e) {
            System.out.println("PANIC : error configuring " + DfTypeUtil.toClassTitle(this));
            e.printStackTrace();
        }
    }

    protected DfFlutistRollingFileAppender createOriginalRollingFileAppender(String logfile) throws Exception {
        return new DfFlutistRollingFileAppender(new PatternLayout("%d - %m%n"), logfile, true);
    }
}
