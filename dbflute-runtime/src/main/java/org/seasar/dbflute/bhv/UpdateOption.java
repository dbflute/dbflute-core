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
package org.seasar.dbflute.bhv;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.seasar.dbflute.Entity;
import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.cbean.SpecifyQuery;
import org.seasar.dbflute.cbean.chelper.HpCalcSpecification;
import org.seasar.dbflute.cbean.chelper.HpCalculator;
import org.seasar.dbflute.cbean.chelper.HpSpecifiedColumn;
import org.seasar.dbflute.cbean.sqlclause.SqlClause;
import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.dbmeta.info.ColumnInfo;
import org.seasar.dbflute.dbmeta.info.UniqueInfo;
import org.seasar.dbflute.exception.BatchUpdateColumnModifiedPropertiesFragmentedException;
import org.seasar.dbflute.exception.IllegalConditionBeanOperationException;
import org.seasar.dbflute.exception.SpecifyUpdateColumnInvalidException;
import org.seasar.dbflute.exception.VaryingUpdateCalculationUnsupportedColumnTypeException;
import org.seasar.dbflute.exception.VaryingUpdateCommonColumnSpecificationException;
import org.seasar.dbflute.exception.VaryingUpdateInvalidColumnSpecificationException;
import org.seasar.dbflute.exception.VaryingUpdateNotFoundCalculationException;
import org.seasar.dbflute.exception.VaryingUpdateOptimisticLockSpecificationException;
import org.seasar.dbflute.exception.VaryingUpdatePrimaryKeySpecificationException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.StringKeyMap;
import org.seasar.dbflute.jdbc.StatementConfig;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.DfTypeUtil;

/**
 * The option of update for varying-update.
 * @author jflute
 * @since 0.9.7.2 (2010/06/18 Friday)
 * @param <CB> The type of condition-bean for specification.
 */
