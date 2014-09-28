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
package org.seasar.dbflute.logic.doc.historyhtml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.infra.diffmap.DfDiffMapFile;
import org.seasar.dbflute.logic.jdbc.schemadiff.DfSchemaDiff;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.properties.facade.DfSchemaXmlFacadeProp;
import org.seasar.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 * @since 0.9.7.1 (2010/06/07 Monday)
 */
public class DfSchemaHistory {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                        Basic Resource
    //                                        --------------
    protected final String _historyFile;

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
                } catch (IOException ignored) {
                }
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
    //                                                                              Status
    //                                                                              ======
    public boolean existsHistory() {
        return _existsSchemaDiff && !_schemaDiffList.isEmpty();
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
}
