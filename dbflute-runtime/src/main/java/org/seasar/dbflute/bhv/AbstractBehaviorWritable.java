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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.seasar.dbflute.Entity;
import org.seasar.dbflute.bhv.core.CommonColumnAutoSetupper;
import org.seasar.dbflute.bhv.core.command.AbstractListEntityCommand;
import org.seasar.dbflute.bhv.core.command.BatchDeleteCommand;
import org.seasar.dbflute.bhv.core.command.BatchDeleteNonstrictCommand;
import org.seasar.dbflute.bhv.core.command.BatchInsertCommand;
import org.seasar.dbflute.bhv.core.command.BatchUpdateCommand;
import org.seasar.dbflute.bhv.core.command.BatchUpdateNonstrictCommand;
import org.seasar.dbflute.bhv.core.command.DeleteEntityCommand;
import org.seasar.dbflute.bhv.core.command.DeleteNonstrictEntityCommand;
import org.seasar.dbflute.bhv.core.command.QueryDeleteCBCommand;
import org.seasar.dbflute.bhv.core.command.QueryInsertCBCommand;
import org.seasar.dbflute.bhv.core.command.QueryUpdateCBCommand;
import org.seasar.dbflute.bhv.core.command.UpdateEntityCommand;
import org.seasar.dbflute.bhv.core.command.UpdateNonstrictEntityCommand;
import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.cbean.SpecifyQuery;
import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.dbmeta.info.ColumnInfo;
import org.seasar.dbflute.exception.EntityAlreadyDeletedException;
import org.seasar.dbflute.exception.EntityAlreadyUpdatedException;
import org.seasar.dbflute.exception.IllegalBehaviorStateException;
import org.seasar.dbflute.exception.IllegalConditionBeanOperationException;
import org.seasar.dbflute.exception.OptimisticLockColumnValueNullException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.resource.ResourceContext;

/**
 * The abstract class of writable behavior.
 * @param <ENTITY> The type of entity handled by this behavior.
 * @param <CB> The type of condition-bean handled by this behavior.
 * @author jflute
 */
