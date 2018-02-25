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
package org.dbflute.infra.doc.decomment.parts;

import org.dbflute.helper.HandyDate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author cabos
 * @author deco
 */
public class DfDecoMapPropertyPart {

    protected final String decomment;
    protected final String databaseComment;
    protected final String pieceCode;
    protected final LocalDateTime pieceDatetime;
    protected final String pieceOwner;
    protected final String pieceGitBranch;
    protected final List<String> previousPieceList;
    protected final Long commentVersion;
    protected final List<String> authorList;

    public DfDecoMapPropertyPart(String decomment, String databaseComment, String pieceCode, LocalDateTime pieceDatetime,
                                 String pieceOwner, String pieceGitBranch, List<String> previousPieceList, 
                                 long commentVersion, List<String> authorList) {
        this.decomment = decomment;
        this.databaseComment = databaseComment;
        this.pieceCode = pieceCode;
        this.pieceDatetime = pieceDatetime;
        this.pieceOwner = pieceOwner;
        this.pieceGitBranch = pieceGitBranch;
        this.previousPieceList = previousPieceList.stream().distinct().collect(Collectors.toList());
        this.commentVersion = commentVersion;
        this.authorList = authorList.stream().distinct().collect(Collectors.toList());
        if (!this.authorList.contains(pieceOwner)) {
            this.authorList.add(pieceOwner);
        }
    }

    public DfDecoMapPropertyPart(Map<String, Object> propertyMap) {
        this.decomment = (String) propertyMap.get("decomment");
        this.databaseComment = (String) propertyMap.get("databaseComment");
        this.pieceCode = (String) propertyMap.get("pieceCode");
        this.pieceDatetime = new HandyDate((String) propertyMap.get("pieceDatetime")).getLocalDateTime();
        this.pieceOwner = (String) propertyMap.get("pieceOwner");
        this.pieceGitBranch = (String) propertyMap.get("pieceGitBranch");
        this.previousPieceList =
                ((List<?>) propertyMap.get("previousPieceList")).stream().map(obj -> (String) obj).distinct().collect(Collectors.toList());
        this.commentVersion = Long.valueOf((String) propertyMap.get("commentVersion"));
        this.authorList = ((List<?>) propertyMap.get("authorList")).stream().map(obj -> (String) obj).distinct().collect(Collectors.toList());
        if (!this.authorList.contains(pieceOwner)) {
            this.authorList.add(pieceOwner);
        }
    }

    public String getDecomment() {
        return decomment;
    }

    public String getDatabaseComment() {
        return databaseComment;
    }

    public String getPieceCode() {
        return pieceCode;
    }

    public LocalDateTime getPieceDatetime() {
        return pieceDatetime;
    }

    public String getPieceOwner() {
        return pieceOwner;
    }

    public String getPieceGitBranch() {
        return pieceGitBranch;
    }

    public List<String> getPreviousPieceList() {
        return Collections.unmodifiableList(previousPieceList);
    }

    public Long getCommentVersion() {
        return commentVersion;
    }

    public List<String> getAuthorList() {
        return Collections.unmodifiableList(authorList);
    }

    public Map<String, Object> convertToMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("decomment", this.decomment);
        map.put("databaseComment", this.databaseComment);
        map.put("pieceCode", this.pieceCode);
        map.put("pieceOwner", this.pieceOwner);
        map.put("pieceGitBranch", this.pieceGitBranch);
        map.put("pieceDatetime", this.pieceDatetime);
        map.put("previousPieceList", this.previousPieceList);
        map.put("commentVersion", this.commentVersion);
        map.put("authorList", this.authorList);
        return map;
    }
}
