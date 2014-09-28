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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.context.Context;

/**
 * @author jflute
 * @since 0.7.6 (2008/07/01 Tuesday)
 */
public abstract class DfGenerator {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance. */
    public static final Log _log = LogFactory.getLog(DfGenerator.class);

    /** The implementation instance of generator. (Singleton) */
    private static volatile DfGenerator _instance;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    protected DfGenerator() {
    }

    // ===================================================================================
    //                                                                  Singleton Instance
    //                                                                  ==================
    public static DfGenerator getInstance() {
        if (_instance != null) {
            return _instance;
        }
        synchronized (DfGenerator.class) {
            if (_instance != null) {
                return _instance;
            }
            // use flutist generator fixedly @since 1.0.3
            // (not use Velocity's generator in DBFlute)
            _instance = DfFlutistGenerator.getInstance();
        }
        return _instance;
    }

    // ===================================================================================
    //                                                                  Generator's Method
    //                                                                  ==================
    public abstract String getOutputPath();

    public abstract void setOutputPath(String outputPath);

    public abstract void setInputEncoding(String inputEncoding);

    public abstract void setOutputEncoding(String outputEncoding);

    public abstract void setTemplatePath(String templatePath);

    public abstract String parse(String inputTemplate, String outputFile, String objectID, Object object)
            throws Exception;

    public abstract String parse(String controlTemplate, Context controlContext) throws Exception;

    public abstract void shutdown();

    // ===================================================================================
    //                                                                    Skip Information
    //                                                                    ================
    public abstract List<String> getParseFileNameList();

    public abstract List<String> getSkipFileNameList();

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return getInstance().toString();
    }
}
