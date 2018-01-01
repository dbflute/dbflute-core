/*
 * Copyright 2014-2018 the original author or authors.
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.dbflute.infra.doc.decomment.parts.DfDecoMapColumnPart;
import org.dbflute.infra.doc.decomment.parts.DfDecoMapPropertyPart;
import org.dbflute.infra.doc.decomment.parts.DfDecoMapTablePart;
import org.dbflute.optional.OptionalThing;
import org.dbflute.unit.RuntimeTestCase;
import org.dbflute.util.DfTypeUtil;

/**
 * @author hakiba
 * @author cabos
 * @author jflute
 */
public class DfDecoMapFileTest extends RuntimeTestCase {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final String TEST_RESOURCES_PATH = "/src/test/resources";

    // ===================================================================================
    //                                                                               Read
    //                                                                              ======
    public void test_readPickup() throws Exception {
        // ## Arrange ##
        final DfDecoMapFile decoMapFile = new DfDecoMapFile();

        // ## Act ##
        OptionalThing<DfDecoMapPickup> optPickup = decoMapFile.readPickup(buildTestResourcePath());

        // ## Assert ##
        assertTrue(optPickup.isPresent());
        DfDecoMapPickup pickup = optPickup.get();

        assertEquals(pickup.getFormatVersion(), "1.0");
        assertEquals(pickup.getPickupDatetime(), DfTypeUtil.toLocalDateTime("2017-11-09T09:09:09.009"));
        {
            DfDecoMapTablePart member = extractPickupTableAsOne(pickup, "MEMBER");
            assertEquals(0, member.getPropertyList().size());
            assertEquals(1, member.getColumnList().size());
            {
                DfDecoMapColumnPart memberName = extractPickupColumnAsOne(member, "MEMBER_NAME");
                assertEquals(1, memberName.getPropertyList().size());
                assertEquals("cabos", memberName.getPropertyList().get(0).getPieceOwner());
            }
        }
        {
            DfDecoMapTablePart login = extractPickupTableAsOne(pickup, "MEMBER_LOGIN");
            assertEquals(1, login.getPropertyList().size());
            assertEquals("cabos", login.getPropertyList().get(0).getPieceOwner());
            assertEquals(2, login.getColumnList().size());
            {
                DfDecoMapColumnPart memberName = extractPickupColumnAsOne(login, "LOGIN_DATETIME");
                assertEquals(1, memberName.getPropertyList().size());
                assertEquals("hakiba", memberName.getPropertyList().get(0).getPieceOwner());
            }
            {
                DfDecoMapColumnPart memberName = extractPickupColumnAsOne(login, "LOGIN_MEMBER_STATUS_CODE");
                assertEquals(2, memberName.getPropertyList().size());
                assertEquals("deco", memberName.getPropertyList().get(0).getPieceOwner());
            }
        }
    }

