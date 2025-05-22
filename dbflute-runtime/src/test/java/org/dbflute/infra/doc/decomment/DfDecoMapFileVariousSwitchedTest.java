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
package org.dbflute.infra.doc.decomment;

import static org.dbflute.system.DBFluteSystem.currentLocalDateTime;

import java.util.Collections;
import java.util.List;

import org.dbflute.infra.doc.decomment.parts.DfDecoMapTablePart;
import org.dbflute.optional.OptionalThing;
import org.dbflute.unit.RuntimeTestCase;
import org.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 */
public class DfDecoMapFileVariousSwitchedTest extends RuntimeTestCase {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final String TEST_RESOURCES_PATH = "/src/test/resources";

    // ===================================================================================
    //                                                                      No Piece Merge
    //                                                                      ==============
    public void test_merge_noPiece_to_existingPickup() throws Exception {
        // ## Arrange ##
        final DfDecoMapFile decoMapFile = new DfDecoMapFile(() -> currentLocalDateTime()) {
            protected String buildPickupFilePath(String clientDirPath) {
                return clientDirPath + "/schema/decomment/pickup/various/switched202207-decomment-pickup.dfmap";
            }
        };
        OptionalThing<DfDecoMapPickup> optPickup = decoMapFile.readPickup(buildTestResourcePath());
        verifyExistingPickup(optPickup);

        // ## Act ##
        DfDecoMapPickup pickup = decoMapFile.merge(optPickup, Collections.emptyList(), Collections.emptyList());

        // ## Assert ##
        List<DfDecoMapTablePart> tableList = pickup.getTableList();
        for (DfDecoMapTablePart part : tableList) {
            log(part.getTableName());
        }

        assertEquals(4, tableList.size());
        assertEquals("MEMBER_SECURITY", tableList.get(0).getTableName());
        assertEquals("MEMBER_LOGIN", tableList.get(1).getTableName());
        assertEquals("MEMBER_FOLLOWING", tableList.get(2).getTableName()); // switched
        assertEquals("MEMBER", tableList.get(3).getTableName());
    }

    private void verifyExistingPickup(OptionalThing<DfDecoMapPickup> optPickup) {
        assertTrue(optPickup.isPresent());
        DfDecoMapPickup pickup = optPickup.get();

        assertEquals(pickup.getFormatVersion(), "1.1");
        assertEquals(pickup.getPickupDatetime(), DfTypeUtil.toLocalDateTime("2022-07-03T15:58:00.809"));

        List<DfDecoMapTablePart> tableList = pickup.getTableList();
        for (DfDecoMapTablePart part : tableList) {
            log(part.getTableName());
        }
        assertEquals(4, tableList.size());
        assertEquals("MEMBER_SECURITY", tableList.get(0).getTableName());
        assertEquals("MEMBER_LOGIN", tableList.get(1).getTableName());
        assertEquals("MEMBER_FOLLOWING", tableList.get(2).getTableName());
        assertEquals("MEMBER", tableList.get(3).getTableName());
    }

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    private String buildTestResourcePath() {
        return getProjectDir().getPath() + TEST_RESOURCES_PATH;
    }
}
