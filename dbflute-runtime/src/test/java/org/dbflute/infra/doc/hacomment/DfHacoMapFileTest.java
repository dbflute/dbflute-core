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
package org.dbflute.infra.doc.hacomment;

import org.dbflute.optional.OptionalThing;
import org.dbflute.unit.RuntimeTestCase;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author shiny
 */
public class DfHacoMapFileTest extends RuntimeTestCase {

    public void test_merge_preserves_diffList_order() {
        // ## Arrange ##
        DfHacoMapFile hacoMapFile = new DfHacoMapFile(() -> LocalDateTime.of(2025, 10, 21, 12, 0, 0));

        DfHacoMapPickup existingPickup = new DfHacoMapPickup();
        existingPickup.setPickupDatetime(LocalDateTime.of(2025, 10, 20, 10, 0, 0));

        List<DfHacoMapDiffPart> diffPartList = new ArrayList<>();
        diffPartList.add(createDiffPart("20251015120000", "2025/10/15 12:00:00", "comment for diff 3", "author3", "PIECE003"));
        diffPartList.add(createDiffPart("20251010100000", "2025/10/10 10:00:00", "comment for diff 1", "author1", "PIECE001"));
        diffPartList.add(createDiffPart("20251012110000", "2025/10/12 11:00:00", "comment for diff 2", "author2", "PIECE002"));
        diffPartList.add(createDiffPart("20251020140000", "2025/10/20 14:00:00", "comment for diff 5", "author5", "PIECE005"));
        diffPartList.add(createDiffPart("20251018130000", "2025/10/18 13:00:00", "comment for diff 4", "author4", "PIECE004"));

        existingPickup.addAllDiffList(diffPartList);

        List<DfHacoMapPiece> emptyPieceList = new ArrayList<>();

        // ## Act ##
        DfHacoMapPickup mergedPickup = hacoMapFile.merge(OptionalThing.of(existingPickup), emptyPieceList);

        // ## Assert ##
        List<DfHacoMapDiffPart> resultDiffList = mergedPickup.getDiffList();
        assertEquals(5, resultDiffList.size());

        List<String> expectedDiffCodes = Arrays.asList(
            "20251015120000", //
            "20251010100000", //
            "20251012110000", //
            "20251020140000", //
            "20251018130000"
        );

        List<String> actualDiffCodes = new ArrayList<>();
        for (DfHacoMapDiffPart diffPart : resultDiffList) {
            actualDiffCodes.add(diffPart.getDiffCode());
        }

        log("Expected order: " + expectedDiffCodes);
        log("Actual order:   " + actualDiffCodes);

        assertEquals(expectedDiffCodes, actualDiffCodes);
    }

    private DfHacoMapDiffPart createDiffPart(String diffCode, String diffDate, String hacomment, String author, String pieceCode) {
        DfHacoMapPropertyPart propertyPart = new DfHacoMapPropertyPart(
            hacomment,              //
            null,                   //
            Arrays.asList(author),  //
            pieceCode,              //
            author,                 //
            LocalDateTime.now(),    //
            new ArrayList<>()
        );
        return new DfHacoMapDiffPart(diffCode, diffDate, propertyPart);
    }
}
