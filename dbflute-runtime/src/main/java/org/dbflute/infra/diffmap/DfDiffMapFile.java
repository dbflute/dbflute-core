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
package org.dbflute.infra.diffmap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Predicate;

import org.dbflute.exception.DfPropFileReadFailureException;
import org.dbflute.exception.DfPropFileWriteFailureException;
import org.dbflute.helper.dfmap.DfMapFile;
import org.dbflute.helper.dfmap.DfMapStyle;
import org.dbflute.helper.filesystem.FileHierarchyTracer;
import org.dbflute.helper.filesystem.FileHierarchyTracingHandler;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.util.Srl;

/**
 * The file handling for difference map.
 * @author jflute
 * @since 0.9.7.1 (2010/06/06 Sunday)
 */
public class DfDiffMapFile {

    // ===================================================================================
    //                                                                             Collect
    //                                                                             =======
    // @since 1.2.6
    public Map<String, Object> collectDiffMap(String pieceDirPath, Predicate<File> pieceDeterminer, String monolithicMapPath) {
        if (pieceDirPath == null || pieceDirPath.trim().isEmpty()) {
            throw new IllegalArgumentException("The argument 'pieceDirPath' should not be null or empty.");
        }
        if (pieceDeterminer == null) {
            throw new IllegalArgumentException("The argument 'pieceDeterminer' should not be null.");
        }
        if (monolithicMapPath == null || monolithicMapPath.trim().isEmpty()) {
            throw new IllegalArgumentException("The argument 'monolithicMapPath' should not be null or empty.");
        }
        // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
        // e.g.
        //  schema
        //   |-schemadiff // since 1.2.6
        //   |  |-2022
        //   |  |  |-diffpiece-maihamadb-20220315-035402-427.diffmap
        //   |  |  |-diffpiece-maihamadb-20220506-152734-812.diffmap
        //   |  |  |-diffpiece-maihamadb-20220506-155155-198.diffmap
        //   |  |-2021
        //   |  |-...
        //   |
        //   |-project-history-maihamadb.diffmap // until 1.2.5
        // _/_/_/_/_/_/_/_/_/_/
        final SortedMap<String, Object> sortedMap = new TreeMap<>((key1, key2) -> {
            return -(key1.compareTo(key2)); // key is diffDate, so recent first
        });
        final File pieceDir = new File(pieceDirPath);
        if (pieceDir.exists()) {
            pickupPiece(pieceDir, pieceDeterminer, sortedMap);
        }
        pickupMonolithic(monolithicMapPath, sortedMap);
        return convertToStyleMap(sortedMap);
    }

    protected void pickupPiece(File pieceDir, Predicate<File> pieceDeterminer, SortedMap<String, Object> sortedMap) {
        final List<File> pieceFileList = new ArrayList<>();
        final FileHierarchyTracer tracer = new FileHierarchyTracer();
        tracer.trace(pieceDir, new FileHierarchyTracingHandler() {
            public boolean isTargetFileOrDir(File currentFile) {
                if (pieceDeterminer.test(currentFile)) { // piece
                    return true;
                }
                if (currentFile.isDirectory() && Srl.isNumberHarfAll(currentFile.getName())) { // e.g. 2022
                    return true;
                }
                return false;
            }

            public void handleFile(File currentFile) throws IOException {
                pieceFileList.add(currentFile);
            }
        });
        // no needed by sorted map
        //orderPieceFileListAsRecent(pieceFileList);
        for (File pieceFile : pieceFileList) {
            try {
                // basically one element and no overriding but no check here because of no big problem
                final Map<String, Object> readMap = readMap(new FileInputStream(pieceFile));
                sortedMap.putAll(readMap);
            } catch (FileNotFoundException e) { // no way, just in case
                throw new DfPropFileReadFailureException("Not found the piece file: " + pieceFile, e);
            }
        }
    }

    protected void pickupMonolithic(String monolithicMapPath, SortedMap<String, Object> sortedMap) {
        final File monolithicMapFile = new File(monolithicMapPath);
        if (monolithicMapFile.exists()) {
            try {
                final Map<String, Object> readMap = readMap(new FileInputStream(monolithicMapFile));
                sortedMap.putAll(readMap); // also no check here
            } catch (FileNotFoundException e) { // no way, just in case
                throw new DfPropFileReadFailureException("Not found the monolithic map file: " + monolithicMapPath, e);
            }
        }
    }

    protected Map<String, Object> convertToStyleMap(SortedMap<String, Object> sortedMap) {
        final Map<String, Object> resultMap = new DfMapStyle().newStringObjectMap();
        resultMap.putAll(sortedMap);
        return resultMap;
    }

    // ===================================================================================
    //                                                                               Read
    //                                                                              ======
    /**
     * @param ins The input stream for DBFlute property file, which is closed here. (NotNull)
     * @return The read map, keeping order. (NotNull, EmptyAllowed)
     * @throws DfPropFileReadFailureException When it fails to read.
     */
    public Map<String, Object> readMap(InputStream ins) {
        final DfMapFile mapFile = createMapFile();
        try {
            return mapFile.readMap(ins);
        } catch (Exception e) {
            throwDfPropFileReadFailureException(ins, e);
            return null; // unreachable
        }
    }

    protected void throwDfPropFileReadFailureException(InputStream ins, Exception e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to read the diff-map file.");
        br.addItem("Advice");
        br.addElement("Make sure the map-string is correct in the file.");
        br.addElement("For exapmle, the number of start and end braces are the same.");
        br.addItem("DBFlute Property");
        br.addElement(ins);
        final String msg = br.buildExceptionMessage();
        throw new DfPropFileReadFailureException(msg, e);
    }

    // ===================================================================================
    //                                                                               Write
    //                                                                               =====
    /**
     * @param ous The output stream for DBFlute property file, which is closed here. (NotNull)
     * @param map The written map to the file. (NotNull)
     * @throws DfPropFileWriteFailureException When it fails to write.
     */
    public void writeMap(OutputStream ous, Map<String, Object> map) {
        final DfMapFile mapFile = createMapFile();
        try {
            mapFile.writeMap(ous, map);
        } catch (Exception e) {
            throwDfPropFileWriteFailureException(ous, e);
        }
    }

    protected void throwDfPropFileWriteFailureException(OutputStream ous, Exception e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to write the diff-map file.");
        br.addItem("DBFlute Property");
        br.addElement(ous);
        final String msg = br.buildExceptionMessage();
        throw new DfPropFileWriteFailureException(msg, e);
    }

    // ===================================================================================
    //                                                                            Map File
    //                                                                            ========
    protected DfMapFile createMapFile() {
        return new DfMapFile(); // migrated for performance (2018/05/05) 
    }
}