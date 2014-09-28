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
package org.seasar.dbflute.logic.replaceschema.loaddata.impl.dataprop;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.torque.engine.database.model.Table;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.helper.mapstring.MapListString;
import org.seasar.dbflute.properties.propreader.DfOutsideMapPropReader;
import org.seasar.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 * @since 1.0.4B (2013/04/10 Wednesday)
 */
public class DfTableNameProp {

    protected final Map<String, Map<String, String>> _tableNameMapMap = DfCollectionUtil.newHashMap();

    public Map<String, String> getTableNameMap(String dataDirectory) {
        final Map<String, String> cachedMap = _tableNameMapMap.get(dataDirectory);
        if (cachedMap != null) {
            return cachedMap;
        }
        final DfOutsideMapPropReader reader = new DfOutsideMapPropReader();
        String path = dataDirectory + "/tableNameMap.dataprop";
        Map<String, String> resultMap = reader.readMapAsStringValue(path);
        if (resultMap == null || resultMap.isEmpty()) {
            path = dataDirectory + "/table-name.txt"; // old style
            resultMap = reader.readMapAsStringValue(path);
        }
        final StringKeyMap<String> flmap = StringKeyMap.createAsFlexible();
        flmap.putAll(resultMap);
        _tableNameMapMap.put(dataDirectory, flmap);
        return _tableNameMapMap.get(dataDirectory);
    }

    public void outputTableNameMap(String baseDir, Map<String, Table> tableNameMap) {
        final Map<String, String> map = new LinkedHashMap<String, String>();
        for (Entry<String, Table> entry : tableNameMap.entrySet()) {
            final String sheetName = entry.getKey();
            final Table table = entry.getValue();
            map.put(sheetName, table.getTableSqlName());
        }
        final String mapString = new MapListString().buildMapString(map);
        final File dataPropFile = new File(baseDir + "/tableNameMap.dataprop");
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dataPropFile), "UTF-8"));
            bw.write(mapString);
            bw.flush();
        } catch (IOException e) {
            String msg = "Failed to write tableNameMap.dataprop: " + dataPropFile;
            throw new IllegalStateException(msg, e);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
