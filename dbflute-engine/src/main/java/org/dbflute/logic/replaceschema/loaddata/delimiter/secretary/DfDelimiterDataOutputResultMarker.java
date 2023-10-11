/*
 * Copyright 2014-2023 the original author or authors.
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
package org.dbflute.logic.replaceschema.loaddata.delimiter.secretary;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Map.Entry;

import org.dbflute.logic.replaceschema.loaddata.delimiter.DfDelimiterDataResource;
import org.dbflute.logic.replaceschema.loaddata.delimiter.DfDelimiterDataResultInfo;
import org.dbflute.logic.replaceschema.loaddata.delimiter.DfDelimiterDataResultInfo.DfDelimiterDataLoadedMeta;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.2.5 extracted from DfDelimiterDataHandlerImpl (2021/01/19 Tuesday at roppongi japanese)
 */
public class DfDelimiterDataOutputResultMarker {

    // ===================================================================================
    //                                                                               Mark
    //                                                                              ======
    public void outputResultMark(DfDelimiterDataResource resource, DfDelimiterDataResultInfo resultInfo, String dataDirectory) {
        final String fileType = resource.getFileType();
        final StringBuilder sb = new StringBuilder();
        final String title = Srl.initCap(fileType); // e.g. Tsv like Xls
        sb.append(ln()).append("* * * * * * * * * *");
        sb.append(ln()).append("*                 *");
        sb.append(ln()).append("* " + title + " Data Result *");
        sb.append(ln()).append("*                 *");
        sb.append(ln()).append("* * * * * * * * * *");
        sb.append(ln()).append("data-directory: ").append(dataDirectory);
        sb.append(ln());
        final Map<String, Map<String, DfDelimiterDataLoadedMeta>> loadedMetaMap = resultInfo.getLoadedMetaMap();
        final Map<String, DfDelimiterDataLoadedMeta> elementMap = loadedMetaMap.get(dataDirectory);
        if (elementMap == null) { // e.g. no delimiter file
            return;
        }
        for (Entry<String, DfDelimiterDataLoadedMeta> entry : elementMap.entrySet()) {
            final DfDelimiterDataLoadedMeta loadedMeta = entry.getValue();
            final String delimiterFilePureName = Srl.substringLastRear(loadedMeta.getFileName(), "/");
            final Integer successRowCount = loadedMeta.getSuccessRowCount();
            sb.append(ln()).append(delimiterFilePureName).append(" (").append(successRowCount).append(")");
        }
        final String outputFilePureName = fileType.toLowerCase() + "-data-result.dfmark";
        final File dataPropFile = new File(dataDirectory + "/" + outputFilePureName);
        if (dataPropFile.exists()) {
            dataPropFile.delete();
        }
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dataPropFile), "UTF-8"));
            bw.write(sb.toString());
            bw.flush();
        } catch (IOException e) {
            String msg = "Failed to write " + outputFilePureName + ": " + dataPropFile;
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
