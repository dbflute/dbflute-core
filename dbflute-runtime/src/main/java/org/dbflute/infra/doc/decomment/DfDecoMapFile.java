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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.dbflute.helper.HandyDate;
import org.dbflute.helper.mapstring.MapListFile;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.infra.doc.decomment.exception.DfDecoMapFileReadFailureException;
import org.dbflute.infra.doc.decomment.exception.DfDecoMapFileWriteFailureException;
import org.dbflute.infra.doc.decomment.parts.DfDecoMapColumnPart;
import org.dbflute.infra.doc.decomment.parts.DfDecoMapPropertyPart;
import org.dbflute.infra.doc.decomment.parts.DfDecoMapTablePart;
import org.dbflute.optional.OptionalThing;
import org.dbflute.util.DfStringUtil;
import org.dbflute.util.DfTypeUtil;

// done cabos DfDecoMapFile by jflute (2017/07/27)
// done cabos add copyright in source file header like this class to classes of infra.doc.decomment by jflute (2017/11/11)

/**
 * @author cabos
 * @author hakiba
 * @author jflute
 * @author deco
 */
public class DfDecoMapFile {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    // e.g. dbflute_maihamadb/scheme/decomment/
    private static final String BASE_DECOMMENT_DIR_PATH = "/schema/decomment/";
    // e.g. dbflute_maihamadb/scheme/decomment/piece/
    private static final String BASE_PICKUP_DIR_PATH = BASE_DECOMMENT_DIR_PATH + "piece/";
    // e.g. dbflute_maihamadb/scheme/decomment/pickup/decomment-pickup.dfmap
    private static final String BASE_PIECE_FILE_PATH = BASE_DECOMMENT_DIR_PATH + "pickup/decomment-pickup.dfmap";

    private static final Map<String, String> REPLACE_CHAR_MAP;

    static {
        // done cabos add spaces and replaceChar should be underscore? by jflute (2017/09/07)
        List<String> notAvailableCharList = Arrays.asList("/", "\\", "<", ">", "*", "?", "\"", "|", ":", ";", "\0", " ");
        String replaceChar = "_";
        REPLACE_CHAR_MAP = notAvailableCharList.stream().collect(Collectors.toMap(ch -> ch, ch -> replaceChar));
    }

    // ===================================================================================
    //                                                                               Read
    //                                                                              ======
    // -----------------------------------------------------
    //                                                 Piece
    //                                                 -----
    // done yuto write e.g. (2017/11/11)
    // map:{
    //     ; formatVersion = 1.0
    //     ; tableName = MEMBER
    //     ; columnName = null
    //     ; targetType = TABLE
    //     ; decomment = loginable user, my name is deco
    //     ; databaseComment = loginable user
    //     ; commentVersion = 0
    //     ; authorList = list:{ deco }
    //     ; branchName = develop
    //     ; pieceCode = AL3OR1P
    //     ; pieceDatetime = 2017-12-31T12:34:56.789
    //     ; pieceOwner = deco
    //     ; previousPieceList = list:{}
    // }
    // map:{
    //     ; formatVersion = 1.0
    //     ; tableName = MEMBER
    //     ; columnName = MEMBER_NAME
    //     ; targetType = COLUMN
    //     ; decomment = sea mystic land oneman
    //     ; databaseComment = sea mystic
    //     ; commentVersion = 1
    //     ; authorList = list:{ cabos ; hakiba ; deco ; jflute }
    //     ; branchName = master
    //     ; pieceCode = HF7ELSE
    //     ; pieceDatetime = 2017-10-15T16:17:18.199
    //     ; pieceOwner = jflute
    //     ; previousPieceList = list:{ FE893L1 }
    // }
    // done cabos I just noticed that this should be readPieceList()... by jflute (2017/11/18)
    // done cabos write javadoc by jflute (2017/11/18)
    /**
     * Read all decomment piece map file in "clientDirPath/schema/decomment/piece/".
     * @param clientDirPath The path of DBFlute client directory (NotNull)
     * @return List of all decomment piece map (NotNull: If piece map file not exists, returns empty list)
     */
    public List<DfDecoMapPiece> readPieceList(String clientDirPath) {
        assertClientDirPath(clientDirPath);
        String pieceDirPath = buildPieceDirPath(clientDirPath);
        if (Files.notExists(Paths.get(pieceDirPath))) {
            return Collections.emptyList();
        }
        try {
            return Files.list(Paths.get(pieceDirPath)).filter(path -> path.toString().endsWith(".dfmap")).map(path -> {
                return doReadPiece(path);
            }).collect(Collectors.toList());
        } catch (IOException e) {
            throwDecoMapReadFailureException(pieceDirPath, e);
            return Collections.emptyList(); // unreachable
        }
    }

