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

package org.dbflute.infra.doc.hacomment;

import org.dbflute.helper.HandyDate;
import org.dbflute.helper.dfmap.DfMapFile;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.infra.doc.hacomment.exception.DfHacoMapFileReadFailureException;
import org.dbflute.infra.doc.hacomment.exception.DfHacoMapFileWriteFailureException;
import org.dbflute.optional.OptionalThing;
import org.dbflute.util.DfStringUtil;
import org.dbflute.util.DfTypeUtil;

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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author hakiba
 */
public class DfHacoMapFile {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    // e.g. dbflute_maihamadb/scheme/decomment/
    private static final String BASE_HACOMMENT_DIR_PATH = "/schema/hacomment/";
    // e.g. dbflute_maihamadb/scheme/decomment/piece/
    private static final String BASE_PIECE_DIR_PATH = BASE_HACOMMENT_DIR_PATH + "piece/";
    // e.g. dbflute_maihamadb/scheme/decomment/pickup/decomment-pickup.dfmap
    private static final String BASE_PICKUP_FILE_PATH = BASE_HACOMMENT_DIR_PATH + "pickup/hacomment-pickup.dfmap";

    private static final Map<String, String> REPLACE_CHAR_MAP;

    static {
        // done cabos add spaces and replaceChar should be underscore? by jflute (2017/09/07)
        List<String> notAvailableCharList = Arrays.asList("/", "\\", "<", ">", "*", "?", "\"", "|", ":", ";", "\0", " ");
        String replaceChar = "_";
        REPLACE_CHAR_MAP = notAvailableCharList.stream().collect(Collectors.toMap(ch -> ch, ch -> replaceChar));
    }

    private static final Map<String, String> REPLACE_MAP_FOR_HACOMMENT_ID =
            Stream.of("/", " ", ":").collect(Collectors.toMap(ch -> ch, ch -> ""));

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    private final Supplier<LocalDateTime> currentDatetimeSupplier;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfHacoMapFile(Supplier<LocalDateTime> currentDatetimeSupplier) {
        this.currentDatetimeSupplier = currentDatetimeSupplier;
    }

    // ===================================================================================
    //                                                                                Read
    //                                                                                ====

    // -----------------------------------------------------
    //                                                 Piece
    //                                                 -----
    /**
     * Read all hacomment piece map file in "clientDirPath/schema/hacomment/piece/".
     * @param clientDirPath The path of DBFlute client directory (NotNull)
     * @return List of all hacomment piece map (NotNull: If piece map file not exists, returns empty list)
     * @see DfHacoMapPiece#convertToMap()
     */
    public List<DfHacoMapPiece> readPieceList(String clientDirPath) {
        assertClientDirPath(clientDirPath);
        String pieceDirPath = buildPieceDirPath(clientDirPath);
        if (Files.notExists(Paths.get(pieceDirPath))) {
            return Collections.emptyList();
        }
        try {
            return Files.list(Paths.get(pieceDirPath))
                    .filter(path -> path.toString().endsWith(".dfmap"))
                    .filter(path -> path.toString().contains("-piece-"))
                    .map(path -> doReadPiece(path))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throwHacomMapReadFailureException(pieceDirPath, e);
            return Collections.emptyList();
        }
    }

    private DfHacoMapPiece doReadPiece(Path path) {
        final DfMapFile mapFile = new DfMapFile();
        try {
            Map<String, Object> map = mapFile.readMap(Files.newInputStream(path));
            return mappingToDecoMapPiece(map);
        } catch (RuntimeException | IOException e) {
            throwHacomMapReadFailureException(path.toString(), e);
            return null; // unreachable
        }
    }

