/*
 * Copyright 2014-2025 the original author or authors.
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
package org.dbflute.logic.doc.historyhtml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dbflute.DfBuildProperties;
import org.dbflute.helper.HandyDate;
import org.dbflute.helper.dfmap.DfMapStyle;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.infra.diffmap.DfDiffMapFile;
import org.dbflute.infra.doc.hacomment.DfHacoMapDiffPart;
import org.dbflute.infra.doc.hacomment.DfHacoMapFile;
import org.dbflute.infra.doc.hacomment.DfHacoMapPickup;
import org.dbflute.infra.doc.hacomment.DfHacoMapPiece;
import org.dbflute.logic.jdbc.schemadiff.DfSchemaDiff;
import org.dbflute.optional.OptionalThing;
import org.dbflute.properties.DfBasicProperties;
import org.dbflute.properties.DfLittleAdjustmentProperties;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @author hakiba
 * @since 0.9.7.1 (2010/06/07 Monday)
 */
public class DfSchemaHistory {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                        Basic Resource
    //                                        --------------
    protected final String _historyFile; // not null
    protected boolean _useDiffPiece; // @since 1.2.6

    // -----------------------------------------------------
    //                                          Load History
    //                                          ------------
    protected final List<DfSchemaDiff> _schemaDiffList = DfCollectionUtil.newArrayList();
    protected boolean _existsSchemaDiff; // status

    // -----------------------------------------------------
    //                                             Hacomment
    //                                             ---------
    // basically used by only HistoryHTML (keep null if SchemaSyncCheck)
    protected DfHacoMapPickup _hacoMapPickup; // loaded when call loadHacoMap, null allowed

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSchemaHistory(String historyFile) {
        _historyFile = historyFile;
    }

    public DfSchemaHistory useDiffPiece() {
        _useDiffPiece = true;
        return this;
    }

    // -----------------------------------------------------
    //                                          Core History
    //                                          ------------
    public static DfSchemaHistory createAsCore() { // for HistoryHTML
        final String historyFile = getProjectSchemaHistoryFile();
        final DfSchemaHistory history = new DfSchemaHistory(historyFile);
        if (determineCoreDiffPiece()) { // basically here
            history.useDiffPiece(); // use piece way since 1.2.6
        }
        return history;
    }

    protected static String getProjectSchemaHistoryFile() {
        final DfBasicProperties basicProp = DfBuildProperties.getInstance().getBasicProperties();
        return basicProp.getSchemaXmlFacadeProp().getProjectSchemaHistoryFile();
    }

    protected static boolean determineCoreDiffPiece() { // for emergency
        final DfLittleAdjustmentProperties prop = DfBuildProperties.getInstance().getLittleAdjustmentProperties();
        return !prop.isCompatibleMonolithicDiffMapHistory();
    }

    // -----------------------------------------------------
    //                                            Monolithic
    //                                            ----------
    public static DfSchemaHistory createAsMonolithic(String historyFile) { // e.g. SchemaXml(SchemaSyncCheck), AlterCheck
        return new DfSchemaHistory(historyFile);
    }

    // ===================================================================================
    //                                                                           Serialize
    //                                                                           =========
    public void serializeSchemaDiff(DfSchemaDiff schemaDiff) throws IOException {
        final Map<String, Object> serializedMap = prepareCurrentSerializedMap(schemaDiff);
        if (serializedMap.isEmpty()) { // no way, just in case
            return;
        }
        final DfDiffMapFile diffMapFile = createDiffMapFile();
        if (_useDiffPiece) {
            doSerializeByDiffPieceWay(schemaDiff, serializedMap, diffMapFile);
        } else {
            doSerializeByMonolithicWay(schemaDiff, serializedMap, diffMapFile);
        }
    }

    protected Map<String, Object> prepareCurrentSerializedMap(DfSchemaDiff schemaDiff) {
        final Map<String, Object> serializedMap = new DfMapStyle().newStringObjectMap();
        final Map<String, Object> schemaDiffMap = schemaDiff.createSchemaDiffMap();
        final String diffDate = (String) schemaDiffMap.get(DfSchemaDiff.DIFF_DATE_KEY); // as identity
        serializedMap.put(diffDate, schemaDiffMap); // key is for e.g. order
        return serializedMap;
    }

    protected void doSerializeByDiffPieceWay(DfSchemaDiff schemaDiff, Map<String, Object> serializedMap, DfDiffMapFile diffMapFile)
            throws IOException {
        final File outputFile = preparePieceOutputFile(serializedMap);
        writeSerializedMap(serializedMap, diffMapFile, outputFile);
    }

