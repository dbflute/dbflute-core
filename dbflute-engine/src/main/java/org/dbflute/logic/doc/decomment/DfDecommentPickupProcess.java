/*
 * Copyright 2014-2021 the original author or authors.
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
package org.dbflute.logic.doc.decomment;

import java.util.List;

import org.dbflute.infra.doc.decomment.DfDecoMapFile;
import org.dbflute.infra.doc.decomment.DfDecoMapMapping;
import org.dbflute.infra.doc.decomment.DfDecoMapPickup;
import org.dbflute.infra.doc.decomment.DfDecoMapPiece;
import org.dbflute.optional.OptionalThing;
import org.dbflute.system.DBFluteSystem;

/**
 * @author cabos
 * @author jflute
 */
public class DfDecommentPickupProcess {

    private final DfDecoMapFile _decoMapFile = new DfDecoMapFile(() -> {
        return DBFluteSystem.currentLocalDateTime();
    });

    public DfDecoMapPickup pickupDecomment(String clientPath) {
        final List<DfDecoMapPiece> pieceList = readPieceList(clientPath);
        final List<DfDecoMapMapping> mappingList = readMappingList(clientPath);
        final OptionalThing<DfDecoMapPickup> optPickup = readPickup(clientPath);
        final DfDecoMapPickup mergedPickup = mergeDecoMap(optPickup, pieceList, mappingList);
        if (!mergedPickup.getTableList().isEmpty()) { // not to make empty file if no decomments
            writePickup(clientPath, mergedPickup);
        }
        deletePiece(clientPath);
        return mergedPickup;
    }

    private List<DfDecoMapPiece> readPieceList(String clientPath) {
        return _decoMapFile.readPieceList(clientPath);
    }

    private List<DfDecoMapMapping> readMappingList(String clientPath) {
        return _decoMapFile.readMappingList(clientPath);
    }

    private OptionalThing<DfDecoMapPickup> readPickup(String clientPath) {
        return _decoMapFile.readPickup(clientPath);
    }

    private DfDecoMapPickup mergeDecoMap(OptionalThing<DfDecoMapPickup> optPickup, List<DfDecoMapPiece> pieceList,
            List<DfDecoMapMapping> mappingList) {
        return _decoMapFile.merge(optPickup, pieceList, mappingList);
    }

    private void writePickup(String clientPath, DfDecoMapPickup mergedPickup) {
        _decoMapFile.writePickup(clientPath, mergedPickup);
    }

    private void deletePiece(String clientPath) {
        _decoMapFile.deletePiece(clientPath);
    }
}
