/*
 * Copyright 2014-2017 the original author or authors.
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.dbflute.optional.OptionalThing;
import org.dbflute.unit.RuntimeTestCase;

import static org.dbflute.system.DBFluteSystem.currentLocalDateTime;

/**
 * @author hakiba
 * @author cabos
 */
public class DfDecoMapFileTest extends RuntimeTestCase {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Long LATEST_COMMENT_VERSION = 3L;

    // ===================================================================================
    //                                                                               Merge
    //                                                                               =====
    public void test_mergeAllColumnComment() throws Exception {
        // ## Arrange ##
        DfDecoMapFile decoMapFile = new DfDecoMapFile();
        OptionalThing<DfDecoMapPickup> optPickup = OptionalThing.empty(); // not exists pickup
        LocalDateTime now = currentLocalDateTime();
        DfDecoMapPiece piece1 = preparePiece("MEMBER", "MEMBER_NAME", "hakiba", LATEST_COMMENT_VERSION, now);
        DfDecoMapPiece piece2 = preparePiece("MEMBER", "MEMBER_STATUS", "cabos", LATEST_COMMENT_VERSION, now);
        DfDecoMapPiece piece3 = preparePiece("PURCHASE", "PURCHASE_PRODUCT", "cabos", LATEST_COMMENT_VERSION, now);
        List<DfDecoMapPiece> pieceList = Arrays.asList(piece1, piece2, piece3);

        // ## Act ##
        DfDecoMapPickup result = decoMapFile.merge(optPickup, pieceList);

        // ## Assert ##
        assertNotNull(result);
        log(result);
        // assert all table and column
        long columnCount = result.getTableList()
                .stream()
                .map(tablePart -> tablePart.getColumnList())
                .flatMap(columnParts -> columnParts.stream())
                .count();
        assertEquals(3, columnCount);
    }

    private DfDecoMapPiece preparePiece(String tableName, String columnName, String author, long commentVersion,
                                        LocalDateTime decommentDateTime) {
        DfDecoMapPiece piece = new DfDecoMapPiece();
        piece.setFormatVersion("1.0");
        piece.setTableName(tableName);
        piece.setColumnName(columnName);
        piece.setTargetType(DfDecoMapPieceTargetType.Column);
        piece.setDecomment("decomment");
        piece.setDatabaseComment("databasecomment");
        piece.setCommentVersion(commentVersion);
        piece.addAllAuthors(Collections.singletonList(author));
        piece.setPieceCode("DE000000");
        piece.setPieceDatetime(decommentDateTime);
        piece.setPieceOwner(author);
        piece.addAllPreviousPieces(Collections.emptyList());
        return piece;
    }

    // ===================================================================================
    //                                                                           file name
    //                                                                           =========
    public void test_buildPieceFileName() throws Exception {
        // ## Arrange ##
        final String sampleTableName = "EBISU_GARDEN_PLACE";
        final String sampleColumnName = "PLAZA";
        final String sampleAuthor = "cabos";
        final String samplePieceCode = "FE893L1";
        final String currentDateStr = "2999/12/31";
        DfDecoMapFile decoMapFile = new DfDecoMapFile() {
            @Override
            protected String getCurrentDateStr() {
                return currentDateStr;
            }
        };

        // e.g decomment-piece-TABLE_NAME-20170316-123456-789-authorName.dfmap
        final String expFileName = "decomment-piece-" + sampleTableName + "-" + sampleColumnName + "-" + currentDateStr + "-" + sampleAuthor
                + "-" + samplePieceCode + ".dfmap";

        // ## Act ##
        final String fileName = decoMapFile.buildPieceFileName(sampleTableName, sampleColumnName, sampleAuthor, samplePieceCode);

        // ## Assert ##
        assertEquals(fileName, expFileName);
    }

    public void test_getCurrentDateStr() throws Exception {
        // ## Arrange ##
        final LocalDateTime currentDate = currentLocalDateTime();
        DfDecoMapFile decoMapFile = new DfDecoMapFile() {

            @Override
            protected LocalDateTime getCurrentLocalDateTime() {
                return currentDate;
            }
        };

        // e.g 20170316-123456-789
        String expDatePattern = "yyyyMMdd-HHmmss-SSS";
        final String expDateStr = DateTimeFormatter.ofPattern(expDatePattern).format(currentDate);

        // ## Act ##
        final String dateStr = decoMapFile.getCurrentDateStr();

        // ## Assert ##
        assertEquals(dateStr, expDateStr);
    }
}