    // done cabos DBFlute uses doRead...() style for internal process so please change it by jflute (2017/11/18)
    private DfDecoMapPiece doReadPiece(Path path) {
        final MapListFile mapListFile = createMapListFile();
        try {
            Map<String, Object> map = mapListFile.readMap(Files.newInputStream(path));
            return mappingToDecoMapPiece(map);
        } catch (RuntimeException e) {
            throwDecoMapReadFailureException(path.toString(), e);
            return null; // unreachable
        } catch (IOException e) {
            throwDecoMapReadFailureException(path.toString(), e);
            return null; // unreachable
        }
    }

    // done hakiba cast check by hakiba (2017/07/29)
    private DfDecoMapPiece mappingToDecoMapPiece(Map<String, Object> map) {
        String formatVersion = (String) map.get("formatVersion");
        String tableName = (String) map.get("tableName");
        String columnName = (String) map.get("columnName");
        DfDecoMapPieceTargetType targetType = DfDecoMapPieceTargetType.of(map.get("targetType")).get();
        String decomment = (String) map.get("decomment");
        String databaseComment = (String) map.get("databaseComment");
        Long commentVersion = Long.valueOf(map.get("commentVersion").toString());
        @SuppressWarnings("unchecked")
        List<String> authorList = (List<String>) map.get("authorList");
        String branchName = (String) map.get("branchName");
        String pieceCode = (String) map.get("pieceCode");
        LocalDateTime pieceDatetime = new HandyDate((String) map.get("pieceDatetime")).getLocalDateTime();
        String pieceOwner = (String) map.get("pieceOwner");
        @SuppressWarnings("unchecked")
        List<String> previousPieceList = (List<String>) map.get("previousPieceList");
        return new DfDecoMapPiece(formatVersion, tableName, columnName, targetType, decomment, databaseComment, commentVersion, authorList,
                branchName, pieceCode, pieceDatetime, pieceOwner, previousPieceList);
    }

    // -----------------------------------------------------
    //                                                Pickup
    //                                                ------
    // map:{
    //     ; formatVersion = 1.0
    //     ; pickupDatetime = 2017-11-09T09:09:09.009
    //     ; decoMap = map:{
    //         ; tableList = list:{
    //             ; map:{
    //                 ; tableName = MEMBER
    //                 ; propertyList = list:{
    //                     ; map:{
    //                         ; decomment = first decomment
    //                         ; databaseComment = ...
    //                         ; commentVersion = ...
    //                         ; authorList = list:{ deco }
    //                         ; branchName = develop
    //                         ; pieceCode = DECO0000
    //                         ; pieceDatetime = 2017-11-05T00:38:13.645
    //                         ; pieceOwner = cabos
    //                         ; previousPieceList = list:{}
    //                     }
    //                     ; map:{ // propertyList size is more than 2 if decomment conflicts exists
    //                         ; ...
    //                     }
    //                 }
    //                 ; columnList = list:{
    //                     ; map:{
    //                         ; columnName = MEMBER_NAME
    //                         ; propertyList = list:{
    //                             ; map:{
    //                                 ; decomment = sea mystic land oneman
    //                                 ; databaseComment = sea mystic
    //                                 ; commentVersion = 1
    //                                 ; authorList = list:{ cabos, hakiba, deco, jflute }
    //                                 ; branchName = master
    //                                 ; pieceCode = HAKIBA00
    //                                 ; pieceDatetime = 2017-11-05T00:38:13.645
    //                                 ; pieceOwner = cabos
    //                                 ; previousPieceList = list:{ JFLUTE00, CABOS000 }
    //                             }
    //                         }
    //                     }
    //                     ; ... // more other columns
    //                 }
    //             }
    //             ; map:{ // Of course, other table decomment info is exists that
    //                 ; tableName = MEMBER_LOGIN
    //                 ; ...
    //             }
    //         }
    //     }
    // }
    // done hakiba sub tag comment by jflute (2017/08/17)
    /**
     * Read decomment pickup map file at "clientDirPath/schema/decomment/pickup/decomment-pickup.dfmap".
     * @param clientDirPath The path of DBFlute client directory (NotNull)
     * @return pickup decomment map (NotNull: If pickup map file not exists, returns empty)
     */
    public OptionalThing<DfDecoMapPickup> readPickup(String clientDirPath) {
        assertClientDirPath(clientDirPath);
        String filePath = buildPickupFilePath(clientDirPath);
        if (Files.notExists(Paths.get(filePath))) {
            // done hakiba null pointer so use optional thing and stream empty by jflute (2017/10/05)
            return OptionalThing.empty();
        }
        return OptionalThing.ofNullable(doReadPickup(Paths.get(filePath)), () -> {});
    }

