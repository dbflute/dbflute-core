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
package org.dbflute.cbean.exception;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.dbflute.cbean.ConditionBean;
import org.dbflute.cbean.ConditionQuery;
import org.dbflute.cbean.chelper.HpCBPurpose;
import org.dbflute.cbean.chelper.HpInvalidQueryInfo;
import org.dbflute.cbean.ckey.ConditionKey;
import org.dbflute.cbean.cvalue.ConditionValue;
import org.dbflute.cbean.dream.SpecifiedColumn;
import org.dbflute.cbean.ordering.ManualOrderOption;
import org.dbflute.cbean.sqlclause.orderby.OrderByElement;
import org.dbflute.dbmeta.DBMeta;
import org.dbflute.dbmeta.info.ColumnInfo;
import org.dbflute.exception.ColumnQueryInvalidColumnSpecificationException;
import org.dbflute.exception.FixedConditionParameterNotFoundException;
import org.dbflute.exception.IllegalConditionBeanOperationException;
import org.dbflute.exception.InvalidQueryRegisteredException;
import org.dbflute.exception.OptionThatsBadTimingException;
import org.dbflute.exception.OrScopeQueryAndPartAlreadySetupException;
import org.dbflute.exception.OrScopeQueryAndPartNotOrScopeException;
import org.dbflute.exception.OrderByIllegalPurposeException;
import org.dbflute.exception.PagingPageSizeNotPlusException;
import org.dbflute.exception.QueryAlreadyRegisteredException;
import org.dbflute.exception.QueryDerivedReferrerInvalidColumnSpecificationException;
import org.dbflute.exception.QueryDerivedReferrerSelectAllPossibleException;
import org.dbflute.exception.QueryIllegalPurposeException;
import org.dbflute.exception.QueryThatsBadTimingException;
import org.dbflute.exception.RequiredOptionNotFoundException;
import org.dbflute.exception.ScalarConditionInvalidColumnSpecificationException;
import org.dbflute.exception.ScalarConditionUnmatchedColumnTypeException;
import org.dbflute.exception.ScalarSelectInvalidColumnSpecificationException;
import org.dbflute.exception.SetupSelectIllegalPurposeException;
import org.dbflute.exception.SetupSelectThatsBadTimingException;
import org.dbflute.exception.SpecifiedDerivedOrderByAliasNameNotFoundException;
import org.dbflute.exception.SpecifyColumnAlreadySpecifiedEveryColumnException;
import org.dbflute.exception.SpecifyColumnAlreadySpecifiedExceptColumnException;
import org.dbflute.exception.SpecifyColumnNotSetupSelectColumnException;
import org.dbflute.exception.SpecifyColumnTwoOrMoreColumnException;
import org.dbflute.exception.SpecifyColumnWithDerivedReferrerException;
import org.dbflute.exception.SpecifyDerivedReferrerConflictAliasNameException;
import org.dbflute.exception.SpecifyDerivedReferrerEntityPropertyNotFoundException;
import org.dbflute.exception.SpecifyDerivedReferrerIllegalPurposeException;
import org.dbflute.exception.SpecifyDerivedReferrerInvalidAliasNameException;
import org.dbflute.exception.SpecifyDerivedReferrerInvalidColumnSpecificationException;
import org.dbflute.exception.SpecifyDerivedReferrerSelectAllPossibleException;
import org.dbflute.exception.SpecifyDerivedReferrerTwoOrMoreException;
import org.dbflute.exception.SpecifyEveryColumnAlreadySpecifiedColumnException;
import org.dbflute.exception.SpecifyExceptColumnAlreadySpecifiedColumnException;
import org.dbflute.exception.SpecifyIllegalPurposeException;
import org.dbflute.exception.SpecifyRelationIllegalPurposeException;
import org.dbflute.exception.SpecifyThatsBadTimingException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.Srl;

/**
 * @author jflute
 */
public class ConditionBeanExceptionThrower implements Serializable {

    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                       Set up Select
    //                                                                       =============
    public void throwSetupSelectIllegalPurposeException(HpCBPurpose purpose, ConditionBean baseCB, String foreignPropertyName) {
        final ExceptionMessageBuilder br = createExceptionMessageBuilder();
        br.addNotice("Bad location to call SetupSelect.");
        br.addItem("Advice");
        br.addElement("The condition-bean is not allowed to set up select.");
        br.addElement("Because this is for " + purpose + ".");
        br.addElement("For example:");
        br.addElement("  (x): (ExistsReferrer)");
        br.addElement("    cb.query().existsPurchase(purchaseCB -> {");
        br.addElement("        purchaseCB.setupSelect_Product(); // *NG");
        br.addElement("    });");
        br.addElement("  (x): (Union)");
        br.addElement("    cb.union(unionCB -> {");
        br.addElement("        unionCB.setupSelect_MemberStatus(); // *NG");
        br.addElement("    });");
        br.addElement("  (o): (Normal Use)");
        br.addElement("    cb.setupSelect_MemberStatus(); // OK");
        br.addItem("ConditionBean"); // don't use displaySql because of illegal CB's state
        br.addElement(baseCB.getClass().getName());
        br.addElement("(" + purpose + ")");
        br.addItem("Setup Relation");
        br.addElement(foreignPropertyName);
        final String msg = br.buildExceptionMessage();
        throw new SetupSelectIllegalPurposeException(msg);
    }

    public void throwSetupSelectThatsBadTimingException(ConditionBean lockedCB, String foreignPropertyName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("That's bad timing for SetupSelect.");
        br.addItem("Advice");
        br.addElement("The condition-bean was locked in the timing.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    ListResultBean<Member> memberList = memberBhv.selectList(cb -> {");
        br.addElement("        cb.query().existsPurchase(purchaseCB -> {");
        br.addElement("            cb.setupSelect_MemberStatus(); // *NG");
        br.addElement("        });");
        br.addElement("    });");
        br.addElement("  (o):");
        br.addElement("    cb.setupSelect_MemberStatus(); // OK");
        br.addElement("    ListResultBean<Member> memberList = memberBhv.selectList(cb -> {");
        br.addElement("        cb.query().existsPurchase(purchaseCB -> {");
        br.addElement("            purchaseCB.query().set... // you can use only purchaseCB here");
        br.addElement("        });");
        br.addElement("    });");
        Class<? extends ConditionBean> cbType = lockedCB.getClass();
        br.addItem("Locked ConditionBean"); // don't use displaySql because of illegal CB's state
        br.addElement(cbType.getName());
        br.addElement("(" + lockedCB.getPurpose() + ")");
        br.addItem("Your SetupSelect");
        br.addElement(cbType.getSimpleName() + "#setupSelect_" + initCap(foreignPropertyName) + "()");
        final String msg = br.buildExceptionMessage();
        throw new SetupSelectThatsBadTimingException(msg);
    }

    // unused because it has been allowed
    //public void throwSetupSelectAfterUnionException(ConditionBean baseCB, String foreignPropertyName) {
    //    final ExceptionMessageBuilder br = createExceptionMessageBuilder();
    //    br.addNotice("The setup-select was called after union.");
    //    br.addItem("Advice");
    //    br.addElement("The setup-select should be called before calling union().");
    //    br.addElement("For example:");
    //    br.addElement("  (x):");
    //    br.addElement("    MemberCB cb = new MemberCB();");
    //    br.addElement("    cb.query().setXxx...;");
    //    br.addElement("    cb.union(new UnionQuery<MemberCB>() {");
    //    br.addElement("        public void query(MemberCB unionCB) {");
    //    br.addElement("            unionCB.query().setXxx...;");
    //    br.addElement("        }");
    //    br.addElement("    });");
    //    br.addElement("    cb.setupSelect_MemberStatus(); // *NG");
    //    br.addElement("  (o):");
    //    br.addElement("    MemberCB cb = new MemberCB();");
    //    br.addElement("    cb.setupSelect_MemberStatus(); // you should call here");
    //    br.addElement("    cb.query().setXxx...;");
    //    br.addElement("    cb.union(new UnionQuery<MemberCB>() {");
    //    br.addElement("        public void query(MemberCB unionCB) {");
    //    br.addElement("            unionCB.query().setXxx...;");
    //    br.addElement("        }");
    //    br.addElement("    });");
    //    br.addItem("ConditionBean"); // don't use displaySql because of illegal CB's state
    //    br.addElement(baseCB.getClass().getName());
    //    br.addItem("Setup Relation");
    //    br.addElement(foreignPropertyName);
    //    final String msg = br.buildExceptionMessage();
    //    throw new SetupSelectAfterUnionException(msg);
    //}