    protected void doSerializeByMonolithicWay(DfSchemaDiff schemaDiff, Map<String, Object> serializedMap, DfDiffMapFile diffMapFile)
            throws IOException {
        serializedMap.putAll(collectDiffMap(diffMapFile)); // merge
        writeSerializedMap(serializedMap, diffMapFile, new File(_historyFile));
    }

    // ===================================================================================
    //                                                                        Load History
    //                                                                        ============
    /**
     * Load existing history file as schemaDiffList. <br>
     * You can get the result from getSchemaDiffList() after loading.
     */
    public void loadHistory() { // called by e.g. DfDocumentSelector
        final Map<String, Object> diffMap = collectDiffMap(createDiffMapFile());
        if (diffMap.isEmpty()) {
            _existsSchemaDiff = false;
            return;
        }
        try {
            acceptDiffMap(diffMap);
            _existsSchemaDiff = true;
        } catch (RuntimeException e) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("Failed to accept diff-map when loading schema-diff history.");
            br.addItem("History File");
            br.addElement(_historyFile);
            final String msg = br.buildExceptionMessage();
            throw new IllegalStateException(msg, e);
        }
    }

    protected void acceptDiffMap(Map<String, Object> diffMap) {
        final Set<Entry<String, Object>> entrySet = diffMap.entrySet();
        int index = 0;
        for (Entry<String, Object> entry : entrySet) {
            final String diffDate = entry.getKey();
            final Object value = entry.getValue();
            assertDiffElementMap(diffDate, value);
            @SuppressWarnings("unchecked")
            final Map<String, Object> schemaDiffMap = (Map<String, Object>) value;
            final DfSchemaDiff schemaDiff = DfSchemaDiff.createAsHistory();
            schemaDiff.acceptSchemaDiffMap(schemaDiffMap);
            if (index == 0) {
                schemaDiff.setLatest(true);
            }
            _schemaDiffList.add(schemaDiff);
            ++index;
        }
    }

    protected void assertDiffElementMap(String key, Object value) {
        if (!(value instanceof Map<?, ?>)) { // basically no way
            throw new IllegalStateException("The elements of diff should be Map: date=" + key + " value=" + value);
        }
    }

    public boolean existsHistory() {
        return _existsSchemaDiff && !_schemaDiffList.isEmpty();
    }

    // ===================================================================================
    //                                                                           Hacomment
    //                                                                           =========
    public void loadHacoMap() { // called by e.g. DfDocumentSelector
        final String clientDirPath = ".";
        final DfHacoMapFile hacoMapFile = new DfHacoMapFile(() -> DBFluteSystem.currentLocalDateTime());
        // done hakiba add exception handling  (2018/04/28)
        try {
            final List<DfHacoMapPiece> pieceList = hacoMapFile.readPieceList(clientDirPath);
            final OptionalThing<DfHacoMapPickup> optPickup = hacoMapFile.readPickup(clientDirPath);
            _hacoMapPickup = hacoMapFile.merge(optPickup, pieceList);
        } catch (RuntimeException e) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("Failed to load haco-map.");
            br.addItem("File Path");
            br.addElement(clientDirPath);
            final String msg = br.buildExceptionMessage();
            throw new IllegalStateException(msg, e);
        }
    }

    public boolean existsHacoMapPickup() {
        return _hacoMapPickup != null && !getHacoMapDiffList().isEmpty();
    }

    // ===================================================================================
    //                                                                         DiffFile IO
    //                                                                         ===========
    // -----------------------------------------------------
    //                                            Read/Write
    //                                            ----------
    protected Map<String, Object> collectDiffMap(DfDiffMapFile diffMapFile) {
        // widely collect here in spite of useDiffPiece because... by jflute (2022/05/07)
        // 1. for when compatible option enabled after making pieces
        // 2. almost no problem if non piece task e.g. AlterCheck
        return diffMapFile.collectDiffMap(derivePieceBaseDirPath(), file -> {
            if (_useDiffPiece) {
                return Srl.isQuotedAnything(file.getName(), getPieceFilePrefix(), getPieceFileExt());
            } else { // e.g. SchemaSyncCheck
                return false; // fixedly ignore pieces
            }
        }, _historyFile); // as recent order
    }

    protected void writeSerializedMap(Map<String, Object> serializedMap, DfDiffMapFile diffMapFile, File outputFile)
            throws FileNotFoundException {
        final File outputDir = outputFile.getParentFile();
        if (!outputDir.exists()) { // possible, optional directory
            outputDir.mkdirs(); // also middle directory
        }
        diffMapFile.writeMap(new FileOutputStream(outputFile), serializedMap);
    }

    // -----------------------------------------------------
    //                                            Piece File
    //                                            ----------
    protected File preparePieceOutputFile(Map<String, Object> serializedMap) {
        final HandyDate diffDate = extractFirstDiffDate(serializedMap);
        final String pieceDirPath = derivePieceBaseDirPath();
        final String middleDirName = derivePieceMiddleDirName(diffDate);
        final String pieceFileName = derivePiecePureFileName(diffDate);
        return new File(pieceDirPath + "/" + middleDirName + "/" + pieceFileName);
    }

    protected HandyDate extractFirstDiffDate(Map<String, Object> serializedMap) {
        final Entry<String, Object> firstEntry = serializedMap.entrySet().iterator().next(); // already checked
        @SuppressWarnings("unchecked")
        final Map<String, Object> diffMap = (Map<String, Object>) firstEntry.getValue(); // always map
        final String diffDate = (String) diffMap.get(DfSchemaDiff.DIFF_DATE_KEY); // not null
        if (diffDate == null) {
            throw new IllegalStateException("Not found the diffDate on the diffMap: " + diffMap);
        }
        return new HandyDate(diffDate); // should be parsed by HandyDate
    }

    protected String derivePieceBaseDirPath() {
        // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
        // e.g.
        //  schema
        //   |-schemadiff // since 1.2.6
        //   |  |-2022
        //   |  |  |-diffpiece-maihamadb-20220315-035402-427.diffmap
        //   |  |  |-diffpiece-maihamadb-20220506-152734-812.diffmap
        //   |  |  |-diffpiece-maihamadb-20220506-155155-198.diffmap
        //   |  |-2021
        //   |  |-...
        //   |
        //   |-project-history-maihamadb.diffmap // until 1.2.5
        // _/_/_/_/_/_/_/_/_/_/
        final String schemadiffDirName = buildSchemadiffDirName();
        final String diffPieceDir;
        if (Srl.contains(_historyFile, "/")) {
            diffPieceDir = Srl.substringLastFront(_historyFile, "/") + "/" + schemadiffDirName;
        } else { // the history file is current directory
            diffPieceDir = "./" + schemadiffDirName;
        }
        return diffPieceDir;
    }

    protected String buildSchemadiffDirName() {
        final String diffKeyword = "schemadiff";
        final String pieceEnvType = getBasicProperties().getProjectSchemaHistoryDiffPieceEnvType(); // null allowed
        final String schemadiffDirName;
        if (Srl.is_NotNull_and_NotTrimmedEmpty(pieceEnvType)) { // for e.g. DBFLUTE_ENVIRONMENT_TYPE
            // schema
            //  |-schemadiff
            //  |  |-2024
            //  |  |-...
            //  |-schemadiff_prod
            //  |  |-2024
            //  |  |-...
            schemadiffDirName = diffKeyword + "_" + pieceEnvType;
        } else { // mainly here
            schemadiffDirName = diffKeyword;
        }
        return schemadiffDirName;
    }

    protected String derivePieceMiddleDirName(HandyDate diffDate) {
        // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
        // e.g. 2022 (if 2022/05/06 15:27:34.812)
        // _/_/_/_/_/_/_/_/_/_/
        return String.valueOf(diffDate.getYear());
    }

    protected String derivePiecePureFileName(HandyDate diffDate) {
        // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
        // e.g. diffpiece-maihamadb-20220506-152734-812.diffmap
        // _/_/_/_/_/_/_/_/_/_/
        final String projectName = getBasicProperties().getProjectName();
        final String timePart = diffDate.toDisp("yyyyMMdd-HHmmss-SSS"); // milliseconds since 1.2.6
        return getPieceFilePrefix() + "-" + projectName + "-" + timePart + "." + getPieceFileExt();
    }

    protected String getPieceFilePrefix() {
        // avoid search noise of Open Resoure for SchemaHTML by jflute (2022/05/07)
        //return "schemadiff-piece";
        return "diffpiece";
    }

    protected String getPieceFileExt() {
        return "diffmap";
    }

    // -----------------------------------------------------
    //                                       DiffMap Handler
    //                                       ---------------
    protected DfDiffMapFile createDiffMapFile() {
        return new DfDiffMapFile();
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBasicProperties getBasicProperties() {
        return DfBuildProperties.getInstance().getBasicProperties();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getHistoryFile() {
        return _historyFile;
    }

    public List<DfSchemaDiff> getSchemaDiffList() {
        return _schemaDiffList;
    }

    public List<DfHacoMapDiffPart> getHacoMapDiffList() { // if loaded
        // should be empty list if null, for e.g. SchemaSyncCheck (unrelated to hacomment)
        return _hacoMapPickup != null ? _hacoMapPickup.getDiffList() : DfCollectionUtil.emptyList();
    }
}