public abstract class AbstractBehaviorWritable<ENTITY extends Entity, CB extends ConditionBean> extends
        AbstractBehaviorReadable<ENTITY, CB> implements BehaviorWritable {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The auto-set-upper of common column. (NotNull) */
    protected CommonColumnAutoSetupper _commonColumnAutoSetupper;

    // ===================================================================================
    //                                                                       Entity Update
    //                                                                       =============
    // -----------------------------------------------------
    //                                                Insert
    //                                                ------
    protected void doInsert(ENTITY entity, InsertOption<CB> option) {
        assertEntityNotNull(entity);
        prepareInsertOption(option);
        delegateInsert(entity, option);
    }

    protected void prepareInsertOption(InsertOption<CB> option) {
        if (option == null) {
            return;
        }
        assertInsertOptionStatus(option);
        if (option.hasSpecifiedInsertColumn()) {
            final CB cb = createCBForSpecifiedUpdate();
            option.resolveInsertColumnSpecification(cb);
        }
    }

    protected void assertInsertOptionStatus(InsertOption<? extends ConditionBean> option) {
        if (option.isCommonColumnAutoSetupDisabled() && !getDBMeta().hasCommonColumn()) {
            String msg = "The common column auto-setup disabling was set to the table not defined common columns:";
            msg = msg + " table=" + getTableDbName() + " option=" + option;
            throw new IllegalStateException(msg);
        }
        if (option.isPrimaryKeyIdentityDisabled() && !getDBMeta().hasIdentity()) {
            String msg = "The identity disabling was set to the table not defined identity:";
            msg = msg + " table=" + getTableDbName() + " option=" + option;
            throw new IllegalStateException(msg);
        }
    }

    protected CB createCBForSpecifiedUpdate() {
        final CB cb = newConditionBean();
        cb.xsetupForSpecifiedUpdate();
        return cb;
    }

    protected void assertInsertOptionNotNull(InsertOption<? extends ConditionBean> option) { // for varyingInsert()
        assertObjectNotNull("option (for insert)", option);
    }

    // -----------------------------------------------------
    //                                                Create
    //                                                ------
    /**
     * {@inheritDoc}
     */
    public void create(Entity entity, InsertOption<? extends ConditionBean> option) {
        doCreate(entity, option);
    }

    protected void doCreate(Entity entity, InsertOption<? extends ConditionBean> option) {
        doInsert(downcast(entity), downcast(option));
    }

    // -----------------------------------------------------
    //                                                Update
    //                                                ------
    protected void doUpdate(ENTITY entity, UpdateOption<CB> option) {
        assertEntityNotNull(entity);
        prepareUpdateOption(option);
        helpUpdateInternally(entity, option);
    }

    protected void doUpdateNonstrict(ENTITY entity, UpdateOption<CB> option) {
        assertEntityNotNull(entity);
        prepareUpdateOption(option);
        helpUpdateNonstrictInternally(entity, option);
    }

    protected void prepareUpdateOption(UpdateOption<CB> option) {
        if (option == null) {
            return;
        }
        assertUpdateOptionStatus(option);
        if (option.hasSelfSpecification()) {
            final CB cb = createCBForVaryingUpdate();
            option.resolveSelfSpecification(cb);
        }
        if (option.hasSpecifiedUpdateColumn()) {
            final CB cb = createCBForSpecifiedUpdate();
            option.resolveUpdateColumnSpecification(cb);
        }
    }

    protected CB createCBForVaryingUpdate() {
        final CB cb = newConditionBean();
        cb.xsetupForVaryingUpdate();
        return cb;
    }

    protected <RESULT extends ENTITY> void helpUpdateInternally(RESULT entity, UpdateOption<CB> option) {
        assertEntityNotNull(entity);
        assertEntityHasOptimisticLockValue(entity);
        final int updatedCount = delegateUpdate(entity, option);
        if (updatedCount == 0) {
            throwUpdateEntityAlreadyDeletedException(entity);
        } else if (updatedCount > 1) {
            throwUpdateEntityDuplicatedException(entity, updatedCount);
        }
    }

    protected <RESULT extends ENTITY> void helpUpdateNonstrictInternally(RESULT entity, UpdateOption<CB> option) {
        assertEntityNotNull(entity);
        final int updatedCount = delegateUpdateNonstrict(entity, option);
        if (updatedCount == 0) {
            throwUpdateEntityAlreadyDeletedException(entity);
        } else if (updatedCount > 1) {
            throwUpdateEntityDuplicatedException(entity, updatedCount);
        }
    }

    protected void throwUpdateEntityAlreadyDeletedException(ENTITY entity) {
        createBhvExThrower().throwUpdateEntityAlreadyDeletedException(entity);
    }

    protected void throwUpdateEntityDuplicatedException(ENTITY entity, int count) {
        createBhvExThrower().throwUpdateEntityDuplicatedException(entity, count);
    }

    protected void assertUpdateOptionStatus(UpdateOption<? extends ConditionBean> option) {
        if (option.isCommonColumnAutoSetupDisabled() && !getDBMeta().hasCommonColumn()) {
            String msg = "The common column auto-setup disabling was set to the table not defined common columns:";
            msg = msg + " table=" + getTableDbName() + " option=" + option;
            throw new IllegalStateException(msg);
        }
    }

    protected void assertUpdateOptionNotNull(UpdateOption<? extends ConditionBean> option) { // for varyingUpdate()
        assertObjectNotNull("option (for update)", option);
    }

    // -----------------------------------------------------
    //                                                Modify
    //                                                ------
    /**
     * {@inheritDoc}
     */
    public void modify(Entity entity, UpdateOption<? extends ConditionBean> option) {
        doModify(entity, option);
    }

    protected void doModify(Entity entity, UpdateOption<? extends ConditionBean> option) {
        doUpdate(downcast(entity), downcast(option));
    }

    /**
     * {@inheritDoc}
     */
    public void modifyNonstrict(Entity entity, UpdateOption<? extends ConditionBean> option) {
        doModifyNonstrict(entity, option);
    }

    protected void doModifyNonstrict(Entity entity, UpdateOption<? extends ConditionBean> option) {
        if (getDBMeta().hasOptimisticLock()) {
            doUpdateNonstrict(downcast(entity), downcast(option));
        } else {
            doUpdate(downcast(entity), downcast(option));
        }
    }

    // -----------------------------------------------------
    //                                      Insert or Update
    //                                      ----------------
    protected void doInsertOrUpdate(ENTITY entity, InsertOption<CB> insertOption, UpdateOption<CB> updateOption) {
        assertEntityNotNull(entity);
        helpInsertOrUpdateInternally(entity, insertOption, updateOption);
    }

    protected void doInsertOrUpdateNonstrict(ENTITY entity, InsertOption<CB> insertOption, UpdateOption<CB> updateOption) {
        assertEntityNotNull(entity);
        helpInsertOrUpdateNonstrictInternally(entity, insertOption, updateOption);
    }

    protected <RESULT extends ENTITY> void helpInsertOrUpdateInternally(RESULT entity, InsertOption<CB> insOption,
            UpdateOption<CB> updOption) {
        assertEntityNotNull(entity);
        if (helpDetermineInsertOrUpdateDirectInsert(entity)) {
            doCreate(entity, insOption);
            return;
        }
        RuntimeException updateException = null;
        try {
            doModify(entity, updOption);
        } catch (EntityAlreadyUpdatedException e) { // already updated (or means not found)
            updateException = e;
        } catch (EntityAlreadyDeletedException e) { // means not found
            updateException = e;
        } catch (OptimisticLockColumnValueNullException e) { // means insert?
            updateException = e;
        }
        if (updateException == null) {
            return;
        }
        final CB cb = newConditionBean();
        final Set<String> uniqueDrivenProperties = entity.myuniqueDrivenProperties();
        if (uniqueDrivenProperties != null && !uniqueDrivenProperties.isEmpty()) {
            for (String prop : uniqueDrivenProperties) {
                final DBMeta dbmeta = entity.getDBMeta();
                final ColumnInfo columnInfo = dbmeta.findColumnInfo(prop);
                final Object value = columnInfo.read(entity); // already checked in update process
                cb.localCQ().invokeQueryEqual(columnInfo.getColumnDbName(), value);
            }
        } else {
            cb.acceptPrimaryKeyMap(getDBMeta().extractPrimaryKeyMap(entity));
        }
        if (readCount(cb) == 0) { // anyway if not found, insert
            doCreate(entity, insOption);
        } else {
            throw updateException;
        }
    }

    protected <RESULT extends ENTITY> void helpInsertOrUpdateNonstrictInternally(RESULT entity,
            InsertOption<? extends ConditionBean> insOption, UpdateOption<? extends ConditionBean> updOption) {
        assertEntityNotNull(entity);
        if (helpDetermineInsertOrUpdateDirectInsert(entity)) {
            doCreate(entity, insOption);
        } else {
            try {
                doModifyNonstrict(entity, updOption);
            } catch (EntityAlreadyDeletedException ignored) { // means not found
                doCreate(entity, insOption);
            }
        }
    }

    protected boolean helpDetermineInsertOrUpdateDirectInsert(Entity entity) {
        final Set<String> uniqueDrivenProperties = entity.myuniqueDrivenProperties();
        if (uniqueDrivenProperties != null && !uniqueDrivenProperties.isEmpty()) {
            return false;
        }
        return !entity.hasPrimaryKeyValue();
    }

    // -----------------------------------------------------
    //                                      Create or Modify
    //                                      ----------------
    /**
     * {@inheritDoc}
     */
    public void createOrModify(Entity entity, InsertOption<? extends ConditionBean> insertOption,
            UpdateOption<? extends ConditionBean> updateOption) {
        doCreateOrModify(entity, insertOption, updateOption);
    }

    protected void doCreateOrModify(Entity entity, InsertOption<? extends ConditionBean> insertOption,
            UpdateOption<? extends ConditionBean> updateOption) {
        doInsertOrUpdate(downcast(entity), downcast(insertOption), downcast(updateOption));
    }

    /**
     * {@inheritDoc}
     */
    public void createOrModifyNonstrict(Entity entity, InsertOption<? extends ConditionBean> insertOption,
            UpdateOption<? extends ConditionBean> updateOption) {
        doCreateOrModifyNonstrict(entity, insertOption, updateOption);
    }

    protected void doCreateOrModifyNonstrict(Entity entity, InsertOption<? extends ConditionBean> insertOption,
            UpdateOption<? extends ConditionBean> updateOption) {
        if (getDBMeta().hasOptimisticLock()) {
            doInsertOrUpdateNonstrict(downcast(entity), downcast(insertOption), downcast(updateOption));
        } else {
            doInsertOrUpdate(downcast(entity), downcast(insertOption), downcast(updateOption));
        }
    }

    // -----------------------------------------------------
    //                                                Delete
    //                                                ------
    protected void doDelete(ENTITY entity, final DeleteOption<CB> option) {
        assertEntityNotNull(entity);
        prepareDeleteOption(option);
        helpDeleteInternally(entity, option);
    }

    protected void doDeleteNonstrict(ENTITY entity, final DeleteOption<CB> option) {
        assertEntityNotNull(entity);
        prepareDeleteOption(option);
        helpDeleteNonstrictInternally(entity, option);
    }

    protected void prepareDeleteOption(DeleteOption<CB> option) {
        if (option != null) {
            assertDeleteOptionStatus(option);
        }
    }

    protected <RESULT extends ENTITY> void helpDeleteInternally(RESULT entity,
            DeleteOption<? extends ConditionBean> option) {
        assertEntityNotNull(entity);
        assertEntityHasOptimisticLockValue(entity);
        final int deletedCount = delegateDelete(entity, option);
        if (deletedCount == 0) {
            throwUpdateEntityAlreadyDeletedException(entity);
        } else if (deletedCount > 1) {
            throwUpdateEntityDuplicatedException(entity, deletedCount);
        }
    }

    protected <RESULT extends ENTITY> void helpDeleteNonstrictInternally(RESULT entity,
            DeleteOption<? extends ConditionBean> option) {
        assertEntityNotNull(entity);
        final int deletedCount = delegateDeleteNonstrict(entity, option);
        if (deletedCount == 0) {
            throwUpdateEntityAlreadyDeletedException(entity);
        } else if (deletedCount > 1) {
            throwUpdateEntityDuplicatedException(entity, deletedCount);
        }
    }

    protected <RESULT extends ENTITY> void helpDeleteNonstrictIgnoreDeletedInternally(RESULT entity,
            DeleteOption<? extends ConditionBean> option) {
        assertEntityNotNull(entity);
        final int deletedCount = delegateDeleteNonstrict(entity, option);
        if (deletedCount == 0) {
            return;
        } else if (deletedCount > 1) {
            throwUpdateEntityDuplicatedException(entity, deletedCount);
        }
    }

    protected void assertDeleteOptionStatus(DeleteOption<? extends ConditionBean> option) {
    }

    protected void assertDeleteOptionNotNull(DeleteOption<? extends ConditionBean> option) { // for varyingDelete()
        assertObjectNotNull("option (for delete)", option);
    }

    // -----------------------------------------------------
    //                                                Remove
    //                                                ------
    /**
     * {@inheritDoc}
     */
    public void remove(Entity entity, DeleteOption<? extends ConditionBean> option) {
        doRemove(entity, option);
    }

    protected void doRemove(Entity entity, DeleteOption<? extends ConditionBean> option) {
        doDelete(downcast(entity), downcast(option));
    }

    /**
     * {@inheritDoc}
     */
    public void removeNonstrict(Entity entity, DeleteOption<? extends ConditionBean> option) {
        doRemoveNonstrict(entity, option);
    }

    protected void doRemoveNonstrict(Entity entity, DeleteOption<? extends ConditionBean> option) {
        if (getDBMeta().hasOptimisticLock()) {
            doDeleteNonstrict(downcast(entity), downcast(option));
        } else {
            doDelete(downcast(entity), downcast(option));
        }
    }

    // ===================================================================================
    //                                                                        Batch Update
    //                                                                        ============
    // -----------------------------------------------------
    //                                          Batch Insert
    //                                          ------------
    protected int[] doBatchInsert(List<ENTITY> entityList, InsertOption<CB> option) {
        assertEntityListNotNull(entityList);
        final InsertOption<CB> rlop;
        if (option != null) {
            rlop = option;
        } else {
            rlop = createPlainInsertOption();
        }
        prepareBatchInsertOption(entityList, rlop); // required
        return delegateBatchInsert(entityList, rlop);
    }

    protected InsertOption<CB> createPlainInsertOption() {
        return new InsertOption<CB>();
    }

    protected <ELEMENT extends ENTITY> void prepareBatchInsertOption(List<ELEMENT> entityList, InsertOption<CB> option) { // might be overridden to set option
        if (isBatchInsertColumnModifiedPropertiesFragmentedDisallowed()) {
            option.xdisallowInsertColumnModifiedPropertiesFragmented(); // default is allowed so use 'disallow' as option
        }
        if (isCompatibleBatchInsertDefaultEveryColumn()) {
            option.xtoBeCompatibleBatchInsertDefaultEveryColumn(); // old style (basically no more use)
        }
        option.xacceptInsertColumnModifiedPropertiesIfNeeds(entityList);
        prepareInsertOption(option);
    }

    protected boolean isBatchInsertColumnModifiedPropertiesFragmentedDisallowed() {
        return false; // might be overridden by generator option 
    }

    protected boolean isCompatibleBatchInsertDefaultEveryColumn() {
        return false; // might be overridden by generator option
    }

    // -----------------------------------------------------
    //                                           Lump Create
    //                                           -----------
    /**
     * {@inheritDoc}
     */
    public int[] lumpCreate(List<? extends Entity> entityList, InsertOption<? extends ConditionBean> option) {
        @SuppressWarnings("unchecked")
        final List<Entity> castList = (List<Entity>) entityList;
        return doLumpCreate(castList, option);
    }

    protected int[] doLumpCreate(List<Entity> entityList, InsertOption<? extends ConditionBean> option) {
        return doBatchInsert(downcast(entityList), downcast(option));
    }

    // -----------------------------------------------------
    //                                          Batch Update
    //                                          ------------
    protected int[] doBatchUpdate(List<ENTITY> entityList, UpdateOption<CB> option) {
        assertEntityListNotNull(entityList);
        final UpdateOption<CB> rlop;
        if (option != null) {
            rlop = option;
        } else {
            rlop = createPlainUpdateOption();
        }
        prepareBatchUpdateOption(entityList, rlop); // required
        return delegateBatchUpdate(entityList, rlop);
    }

    protected int[] doBatchUpdateNonstrict(List<ENTITY> entityList, UpdateOption<CB> option) {
        assertEntityListNotNull(entityList);
        final UpdateOption<CB> rlop;
        if (option != null) {
            rlop = option;
        } else {
            rlop = createPlainUpdateOption();
        }
        prepareBatchUpdateOption(entityList, rlop);
        return delegateBatchUpdateNonstrict(entityList, rlop);
    }

    protected UpdateOption<CB> createPlainUpdateOption() {
        return new UpdateOption<CB>();
    }

    protected UpdateOption<CB> createSpecifiedUpdateOption(SpecifyQuery<CB> updateColumnSpec) {
        assertUpdateColumnSpecificationNotNull(updateColumnSpec);
        final UpdateOption<CB> option = createPlainUpdateOption();
        option.specify(updateColumnSpec);
        return option;
    }

    protected void assertUpdateColumnSpecificationNotNull(SpecifyQuery<? extends ConditionBean> updateColumnSpec) {
        assertObjectNotNull("updateColumnSpec", updateColumnSpec);
    }

    protected <RESULT extends ENTITY> void prepareBatchUpdateOption(List<RESULT> entityList, UpdateOption<CB> option) {
        if (isBatchUpdateColumnModifiedPropertiesFragmentedAllowed()) {
            option.xallowUpdateColumnModifiedPropertiesFragmented(); // default is disallowed so use 'allow' as option
        }
        if (isCompatibleBatchUpdateDefaultEveryColumn()) {
            option.xtoBeCompatibleBatchUpdateDefaultEveryColumn(); // old style (basically no more use)
        }
        option.xacceptUpdateColumnModifiedPropertiesIfNeeds(entityList);
        prepareUpdateOption(option);
    }

    protected boolean isBatchUpdateColumnModifiedPropertiesFragmentedAllowed() {
        return false; // might be overridden by generator option
    }

    protected boolean isCompatibleBatchUpdateDefaultEveryColumn() {
        return false; // might be overridden by generator option
    }

    // -----------------------------------------------------
    //                                           Lump Modify
    //                                           -----------
    /**
     * {@inheritDoc}
     */
    public int[] lumpModify(List<? extends Entity> entityList, UpdateOption<? extends ConditionBean> option) {
        @SuppressWarnings("unchecked")
        final List<Entity> castList = (List<Entity>) entityList;
        return doLumpModify(castList, option);
    }

    protected int[] doLumpModify(List<Entity> entityList, UpdateOption<? extends ConditionBean> option) {
        return doBatchUpdate(downcast(entityList), downcast(option));
    }

    /**
     * {@inheritDoc}
     */
    public int[] lumpModifyNonstrict(List<? extends Entity> entityList, UpdateOption<? extends ConditionBean> option) {
        @SuppressWarnings("unchecked")
        final List<Entity> castList = (List<Entity>) entityList;
        return doLumpModifyNonstrict(castList, option);
    }

    protected int[] doLumpModifyNonstrict(List<Entity> entityList, UpdateOption<? extends ConditionBean> option) {
        if (getDBMeta().hasOptimisticLock()) {
            return doBatchUpdateNonstrict(downcast(entityList), downcast(option));
        } else {
            return doBatchUpdate(downcast(entityList), downcast(option));
        }
    }

    // -----------------------------------------------------
    //                                          Batch Delete
    //                                          ------------
    protected int[] doBatchDelete(List<ENTITY> entityList, DeleteOption<CB> option) {
        assertEntityListNotNull(entityList);
        prepareDeleteOption(option);
        return delegateBatchDelete(entityList, option);
    }

    protected int[] doBatchDeleteNonstrict(List<ENTITY> entityList, DeleteOption<CB> option) {
        assertEntityListNotNull(entityList);
        prepareDeleteOption(option);
        return delegateBatchDeleteNonstrict(entityList, option);
    }

    // -----------------------------------------------------
    //                                           Lump Remove
    //                                           -----------
    /**
     * {@inheritDoc}
     */
    public int[] lumpRemove(List<? extends Entity> entityList, DeleteOption<? extends ConditionBean> option) {
        @SuppressWarnings("unchecked")
        final List<Entity> castList = (List<Entity>) entityList;
        return doLumpRemove(castList, option);
    }

    protected int[] doLumpRemove(List<Entity> entityList, DeleteOption<? extends ConditionBean> option) {
        return doBatchDelete(downcast(entityList), downcast(option));
    }

    /**
     * {@inheritDoc}
     */
    public int[] lumpRemoveNonstrict(List<? extends Entity> entityList, DeleteOption<? extends ConditionBean> option) {
        @SuppressWarnings("unchecked")
        final List<Entity> castList = (List<Entity>) entityList;
        return doLumpRemoveNonstrict(castList, option);
    }

    protected int[] doLumpRemoveNonstrict(List<Entity> entityList, DeleteOption<? extends ConditionBean> option) {
        if (getDBMeta().hasOptimisticLock()) {
            return doBatchDeleteNonstrict(downcast(entityList), downcast(option));
        } else {
            return doBatchDelete(downcast(entityList), downcast(option));
        }
    }

    // =====================================================================================
    //                                                                          Query Update
    //                                                                          ============
    // -----------------------------------------------------
    //                                          Query Insert
    //                                          ------------
    protected int doQueryInsert(QueryInsertSetupper<ENTITY, CB> setupper, InsertOption<CB> option) {
        assertObjectNotNull("setupper", setupper);
        prepareInsertOption(option);
        final ENTITY et = newEntity();
        final CB cb = createCBForQueryInsert();
        return delegateQueryInsert(et, cb, setupper.setup(et, cb), option);
    }

    protected CB createCBForQueryInsert() {
        final CB cb = newConditionBean();
        cb.xsetupForQueryInsert();
        return cb;
    }

    // -----------------------------------------------------
    //                                          Range Create
    //                                          ------------
    /**
     * {@inheritDoc}
     */
    public int rangeCreate(QueryInsertSetupper<? extends Entity, ? extends ConditionBean> setupper,
            InsertOption<? extends ConditionBean> option) {
        return doRangeCreate(setupper, option);
    }

    protected int doRangeCreate(QueryInsertSetupper<? extends Entity, ? extends ConditionBean> setupper,
            InsertOption<? extends ConditionBean> option) {
        return doQueryInsert(downcast(setupper), downcast(option));
    }

    // -----------------------------------------------------
    //                                          Query Update
    //                                          ------------
    protected int doQueryUpdate(ENTITY entity, CB cb, UpdateOption<CB> option) {
        assertObjectNotNull("${myEntityVariableName}", entity);
        assertCBStateValid(cb);
        prepareUpdateOption(option);
        return checkCountBeforeQueryUpdateIfNeeds(cb) ? delegateQueryUpdate(entity, cb, option) : 0;
    }

    /**
     * Check record count before QueryUpdate if it needs. (against MySQL's deadlock of next-key lock)
     * @param cb The condition-bean for QueryUpdate. (NotNull)
     * @return true if record count exists or no check.
     */
    protected boolean checkCountBeforeQueryUpdateIfNeeds(ConditionBean cb) {
        final boolean countExists;
        if (cb.isQueryUpdateCountPreCheck()) {
            countExists = readCount(cb) > 0;
        } else {
            countExists = true; // means no check
        }
        return countExists;
    }

    // -----------------------------------------------------
    //                                          Range Modify
    //                                          ------------
    /**
     * {@inheritDoc}
     */
    public int rangeModify(Entity entity, ConditionBean cb, UpdateOption<? extends ConditionBean> option) {
        return doRangeModify(entity, cb, option);
    }

    protected int doRangeModify(Entity entity, ConditionBean cb, UpdateOption<? extends ConditionBean> option) {
        return doQueryUpdate(downcast(entity), downcast(cb), downcast(option));
    }

    // -----------------------------------------------------
    //                                          Query Delete
    //                                          ------------
    protected int doQueryDelete(CB cb, DeleteOption<CB> option) {
        assertCBStateValid(cb);
        prepareDeleteOption(option);
        return checkCountBeforeQueryUpdateIfNeeds(cb) ? delegateQueryDelete(cb, option) : 0;
    }

    // -----------------------------------------------------
    //                                          Range Remove
    //                                          ------------
    /**
     * {@inheritDoc}
     */
    public int rangeRemove(ConditionBean cb, DeleteOption<? extends ConditionBean> option) {
        return doRangeRemove(cb, option);
    }

    protected int doRangeRemove(ConditionBean cb, DeleteOption<? extends ConditionBean> option) {
        return doQueryDelete(downcast(cb), downcast(option));
    }

    // ===================================================================================
    //                                                                      Delegate Entry
    //                                                                      ==============
    // -----------------------------------------------------
    //                                         Entity Update
    //                                         -------------
    protected int delegateInsert(Entity entity, InsertOption<? extends ConditionBean> option) {
        if (!processBeforeInsert(entity, option)) {
            return 0;
        }
        return invoke(createInsertEntityCommand(entity, option));
    }

    protected int delegateUpdate(Entity entity, UpdateOption<? extends ConditionBean> option) {
        if (!processBeforeUpdate(entity, option)) {
            return 0;
        }
        if (getDBMeta().hasOptimisticLock()) {
            return invoke(createUpdateEntityCommand(entity, option));
        } else {
            return delegateUpdateNonstrict(entity, option);
        }
    }

    protected int delegateUpdateNonstrict(Entity entity, UpdateOption<? extends ConditionBean> option) {
        if (!processBeforeUpdate(entity, option)) {
            return 0;
        }
        return invoke(createUpdateNonstrictEntityCommand(entity, option));
    }

    protected int delegateDelete(Entity entity, DeleteOption<? extends ConditionBean> option) {
        if (!processBeforeDelete(entity, option)) {
            return 0;
        }
        if (getDBMeta().hasOptimisticLock()) {
            return invoke(createDeleteEntityCommand(entity, option));
        } else {
            return delegateDeleteNonstrict(entity, option);
        }
    }

    protected int delegateDeleteNonstrict(Entity entity, DeleteOption<? extends ConditionBean> option) {
        if (!processBeforeDelete(entity, option)) {
            return 0;
        }
        return invoke(createDeleteNonstrictEntityCommand(entity, option));
    }

    // -----------------------------------------------------
    //                                          Batch Update
    //                                          ------------
    protected int[] delegateBatchInsert(List<? extends Entity> entityList, InsertOption<? extends ConditionBean> option) {
        if (entityList.isEmpty()) {
            return new int[] {};
        }
        return invoke(createBatchInsertCommand(processBatchInternally(entityList, option), option));
    }

    protected int[] delegateBatchUpdate(List<? extends Entity> entityList, UpdateOption<? extends ConditionBean> option) {
        if (entityList.isEmpty()) {
            return new int[] {};
        }
        if (getDBMeta().hasOptimisticLock()) {
            return invoke(createBatchUpdateCommand(processBatchInternally(entityList, option, false), option));
        } else {
            return delegateBatchUpdateNonstrict(entityList, option);
        }
    }

    protected int[] delegateBatchUpdateNonstrict(List<? extends Entity> entityList,
            UpdateOption<? extends ConditionBean> option) {
        if (entityList.isEmpty()) {
            return new int[] {};
        }
        return invoke(createBatchUpdateNonstrictCommand(processBatchInternally(entityList, option, true), option));
    }

    protected int[] delegateBatchDelete(List<? extends Entity> entityList, DeleteOption<? extends ConditionBean> option) {
        if (entityList.isEmpty()) {
            return new int[] {};
        }
        if (getDBMeta().hasOptimisticLock()) {
            return invoke(createBatchDeleteCommand(processBatchInternally(entityList, option, false), option));
        } else {
            return delegateBatchDeleteNonstrict(entityList, option);
        }
    }

    protected int[] delegateBatchDeleteNonstrict(List<? extends Entity> entityList,
            DeleteOption<? extends ConditionBean> option) {
        if (entityList.isEmpty()) {
            return new int[] {};
        }
        return invoke(createBatchDeleteNonstrictCommand(processBatchInternally(entityList, option, true), option));
    }

    // -----------------------------------------------------
    //                                          Query Update
    //                                          ------------
    protected int delegateQueryInsert(Entity entity, ConditionBean inCB, ConditionBean resCB,
            InsertOption<? extends ConditionBean> option) {
        if (!processBeforeQueryInsert(entity, inCB, resCB, option)) {
            return 0;
        }
        return invoke(createQueryInsertCBCommand(entity, inCB, resCB, option));
    }

    protected int delegateQueryUpdate(Entity entity, ConditionBean cb, UpdateOption<? extends ConditionBean> option) {
        if (!processBeforeQueryUpdate(entity, cb, option)) {
            return 0;
        }
        return invoke(createQueryUpdateCBCommand(entity, cb, option));
    }

    protected int delegateQueryDelete(ConditionBean cb, DeleteOption<? extends ConditionBean> option) {
        if (!processBeforeQueryDelete(cb, option)) {
            return 0;
        }
        return invoke(createQueryDeleteCBCommand(cb, option));
    }

    // =====================================================================================
    //                                                                        Process Method
    //                                                                        ==============
    // -----------------------------------------------------
    //                                                Insert
    //                                                ------
    /**
     * Process before insert. </br >
     * You can stop the process by your extension.
     * @param entity The entity for insert. (NotNull)
     * @param option The option of insert. (NullAllowed)
     * @return Execution Determination. (true: execute / false: non)
     */
    protected boolean processBeforeInsert(Entity entity, InsertOption<? extends ConditionBean> option) {
        assertEntityNotNull(entity); // primary key is checked later
        frameworkFilterEntityOfInsert(entity, option);
        filterEntityOfInsert(entity, option);
        assertEntityOfInsert(entity, option);
        // check primary key after filtering at an insert process
        // because a primary key value may be set in filtering process
        // (for example, sequence)
        if (!entity.getDBMeta().hasIdentity()) { // identity does not need primary key value here
            assertEntityNotNullAndHasPrimaryKeyValue(entity);
        }
        return true;
    }

    /**
     * Process before query-insert. </br >
     * You can stop the process by your extension.
     * @param entity The entity for query-insert. (NotNull)
     * @param intoCB The condition-bean for inserted table. (NotNull)
     * @param resourceCB The condition-bean for resource table. (NotNull)
     * @param option The option of insert. (NullAllowed)
     * @return Execution Determination. (true: execute / false: non)
     */
    protected boolean processBeforeQueryInsert(Entity entity, ConditionBean intoCB, ConditionBean resourceCB,
            InsertOption<? extends ConditionBean> option) {
        assertEntityNotNull(entity); // query-insert doesn't need to check primary key
        assertObjectNotNull("intoCB", intoCB);
        if (resourceCB == null) {
            String msg = "The set-upper of query-insert should return a condition-bean for resource table:";
            msg = msg + " inserted=" + entity.getTableDbName();
            throw new IllegalConditionBeanOperationException(msg);
        }
        frameworkFilterEntityOfInsert(entity, option);
        setupExclusiveControlColumnOfQueryInsert(entity);
        filterEntityOfInsert(entity, option);
        assertEntityOfInsert(entity, option);
        return true;
    }

    protected void setupExclusiveControlColumnOfQueryInsert(Entity entity) {
        final DBMeta dbmeta = getDBMeta();
        if (dbmeta.hasVersionNo()) {
            final ColumnInfo columnInfo = dbmeta.getVersionNoColumnInfo();
            columnInfo.write(entity, InsertOption.VERSION_NO_FIRST_VALUE);
        }
        if (dbmeta.hasUpdateDate()) {
            final ColumnInfo columnInfo = dbmeta.getUpdateDateColumnInfo();
            columnInfo.write(entity, ResourceContext.getAccessTimestamp());
        }
    }

    /**
     * {Framework Method} Filter the entity of insert.
     * @param entity The entity for insert. (NotNull)
     * @param option The option of insert. (NullAllowed)
     */
    protected void frameworkFilterEntityOfInsert(Entity entity, InsertOption<? extends ConditionBean> option) {
        injectSequenceToPrimaryKeyIfNeeds(entity);
        setupCommonColumnOfInsertIfNeeds(entity, option);
    }

    /**
     * Set up common columns of insert if it needs.
     * @param entity The entity for insert. (NotNull)
     * @param option The option of insert. (NullAllowed)
     */
    protected void setupCommonColumnOfInsertIfNeeds(Entity entity, InsertOption<? extends ConditionBean> option) {
        if (option != null && option.isCommonColumnAutoSetupDisabled()) {
            return;
        }
        final CommonColumnAutoSetupper setupper = getCommonColumnAutoSetupper();
        assertCommonColumnAutoSetupperNotNull();
        setupper.handleCommonColumnOfInsertIfNeeds(entity);
    }

    private void assertCommonColumnAutoSetupperNotNull() {
        if (_commonColumnAutoSetupper != null) {
            return;
        }
        final ExceptionMessageBuilder br = createExceptionMessageBuilder();
        br.addNotice("Not found the auto set-upper of common column in the behavior!");
        br.addItem("Advice");
        br.addElement("Please confirm the definition of the set-upper at your component configuration of DBFlute.");
        br.addItem("Behavior");
        br.addElement("Behavior for " + getTableDbName());
        br.addItem("Attribute");
        br.addElement("behaviorCommandInvoker   : " + _behaviorCommandInvoker);
        br.addElement("behaviorSelector         : " + _behaviorSelector);
        br.addElement("commonColumnAutoSetupper : " + _commonColumnAutoSetupper);
        final String msg = br.buildExceptionMessage();
        throw new IllegalBehaviorStateException(msg);
    }

    /**
     * Filter the entity of insert. (for extension)
     * @param entity The entity for insert. (NotNull)
     * @param option The option of insert. (NullAllowed)
     */
    protected void filterEntityOfInsert(Entity entity, InsertOption<? extends ConditionBean> option) {
    }

    /**
     * Assert the entity of insert. (for extension)
     * @param entity The entity for insert. (NotNull)
     * @param option The option of insert. (NullAllowed)
     */
    protected void assertEntityOfInsert(Entity entity, InsertOption<? extends ConditionBean> option) {
    }

    // -----------------------------------------------------
    //                                                Update
    //                                                ------
    /**
     * Process before update. </br >
     * You can stop the process by your extension.
     * @param entity The entity for update that has primary key. (NotNull)
     * @param option The option of update. (NullAllowed)
     * @return Execution Determination. (true: execute / false: non)
     */
    protected boolean processBeforeUpdate(Entity entity, UpdateOption<? extends ConditionBean> option) {
        assertEntityNotNullAndHasPrimaryKeyValue(entity);
        frameworkFilterEntityOfUpdate(entity, option);
        filterEntityOfUpdate(entity, option);
        assertEntityOfUpdate(entity, option);
        return true;
    }

    /**
     * Process before query-update. </br >
     * You can stop the process by your extension.
     * @param entity The entity for update that is not needed primary key. (NotNull)
     * @param cb The condition-bean for query. (NotNull)
     * @param option The option of update. (NullAllowed)
     * @return Execution Determination. (true: execute / false: non)
     */
    protected boolean processBeforeQueryUpdate(Entity entity, ConditionBean cb,
            UpdateOption<? extends ConditionBean> option) {
        assertEntityNotNull(entity); // query-update doesn't need to check primary key
        assertCBStateValid(cb);
        frameworkFilterEntityOfUpdate(entity, option);
        filterEntityOfUpdate(entity, option);
        assertEntityOfUpdate(entity, option);
        assertQueryUpdateStatus(entity, cb, option);
        return true;
    }

    /**
     * {Framework Method} Filter the entity of update.
     * @param entity The entity for update. (NotNull)
     * @param option The option of update. (NullAllowed)
     */
    protected void frameworkFilterEntityOfUpdate(Entity entity, UpdateOption<? extends ConditionBean> option) {
        setupCommonColumnOfUpdateIfNeeds(entity, option);
    }

    /**
     * Set up common columns of update if it needs.
     * @param entity The entity for update. (NotNull)
     * @param option The option of update. (NullAllowed)
     */
    protected void setupCommonColumnOfUpdateIfNeeds(Entity entity, UpdateOption<? extends ConditionBean> option) {
        if (option != null && option.isCommonColumnAutoSetupDisabled()) {
            return;
        }
        final CommonColumnAutoSetupper setupper = getCommonColumnAutoSetupper();
        assertCommonColumnAutoSetupperNotNull();
        setupper.handleCommonColumnOfUpdateIfNeeds(entity);
    }

    /**
     * Filter the entity of update. (for extension)
     * @param entity The entity for update. (NotNull)
     * @param option The option of update. (NullAllowed)
     */
    protected void filterEntityOfUpdate(Entity entity, UpdateOption<? extends ConditionBean> option) {
    }

    /**
     * Assert the entity of update. (for extension)
     * @param entity The entity for update. (NotNull)
     * @param option The option of update. (NullAllowed)
     */
    protected void assertEntityOfUpdate(Entity entity, UpdateOption<? extends ConditionBean> option) {
    }

    /**
     * Assert that the query-update is legal status.
     * @param entity The entity for query-update. (NotNull)
     * @param cb The condition-bean for query-update. (NotNull)
     * @param option The option of update. (NullAllowed)
     */
    protected void assertQueryUpdateStatus(Entity entity, ConditionBean cb, UpdateOption<? extends ConditionBean> option) {
        if (option != null && option.isNonQueryUpdateAllowed()) {
            return;
        }
        if (cb.hasSelectAllPossible()) {
            createBhvExThrower().throwNonQueryUpdateNotAllowedException(entity, cb, option);
        }
    }

    // -----------------------------------------------------
    //                                                Delete
    //                                                ------
    /**
     * Process before delete. </br >
     * You can stop the process by your extension.
     * @param entity The entity for delete that has primary key. (NotNull)
     * @param option The option of delete. (NullAllowed)
     * @return Execution Determination. (true: execute / false: non)
     */
    protected boolean processBeforeDelete(Entity entity, DeleteOption<? extends ConditionBean> option) {
        assertEntityNotNullAndHasPrimaryKeyValue(entity);
        frameworkFilterEntityOfDelete(entity, option);
        filterEntityOfDelete(entity, option);
        assertEntityOfDelete(entity, option);
        return true;
    }

    /**
     * Process before query-delete. </br >
     * You can stop the process by your extension.
     * @param cb The condition-bean for query. (NotNull)
     * @param option The option of delete. (NullAllowed)
     * @return Execution Determination. (true: execute / false: non)
     */
    protected boolean processBeforeQueryDelete(ConditionBean cb, DeleteOption<? extends ConditionBean> option) {
        assertCBStateValid(cb);
        assertQueryDeleteStatus(cb, option);
        return true;
    }

    /**
     * {Framework Method} Filter the entity of delete. {not called if query-delete}
     * @param entity The entity for delete that has primary key. (NotNull)
     * @param option The option of delete. (NullAllowed)
     */
    protected void frameworkFilterEntityOfDelete(Entity entity, DeleteOption<? extends ConditionBean> option) {
    }

    /**
     * Filter the entity of delete. (for extension) {not called if query-delete}
     * @param entity The entity for delete that has primary key. (NotNull)
     * @param option The option of delete. (NullAllowed)
     */
    protected void filterEntityOfDelete(Entity entity, DeleteOption<? extends ConditionBean> option) {
    }

    /**
     * Assert the entity of delete. (for extension) {not called if query-delete}
     * @param entity The entity for delete that has primary key. (NotNull)
     * @param option The option of delete. (NullAllowed)
     */
    protected void assertEntityOfDelete(Entity entity, DeleteOption<? extends ConditionBean> option) {
    }

    /**
     * Assert that the query-delete is legal status.
     * @param cb The condition-bean for query-delete. (NotNull)
     * @param option The option of delete. (NullAllowed)
     */
    protected void assertQueryDeleteStatus(ConditionBean cb, DeleteOption<? extends ConditionBean> option) {
        if (option != null && option.isNonQueryDeleteAllowed()) {
            return;
        }
        if (cb.hasSelectAllPossible()) {
            createBhvExThrower().throwNonQueryDeleteNotAllowedException(cb, option);
        }
    }

    // -----------------------------------------------------
    //                                                Common
    //                                                ------
    protected void injectSequenceToPrimaryKeyIfNeeds(Entity entity) {
        final DBMeta dbmeta = entity.getDBMeta();
        if (!dbmeta.hasSequence() || dbmeta.hasCompoundPrimaryKey() || entity.hasPrimaryKeyValue()) {
            return;
        }
        // basically property(column) type is same as next value type
        // so there is NOT type conversion cost when writing to the entity
        dbmeta.getPrimaryUniqueInfo().getFirstColumn().write(entity, readNextVal());
    }

    protected void assertEntityHasOptimisticLockValue(Entity entity) {
        assertEntityHasVersionNoValue(entity);
        assertEntityHasUpdateDateValue(entity);
    }

    protected void assertEntityHasVersionNoValue(Entity entity) {
        if (!getDBMeta().hasVersionNo()) {
            return;
        }
        if (hasVersionNoValue(entity)) {
            return;
        }
        throwVersionNoValueNullException(entity);
    }

    protected void throwVersionNoValueNullException(Entity entity) {
        createBhvExThrower().throwVersionNoValueNullException(entity);
    }

    protected void assertEntityHasUpdateDateValue(Entity entity) {
        if (!getDBMeta().hasUpdateDate()) {
            return;
        }
        if (hasUpdateDateValue(entity)) {
            return;
        }
        throwUpdateDateValueNullException(entity);
    }

    protected void throwUpdateDateValueNullException(Entity entity) {
        createBhvExThrower().throwUpdateDateValueNullException(entity);
    }

    // -----------------------------------------------------
    //                                                 Batch
    //                                                 -----
    protected <ELEMENT extends Entity> List<ELEMENT> processBatchInternally(List<ELEMENT> entityList,
            InsertOption<? extends ConditionBean> option) {
        assertObjectNotNull("entityList", entityList);
        final List<ELEMENT> filteredList = new ArrayList<ELEMENT>();
        for (ELEMENT entity : entityList) {
            if (!processBeforeInsert(entity, option)) {
                continue;
            }
            filteredList.add(entity);
        }
        return filteredList;
    }

    protected <ELEMENT extends Entity> List<ELEMENT> processBatchInternally(List<ELEMENT> entityList,
            UpdateOption<? extends ConditionBean> option, boolean nonstrict) {
        assertObjectNotNull("entityList", entityList);
        final List<ELEMENT> filteredList = new ArrayList<ELEMENT>();
        for (ELEMENT entity : entityList) {
            if (!processBeforeUpdate(entity, option)) {
                continue;
            }
            if (!nonstrict) {
                assertEntityHasOptimisticLockValue(entity);
            }
            filteredList.add(entity);
        }
        return filteredList;
    }

    protected <ELEMENT extends Entity> List<ELEMENT> processBatchInternally(List<ELEMENT> entityList,
            DeleteOption<? extends ConditionBean> option, boolean nonstrict) {
        assertObjectNotNull("entityList", entityList);
        final List<ELEMENT> filteredList = new ArrayList<ELEMENT>();
        for (ELEMENT entity : entityList) {
            if (!processBeforeDelete(entity, option)) {
                continue;
            }
            if (!nonstrict) {
                assertEntityHasOptimisticLockValue(entity);
            }
            filteredList.add(entity);
        }
        return filteredList;
    }

    // ===================================================================================
    //                                                                    Behavior Command
    //                                                                    ================
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    // an insert command creation is defined on the readable interface for non-primary key value

    protected UpdateEntityCommand createUpdateEntityCommand(Entity entity, UpdateOption<? extends ConditionBean> option) {
        assertBehaviorCommandInvoker("createUpdateEntityCommand");
        final UpdateEntityCommand cmd = newUpdateEntityCommand();
        xsetupEntityCommand(cmd, entity);
        cmd.setUpdateOption(option);
        return cmd;
    }

    protected UpdateEntityCommand newUpdateEntityCommand() {
        return new UpdateEntityCommand();
    }

    protected UpdateNonstrictEntityCommand createUpdateNonstrictEntityCommand(Entity entity,
            UpdateOption<? extends ConditionBean> option) {
        assertBehaviorCommandInvoker("createUpdateNonstrictEntityCommand");
        final UpdateNonstrictEntityCommand cmd = newUpdateNonstrictEntityCommand();
        xsetupEntityCommand(cmd, entity);
        cmd.setUpdateOption(option);
        return cmd;
    }

    protected UpdateNonstrictEntityCommand newUpdateNonstrictEntityCommand() {
        return new UpdateNonstrictEntityCommand();
    }

    protected DeleteEntityCommand createDeleteEntityCommand(Entity entity, DeleteOption<? extends ConditionBean> option) {
        assertBehaviorCommandInvoker("createDeleteEntityCommand");
        final DeleteEntityCommand cmd = newDeleteEntityCommand();
        xsetupEntityCommand(cmd, entity);
        cmd.setDeleteOption(option);
        return cmd;
    }

    protected DeleteEntityCommand newDeleteEntityCommand() {
        return new DeleteEntityCommand();
    }

    protected DeleteNonstrictEntityCommand createDeleteNonstrictEntityCommand(Entity entity,
            DeleteOption<? extends ConditionBean> option) {
        assertBehaviorCommandInvoker("createDeleteNonstrictEntityCommand");
        final DeleteNonstrictEntityCommand cmd = newDeleteNonstrictEntityCommand();
        xsetupEntityCommand(cmd, entity);
        cmd.setDeleteOption(option);
        return cmd;
    }

    protected DeleteNonstrictEntityCommand newDeleteNonstrictEntityCommand() {
        return new DeleteNonstrictEntityCommand();
    }

    // -----------------------------------------------------
    //                                                 Batch
    //                                                 -----
    protected BatchInsertCommand createBatchInsertCommand(List<? extends Entity> entityList,
            InsertOption<? extends ConditionBean> option) {
        assertBehaviorCommandInvoker("createBatchInsertCommand");
        final BatchInsertCommand cmd = newBatchInsertCommand();
        xsetupListEntityCommand(cmd, entityList);
        cmd.setInsertOption(option);
        return cmd;
    }

    protected BatchInsertCommand newBatchInsertCommand() {
        return new BatchInsertCommand();
    }

    protected BatchUpdateCommand createBatchUpdateCommand(List<? extends Entity> entityList,
            UpdateOption<? extends ConditionBean> option) {
        assertBehaviorCommandInvoker("createBatchUpdateCommand");
        final BatchUpdateCommand cmd = newBatchUpdateCommand();
        xsetupListEntityCommand(cmd, entityList);
        cmd.setUpdateOption(option);
        return cmd;
    }

    protected BatchUpdateCommand newBatchUpdateCommand() {
        return new BatchUpdateCommand();
    }

    protected BatchUpdateNonstrictCommand createBatchUpdateNonstrictCommand(List<? extends Entity> entityList,
            UpdateOption<? extends ConditionBean> option) {
        assertBehaviorCommandInvoker("createBatchUpdateNonstrictCommand");
        final BatchUpdateNonstrictCommand cmd = newBatchUpdateNonstrictCommand();
        xsetupListEntityCommand(cmd, entityList);
        cmd.setUpdateOption(option);
        return cmd;
    }

    protected BatchUpdateNonstrictCommand newBatchUpdateNonstrictCommand() {
        return new BatchUpdateNonstrictCommand();
    }

    protected BatchDeleteCommand createBatchDeleteCommand(List<? extends Entity> entityList,
            DeleteOption<? extends ConditionBean> option) {
        assertBehaviorCommandInvoker("createBatchDeleteCommand");
        final BatchDeleteCommand cmd = newBatchDeleteCommand();
        xsetupListEntityCommand(cmd, entityList);
        cmd.setDeleteOption(option);
        return cmd;
    }

    protected BatchDeleteCommand newBatchDeleteCommand() {
        return new BatchDeleteCommand();
    }

    protected BatchDeleteNonstrictCommand createBatchDeleteNonstrictCommand(List<? extends Entity> entityList,
            DeleteOption<? extends ConditionBean> option) {
        assertBehaviorCommandInvoker("createBatchDeleteNonstrictCommand");
        final BatchDeleteNonstrictCommand cmd = newBatchDeleteNonstrictCommand();
        xsetupListEntityCommand(cmd, entityList);
        cmd.setDeleteOption(option);
        return cmd;
    }

    protected BatchDeleteNonstrictCommand newBatchDeleteNonstrictCommand() {
        return new BatchDeleteNonstrictCommand();
    }

    /**
     * @param command The command of behavior. (NotNull)
     * @param entityList The list of entity. (NotNull, NotEmpty)
     */
    protected void xsetupListEntityCommand(AbstractListEntityCommand command, List<? extends Entity> entityList) {
        if (entityList.isEmpty()) {
            String msg = "The argument 'entityList' should not be empty: " + entityList;
            throw new IllegalStateException(msg);
        }
        command.setTableDbName(getTableDbName());
        _behaviorCommandInvoker.injectComponentProperty(command);
        command.setEntityType(entityList.get(0).getClass()); // *The list should not be empty!
        command.setEntityList(entityList);
    }

    // -----------------------------------------------------
    //                                                 Query
    //                                                 -----
    protected QueryInsertCBCommand createQueryInsertCBCommand(Entity entity, ConditionBean intoCB,
            ConditionBean resourceCB, InsertOption<? extends ConditionBean> option) {
        assertBehaviorCommandInvoker("createQueryInsertCBCommand");
        final QueryInsertCBCommand cmd = new QueryInsertCBCommand();
        cmd.setTableDbName(getTableDbName());
        _behaviorCommandInvoker.injectComponentProperty(cmd);
        cmd.setEntity(entity);
        cmd.setIntoConditionBean(intoCB);
        cmd.setConditionBean(resourceCB);
        cmd.setInsertOption(option);
        return cmd;
    }

    protected QueryUpdateCBCommand createQueryUpdateCBCommand(Entity entity, ConditionBean cb,
            UpdateOption<? extends ConditionBean> option) {
        assertBehaviorCommandInvoker("createQueryUpdateCBCommand");
        final QueryUpdateCBCommand cmd = new QueryUpdateCBCommand();
        cmd.setTableDbName(getTableDbName());
        _behaviorCommandInvoker.injectComponentProperty(cmd);
        cmd.setEntity(entity);
        cmd.setConditionBean(cb);
        cmd.setUpdateOption(option);
        return cmd;
    }

    protected QueryDeleteCBCommand createQueryDeleteCBCommand(ConditionBean cb,
            DeleteOption<? extends ConditionBean> option) {
        assertBehaviorCommandInvoker("createQueryDeleteCBCommand");
        final QueryDeleteCBCommand cmd = new QueryDeleteCBCommand();
        cmd.setTableDbName(getTableDbName());
        _behaviorCommandInvoker.injectComponentProperty(cmd);
        cmd.setConditionBean(cb);
        cmd.setDeleteOption(option);
        return cmd;
    }

    // ===================================================================================
    //                                                                         Type Helper
    //                                                                         ===========
    @SuppressWarnings("unchecked")
    protected InsertOption<CB> downcast(InsertOption<? extends ConditionBean> option) {
        return (InsertOption<CB>) option;
    }

    @SuppressWarnings("unchecked")
    protected UpdateOption<CB> downcast(UpdateOption<? extends ConditionBean> option) {
        return (UpdateOption<CB>) option;
    }

    @SuppressWarnings("unchecked")
    protected DeleteOption<CB> downcast(DeleteOption<? extends ConditionBean> option) {
        return (DeleteOption<CB>) option;
    }

    @SuppressWarnings("unchecked")
    protected QueryInsertSetupper<ENTITY, CB> downcast(
            QueryInsertSetupper<? extends Entity, ? extends ConditionBean> setupper) {
        return (QueryInsertSetupper<ENTITY, CB>) setupper;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    /**
     * Get the auto set-upper of common column.
     * @return The auto set-upper of common column. (NullAllowed: But normally NotNull)
     */
    protected CommonColumnAutoSetupper getCommonColumnAutoSetupper() {
        return _commonColumnAutoSetupper;
    }

    /**
     * Set the auto set-upper of common column.
     * @param commonColumnAutoSetupper The auto set-upper of common column. (NotNull)
     */
    public void setCommonColumnAutoSetupper(CommonColumnAutoSetupper commonColumnAutoSetupper) {
        this._commonColumnAutoSetupper = commonColumnAutoSetupper;
    }
}
