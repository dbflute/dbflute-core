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
package org.dbflute.infra.doc.decomment.parts;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.dbflute.helper.HandyDate;

/**
 * @author cabos
 */
public class DfDecoMapPropertyPart {

    protected String decomment;
    protected String databaseComment;
    protected String pieceCode;
    protected LocalDateTime pieceDatetime;
    protected String pieceOwner;
    protected List<String> previousPieceList = new ArrayList<>();
    protected long commentVersion;
    protected List<String> authorList = new ArrayList<>();

    public DfDecoMapPropertyPart() {
    }

    public DfDecoMapPropertyPart(Map<String, Object> propertyMap) {
        this.decomment = (String) propertyMap.get("decomment");
        this.databaseComment = (String) propertyMap.get("databaseComment");
        this.pieceCode = (String) propertyMap.get("pieceCode");
        this.pieceDatetime = new HandyDate((String) propertyMap.get("pieceDatetime")).getLocalDateTime();
        this.pieceOwner = (String) propertyMap.get("pieceOwner");
        this.previousPieceList =
                ((List<?>) propertyMap.get("previousPieceList")).stream().map(obj -> (String) obj).collect(Collectors.toList());
        this.commentVersion = Long.valueOf((String) propertyMap.get("commentVersion"));
        this.authorList = ((List<?>) propertyMap.get("authorList")).stream().map(obj -> (String) obj).collect(Collectors.toList());
    }

    public String getDecomment() {
        return decomment;
    }

    public void setDecomment(String decomment) {
        this.decomment = decomment;
    }

    public String getDatabaseComment() {
        return databaseComment;
    }

    public void setDatabaseComment(String databaseComment) {
        this.databaseComment = databaseComment;
    }

    public void setPieceCode(String pieceCode) {
        this.pieceCode = pieceCode;
    }

    public String getPieceCode() {
        return pieceCode;
    }

    public LocalDateTime getPieceDatetime() {
        return pieceDatetime;
    }

    public void setPieceDatetime(LocalDateTime pieceDatetime) {
        this.pieceDatetime = pieceDatetime;
    }

    public void setPieceOwner(String pieceOwner) {
        this.pieceOwner = pieceOwner;
    }

    public String getPieceOwner() {
        return pieceOwner;
    }

    public void addAllPreviousPieces(Collection<String> previousPieces) {
        Set<String> previousSet = new LinkedHashSet<>(this.previousPieceList);
        previousSet.addAll(previousPieces);
        this.previousPieceList = new ArrayList<>(previousSet);
    }

    public List<String> getPreviousPieceList() {
        return previousPieceList;
    }

    public long getCommentVersion() {
        return commentVersion;
    }

    public void setCommentVersion(long commentVersion) {
        this.commentVersion = commentVersion;
    }

    public List<String> getAuthorList() {
        return authorList;
    }

    public void addAllAuthors(Collection<String> authors) {
        Set<String> authorSet = new LinkedHashSet<>(this.authorList);
        authorSet.addAll(authors);
        this.authorList = new ArrayList<>(authors);
    }

    public Map<String, Object> convertToMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("decomment", this.decomment);
        map.put("databaseComment", this.databaseComment);
        map.put("pieceCode", this.pieceCode);
        map.put("pieceOwner", this.pieceOwner);
        map.put("pieceDatetime", this.pieceDatetime);
        map.put("previousPieceList", this.previousPieceList);
        map.put("commentVersion", this.commentVersion);
        map.put("authorList", this.authorList);
        return map;
    }
}
