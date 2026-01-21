/*
 * Copyright 2014-2026 the original author or authors.
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
package org.dbflute.logic.doc.lreverse.origindate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;

import org.dbflute.helper.HandyDate;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.logic.replaceschema.loaddata.base.dataprop.DfLoadingControlProp;
import org.dbflute.logic.replaceschema.loaddata.base.dataprop.dateadj.DfDateAdjustmentPreparer;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.util.Srl;

/**
 * @author jflute
 */
public class DfLReverseOriginDateSynchronizer {

    protected static final String KEY_ORIGIN_DATE = DfDateAdjustmentPreparer.KEY_ORIGIN_DATE;
    protected static final String MY_ORIGIN_DATE_BEGIN = DfDateAdjustmentPreparer.MY_ORIGIN_DATE_BEGIN;
    protected static final String MY_ORIGIN_DATE_END = DfDateAdjustmentPreparer.MY_ORIGIN_DATE_END;

    // ===================================================================================
    //                                                                         Synchronize
    //                                                                         ===========
    public String synchronizeOriginDate(File dataDir) {
        final String mapPath = resolvePath(dataDir) + "/" + DfLoadingControlProp.LOADING_CONTROL_MAP_NAME;
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
        br.addElement("The loadingControlMap.dataprop should exist in reversetsv/reversexls");
        br.addElement("if synchronization of origin date is valid for LoadDataReverse.");
        br.addItem("Map File");
        br.addElement(mapFile);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }

    // ===================================================================================
    //                                                                   Prepare MapString
    //                                                                   =================
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
                } catch (IOException ignored) {}
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

    // ===================================================================================
    //                                                                     OriginDate Line
    //                                                                     ===============
    protected boolean handleOriginDateSyncLine(File mapFile, StringBuilder sb, String line, StringBuilder resultSb) {
        final String newDateExp = prepareNewOriginDateExp();
        final boolean rootOriginHandled = filterOriginDateAsRoot(mapFile, sb, line, resultSb, newDateExp);
        final boolean myOriginHandled = filterOriginDateAsMyOrigin(mapFile, sb, line, resultSb, newDateExp);
        final boolean eitherHandled = rootOriginHandled || myOriginHandled;
        if (!eitherHandled) { // both no handling
            sb.append(line).append(ln());
        }
        return eitherHandled;
    }

    // -----------------------------------------------------
    //                                    OriginDate as Root
    //                                    ------------------
    protected boolean filterOriginDateAsRoot(File mapFile, StringBuilder sb, String line, StringBuilder resultSb, String newDateExp) {
        final String keyOriginDate = KEY_ORIGIN_DATE;
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
            sb.append(frontStr).append(keyOriginDate).append(" = ").append(newDateExp);
            sb.append(lastRearStr);
            sb.append(ln());
            handled = true;
            resultSb.append("df:originDate: ").append(originDate).append(" -> ").append(newDateExp);
            resultSb.append(ln());
        }
        return handled;
    }

    protected void throwLoadingControlMapOriginDateParseFailureException(File mapFile, String line) {
        final String keyOriginDate = KEY_ORIGIN_DATE;
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to parse the origin date of loading control map.");
        br.addItem("Advice");
        br.addElement("If synchronization of origin date is valid for LoadDataReverse,");
        br.addElement("the origin date property setting should be like this:");
        br.addElement("(setting cannot have linefeed)");
        br.addElement("For example:");
        br.addElement("  (x): " + keyOriginDate);
        br.addElement("       = 2013/04/12 // *Bad: don't use linefeed");
        br.addElement("  (x): " + keyOriginDate + " =");
        br.addElement("       2013/04/12 // *Bad: don't use linefeed");
        br.addElement("  (x): " + keyOriginDate + " =     // *Bad: empty originDate");
        br.addElement("  (o): " + keyOriginDate + " = 2013/04/12");
        br.addElement("  (o): " + keyOriginDate + " = 2013/04/12 ; ...");
        br.addElement("  (o): " + keyOriginDate + " = 2013/04/12 }");
        br.addItem("Current Line");
        br.addElement(line);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }

    // -----------------------------------------------------
    //                                         My OriginDate
    //                                         -------------
    protected boolean filterOriginDateAsMyOrigin(File mapFile, StringBuilder sb, String line, StringBuilder resultSb, String newDateExp) {
        final String originDateBegin = MY_ORIGIN_DATE_BEGIN;
        final String originDateEnd = MY_ORIGIN_DATE_END;
        final StringBuilder newLineSb = new StringBuilder();
        boolean atLeastOneHandled = false;
        String remainderLine = line;
        if (!line.trim().startsWith("#")) {
            while (remainderLine.contains(originDateBegin)) {
                // zetsumyo
                final String frontStr = Srl.substringFirstFront(remainderLine, originDateBegin); // e.g. addDay($distance)
                final String rearStr = Srl.substringFirstRear(remainderLine, originDateBegin).trim(); // e.g. 2026/01/21) || 2026/01/21, ...)
                if (!rearStr.contains(originDateEnd)) {
                    throwLoadingControlMapMyOriginDateParseFailureException(mapFile, line);
                }
                final String myOriginDate = Srl.substringFirstFront(rearStr, ",", originDateEnd); // e.g. 2026/01/21
                final String lastRearStr = Srl.substringFirstRear(rearStr, myOriginDate); // e.g. , where ...) ... || ) ...
                final String lastRearBeforeEnd = Srl.substringFirstFront(lastRearStr, originDateEnd); // e.g. , where ...) || )
                if (myOriginDate.trim().length() == 0) {
                    throwLoadingControlMapOriginDateParseFailureException(mapFile, line);
                }
                // can be synchronized here
                newLineSb.append(frontStr).append(originDateBegin); // e.g. addDay($distance) df:originDate(
                newLineSb.append(newDateExp); // e.g. 2026/01/21
                newLineSb.append(lastRearBeforeEnd).append(originDateEnd); // e.g. , where ...)
                remainderLine = Srl.substringFirstRear(rearStr, originDateEnd); // may be next myOrigin
                atLeastOneHandled = true;
                resultSb.append("df:myOriginDate: ").append(myOriginDate).append(" -> ").append(newDateExp);
                resultSb.append(ln());
            }
            if (atLeastOneHandled) {
                if (!remainderLine.isEmpty()) { // comment line or myOriginDate rear
                    newLineSb.append(remainderLine);
                }
                sb.append(newLineSb);
                sb.append(ln());
            }
        }
        return atLeastOneHandled;
    }

    protected void throwLoadingControlMapMyOriginDateParseFailureException(File mapFile, String line) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to parse the myOriginDate of loading control map.");
        br.addItem("Advice");
        br.addElement("If synchronization of myOriginDate is valid for LoadDataReverse,");
        br.addElement("the origin date property setting should be like this:");
        br.addElement("(setting cannot have linefeed)");
        br.addElement("For example:");
        br.addElement("  (x): addDay($distance) df:myOriginDate(2026/01/21 // *Bad: no end");
        br.addElement("  (x): addDay($distance) df:myOriginDate(2026/01/21");
        br.addElement("                         ) // *Bad: don't use linefeed");
        br.addElement("  (x): addDay($distance) df:myOriginDate() // *Bad: no originDate");
        br.addElement("  (o): addDay($distance) df:myOriginDate(2026/01/21) // Good");
        br.addElement("  (o): addDay($distance) df:myOriginDate(2026/01/21, where ...) // Good");
        br.addItem("Current Line");
        br.addElement(line);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }

    // -----------------------------------------------------
    //                                        New OriginDate
    //                                        --------------
    protected String prepareNewOriginDateExp() {
        final Date currentDate = DBFluteSystem.currentDate();
        return new HandyDate(currentDate).toDisp("yyyy/MM/dd");
    }

    // ===================================================================================
    //                                                                           Write Map
    //                                                                           =========
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
                } catch (IOException ignored) {}
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

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String resolvePath(File file) {
        return Srl.replace(file.getPath(), "\\", "/");
    }

    protected String ln() {
        return DBFluteSystem.ln();

    }
}