    // done hakiba cast check by hakiba (2017/07/29)
    private DfHacoMapPiece mappingToDecoMapPiece(Map<String, Object> map) {
        String diffCode = (String) map.get("diffCode");
        String diffdate = (String) map.get("diffDate");
        String hacomment = (String) map.get("hacomment");
        String diffComment = (String) map.get("diffComment");
        @SuppressWarnings("unchecked")
        List<String> authorList = (List<String>) map.get("authorList");
        String pieceCode = (String) map.get("pieceCode");
        LocalDateTime pieceDatetime = new HandyDate((String) map.get("pieceDatetime")).getLocalDateTime();
        String pieceOwner = (String) map.get("pieceOwner");
        @SuppressWarnings("unchecked")
        List<String> previousPieceList = (List<String>) map.get("previousPieceList");
        return new DfHacoMapPiece(diffCode, diffdate, hacomment, diffComment, authorList, pieceCode, pieceOwner, pieceDatetime,
                previousPieceList);
    }
    // -----------------------------------------------------
    //                                                Pickup
    //                                                ------
    /**
     * Read hacomment pickup map file at "clientDirPath/schema/hacomment/pickup/hacomment-pickup.dfmap".
     * @param clientDirPath The path of DBFlute client directory (NotNull)
     * @return pickup hacomment map (NotNull: If pickup map file not exists, returns empty)
     * @see DfHacoMapPickup#convertToMap()
     */
    public OptionalThing<DfHacoMapPickup> readPickup(String clientDirPath) {
        assertClientDirPath(clientDirPath);
        String filePath = buildPickupFilePath(clientDirPath);
        if (Files.notExists(Paths.get(filePath)))
            return OptionalThing.empty();
        return OptionalThing.ofNullable(doReadPickup(Paths.get(filePath)), () -> {});
    }

    private DfHacoMapPickup doReadPickup(Path path) {
        DfMapFile mapFile = new DfMapFile();
        try {
            Map<String, Object> map = mapFile.readMap(Files.newInputStream(path));
            return mappingToHacoMapPickup(map);
        } catch (RuntimeException | IOException e) {
            throwHacomMapReadFailureException(path.toString(), e);
            return null; // unreachable
        }
    }

