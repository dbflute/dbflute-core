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
package org.seasar.dbflute.logic.doc.lreverse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;

import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.HandyDate;
import org.seasar.dbflute.logic.replaceschema.loaddata.impl.dataprop.DfLoadingControlProp;
import org.seasar.dbflute.resource.DBFluteSystem;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfLReverseOriginDateSynchronizer {

    public String synchronizeOriginDate(String dataDir) {
        final String mapPath = dataDir + "/" + DfLoadingControlProp.LOADING_CONTROL_MAP_NAME;
        final File mapFile = new File(mapPath);
        if (!mapFile.exists()) {
            throwLoadingControlMapNotFoundException(mapFile);
        }
        final StringBuilder resultSb = new StringBuilder();
        final String mapString = prepareSynchronizedOriginDateMapString(mapFile, resultSb);
        writeMapStringToLoadingControlMap(mapFile, mapString);
        return resultSb.toString();
    }

    protected void throwLoadingControlMapNotFoundException(File mapFile) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the loading control map (so cannot synchronize).");
        br.addItem("Advice");
        br.addElement("The loadingControlMap.dataprop should exist in reversexls");
        br.addElement("if synchronization of origin date is valid for LoadDataReverse.");
        br.addItem("Map File");
        br.addElement(mapFile);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }

    protected String prepareSynchronizedOriginDateMapString(File mapFile, StringBuilder resultSb) {
        boolean prepared = false;
        final StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(mapFile), "UTF-8"));
            while (true) {
                final String line = br.readLine();
                if (line == null) {
                    break;
                }
                final boolean handled = handleOriginDateSyncLine(mapFile, sb, line, resultSb);
                if (handled) {
                    prepared = true;
                }
            }
        } catch (IOException e) {
            throwLoadingControlMapReadFailureException(mapFile, e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored) {
                }
            }
        }
        final String mapString = sb.toString();
        if (!prepared) {
            throwSynchronizedOriginDateNotFoundException(mapFile, mapString);
        }
        return mapString;
    }

    protected void throwLoadingControlMapReadFailureException(File mapFile, IOException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to read the loading control map.");
        br.addItem("Map File");
        br.addElement(mapFile);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg, e);
    }

    protected boolean handleOriginDateSyncLine(File mapFile, StringBuilder sb, String line, StringBuilder resultSb) {
        final String keyOriginDate = DfLoadingControlProp.KEY_ORIGIN_DATE;
        boolean handled = false;
        if (!line.trim().startsWith("#") && line.contains(keyOriginDate)) {
            final String frontStr = Srl.substringFirstFront(line, keyOriginDate);
            final String rearStr = Srl.substringFirstRear(line, keyOriginDate).trim();
            if (!rearStr.startsWith("=")) {
                throwLoadingControlMapOriginDateParseFailureException(mapFile, line);
            }
            final String equalRear = Srl.substringFirstRear(rearStr, "="); // keep space e.g. 2013/04/12 ...
            final String originDate = Srl.substringFirstFront(equalRear, ";", "}").trim(); // e.g. 2013/04/12
            final String lastRearStr = Srl.substringFirstRear(equalRear, originDate); // keep space e.g. "; ...", "}"
            if (originDate.trim().length() == 0) {
                throwLoadingControlMapOriginDateParseFailureException(mapFile, line);
            }
            // can be synchronized here
            final Date currentDate = DBFluteSystem.currentDate();
            final String newDateExp = new HandyDate(currentDate).toDisp("yyyy/MM/dd");
            sb.append(frontStr).append(keyOriginDate).append(" = ").append(newDateExp);
            sb.append(lastRearStr);
            handled = true;
            resultSb.append(originDate).append(" -> ").append(newDateExp);
        } else {
            sb.append(line);
        }
        sb.append("\n");
        return handled;
    }

    protected void throwLoadingControlMapOriginDateParseFailureException(File mapFile, String line) {
        final String keyOriginDate = DfLoadingControlProp.KEY_ORIGIN_DATE;
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to parse the origin date of loading control map.");
        br.addItem("Advice");
        br.addElement("If synchronization of origin date is valid for LoadDataReverse,");
        br.addElement("the origin date property setting should be like this:");
        br.addElement("(setting cannot have linefeed)");
        br.addElement("For example:");
        br.addElement("  (o): " + keyOriginDate + " = 2013/04/12");
        br.addElement("  (o): " + keyOriginDate + " = 2013/04/12 ; ...");
        br.addElement("  (o): " + keyOriginDate + " = 2013/04/12 }");
        br.addElement("  (x): " + keyOriginDate);
        br.addElement("       = 2013/04/12 // *no linefeed");
        br.addElement("  (x): " + keyOriginDate + " =");
        br.addElement("       2013/04/12 // *no linefeed");
        br.addItem("Current Line");
        br.addElement(line);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }

    protected void throwSynchronizedOriginDateNotFoundException(File mapFile, String mapString) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the origin date in loading control map (so cannot synchronize).");
        br.addItem("Advice");
        br.addElement("The origin date in the loadingControlMap.dataprop should exist");
        br.addElement("if synchronization of origin date is valid for LoadDataReverse.");
        br.addItem("Map File");
        br.addElement(mapFile);
        br.addItem("Map String");
        br.addElement(mapString);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }

    protected void writeMapStringToLoadingControlMap(File mapFile, String mapString) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mapFile), "UTF-8"));
            bw.write(mapString);
            bw.flush();
        } catch (IOException e) {
            throwLoadingControlMapWriteFailureException(mapFile, mapString, e);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    protected void throwLoadingControlMapWriteFailureException(File mapFile, String mapString, IOException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to write the loading control map.");
        br.addItem("Map File");
        br.addElement(mapFile);
        br.addItem("Map String");
        br.addElement(mapString);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg, e);
    }
}