    private DfDecoMapPickup doReadPickup(Path path) {
        MapListFile mapListFile = createMapListFile();
        try {
            Map<String, Object> map = mapListFile.readMap(Files.newInputStream(path));
            return mappingToDecoMapPickup(map);
        } catch (RuntimeException e) {
            throwDecoMapReadFailureException(path.toString(), e);
            return null; // unreachable
        } catch (IOException e) {
            throwDecoMapReadFailureException(path.toString(), e);
            return null; // unreachable
        }
    }

    private DfDecoMapPickup mappingToDecoMapPickup(Map<String, Object> map) {
        String formatVersion = (String) map.getOrDefault("formatVersion", DfDecoMapPickup.DEFAULT_FORMAT_VERSION);
        LocalDateTime pickupDatetime = DfTypeUtil.toLocalDateTime(map.get("pickupDatetime"));
        DfDecoMapPickup pickup = new DfDecoMapPickup(formatVersion);
        pickup.setPickupDatetime(pickupDatetime);

        @SuppressWarnings("unchecked")
        Map<String, List<Map<String, Object>>> decoMap =
                (Map<String, List<Map<String, Object>>>) map.getOrDefault("decoMap", new LinkedHashMap<>());
        if (decoMap.isEmpty()) {
            return pickup;
        }

        List<Map<String, Object>> tableMapList = decoMap.getOrDefault("tableList", new ArrayList<>());
        if (tableMapList.isEmpty()) {
            return pickup;
        }

        List<DfDecoMapTablePart> tableList = tableMapList.stream().map(tablePartMap -> {
            return new DfDecoMapTablePart(tablePartMap);
        }).collect(Collectors.toList());

        pickup.addAllTables(tableList);
        return pickup;
    }

    protected void throwDecoMapReadFailureException(String path, Exception cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to read the deco-map file or directory.");
        br.addItem("path");
        br.addElement(path);
        final String msg = br.buildExceptionMessage();
        throw new DfDecoMapFileReadFailureException(msg, cause);
    }

    // ===================================================================================
    //                                                                               Write
    //                                                                               =====
    // -----------------------------------------------------
    //                                                 Piece
    //                                                 -----
    /**
     * Write single decomment piece map file at "clientDirPath/schema/decomment/piece".
     * @param clientDirPath The path of DBFlute client directory (NotNull)
     * @param decoMapPiece Decoment piece map (NotNull)
     */
    public void writePiece(String clientDirPath, DfDecoMapPiece decoMapPiece) {
        assertClientDirPath(clientDirPath);
        String pieceMapPath = buildPieceDirPath(clientDirPath) + buildPieceFileName(decoMapPiece);
        // done cabos remove 'df' from variable name by jflute (2017/08/10)
        // done cabos make and throw PhysicalCabosException (application exception) see ClientNotFoundException by jflute (2017/08/10)
        doWritePiece(pieceMapPath, decoMapPiece);
    }

