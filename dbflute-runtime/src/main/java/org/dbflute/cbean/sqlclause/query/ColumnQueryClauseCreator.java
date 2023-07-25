/*
 * Copyright 2014-2022 the original author or authors.
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
package org.dbflute.cbean.sqlclause.query;

import java.util.function.BiFunction;

import org.dbflute.cbean.ConditionBean;
import org.dbflute.cbean.chelper.HpCalcSpecification;
import org.dbflute.cbean.sqlclause.SqlClause;
import org.dbflute.cbean.sqlclause.join.InnerJoinNoWaySpeaker;
import org.dbflute.cbean.sqlclause.subquery.SubQueryIndentProcessor;
import org.dbflute.dbmeta.info.ColumnInfo;
import org.dbflute.exception.ColumnQueryCalculationUnsupportedColumnTypeException;
import org.dbflute.system.DBFluteSystem;

/**
 * @author jflute
 * @since 1.2.7 (2023/07/25 Tuesday at ichihara)
 */
public class ColumnQueryClauseCreator {

    // ===================================================================================
    //                                                                       Create Clause
    //                                                                       =============
    public <CB extends ConditionBean> QueryClause createColumnQueryClause(String leftColumn, String operand, String rightColumn,
            HpCalcSpecification<CB> rightCalcSp, BiFunction<ColumnInfo, String, String> decryptor) {
        return new QueryClause() {
            @Override
            public String toString() {
                final String leftExp = resolveColumnExp(rightCalcSp.getLeftCalcSp(), leftColumn);
                final String rightExp = resolveColumnExp(rightCalcSp, rightColumn);
                return buildColumnQueryClause(leftExp, operand, rightExp);
            }

            protected String resolveColumnExp(HpCalcSpecification<CB> calcSp, String columnExp) {
                final String resolvedExp;
                if (calcSp != null) {
                    final String statement = calcSp.buildStatementToSpecifidName(columnExp);
                    if (statement != null) { // exists calculation
                        assertCalculationColumnType(calcSp);
                        resolvedExp = statement; // cipher already resolved
                    } else {
                        final ColumnInfo columnInfo = calcSp.getSpecifiedColumnInfo();
                        if (columnInfo != null) { // means plain column
                            resolvedExp = decryptor.apply(columnInfo, columnExp);
                        } else { // deriving sub-query
                            resolvedExp = columnExp;
                        }
                    }
                } else {
                    resolvedExp = columnExp;
                }
                return resolvedExp;
            }

            protected void assertCalculationColumnType(HpCalcSpecification<CB> calcSp) {
                if (calcSp.hasConvert()) {
                    return; // because it may be Date type
                }
                final ColumnInfo columnInfo = calcSp.getResolvedSpecifiedColumnInfo();
                if (columnInfo != null) { // basically true but checked just in case
                    if (!columnInfo.isObjectNativeTypeNumber()) {
                        // *simple message because other types may be supported at the future
                        String msg = "Not number column specified: " + columnInfo;
                        throw new ColumnQueryCalculationUnsupportedColumnTypeException(msg);
                    }
                }
            }
        };
    }

    protected String buildColumnQueryClause(String leftExp, String operand, String rightExp) { // can be overridden just in case
        final StringBuilder sb = new StringBuilder();
        if (hasSubQueryEndOnLastLine(leftExp)) {
            if (hasSubQueryEndOnLastLine(rightExp)) { // (sub-query = sub-query)
                // add line separator before right expression
                // because of independent format for right query
                sb.append(reflectToSubQueryEndOnLastLine(leftExp, " " + operand + " "));
                sb.append(ln()).append("       ").append(rightExp);
            } else { // (sub-query = column)
                sb.append(reflectToSubQueryEndOnLastLine(leftExp, " " + operand + " " + rightExp));
            }
        } else { // (column = sub-query) or (column = column) 
            sb.append(leftExp).append(" ").append(operand).append(" ").append(rightExp);
        }
        return sb.toString();
    }

    protected boolean hasSubQueryEndOnLastLine(String columnExp) {
        return SubQueryIndentProcessor.hasSubQueryEndOnLastLine(columnExp);
    }

    protected String reflectToSubQueryEndOnLastLine(String columnExp, String inserted) {
        return SubQueryIndentProcessor.moveSubQueryEndToRear(columnExp + inserted);
    }

    // ===================================================================================
    //                                                                            Register
    //                                                                            ========
    public <CB extends ConditionBean> void registerColumnQueryClause(SqlClause sqlClause, QueryClause queryClause,
            HpCalcSpecification<CB> leftCalcSp, HpCalcSpecification<CB> rightCalcSp) {
        // /= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
        // may null-revived -> no way to be inner-join
        // (DerivedReferrer or conversion's coalesce)
        // 
        // for example, the following SQL is no way to be inner
        // (suppose if PURCHASE refers WITHDRAWAL)
        // 
        // select mb.MEMBER_ID, mb.MEMBER_NAME
        //      , mb.MEMBER_STATUS_CODE, wd.MEMBER_ID as WD_MEMBER_ID
        //   from MEMBER mb
        //     left outer join MEMBER_SERVICE ser on mb.MEMBER_ID = ser.MEMBER_ID
        //     left outer join MEMBER_WITHDRAWAL wd on mb.MEMBER_ID = wd.MEMBER_ID
        //  where (select coalesce(max(pc.PURCHASE_PRICE), 0)
        //           from PURCHASE pc
        //          where pc.MEMBER_ID = wd.MEMBER_ID -- may null
        //        ) < ser.SERVICE_POINT_COUNT
        //  order by mb.MEMBER_ID
        // 
        // it has a possible to be inner-join in various case
        // but it is hard to analyze in detail so simplify it
        // = = = = = = = = = =/
        final QueryUsedAliasInfo leftInfo = createColumnQueryAliasInfo(leftCalcSp);
        final QueryUsedAliasInfo rightInfo = createColumnQueryAliasInfo(rightCalcSp);
        sqlClause.registerWhereClause(queryClause, leftInfo, rightInfo);
    }

    protected <CB extends ConditionBean> QueryUsedAliasInfo createColumnQueryAliasInfo(final HpCalcSpecification<CB> calcSp) {
        final String usedAliasName = calcSp.getResolvedSpecifiedTableAliasName();
        return new QueryUsedAliasInfo(usedAliasName, new InnerJoinNoWaySpeaker() {
            public boolean isNoWayInner() {
                return calcSp.mayNullRevived();
            }
        });
    }

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    protected String ln() {
        return DBFluteSystem.ln();
    }
}
