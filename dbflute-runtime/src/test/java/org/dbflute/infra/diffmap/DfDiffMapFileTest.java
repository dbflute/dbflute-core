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
package org.dbflute.infra.diffmap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.dbflute.unit.RuntimeTestCase;

/**
 * @author jflute
 * @since 1.2.6 (2022/05/06 Friday at roppongi japanese)
 */
public class DfDiffMapFileTest extends RuntimeTestCase {

    // ===================================================================================
    //                                                                             Collect
    //                                                                             =======
    public void test_collectDiffMap_basic() throws Exception {
        // ## Arrange ##
        DfDiffMapFile mapFile = new DfDiffMapFile();
        String pieceDirPath = getDocksideBase() + "/schemadiff";
        String monolithicMapPath = getDocksideBase() + "/mock-project-history-docksidedb.diffmap";

        // ## Act ##
        Map<String, Object> collectedMap = mapFile.collectDiffMap(pieceDirPath, file -> {
            final String pureName = file.getName();
            return pureName.startsWith("mock-diffpiece") && pureName.endsWith(".diffmap");
        }, monolithicMapPath);

        // ## Assert ##
        log(collectedMap);
        assertNotNull(collectedMap.get("2022/11/11 11:11:11.111"));
        assertNotNull(collectedMap.get("2022/05/06 15:51:55"));
        assertNotNull(collectedMap.get("2022/05/06 15:27:34.812"));
        assertNotNull(collectedMap.get("2022/03/15 03:54:02.427"));
        assertNotNull(collectedMap.get("2021/09/08 12:34:56.789"));
        assertNotNull(collectedMap.get("2019/12/30 20:03:21")); // latest monolithic
        assertNotNull(collectedMap.get("2010/06/12 16:53:57")); // first monolithic
        assertNotNull(collectedMap.get("1985/09/08 12:34:56.789")); // first piece
        Iterator<Entry<String, Object>> iterator = collectedMap.entrySet().iterator();
        assertEquals("2022/11/11 11:11:11.111", iterator.next().getKey());
        assertEquals("2022/05/06 15:51:55", iterator.next().getKey());
        assertEquals("2022/05/06 15:27:34.812", iterator.next().getKey());
        assertEquals("2022/03/15 03:54:02.427", iterator.next().getKey());
        assertEquals("2021/09/08 12:34:56.789", iterator.next().getKey());
        assertEquals("2019/12/30 20:03:21", iterator.next().getKey());
        assertEquals("1985/09/08 12:34:56.789", new LinkedList<>(collectedMap.entrySet()).getLast().getKey());
    }

    // ===================================================================================
    //                                                                               Read
    //                                                                              ======
    public void test_read_basic() throws Exception {
        // ## Arrange ##
        DfDiffMapFile mapFile = new DfDiffMapFile();
        String path = getDocksideBase() + "/mock-project-history-docksidedb.diffmap";

        // ## Act ##
        Map<String, Object> map = mapFile.readMap(new FileInputStream(new File(path)));

        // ## Assert ##
        log(map);
        assertNotNull(map.get("2019/12/30 20:03:21"));
    }

    // ===================================================================================
    //                                                                               Write
    //                                                                               =====
    public void test_write_basic() throws Exception {
        // ## Arrange ##
        DfDiffMapFile mapFile = new DfDiffMapFile();
        String path = getDocksideBase() + "/written-mock-project-history-docksidedb.diffmap";
        Map<String, Object> writtenMap = newHashMap("sea", "mystic");

        // ## Act ##
        mapFile.writeMap(new FileOutputStream(new File(path)), writtenMap);

        // ## Assert ##
        Map<String, Object> readMap = mapFile.readMap(new FileInputStream(new File(path)));
        log(readMap);
        assertEquals("mystic", readMap.get("sea"));
        new File(path).delete(); // just in case
    }

    protected String getDocksideBase() throws IOException {
        return getTestCaseBuildDir().getCanonicalPath() + "/diffmap/dockside";
    }
}
