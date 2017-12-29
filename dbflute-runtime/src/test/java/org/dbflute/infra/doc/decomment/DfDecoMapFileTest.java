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

import org.dbflute.infra.doc.decomment.parts.DfDecoMapColumnPart;
import org.dbflute.infra.doc.decomment.parts.DfDecoMapPropertyPart;
import org.dbflute.infra.doc.decomment.parts.DfDecoMapTablePart;
import org.dbflute.optional.OptionalThing;
import org.dbflute.unit.RuntimeTestCase;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.dbflute.system.DBFluteSystem.currentLocalDateTime;

/**
 * @author hakiba
 * @author cabos
 */
public class DfDecoMapFileTest extends RuntimeTestCase {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final String TEST_RESOURCES_PATH = "/src/test/resources";

    // ===================================================================================
    //                                                                               Merge
    //                                                                               =====
    public void test_merge_OnlyColumnComment() throws Exception {
        // ## Arrange ##
        final DfDecoMapFile decoMapFile = new DfDecoMapFile();
        final OptionalThing<DfDecoMapPickup> optPickup = OptionalThing.empty(); // not exists pickup
        final DfDecoMapPiece piece1 = preparePiece(DfDecoMapPieceTargetType.Column,"MEMBER", "MEMBER_NAME", "hakiba", "SAMPLEPI", Collections.emptyList());
        final DfDecoMapPiece piece2 = preparePiece(DfDecoMapPieceTargetType.Column,"MEMBER", "MEMBER_STATUS", "cabos", "ECECODED", Collections.emptyList());
        final DfDecoMapPiece piece3 = preparePiece(DfDecoMapPieceTargetType.Column,"PURCHASE", "PURCHASE_PRODUCT", "cabos", "AYOOOOO", Collections.emptyList());
        final List<DfDecoMapPiece> pieceList = Arrays.asList(piece1, piece2, piece3);

        // ## Act ##
        final DfDecoMapPickup result = decoMapFile.merge(optPickup, pieceList);

        // ## Assert ##
        assertNotNull(result);
        log(result);

        {
            DfDecoMapTablePart member = extractPickupTableAsOne(result, "MEMBER");
            assertEquals(0, member.getPropertyList().size());
            assertEquals(2, member.getColumnList().size());
            {
                DfDecoMapColumnPart memberName = extractPickupColumnAsOne(member, "MEMBER_NAME");
                assertEquals(1, memberName.getPropertyList().size());
                assertEquals("hakiba", memberName.getPropertyList().get(0).getPieceOwner());
            }
            {
                DfDecoMapColumnPart memberStatus = extractPickupColumnAsOne(member, "MEMBER_STATUS");
                assertEquals(1, memberStatus.getPropertyList().size());
                assertEquals("cabos", memberStatus.getPropertyList().get(0).getPieceOwner());
            }
        }
        {
            DfDecoMapTablePart member = extractPickupTableAsOne(result, "PURCHASE");
            assertEquals(0, member.getPropertyList().size());
            assertEquals(1, member.getColumnList().size());
            {
                DfDecoMapColumnPart purchaseProduct = extractPickupColumnAsOne(member, "PURCHASE_PRODUCT");
                assertEquals(1, purchaseProduct.getPropertyList().size());
                assertEquals("cabos", purchaseProduct.getPropertyList().get(0).getPieceOwner());
            }
        }
    }

