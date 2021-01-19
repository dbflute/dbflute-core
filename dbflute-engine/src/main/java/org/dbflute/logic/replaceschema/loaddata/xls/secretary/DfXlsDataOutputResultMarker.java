/*
 * Copyright 2014-2021 the original author or authors.
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
package org.dbflute.logic.replaceschema.loaddata.xls.secretary;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.dbflute.system.DBFluteSystem;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.2.5 extracted from DfXlsDataHandlerImpl (2021/01/18 Monday at roppongi japanese)
 */
public class DfXlsDataOutputResultMarker {

    // ===================================================================================
    //                                                                               Mark
    //                                                                              ======
    public void markOutputResult(String outputDir, String outputMsg) {
        final StringBuilder sb = new StringBuilder();
        sb.append(ln()).append("* * * * * * * * * *");
        sb.append(ln()).append("*                 *");
        sb.append(ln()).append("* Xls Data Result *");
        sb.append(ln()).append("*                 *");
        sb.append(ln()).append("* * * * * * * * * *");
        sb.append(ln()).append("data-directory: ").append(outputDir);
        sb.append(ln());
        sb.append(ln()).append(Srl.ltrim(outputMsg));
        final File dataPropFile = new File(outputDir + "/xls-data-result.dfmark");
        if (dataPropFile.exists()) {
            dataPropFile.delete();
        }
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dataPropFile), "UTF-8"));
            bw.write(sb.toString());
            bw.flush();
        } catch (IOException e) {
            String msg = "Failed to write xls-data-result.dfmark: " + dataPropFile;
            throw new IllegalStateException(msg, e);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ignored) {}
            }
        }
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return DBFluteSystem.ln();
    }
}
