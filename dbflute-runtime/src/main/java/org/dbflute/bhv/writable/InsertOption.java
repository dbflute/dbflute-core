/*
 * Copyright 2014-2024 the original author or authors.
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
package org.dbflute.bhv.writable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.dbflute.Entity;
import org.dbflute.cbean.ConditionBean;
import org.dbflute.cbean.coption.StatementConfigCall;
import org.dbflute.cbean.scoping.SpecifyQuery;
import org.dbflute.cbean.sqlclause.SqlClause;
import org.dbflute.dbmeta.DBMeta;
import org.dbflute.dbmeta.info.ColumnInfo;
import org.dbflute.dbmeta.info.PrimaryInfo;
import org.dbflute.exception.BatchInsertColumnModifiedPropertiesFragmentedException;
import org.dbflute.exception.IllegalConditionBeanOperationException;
import org.dbflute.exception.SpecifyUpdateColumnInvalidException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.jdbc.StatementConfig;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfTypeUtil;

/**
 * The option of insert for varying-insert.
 * @param <CB> The type of condition-bean for specification.
 * @author jflute
 * @since 0.9.7.8 (2010/12/16 Thursday)
 */
public class InsertOption<CB extends ConditionBean> implements WritableOption<CB> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    public static final Long VERSION_NO_FIRST_VALUE = 0L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected SpecifyQuery<CB> _insertColumnSpecification;
    protected CB _insertColumnSpecifiedCB;
    protected Set<String> _forcedSpecifiedInsertColumnSet;
    protected boolean _exceptCommonColumnForcedSpecified;
    protected boolean _insertColumnModifiedPropertiesFragmentedAllowed = true; // as default of batch insert
    protected boolean _compatibleBatchInsertDefaultEveryColumn;
    protected boolean _compatibleInsertColumnNotNullOnly;

    protected boolean _disableCommonColumnAutoSetup;
    protected boolean _disablePrimaryKeyIdentity;
    protected Integer _batchInsertLoggingLimit;
    protected StatementConfig _insertStatementConfig;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Constructor.
     * <pre>
     * Member member = new Member();
     * member.set...(value);
     * memberBhv.<span style="color: #CC4747">varyingInsert</span>(member, op <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #3F7E5E">// e.g. you can insert by your values for common columns</span>
     *     op.<span style="color: #CC4747">disableCommonColumnAutoSetup</span>();
     * 
     *     <span style="color: #3F7E5E">// e.g. you can insert by your values for primary key</span>
     *     op.<span style="color: #CC4747">disablePrimaryKeyIdentity</span>();
     * });
     * </pre>
     */
    public InsertOption() {
    }

    // ===================================================================================
    //                                                                       Insert Column
    //                                                                       =============
    // -----------------------------------------------------
    //                                        Specify Column
    //                                        --------------
    /**
     * Specify insert columns manually. <br>
     * You can insert fixed columns instead of modified update columns.
     * <pre>
     * Member member = new Member();
     * member.setMemberId(3);
     * member.setOthers...(value);
     * memberBhv.varyingUpdate(member, op <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     op.<span style="color: #CC4747">specify</span>(cb <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *         <span style="color: #3F7E5E">// only MemberName and Birthdate are updated</span>
     *         <span style="color: #3F7E5E">// with common columns for update and an exclusive control column</span>
     *         cb.specify().columnMemberName();
     *         cb.specify().columnBirthdate();
     *     });
     * });
     * </pre>
     * @param insertColumnSpecification The query for specifying insert columns. (NotNull)
     */
    public void specify(SpecifyQuery<CB> insertColumnSpecification) {
        if (insertColumnSpecification == null) {
            String msg = "The argument 'insertColumnSpecification' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        _insertColumnSpecification = insertColumnSpecification;
    }

    public void resolveInsertColumnSpecification(CB cb) {
        if (_insertColumnSpecification == null) {
            return;
        }
        _insertColumnSpecification.specify(cb);
        _insertColumnSpecifiedCB = cb;
        if (!_exceptCommonColumnForcedSpecified) {
            xacceptCommonColumnForcedSpecification(cb);
        }
        // an exclusive control column is specified forcedly by behavior's logic
    }

    // -----------------------------------------------------
    //                                         Common Column
    //                                         -------------
    /**
     * Except common columns from forced specified insert columns.
     * @return The option of insert. (NotNull: returns this)
     */
    public InsertOption<CB> exceptCommonColumnForcedSpecified() {
        _exceptCommonColumnForcedSpecified = true;
        return this;
    }

    protected void xacceptCommonColumnForcedSpecification(CB cb) { // internal
        final List<ColumnInfo> beforeInsertList = cb.asDBMeta().getCommonColumnInfoBeforeInsertList();
        if (beforeInsertList == null || beforeInsertList.isEmpty()) {
            return;
        }
        for (ColumnInfo columnInfo : beforeInsertList) {
            addForcedSpecifiedInsertColumn(columnInfo);
        }
    }

    protected void addForcedSpecifiedInsertColumn(ColumnInfo columnInfo) {
        if (_forcedSpecifiedInsertColumnSet == null) {
            _forcedSpecifiedInsertColumnSet = DfCollectionUtil.newHashSet();
        }
        _forcedSpecifiedInsertColumnSet.add(columnInfo.getColumnDbName());
    }

    // -----------------------------------------------------
    //                                   Modified Properties
    //                                   -------------------
    public void xacceptInsertColumnModifiedPropertiesIfNeeds(List<? extends Entity> entityList) { // internal
        if (entityList == null) {
            throw new IllegalArgumentException("The argument 'entityList' should not be null.");
        }
        if (_insertColumnSpecification != null) {
            return; // already specified
        }
        if (entityList.isEmpty()) {
            return; // do nothing
        }
        if (xisCompatibleBatchInsertDefaultEveryColumn()) {
            return; // every column for compatible
        }
        final Entity firstEntity = entityList.get(0);
        if (firstEntity.createdBySelect()) { // all columns e.g. copy insert
            specify(new SpecifyQuery<CB>() {
                public void specify(CB cb) {
                    final List<ColumnInfo> infoList = firstEntity.asDBMeta().getColumnInfoList();
                    for (ColumnInfo info : infoList) {
                        if (!info.isPrimary()) { // except PK
                            cb.localSp().xspecifyColumn(info.getColumnDbName());
                        }
                    }
                }
            });
        } else { // least common multiple or same-set columns
            final Set<String> targetProps = xgatherInsertColumnModifiedProperties(entityList, firstEntity);
            final DBMeta dbmeta = firstEntity.asDBMeta();
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
    }

    public void xallowInsertColumnModifiedPropertiesFragmented() { // back to default
        _insertColumnModifiedPropertiesFragmentedAllowed = true;
    }

    public void xdisallowInsertColumnModifiedPropertiesFragmented() { // might be called by generator for option
        _insertColumnModifiedPropertiesFragmentedAllowed = false;
    }

    public boolean xisInsertColumnModifiedPropertiesFragmentedAllowed() {
        return _insertColumnModifiedPropertiesFragmentedAllowed;
    }

    protected Set<String> xgatherInsertColumnModifiedProperties(List<? extends Entity> entityList, Entity firstEntity) {
        if (xisInsertColumnModifiedPropertiesFragmentedAllowed()) { // least common multiple (mainly here)
            final Set<String> mergedProps = new LinkedHashSet<String>();
            for (Entity entity : entityList) {
                mergedProps.addAll(entity.mymodifiedProperties());
            }
            return mergedProps;
        } else { // same-set columns
            final Set<String> firstProps = firstEntity.mymodifiedProperties();
            for (Entity entity : entityList) { // for check
                if (!entity.mymodifiedProperties().equals(firstProps)) {
                    throwBatchInsertColumnModifiedPropertiesFragmentedException(firstProps, entity);
                }
            }
            return firstProps; // use first entity's
        }
    }

    protected void throwBatchInsertColumnModifiedPropertiesFragmentedException(Set<String> baseProps, Entity entity) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The modified properties in the entity are fragmented as batch insert column.");
        br.addItem("Advice");
        br.addElement("You should specify the same-set columns to your entities.");
        br.addElement("For example:");
        br.addElement("");
        br.addElement("  (x): (BatchInsert)");
        br.addElement("    for (... : ...) {");
        br.addElement("        Member member = new Member();");
        br.addElement("        member.setMemberName(\"foo\");");
        br.addElement("        if (...) { // only a part of entities is set");
        br.addElement("            member.setBirthdate(currentDate());");
        br.addElement("        }");
        br.addElement("        memberList.add(member); // throws exception");
        br.addElement("    }");
        br.addElement("    memberBhv.batchInsert(memberList);");
        br.addElement("  (o): (BatchInsert)");
        br.addElement("    for (... : ...) {");
        br.addElement("        Member member = new Member();");
        br.addElement("        member.setMemberName(\"foo\");");
        br.addElement("        member.setBirthdate(currentDate()); // always set");
        br.addElement("        memberList.add(member); // all records are current date");
        br.addElement("    }");
        br.addElement("    memberBhv.batchInsert(memberList);");
        br.addElement("  (o): (BatchInsert)");
        br.addElement("    for (... : ...) {");
        br.addElement("        Member member = new Member();");
        br.addElement("        member.setMemberName(\"foo\");");
        br.addElement("        if (...) {");
        br.addElement("            member.setBirthdate(currentDate());");
        br.addElement("        } else {");
        br.addElement("            member.setBirthdate(null); // explicitly null");
        br.addElement("        }");
        br.addElement("        memberList.add(member);");
        br.addElement("    }");
        br.addElement("    memberBhv.batchInsert(memberList); // current date or null without default");
        br.addItem("Insert Table");
        br.addElement(entity.asDBMeta().getTableDbName());
        br.addItem("Base Properties");
        br.addElement(baseProps);
        br.addItem("Fragmented Entity");
        br.addElement(entity.asDBMeta().extractPrimaryKeyMap(entity));
        br.addItem("Fragmented Properties");
        br.addElement(entity.mymodifiedProperties());
        final String msg = br.buildExceptionMessage();
        throw new BatchInsertColumnModifiedPropertiesFragmentedException(msg);
    }

    // -----------------------------------------------------
    //                               Compatible InsertColumn
    //                               -----------------------
    // for BatchInsert
    public void xtoBeCompatibleBatchInsertDefaultEveryColumn() {
        _compatibleBatchInsertDefaultEveryColumn = true;
    }

    public boolean xisCompatibleBatchInsertDefaultEveryColumn() {
        return _compatibleBatchInsertDefaultEveryColumn;
    }

    // for EntityInsert
    public void xtoBeCompatibleInsertColumnNotNullOnly() {
        _compatibleInsertColumnNotNullOnly = true;
    }

    public boolean xisCompatibleInsertColumnNotNullOnly() {
        return _compatibleInsertColumnNotNullOnly;
    }

    // -----------------------------------------------------
    //                                        Insert Process
    //                                        --------------
    public void xcheckSpecifiedInsertColumnPrimaryKey() { // checked later by process if it needs
        if (_insertColumnSpecification == null) {
            return;
        }
        assertInsertColumnSpecifiedCB();
        final CB cb = _insertColumnSpecifiedCB;
        final String basePointAliasName = cb.getSqlClause().getBasePointAliasName();
        final DBMeta dbmeta = cb.asDBMeta();
        if (dbmeta.hasPrimaryKey()) {
            final PrimaryInfo pkInfo = dbmeta.getPrimaryInfo();
            final List<ColumnInfo> pkList = pkInfo.getPrimaryColumnList();
            for (ColumnInfo pk : pkList) {
                final String columnDbName = pk.getColumnDbName();
                if (cb.getSqlClause().hasSpecifiedSelectColumn(basePointAliasName, columnDbName)) {
                    String msg = "PK columns should not be allowed to specify as update columns: " + columnDbName;
                    throw new SpecifyUpdateColumnInvalidException(msg);
                }
            }
        }
    }

    public boolean hasSpecifiedInsertColumn() {
        return _insertColumnSpecification != null;
    }

    public boolean isSpecifiedInsertColumn(String columnDbName) {
        if (_forcedSpecifiedInsertColumnSet != null && _forcedSpecifiedInsertColumnSet.contains(columnDbName)) {
            return true; // basically common column
        }
        assertInsertColumnSpecifiedCB();
        final SqlClause sqlClause = _insertColumnSpecifiedCB.getSqlClause();
        return sqlClause.hasSpecifiedSelectColumn(sqlClause.getBasePointAliasName(), columnDbName);
    }

    protected void assertInsertColumnSpecifiedCB() {
        if (_insertColumnSpecifiedCB == null) {
            String msg = "The CB for specification of update columns should be required here.";
            throw new IllegalStateException(msg);
        }
    }

    // ===================================================================================
    //                                                                       Common Column
    //                                                                       =============
    /**
     * Disable auto-setup for common columns. <br>
     * You can insert by your values for common columns.
     * <pre>
     * Member member = new Member();
     * member.setOthers...(value);
     * member.setRegisterDatetime(registerDatetime);
     * member.setRegisterUser(registerUser);
     * member.setUpdateDatetime(updateDatetime);
     * member.setUpdateUser(updateUser);
     * memberBhv.varyingInsert(member, op <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     op.<span style="color: #CC4747">disableCommonColumnAutoSetup</span>();
     * });
     * </pre>
     * @return The option of insert. (NotNull: returns this)
     */
    public InsertOption<CB> disableCommonColumnAutoSetup() {
        _disableCommonColumnAutoSetup = true;
        return this;
    }

    public boolean isCommonColumnAutoSetupDisabled() {
        return _disableCommonColumnAutoSetup;
    }

    // ===================================================================================
    //                                                                     Identity Insert
    //                                                                     ===============
    /**
     * Disable identity for primary key. <br>
     * you can insert by your value for primary key.
     * <pre>
     * Member member = new Member();
     * member.setMemberId(123); <span style="color: #3F7E5E">// instead of identity</span>
     * member.setOthers...(value);
     * memberBhv.varyingInsert(member, op <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     op.<span style="color: #CC4747">disablePrimaryKeyIdentity</span>();
     * });
     * </pre>
     * @return The option of insert. (NotNull: returns this)
     */
    public InsertOption<CB> disablePrimaryKeyIdentity() {
        _disablePrimaryKeyIdentity = true;
        return this;
    }

    public boolean isPrimaryKeyIdentityDisabled() {
        return _disablePrimaryKeyIdentity;
    }

    // ===================================================================================
    //                                                                       Batch Logging
    //                                                                       =============
    /**
     * Limit batch-insert logging by logging size. <br>
     * For example, if you set 3, only 3 records are logged. <br>
     * This also works to SqlLogHandler's call-back and SqlResultInfo's displaySql.
     * @param batchInsertLoggingLimit The limit size of batch-insert logging. (NullAllowed: if null and minus, means no limit)
     */
    public void limitBatchInsertLogging(Integer batchInsertLoggingLimit) {
        this._batchInsertLoggingLimit = batchInsertLoggingLimit;
    }

    public Integer getBatchInsertLoggingLimit() {
        return _batchInsertLoggingLimit;
    }

    // ===================================================================================
    //                                                                           Configure
    //                                                                           =========
    /**
     * Configure statement JDBC options. e.g. queryTimeout, fetchSize, ... (only one-time call)
     * <pre>
     * <span style="color: #0000C0">memberBhv</span>.varyingInsert(member, <span style="color: #553000">op</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">op</span>.<span style="color: #CC4747">configure</span>(<span style="color: #553000">conf</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> <span style="color: #553000">conf</span>.<span style="color: #994747">queryTimeout</span>(<span style="color: #2A00FF">3</span>)));
     * </pre>
     * @param confLambda The callback for configuration of statement for insert. (NotNull)
     */
    public void configure(StatementConfigCall<StatementConfig> confLambda) {
        assertStatementConfigNotDuplicated(confLambda);
        _insertStatementConfig = createStatementConfig(confLambda);
    }

    protected void assertStatementConfigNotDuplicated(StatementConfigCall<StatementConfig> configCall) {
        if (_insertStatementConfig != null) {
            String msg = "Already registered the configuration: existing=" + _insertStatementConfig + ", new=" + configCall;
            throw new IllegalConditionBeanOperationException(msg);
        }
    }

    protected StatementConfig createStatementConfig(StatementConfigCall<StatementConfig> configCall) {
        if (configCall == null) {
            throw new IllegalArgumentException("The argument 'confLambda' should not be null.");
        }
        final StatementConfig config = newStatementConfig();
        configCall.callback(config);
        return config;
    }

    protected StatementConfig newStatementConfig() {
        return new StatementConfig();
    }

    public StatementConfig getInsertStatementConfig() {
        return _insertStatementConfig;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (_disableCommonColumnAutoSetup) {
            sb.append("CommonColumnDisabled");
        }
        if (_disablePrimaryKeyIdentity) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("PKIdentityDisabled");
        }
        if (sb.length() == 0) {
            sb.append("default");
        }
        return DfTypeUtil.toClassTitle(this) + ":{" + sb.toString() + "}";
    }
}