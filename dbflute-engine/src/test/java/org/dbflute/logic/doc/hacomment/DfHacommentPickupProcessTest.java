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
package org.dbflute.logic.doc.hacomment;

import java.time.LocalDateTime;

import org.dbflute.infra.doc.hacomment.DfHacoMapPickup;
import org.dbflute.unit.EngineTestCase;

/**
 * @author shiny
 */
public class DfHacommentPickupProcessTest extends EngineTestCase {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    // Test resource directory specifically used to test that pickupDatetime is not updated when there are no pieces.
    private static final String TEST_RESOURCES_PATH = "/dbflute-engine/src/test/resources/hacomment/no-pieces-test";

    // ===================================================================================
    //                                                                               Basic
    //                                                                               =====
    public void test_pickupHacomment_shouldNotUpdatePickupDatetime_whenNoPieces() {
        // ## Arrange ##
        DfHacommentPickupProcess process = new DfHacommentPickupProcess();

        String clientPath = buildTestResourcePath();
        log("clientPath: " + clientPath);

        LocalDateTime expectedDatetime = LocalDateTime.of(2025, 1, 1, 12, 0, 0);

        // ## Act ##
        DfHacoMapPickup result = process.pickupHacomment(clientPath);

        // ## Assert ##
        LocalDateTime actualDatetime = result.getPickupDatetime();
        assertEquals(expectedDatetime, actualDatetime);
    }

    private String buildTestResourcePath() {
        return getProjectDir().getPath() + TEST_RESOURCES_PATH;
    }
}
