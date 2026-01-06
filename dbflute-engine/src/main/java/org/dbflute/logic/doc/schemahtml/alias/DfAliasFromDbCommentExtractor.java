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
package org.dbflute.logic.doc.schemahtml.alias;

import java.util.Date;
import java.util.function.Supplier;

import org.dbflute.DfBuildProperties;
import org.dbflute.logic.doc.supplement.firstdate.DfFirstDateDeterminer;
import org.dbflute.optional.OptionalThing;
import org.dbflute.properties.DfDocumentProperties;

/**
 * @author jflute
 * @since 1.3.2 (2026/01/06 Tuesday at ichihara)
 */
public class DfAliasFromDbCommentExtractor {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfFirstDateDeterminer _firstDateDeterminer = new DfFirstDateDeterminer();

    // ===================================================================================
    //                                                                               Alias
    //                                                                               =====
    public String extractTableAlias(String comment, Date tableFirstDate) { // alias is trimmed
        return doExtractAliasFromDbComment(comment, () -> {
            return judgeDbCommentOnAliasBasis(firstDateAfter -> {
                return _firstDateDeterminer.determineTableFirstDateAfter(tableFirstDate, firstDateAfter);
            });
        });
    }

    public String extractColumnAlias(String comment, Date columnFirstDate, Supplier<Date> tableFirstDateProvider) { // alias is trimmed
        return doExtractAliasFromDbComment(comment, () -> {
            return judgeDbCommentOnAliasBasis(firstDateAfter -> {
                return _firstDateDeterminer.determineColumnFirstDateAfter(columnFirstDate, tableFirstDateProvider, firstDateAfter);
            });
        });
    }

    protected String doExtractAliasFromDbComment(String comment, DbCommentOnAliasBasisJudge onAliasBasisJudge) { // alias is trimmed
        if (isAliasHandling(comment)) {
            if (hasAliasDelimiter(comment)) {
                final String delimiter = getAliasDelimiterInDbComment();
                final String candidateAlias = comment.substring(0, comment.indexOf(delimiter)).trim();
                if (isDbCommentNumberAliasTreatedAsDescription()) {
                    try {
                        Integer.parseInt(candidateAlias);
                        // maybe number classification description e.g. 1:formalized, 2:provisional, ...
                        return null; // the candidate alias cannot be treated as alias
                    } catch (NumberFormatException ignored) {
                        return candidateAlias; // no problem
                    }
                } else { // mainly here
                    return candidateAlias;
                }
            } else { // no delimiter
                if (onAliasBasisJudge.decide()) {
                    return comment != null ? comment.trim() : null; // because the comment is for alias
                }
            }
        }
        return null; // alias does not exist everywhere if alias handling is not valid
    }

    // ===================================================================================
    //                                                                         Description
    //                                                                         ===========
    public String extractTableDescription(String comment, Date tableFirstDate) { // description is trimmed
        return doExtractDescriptionFromDbComment(comment, () -> {
            return extractTableAlias(comment, tableFirstDate);
        }, () -> {
            return judgeDbCommentOnAliasBasis(firstDateAfter -> {
                return _firstDateDeterminer.determineTableFirstDateAfter(tableFirstDate, firstDateAfter);
            });
        });
    }

    public String extractColumnDescription(String comment, Date columnFirstDate, Supplier<Date> tableFirstDateProvider) { // description is trimmed
        return doExtractDescriptionFromDbComment(comment, () -> {
            return extractColumnAlias(comment, columnFirstDate, tableFirstDateProvider);
        }, () -> {
            return judgeDbCommentOnAliasBasis(firstDateAfter -> {
                return _firstDateDeterminer.determineColumnFirstDateAfter(columnFirstDate, tableFirstDateProvider, firstDateAfter);
            });
        });
    }

    protected String doExtractDescriptionFromDbComment(String comment, Supplier<String> aliasExtractor,
            DbCommentOnAliasBasisJudge onAliasBasisJudge) { // description is trimmed
        if (isAliasHandling(comment)) {
            if (hasAliasDelimiter(comment)) {
                final String aliasFromDbComment = aliasExtractor.get(); // null allowed
                if (aliasFromDbComment != null) { // also comment is not null
                    final String delimiter = getAliasDelimiterInDbComment();
                    return comment.substring(comment.indexOf(delimiter) + delimiter.length()).trim();
                }
            } else { // no delimiter
                if (onAliasBasisJudge.decide()) { // the comment is for alias
                    return null; // so no description here
                }
            }
        }
        return comment != null ? comment.trim() : null;
    }

    // ===================================================================================
    //                                                                      Alias Hadnling
    //                                                                      ==============
    protected boolean isAliasHandling(String comment) {
        if (comment == null || comment.trim().length() == 0) {
            return false;
        }
        return isAliasDelimiterInDbCommentValid();
    }

    protected boolean hasAliasDelimiter(String comment) {
        final String delimiter = getAliasDelimiterInDbComment();
        return comment.contains(delimiter);
    }

    // ===================================================================================
    //                                                                      on Alias Basis
    //                                                                      ==============
    public static interface DbCommentOnAliasBasisJudge {

        boolean decide();
    }

    protected boolean judgeDbCommentOnAliasBasis(DfAliasBasisFirstDateAfterEvaluator evaluator) {
        if (!isDbCommentOnAliasBasis()) {
            return false;
        }
        // basically alias basis here
        final OptionalThing<Date> optFirstDateAfter = getAliasBasisIfFirstDateAfter();
        if (optFirstDateAfter.isEmpty()) {
            return true; // no condition so complete alias basis
        }
        // firstDate condition is specified here so it needs to check table first date
        final Date firstDateAfter = optFirstDateAfter.get(); // not null
        return evaluator.evaluate(firstDateAfter);
    }

    public static interface DfAliasBasisFirstDateAfterEvaluator {

        boolean evaluate(Date firstDateAfter);
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected boolean isAliasDelimiterInDbCommentValid() {
        return getDocumentProperties().isAliasDelimiterInDbCommentValid();
    }

    protected String getAliasDelimiterInDbComment() {
        return getDocumentProperties().getAliasDelimiterInDbComment();
    }

    protected boolean isDbCommentOnAliasBasis() {
        return getDocumentProperties().isDbCommentOnAliasBasis();
    }

    protected OptionalThing<Date> getAliasBasisIfFirstDateAfter() {
        return getDocumentProperties().getAliasBasisIfFirstDateAfter();
    }

    protected boolean isDbCommentNumberAliasTreatedAsDescription() {
        return getDocumentProperties().isDbCommentNumberAliasTreatedAsDescription();
    }

    protected DfDocumentProperties getDocumentProperties() {
        return getProperties().getDocumentProperties();
    }

    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }
}