public class UpdateOption<CB extends ConditionBean> implements WritableOption<CB> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected List<HpCalcSpecification<CB>> _selfSpecificationList;
    protected Map<String, HpCalcSpecification<CB>> _selfSpecificationMap;

    protected SpecifyQuery<CB> _updateColumnSpecification;
    protected CB _updateColumnSpecifiedCB;
    protected Set<String> _forcedSpecifiedUpdateColumnSet;
    protected boolean _exceptCommonColumnForcedSpecified;
    protected boolean _updateColumnModifiedPropertiesFragmentedAllowed; // as default of batch update
    protected boolean _compatibleBatchUpdateDefaultEveryColumn;
    protected boolean _disableCommonColumnAutoSetup;
    protected boolean _nonQueryUpdateAllowed;
    protected boolean _queryUpdateForcedDirectAllowed;
    protected Integer _batchLoggingUpdateLimit;
    protected StatementConfig _updateStatementConfig;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Constructor.
     * <pre>
     * Purchase purchase = new Purchase();
     * purchase.setPurchaseId(value); <span style="color: #3F7E5E">// required</span>
     * purchase.setOther...(value); <span style="color: #3F7E5E">// you should set only modified columns</span>
     * 
     * <span style="color: #3F7E5E">// e.g. you can update by self calculation values</span>
     * UpdateOption&lt;PurchaseCB&gt; option = <span style="color: #DD4747">new UpdateOption&lt;PurchaseCB&gt;()</span>;
     * option.<span style="color: #DD4747">self</span>(new SpecifyQuery&lt;PurchaseCB&gt;() {
     *     public void specify(PurchaseCB cb) {
     *         cb.specify().<span style="color: #DD4747">columnPurchaseCount()</span>;
     *     }
     * }).<span style="color: #DD4747">plus</span>(1); <span style="color: #3F7E5E">// PURCHASE_COUNT = PURCHASE_COUNT + 1</span>
     * 
     * <span style="color: #3F7E5E">// e.g. you can update by your values for common columns</span>
     * option.<span style="color: #DD4747">disableCommonColumnAutoSetup</span>();
     * 
     * purchaseBhv.<span style="color: #DD4747">varyingUpdate</span>(purchase, option);
     * </pre>
     */
    public UpdateOption() {
    }

    // ===================================================================================
    //                                                                    Self Calculation
    //                                                                    ================
    /**
     * Specify a self calculation as update value. <br />
     * You can specify a column except PK column, common column and optimistic-lock column.
     * And you can specify only one column that is a number type.
     * <pre>
     * Purchase purchase = new Purchase();
     * purchase.setPurchaseId(value); <span style="color: #3F7E5E">// required</span>
     * purchase.setOther...(value); <span style="color: #3F7E5E">// you should set only modified columns</span>
     * UpdateOption&lt;PurchaseCB&gt; option = new UpdateOption&lt;PurchaseCB&gt;();
     * option.<span style="color: #DD4747">self</span>(new SpecifyQuery&lt;PurchaseCB&gt;() {
     *     public void specify(PurchaseCB cb) {
     *         cb.specify().<span style="color: #DD4747">columnPurchaseCount()</span>;
     *     }
     * }).<span style="color: #DD4747">plus</span>(1); <span style="color: #3F7E5E">// PURCHASE_COUNT = PURCHASE_COUNT + 1</span>
     * purchaseBhv.<span style="color: #DD4747">varyingUpdateNonstrict</span>(purchase, option);
     * </pre>
     * @param selfCalculationSpecification The query for specification that specifies only one column. (NotNull)
     * @return The calculation of specification for the specified column. (NotNull)
     */
    public HpCalculator self(SpecifyQuery<CB> selfCalculationSpecification) {
        if (selfCalculationSpecification == null) {
            String msg = "The argument 'selfCalculationSpecification' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (_selfSpecificationList == null) {
            _selfSpecificationList = DfCollectionUtil.newArrayList();
        }
        final HpCalcSpecification<CB> specification = new HpCalcSpecification<CB>(selfCalculationSpecification);
        _selfSpecificationList.add(specification);
        return specification;
    }

    public boolean hasSelfSpecification() {
        return _selfSpecificationList != null && !_selfSpecificationList.isEmpty();
    }

    public void resolveSelfSpecification(CB cb) {
        if (_selfSpecificationList == null || _selfSpecificationList.isEmpty()) {
            return;
        }
        _selfSpecificationMap = StringKeyMap.createAsFlexibleOrdered();
        for (HpCalcSpecification<CB> specification : _selfSpecificationList) {
            specification.specify(cb);
            final String columnDbName = specification.getResolvedSpecifiedColumnDbName();
            assertSpecifiedColumn(cb, columnDbName);
            _selfSpecificationMap.put(columnDbName, specification);
        }
    }

    protected void assertSpecifiedColumn(CB cb, String columnDbName) {
        if (columnDbName == null) {
            throwVaryingUpdateInvalidColumnSpecificationException(cb);
        }
        final ColumnInfo columnInfo = cb.getDBMeta().findColumnInfo(columnDbName);
        if (columnInfo.isPrimary()) {
            throwVaryingUpdatePrimaryKeySpecificationException(columnInfo);
        }
        if (columnInfo.isCommonColumn()) {
            throwVaryingUpdateCommonColumnSpecificationException(columnInfo);
        }
        if (columnInfo.isOptimisticLock()) {
            throwVaryingUpdateOptimisticLockSpecificationException(columnInfo);
        }
        if (!columnInfo.isObjectNativeTypeNumber() && !columnInfo.isObjectNativeTypeDate()) {
            // *simple message because other types may be supported at the future
            String msg = "Not number or date column specified: " + columnInfo;
            throw new VaryingUpdateCalculationUnsupportedColumnTypeException(msg);
        }
    }

    protected void throwVaryingUpdateInvalidColumnSpecificationException(CB cb) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The specified column for varying-update was invalid.");
        br.addItem("Advice");
        br.addElement("You should call specify().column[TargetColumn]() only once.");
        br.addElement("For example:");
        br.addElement("");
        br.addElement("  (x):");
        br.addElement("    option.self(new SpecifyQuery<PurchaseCB>() {");
        br.addElement("        public void specify(PurchaseCB cb) {");
        br.addElement("            // *no, empty");
        br.addElement("        }");
        br.addElement("    });");
        br.addElement("  (x):");
        br.addElement("    option.self(new SpecifyQuery<PurchaseCB>() {");
        br.addElement("        public void specify(PurchaseCB cb) {");
        br.addElement("            cb.specify().columnPurchaseCount();");
        br.addElement("            cb.specify().columnPurchasePrice(); // *no, duplicated");
        br.addElement("        }");
        br.addElement("    });");
        br.addElement("  (o)");
        br.addElement("    option.self(new SpecifyQuery<PurchaseCB>() {");
        br.addElement("        public void specify(PurchaseCB cb) {");
        br.addElement("            cb.specify().columnPurchaseCount(); // OK");
        br.addElement("        }");
        br.addElement("    });");
        br.addItem("Target Table");
        br.addElement(cb.getTableDbName());
        final String msg = br.buildExceptionMessage();
        throw new VaryingUpdateInvalidColumnSpecificationException(msg);
    }

    protected void throwVaryingUpdatePrimaryKeySpecificationException(ColumnInfo columnInfo) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The primary key column was specified.");
        br.addItem("Advice");
        br.addElement("Varying-update is not allowed to specify a PK column.");
        br.addItem("Target Table");
        br.addElement(columnInfo.getDBMeta().getTableDbName());
        br.addItem("Specified Column");
        br.addElement(columnInfo);
        final String msg = br.buildExceptionMessage();
        throw new VaryingUpdatePrimaryKeySpecificationException(msg);
    }

    protected void throwVaryingUpdateCommonColumnSpecificationException(ColumnInfo columnInfo) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The column for optimistic lock was specified.");
        br.addItem("Advice");
        br.addElement("Varying-update is not allowed to specify a optimistic-lock column.");
        br.addItem("Target Table");
        br.addElement(columnInfo.getDBMeta().getTableDbName());
        br.addItem("Specified Column");
        br.addElement(columnInfo);
        final String msg = br.buildExceptionMessage();
        throw new VaryingUpdateCommonColumnSpecificationException(msg);
    }

    protected void throwVaryingUpdateOptimisticLockSpecificationException(ColumnInfo columnInfo) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The column for optimistic lock was specified.");
        br.addItem("Advice");
        br.addElement("Varying-update is not allowed to specify a optimistic-lock column.");
        br.addItem("Target Table");
        br.addElement(columnInfo.getDBMeta().getTableDbName());
        br.addItem("Specified Column");
        br.addElement(columnInfo);
        final String msg = br.buildExceptionMessage();
        throw new VaryingUpdateOptimisticLockSpecificationException(msg);
    }

    protected String getSpecifiedColumnDbNameAsOne(CB cb) {
        return cb.getSqlClause().getSpecifiedColumnDbNameAsOne();
    }

    // ===================================================================================
    //                                                                     Build Statement
    //                                                                     ===============
    public boolean hasStatement(String columnDbName) {
        return findStatementSpecification(columnDbName) != null;
    }

    public String buildStatement(String columnDbName) {
        return doBuildStatement(columnDbName, null);
    }

    public String buildStatement(String columnDbName, String aliasName) {
        return doBuildStatement(columnDbName, aliasName);
    }

    protected String doBuildStatement(String columnDbName, String aliasName) {
        final HpCalcSpecification<CB> calcSp = findStatementSpecification(columnDbName);
        if (calcSp == null) {
            return null;
        }
        final HpSpecifiedColumn specifiedColumn = calcSp.getResolvedSpecifiedColumn();
        if (specifiedColumn != null && specifiedColumn.hasSpecifyCalculation()) {
            throwVaryingUpdateSpecifyCalculatonUnsupportedException(columnDbName);
        }
        final String statement = calcSp.buildStatementAsSqlName(aliasName);
        if (statement == null) { // means non-calculation
            throwVaryingUpdateNotFoundCalculationException(columnDbName);
        }
        return statement;
    }

    protected HpCalcSpecification<CB> findStatementSpecification(String columnDbName) {
        // only "self" supported yet
        return _selfSpecificationMap != null ? _selfSpecificationMap.get(columnDbName) : null;
    }

    protected void throwVaryingUpdateSpecifyCalculatonUnsupportedException(String columnDbName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("VaryingUpdate with SpecifyCalculation is unsupported.");
        br.addItem("Advice");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    option.self(new SpecifyQuery<PurchaseCB>() {");
        br.addElement("        public void specify(PurchaseCB cb) {");
        br.addElement("            cb.specify().columnPurchaseCount().plus(1); // *NG");
        br.addElement("        }");
        br.addElement("    }).multiply(3);");
        br.addElement("  (o):");
        br.addElement("    option.self(new SpecifyQuery<PurchaseCB>() {");
        br.addElement("        public void specify(PurchaseCB cb) {");
        br.addElement("            cb.specify().columnPurchaseCount();");
        br.addElement("        }");
        br.addElement("    }).plus(1).multiply(3); // OK");
        br.addItem("Specified Column");
        br.addElement(columnDbName);
        final String msg = br.buildExceptionMessage();
        throw new IllegalConditionBeanOperationException(msg); // because of specified by ConditionBean
    }

    protected void throwVaryingUpdateNotFoundCalculationException(String columnDbName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The calculation of specified column for VaryingUpdate was not found.");
        br.addItem("Advice");
        br.addElement("You should call plus()/minus()/... methods after specification.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    option.self(new SpecifyQuery<PurchaseCB>() {");
        br.addElement("        public void specify(PurchaseCB cb) {");
        br.addElement("            cb.specify().columnPurchaseCount();");
        br.addElement("        }");
        br.addElement("    }); // *NG");
        br.addElement("  (o):");
        br.addElement("    option.self(new SpecifyQuery<PurchaseCB>() {");
        br.addElement("        public void specify(PurchaseCB cb) {");
        br.addElement("            cb.specify().columnPurchaseCount();");
        br.addElement("        }");
        br.addElement("    }).plus(1); // OK");
        br.addItem("Specified Column");
        br.addElement(columnDbName);
        final String msg = br.buildExceptionMessage();
        throw new VaryingUpdateNotFoundCalculationException(msg);
    }

    // ===================================================================================
    //                                                                       Update Column
    //                                                                       =============
    // -----------------------------------------------------
    //                                        Specify Column
    //                                        --------------
    /**
     * Specify update columns manually. <br />
     * You can update fixed columns instead of modified update columns.
     * <pre>
     * Member member = new Member();
     * member.setMemberId(3);
     * member.setOthers...(value);
     * UpdateOption&lt;MemberCB&gt; option = new UpdateOption&lt;MemberCB&gt;();
     * option.<span style="color: #DD4747">specify</span>(new SpecifyQuery&lt;MemberCB&gt;() {
     *     public void query(MemberCB cb) {
     *         <span style="color: #3F7E5E">// only MemberName and Birthdate are updated</span>
     *         <span style="color: #3F7E5E">// with common columns for update and an exclusive control column</span>
     *         cb.specify().columnMemberName();
     *         cb.specify().columnBirthdate();
     *     }
     * });
     * memberBhv.varyingUpdate(member, option);
     * </pre>
     * @param updateColumnSpecification The query for specifying update columns. (NotNull)
     */
    public void specify(SpecifyQuery<CB> updateColumnSpecification) {
        if (updateColumnSpecification == null) {
            String msg = "The argument 'updateColumnSpecification' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        _updateColumnSpecification = updateColumnSpecification;
    }

    public void resolveUpdateColumnSpecification(CB cb) {
        if (_updateColumnSpecification == null) {
            return;
        }
        _updateColumnSpecification.specify(cb);
        _updateColumnSpecifiedCB = cb;
        if (!_exceptCommonColumnForcedSpecified) {
            xacceptCommonColumnForcedSpecification(cb);
        }
        // an exclusive control column is specified forcedly by behavior's logic
    }

    // -----------------------------------------------------
    //                                         Common Column
    //                                         -------------
    /**
     * Except common columns from forced specified update columns.
     * @return The option of update. (NotNull: returns this)
     */
    public UpdateOption<CB> exceptCommonColumnForcedSpecified() {
        _exceptCommonColumnForcedSpecified = true;
        return this;
    }

    protected void xacceptCommonColumnForcedSpecification(CB cb) { // internal
        final List<ColumnInfo> beforeUpdateList = cb.getDBMeta().getCommonColumnInfoBeforeUpdateList();
        if (beforeUpdateList == null || beforeUpdateList.isEmpty()) {
            return;
        }
        for (ColumnInfo columnInfo : beforeUpdateList) {
            addForcedSpecifiedUpdateColumn(columnInfo);
        }
    }

    protected void addForcedSpecifiedUpdateColumn(ColumnInfo columnInfo) {
        if (_forcedSpecifiedUpdateColumnSet == null) {
            _forcedSpecifiedUpdateColumnSet = DfCollectionUtil.newHashSet();
        }
        _forcedSpecifiedUpdateColumnSet.add(columnInfo.getColumnDbName());
    }

    // -----------------------------------------------------
    //                                   Modified Properties
    //                                   -------------------
    // for BatchUpdate
    public void xacceptUpdateColumnModifiedPropertiesIfNeeds(List<? extends Entity> entityList) { // internal
        if (entityList == null) {
            throw new IllegalArgumentException("The argument 'entityList' should not be null.");
        }
        if (_updateColumnSpecification != null) {
            return; // already specified
        }
        if (entityList.isEmpty()) {
            return; // do nothing
        }
        if (xisCompatibleBatchUpdateDefaultEveryColumn()) {
            return; // every column for compatible
        }
        final Entity firstEntity = entityList.get(0);
        final Set<String> targetProps = xgatherUpdateColumnModifiedProperties(entityList, firstEntity);
        final DBMeta dbmeta = firstEntity.getDBMeta();
        specify(new SpecifyQuery<CB>() {
            public void specify(CB cb) {
                // you don't need to specify primary key because primary key has special handling
                for (String prop : targetProps) {
                    final ColumnInfo info = dbmeta.findColumnInfo(prop);
                    if (!info.isPrimary()) { // except PK
                        cb.localSp().xspecifyColumn(info.getColumnDbName());
                    }
                }
            }
        });
    }

    public void xallowUpdateColumnModifiedPropertiesFragmented() { // might be called by generator for option
        _updateColumnModifiedPropertiesFragmentedAllowed = true;
    }

    public void xdisallowUpdateColumnModifiedPropertiesFragmented() { // for back to default
        _updateColumnModifiedPropertiesFragmentedAllowed = false;
    }

    public boolean xisUpdateColumnModifiedPropertiesFragmentedAllowed() {
        return _updateColumnModifiedPropertiesFragmentedAllowed;
    }

    protected Set<String> xgatherUpdateColumnModifiedProperties(List<? extends Entity> entityList, Entity firstEntity) {
        // no use for now (same-set columns basis)
        //if (firstEntity.createdBySelect() || ...) { // ...
        if (xisUpdateColumnModifiedPropertiesFragmentedAllowed()) { // least common multiple
            final Set<String> mergedProps = new LinkedHashSet<String>();
            for (Entity entity : entityList) { // for merge
                mergedProps.addAll(entity.modifiedProperties());
            }
            return mergedProps;
        } else { // same-set columns (mainly here)
            final Set<String> firstProps = firstEntity.modifiedProperties();
            for (Entity entity : entityList) { // for check
                if (!entity.modifiedProperties().equals(firstProps)) {
                    throwBatchUpdateColumnModifiedPropertiesFragmentedException(firstProps, entity);
                }
            }
            return firstProps; // use first entity's
        }
    }

    protected void throwBatchUpdateColumnModifiedPropertiesFragmentedException(Set<String> baseProps, Entity entity) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The modified properties in the entity are fragmented as batch update column.");
        br.addItem("Advice");
        br.addElement("You should specify the same-set columns to your entities.");
        br.addElement("For example:");
        br.addElement("");
        br.addElement("  (x): (BatchUpdate)");
        br.addElement("    for (... : ...) {");
        br.addElement("        Member member = new Member();");
        br.addElement("        member.setMemberName(\"foo\");");
        br.addElement("        if (...) { // only a part of entities is set");
        br.addElement("            member.setMemberStatusCode_Formalized();");
        br.addElement("        }");
        br.addElement("        member.setVersionNo(...);");
        br.addElement("        memberList.add(member);");
        br.addElement("    }");
        br.addElement("    memberBhv.batchUpdate(memberList); // throws exception");
        br.addElement("  (o): (BatchUpdate)");
        br.addElement("    for (... : ...) {");
        br.addElement("        Member member = new Member();");
        br.addElement("        member.setMemberName(\"foo\");");
        br.addElement("        if (...) {");
        br.addElement("            member.setMemberStatusCode_Formalized();");
        br.addElement("        } else {");
        br.addElement("            member.setMemberStatusCode_Provisional();");
        br.addElement("        }");
        br.addElement("        member.setVersionNo(...);");
        br.addElement("        memberList.add(member);");
        br.addElement("    }");
        br.addElement("    memberBhv.batchUpdate(memberList); // MEMBER_STATUS_CODE is updated");
        br.addElement("  (o): (EntityUpdate)");
        br.addElement("    for (... : ...) {");
        br.addElement("        Member member = new Member();");
        br.addElement("        member.setMemberName(\"foo\");");
        br.addElement("        if (...) { // only a part of entities is set");
        br.addElement("            member.setMemberStatusCode_Formalized();");
        br.addElement("        }");
        br.addElement("        member.setVersionNo(...);");
        br.addElement("        memberList.add(member);");
        br.addElement("    }");
        br.addElement("    for (Member member : memberList) {");
        br.addElement("        memberBhv.update(member); // keep or update new value of MEMBER_STATUS_CODE");
        br.addElement("    }");
        br.addItem("Update Table");
        br.addElement(entity.getDBMeta().getTableDbName());
        br.addItem("Base Properties");
        br.addElement(baseProps);
        br.addItem("Fragmented Entity");
        br.addElement(entity.getDBMeta().extractPrimaryKeyMap(entity));
        br.addItem("Fragmented Properties");
        br.addElement(entity.modifiedProperties());
        final String msg = br.buildExceptionMessage();
        throw new BatchUpdateColumnModifiedPropertiesFragmentedException(msg);
    }

    // -----------------------------------------------------
    //                               Compatible UpdateColumn
    //                               -----------------------
    // for BatchUpdate
    public void xtoBeCompatibleBatchUpdateDefaultEveryColumn() {
        _compatibleBatchUpdateDefaultEveryColumn = true;
    }

    public boolean xisCompatibleBatchUpdateDefaultEveryColumn() {
        return _compatibleBatchUpdateDefaultEveryColumn;
    }

    // -----------------------------------------------------
    //                                        Update Process
    //                                        --------------
    public void xcheckSpecifiedUpdateColumnPrimaryKey() { // checked later by process if it needs
        if (_updateColumnSpecification == null) {
            return;
        }
        assertUpdateColumnSpecifiedCB();
        final CB cb = _updateColumnSpecifiedCB;
        final String basePointAliasName = cb.getSqlClause().getBasePointAliasName();
        final DBMeta dbmeta = cb.getDBMeta();
        if (dbmeta.hasPrimaryKey()) {
            final UniqueInfo pkInfo = dbmeta.getPrimaryUniqueInfo();
            final List<ColumnInfo> pkList = pkInfo.getUniqueColumnList();
            for (ColumnInfo pk : pkList) {
                final String columnDbName = pk.getColumnDbName();
                if (cb.getSqlClause().hasSpecifiedSelectColumn(basePointAliasName, columnDbName)) {
                    String msg = "PK columns should not be allowed to specify as update columns: " + columnDbName;
                    throw new SpecifyUpdateColumnInvalidException(msg);
                }
            }
        }
    }

    public boolean hasSpecifiedUpdateColumn() {
        return _updateColumnSpecification != null;
    }

    public boolean isSpecifiedUpdateColumn(String columnDbName) {
        if (_forcedSpecifiedUpdateColumnSet != null && _forcedSpecifiedUpdateColumnSet.contains(columnDbName)) {
            return true; // basically common column
        }
        assertUpdateColumnSpecifiedCB();
        final SqlClause sqlClause = _updateColumnSpecifiedCB.getSqlClause();
        return sqlClause.hasSpecifiedSelectColumn(sqlClause.getBasePointAliasName(), columnDbName);
    }

    protected void assertUpdateColumnSpecifiedCB() {
        if (_updateColumnSpecifiedCB == null) {
            String msg = "The CB for specification of update columns should be required here.";
            throw new IllegalStateException(msg);
        }
    }

    // ===================================================================================
    //                                                                       Common Column
    //                                                                       =============
    /**
     * Disable auto-setup for common columns. <br />
     * You can update by your values for common columns.
     * <pre>
     * Member member = new Member();
     * member.setMemberId(3);
     * member.setOthers...(value);
     * member.setUpdateDatetime(updateDatetime);
     * member.setUpdateUser(updateUser);
     * UpdateOption&lt;MemberCB&gt; option = new UpdateOption&lt;MemberCB&gt;();
     * option.<span style="color: #DD4747">disableCommonColumnAutoSetup</span>();
     * memberBhv.varyingUpdate(member, option);
     * </pre>
     * @return The option of update. (NotNull: returns this)
     */
    public UpdateOption<CB> disableCommonColumnAutoSetup() {
        _disableCommonColumnAutoSetup = true;
        return this;
    }

    public boolean isCommonColumnAutoSetupDisabled() {
        return _disableCommonColumnAutoSetup;
    }

    // ===================================================================================
    //                                                                        Query Update
    //                                                                        ============
    /**
     * Allow you to non-query-update (means query-update without a query condition). <br />
     * Normally it is not allowed, so you can do it by this option if you want.
     * @return The option of update. (NotNull: returns this)
     */
    public UpdateOption<CB> allowNonQueryUpdate() {
        _nonQueryUpdateAllowed = true;
        return this;
    }

    public boolean isNonQueryUpdateAllowed() {
        return _nonQueryUpdateAllowed;
    }

    /**
     * Allow you to use direct clause in query update forcedly.
     * @return The option of update. (NotNull: returns this)
     */
    public UpdateOption<CB> allowQueryUpdateForcedDirect() {
        _queryUpdateForcedDirectAllowed = true;
        return this;
    }

    public boolean isQueryUpdateForcedDirectAllowed() {
        return _queryUpdateForcedDirectAllowed;
    }

    // ===================================================================================
    //                                                                       Batch Logging
    //                                                                       =============
    /**
     * Limit batch-update logging by logging size. <br />
     * For example, if you set 3, only 3 records are logged. <br />
     * This also works to SqlLogHandler's call-back and SqlResultInfo's displaySql.
     * @param batchLoggingUpdateLimit The limit size of batch-update logging. (NullAllowed: if null and minus, means no limit)
     */
    public void limitBatchUpdateLogging(Integer batchLoggingUpdateLimit) {
        _batchLoggingUpdateLimit = batchLoggingUpdateLimit;
    }

    public Integer getBatchUpdateLoggingLimit() {
        return _batchLoggingUpdateLimit;
    }

    // ===================================================================================
    //                                                                           Configure
    //                                                                           =========
    /**
     * Configure statement JDBC options. (For example, queryTimeout, fetchSize, ...)
     * @param updateStatementConfig The configuration of statement for update. (NullAllowed)
     */
    public void configure(StatementConfig updateStatementConfig) {
        _updateStatementConfig = updateStatementConfig;
    }

    public StatementConfig getUpdateStatementConfig() {
        return _updateStatementConfig;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (_selfSpecificationList != null && !_selfSpecificationList.isEmpty()) {
            sb.append("SelfCalculationSpecified");
        }
        if (_updateColumnSpecification != null) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("UpdateColumnSpecified");
        }
        if (_disableCommonColumnAutoSetup) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("CommonColumnDisabled");
        }
        if (_nonQueryUpdateAllowed) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("NonQueryUpdateAllowed");
        }
        if (_batchLoggingUpdateLimit != null) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("batchLogging(" + _batchLoggingUpdateLimit + ")");
        }
        if (sb.length() == 0) {
            sb.append("default");
        }
        return DfTypeUtil.toClassTitle(this) + ":{" + sb.toString() + "}";
    }
}