    // ===================================================================================
    //                                                                             Specify
    //                                                                             =======
    public void throwSpecifyIllegalPurposeException(HpCBPurpose purpose, ConditionBean baseCB) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Bad location to call specify().");
        br.addItem("Advice");
        br.addElement("The condition-bean is not allowed to specify() there.");
        br.addElement("Because this is for " + purpose + ".");
        br.addElement("For example:");
        br.addElement("  (x): (ExistsReferrer)");
        br.addElement("    cb.query().existsPurchaseList(purchaseCB -> {");
        br.addElement("        purchaseCB.specify()... // *NG");
        br.addElement("    });");
        br.addElement("  (x): (Union)");
        br.addElement("    cb.union(unionCB -> {");
        br.addElement("        unionCB.specify()... // *NG");
        br.addElement("    });");
        br.addElement("  (o): (ExistsReferrer)");
        br.addElement("    cb.specify()... // OK");
        br.addElement("    cb.query().existsPurchaseList(purchaseCB -> {");
        br.addElement("        purchaseCB.query().set...");
        br.addElement("    });");
        br.addItem("ConditionBean"); // don't use displaySql because of illegal CB's state
        br.addElement(baseCB.getClass().getName());
        br.addElement("(" + baseCB.getPurpose() + ")");
        final String msg = br.buildExceptionMessage();
        throw new SpecifyIllegalPurposeException(msg);
    }

    public void throwSpecifyThatsBadTimingException(ConditionBean lockedCB) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("That's bad timing for Specify.");
        br.addItem("Advice");
        br.addElement("The condition-bean was locked in the timing.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    cb.specify().derivedPurchase().max(purchaseCB -> {");
        br.addElement("        cb.specify().columnMemberId(); // *NG");
        br.addElement("        purchaseCB.query().set...");
        br.addElement("    }, ...);");
        br.addElement("  (o):");
        br.addElement("    cb.specify().derivedPurchase().max(purchaseCB -> {");
        br.addElement("        purchaseCB.specify().columnMemberId(); // OK");
        br.addElement("        purchaseCB.query().set...");
        br.addElement("    }, ...);");
        br.addItem("Locked ConditionBean"); // don't use displaySql because of illegal CB's state
        br.addElement(lockedCB.getClass().getName());
        br.addElement("(" + lockedCB.getPurpose() + ")");
        final String msg = br.buildExceptionMessage();
        throw new SpecifyThatsBadTimingException(msg);
    }

    public void throwSpecifyColumnTwoOrMoreColumnException(HpCBPurpose purpose, ConditionBean baseCB, String columnName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("You specified two or more columns!");
        br.addItem("Advice");
        br.addElement("The condition-bean is not allowed to specify two or more columns.");
        br.addElement("Because the conditoin-bean is for " + purpose + ".");
        br.addElement("For example:");
        br.addElement("  (x): (DerivedReferrer)");
        br.addElement("    cb.specify().derivedPurchase().max(purchaseCB -> {");
        br.addElement("        purchaseCB.specify().columnPurchaseCount();");
        br.addElement("        purchaseCB.specify().columnPurchasePrice(); // *NG");
        br.addElement("    });");
        br.addElement("  (x): (ColumnQuery)");
        br.addElement("    cb.columnQuery(colCB -> {");
        br.addElement("        colCB.specify().columnMemberName();");
        br.addElement("        colCB.specify().columnBirthdate(); // *NG");
        br.addElement("    })...");
        br.addElement("  (o): (DerivedReferrer)");
        br.addElement("    cb.specify().derivedPurchase().max(purchaseCB -> {");
        br.addElement("        purchaseCB.specify().columnPurchaseCount();");
        br.addElement("    });");
        br.addItem("ConditionBean"); // don't use displaySql because of illegal CB's state
        br.addElement(baseCB.getClass().getName());
        br.addElement("(" + baseCB.getPurpose() + ")");
        br.addItem("Specified Column");
        br.addElement(baseCB.asTableDbName() + "." + columnName);
        final String msg = br.buildExceptionMessage();
        throw new SpecifyColumnTwoOrMoreColumnException(msg);
    }

    public void throwSpecifyColumnNotSetupSelectColumnException(ConditionBean baseCB, String columnName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Lonely specify().column... was called. (without SetupSelect)");
        br.addItem("Advice");
        br.addElement("SpecifyColumn needs SetupSelect for the table.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    memberBhv.selectEntity(cb -> {");
        br.addElement("        cb.specify().specifyMemberStatus().columnMemberStatusName(); // *NG");
        br.addElement("    });");
        br.addElement("  (o):");
        br.addElement("    memberBhv.selectEntity(cb -> {");
        br.addElement("        cb.setupSelect_MemberStatus(); // *Point");
        br.addElement("        cb.specify().specifyMemberStatus().columnMemberStatusName(); // OK");
        br.addElement("    });");
        br.addItem("ConditionBean"); // don't use displaySql because of illegal CB's state
        br.addElement(baseCB.getClass().getName());
        br.addElement("(" + baseCB.getPurpose() + ")");
        br.addItem("Specified Column");
        br.addElement(baseCB.asTableDbName() + "." + columnName);
        final String msg = br.buildExceptionMessage();
        throw new SpecifyColumnNotSetupSelectColumnException(msg);
    }

    public void throwSpecifyColumnWithDerivedReferrerException(HpCBPurpose purpose, ConditionBean baseCB, String columnName,
            String referrerName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("You specified both SpecifyColumn and (Specify)DerivedReferrer!");
        br.addItem("Advice");
        br.addElement("The condition-bean is not allowed to specify both functions.");
        br.addElement("Because the conditoin-bean is for " + purpose + ".");
        br.addElement("For example:");
        br.addElement("  (x): (ColumnQuery)");
        br.addElement("    cb.columnQuery(cb -> {");
        br.addElement("        cb.specify().columnBirthdate();");
        br.addElement("        cb.specify().derivedPurchase().max(...); // *NG");
        br.addElement("    }).greaterEqual(...);");
        br.addElement("  (o): (ColumnQuery)");
        br.addElement("    cb.columnQuery(colCB -> {");
        br.addElement("        colCB.specify().columnBirthdate(); // OK");
        br.addElement("    }).greaterEqual(...);");
        br.addElement("  (o): (ColumnQuery)");
        br.addElement("    cb.columnQuery(colCB -> {");
        br.addElement("        colCB.specify().derivedPurchase().max(...); // OK");
        br.addElement("    }).greaterEqual(...);");
        br.addItem("ConditionBean"); // don't use displaySql because of illegal CB's state
        br.addElement(baseCB.getClass().getName());
        br.addElement("(" + baseCB.getPurpose() + ")");
        br.addItem("Specified Column");
        br.addElement(baseCB.asTableDbName() + "." + columnName);
        br.addItem("Derived Referrer");
        br.addElement(referrerName);
        final String msg = br.buildExceptionMessage();
        throw new SpecifyColumnWithDerivedReferrerException(msg);
    }

    public void throwSpecifyColumnAlreadySpecifiedEveryColumnException(String tableDbName, String columnName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The SpecifyColumn is specified after SpecifyEveryColumn.");
        br.addItem("Advice");
        br.addElement("You cannot specify columns with every column.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    memberBhv.batchUpdate(memberList, colCB -> {");
        br.addElement("        colCB.specify().everyColumn();");
        br.addElement("        colCB.specify().columnMemberName(); // *No");
        br.addElement("    });");
        br.addElement("  (o):");
        br.addElement("    memberBhv.batchUpdate(memberList, colCB -> {");
        br.addElement("        colCB.specify().everyColumn();");
        br.addElement("    });");
        br.addItem("Base Table");
        br.addElement(tableDbName);
        br.addItem("Specified Column");
        br.addElement(columnName);
        final String msg = br.buildExceptionMessage();
        throw new SpecifyColumnAlreadySpecifiedEveryColumnException(msg);
    }

    public void throwSpecifyColumnAlreadySpecifiedExceptColumnException(String tableDbName, String columnName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The SpecifyColumn is specified after SpecifyExceptColumn.");
        br.addItem("Advice");
        br.addElement("You cannot specify columns with except columns.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    ... = memberBhv.selectList(cb -> {");
        br.addElement("        cb.specify().exceptRecordMetaColumn();");
        br.addElement("        cb.specify().columnMemberName(); // *No");
        br.addElement("    });");
        br.addElement("  (o):");
        br.addElement("    ... = memberBhv.selectList(cb -> {");
        br.addElement("        cb.specify().exceptRecordMetaColumn();");
        br.addElement("    });");
        br.addElement("  (o):");
        br.addElement("    ... = memberBhv.selectList(cb -> {");
        br.addElement("        cb.specify().columnMemberName();");
        br.addElement("    });");
        br.addItem("Base Table");
        br.addElement(tableDbName);
        br.addItem("Specified Column");
        br.addElement(columnName);
        final String msg = br.buildExceptionMessage();
        throw new SpecifyColumnAlreadySpecifiedExceptColumnException(msg);
    }

    public void throwSpecifyEveryColumnAlreadySpecifiedColumnException(String tableDbName, Map<String, SpecifiedColumn> specifiedColumnMap) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The SpecifyEveryColumn is specified after SpecifyColumn.");
        br.addItem("Advice");
        br.addElement("You cannot specify columns with every column.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    memberBhv.batchUpdate(memberList, colCB -> {");
        br.addElement("        colCB.specify().columnMemberName();");
        br.addElement("        colCB.specify().everyColumn(); // *No");
        br.addElement("    });");
        br.addElement("  (o):");
        br.addElement("    memberBhv.batchUpdate(memberList, colCB -> {");
        br.addElement("        colCB.specify().everyColumn();");
        br.addElement("    });");
        br.addItem("Base Table");
        br.addElement(tableDbName);
        if (specifiedColumnMap != null) { // basically true
            br.addItem("Specified Column");
            final Collection<SpecifiedColumn> columnList = specifiedColumnMap.values();
            for (SpecifiedColumn column : columnList) {
                br.addElement(column);
            }
        }
        final String msg = br.buildExceptionMessage();
        throw new SpecifyEveryColumnAlreadySpecifiedColumnException(msg);
    }

    public void throwSpecifyExceptColumnAlreadySpecifiedColumnException(String tableDbName, Map<String, SpecifiedColumn> specifiedColumnMap) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The SpecifyExceptColumn is specified after SpecifyColumn.");
        br.addItem("Advice");
        br.addElement("You cannot specify columns with except columns.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    ... = memberBhv.selectList(cb -> {");
        br.addElement("        cb.specify().columnMemberName(");
        br.addElement("        cb.specify().exceptRecordMetaColumn(); // *No");
        br.addElement("    });");
        br.addElement("  (o):");
        br.addElement("    ... = memberBhv.selectList(cb -> {");
        br.addElement("        cb.specify().exceptRecordMetaColumn();");
        br.addElement("    });");
        br.addElement("  (o):");
        br.addElement("    ... = memberBhv.selectList(cb -> {");
        br.addElement("        cb.specify().columnMemberName()");
        br.addElement("    });");
        br.addItem("Base Table");
        br.addElement(tableDbName);
        if (specifiedColumnMap != null) { // basically true
            br.addItem("Specified Column");
            final Collection<SpecifiedColumn> columnList = specifiedColumnMap.values();
            for (SpecifiedColumn column : columnList) {
                br.addElement(column);
            }
        }
        final String msg = br.buildExceptionMessage();
        throw new SpecifyExceptColumnAlreadySpecifiedColumnException(msg);
    }

    public void throwSpecifyRelationIllegalPurposeException(HpCBPurpose purpose, ConditionBean baseCB, String relationName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Bad location to call specify() for relation.");
        br.addItem("Advice");
        br.addElement("The condition-bean is not allowed to specify() for relation.");
        br.addElement("Because this is for " + purpose + ".");
        br.addElement("For example:");
        br.addElement("  (x): (ScalarSelect)");
        br.addElement("    memberBhv.selectScalar(Date.class).max(cb -> {");
        br.addElement("        cb.specify().specifyMemberStatus().col.. // *NG");
        br.addElement("    });");
        br.addElement("  (x): (ScalarCondition)");
        br.addElement("    cb.query().scalar_Equal().max(Date.class).max(scalarCB -> {");
        br.addElement("        scalarCB.specify().specifyMemberStatusName().col..; // *NG");
        br.addElement("    });");
        br.addElement("  (x): (VaryingUpdate)");
        br.addElement("    memberBhv.varyingUpdate(member, op -> op.self(colCB -> {");
        br.addElement("        colCB.specify().specifyMemberStatus().col.. // *NG");
        br.addElement("    });");
        br.addElement("  (o): (ScalarSelect)");
        br.addElement("    memberBhv.scalarSelect(Date.class).max(scalarCB -> {");
        br.addElement("        scalarCB.specify().columnBirthdate(); // OK");
        br.addElement("    });");
        // don't use displaySql because of illegal CB's state
        br.addItem("ConditionBean");
        br.addElement(baseCB.getClass().getName());
        br.addElement("(" + purpose + ")");
        br.addItem("Specified Relation");
        br.addElement(relationName);
        final String msg = br.buildExceptionMessage();
        throw new SpecifyRelationIllegalPurposeException(msg);
    }

    public void throwSpecifyDerivedReferrerIllegalPurposeException(HpCBPurpose purpose, ConditionBean baseCB, String referrerName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Bad location to call (Specify)DerivedReferrer.");
        br.addItem("Advice");
        br.addElement("The condition-bean is not allowed to (Specify)DerivedReferrer.");
        br.addElement("Because this is for " + purpose + ".");
        br.addElement("For example:");
        br.addElement("  (x): (ScalaCondition)");
        br.addElement("    cb.query().scalar_Equal().max(scalarCB -> {");
        br.addElement("        scalarCB.specify().derivedPurchase()...; // *NG");
        br.addElement("    });");
        br.addElement("  (o): (ScalaCondition)");
        br.addElement("    cb.query().scalar_Equal().max(scalarCB -> {");
        br.addElement("        scalarCB.specify().columnPurchaseCount(); // OK");
        br.addElement("    });");
        // don't use displaySql because of illegal CB's state
        br.addItem("ConditionBean");
        br.addElement(baseCB.getClass().getName());
        br.addElement("(" + purpose + ")");
        br.addItem("Specified Referrer");
        br.addElement(referrerName);
        final String msg = br.buildExceptionMessage();
        throw new SpecifyDerivedReferrerIllegalPurposeException(msg);
    }

    public void throwSpecifyDerivedReferrerTwoOrMoreException(HpCBPurpose purpose, ConditionBean baseCB, String referrerName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The two-or-more derived-referrers was specifed.");
        br.addItem("Advice");
        br.addElement("The condition-bean is not allowed to specify two-or-more derived referrers.");
        br.addElement("Because this is for " + purpose + ".");
        br.addElement("For example:");
        br.addElement("  (x): (ColumnQuery)");
        br.addElement("    cb.columnQuery(colCB -> {");
        br.addElement("        colCB.specify().derivedPurchase().max(...);");
        br.addElement("        colCB.specify().derivedPurchase().max(...); // *NG");
        br.addElement("    }).greaterEqual(...);");
        br.addElement("  (o): (ColumnQuery)");
        br.addElement("    cb.columnQuery(colCB -> {");
        br.addElement("        colCB.specify().derivedPurchase().max(...); // OK");
        br.addElement("    }).greaterEqual(...);");
        // don't use displaySql because of illegal CB's state
        br.addItem("ConditionBean");
        br.addElement(baseCB.getClass().getName());
        br.addElement("(" + purpose + ")");
        br.addItem("Specified Referrer");
        br.addElement(referrerName);
        final String msg = br.buildExceptionMessage();
        throw new SpecifyDerivedReferrerTwoOrMoreException(msg);
    }

    // ===================================================================================
    //                                                                       Scalar Select
    //                                                                       =============
    public void throwScalarSelectInvalidColumnSpecificationException(ConditionBean cb, Class<?> resultType) {
        final ExceptionMessageBuilder br = createExceptionMessageBuilder();
        br.addNotice("The specified column for scalar select was invalid.");
        br.addItem("Advice");
        br.addElement("You should call specify().column[TargetColumn]() only once.");
        br.addElement("For example:");
        br.addElement("");
        br.addElement("  (x): (empty)");
        br.addElement("    memberBhv.selectScalar(LocalDate.class).max(cb -> {");
        br.addElement("        // *NG, it should not be empty");
        br.addElement("    });");
        br.addElement("");
        br.addElement("  (x): (duplicated)");
        br.addElement("    memberBhv.selectScalar(LocalDate.class).max(cb -> {");
        br.addElement("        // *NG, it should be the only one");
        br.addElement("        cb.specify().columnBirthdate();");
        br.addElement("        cb.specify().columnRegisterDatetime();");
        br.addElement("    });");
        br.addElement("");
        br.addElement("  (o):");
        br.addElement("    memberBhv.selectScalar(LocalDate.class).max(cb -> {");
        br.addElement("        cb.specify().columnBirthdate(); // OK");
        br.addElement("    });");
        br.addItem("ConditionBean"); // don't use displaySql because of illegal CB's state
        br.addElement(cb.getClass().getName());
        br.addElement("(" + cb.getPurpose() + ")");
        br.addItem("Result Type");
        br.addElement(resultType.getName());
        final String msg = br.buildExceptionMessage();
        throw new ScalarSelectInvalidColumnSpecificationException(msg);
    }

    // ===================================================================================
    //                                                            Specify Derived Referrer
    //                                                            ========================
    public void throwSpecifyDerivedReferrerInvalidAliasNameException(ConditionQuery localCQ) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The alias name for (Specify)DerivedReferrer was INVALID.");
        br.addItem("Advice");
        br.addElement("You should set valid alias name. (NotNull, NotEmpty)");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    cb.specify().derivePurchaseList().max(purchaseCB -> {");
        br.addElement("        purchaseCB.specify().columnPurchaseDatetime();");
        br.addElement("    }, null); // *No! {null, \"\", \"   \"} are NG!");
        br.addElement("");
        br.addElement("  (o):");
        br.addElement("    cb.specify().derivePurchaseList().max(purchaseCB -> {");
        br.addElement("        purchaseCB.specify().columnPurchaseDatetime();");
        br.addElement("    }, Member.ALIAS_latestPurchaseDatetime); // OK");
        br.addItem("BasePoint Table");
        br.addElement(localCQ.asTableDbName());
        final String msg = br.buildExceptionMessage();
        throw new SpecifyDerivedReferrerInvalidAliasNameException(msg);
    }

    public void throwSpecifyDerivedReferrerConflictAliasNameException(ConditionQuery localCQ, String aliasName, ColumnInfo existingColumn) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The alias name for (Specify)DerivedReferrer was CONFLICT.");
        br.addItem("Advice");
        br.addElement("You should set unique alias name in the select world.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    cb.specify().derivePurchaseList().max(purchaseCB -> {");
        br.addElement("        purchaseCB.specify().columnPurchaseDatetime();");
        br.addElement("    }, \"$memberName\"); // *NG: same name as MEMBER's MEMBER_NAME");
        br.addElement("");
        br.addElement("  (o):");
        br.addElement("    cb.specify().derivePurchaseList().max(purchaseCB -> {");
        br.addElement("        purchaseCB.specify().columnPurchaseDatetime();");
        br.addElement("    }, \"$latestPurchaseDatetime\"); // OK");
        br.addItem("BasePoint Table");
        br.addElement(localCQ.asTableDbName());
        br.addItem("Conflict Alias Name");
        br.addElement(aliasName);
        br.addItem("Existing Column");
        br.addElement(existingColumn);
        final String msg = br.buildExceptionMessage();
        throw new SpecifyDerivedReferrerConflictAliasNameException(msg);
    }

    public void throwSpecifyDerivedReferrerEntityPropertyNotFoundException(String aliasName, Class<?> entityType) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The property for derived-referrer was NOT FOUND in the entity!");
        br.addItem("Advice");
        br.addElement("You should implement a property (setter and getter) in the entity.");
        br.addElement("Or you should confirm whether the alias name has typo or not.");
        br.addElement("For example:");
        br.addElement("  (ConditionBean):");
        br.addElement("    cb.specify().derivePurchaseList().max(purchaseCB -> {");
        br.addElement("        purchaseCB.specify().columnPurchasePrice();");
        br.addElement("    }, Member.ALIAS_highestPurchasePrice); // OK");
        br.addElement("");
        br.addElement("  (Extended Entity):");
        br.addElement("    // in the entity of Member...");
        br.addElement("    public static final String ALIAS_highestPurchasePrice = \"HIGHEST_PURCHASE_PRICE\";");
        br.addElement("    protected Integer _highestPurchasePrice;");
        br.addElement("    public Integer getHighestPurchasePrice() {");
        br.addElement("        return _highestPurchasePrice;");
        br.addElement("    }");
        br.addElement("    public void setHighestPurchasePrice(Integer highestPurchasePrice) {");
        br.addElement("        _highestPurchasePrice = highestPurchasePrice;");
        br.addElement("    }");
        br.addElement("");
        br.addElement("Or if you use derived mappable alias (no entity property, key-deriven handling)");
        br.addElement("You should add mappaable alias prefix '$' to your alias name like this:");
        br.addElement("For example:");
        br.addElement("  (ConditionBean and Entity handling):");
        br.addElement("    String highestAlias = \"$HIGHEST_PURCHASE_PRICE\"; // OK");
        br.addElement("    cb.specify().derivePurchaseList().max(purchaseCB -> {");
        br.addElement("        purchaseCB.specify().columnPurchasePrice();");
        br.addElement("    }, highestAlias);");
        br.addElement("    List<Member> memberList = memberBhv.selectList(cb);");
        br.addElement("    for (Member member ; memberList) {");
        br.addElement("        ... = member.derived(highestAlias, Integer.class);");
        br.addElement("    }");
        br.addItem("Alias Name");
        br.addElement(aliasName);
        br.addItem("Target Entity");
        br.addElement(entityType);
        final String msg = br.buildExceptionMessage();
        throw new SpecifyDerivedReferrerEntityPropertyNotFoundException(msg);
    }

    public void throwSpecifyDerivedReferrerInvalidColumnSpecificationException(String function, String aliasName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The specified the column for (Specify)DerivedReferrer was INVALID!");
        br.addItem("Advice");
        br.addElement("You should call specify().column[TargetColumn]() only once.");
        br.addElement("(If your function is count(), the target column should be primary key)");
        br.addElement("For example:");
        br.addElement("  (x): (empty)");
        br.addElement("    cb.specify().derivePurchaseList().max(purchaseCB -> {");
        br.addElement("        // *NG, it should not be empty");
        br.addElement("    }, Member.ALIAS_latestPurchaseDatetime);");
        br.addElement("  (x): (duplicated)");
        br.addElement("    cb.specify().derivePurchaseList().max(purchaseCB -> {");
        br.addElement("        // *NG, it should be the only one");
        br.addElement("        purchaseCB.specify().columnPurchaseDatetime();");
        br.addElement("        purchaseCB.specify().columnPurchaseCount();");
        br.addElement("    }, Member.ALIAS_latestPurchaseDatetime);");
        br.addElement("  (o):");
        br.addElement("    cb.specify().derivePurchaseList().max(purchaseCB -> {");
        br.addElement("        purchaseCB.specify().columnPurchaseDatetime(); // OK");
        br.addElement("    }, Member.ALIAS_latestPurchaseDatetime);");
        br.addItem("Function Method");
        br.addElement(xconvertFunctionToMethod(function));
        br.addItem("Alias Name");
        br.addElement(aliasName);
        final String msg = br.buildExceptionMessage();
        throw new SpecifyDerivedReferrerInvalidColumnSpecificationException(msg);
    }

    // *see DerivedReferrer@assertDerivedReferrerColumnType()'s comment
    //public void throwSpecifyDerivedReferrerUnmatchedColumnTypeException(String function, String derivedColumnDbName,
    //        Class<?> derivedColumnType) {
    //    final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
    //    br.addNotice("Unmatched the type of the specified the column for (Specify)DerivedReferrer.");
    //    br.addItem("Advice");
    //    br.addElement("The type of the specified the column unmatched with the function!");
    //    br.addElement("You should confirm the list as follow:");
    //    br.addElement("    count() : String, Number, Date *with distinct same");
    //    br.addElement("    max()   : String, Number, Date");
    //    br.addElement("    min()   : String, Number, Date");
    //    br.addElement("    sum()   : Number");
    //    br.addElement("    avg()   : Number");
    //    br.addItem("Function");
    //    br.addElement(function);
    //    br.addItem("Derive Column");
    //    br.addElement(derivedColumnDbName + "(" + derivedColumnType.getName() + ")");
    //    final String msg = br.buildExceptionMessage();
    //    throw new SpecifyDerivedReferrerUnmatchedColumnTypeException(msg);
    //}

    public void throwSpecifyDerivedReferrerSelectAllPossibleException(String function, ConditionQuery subQuery, String aliasName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The (Specify)DerivedReferrer might select all.");
        br.addItem("Advice");
        br.addElement("If you suppress correlation, set your original correlation condition.");
        br.addItem("Function");
        br.addElement(function);
        br.addItem("Referrer");
        br.addElement(subQuery.asTableDbName());
        br.addItem("Alias Name");
        br.addElement(aliasName);
        final String msg = br.buildExceptionMessage();
        throw new SpecifyDerivedReferrerSelectAllPossibleException(msg);
    }

    // ===================================================================================
    //                                                           Specified Derived OrderBy
    //                                                           =========================
    public void throwSpecifiedDerivedOrderByAliasNameNotFoundException(String aliasName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the aliasName was not found for SpecifiedDerivedOrderBy.");
        br.addItem("Advice");
        br.addElement("You should specified an alias name specified as (Specify)DerivedReferrer.");
        br.addElement("For example:");
        br.addElement("  (x)");
        br.addElement("    cb.specify().derivePurchaseList().max(purchaseCB -> {");
        br.addElement("        purchaseCB.specify().specifyProduct().columnProductName(); // *NG");
        br.addElement("    }, \"LATEST_PURCHASE_DATETIME\");");
        br.addElement("    cb.query().addSpecifiedDerivedOrderBy_Desc(\"WRONG_NAME_DATETIME\");");
        br.addElement("  (o):");
        br.addElement("    cb.specify().derivePurchaseList().max(purchaseCB -> {");
        br.addElement("        purchaseCB.specify().columnPurchaseDatetime(); // OK");
        br.addElement("    }, \"LATEST_PURCHASE_DATETIME\");");
        br.addElement("    cb.query().addSpecifiedDerivedOrderBy_Desc(\"LATEST_PURCHASE_DATETIME\");");
        br.addItem("NotFound Alias Name");
        br.addElement(aliasName);
        final String msg = br.buildExceptionMessage();
        throw new SpecifiedDerivedOrderByAliasNameNotFoundException(msg);
    }

    // -----------------------------------------------------
    //                                Query Derived Referrer
    //                                ----------------------
    public void throwQueryDerivedReferrerInvalidColumnSpecificationException(String function) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The specified the column for (Query)DerivedReferrer was INVALID!");
        br.addItem("Advice");
        br.addElement("You should call specify().column[TargetColumn]() only once.");
        br.addElement("(If your function is count(), the target column should be primary key)");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    cb.query().derivedPurchase().max(purchaseCB -> {");
        br.addElement("        // *NG, it should not be empty");
        br.addElement("    }).greaterEqual(123);");
        br.addElement("  (x):");
        br.addElement("    cb.query().derivedPurchase().max(purchaseCB -> {");
        br.addElement("        // *NG, it should be the only one");
        br.addElement("        purchaseCB.specify().columnPurchaseDatetime();");
        br.addElement("        purchaseCB.specify().columnPurchaseCount();");
        br.addElement("    }).greaterEqual(123);");
        br.addElement("  (o):");
        br.addElement("    cb.query().derivedPurchase().max(purchaseCB -> {");
        br.addElement("        purchaseCB.specify().columnPurchaseDatetime(); // OK");
        br.addElement("    }).greaterEqual(123);");
        br.addItem("Function Method");
        br.addElement(xconvertFunctionToMethod(function));
        final String msg = br.buildExceptionMessage();
        throw new QueryDerivedReferrerInvalidColumnSpecificationException(msg);
    }

    // *see DerivedReferrer@assertDerivedReferrerColumnType()'s comment
    //public void throwQueryDerivedReferrerUnmatchedColumnTypeException(String function, String derivedColumnDbName,
    //        Class<?> derivedColumnType, Object value) {
    //    final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
    //    br.addNotice("Unmatched he type of the specified the column for (Query)DerivedReferrer.");
    //    br.addItem("Advice");
    //    br.addElement("The type of the specified the column unmatched");
    //    br.addElement("with the function or the parameter.");
    //    br.addElement("You should confirm the list as follow:");
    //    br.addElement("    count() : String, Number, Date *with distinct same");
    //    br.addElement("    max()   : String, Number, Date");
    //    br.addElement("    min()   : String, Number, Date");
    //    br.addElement("    sum()   : Number");
    //    br.addElement("    avg()   : Number");
    //    br.addItem("Function Method");
    //    br.addElement(xconvertFunctionToMethod(function));
    //    br.addItem("Derived Column");
    //    br.addElement(derivedColumnDbName + "(" + derivedColumnType.getName() + ")");
    //    br.addItem("Parameter Type");
    //    br.addElement((value != null ? value.getClass() : null));
    //    final String msg = br.buildExceptionMessage();
    //    throw new QueryDerivedReferrerUnmatchedColumnTypeException(msg);
    //}

    public void throwQueryDerivedReferrerSelectAllPossibleException(String function, ConditionQuery subQuery) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The (Query)DerivedReferrer might select all.");
        br.addItem("Advice");
        br.addElement("If you suppress correlation, set your original correlation condition.");
        br.addItem("Function");
        br.addElement(function);
        br.addItem("Referrer");
        br.addElement(subQuery.asTableDbName());
        final String msg = br.buildExceptionMessage();
        throw new QueryDerivedReferrerSelectAllPossibleException(msg);
    }

    // ===================================================================================
    //                                                                               Query
    //                                                                               =====
    public void throwQueryIllegalPurposeException(HpCBPurpose purpose, ConditionBean baseCB) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Bad location to call query().");
        br.addItem("Advice");
        br.addElement("The condition-bean is not allowed to set query.");
        br.addElement("(contains OrScopeQuery and ColumnQuery)");
        br.addElement("Because this is for " + purpose + ".");
        br.addElement("For example:");
        br.addElement("  (x): (ColumnQuery)");
        br.addElement("    cb.columnQuery(colCB -> {");
        br.addElement("        colCB.query().set...();  // *NG");
        br.addElement("        colCB.columnQuery(...);  // *NG");
        br.addElement("        colCB.orScopeQuery(...); // *NG");
        br.addElement("    })...");
        br.addElement("  (x): (VaryingUpdate)");
        br.addElement("    UpdateOption option = new UpdateOption().self(colCB -> {");
        br.addElement("        colCB.query().set...();  // *NG");
        br.addElement("        colCB.columnQuery(...);  // *NG");
        br.addElement("        colCB.orScopeQuery(...); // *NG");
        br.addElement("    });");
        br.addElement("  (o): (ColumnQuery)");
        br.addElement("    cb.columnQuery(colCB -> {");
        br.addElement("        colCB.specify().column...();  // OK");
        br.addElement("    })...");
        br.addElement("  (o): (VaryingUpdate)");
        br.addElement("    UpdateOption option = new UpdateOption().self(colCB -> {");
        br.addElement("        colCB.specify().column...();  // OK");
        br.addElement("    });");
        br.addItem("ConditionBean"); // don't use displaySql because of illegal CB's state
        br.addElement(baseCB.getClass().getName());
        br.addElement("(" + purpose + ")");
        final String msg = br.buildExceptionMessage();
        throw new QueryIllegalPurposeException(msg);
    }

    public void throwQueryThatsBadTimingException(ConditionBean lockedCB) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("That's bad timing for Query.");
        br.addItem("Advice");
        br.addElement("The condition-bean was locked in the timing.");
        br.addElement("For example:");
        br.addElement("(x):");
        br.addElement("  cb.query().existsPurchaseList(purchaseCB -> {");
        br.addElement("      purchaseCB.query().setPurchasePrice_GreaterThan(2000);");
        br.addElement("      cb.query().setBirthdate_GreaterThan(currentDate()); // *NG");
        br.addElement("  });");
        br.addElement("(o):");
        br.addElement("  cb.query().existsPurchaseList(purchaseCB -> {");
        br.addElement("      purchaseCB.query().setPurchasePrice_GreaterThan(2000);");
        br.addElement("  });");
        br.addElement("  cb.query().setBirthdate_GreaterThan(currentDate()); // OK");
        br.addItem("Locked ConditionBean");
        br.addElement(lockedCB.getClass().getName());
        br.addElement("(" + lockedCB.getPurpose() + ")");
        final String msg = br.buildExceptionMessage();
        throw new QueryThatsBadTimingException(msg);
    }

    public void throwQueryAlreadyRegisteredException(ConditionKey key, Object value, ConditionValue cvalue, String columnDbName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Already registered the query. (cannot override it)");
        br.addItem("Advice");
        br.addElement("Overriding query is not allowed as default setting.");
        br.addElement("Mistake? Or do you really want to override it?");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    cb.query().setMemberId_Equal(3);");
        br.addElement("    cb.query().setMemberId_Equal(4); // *NG");
        br.addElement("  (x):");
        br.addElement("    cb.query().setMemberId_Equal(3);");
        br.addElement("    cb.query().setMemberId_Equal(3); // *NG");
        br.addElement("  (o):");
        br.addElement("    cb.query().setMemberId_Equal(3);");
        br.addElement("    cb.query().setMemberAccount_Equal(\"Pixy\"); // OK");
        br.addElement("  (o):");
        br.addElement("    cb.query().setMemberId_Equal(3); // overridden");
        br.addElement("    cb.enableOverridingQuery(() -> {");
        br.addElement("        cb.query().setMemberId_Equal(4); // OK (overrides it)");
        br.addElement("    });");
        br.addItem("Column Name");
        br.addElement(columnDbName);
        br.addItem("Condition Key");
        br.addElement(key);
        br.addItem("Already Registered");
        br.addElement(cvalue);
        br.addItem("New Value");
        br.addElement(value);
        final String msg = br.buildExceptionMessage();
        throw new QueryAlreadyRegisteredException(msg);
    }

    public void throwInvalidQueryRegisteredException(HpInvalidQueryInfo... invalidQueryInfoAry) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Registered the invalid query. (null or empty)");
        br.addItem("Advice");
        br.addElement("The value of null or empty is not allowed to query as default.");
        br.addElement("For example: (when checked by default)");
        br.addElement("  (x):");
        br.addElement("    cb.query().setMemberName_Equal(null); // exception");
        br.addElement("    cb.query().setMemberName_Equal(\"\"); // exception");
        br.addElement("  (o):");
        br.addElement("    cb.query().setMemberName_Equal(\"Pixy\"); // normal query");
        br.addElement("  (o):");
        br.addElement("    cb.ignoreNullOrEmptyQuery();");
        br.addElement("    cb.query().setMemberName_Equal(null); // no condition");
        br.addElement("    cb.query().setMemberName_Equal(\"\"); // no condition");
        br.addItem("Invalid Query");
        for (HpInvalidQueryInfo invalidQueryInfo : invalidQueryInfoAry) {
            br.addElement(invalidQueryInfo.buildDisplay());
        }
        final String msg = br.buildExceptionMessage();
        throw new InvalidQueryRegisteredException(msg);
    }

    public void throwLikeSearchOptionNotFoundException(String colName, String value, DBMeta dbmeta) {
        final String capPropName = initCap(dbmeta.findColumnInfo(colName).getPropertyName());
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the option of like-search. (should not be null)");
        br.addItem("Advice");
        br.addElement("Please confirm your method call:");
        final String beanName = DfTypeUtil.toClassTitle(this);
        final String methodName = "set" + capPropName + "_LikeSearch('" + value + "', likeSearchOption);";
        br.addElement("    " + beanName + "." + methodName);
        final String msg = br.buildExceptionMessage();
        throw new RequiredOptionNotFoundException(msg);
    }

    public void throwOrderByIllegalPurposeException(HpCBPurpose purpose, ConditionBean baseCB, String tableDbName, String columnName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Bad location to call order-by.");
        br.addItem("Advice");
        br.addElement("The condition-bean is not allowed to order.");
        br.addElement("Because this is for " + purpose + ".");
        br.addElement("For example:");
        br.addElement("  (x): (ExistsReferrer)");
        br.addElement("    cb.query().existsPurchase(purchaseCB -> {");
        br.addElement("        purchaseCB.query().addOrderBy...; // *NG");
        br.addElement("    });");
        br.addElement("  (x): (Union)");
        br.addElement("    cb.union(unionCB -> {");
        br.addElement("        unionCB.query().addOrderBy...; // *NG");
        br.addElement("    });");
        br.addElement("  (x): (DerivedReferrer)");
        br.addElement("    cb.specify().derivedPurchase().max(purchaseCB -> {");
        br.addElement("        purchaseCB.query().addOrderBy...; // *NG");
        br.addElement("    });");
        br.addItem("ConditionBean"); // don't use displaySql because of illegal CB's state
        br.addElement(baseCB.getClass().getName());
        br.addElement("(" + baseCB.getPurpose() + ")");
        br.addItem("Order-By Column");
        br.addElement(tableDbName + "." + columnName);
        final String msg = br.buildExceptionMessage();
        throw new OrderByIllegalPurposeException(msg);
    }

    // ===================================================================================
    //                                                                        Column Query
    //                                                                        ============
    public void throwColumnQueryInvalidColumnSpecificationException(ConditionBean baseCB) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The specified the column for ColumnQuery was INVALID!");
        br.addItem("Advice");
        br.addElement("You should call specify().column[TargetColumn]() only once.");
        br.addElement("For example:");
        br.addElement("  (x): (empty)");
        br.addElement("    cb.columnQuery(colCB -> {");
        br.addElement("        // *NG, it should not be empty");
        br.addElement("    }).lessThan...;");
        br.addElement("  (x): (duplicated)");
        br.addElement("    cb.columnQuery(colCB -> {");
        br.addElement("        // *NG, it should be the only one");
        br.addElement("        colCB.specify().columnMemberName();");
        br.addElement("        colCB.specify().columnBirthdate();");
        br.addElement("    }).lessThan...;");
        br.addElement("  (o):");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    cb.columnQuery(colCB -> {");
        br.addElement("        colCB.specify().columnBirthdate();");
        br.addElement("    }).lessThan(colCB -> {");
        br.addElement("        colCB.specify().columnFormalizedDatetime();");
        br.addElement("    });");
        br.addItem("ConditionBean"); // don't use displaySql because of illegal CB's state
        br.addElement(baseCB.getClass().getName());
        br.addElement("(" + baseCB.getPurpose() + ")");
        final String msg = br.buildExceptionMessage();
        throw new ColumnQueryInvalidColumnSpecificationException(msg);
    }

    // -----------------------------------------------------
    //                                       Function Helper
    //                                       ---------------
    protected String xconvertFunctionToMethod(String function) {
        if (function != null && function.contains("(")) { // For example 'count(distinct'
            int index = function.indexOf("(");
            String front = function.substring(0, index);
            if (function.length() > front.length() + "(".length()) {
                String rear = function.substring(index + "(".length());
                function = front + initCap(rear);
            } else {
                function = front;
            }
        }
        return function + "()";
    }

    // ===================================================================================
    //                                                                       OrScope Query
    //                                                                       =============
    public void throwOrScopeQueryAndPartNotOrScopeException(ConditionBean cb) {
        final ExceptionMessageBuilder br = createExceptionMessageBuilder();
        br.addNotice("The or-scope query was not set up.");
        br.addItem("Advice");
        br.addElement("The and-part of or-scope query works only in or-scope query.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    cb.orScopeQueryAndPart(andCB -> { // *NG");
        br.addElement("        ...");
        br.addElement("    });");
        br.addElement("  (o):");
        br.addElement("    cb.orScopeQuery(orCB -> {");
        br.addElement("        orCB.orScopeQueryAndPart(andCB -> {");
        br.addElement("            andCB.query().set...();");
        br.addElement("            andCB.query().set...();");
        br.addElement("        });");
        br.addElement("    });");
        br.addItem("ConditionBean"); // don't use displaySql because of illegal CB's state
        br.addElement(cb.getClass().getName());
        br.addElement("(" + cb.getPurpose() + ")");
        final String msg = br.buildExceptionMessage();
        throw new OrScopeQueryAndPartNotOrScopeException(msg);
    }

    public void throwOrScopeQueryAndPartAlreadySetupException(ConditionBean cb) {
        final ExceptionMessageBuilder br = createExceptionMessageBuilder();
        br.addNotice("The and-part of or-scope has already been set up.");
        br.addItem("Advice");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    cb.orScopeQuery(orCB -> {");
        br.addElement("        orCB.orScopeQueryAndPart(andCB -> {");
        br.addElement("            andCB.orScopeQueryAndPart(andCB -> {"); // *NG");
        br.addElement("                ...");
        br.addElement("            });");
        br.addElement("        });");
        br.addElement("    });");
        br.addItem("ConditionBean"); // don't use displaySql because of illegal CB's state
        br.addElement(cb.getClass().getName());
        br.addElement("(" + cb.getPurpose() + ")");
        final String msg = br.buildExceptionMessage();
        throw new OrScopeQueryAndPartAlreadySetupException(msg);
    }

    // ===================================================================================
    //                                                                    Scalar Condition
    //                                                                    ================
    public void throwScalarConditionInvalidColumnSpecificationException(String function) {
        final ExceptionMessageBuilder br = createExceptionMessageBuilder();
        br.addNotice("The specified the column for ScalarCondition was invalid.");
        br.addItem("Advice");
        br.addElement("You should call specify().column[TargetColumn]() only once.");
        br.addElement("(If your function is count(), the target column should be primary key)");
        br.addElement("For example:");
        br.addElement("  (x): (empty)");
        br.addElement("    cb.query().scalar_Equal().max(memberCB -> {");
        br.addElement("        // *NG, it should not be empty");
        br.addElement("    });");
        br.addElement("");
        br.addElement("  (x): (duplicated)");
        br.addElement("    cb.query().scalar_Equal().max(memberCB -> {");
        br.addElement("        // *NG, it should be the only one");
        br.addElement("        memberCB.specify().columnBirthdate();");
        br.addElement("        memberCB.specify().columnMemberName();");
        br.addElement("    });");
        br.addElement("");
        br.addElement("  (o):");
        br.addElement("    cb.query().scalar_Equal().max(memberCB -> {");
        br.addElement("        memberCB.specify().columnBirthdate(); // OK");
        br.addElement("    });");
        br.addItem("Function Method");
        br.addElement(xconvertFunctionToMethod(function));
        final String msg = br.buildExceptionMessage();
        throw new ScalarConditionInvalidColumnSpecificationException(msg);
    }

    public void throwScalarConditionPartitionByInvalidColumnSpecificationException(String function) {
        final ExceptionMessageBuilder br = createExceptionMessageBuilder();
        br.addNotice("The specified the column for ScalarCondition PartitionBy was invalid.");
        br.addItem("Advice");
        br.addElement("You should call specify().column[TargetColumn]() only once.");
        br.addElement("For example:");
        br.addElement("  (x): (empty)");
        br.addElement("    cb.query().scalar_Equal().max(memberCB -> {");
        br.addElement("        memberCB.specify().columnBirthdate()");
        br.addElement("    }).partitionBy(colCB -> {");
        br.addElement("        // *NG, it should not be empty");
        br.addElement("    });");
        br.addElement("");
        br.addElement("  (x): (duplicated)");
        br.addElement("    cb.query().scalar_Equal().max(memberCB -> {");
        br.addElement("        memberCB.specify().columnBirthdate()");
        br.addElement("    }).partitionBy(colCB -> {");
        br.addElement("        // *NG, it should be the only one");
        br.addElement("        colCB.specify().columnBirthdate();");
        br.addElement("        colCB.specify().columnMemberName();");
        br.addElement("    });");
        br.addElement("");
        br.addElement("  (o):");
        br.addElement("    cb.query().scalar_Equal().max(memberCB -> {");
        br.addElement("        memberCB.specify().columnBirthdate()");
        br.addElement("    }).partitionBy(colCB -> {");
        br.addElement("        colCB.specify().columnMemberStatusCode(); // OK");
        br.addElement("    });");
        br.addItem("Function Method");
        br.addElement(xconvertFunctionToMethod(function));
        final String msg = br.buildExceptionMessage();
        throw new ScalarConditionInvalidColumnSpecificationException(msg);
    }

    public void throwScalarConditionUnmatchedColumnTypeException(String function, String deriveColumnName, Class<?> deriveColumnType) {
        final ExceptionMessageBuilder br = createExceptionMessageBuilder();
        br.addNotice("The type of the specified the column unmatched with the function.");
        br.addItem("Advice");
        br.addElement("You should confirm the list as follow:");
        br.addElement("    max()   : String, Number, Date");
        br.addElement("    min()   : String, Number, Date");
        br.addElement("    sum()   : Number");
        br.addElement("    avg()   : Number");
        br.addItem("Function Method");
        br.addElement(xconvertFunctionToMethod(function));
        br.addItem("Derive Column");
        br.addElement(deriveColumnName + "(" + deriveColumnType.getName() + ")");
        final String msg = br.buildExceptionMessage();
        throw new ScalarConditionUnmatchedColumnTypeException(msg);
    }

    // ===================================================================================
    //                                                                              Paging
    //                                                                              ======
    public void throwPagingPageSizeNotPlusException(ConditionBean cb, int pageSize, int pageNumber) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Page size for paging should not be minus or zero.");
        br.addItem("Advice");
        br.addElement("Confirm the value of your parameter 'pageSize'.");
        br.addElement("The first parameter of paging() should be a plus value.");
        br.addElement("For example:");
        br.addElement("  (x): cb.paging(0, 1);");
        br.addElement("  (x): cb.paging(-3, 2);");
        br.addElement("  (o): cb.paging(20, 3);");
        br.addItem("ConditionBean");
        br.addElement(cb.getClass().getName());
        br.addElement("(" + cb.getPurpose() + ")");
        br.addItem("Page Size");
        br.addElement(pageSize);
        br.addItem("Page Number");
        br.addElement(pageNumber);
        final String msg = br.buildExceptionMessage();
        throw new PagingPageSizeNotPlusException(msg);
    }

    // ===================================================================================
    //                                                                      FixedCondition
    //                                                                      ==============
    public void throwFixedConditionParameterNotFoundException(String tableDbName, String property, String fixedCondition,
            Map<String, Object> parameterMap) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the required parameter for the fixed condition.");
        br.addItem("Advice");
        br.addElement("Make sure your parameters to make the BizOneToOne relation.");
        br.addElement("For example:");
        br.addElement("  (x): cb.setupSelect_MemberAddressAsValid(null);");
        br.addElement("  (x): (ColumnQuery)");
        br.addElement("    cb.columnQuery(colCB -> {");
        br.addElement("        ...");
        br.addElement("    }).lessThan(colCB -> {");
        br.addElement("        colCB.specify().specifyMemberAddressAsValid().columnAddress();");
        br.addElement("    })");
        br.addElement("  (x): (DerivedReferrer)");
        br.addElement("    cb.specify().derivedMemberList(memberCB -> {");
        br.addElement("        memberCB.specify().specifyMemberAddressAsValid().columnAddress();");
        br.addElement("    });");
        br.addElement("  (o): cb.setupSelect_MemberAddressAsValid(currentDate());");
        br.addElement("  (o):");
        br.addElement("    Date current = currentDate();");
        br.addElement("    cb.setupSelect_MemberAddressAsValid(current);");
        br.addElement("    cb.query().queryMemberAddressAsValid(current).set...();");
        br.addElement("  (o): (SpecifyColumn)");
        br.addElement("    cb.setupSelect_MemberAddressAsValid(currentDate());");
        br.addElement("    cb.specify().specifyMemberAddressAsValid().columnAddress();");
        br.addElement("  (o): (SpecifyColumn)");
        br.addElement("    Date current = currentDate();");
        br.addElement("    cb.setupSelect_MemberAddressAsValid(current);");
        br.addElement("    cb.specify().specifyMemberAddressAsValid(current).columnAddress();");
        br.addElement("  (o): (ColumnQuery)");
        br.addElement("    cb.columnQuery(colCB -> {");
        br.addElement("        ...");
        br.addElement("    }).lessThan(colCB -> {");
        br.addElement("        colCB.specify().specifyMemberAddressAsValid(currentDate()).columnAddress();");
        br.addElement("    });");
        br.addElement("  (o): (DerivedReferrer)");
        br.addElement("    cb.specify().derivedMemberList(memberCB -> {");
        br.addElement("        memberCB.specify().specifyMemberAddressAsValid(currentDate()).columnAddress();");
        br.addElement("    });");
        br.addItem("Local Table");
        br.addElement(tableDbName);
        br.addItem("Relation Property");
        br.addElement(property);
        br.addItem("FixedCondition");
        br.addElement(fixedCondition);
        br.addItem("Parameters");
        br.addElement(parameterMap);
        final String msg = br.buildExceptionMessage();
        throw new FixedConditionParameterNotFoundException(msg);
    }

    // ===================================================================================
    //                                                                        Manual Order
    //                                                                        ============
    public void throwManualOrderNotFoundOrderByException(ConditionBean baseCB, ManualOrderOption moOp) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found order-by element for the ManualOrder.");
        br.addItem("Advice");
        br.addElement("Make sure your implementation:");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    cb.query().withManualOrder(op -> { // *NG");
        br.addElement("        op.when_LessEqual(...);");
        br.addElement("    });");
        br.addElement("  (o):");
        br.addElement("    cb.query().addOrderBy_Birthdate_Asc().withManualOrder(op -> { // OK");
        br.addElement("        op.when_LessEqual(...);");
        br.addElement("    });");
        br.addItem("ConditionBean"); // don't use displaySql because of illegal CB's state
        br.addElement(baseCB != null ? baseCB.getClass().getName() : baseCB); // check just in case
        br.addItem("ManualOrderOption");
        br.addElement(moOp);
        final String msg = br.buildExceptionMessage();
        throw new IllegalConditionBeanOperationException(msg);
    }

    public void throwManualOrderSameBeanAlreadyExistsException(ConditionBean baseCB, ManualOrderOption existingMoOp,
            OrderByElement existingOrder, ManualOrderOption specifiedMob, OrderByElement specifiedOrder) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The same ManualOrder option with other columns was registered.");
        br.addItem("Advice");
        br.addElement("You can use manual-order-bean one time.");
        br.addElement("Make sure your implementation:");
        br.addElement("For example:");
        br.addElement("  (x):"); // basically not comes here if lambda style so no needs to migrate this message 
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    ManualOrderOption mob = new ManualOrderOption();");
        br.addElement("    mob.when_LessEqual(...);");
        br.addElement("    cb.query().addOrderBy_Birthdate_Asc().withManualOrder(mob);");
        br.addElement("    cb.query().addOrderBy_MemberId_Asc().withManualOrder(mob); // *NG");
        br.addElement("  (o):");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    ManualOrderOption birthMob = new ManualOrderOption();");
        br.addElement("    birthMob.when_LessEqual(...);");
        br.addElement("    cb.query().addOrderBy_Birthdate_Asc().withManualOrder(birthMob);");
        br.addElement("    ManualOrderOption idMob = new ManualOrderOption();");
        br.addElement("    idMob.when_LessEqual(...);");
        br.addElement("    cb.query().addOrderBy_MemberId_Asc().withManualOrder(idMob); // OK");
        br.addItem("ConditionBean"); // don't use displaySql because of illegal CB's state
        br.addElement(baseCB != null ? baseCB.getClass().getName() : baseCB); // check just in case
        br.addItem("Existing Option");
        br.addElement(existingMoOp);
        br.addElement(existingOrder);
        br.addItem("Specified Bean");
        br.addElement(specifiedMob);
        br.addElement(specifiedOrder);
        final String msg = br.buildExceptionMessage();
        throw new IllegalConditionBeanOperationException(msg);
    }

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    public void throwOptionThatsBadTimingException(ConditionBean lockedCB, String optionName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("That's bad timing for Option.");
        br.addItem("Advice");
        br.addElement("The condition-bean was locked in the timing.");
        br.addElement("For example:");
        br.addElement("(x):");
        br.addElement("  cb.query().existsPurchaseList(purchaseCB -> {");
        br.addElement("      cb.ignoreNullOrEmptyQuery(); // *NG");
        br.addElement("      purchaseCB.query().setPurchasePrice_GreaterThan(2000);");
        br.addElement("  });");
        br.addElement("(o):");
        br.addElement("  cb.ignoreNullOrEmptyQuery(); // OK");
        br.addElement("  cb.query().existsPurchaseList(purchaseCB -> {");
        br.addElement("      purchaseCB.query().setPurchasePrice_GreaterThan(2000);");
        br.addElement("  });");
        br.addElement("(o):");
        br.addElement("  cb.query().existsPurchaseList(purchaseCB -> {");
        br.addElement("      purchaseCB.ignoreNullOrEmptyQuery(); // OK");
        br.addElement("      purchaseCB.query().setPurchasePrice_GreaterThan(2000);");
        br.addElement("  });");
        br.addItem("Locked ConditionBean"); // don't use displaySql because of illegal CB's state
        br.addElement(lockedCB.getClass().getName());
        br.addElement("(" + lockedCB.getPurpose() + ")");
        br.addItem("Called Option");
        br.addElement(optionName);
        final String msg = br.buildExceptionMessage();
        throw new OptionThatsBadTimingException(msg);
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    /**
     * Get the value of line separator.
     * @return The value of line separator. (NotNull)
     */
    protected String ln() {
        return DBFluteSystem.ln();
    }

    protected String initCap(String str) {
        return Srl.initCap(str);
    }

    protected String initUncap(String str) {
        return Srl.initUncap(str);
    }

    // ===================================================================================
    //                                                                    Exception Helper
    //                                                                    ================
    protected ExceptionMessageBuilder createExceptionMessageBuilder() {
        return new ExceptionMessageBuilder();
    }
}
