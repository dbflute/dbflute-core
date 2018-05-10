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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dbflute.helper.mapstring.MapListString;

/**
 * @author hakiba
 * @author cabos
 * @author jflute
 * @author deco
 */
public class DfDecoMapPiece {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String DEFAULT_FORMAT_VERSION = "1.1";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String formatVersion;
    protected final String tableName;
    protected final String columnName;
    protected final DfDecoMapPieceTargetType targetType;
    protected final String decomment;
    protected final String databaseComment;
    protected final Long commentVersion;
    protected final List<String> authorList;
    protected final String pieceCode;
    protected final LocalDateTime pieceDatetime;
    protected final String pieceOwner;
    protected final String pieceGitBranch;
    protected final List<String> previousPieceList;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfDecoMapPiece(String formatVersion, String tableName, String columnName, DfDecoMapPieceTargetType targetType, String decomment,
            String databaseComment, Long commentVersion, List<String> authorList, String pieceCode, LocalDateTime pieceDatetime,
            String pieceOwner, String pieceGitBranch, List<String> previousPieceList) {
        this.formatVersion = formatVersion;
        this.tableName = tableName;
        this.columnName = columnName;
        this.targetType = targetType;
        this.decomment = decomment;
        this.databaseComment = databaseComment;
        this.commentVersion = commentVersion;
        this.authorList = new ArrayList<>(authorList);
        if (!authorList.contains(pieceOwner)) {
            this.authorList.add(pieceOwner);
        }
        this.pieceCode = pieceCode;
        this.pieceDatetime = pieceDatetime;
        this.pieceOwner = pieceOwner;
        this.pieceGitBranch = pieceGitBranch;
        this.previousPieceList = previousPieceList;
    }

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
    //     ; pieceGitBranch = develop
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
    //     ; pieceGitBranch = master
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
        map.put("pieceGitBranch", this.pieceGitBranch);
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

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public DfDecoMapPieceTargetType getTargetType() {
        return targetType;
    }

    public boolean isTargetTypeTable() {
        return targetType == DfDecoMapPieceTargetType.Table;
    }

    public boolean isTargetTypeColumn() {
        return targetType == DfDecoMapPieceTargetType.Column;
    }

    public String getDecomment() {
        return decomment;
    }

    public String getDatabaseComment() {
        return databaseComment;
    }

    public Long getCommentVersion() {
        return commentVersion;
    }

    public List<String> getAuthorList() {
        return Collections.unmodifiableList(authorList);
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
}