    public void test_merge_OverwritePickupByPiece() {
        // ## Arrange ##
        final DfDecoMapFile decoMapFile = new DfDecoMapFile();
        final String tableName = "MEMBER_LOGIN";
        final String pieceCode = "NEWPIECE";
        final String author = "borderman";
        final OptionalThing<DfDecoMapPickup> optPickup = decoMapFile.readPickup(getProjectDir().getPath() + TEST_RESOURCES_PATH);
        final DfDecoMapTablePart table = extractPickupTableAsOne(optPickup.get(), tableName);
        final List<String> pieceCodeList = table.getPropertyList().stream().flatMap(property -> property.getPreviousPieceList().stream()).collect(Collectors.toList());
        pieceCodeList.addAll(table.getPropertyList().stream().map(property -> property.getPieceCode()).collect(Collectors.toList()));
        final DfDecoMapPiece piece = preparePiece(DfDecoMapPieceTargetType.Table, tableName, null, author, pieceCode, pieceCodeList);

        // ## Act ##
        DfDecoMapPickup mergedPickUp = decoMapFile.merge(optPickup, Collections.singletonList(piece));

        // ## Assert ##
        assertNotNull(mergedPickUp);
        log(mergedPickUp);

        DfDecoMapTablePart mergedTable = extractPickupTableAsOne(optPickup.get(), tableName);
        assertNotNull(mergedTable);
        assertEquals(tableName, mergedTable.getTableName());

        List<DfDecoMapPropertyPart> mergedPropertyList = mergedTable.getPropertyList();
        assertHasAnyElement(mergedPropertyList);

        DfDecoMapPropertyPart mergedProperty = mergedPropertyList.get(0);
        assertEquals(pieceCode, mergedProperty.getPieceCode());
        assertEquals(author, mergedProperty.getPieceOwner());
    }

    public void test_merge_AllTestPiecesAndPickupAtResources() throws Exception {
        // ## Arrange ##
        final DfDecoMapFile decoMapFile = new DfDecoMapFile();
        final List<DfDecoMapPiece> pieceList = decoMapFile.readPieceList(getProjectDir().getPath() + TEST_RESOURCES_PATH);
        final OptionalThing<DfDecoMapPickup> optPickUp = decoMapFile.readPickup(getProjectDir().getPath() + TEST_RESOURCES_PATH);

        // ## Act ##
        final DfDecoMapPickup mergedPickUp = decoMapFile.merge(optPickUp, pieceList);

        // ## Assert ##
        assertNotNull(mergedPickUp);
        log(mergedPickUp);
        {
            DfDecoMapTablePart member = extractPickupTableAsOne(mergedPickUp, "MEMBER");
            assertEquals(1, member.getPropertyList().size()); // from piece
            assertEquals("deco", member.getPropertyList().get(0).getPieceOwner());
            assertEquals(3, member.getColumnList().size()); // from piece
            {
                DfDecoMapColumnPart birthdate = extractPickupColumnAsOne(member, "BIRTHDATE");
                assertHasOnlyOneElement(birthdate.getPropertyList());
                assertEquals("hakiba", birthdate.getPropertyList().get(0).getPieceOwner());
            }
        }
        {
            DfDecoMapTablePart login = extractPickupTableAsOne(mergedPickUp, "MEMBER_LOGIN");
            assertEquals(1, login.getPropertyList().size()); // from pickup
            assertEquals("cabos", login.getPropertyList().get(0).getPieceOwner());
            assertEquals(2, login.getColumnList().size()); // from pickup
            {
                DfDecoMapColumnPart loginDatetime = extractPickupColumnAsOne(login, "LOGIN_DATETIME");
                assertEquals(1, loginDatetime.getPropertyList().size()); // from pickup
                assertEquals("hakiba", loginDatetime.getPropertyList().get(0).getPieceOwner());
            }
            {
                DfDecoMapColumnPart statusCode = extractPickupColumnAsOne(login, "LOGIN_MEMBER_STATUS_CODE");
                assertEquals(2, statusCode.getPropertyList().size()); // conflict from pickup
                assertEquals("deco", statusCode.getPropertyList().get(0).getPieceOwner());
            }
        }
        {
            DfDecoMapTablePart purchase = extractPickupTableAsOne(mergedPickUp, "PURCHASE");
            assertEquals(2, purchase.getPropertyList().size()); // conflict
            assertEquals("deco", purchase.getPropertyList().get(0).getPieceOwner());
            assertEquals(1, purchase.getColumnList().size()); // conflict
            {
                DfDecoMapColumnPart purchaseDatetime = extractPickupColumnAsOne(purchase, "PURCHASE_DATETIME");
                assertEquals(3, purchaseDatetime.getPropertyList().size()); // conflict, should be cabos, hakiba, deco
            }
        }
    }

