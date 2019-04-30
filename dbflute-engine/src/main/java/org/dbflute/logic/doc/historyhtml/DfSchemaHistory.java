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
package org.dbflute.logic.doc.historyhtml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dbflute.DfBuildProperties;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.infra.diffmap.DfDiffMapFile;
import org.dbflute.infra.doc.hacomment.DfHacoMapDiffPart;
import org.dbflute.infra.doc.hacomment.DfHacoMapFile;
import org.dbflute.infra.doc.hacomment.DfHacoMapPickup;
import org.dbflute.infra.doc.hacomment.DfHacoMapPiece;
import org.dbflute.logic.jdbc.schemadiff.DfSchemaDiff;
import org.dbflute.optional.OptionalThing;
import org.dbflute.properties.DfBasicProperties;
import org.dbflute.properties.facade.DfSchemaXmlFacadeProp;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.util.DfCollectionUtil;

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

    // -----------------------------------------------------
    //                                             Hacomment
    //                                             ---------
    // basically used by only HistoryHTML (keep null if SchemaSyncCheck)
    protected DfHacoMapPickup _hacoMapPickup; // loaded when call loadHacoMap, null allowed

    // -----------------------------------------------------
    //                                          Load History
    //                                          ------------
    protected final List<DfSchemaDiff> _schemaDiffList = DfCollectionUtil.newArrayList();
    protected boolean _existsSchemaDiff; // status

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSchemaHistory(String historyFile) {
        _historyFile = historyFile;
    }

    public static DfSchemaHistory createAsCore() { // pure HistoryHTML
        final DfBasicProperties basicProp = DfBuildProperties.getInstance().getBasicProperties();
        final DfSchemaXmlFacadeProp facadeProp = basicProp.getSchemaXmlFacadeProp();
        return new DfSchemaHistory(facadeProp.getProjectSchemaHistoryFile());
    }

    public static DfSchemaHistory createAsPlain(String historyFile) { // e.g. SchemaSyncCheck, AlterCheck
        return new DfSchemaHistory(historyFile);
    }

    // ===================================================================================
    //                                                                           Serialize
    //                                                                           =========
    public void serializeSchemaDiff(DfSchemaDiff schemaDiff) throws IOException {
        final String path = _historyFile;
        final DfDiffMapFile diffMapFile = createDiffMapFile();
        final File file = new File(path);

        // ordered by DIFF_DATE desc
        final Map<String, Object> serializedMap = DfCollectionUtil.newLinkedHashMap();
        final Map<String, Object> schemaDiffMap = schemaDiff.createSchemaDiffMap();
        serializedMap.put((String) schemaDiffMap.get(DfSchemaDiff.DIFF_DATE_KEY), schemaDiffMap);

        if (file.exists()) {
            FileInputStream ins = null;
            try {
                ins = new FileInputStream(file);
                final Map<String, Object> existingMap = diffMapFile.readMap(ins);
                final Set<Entry<String, Object>> entrySet = existingMap.entrySet();
                int count = 0;
                final int historyLimit = getHistoryLimit();
                final boolean historyLimitValid = historyLimit >= 0;
                for (Entry<String, Object> entry : entrySet) {
                    if (historyLimitValid && count >= historyLimit) {
                        break;
                    }
                    serializedMap.put(entry.getKey(), entry.getValue());
                    ++count;
                }
            } finally {
                if (ins != null) {
                    ins.close();
                }
            }
        } else {
            file.createNewFile();
        }

        FileOutputStream ous = null;
        try {
            ous = new FileOutputStream(path);
            diffMapFile.writeMap(ous, serializedMap);
        } finally {
            if (ous != null) {
                ous.close();
            }
        }
    }

    protected int getHistoryLimit() {
        return -1; // as default (no limit)
    }

    // ===================================================================================
    //                                                                        Load History
    //                                                                        ============
    public void loadHistory() {
        final File file = new File(_historyFile);
        if (!file.exists()) {
            _existsSchemaDiff = false;
            return;
        }
        final DfDiffMapFile diffMapFile = createDiffMapFile();
        final Map<String, Object> diffMap;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            diffMap = diffMapFile.readMap(fis);
        } catch (FileNotFoundException ignored) {
            _existsSchemaDiff = false;
            return;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ignored) {}
            }
        }
        try {
            acceptDiffMap(diffMap);
        } catch (RuntimeException e) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("Failed to accept diff-map.");
            br.addItem("File Path");
            br.addElement(_historyFile);
            br.addItem("Exception");
            br.addElement(e.getClass().getName());
            br.addElement(e.getMessage());
            final String msg = br.buildExceptionMessage();
            throw new IllegalStateException(msg, e);
        }
        _existsSchemaDiff = true;
    }

    protected void acceptDiffMap(Map<String, Object> diffMap) {
        final Set<Entry<String, Object>> entrySet = diffMap.entrySet();
        int index = 0;
        for (Entry<String, Object> entry : entrySet) {
            final String key = entry.getKey(); // diffDate
            final Object value = entry.getValue();
            assertDiffElementMap(key, value);
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
            String msg = "The elements of diff should be Map:";
            msg = msg + " date=" + key + " value=" + value;
            throw new IllegalStateException(msg);
        }
    }

    // ===================================================================================
    //                                                                        Load HacoMap
    //                                                                        ============
    public void loadHacoMap() {
        String clientDirPath = ".";
        DfHacoMapFile hacoMapFile = new DfHacoMapFile(() -> DBFluteSystem.currentLocalDateTime());
        // done hakiba add exception handling  (2018/04/28)
        try {
            List<DfHacoMapPiece> pieceList = hacoMapFile.readPieceList(clientDirPath);
            OptionalThing<DfHacoMapPickup> optPickup = hacoMapFile.readPickup(clientDirPath);
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

    // ===================================================================================
    //                                                                              Status
    //                                                                              ======
    public boolean existsHistory() {
        return _existsSchemaDiff && !_schemaDiffList.isEmpty();
    }

    public boolean existsHacoMapPickup() {
        return _hacoMapPickup != null && !getHacoMapDiffList().isEmpty();
    }

    // ===================================================================================
    //                                                                         Schema Diff
    //                                                                         ===========
    protected DfDiffMapFile createDiffMapFile() {
        return new DfDiffMapFile();
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
