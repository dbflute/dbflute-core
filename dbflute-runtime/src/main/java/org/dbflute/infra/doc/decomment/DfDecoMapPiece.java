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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dbflute.helper.mapstring.MapListString;

/**
 * @author hakiba
 * @author cabos
 * @author jflute
 */
public class DfDecoMapPiece {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final String DEFAULT_FORMAT_VERSION = "1.0";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String formatVersion = DEFAULT_FORMAT_VERSION;
    protected String tableName;
    protected String columnName;
    protected DfDecoMapPieceTargetType targetType;
    protected String decomment;
    protected String databaseComment;
    protected Long commentVersion;
    protected List<String> authorList = new ArrayList<>();
    protected String pieceCode;
    protected LocalDateTime pieceDatetime;
    protected String pieceOwner;
    protected List<String> previousPieceList = new ArrayList<>();

    // done cabos move to before accessor by jflute (2017/08/10)
    // ===================================================================================
    //                                                                           Converter
    //                                                                           =========
    // done (by jflute) yuto write e.g. (2017/11/11)
    // map:{
    //     ; formatVersion = 1.0
    //     ; tableName = MEMBER
    //     ; columnName = null
    //     ; targetType = TABLE
    //     ; decomment = loginable user, my name is deco
    //     ; databaseComment = loginable user
    //     ; commentVersion = 0
    //     ; authorList = list:{ deco }
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
    //     ; pieceCode = HF7ELSE
    //     ; pieceDatetime = 2017-10-15T16:17:18.199
    //     ; pieceOwner = jflute
    //     ; previousPieceList = list:{ FE893L1 }
    // }
    public Map<String, Object> convertToMap() {
        // done cabos Map by jflute (2017/08/10)
        // done cabos use Linked by jflute (2017/09/07)
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("formatVersion", this.formatVersion);
        map.put("tableName", this.tableName);
        map.put("columnName", this.columnName);
        map.put("targetType", this.targetType.code());
        map.put("decomment", this.decomment);
        map.put("databaseComment", this.databaseComment);
        map.put("commentVersion", this.commentVersion);
        map.put("authorList", this.authorList);
        map.put("pieceCode", this.pieceCode);
        map.put("pieceDatetime", this.pieceDatetime);
        map.put("pieceOwner", this.pieceOwner);
        map.put("previousPieceList", this.previousPieceList);
        return map;
    }

    // ===================================================================================
    //                                                                            Override
    //                                                                            ========
    @Override
    public String toString() {
        return new MapListString().buildMapString(this.convertToMap());
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    // done cabos order get/set, get/set, ... please m(_ _)m by jflute (2017/11/11)
    public String getFormatVersion() {
        return formatVersion;
    }

    public void setFormatVersion(String formatVersion) {
        this.formatVersion = formatVersion;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public DfDecoMapPieceTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(DfDecoMapPieceTargetType targetType) {
        this.targetType = targetType;
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

    public Long getCommentVersion() {
        return commentVersion;
    }

    public void setCommentVersion(Long commentVersion) {
        this.commentVersion = commentVersion;
    }

    public List<String> getAuthorList() {
        return authorList;
    }

    public void addAuthor(String author) {
        // done cabos use LinkedHashSet to keep order by jflute (2017/09/07)
        Set<String> authorSet = new LinkedHashSet<>(this.authorList);
        authorSet.add(author);
        this.authorList = new ArrayList<>(authorSet);
    }

    public void addAllAuthors(Collection<String> authors) {
        Set<String> authorSet = new LinkedHashSet<>(this.authorList);
        authorSet.addAll(authors);
        this.authorList = new ArrayList<>(authorSet);
    }

    public String getPieceCode() {
        return pieceCode;
    }

    public void setPieceCode(String pieceCode) {
        this.pieceCode = pieceCode;
    }

    public LocalDateTime getPieceDatetime() {
        return pieceDatetime;
    }

    public void setPieceDatetime(LocalDateTime pieceDatetime) {
        this.pieceDatetime = pieceDatetime;
    }

    public String getPieceOwner() {
        return pieceOwner;
    }

    public void setPieceOwner(String pieceOwner) {
        this.pieceOwner = pieceOwner;
    }

    public List<String> getPreviousPieceList() {
        return previousPieceList;
    }

    public void addAllPreviousPieces(Collection<String> previousPieces) {
        Set<String> previousSet = new LinkedHashSet<>(this.authorList);
        previousSet.addAll(previousPieces);
        this.previousPieceList = new ArrayList<>(previousPieces);
    }
}