    private DfHacoMapPickup mappingToHacoMapPickup(Map<String, Object> map) {
        LocalDateTime pickupDatetime = DfTypeUtil.toLocalDateTime(map.get("pickupDatetime"));
        String formatVersion = (String) map.get("formatVersion");
        DfHacoMapPickup pickup = new DfHacoMapPickup(formatVersion);
        pickup.setPickupDatetime(pickupDatetime);

        @SuppressWarnings("unchecked")
        Map<String, Object> hacoMap = (Map<String, Object>) map.getOrDefault("hacoMap", new ArrayList<>());
        if (hacoMap.isEmpty()) {
            return pickup;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> diffMapList = (List<Map<String, Object>>) hacoMap.getOrDefault("diffList", new ArrayList<>());

        List<DfHacoMapDiffPart> diffList = diffMapList.stream().map(diffMap -> new DfHacoMapDiffPart(diffMap)).collect(Collectors.toList());

        pickup.addAllDiffList(diffList);

        return pickup;
    }

    // -----------------------------------------------------
    //                                             Exception
    //                                             ---------
    protected void throwHacomMapReadFailureException(String path, Exception cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to read the haco-map file or directory.");
        br.addItem("path");
        br.addElement(path);
        final String msg = br.buildExceptionMessage();
        throw new DfHacoMapFileReadFailureException(msg, cause);
    }

    // ===================================================================================
    //                                                                               Write
    //                                                                               =====
    // -----------------------------------------------------
    //                                                 Piece
    //                                                 -----
    public void writePiece(String clientDirPath, DfHacoMapPiece piece) {
        assertClientDirPath(clientDirPath);

        String piecePath = buildPieceDirPath(clientDirPath) + buildPieceFileName(piece);
        doWritePiece(piecePath, piece);
    }

    private void doWritePiece(String pieceFilePath, DfHacoMapPiece piece) {
        File pieceMapFile = new File(pieceFilePath);
        if (pieceMapFile.exists()) { // no way, but just in case
            pieceMapFile.delete(); // simply delete old file
        }
        createPieceMapFile(pieceMapFile);

        Map<String, Object> hacoMap = piece.convertToMap();
        createMapFile(pieceFilePath, pieceMapFile, hacoMap);
    }

    protected void createPieceMapFile(File pieceMapFile) {
        try {
            Files.createDirectories(Paths.get(pieceMapFile.getParentFile().getAbsolutePath()));
            Files.createFile(Paths.get(pieceMapFile.getAbsolutePath()));
        } catch (IOException e) {
            throwHacoMapWriteFailureException(pieceMapFile.getPath(), e);
        }
    }

    // -----------------------------------------------------
    //                                                Pickup
    //                                                ------
    public void writePickup(String clientDirPath, DfHacoMapPickup pickup) {
        assertClientDirPath(clientDirPath);
        doWritePickup(buildPickupFilePath(clientDirPath), pickup);
    }

    protected void doWritePickup(String pickupFilePath, DfHacoMapPickup pickup) {
        File pickupMapFile = new File(pickupFilePath);
        if (pickupMapFile.exists()) { // no way, but just in case
            pickupMapFile.delete(); // simply delete old file
        }
        createPickupMapFile(pickupMapFile);

        final Map<String, Object> hacoMap = pickup.convertToMap();
        createMapFile(pickupFilePath, pickupMapFile, hacoMap);
    }

    protected void createPickupMapFile(File pickupMapFile) {
        try {
            Files.createDirectories(Paths.get(pickupMapFile.getParentFile().getAbsolutePath()));
            Files.createFile(Paths.get(pickupMapFile.getAbsolutePath()));
        } catch (IOException e) {
            throwHacoMapWriteFailureException(pickupMapFile.getPath(), e);
        }
    }

    // -----------------------------------------------------
    //                                                Common
    //                                                ------
    private void createMapFile(String mappingFilePath, File mappingMapFile, Map<String, Object> hacoMap) {
        DfMapFile mapFile = new DfMapFile();
        try (OutputStream ous = new FileOutputStream(mappingMapFile)) {
            try {
                mapFile.writeMap(ous, hacoMap);
            } catch (IOException e) {
                throwHacoMapWriteFailureException(mappingFilePath, e);
            }
        } catch (IOException e) {
            throwHacoMapResourceReleaseFailureException(mappingFilePath, hacoMap, e);
        }
    }

    /**
     * Build piece file name for piece map file<br>
     * e.g.  hacomment-piece-diffdate20180220161718-20171015-161718-199-jflute-HF7ELSE.dfmap <br>
     * @param piece Decoment piece map (NotNull)
     * @return piece file name
     */
    public String buildPieceFileName(DfHacoMapPiece piece) {
        String diffDateStr = generatePieceFileName(piece);
        String filteredOwner = DfStringUtil.replaceBy(piece.getPieceOwner(), REPLACE_CHAR_MAP);
        // e.g. hacomment-piece-diffdate20180220161718-20171015-161718-199-jflute-HF7ELSE.dfmap
        return "hacomment-piece-" + diffDateStr + "-" + getCurrentDateStr() + "-" + filteredOwner + "-" + piece.pieceCode + ".dfmap";
    }

    private String generatePieceFileName(DfHacoMapPiece piece) {
        // e.g. 2018/02/21 16:17:18 -> diffdate20180220161718
        return "diffdate" + generateDiffCode(piece.getDiffDate());
    }

    protected String getCurrentDateStr() {
        return DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS").format(getCurrentLocalDateTime());
    }

    // -----------------------------------------------------
    //                                             Exception
    //                                             ---------
    protected void throwHacoMapWriteFailureException(String path, Exception cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to create the haco-map file.");
        br.addItem("Path");
        br.addElement(path);
        final String msg = br.buildExceptionMessage();
        throw new DfHacoMapFileWriteFailureException(msg, cause);
    }

    protected void throwHacoMapResourceReleaseFailureException(String path, Map<String, Object> hacoMap, Exception cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Maybe... fail to execute \"outputStream.close()\".");
        br.addItem("Path");
        br.addElement(path);
        br.addItem("HacoMap");
        br.addElement(hacoMap);
        final String msg = br.buildExceptionMessage();
        throw new DfHacoMapFileWriteFailureException(msg, cause);
    }

    // ===================================================================================
    //                                                                               Merge
    //                                                                               =====
    /**
     * merge piece map and pickup map with previous piece code.
     *
     * @param optPickup Hacoment pickup map (NotNull: If pickup map file not exists, Empty allowed)
     * @param pieces hacoment piece maps in piece directory (NotNull: If piece map file not exists, Empty allowed)
     * @return pickup hacomment map (NotNull)
     */
    public DfHacoMapPickup merge(OptionalThing<DfHacoMapPickup> optPickup, List<DfHacoMapPiece> pieces) {
        Set<String> pieceCodeSet = extractAllMergedPieceCode(optPickup, pieces);
        DfHacoMapPickup mergedPickup = doMerge(optPickup, pieces, pieceCodeSet);
        return mergedPickup;
    }

