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

package org.dbflute.infra.doc.hacomment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hakiba
 */
public class DfHacoMapPiece {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String diffCode;
    protected final String diffDate;
    protected final String hacomment;
    protected final String diffComment;
    protected final List<String> authorList;
    protected final String pieceCode;
    protected final String pieceOwner;
    protected final LocalDateTime pieceDatetime;
    protected final List<String> previousPieceList;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfHacoMapPiece(String diffCode, String diffDate, String hacomment, String diffComment, List<String> authorList, String pieceCode,
                          String pieceOwner, LocalDateTime pieceDatetime, List<String> previousPieceList) {
        this.diffCode = diffCode;
        this.diffDate = diffDate;
        this.hacomment = hacomment;
        this.diffComment = diffComment;
        this.authorList = new ArrayList<>(authorList);
        if (!authorList.contains(pieceOwner)) {
            this.authorList.add(pieceOwner);
        }
        this.pieceCode = pieceCode;
        this.pieceOwner = pieceOwner;
        this.pieceDatetime = pieceDatetime;
        this.previousPieceList = previousPieceList;
    }

    @SuppressWarnings("unchecked")
    public DfHacoMapPiece(Map<String, Object> map) {
        this.diffCode = (String) map.get("diffCode");
        this.diffDate = (String) map.get("diffDate");
        this.hacomment = (String) map.get("hacomment");
        this.diffComment = (String) map.get("diffComment");
        this.authorList = (List<String>) map.get("authorList");
        this.pieceCode = (String) map.get("pieceCode");
        this.pieceOwner = (String) map.get("pieceOwner");
        this.pieceDatetime = (LocalDateTime) map.get("pieceDatetime");
        this.previousPieceList = (List<String>) map.get("previousPieceList");
    }

    // ===================================================================================
    //                                                                           Converter
    //                                                                           =========
    public Map<String, Object> convertToMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("diffCode", this.diffCode);
        map.put("diffDate", this.diffDate);
        map.put("hacomment", this.hacomment);
        map.put("diffComment", this.diffComment);
        map.put("authorList", this.authorList);
        map.put("pieceCode", this.pieceCode);
        map.put("pieceOwner", this.pieceOwner);
        map.put("pieceDatetime", this.pieceDatetime);
        map.put("previousPieceList", this.previousPieceList);
        return map;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getDiffCode() {
        return diffCode;
    }

    public String getDiffDate() {
        return diffDate;
    }

    public String getHacomment() {
        return hacomment;
    }

    public String getDiffComment() {
        return diffComment;
    }

    public List<String> getAuthorList() {
        return authorList;
    }

    public String getPieceCode() {
        return pieceCode;
    }

    public String getPieceOwner() {
        return pieceOwner;
    }

    public LocalDateTime getPieceDatetime() {
        return pieceDatetime;
    }

    public List<String> getPreviousPieceList() {
        return previousPieceList;
    }
}
