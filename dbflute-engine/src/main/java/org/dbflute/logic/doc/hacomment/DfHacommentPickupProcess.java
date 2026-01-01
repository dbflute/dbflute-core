/*
 * Copyright 2014-2026 the original author or authors.
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

import java.util.List;

import org.dbflute.infra.doc.hacomment.DfHacoMapFile;
import org.dbflute.infra.doc.hacomment.DfHacoMapPickup;
import org.dbflute.infra.doc.hacomment.DfHacoMapPiece;
import org.dbflute.optional.OptionalThing;
import org.dbflute.system.DBFluteSystem;

/**
 * @author hakiba
 * @author shiny
 */
public class DfHacommentPickupProcess {

    private final DfHacoMapFile _hacoMapFile = new DfHacoMapFile(() -> DBFluteSystem.currentLocalDateTime());

    // ===================================================================================
    //                                                                             Pick up
    //                                                                           =========
    public DfHacoMapPickup pickupHacomment(String clientPath) {
        List<DfHacoMapPiece> pieceList = readPieceList(clientPath);
        OptionalThing<DfHacoMapPickup> optPickup = readPickup(clientPath);

        final DfHacoMapPickup pickUp;
        if (pieceList.isEmpty()) { // no resources exist to pick up
            pickUp = orElseEmptyPickup(optPickup);
        } else { // pick up resources exists, needs to merge
            pickUp = mergeDecoMap(optPickup, pieceList);
            migratePieceToPickUp(clientPath, pickUp);
        }
        return pickUp;
    }

    // -----------------------------------------------------
    //                                     no Piece Handling
    //                                                ------
    protected DfHacoMapPickup orElseEmptyPickup(OptionalThing<DfHacoMapPickup> optPickup) {
        return optPickup.orElseGet(() -> {
            DfHacoMapPickup pickup = new DfHacoMapPickup();
            pickup.setPickupDatetime(DBFluteSystem.currentLocalDateTime());
            return pickup;
        });
    }

    // -----------------------------------------------------
    //                                     Migrate to Pickup
    //                                                ------
    protected void migratePieceToPickUp(String clientPath, DfHacoMapPickup mergedPickup) {
        if (!mergedPickup.getDiffList().isEmpty()) { // not to make empty file if no hacomments
            writePickup(clientPath, mergedPickup);
        }
        deletePiece(clientPath);
    }

    // ===================================================================================
    //                                                                      File Operation
    //                                                                      ==============
    private List<DfHacoMapPiece> readPieceList(String clientPath) {
        return _hacoMapFile.readPieceList(clientPath);
    }

    private OptionalThing<DfHacoMapPickup> readPickup(String clientPath) {
        return _hacoMapFile.readPickup(clientPath);
    }

    private DfHacoMapPickup mergeDecoMap(OptionalThing<DfHacoMapPickup> optPickup, List<DfHacoMapPiece> pieceList) {
        return _hacoMapFile.merge(optPickup, pieceList);
    }

    private void writePickup(String clientPath, DfHacoMapPickup mergedPickup) {
        _hacoMapFile.writePickup(clientPath, mergedPickup);
    }

    private void deletePiece(String clientPath) {
        _hacoMapFile.deletePiece(clientPath);
    }
}
