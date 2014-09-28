/*
 * Copyright 2004-2014 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.exception.thrower;

import java.util.Collection;
import java.util.Map;

import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.cbean.ConditionQuery;
import org.seasar.dbflute.cbean.ManualOrderBean;
import org.seasar.dbflute.cbean.chelper.HpCBPurpose;
import org.seasar.dbflute.cbean.chelper.HpInvalidQueryInfo;
import org.seasar.dbflute.cbean.chelper.HpSpecifiedColumn;
import org.seasar.dbflute.cbean.ckey.ConditionKey;
import org.seasar.dbflute.cbean.cvalue.ConditionValue;
import org.seasar.dbflute.cbean.sqlclause.orderby.OrderByElement;
import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.exception.ColumnQueryInvalidColumnSpecificationException;
import org.seasar.dbflute.exception.FixedConditionParameterNotFoundException;
import org.seasar.dbflute.exception.IllegalConditionBeanOperationException;
import org.seasar.dbflute.exception.InvalidQueryRegisteredException;
import org.seasar.dbflute.exception.OrScopeQueryAndPartAlreadySetupException;
import org.seasar.dbflute.exception.OrScopeQueryAndPartNotOrScopeException;
import org.seasar.dbflute.exception.OrderByIllegalPurposeException;
import org.seasar.dbflute.exception.PagingPageSizeNotPlusException;
import org.seasar.dbflute.exception.QueryAlreadyRegisteredException;
import org.seasar.dbflute.exception.QueryDerivedReferrerInvalidColumnSpecificationException;
import org.seasar.dbflute.exception.QueryDerivedReferrerSelectAllPossibleException;
import org.seasar.dbflute.exception.QueryDerivedReferrerUnmatchedColumnTypeException;
import org.seasar.dbflute.exception.QueryIllegalPurposeException;
import org.seasar.dbflute.exception.QueryThatsBadTimingException;
import org.seasar.dbflute.exception.RequiredOptionNotFoundException;
import org.seasar.dbflute.exception.ScalarConditionInvalidColumnSpecificationException;
import org.seasar.dbflute.exception.ScalarConditionUnmatchedColumnTypeException;
import org.seasar.dbflute.exception.ScalarSelectInvalidColumnSpecificationException;
import org.seasar.dbflute.exception.SetupSelectIllegalPurposeException;
import org.seasar.dbflute.exception.SetupSelectThatsBadTimingException;
import org.seasar.dbflute.exception.SpecifiedDerivedOrderByAliasNameNotFoundException;
import org.seasar.dbflute.exception.SpecifyColumnAlreadySpecifiedEveryColumnException;
import org.seasar.dbflute.exception.SpecifyColumnAlreadySpecifiedExceptColumnException;
import org.seasar.dbflute.exception.SpecifyColumnNotSetupSelectColumnException;
import org.seasar.dbflute.exception.SpecifyColumnTwoOrMoreColumnException;
import org.seasar.dbflute.exception.SpecifyColumnWithDerivedReferrerException;
import org.seasar.dbflute.exception.SpecifyDerivedReferrerEntityPropertyNotFoundException;
import org.seasar.dbflute.exception.SpecifyDerivedReferrerIllegalPurposeException;
import org.seasar.dbflute.exception.SpecifyDerivedReferrerInvalidAliasNameException;
import org.seasar.dbflute.exception.SpecifyDerivedReferrerInvalidColumnSpecificationException;
import org.seasar.dbflute.exception.SpecifyDerivedReferrerSelectAllPossibleException;
import org.seasar.dbflute.exception.SpecifyDerivedReferrerTwoOrMoreException;
import org.seasar.dbflute.exception.SpecifyDerivedReferrerUnmatchedColumnTypeException;
import org.seasar.dbflute.exception.SpecifyEveryColumnAlreadySpecifiedColumnException;
import org.seasar.dbflute.exception.SpecifyExceptColumnAlreadySpecifiedColumnException;
import org.seasar.dbflute.exception.SpecifyIllegalPurposeException;
import org.seasar.dbflute.exception.SpecifyRelationIllegalPurposeException;
import org.seasar.dbflute.exception.SpecifyThatsBadTimingException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.resource.DBFluteSystem;
import org.seasar.dbflute.util.DfTypeUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class ConditionBeanExceptionThrower {

    // ===================================================================================
    //                                                                       Set up Select
    //                                                                       =============
    public void throwSetupSelectIllegalPurposeException(HpCBPurpose purpose, ConditionBean baseCB,
            String foreignPropertyName) {
        final ExceptionMessageBuilder br = createExceptionMessageBuilder();
        br.addNotice("The purpose was illegal for SetupSelect.");
        br.addItem("Advice");
        br.addElement("This condition-bean is not allowed to set up select.");
        br.addElement("Because this is for " + purpose + ".");
        br.addElement("For example:");
        br.addElement("  (x): (ExistsReferrer)");
        br.addElement("    cb.query().existsXxxList(new SubQuery<PurchaseCB>() {");
        br.addElement("        public void query(PurchaseCB subCB) {");
        br.addElement("            subCB.setupSelect_Product(); // *NG");
        br.addElement("        }");
        br.addElement("    });");
        br.addElement("  (x): (Union)");
        br.addElement("    cb.union(new UnionQuery<MemberCB>() {");
        br.addElement("        public void query(MemberCB unionCB) {");
        br.addElement("            unionCB.setupSelect_MemberStatus(); // *NG");
        br.addElement("        }");
        br.addElement("    });");
        br.addElement("  (o): (Normal Use)");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    cb.setupSelect_MemberStatus(); // OK");
        br.addItem("ConditionBean");
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
        br.addElement("(x):");
        br.addElement("  MemberCB cb = new MemberCB()");
        br.addElement("  cb.setupSelect_MemberStatus();");
        br.addElement("  ListResultBean<Member> memberList = memberBhv.selectList(cb);");
        br.addElement("  memberBhv.loadPurchaseList(cb, referrerCB -> {");
        br.addElement("      cb.setupSelect_MemberStatus(); // *NG");
        br.addElement("  });");
        br.addElement("(o):");
        br.addElement("  MemberCB cb = new MemberCB()");
        br.addElement("  cb.setupSelect_MemberStatus();");
        br.addElement("  ListResultBean<Member> memberList = memberBhv.selectList(cb);");
        br.addElement("  memberBhv.loadPurchaseList(cb, referrerCB -> {");
        br.addElement("      referrerCB.setupSelect_Product(); // OK");
        br.addElement("  });");
        Class<? extends ConditionBean> cbType = lockedCB.getClass();
        br.addItem("Locked ConditionBean");
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
    //    br.addItem("ConditionBean");
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
        br.addNotice("The purpose was illegal to specify.");
        br.addItem("Advice");
        br.addElement("This condition-bean is not allowed to specify.");
        br.addElement("Because this is for " + purpose + ".");
        br.addElement("For example:");
        br.addElement("  (x): (ExistsReferrer)");
        br.addElement("    cb.query().existsPurchaseList(new SubQuery<PurchaseCB>() {");
        br.addElement("        public void query(PurchaseCB subCB) {");
        br.addElement("            subCB.specify()... // *NG");
        br.addElement("        }");
        br.addElement("    });");
        br.addElement("  (x): (Union)");
        br.addElement("    cb.union(new UnionQuery<PurchaseCB>() {");
        br.addElement("        public void query(PurchaseCB unionCB) {");
        br.addElement("            unionCB.specify()... // *NG");
        br.addElement("        }");
        br.addElement("    });");
        br.addElement("  (o): (ExistsReferrer)");
        br.addElement("    cb.query().existsPurchaseList(new SubQuery<PurchaseCB>() {");
        br.addElement("        public void query(PurchaseCB subCB) {");
        br.addElement("            subCB.query().setPurchaseCount_GreaterEqual(3); // OK");
        br.addElement("        }");
        br.addElement("    });");
        // don't use displaySql because of illegal CB's state
        br.addItem("ConditionBean");
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
        br.addElement("(x):");
        br.addElement("  MemberCB cb = new MemberCB()");
        br.addElement("  cb.specify().derivedPurchaseList().max(subCB -> {");
        br.addElement("      cb.specify().columnMemberId(); // *NG");
        br.addElement("      subCB.query().setPurchasePrice_GreaterThan(2000);");
        br.addElement("  }, ...);");
        br.addElement("(o):");
        br.addElement("  MemberCB cb = new MemberCB()");
        br.addElement("  cb.specify().derivedPurchaseList().max(subCB -> {");
        br.addElement("      subCB.specify().columnMemberId(); // *OK");
        br.addElement("      subCB.query().setPurchasePrice_GreaterThan(2000);");
        br.addElement("  }, ...);");
        br.addItem("Locked ConditionBean");
        br.addElement(lockedCB.getClass().getName());
        br.addElement("(" + lockedCB.getPurpose() + ")");
        final String msg = br.buildExceptionMessage();
        throw new SpecifyThatsBadTimingException(msg);
    }

    public void throwSpecifyColumnTwoOrMoreColumnException(HpCBPurpose purpose, ConditionBean baseCB, String columnName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("You specified two or more columns!");
        br.addItem("Advice");
        br.addElement("This condition-bean is not allowed to specify two or more columns.");
        br.addElement("Because the conditoin-bean is for " + purpose + ".");
        br.addElement("For example:");
        br.addElement("  (x): (DerivedReferrer)");
        br.addElement("    cb.specify().derivedPurchaseList().max(new SubQuery<PurchaseCB>() {");
        br.addElement("        public void query(PurchaseCB subCB) {");
        br.addElement("            subCB.specify().columnPurchaseCount();");
        br.addElement("            subCB.specify().columnPurchasePrice(); // *NG");
        br.addElement("        }");
        br.addElement("    });");
        br.addElement("  (x): (ColumnQuery)");
        br.addElement("    cb.columnQuery(new SpecifyQuery<MemberCB>() {");
        br.addElement("        public void specify(MemberCB cb) {");
        br.addElement("            cb.specify().columnMemberName();");
        br.addElement("            cb.specify().columnBirthdate(); // *NG");
        br.addElement("        }");
        br.addElement("    })...");
        br.addElement("  (o): (DerivedReferrer)");
        br.addElement("    cb.specify().derivedPurchaseList().max(new SubQuery<PurchaseCB>() {");
        br.addElement("        public void query(PurchaseCB subCB) {");
        br.addElement("            subCB.specify().columnPurchaseCount();");
        br.addElement("        }");
        br.addElement("    });");
        // don't use displaySql because of illegal CB's state
        br.addItem("ConditionBean");
        br.addElement(baseCB.getClass().getName());
        br.addElement("(" + baseCB.getPurpose() + ")");
        br.addItem("Specified Column");
        br.addElement(baseCB.getTableDbName() + "." + columnName);
        final String msg = br.buildExceptionMessage();
        throw new SpecifyColumnTwoOrMoreColumnException(msg);
    }

    public void throwSpecifyColumnNotSetupSelectColumnException(ConditionBean baseCB, String columnName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("You specified the column that had not been set up!");
        br.addItem("Advice");
        br.addElement("You should call setupSelect_[ForeignTable]()");
        br.addElement("before calling specify[ForeignTable]().column[TargetColumn]().");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    cb.specify().specifyMemberStatus().columnMemberStatusName(); // *NG");
        br.addElement("  (o):");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    cb.setupSelect_MemberStatus(); // *Point");
        br.addElement("    cb.specify().specifyMemberStatus().columnMemberStatusName();");
        // don't use displaySql because of illegal CB's state
        br.addItem("ConditionBean");
        br.addElement(baseCB.getClass().getName());
        br.addElement("(" + baseCB.getPurpose() + ")");
        br.addItem("Specified Column");
        br.addElement(baseCB.getTableDbName() + "." + columnName);
        final String msg = br.buildExceptionMessage();
        throw new SpecifyColumnNotSetupSelectColumnException(msg);
    }

    public void throwSpecifyColumnWithDerivedReferrerException(HpCBPurpose purpose, ConditionBean baseCB,
            String columnName, String referrerName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("You specified both SpecifyColumn and (Specify)DerivedReferrer!");
        br.addItem("Advice");
        br.addElement("This condition-bean is not allowed to specify both functions.");
        br.addElement("Because the conditoin-bean is for " + purpose + ".");
        br.addElement("For example:");
        br.addElement("  (x): (ColumnQuery)");
        br.addElement("    cb.columnQuery(new SpecifyQuery<MemberCB> {");
        br.addElement("        public void query(MemberCB cb) {");
        br.addElement("            cb.specify().columnBirthdate();");
        br.addElement("            cb.specify().derivedPurchaseList().max(...); // *NG");
        br.addElement("        }");
        br.addElement("    }).greaterEqual(...);");
        br.addElement("  (o): (ColumnQuery)");
        br.addElement("    cb.columnQuery(new SpecifyQuery<MemberCB> {");
        br.addElement("        public void query(MemberCB cb) {");
        br.addElement("            cb.specify().columnBirthdate(); // OK");
        br.addElement("        }");
        br.addElement("    }).greaterEqual(...);");
        br.addElement("  (o): (ColumnQuery)");
        br.addElement("    cb.columnQuery(new SpecifyQuery<MemberCB> {");
        br.addElement("        public void query(MemberCB cb) {");
        br.addElement("            cb.specify().derivedPurchaseList().max(...); // OK");
        br.addElement("        }");
        br.addElement("    }).greaterEqual(...);");
        // don't use displaySql because of illegal CB's state
        br.addItem("ConditionBean");
        br.addElement(baseCB.getClass().getName());
        br.addElement("(" + baseCB.getPurpose() + ")");
        br.addItem("Specified Column");
        br.addElement(baseCB.getTableDbName() + "." + columnName);
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
        br.addElement("    memberBhv.batchUpdate(memberList, new SpecifyQuery<MemberCB>() {");
        br.addElement("        public void specify(MemberCB cb) {");
        br.addElement("            cb.specify().everyColumn();");
        br.addElement("            cb.specify().columnMemberName(); // *No");
        br.addElement("        }");
        br.addElement("    }");
        br.addElement("  (o):");
        br.addElement("    memberBhv.batchUpdate(memberList, new SpecifyQuery<MemberCB>() {");
        br.addElement("        public void specify(MemberCB cb) {");
        br.addElement("            cb.specify().everyColumn();");
        br.addElement("        }");
        br.addElement("    }");
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
        br.addElement("    cb.specify().exceptRecordMetaColumn();");
        br.addElement("    cb.specify().columnMemberName(); // *No");
        br.addElement("    ... = memberBhv.selectList(cb);");
        br.addElement("  (o):");
        br.addElement("    cb.specify().exceptRecordMetaColumn();");
        br.addElement("    ... = memberBhv.selectList(cb);");
        br.addElement("  (o):");
        br.addElement("    cb.specify().columnMemberName();");
        br.addElement("    ... = memberBhv.selectList(cb);");
        br.addItem("Base Table");
        br.addElement(tableDbName);
        br.addItem("Specified Column");
        br.addElement(columnName);
        final String msg = br.buildExceptionMessage();
        throw new SpecifyColumnAlreadySpecifiedExceptColumnException(msg);
    }

    public void throwSpecifyEveryColumnAlreadySpecifiedColumnException(String tableDbName,
            Map<String, HpSpecifiedColumn> specifiedColumnMap) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The SpecifyEveryColumn is specified after SpecifyColumn.");
        br.addItem("Advice");
        br.addElement("You cannot specify columns with every column.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    memberBhv.batchUpdate(memberList, new SpecifyQuery<MemberCB>() {");
        br.addElement("        public void specify(MemberCB cb) {");
        br.addElement("            cb.specify().columnMemberName();");
        br.addElement("            cb.specify().everyColumn(); // *No");
        br.addElement("        }");
        br.addElement("    }");
        br.addElement("  (o):");
        br.addElement("    memberBhv.batchUpdate(memberList, new SpecifyQuery<MemberCB>() {");
        br.addElement("        public void specify(MemberCB cb) {");
        br.addElement("            cb.specify().everyColumn();");
        br.addElement("        }");
        br.addElement("    }");
        br.addItem("Base Table");
        br.addElement(tableDbName);
        if (specifiedColumnMap != null) { // basically true
            br.addItem("Specified Column");
            final Collection<HpSpecifiedColumn> columnList = specifiedColumnMap.values();
            for (HpSpecifiedColumn column : columnList) {
                br.addElement(column);
            }
        }
        final String msg = br.buildExceptionMessage();
        throw new SpecifyEveryColumnAlreadySpecifiedColumnException(msg);
    }

    public void throwSpecifyExceptColumnAlreadySpecifiedColumnException(String tableDbName,
            Map<String, HpSpecifiedColumn> specifiedColumnMap) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The SpecifyExceptColumn is specified after SpecifyColumn.");
        br.addItem("Advice");
        br.addElement("You cannot specify columns with except columns.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    cb.specify().columnMemberName();");
        br.addElement("    cb.specify().exceptRecordMetaColumn(); // *No");
        br.addElement("    ... = memberBhv.selectList(cb);");
        br.addElement("  (o):");
        br.addElement("    cb.specify().exceptRecordMetaColumn();");
        br.addElement("    ... = memberBhv.selectList(cb);");
        br.addElement("  (o):");
        br.addElement("    cb.specify().columnMemberName();");
        br.addElement("    ... = memberBhv.selectList(cb);");
        br.addItem("Base Table");
        br.addElement(tableDbName);
        if (specifiedColumnMap != null) { // basically true
            br.addItem("Specified Column");
            final Collection<HpSpecifiedColumn> columnList = specifiedColumnMap.values();
            for (HpSpecifiedColumn column : columnList) {
                br.addElement(column);
            }
        }
        final String msg = br.buildExceptionMessage();
        throw new SpecifyExceptColumnAlreadySpecifiedColumnException(msg);
    }

    public void throwSpecifyRelationIllegalPurposeException(HpCBPurpose purpose, ConditionBean baseCB,
            String relationName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("You specified the relation in the purpose that is not allowed to do it.");
        br.addItem("Advice");
        br.addElement("This condition-bean is not allowed to specify a relation.");
        br.addElement("Because this is for " + purpose + ".");
        br.addElement("For example:");
        br.addElement("  (x): (ScalarSelect)");
        br.addElement("    memberBhv.scalarSelect(Date.class).max(new ScalarQuery<MemberCB>() {");
        br.addElement("        public void query(MemberCB cb) {");
        br.addElement("            cb.specify().specifyMemberStatus().col.. // *NG");
        br.addElement("        }");
        br.addElement("    });");
        br.addElement("  (x): (ScalarCondition)");
        br.addElement("    cb.query().scalar_Equal().max(Date.class).max(new SubQuery<MemberCB>() {");
        br.addElement("        public void query(MemberCB subCB) {");
        br.addElement("            subCB.specify().specifyMemberStatusName().col..; // *NG");
        br.addElement("        }");
        br.addElement("    });");
        br.addElement("  (x): (VaryingUpdate)");
        br.addElement("    UpdateOption option = new UpdateOption().self(new SpecifyQuery<MemberCB>() {");
        br.addElement("        public void specify(MemberCB cb) {");
        br.addElement("            cb.specify().specifyMemberStatus().col.. // *NG");
        br.addElement("        }");
        br.addElement("    });");
        br.addElement("  (o): (ScalarSelect)");
        br.addElement("    memberBhv.scalarSelect(Date.class).max(new ScalarQuery<MemberCB>() {");
        br.addElement("        public void query(MemberCB cb) {");
        br.addElement("            cb.specify().columnBirthdate(); // OK");
        br.addElement("        }");
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

    public void throwSpecifyDerivedReferrerIllegalPurposeException(HpCBPurpose purpose, ConditionBean baseCB,
            String referrerName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The purpose was illegal for derived-referrer specification.");
        br.addItem("Advice");
        br.addElement("This condition-bean is not allowed to specify a derived referrer.");
        br.addElement("Because this is for " + purpose + ".");
        br.addElement("For example:");
        br.addElement("  (x): (ScalaCondition)");
        br.addElement("    cb.query().scalar_Equal().max(new SubQuery<PurchaseCB>() {");
        br.addElement("        public void query(PurchaseCB subCB) {");
        br.addElement("            subCB.specify().derivedPurchaseList()...; // *NG");
        br.addElement("        }");
        br.addElement("    });");
        br.addElement("  (o): (ScalaCondition)");
        br.addElement("    cb.query().scalar_Equal().max(new SubQuery<PurchaseCB>() {");
        br.addElement("        public void query(PurchaseCB subCB) {");
        br.addElement("            subCB.specify().columnPurchaseCount(); // OK");
        br.addElement("        }");
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

    public void throwSpecifyDerivedReferrerTwoOrMoreException(HpCBPurpose purpose, ConditionBean baseCB,
            String referrerName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The two-or-more derived-referrers was specifed.");
        br.addItem("Advice");
        br.addElement("This condition-bean is not allowed to specify two-or-more derived referrers.");
        br.addElement("Because this is for " + purpose + ".");
        br.addElement("For example:");
        br.addElement("  (x): (ColumnQuery)");
        br.addElement("    cb.columnQuery(new SpecifyQuery<MemberCB> {");
        br.addElement("        public void query(MemberCB cb) {");
        br.addElement("            cb.specify().derivedPurchaseList().max(...);");
        br.addElement("            cb.specify().derivedPurchaseList().max(...); // *NG");
        br.addElement("        }");
        br.addElement("    }).greaterEqual(...);");
        br.addElement("  (o): (ColumnQuery)");
        br.addElement("    cb.columnQuery(new SpecifyQuery<MemberCB> {");
        br.addElement("        public void query(MemberCB cb) {");
        br.addElement("            cb.specify().derivedPurchaseList().max(...); // OK");
        br.addElement("        }");
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
        br.addElement("    memberBhv.scalarSelect(Date.class).max(new ScalarQuery<MemberCB>() {");
        br.addElement("        public void query(MemberCB cb) {");
        br.addElement("            // *NG, it should not be empty");
        br.addElement("        }");
        br.addElement("    });");
        br.addElement("");
        br.addElement("  (x): (duplicated)");
        br.addElement("    memberBhv.scalarSelect(Date.class).max(new ScalarQuery<MemberCB>() {");
        br.addElement("        public void query(MemberCB cb) {");
        br.addElement("            // *NG, it should be the only one");
        br.addElement("            cb.specify().columnMemberBirthday();");
        br.addElement("            cb.specify().columnRegisterDatetime();");
        br.addElement("        }");
        br.addElement("    });");
        br.addElement("");
        br.addElement("  (o):");
        br.addElement("    memberBhv.scalarSelect(Date.class).max(new ScalarQuery<MemberCB>() {");
        br.addElement("        public void query(MemberCB cb) {");
        br.addElement("            cb.specify().columnMemberBirthday(); // OK");
        br.addElement("        }");
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
        br.addNotice("The alias name for specify derived-referrer was INVALID.");
        br.addItem("Advice");
        br.addElement("You should set valid alias name. {NotNull, NotEmpty}");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    cb.specify().derivePurchaseList().max(new SubQuery<PurchaseCB>() {");
        br.addElement("        public void query(PurchaseCB subCB) {");
        br.addElement("            subCB.specify().columnPurchaseDatetime();");
        br.addElement("        }");
        br.addElement("    }, null); // *No! {null, \"\", \"   \"} are NG!");
        br.addElement("");
        br.addElement("  (o):");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    cb.specify().derivePurchaseList().max(new SubQuery<PurchaseCB>() {");
        br.addElement("        public void query(PurchaseCB subCB) {");
        br.addElement("            subCB.specify().columnPurchaseDatetime();");
        br.addElement("        }");
        br.addElement("    }, Member.ALIAS_latestPurchaseDatetime); // OK");
        br.addItem("Local Table");
        br.addElement(localCQ.getTableDbName());
        final String msg = br.buildExceptionMessage();
        throw new SpecifyDerivedReferrerInvalidAliasNameException(msg);
    }

    public void throwSpecifyDerivedReferrerEntityPropertyNotFoundException(String aliasName, Class<?> entityType) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The property for derived-referrer was NOT FOUND in the entity!");
        br.addItem("Advice");
        br.addElement("You should implement a property (setter and getter) in the entity.");
        br.addElement("Or you should confirm whether the alias name has typo or not.");
        br.addElement("For example:");
        br.addElement("  (ConditionBean):");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    cb.specify().derivePurchaseList().max(new SubQuery<PurchaseCB>() {");
        br.addElement("        public void query(PurchaseCB subCB) {");
        br.addElement("            subCB.specify().columnPurchasePrice();");
        br.addElement("        }");
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
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    String key = \"$highestPurchasePrice\"; // OK");
        br.addElement("    cb.specify().derivePurchaseList().max(new SubQuery<PurchaseCB>() {");
        br.addElement("        public void query(PurchaseCB subCB) {");
        br.addElement("            subCB.specify().columnPurchasePrice();");
        br.addElement("        }");
        br.addElement("    }, key);");
        br.addElement("    List<Member> memberList = memberBhv.selectList(cb);");
        br.addElement("    for (Member member ; memberList) {");
        br.addElement("        Integer highestPurchasePrice = member.derived(key);");
        br.addElement("    }");
        br.addItem("Alias Name");
        br.addElement(aliasName);
        br.addItem("Target Entity");
        br.addElement(entityType);
        final String msg = br.buildExceptionMessage();
        throw new SpecifyDerivedReferrerEntityPropertyNotFoundException(msg);
    }

    public void throwSpecifyDerivedReferrerInvalidColumnSpecificationException(String function, String aliasName) {
        String msg = "Look! Read the message below." + ln();
        msg = msg + "/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *" + ln();
        msg = msg + "The specified the column for derived-referrer was INVALID!" + ln();
        msg = msg + ln();
        msg = msg + "[Advice]" + ln();
        msg = msg + "You should call specify().column[TargetColumn]() only once." + ln();
        msg = msg + "(If your function is count(), the target column should be primary key.)" + ln();
        msg = msg + "For example:" + ln();
        msg = msg + "  (x): (empty)" + ln();
        msg = msg + "    MemberCB cb = new MemberCB();" + ln();
        msg = msg + "    cb.specify().derivePurchaseList().max(new SubQuery<PurchaseCB>() {" + ln();
        msg = msg + "        public void query(PurchaseCB subCB) {" + ln();
        msg = msg + "            // *NG, it should not be empty" + ln();
        msg = msg + "        }" + ln();
        msg = msg + "    }, Member.ALIAS_latestPurchaseDatetime);" + ln();
        msg = msg + ln();
        msg = msg + "  (x): (duplicated)" + ln();
        msg = msg + "    MemberCB cb = new MemberCB();" + ln();
        msg = msg + "    cb.specify().derivePurchaseList().max(new SubQuery<PurchaseCB>() {" + ln();
        msg = msg + "        public void query(PurchaseCB subCB) {" + ln();
        msg = msg + "            // *NG, it should be the only one" + ln();
        msg = msg + "            subCB.specify().columnPurchaseDatetime();" + ln();
        msg = msg + "            subCB.specify().columnPurchaseCount();" + ln();
        msg = msg + "        }" + ln();
        msg = msg + "    }, Member.ALIAS_latestPurchaseDatetime);" + ln();
        msg = msg + ln();
        msg = msg + "  (o):" + ln();
        msg = msg + "    MemberCB cb = new MemberCB();" + ln();
        msg = msg + "    cb.specify().derivePurchaseList().max(new SubQuery<PurchaseCB>() {" + ln();
        msg = msg + "        public void query(PurchaseCB subCB) {" + ln();
        msg = msg + "            subCB.specify().columnPurchaseDatetime(); // OK" + ln();
        msg = msg + "        }" + ln();
        msg = msg + "    }, Member.ALIAS_latestPurchaseDatetime);" + ln();
        msg = msg + ln();
        msg = msg + "[Function Method]" + ln() + xconvertFunctionToMethod(function) + ln();
        msg = msg + ln();
        msg = msg + "[Alias Name]" + ln() + aliasName + ln();
        msg = msg + "* * * * * * * * * */";
        throw new SpecifyDerivedReferrerInvalidColumnSpecificationException(msg);
    }

    public void throwSpecifyDerivedReferrerUnmatchedColumnTypeException(String function, String derivedColumnDbName,
            Class<?> derivedColumnType) {
        String msg = "Look! Read the message below." + ln();
        msg = msg + "/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *" + ln();
        msg = msg + "The type of the specified the column unmatched with the function!" + ln();
        msg = msg + ln();
        msg = msg + "[Advice]" + ln();
        msg = msg + "You should confirm the list as follow:" + ln();
        msg = msg + "    count() : String, Number, Date *with distinct same" + ln();
        msg = msg + "    max()   : String, Number, Date" + ln();
        msg = msg + "    min()   : String, Number, Date" + ln();
        msg = msg + "    sum()   : Number" + ln();
        msg = msg + "    avg()   : Number" + ln();
        msg = msg + ln();
        msg = msg + "[Function]" + ln() + function + ln();
        msg = msg + ln();
        msg = msg + "[Derive Column]" + ln() + derivedColumnDbName + "(" + derivedColumnType.getName() + ")" + ln();
        msg = msg + "* * * * * * * * * */";
        throw new SpecifyDerivedReferrerUnmatchedColumnTypeException(msg);
    }

    public void throwSpecifyDerivedReferrerSelectAllPossibleException(String function, ConditionQuery subQuery,
            String aliasName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The specify derived-referrer might select all.");
        br.addItem("Advice");
        br.addElement("If you suppress correlation, set your original correlation condition.");
        br.addItem("Function");
        br.addElement(function);
        br.addItem("Referrer");
        br.addElement(subQuery.getTableDbName());
        br.addItem("Alias Name");
        br.addElement(aliasName);
        final String msg = br.buildExceptionMessage();
        throw new SpecifyDerivedReferrerSelectAllPossibleException(msg);
    }

    // ===================================================================================
    //                                                           Specified Derived OrderBy
    //                                                           =========================
    public void throwSpecifiedDerivedOrderByAliasNameNotFoundException(String aliasName) {
        String msg = "Look! Read the message below." + ln();
        msg = msg + "/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *" + ln();
        msg = msg + "The aliasName was not found in specified alias names." + ln();
        msg = msg + ln();
        msg = msg + "[Advice]" + ln();
        msg = msg + "You should specified an alias name that is the same as one in specify-derived-referrer." + ln();
        msg = msg + "For example:" + ln();
        msg = msg + "  (x):" + ln();
        msg = msg + "    MemberCB cb = new MemberCB();" + ln();
        msg = msg + "    cb.specify().derivePurchaseList().max(new SubQuery<PurchaseCB>() {" + ln();
        msg = msg + "        public void query(PurchaseCB subCB) {" + ln();
        msg = msg + "            subCB.specify().specifyProduct().columnProductName(); // *NG" + ln();
        msg = msg + "        }" + ln();
        msg = msg + "    }, \"LATEST_PURCHASE_DATETIME\");" + ln();
        msg = msg + "    cb.query().addSpecifiedDerivedOrderBy_Desc(\"WRONG_NAME_DATETIME\");" + ln();
        msg = msg + ln();
        msg = msg + "  (o):" + ln();
        msg = msg + "    MemberCB cb = new MemberCB();" + ln();
        msg = msg + "    cb.specify().derivePurchaseList().max(new SubQuery<PurchaseCB>() {" + ln();
        msg = msg + "        public void query(PurchaseCB subCB) {" + ln();
        msg = msg + "            subCB.specify().columnPurchaseDatetime(); // OK" + ln();
        msg = msg + "        }" + ln();
        msg = msg + "    }, \"LATEST_PURCHASE_DATETIME\");" + ln();
        msg = msg + "    cb.query().addSpecifiedDerivedOrderBy_Desc(\"LATEST_PURCHASE_DATETIME\");" + ln();
        msg = msg + ln();
        msg = msg + "[not found Alias Name]" + ln() + aliasName + ln();
        msg = msg + "* * * * * * * * * */";
        throw new SpecifiedDerivedOrderByAliasNameNotFoundException(msg);
    }

    // -----------------------------------------------------
    //                                Query Derived Referrer
    //                                ----------------------
    public void throwQueryDerivedReferrerInvalidColumnSpecificationException(String function) {
        String msg = "Look! Read the message below." + ln();
        msg = msg + "/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *" + ln();
        msg = msg + "The specified the column for derived-referrer was INVALID!" + ln();
        msg = msg + ln();
        msg = msg + "[Advice]" + ln();
        msg = msg + " You should call specify().column[TargetColumn]() only once." + ln();
        msg = msg + " (If your function is count(), the target column should be primary key.)" + ln();
        msg = msg + "For example:" + ln();
        msg = msg + "  (x):" + ln();
        msg = msg + "    MemberCB cb = new MemberCB();" + ln();
        msg = msg + "    cb.query().derivedPurchaseList().max(new SubQuery<PurchaseCB>() {" + ln();
        msg = msg + "        public void query(PurchaseCB subCB) {" + ln();
        msg = msg + "            // *NG, it should not be empty" + ln();
        msg = msg + "        }" + ln();
        msg = msg + "    }).greaterEqual(123);" + ln();
        msg = msg + ln();
        msg = msg + "  (x):" + ln();
        msg = msg + "    MemberCB cb = new MemberCB();" + ln();
        msg = msg + "    cb.query().derivedPurchaseList().max(new SubQuery<PurchaseCB>() {" + ln();
        msg = msg + "        public void query(PurchaseCB subCB) {" + ln();
        msg = msg + "            // *NG, it should be the only one" + ln();
        msg = msg + "            subCB.specify().columnPurchaseDatetime();" + ln();
        msg = msg + "            subCB.specify().columnPurchaseCount();" + ln();
        msg = msg + "        }" + ln();
        msg = msg + "    }).greaterEqual(123);" + ln();
        msg = msg + ln();
        msg = msg + "  (o):" + ln();
        msg = msg + "    MemberCB cb = new MemberCB();" + ln();
        msg = msg + "    cb.query().derivedPurchaseList().max(new SubQuery<PurchaseCB>() {" + ln();
        msg = msg + "        public void query(PurchaseCB subCB) {" + ln();
        msg = msg + "            subCB.specify().columnPurchaseDatetime(); // OK" + ln();
        msg = msg + "        }" + ln();
        msg = msg + "    }).greaterEqual(123);" + ln();
        msg = msg + ln();
        msg = msg + "[Function Method]" + ln() + xconvertFunctionToMethod(function) + ln();
        msg = msg + "* * * * * * * * * */";
        throw new QueryDerivedReferrerInvalidColumnSpecificationException(msg);
    }

    public void throwQueryDerivedReferrerUnmatchedColumnTypeException(String function, String derivedColumnDbName,
            Class<?> derivedColumnType, Object value) {
        String msg = "Look! Read the message below." + ln();
        msg = msg + "/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *" + ln();
        msg = msg + "The type of the specified the column unmatched with the function or the parameter!" + ln();
        msg = msg + ln();
        msg = msg + "[Advice]" + ln();
        msg = msg + "You should confirm the list as follow:" + ln();
        msg = msg + "    count() : String, Number, Date *with distinct same" + ln();
        msg = msg + "    max()   : String, Number, Date" + ln();
        msg = msg + "    min()   : String, Number, Date" + ln();
        msg = msg + "    sum()   : Number" + ln();
        msg = msg + "    avg()   : Number" + ln();
        msg = msg + ln();
        msg = msg + "[Function Method]" + ln() + xconvertFunctionToMethod(function) + ln();
        msg = msg + ln();
        msg = msg + "[Derived Column]" + ln() + derivedColumnDbName + "(" + derivedColumnType.getName() + ")" + ln();
        msg = msg + ln();
        msg = msg + "[Parameter Type]" + ln() + (value != null ? value.getClass() : null) + ln();
        msg = msg + "* * * * * * * * * */";
        throw new QueryDerivedReferrerUnmatchedColumnTypeException(msg);
    }

    public void throwQueryDerivedReferrerSelectAllPossibleException(String function, ConditionQuery subQuery) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The query derived-referrer might select all.");
        br.addItem("Advice");
        br.addElement("If you suppress correlation, set your original correlation condition.");
        br.addItem("Function");
        br.addElement(function);
        br.addItem("Referrer");
        br.addElement(subQuery.getTableDbName());
        final String msg = br.buildExceptionMessage();
        throw new QueryDerivedReferrerSelectAllPossibleException(msg);
    }

    // ===================================================================================
    //                                                                               Query
    //                                                                               =====
    public void throwQueryIllegalPurposeException(HpCBPurpose purpose, ConditionBean baseCB) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The purpose was illegal for query.");
        br.addItem("Advice");
        br.addElement("This condition-bean is not allowed to set query.");
        br.addElement("(contains OrScopeQuery and ColumnQuery)");
        br.addElement("Because this is for " + purpose + ".");
        br.addElement("For example:");
        br.addElement("  (x): (ColumnQuery)");
        br.addElement("    cb.columnQuery(new SpecifyQuery<MemberCB>() {");
        br.addElement("        public void specify(MemberCB cb) {");
        br.addElement("            cb.query().set...();  // *NG");
        br.addElement("            cb.columnQuery(...);  // *NG");
        br.addElement("            cb.orScopeQuery(...); // *NG");
        br.addElement("        }");
        br.addElement("    })...");
        br.addElement("  (x): (VaryingUpdate)");
        br.addElement("    UpdateOption option = new UpdateOption().self(new SpecifyQuery<MemberCB>() {");
        br.addElement("        public void specify(MemberCB cb) {");
        br.addElement("            cb.query().set...();  // *NG");
        br.addElement("            cb.columnQuery(...);  // *NG");
        br.addElement("            cb.orScopeQuery(...); // *NG");
        br.addElement("        }");
        br.addElement("    });");
        // don't use displaySql because of illegal CB's state
        br.addItem("ConditionBean");
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
        br.addElement("  MemberCB cb = new MemberCB()");
        br.addElement("  cb.query().existsPurchaseList(subCB -> {");
        br.addElement("      subCB.query().setPurchasePrice_GreaterThan(2000);");
        br.addElement("      cb.query().setBirthdate_GreaterThan(currentDate()); // *NG");
        br.addElement("  });");
        br.addElement("(o):");
        br.addElement("  MemberCB cb = new MemberCB()");
        br.addElement("  cb.query().existsPurchaseList(subCB -> {");
        br.addElement("      subCB.query().setPurchasePrice_GreaterThan(2000);");
        br.addElement("  });");
        br.addElement("  cb.query().setBirthdate_GreaterThan(currentDate()); // OK");
        br.addItem("Locked ConditionBean");
        br.addElement(lockedCB.getClass().getName());
        br.addElement("(" + lockedCB.getPurpose() + ")");
        final String msg = br.buildExceptionMessage();
        throw new QueryThatsBadTimingException(msg);
    }

    public void throwQueryAlreadyRegisteredException(ConditionKey key, Object value, ConditionValue cvalue,
            String columnDbName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The query has been already registered. (cannot override it)");
        br.addItem("Advice");
        br.addElement("Overriding query is not allowed as default setting.");
        br.addElement("Mistake? Or do you really want to override it?");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    cb.query().setMemberId_Equal(3);");
        br.addElement("    cb.query().setMemberId_Equal(4); // *NG");
        br.addElement("  (x):");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    cb.query().setMemberId_Equal(3);");
        br.addElement("    cb.query().setMemberId_Equal(3); // *NG");
        br.addElement("  (o):");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    cb.query().setMemberId_Equal(3);");
        br.addElement("    cb.query().setMemberAccount_Equal(\"Pixy\"); // OK");
        br.addElement("  (o):");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    cb.enableOverridingQuery();");
        br.addElement("    cb.query().setMemberId_Equal(3); // overridden");
        br.addElement("    cb.query().setMemberId_Equal(4); // OK");
        br.addItem("Column Name");
        br.addElement(columnDbName);
        br.addItem("Condition Key");
        br.addElement(key);
        br.addItem("New Value");
        br.addElement(value);
        br.addItem("Condition Value");
        br.addElement(value);
        final String msg = br.buildExceptionMessage();
        throw new QueryAlreadyRegisteredException(msg);
    }

    public void throwInvalidQueryRegisteredException(HpInvalidQueryInfo... invalidQueryInfoAry) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The invalid query was registered. (check is working)");
        br.addItem("Advice");
        br.addElement("You should not set null or empty to query.");
        br.addElement("For example: (when checked by default)");
        br.addElement("  (x):");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    cb.query().setMemberName_Equal(null); // exception");
        br.addElement("    cb.query().setMemberName_Equal(\"\"); // exception");
        br.addElement("  (o):");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    cb.query().setMemberName_Equal(\"Pixy\"); // normal query");
        br.addElement("  (o):");
        br.addElement("    MemberCB cb = new MemberCB();");
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
        br.addNotice("The likeSearchOption was not found! (should not be null)");
        br.addItem("Advice");
        br.addElement("Please confirm your method call:");
        final String beanName = DfTypeUtil.toClassTitle(this);
        final String methodName = "set" + capPropName + "_LikeSearch('" + value + "', likeSearchOption);";
        br.addElement("    " + beanName + "." + methodName);
        final String msg = br.buildExceptionMessage();
        throw new RequiredOptionNotFoundException(msg);
    }

    public void throwOrderByIllegalPurposeException(HpCBPurpose purpose, ConditionBean baseCB, String tableDbName,
            String columnName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The purpose was illegal for order-by.");
        br.addItem("Advice");
        br.addElement("This condition-bean is not allowed to order.");
        br.addElement("Because this is for " + purpose + ".");
        br.addElement("For example:");
        br.addElement("  (x): (ExistsReferrer)");
        br.addElement("    cb.query().existsXxxList(new SubQuery<PurchaseCB>() {");
        br.addElement("        public void query(PurchaseCB subCB) {");
        br.addElement("            subCB.query().addOrderBy...; // *NG");
        br.addElement("        }");
        br.addElement("    });");
        br.addElement("  (x): (Union)");
        br.addElement("    cb.union(new UnionQuery<MemberCB>() {");
        br.addElement("        public void query(MemberCB unionCB) {");
        br.addElement("            unionCB.query().addOrderBy...; // *NG");
        br.addElement("        }");
        br.addElement("    });");
        br.addElement("  (x): (DerivedReferrer)");
        br.addElement("    cb.specify().derivedPurchaseList().max(new SubQuery<PurchaseCB>() {");
        br.addElement("        public void query(PurchaseCB subCB) {");
        br.addElement("            subCB.query().addOrderBy...; // *NG");
        br.addElement("        }");
        br.addElement("    });");
        // don't use displaySql because of illegal CB's state
        br.addItem("ConditionBean");
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
        br.addNotice("The specified the column for column query was INVALID!");
        br.addItem("Advice");
        br.addElement("You should call specify().column[TargetColumn]() only once.");
        br.addElement("For example:");
        br.addElement("  (x): (empty)");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    cb.columnQuery(new SpecifyQuery<MemberCB>() {");
        br.addElement("        public void specify(MemberCB cb) {");
        br.addElement("            // *NG, it should not be empty");
        br.addElement("        }");
        br.addElement("    }).lessThan...;");
        br.addElement("  (x): (duplicated)");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    cb.columnQuery(new SpecifyQuery<MemberCB>() {");
        br.addElement("        public void specify(MemberCB cb) {");
        br.addElement("            // *NG, it should be the only one");
        br.addElement("            cb.specify().columnMemberName();");
        br.addElement("            cb.specify().columnBirthdate();");
        br.addElement("        }");
        br.addElement("    }).lessThan...;");
        br.addElement("  (o):");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    cb.columnQuery(new SpecifyQuery<MemberCB>() {");
        br.addElement("        public void specify(MemberCB cb) {");
        br.addElement("            cb.specify().columnBirthdate();");
        br.addElement("        }");
        br.addElement("    }).lessThan(new SpecifyQuery<MemberCB>() {");
        br.addElement("        public void specify(MemberCB cb) {");
        br.addElement("            cb.specify().columnFormalizedDatetime();");
        br.addElement("        }");
        br.addElement("    });");
        br.addItem("ConditionBean");
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
        br.addElement("    cb.orScopeQueryAndPart(new AndQuery<MemberCB>() { // *no!");
        br.addElement("        public void query(MemberCB andCB) {");
        br.addElement("            ...");
        br.addElement("        }");
        br.addElement("    });");
        br.addElement("  (o):");
        br.addElement("    cb.orScopeQuery(new OrQuery<MemberCB>() {");
        br.addElement("        public void query(MemberCB orCB) {");
        br.addElement("            orCB.orScopeQueryAndPart(new AndQuery(MemberCB andCB) {");
        br.addElement("                public void query(MemberCB andCB) {");
        br.addElement("                    andCB.query().set...();");
        br.addElement("                    andCB.query().set...();");
        br.addElement("                }");
        br.addElement("            });");
        br.addElement("        }");
        br.addElement("    });");
        br.addItem("ConditionBean");
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
        br.addElement("    cb.orScopeQuery(new OrQuery<MemberCB>() {");
        br.addElement("        public void query(MemberCB orCB) {");
        br.addElement("            orCB.orScopeQueryAndPart(new AndQuery(MemberCB andCB) {");
        br.addElement("                public void query(MemberCB andCB) {");
        br.addElement("                    andCB.orScopeQueryAndPart(new AndQuery(MemberCB andCB) { // *no!");
        br.addElement("                        ...");
        br.addElement("                    }");
        br.addElement("                }");
        br.addElement("            });");
        br.addElement("        }");
        br.addElement("    });");
        br.addItem("ConditionBean");
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
        br.addNotice("The specified the column for scalar-condition was invalid.");
        br.addItem("Advice");
        br.addElement("You should call specify().column[TargetColumn]() only once.");
        br.addElement("(If your function is count(), the target column should be primary key)");
        br.addElement("For example:");
        br.addElement("  (x): (empty)");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    cb.query().scalar_Equal().max(new SubQuery<MemberCB>() {");
        br.addElement("        public void query(MemberCB subCB) {");
        br.addElement("            // *NG, it should not be empty");
        br.addElement("        }");
        br.addElement("    });");
        br.addElement("");
        br.addElement("  (x): (duplicated)");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    cb.query().scalar_Equal().max(new SubQuery<MemberCB>() {");
        br.addElement("        public void query(MemberCB subCB) {");
        br.addElement("            // *NG, it should be the only one");
        br.addElement("            subCB.specify().columnBirthdate();");
        br.addElement("            subCB.specify().columnMemberName();");
        br.addElement("        }");
        br.addElement("    });");
        br.addElement("");
        br.addElement("  (o):");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    cb.query().scalar_Equal().max(new SubQuery<MemberCB>() {");
        br.addElement("        public void query(MemberCB subCB) {");
        br.addElement("            subCB.specify().columnBirthdate(); // *Point");
        br.addElement("        }");
        br.addElement("    });");
        br.addItem("Function Method");
        br.addElement(xconvertFunctionToMethod(function));
        final String msg = br.buildExceptionMessage();
        throw new ScalarConditionInvalidColumnSpecificationException(msg);
    }

    public void throwScalarConditionPartitionByInvalidColumnSpecificationException(String function) {
        final ExceptionMessageBuilder br = createExceptionMessageBuilder();
        br.addNotice("The specified the column for scalar-condition partition-by was invalid.");
        br.addItem("Advice");
        br.addElement("You should call specify().column[TargetColumn]() only once.");
        br.addElement("For example:");
        br.addElement("  (x): (empty)");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    cb.query().scalar_Equal().max(new SubQuery<MemberCB>() {");
        br.addElement("        public void query(MemberCB subCB) {");
        br.addElement("            subCB.specify().columnBirthdate()");
        br.addElement("        }");
        br.addElement("    }).partitionBy(new SpecifyQuery<MemberCB>() {");
        br.addElement("        public void specify(MemberCB cb) {");
        br.addElement("            // *NG, it should not be empty");
        br.addElement("        }");
        br.addElement("    });");
        br.addElement("");
        br.addElement("  (x): (duplicated)");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    cb.query().scalar_Equal().max(new SubQuery<MemberCB>() {");
        br.addElement("        public void query(MemberCB subCB) {");
        br.addElement("            subCB.specify().columnBirthdate()");
        br.addElement("        }");
        br.addElement("    }).partitionBy(new SpecifyQuery<MemberCB>() {");
        br.addElement("        public void specify(MemberCB cb) {");
        br.addElement("            // *NG, it should be the only one");
        br.addElement("            cb.specify().columnBirthdate();");
        br.addElement("            cb.specify().columnMemberName();");
        br.addElement("        }");
        br.addElement("    });");
        br.addElement("");
        br.addElement("  (o):");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    cb.query().scalar_Equal().max(new SubQuery<MemberCB>() {");
        br.addElement("        public void query(MemberCB subCB) {");
        br.addElement("            subCB.specify().columnBirthdate()");
        br.addElement("        }");
        br.addElement("    }).partitionBy(new SpecifyQuery<MemberCB>() {");
        br.addElement("        public void specify(MemberCB cb) {");
        br.addElement("            cb.specify().columnMemberStatusCode(); // *Point");
        br.addElement("        }");
        br.addElement("    });");
        br.addItem("Function Method");
        br.addElement(xconvertFunctionToMethod(function));
        final String msg = br.buildExceptionMessage();
        throw new ScalarConditionInvalidColumnSpecificationException(msg);
    }

    public void throwScalarConditionUnmatchedColumnTypeException(String function, String deriveColumnName,
            Class<?> deriveColumnType) {
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
    public void throwFixedConditionParameterNotFoundException(String tableDbName, String property,
            String fixedCondition, Map<String, Object> parameterMap) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the required parameter for the fixed condition.");
        br.addItem("Advice");
        br.addElement("Make sure your parameters to make the BizOneToOne relation.");
        br.addElement("For example:");
        br.addElement("  (x): cb.setupSelect_MemberAddressAsValid(null);");
        br.addElement("  (x): (ColumnQuery)");
        br.addElement("    cb.columnQuery(new ... {");
        br.addElement("        ...");
        br.addElement("    }).lessThan(new ...) {");
        br.addElement("        cb.specify().specifyMemberAddressAsValid().columnAddress();");
        br.addElement("    })");
        br.addElement("  (x): (DerivedReferrer)");
        br.addElement("    cb.specify().derivedMemberList(new ... {");
        br.addElement("        cb.specify().specifyMemberAddressAsValid().columnAddress();");
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
        br.addElement("    cb.columnQuery(new ... {");
        br.addElement("        ...");
        br.addElement("    }).lessThan(new ...) {");
        br.addElement("        cb.specify().specifyMemberAddressAsValid(currentDate()).columnAddress();");
        br.addElement("    });");
        br.addElement("  (o): (DerivedReferrer)");
        br.addElement("    cb.specify().derivedMemberList(new ... {");
        br.addElement("        cb.specify().specifyMemberAddressAsValid(currentDate()).columnAddress();");
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
    public void throwManualOrderNotFoundOrderByException(ConditionBean baseCB, ManualOrderBean mob) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found order-by element for the ManualOrder.");
        br.addItem("Advice");
        br.addElement("Make sure your implementation:");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    ManualOrderBean mob = new ManualOrderBean();");
        br.addElement("    mob.when_LessEqual(...);");
        br.addElement("    cb.query().withManualOrder(mob); // *NG");
        br.addElement("  (o):");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    ManualOrderBean mob = new ManualOrderBean();");
        br.addElement("    mob.when_LessEqual(...);");
        br.addElement("    cb.query().addOrderBy_Birthdate_Asc().withManualOrder(mob); // OK");
        br.addItem("ConditionBean");
        br.addElement(baseCB != null ? baseCB.getClass().getName() : baseCB); // check just in case
        br.addItem("ManualOrderBean");
        br.addElement(mob);
        final String msg = br.buildExceptionMessage();
        throw new IllegalConditionBeanOperationException(msg);
    }

    public void throwManualOrderSameBeanAlreadyExistsException(ConditionBean baseCB, ManualOrderBean existingMob,
            OrderByElement existingOrder, ManualOrderBean specifiedMob, OrderByElement specifiedOrder) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The same manual-order-bean with other columns was registered.");
        br.addItem("Advice");
        br.addElement("You can use manual-order-bean one time.");
        br.addElement("Make sure your implementation:");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    ManualOrderBean mob = new ManualOrderBean();");
        br.addElement("    mob.when_LessEqual(...);");
        br.addElement("    cb.query().addOrderBy_Birthdate_Asc().withManualOrder(mob);");
        br.addElement("    cb.query().addOrderBy_MemberId_Asc().withManualOrder(mob); // *NG");
        br.addElement("  (o):");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    ManualOrderBean birthMob = new ManualOrderBean();");
        br.addElement("    birthMob.when_LessEqual(...);");
        br.addElement("    cb.query().addOrderBy_Birthdate_Asc().withManualOrder(birthMob);");
        br.addElement("    ManualOrderBean idMob = new ManualOrderBean();");
        br.addElement("    idMob.when_LessEqual(...);");
        br.addElement("    cb.query().addOrderBy_MemberId_Asc().withManualOrder(idMob); // OK");
        br.addItem("ConditionBean");
        br.addElement(baseCB != null ? baseCB.getClass().getName() : baseCB); // check just in case
        br.addItem("Existing Bean");
        br.addElement(existingMob);
        br.addElement(existingOrder);
        br.addItem("Specified Bean");
        br.addElement(specifiedMob);
        br.addElement(specifiedOrder);
        final String msg = br.buildExceptionMessage();
        throw new IllegalConditionBeanOperationException(msg);
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    /**
     * Get the value of line separator.
     * @return The value of line separator. (NotNull)
     */
    protected String ln() {
        return DBFluteSystem.getBasicLn();
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