    private Set<String> extractAllMergedPieceCode(OptionalThing<DfHacoMapPickup> pickupOpt, List<DfHacoMapPiece> pieces) {
        Stream<String> pickupPieceCodeStream = pickupOpt.map(pickup -> pickup.getDiffList().stream().flatMap(diffPart -> {
            return diffPart.getPropertyList().stream().flatMap(property -> property.getPreviousPieceList().stream());
        })).orElse(Stream.empty());
        Stream<String> previousPieceCodeStream = pieces.stream().flatMap(piece -> piece.previousPieceList.stream());
        return Stream.concat(pickupPieceCodeStream, previousPieceCodeStream).collect(Collectors.toSet());
    }

    private DfHacoMapPickup doMerge(OptionalThing<DfHacoMapPickup> pickupOpt, List<DfHacoMapPiece> pieces, Set<String> mergedPieceCodeSet) {
        // convert diffPart list
        Stream<DfHacoMapDiffPart> piecesDiffPartStream = pieces.stream().map(piece -> mappingPieceToDiffPart(piece));
        Stream<DfHacoMapDiffPart> pickupDiffPartStream = pickupOpt.map(pickup -> pickup.getDiffList().stream()).orElse(Stream.empty());

        // grouping to diffPartMap (key: diffCode, value: diffPart list)
        Map<String, List<DfHacoMapDiffPart>> diffPartMap =
                Stream.concat(piecesDiffPartStream, pickupDiffPartStream).collect(Collectors.groupingBy(diffPart -> diffPart.diffCode));

        // filter merged property by piece code
        List<DfHacoMapDiffPart> filteredDiffPartList = diffPartMap.entrySet().stream().map(diffPartEntry -> {
            String diffDate = diffPartEntry.getValue()
                    .stream()
                    .findFirst()
                    .map(diffPart -> diffPart.diffDate)
                    .orElseThrow(() -> new IllegalStateException("Diffdate is null. \n" + diffPartEntry));

            List<DfHacoMapPropertyPart> filteredPropertyPartList = diffPartEntry.getValue()
                    .stream()
                    .flatMap(hacoMapDiffPart -> hacoMapDiffPart.getPropertyList().stream())
                    .filter(propertyPart -> !mergedPieceCodeSet.contains(propertyPart.pieceCode))
                    .collect(Collectors.toList());

            return new DfHacoMapDiffPart(diffPartEntry.getKey(), diffDate, filteredPropertyPartList);
        }).collect(Collectors.toList());

        // create new pickup
        DfHacoMapPickup newPickup = new DfHacoMapPickup();
        newPickup.addAllDiffList(filteredDiffPartList);
        newPickup.setPickupDatetime(getCurrentLocalDateTime());

        return newPickup;
    }

    private DfHacoMapDiffPart mappingPieceToDiffPart(DfHacoMapPiece piece) {
        DfHacoMapPropertyPart propertyPart =
                new DfHacoMapPropertyPart(piece.hacomment, piece.diffComment, piece.authorList, piece.pieceCode, piece.pieceOwner,
                        piece.pieceDatetime, piece.previousPieceList);
        return new DfHacoMapDiffPart(piece.diffCode, piece.diffDate, propertyPart);
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
            for (File pieceFile : Objects.requireNonNull(pieceDir.listFiles())) {
                if (pieceFile.isFile()) {
                    pieceFile.delete();
                } else {
                    doDeletePiece(pieceFile.getAbsolutePath());
                }
            }
        }
    }

    // ===================================================================================
    //                                                                           Diff Code
    //                                                                           =========
    public String generateDiffCode(String diffDate) {
        return DfStringUtil.replaceBy(diffDate, REPLACE_MAP_FOR_HACOMMENT_ID);
    }

    // ===================================================================================
    //                                                                           File Path
    //                                                                           =========
    protected String buildPieceDirPath(String clientDirPath) {
        return clientDirPath + BASE_PIECE_DIR_PATH;
    }

    protected String buildPickupFilePath(String clientDirPath) {
        return clientDirPath + BASE_PICKUP_FILE_PATH;
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

    // ===================================================================================
    //                                                                          Time Logic
    //                                                                          ==========
    protected LocalDateTime getCurrentLocalDateTime() {
        // done hakiba use callback by jflute (2018/02/22)
        return this.currentDatetimeSupplier.get();
    }
}