    /**
     * Build piece file name for piece map file<br>
     * e.g. table decomment : decomment-piece-TABLE_NAME-20171224-143000-123-owner-ABCDEFG.dfmap <br>
     * e.g. column decomment : decomment-piece-TABLE_NAME-COLUMN_NAME-20171224-143000-123-owner-ABCDEFG.dfmap <br>
     * @param decoMapPiece Decoment piece map (NotNull)
     * @return piece file name
     */
    protected String buildPieceFileName(DfDecoMapPiece decoMapPiece) {
        String tableName = decoMapPiece.getTableName();
        String columnName = decoMapPiece.getColumnName();
        String owner = decoMapPiece.getPieceOwner();
        String pieceCode = decoMapPiece.getPieceCode();
        if (decoMapPiece.getTargetType() == DfDecoMapPieceTargetType.Table) {
            // e.g. decomment-piece-MEMBER-20171015-161718-199-jflute-HF7ELSE.dfmap
            return "decomment-piece-" + tableName + "-" + getCurrentDateStr() + "-" + filterOwner(owner) + "-" + pieceCode + ".dfmap";
        } else if (decoMapPiece.getTargetType() == DfDecoMapPieceTargetType.Column) {
            // e.g. decomment-piece-MEMBER-MEMBER_NAME-20171015-161718-199-jflute-HF7ELSE.dfmap
            return "decomment-piece-" + tableName + "-" + columnName + "-" + getCurrentDateStr() + "-" + filterOwner(owner) + "-"
                    + pieceCode + ".dfmap";
        }
        throwIllegalTargetTypeException(decoMapPiece);
        return null; // unreachable
    }

    protected String filterOwner(String owner) {
        return DfStringUtil.replaceBy(owner, REPLACE_CHAR_MAP);
    }

    protected String getCurrentDateStr() {
        return DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS").format(getCurrentLocalDateTime());
    }

    protected LocalDateTime getCurrentLocalDateTime() {
        return LocalDateTime.now();
    }

    private void doWritePiece(String pieceFilePath, DfDecoMapPiece decoMapPiece) {
        File pieceMapFile = new File(pieceFilePath);
        if (pieceMapFile.exists()) { // no way, but just in case
            pieceMapFile.delete(); // simply delete old file
        }
        createPieceMapFile(pieceMapFile);

        final Map<String, Object> decoMap = decoMapPiece.convertToMap();
        final MapListFile mapListFile = createMapListFile();
        try (OutputStream ous = new FileOutputStream(pieceMapFile)) {
            try {
                mapListFile.writeMap(ous, decoMap);
            } catch (IOException e) {
                throwDecoMapWriteFailureException(pieceFilePath, decoMap, e);
            }
        } catch (IOException e) {
            throwDecoMapResourceReleaseFailureException(pieceFilePath, decoMap, e);
        }
    }

    protected void createPieceMapFile(File pieceMapFile) {
        try {
            Files.createDirectories(Paths.get(pieceMapFile.getParentFile().getAbsolutePath()));
            Files.createFile(Paths.get(pieceMapFile.getAbsolutePath()));
        } catch (IOException e) {
            throwDecoMapWriteFailureException(pieceMapFile.getPath(), e);
        }
    }

    // -----------------------------------------------------
    //                                                Pickup
    //                                                ------
    public void writePickup(String clientDirPath, DfDecoMapPickup decoMapPickup) {
        assertClientDirPath(clientDirPath);
        doWritePickup(buildPickupFilePath(clientDirPath), decoMapPickup);
    }

    protected void doWritePickup(String pickupFilePath, DfDecoMapPickup decoMapPickup) {
        File pickupMapFile = new File(pickupFilePath);
        if (pickupMapFile.exists()) { // no way, but just in case
            pickupMapFile.delete(); // simply delete old file
        }
        createPickupMapFile(pickupMapFile);

        final Map<String, Object> decoMap = decoMapPickup.convertToMap();
        final MapListFile mapListFile = createMapListFile();
        try (OutputStream ous = new FileOutputStream(pickupMapFile)) {
            try {
                mapListFile.writeMap(ous, decoMap);
            } catch (IOException e) {
                throwDecoMapWriteFailureException(pickupFilePath, decoMap, e);
            }
        } catch (IOException e) {
            throwDecoMapResourceReleaseFailureException(pickupFilePath, decoMap, e);
        }
    }

    protected void createPickupMapFile(File pickupMapFile) {
        try {
            Files.createDirectories(Paths.get(pickupMapFile.getParentFile().getAbsolutePath()));
            Files.createFile(Paths.get(pickupMapFile.getAbsolutePath()));
        } catch (IOException e) {
            throwDecoMapWriteFailureException(pickupMapFile.getPath(), e);
        }
    }