    // ===================================================================================
    //                                                                               Merge
    //                                                                               =====
    public void test_merge_OnlyColumnComment() throws Exception {
        // ## Arrange ##
        final DfDecoMapFile decoMapFile = new DfDecoMapFile();
        final OptionalThing<DfDecoMapPickup> optPickup = OptionalThing.empty(); // not exists pickup
        final DfDecoMapPiece piece1 = preparePieceColumn("MEMBER", "MEMBER_NAME", "hakiba", Collections.emptyList(), "SAMPLEPI");
        final DfDecoMapPiece piece2 = preparePieceColumn("MEMBER", "MEMBER_STATUS", "cabos", Collections.emptyList(), "ECECODED");
        final DfDecoMapPiece piece3 = preparePieceColumn("PURCHASE", "PURCHASE_PRODUCT", "cabos", Collections.emptyList(), "AYOOOOO");
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

    public void test_merge_noConflict() throws Exception {
        // ## Arrange ##
        final DfDecoMapFile decoMapFile = new DfDecoMapFile();
        final OptionalThing<DfDecoMapPickup> optPickup = OptionalThing.empty(); // not exists pickup
        final String tableName = "MEMBER";
        final String columnName = "MEMBER_NAME";
        final DfDecoMapPiece piece1 = preparePieceColumn(tableName, columnName, "deco", Collections.emptyList(), "SAMPLEPI",
                Arrays.asList("SAVEACTI", "ON00000"));
        final DfDecoMapPiece piece2 = preparePieceColumn(tableName, columnName, "hakiba", Collections.singletonList("deco"), "ECECODED",
                Collections.singletonList("SAMPLEPI"));
        final DfDecoMapPiece piece3 = preparePieceColumn(tableName, columnName, "cabos", Arrays.asList("deco", "hakiba"), "AYOOOOO",
                Collections.singletonList("ECECODED"));
        final DfDecoMapPiece piece4 =
                preparePieceColumn(tableName, columnName, "jflute", Arrays.asList("deco", "hakiba", "cabos"), "SLEEPY00",
                        Collections.singletonList("AYOOOOO"));
        final List<DfDecoMapPiece> pieceList = Arrays.asList(piece1, piece2, piece3, piece4);

        // ## Act ##
        final DfDecoMapPickup result = decoMapFile.merge(optPickup, pieceList);

        // ## Assert ##
        assertNotNull(result);
        log(result);

        // survive piece4 only
        {
            DfDecoMapTablePart member = extractPickupTableAsOne(result, tableName);
            assertEquals(0, member.getPropertyList().size());
            assertEquals(1, member.getColumnList().size());
            {
                DfDecoMapColumnPart memberName = extractPickupColumnAsOne(member, columnName);
                assertEquals(1, memberName.getPropertyList().size());
                {
                    DfDecoMapPropertyPart property = memberName.getPropertyList().get(0);
                    assertEquals("jflute", property.getPieceOwner());
                    assertEquals("SLEEPY00", property.getPieceCode());
                    assertEquals(1, property.getPreviousPieceList().size());
                    assertEquals("AYOOOOO", property.getPreviousPieceList().get(0));
                    assertEquals(property.getAuthorList().size(), 4);
                    assertTrue(property.getAuthorList().containsAll(Arrays.asList("deco", "hakiba", "cabos", "jflute")));
                }
            }
        }
    }

    public void test_merge_conflict() throws Exception {
        // ## Arrange ##
        final DfDecoMapFile decoMapFile = new DfDecoMapFile();
        final OptionalThing<DfDecoMapPickup> optPickup = OptionalThing.empty(); // not exists pickup
        final String tableName = "MEMBER";
        final String columnName = "MEMBER_NAME";
        final DfDecoMapPiece piece1 = preparePieceColumn(tableName, columnName, "deco", Collections.emptyList(), "SAMPLEPI",
                Arrays.asList("SAVEACTI", "ON00000"));
        final DfDecoMapPiece piece2 = preparePieceColumn(tableName, columnName, "hakiba", Collections.singletonList("deco"), "ECECODED",
                Collections.singletonList("SAMPLEPI"));
        final DfDecoMapPiece piece3 = preparePieceColumn(tableName, columnName, "hakiba", Arrays.asList("deco", "hakiba"), "SAMPLE01",
                Collections.singletonList("ECECODED"));
        final DfDecoMapPiece piece4 = preparePieceColumn(tableName, columnName, "cabos", Collections.emptyList(), "AYOOOOOO",
                Collections.singletonList("SAMPLEPI"));
        final DfDecoMapPiece piece5 = preparePieceColumn(tableName, columnName, "cabos", Arrays.asList("deco", "cabos"), "SAMPLE02",
                Collections.singletonList("AYOOOOOO"));
        final List<DfDecoMapPiece> pieceList = Arrays.asList(piece1, piece2, piece3, piece4, piece5);

        // ## Act ##
        final DfDecoMapPickup result = decoMapFile.merge(optPickup, pieceList);

        // ## Assert ##
        assertNotNull(result);
        log(result);

        // survive piece3 and piece5
        {
            DfDecoMapTablePart member = extractPickupTableAsOne(result, tableName);
            assertEquals(0, member.getPropertyList().size());
            assertEquals(1, member.getColumnList().size());
            {
                DfDecoMapColumnPart memberName = extractPickupColumnAsOne(member, columnName);
                assertEquals(2, memberName.getPropertyList().size());
                {
                    DfDecoMapPropertyPart property = memberName.getPropertyList().get(0);
                    assertEquals("hakiba", property.getPieceOwner());
                    assertEquals("SAMPLE01", property.getPieceCode());
                    assertEquals(1, property.getPreviousPieceList().size());
                    assertEquals("ECECODED", property.getPreviousPieceList().get(0));
                    assertEquals(2, property.getAuthorList().size());
                    assertTrue(property.getAuthorList().containsAll(Arrays.asList("deco", "hakiba")));
                }
                {
                    DfDecoMapPropertyPart property = memberName.getPropertyList().get(1);
                    assertEquals("cabos", property.getPieceOwner());
                    assertEquals("SAMPLE02", property.getPieceCode());
                    assertEquals(1, property.getPreviousPieceList().size());
                    assertEquals("AYOOOOOO", property.getPreviousPieceList().get(0));
                    assertEquals(2, property.getAuthorList().size());
                    assertTrue(property.getAuthorList().containsAll(Arrays.asList("deco", "cabos")));
                }

            }
        }
    }

    public void test_merge_OverwritePickupByPiece() {
        // ## Arrange ##
        final DfDecoMapFile decoMapFile = new DfDecoMapFile();
        final String tableName = "MEMBER_LOGIN";
        final String pieceCode = "NEWPIECE";
        final String author = "borderman";
        final OptionalThing<DfDecoMapPickup> optPickup = decoMapFile.readPickup(buildTestResourcePath());
        final DfDecoMapTablePart table = extractPickupTableAsOne(optPickup.get(), tableName);
        final List<String> pieceCodeList =
                table.getPropertyList().stream().flatMap(property -> property.getPreviousPieceList().stream()).collect(Collectors.toList());
        pieceCodeList.addAll(table.getPropertyList().stream().map(property -> property.getPieceCode()).collect(Collectors.toList()));
        final DfDecoMapPiece piece = preparePieceTable(tableName, author, Collections.emptyList(), pieceCode, pieceCodeList);

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
        final List<DfDecoMapPiece> pieceList = decoMapFile.readPieceList(buildTestResourcePath());
        final OptionalThing<DfDecoMapPickup> optPickUp = decoMapFile.readPickup(buildTestResourcePath());

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
        DfDecoMapPiece piece = preparePieceTable(sampleTableName, sampleAuthor, Collections.emptyList(), samplePieceCode);
        DfDecoMapFile decoMapFile = new DfDecoMapFile() {
            @Override
            protected String getCurrentDateStr() {
                return currentDateStr;
            }
        };

        // e.g decomment-piece-TABLE_NAME-20170316-123456-789-authorName.dfmap
        final String expFileName =
                "decomment-piece-" + sampleTableName + "-" + currentDateStr + "-" + sampleAuthor + "-" + samplePieceCode + ".dfmap";

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
        DfDecoMapPiece piece =
                preparePieceColumn(sampleTableName, sampleColumnName, sampleAuthor, Collections.emptyList(), samplePieceCode);
        DfDecoMapFile decoMapFile = new DfDecoMapFile() {
            @Override
            protected String getCurrentDateStr() {
                return currentDateStr;
            }
        };

        // e.g decomment-piece-TABLE_NAME-20170316-123456-789-authorName.dfmap
        final String expFileName =
                "decomment-piece-" + sampleTableName + "-" + sampleColumnName + "-" + currentDateStr + "-" + sampleAuthor + "-"
                        + samplePieceCode + ".dfmap";

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
    private DfDecoMapPiece preparePieceColumn(String tableName, String columnName, String author, List<String> authorList,
            String pieceCode) {
        return preparePieceColumn(tableName, columnName, author, authorList, pieceCode, Collections.emptyList());
    }

    private DfDecoMapPiece preparePieceColumn(String tableName, String columnName, String author, List<String> authorList, String pieceCode,
            List<String> previousPieceList) {
        return doPreparePiece(DfDecoMapPieceTargetType.Column, tableName, columnName, author, authorList, pieceCode, previousPieceList);
    }

    private DfDecoMapPiece preparePieceTable(String tableName, String author, List<String> authorList, String pieceCode) {
        return doPreparePiece(DfDecoMapPieceTargetType.Table, tableName, null, author, authorList, pieceCode, Collections.emptyList());
    }

    private DfDecoMapPiece preparePieceTable(String tableName, String author, List<String> authorList, String pieceCode,
            List<String> previousPieceList) {
        return doPreparePiece(DfDecoMapPieceTargetType.Table, tableName, null, author, authorList, pieceCode, previousPieceList);
    }

    private DfDecoMapPiece doPreparePiece(DfDecoMapPieceTargetType type, String tableName, String columnName, String author,
            List<String> authorList, String pieceCode, List<String> previousPieceList) {
        return new DfDecoMapPiece("1.0", tableName, columnName, type, "decomment",
                "decomment does't mean database comment, means deco chan comment", 1L, authorList, pieceCode, currentLocalDateTime(),
                author, previousPieceList);
    }

    private DfDecoMapTablePart extractPickupTableAsOne(DfDecoMapPickup pickup, String tableName) {
        List<DfDecoMapTablePart> tableList = pickup.getTableList().stream().filter(table -> {
            return table.getTableName().equals(tableName);
        }).collect(Collectors.toList());
        assertHasOnlyOneElement(tableList);
        return tableList.get(0);
    }

    private DfDecoMapColumnPart extractPickupColumnAsOne(DfDecoMapTablePart table, String columnName) {
        List<DfDecoMapColumnPart> columnList =
                table.getColumnList().stream().filter(column -> column.getColumnName().equals(columnName)).collect(Collectors.toList());
        assertHasOnlyOneElement(columnList);
        return columnList.get(0);
    }

    private String buildTestResourcePath() {
        return getProjectDir().getPath() + TEST_RESOURCES_PATH;
    }
}