    // ===================================================================================
    //                                                                           file name
    //                                                                           =========
    public void test_buildPieceFileName_tableDecomment() throws Exception {
        // ## Arrange ##
        final String currentDateStr = "20171224-100000-123";
        final String sampleTableName = "EBISU_GARDEN_PLACE";
        final String sampleAuthor = "cabos";
        final String samplePieceCode = "FE893L1";
        DfDecoMapPiece piece = preparePiece(DfDecoMapPieceTargetType.Table, sampleTableName, null, sampleAuthor,
                samplePieceCode, Collections.emptyList());
        DfDecoMapFile decoMapFile = new DfDecoMapFile() {
            @Override
            protected String getCurrentDateStr() {
                return currentDateStr;
            }
        };

        // e.g decomment-piece-TABLE_NAME-20170316-123456-789-authorName.dfmap
        final String expFileName = "decomment-piece-" + sampleTableName + "-" + currentDateStr + "-" + sampleAuthor
                + "-" + samplePieceCode + ".dfmap";

        // ## Act ##
        final String fileName = decoMapFile.buildPieceFileName(piece);

        // ## Assert ##
        log("expFileName : {} , fileName : {}", expFileName, fileName);
        assertEquals(fileName, expFileName);
    }

    public void test_buildPieceFileName_columnDecomment() throws Exception {
        // ## Arrange ##
        final String currentDateStr = "20171224-100000-123";
        final String sampleTableName = "EBISU_GARDEN_PLACE";
        final String sampleColumnName = "PLAZA";
        final String sampleAuthor = "cabos";
        final String samplePieceCode = "FE893L1";
        DfDecoMapPiece piece = preparePiece(DfDecoMapPieceTargetType.Column, sampleTableName, sampleColumnName, sampleAuthor, samplePieceCode,
                Collections.emptyList());
        DfDecoMapFile decoMapFile = new DfDecoMapFile() {
            @Override
            protected String getCurrentDateStr() {
                return currentDateStr;
            }
        };

        // e.g decomment-piece-TABLE_NAME-20170316-123456-789-authorName.dfmap
        final String expFileName = "decomment-piece-" + sampleTableName + "-" + sampleColumnName + "-" + currentDateStr
                + "-" + sampleAuthor + "-" + samplePieceCode + ".dfmap";

        // ## Act ##
        final String fileName = decoMapFile.buildPieceFileName(piece);

        // ## Assert ##
        log("expFileName : {} , fileName : {}", expFileName, fileName);
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

    // -----------------------------------------------------
    //                                          Assist Logic
    //                                          ------------
    private DfDecoMapPiece preparePiece(DfDecoMapPieceTargetType type, String tableName, String columnName,
                                        String author, String pieceCode, List<String> previousPieceList) {
        DfDecoMapPiece piece = new DfDecoMapPiece("1.0", tableName, columnName, type, "decomment", "decomment does't mean database comment, means deco chan comment", 1L, Collections.singletonList(author), pieceCode, currentLocalDateTime(), author, previousPieceList);
        return piece;
    }

    private DfDecoMapTablePart extractPickupTableAsOne(DfDecoMapPickup pickup, String tableName) {
        List<DfDecoMapTablePart> tableList = pickup.getTableList().stream().filter(table -> {
            return table.getTableName().equals(tableName);
        }).collect(Collectors.toList());
        assertHasOnlyOneElement(tableList);
        return tableList.get(0);
    }

    private DfDecoMapColumnPart extractPickupColumnAsOne(DfDecoMapTablePart table, String columnName) {
        List<DfDecoMapColumnPart> columnList = table.getColumnList().stream()
                .filter(column -> column.getColumnName().equals(columnName)).collect(Collectors.toList());
        assertHasOnlyOneElement(columnList);
        return columnList.get(0);
    }
}