    // -----------------------------------------------------
    //                                             Exception
    //                                             ---------
    protected void throwDecoMapWriteFailureException(String path, Exception cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to create the deco-map file.");
        br.addItem("Path");
        br.addElement(path);
        final String msg = br.buildExceptionMessage();
        throw new DfDecoMapFileWriteFailureException(msg, cause);
    }

    protected void throwDecoMapWriteFailureException(String path, Map<String, Object> decoMap, Exception cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to write the deco-map file.");
        br.addItem("Path");
        br.addElement(path);
        br.addItem("DecoMap");
        br.addElement(decoMap);
        final String msg = br.buildExceptionMessage();
        throw new DfDecoMapFileWriteFailureException(msg, cause);
    }

    protected void throwDecoMapResourceReleaseFailureException(String path, Map<String, Object> decoMap, Exception cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Maybe... fail to execute \"outputStream.close()\".");
        br.addItem("Path");
        br.addElement(path);
        br.addItem("DecoMap");
        br.addElement(decoMap);
        final String msg = br.buildExceptionMessage();
        throw new DfDecoMapFileWriteFailureException(msg, cause);
    }

    protected void throwIllegalTargetTypeException(DfDecoMapPiece decoMapPiece) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Deco map piece target type is illegal");
        br.addItem("Target type");
        br.addElement(decoMapPiece.getTargetType());
        br.addItem("DecoMapPiece");
        br.addElement(decoMapPiece);
        final String msg = br.buildExceptionMessage();
        throw new IllegalArgumentException(msg);
    }

    // ===================================================================================
    //                                                                               Merge
    //                                                                               =====
    // done (by cabos) hakiba write unit test by jflute (2017/09/21)
    /**
     * merge piece map and pickup map with previous piece code clue to go on.<br>
     * <br>
     * <b>Logic:</b>
     * <ol>
     *     <li>Add PropertyList each table or column</li>
     *     <li>Filter already merged piece. <br>
     *         (If piece was already merged, Either previousPieceList(previous piece code) contains it's piece code)</li>
     * </ol>
     * @param pickupOpt Decoment pickup map (NotNull: If pickup map file not exists, Empty allowed)
     * @param pieces Decoment piece map (NotNull: If piece map file not exists, Empty allowed)
     * @return pickup decomment map (NotNull)
     */
    public DfDecoMapPickup merge(OptionalThing<DfDecoMapPickup> pickupOpt, List<DfDecoMapPiece> pieces) {
        Set<String> pieceCodeSet = extractAllMergedPieceCode(pickupOpt, pieces);
        DfDecoMapPickup pickup = pickupOpt.orElse(new DfDecoMapPickup());
        doMerge(pieces, pickup);
        filterMergedProperties(pickup, pieceCodeSet);
        return pickup;
    }

    private Set<String> extractAllMergedPieceCode(OptionalThing<DfDecoMapPickup> optPickup, List<DfDecoMapPiece> pieces) {
        Stream<String> pickupPieceCodeStream = optPickup.map(pickup -> {
            return pickup.getTableList().stream().flatMap(table -> {
                Stream<String> previousTablePieceStream =
                        table.getPropertyList().stream().flatMap(property -> property.getPreviousPieceList().stream());
                Stream<String> previousColumnPieceStream = table.getColumnList()
                        .stream()
                        .flatMap(column -> column.getPropertyList().stream())
                        .flatMap(property -> property.getPreviousPieceList().stream());
                return Stream.concat(previousTablePieceStream, previousColumnPieceStream);
            });
        }).orElse(Stream.empty());
        Stream<String> previousPieceCodeStream = pieces.stream().flatMap(piece -> piece.getPreviousPieceList().stream());
        return Stream.concat(pickupPieceCodeStream, previousPieceCodeStream).collect(Collectors.toSet());
    }

    private void filterMergedProperties(DfDecoMapPickup pickup, Set<String> pieceCodeSet) {
        pickup.getTableList().forEach(table -> {
            filterTablePropertyList(table, pieceCodeSet);
            table.getColumnList().forEach(column -> {
                filterColumnPropertyList(column, pieceCodeSet);
            });
        });
    }

    private void filterTablePropertyList(DfDecoMapTablePart table, Set<String> pieceCodeSet) {
        pieceCodeSet.forEach(pieceCode -> table.removeProperty(pieceCode));
    }

    private void filterColumnPropertyList(DfDecoMapColumnPart column, Set<String> pieceCodeSet) {
        pieceCodeSet.forEach(pieceCode -> column.removeProperty(pieceCode));
    }

    protected void doMerge(List<DfDecoMapPiece> pieces, DfDecoMapPickup pickUp) {
        pieces.forEach(piece -> {
            DfDecoMapPropertyPart property = mappingPieceToProperty(piece);

            if (piece.getTargetType() == DfDecoMapPieceTargetType.Table) { // table decomment
                List<DfDecoMapTablePart> tableList = pickUp.getTableList();

                tableList.stream().filter(table -> table.getTableName().equals(piece.getTableName())).findFirst().map(table -> {
                    // exists other table decomment
                    table.addProperty(property);
                    return table;
                }).orElseGet(() -> {
                    // not exists other table decoment
                    DfDecoMapTablePart table = new DfDecoMapTablePart(piece.getTableName());
                    table.addProperty(property);
                    pickUp.addTable(table);
                    return table;
                });

            } else if (piece.getTargetType() == DfDecoMapPieceTargetType.Column) { // column decomment
                List<DfDecoMapTablePart> tableList = pickUp.getTableList();
                tableList.stream().filter(table -> table.getTableName().equals(piece.getTableName())).findFirst().map(table -> {
                    // exists table or column decoment, but we don't know that target decomment exists now...
                    table.getColumnList()
                            .stream()
                            .filter(column -> column.getColumnName().equals(piece.getColumnName()))
                            .findFirst()
                            .map(column -> {
                                // exists column comment
                                column.addProperty(property);
                                return column;
                            })
                            .orElseGet(() -> {
                                // not exists column comment
                                DfDecoMapColumnPart column = new DfDecoMapColumnPart(piece.getColumnName());
                                column.addProperty(property);
                                table.addColumn(column);
                                return column;
                            });
                    return table;
                }).orElseGet(() -> {
                    // not exists table and column decoment
                    DfDecoMapColumnPart column = new DfDecoMapColumnPart(piece.getColumnName());
                    column.addProperty(property);

                    DfDecoMapTablePart table = new DfDecoMapTablePart(piece.getTableName());
                    table.addColumn(column);
                    pickUp.addTable(table);
                    return table;
                });
            }
        });
        pickUp.setPickupDatetime(getCurrentLocalDateTime());
    }

    private DfDecoMapPropertyPart mappingPieceToProperty(DfDecoMapPiece piece) {
        String pieceCode = piece.getPieceCode();
        return new DfDecoMapPropertyPart(piece.getDecomment(), piece.getDatabaseComment(), pieceCode, piece.getPieceDatetime(),
                piece.getPieceOwner(), piece.getPreviousPieceList(), piece.getCommentVersion(), piece.getAuthorList(), piece.getBranchName());
    }

    // ===================================================================================
    //                                                                              Delete
    //                                                                              ======
    // -----------------------------------------------------
    //                                                 Piece
    //                                                 -----
    public void deletePiece(String clientPath) {
        String pieceDirPath = buildPieceDirPath(clientPath);
        doDeletePiece(pieceDirPath);
    }

    private void doDeletePiece(String piecePath) {
        File pieceDir = new File(piecePath);
        if (pieceDir.isDirectory()) {
            for (File pieceFile : pieceDir.listFiles()) {
                if (pieceFile.isFile()) {
                    pieceFile.delete();
                } else {
                    doDeletePiece(pieceFile.getAbsolutePath());
                }
            }
        }
    }

    // ===================================================================================
    //                                                                        MapList File
    //                                                                        ============
    protected MapListFile createMapListFile() {
        return new MapListFile();
    }

    // ===================================================================================
    //                                                                           File Path
    //                                                                           =========
    protected String buildPieceDirPath(String clientDirPath) {
        return clientDirPath + BASE_PICKUP_DIR_PATH;
    }

    protected String buildPickupFilePath(String clientDirPath) {
        return clientDirPath + BASE_PIECE_FILE_PATH;
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    protected void assertClientDirPath(String clientDirPath) {
        if (clientDirPath == null || clientDirPath.trim().length() == 0) {
            String msg = "The argument 'clientDirPath' should not be null or empty: " + clientDirPath;
            throw new IllegalArgumentException(msg);
        }
    }
}
