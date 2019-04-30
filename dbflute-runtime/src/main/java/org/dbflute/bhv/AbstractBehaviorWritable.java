/*
 * Copyright 2014-2019 the original author or authors.
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
package org.dbflute.bhv;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.dbflute.Entity;
import org.dbflute.bhv.core.BehaviorCommandMeta;
import org.dbflute.bhv.core.command.AbstractBatchUpdateCommand;
import org.dbflute.bhv.core.command.BatchDeleteCommand;
import org.dbflute.bhv.core.command.BatchDeleteNonstrictCommand;
import org.dbflute.bhv.core.command.BatchInsertCommand;
import org.dbflute.bhv.core.command.BatchUpdateCommand;
import org.dbflute.bhv.core.command.BatchUpdateNonstrictCommand;
import org.dbflute.bhv.core.command.DeleteEntityCommand;
import org.dbflute.bhv.core.command.DeleteNonstrictEntityCommand;
import org.dbflute.bhv.core.command.InsertEntityCommand;
import org.dbflute.bhv.core.command.QueryDeleteCBCommand;
import org.dbflute.bhv.core.command.QueryInsertCBCommand;
import org.dbflute.bhv.core.command.QueryUpdateCBCommand;
import org.dbflute.bhv.core.command.UpdateEntityCommand;
import org.dbflute.bhv.core.command.UpdateNonstrictEntityCommand;
import org.dbflute.bhv.core.context.ResourceContext;
import org.dbflute.bhv.writable.DeleteOption;
import org.dbflute.bhv.writable.InsertOption;
import org.dbflute.bhv.writable.QueryInsertSetupper;
import org.dbflute.bhv.writable.UpdateOption;
import org.dbflute.bhv.writable.WritableOptionCall;
import org.dbflute.cbean.ConditionBean;
import org.dbflute.cbean.scoping.SpecifyQuery;
import org.dbflute.dbmeta.DBMeta;
import org.dbflute.dbmeta.info.ColumnInfo;
import org.dbflute.dbmeta.info.UniqueInfo;
import org.dbflute.exception.EntityAlreadyDeletedException;
import org.dbflute.exception.EntityAlreadyUpdatedException;
import org.dbflute.exception.IllegalBehaviorStateException;
import org.dbflute.exception.IllegalConditionBeanOperationException;
import org.dbflute.exception.OptimisticLockColumnValueNullException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.hook.CommonColumnAutoSetupper;
import org.dbflute.optional.OptionalThing;

/**
 * The abstract class of writable behavior.
 * @param <ENTITY> The type of entity handled by this behavior.
 * @param <CB> The type of condition-bean handled by this behavior.
 * @author jflute
 */
public abstract class AbstractBehaviorWritable<ENTITY extends Entity, CB extends ConditionBean> extends AbstractBehaviorReadable<ENTITY, CB>
        implements BehaviorWritable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final int[] EMPTY_INT_ARRAY = new int[] {};

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
        if (option.isCommonColumnAutoSetupDisabled() && !asDBMeta().hasCommonColumn()) {
            String msg = "The common column auto-setup disabling was set to the table not defined common columns:";
            msg = msg + " table=" + asTableDbName() + " option=" + option;
            throw new IllegalStateException(msg);
        }
        if (option.isPrimaryKeyIdentityDisabled() && !asDBMeta().hasIdentity()) {
            String msg = "The identity disabling was set to the table not defined identity:";
            msg = msg + " table=" + asTableDbName() + " option=" + option;
            throw new IllegalStateException(msg);
        }
    }

    protected CB createCBForSpecifiedUpdate() {
        final CB cb = newConditionBean();
        cb.xsetupForSpecifiedUpdate();
        return cb;
    }

    protected InsertOption<CB> createInsertOption(WritableOptionCall<CB, InsertOption<CB>> opCall) {
        assertInsertOpCallNotNull(opCall);
        final InsertOption<CB> op = newInsertOption();
        opCall.callback(op);
        return op;
    }

    protected InsertOption<CB> newInsertOption() {
        return new InsertOption<CB>();
    }

    protected void assertInsertOpCallNotNull(WritableOptionCall<CB, InsertOption<CB>> opCall) { // for varyingInsert()
        assertObjectNotNull("opLambda (for insert)", opCall);
    }

    // -----------------------------------------------------
    //                                                Create
    //                                                ------
    /** {@inheritDoc} */
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
        prepareEntityUpdate(entity, option);
        helpUpdateInternally(entity, option);
    }

    protected void doUpdateNonstrict(ENTITY entity, UpdateOption<CB> option) {
        prepareEntityUpdate(entity, option);
        helpUpdateNonstrictInternally(entity, option);
    }

    protected void prepareEntityUpdate(ENTITY entity, UpdateOption<CB> option) {
        assertEntityNotNull(entity);
        prepareUpdateOption(option);
        prepareEntityUpdateOption(entity, option);
    }

    protected void prepareUpdateOption(UpdateOption<CB> option) { // all update commands
        if (option == null) {
            return;
        }
        assertUpdateOptionStatus(option);
        if (option.hasSelfSpecification()) {
            option.resolveSelfSpecification(() -> createCBForVaryingUpdate());
        }
        if (option.hasSpecifiedUpdateColumn()) {
            final CB cb = createCBForSpecifiedUpdate();
            option.resolveUpdateColumnSpecification(cb);
        }
    }

    protected void prepareEntityUpdateOption(ENTITY entity, UpdateOption<CB> option) { // only for entity update
        if (option == null) {
            return;
        }
        if (option.hasUniqueByUniqueInfo()) {
            reflectUniqueDriven(entity, option.getUniqueByUniqueInfo());
        }
    }

    protected void reflectUniqueDriven(ENTITY entity, UniqueInfo uniqueInfo) {
        final List<ColumnInfo> uniqueColumnList = uniqueInfo.getUniqueColumnList();
        for (ColumnInfo columnInfo : uniqueColumnList) {
            entity.myuniqueByProperty(columnInfo.getPropertyName());
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
        helpReloadPrimaryKeyIfUniqueByIfNeeds(entity, option);
    }

    protected <RESULT extends ENTITY> void helpUpdateNonstrictInternally(RESULT entity, UpdateOption<CB> option) {
        assertEntityNotNull(entity);
        final int updatedCount = delegateUpdateNonstrict(entity, option);
        if (updatedCount == 0) {
            throwUpdateEntityAlreadyDeletedException(entity);
        } else if (updatedCount > 1) {
            throwUpdateEntityDuplicatedException(entity, updatedCount);
        }
        helpReloadPrimaryKeyIfUniqueByIfNeeds(entity, option);
    }

    protected void throwUpdateEntityAlreadyDeletedException(ENTITY entity) {
        createBhvExThrower().throwUpdateEntityAlreadyDeletedException(entity);
    }

    protected void throwUpdateEntityDuplicatedException(ENTITY entity, int count) {
        createBhvExThrower().throwUpdateEntityDuplicatedException(entity, count);
    }

    protected void assertUpdateOptionStatus(UpdateOption<? extends ConditionBean> option) {
        if (option.isCommonColumnAutoSetupDisabled() && !asDBMeta().hasCommonColumn()) {
            String msg = "The common column auto-setup disabling was set to the table not defined common columns:";
            msg = msg + " table=" + asTableDbName() + " option=" + option;
            throw new IllegalStateException(msg);
        }
    }

    protected UpdateOption<CB> createUpdateOption(WritableOptionCall<CB, UpdateOption<CB>> opCall) {
        assertUpdateOpCallNotNull(opCall);
        final UpdateOption<CB> op = newUpdateOption();
        opCall.callback(op);
        return op;
    }

    protected UpdateOption<CB> newUpdateOption() {
        return new UpdateOption<CB>();
    }

    protected void assertUpdateOpCallNotNull(WritableOptionCall<CB, UpdateOption<CB>> opCall) { // for varyingUpdate()
        assertObjectNotNull("opLambda (for update)", opCall);
    }

    protected <RESULT extends ENTITY> void helpReloadPrimaryKeyIfUniqueByIfNeeds(RESULT entity, UpdateOption<CB> option) {
        if (option == null || !option.isReloadPrimaryKeyIfUniqueBy()) {
            return;
        }
        final Set<String> uniqueProp = entity.myuniqueDrivenProperties();
        if (uniqueProp.isEmpty()) { // updated by PK normally
            return;
        }
        final DBMeta dbmeta = entity.asDBMeta();
        if (!dbmeta.hasPrimaryKey()) { // no PK table but has unique key
            return;
        }
        final CB cb = newConditionBean();
        final List<ColumnInfo> pkList = dbmeta.getPrimaryInfo().getPrimaryColumnList();
        for (ColumnInfo pk : pkList) {
            cb.invokeSpecifyColumn(pk.getPropertyName());
        }
        for (String uq : uniqueProp) {
            cb.localCQ().invokeQueryEqual(uq, dbmeta.findColumnInfo(uq).read(entity));
        }
        final Entity read = readEntityWithDeletedCheck(cb);
        dbmeta.acceptPrimaryKeyMap(entity, dbmeta.extractPrimaryKeyMap(read));
    }

    // -----------------------------------------------------
    //                                                Modify
    //                                                ------
    /** {@inheritDoc} */
    public void modify(Entity entity, UpdateOption<? extends ConditionBean> option) {
        doModify(entity, option);
    }

    protected void doModify(Entity entity, UpdateOption<? extends ConditionBean> option) {
        doUpdate(downcast(entity), downcast(option));
    }

    /** {@inheritDoc} */
    public void modifyNonstrict(Entity entity, UpdateOption<? extends ConditionBean> option) {
        doModifyNonstrict(entity, option);
    }

    protected void doModifyNonstrict(Entity entity, UpdateOption<? extends ConditionBean> option) {
        if (asDBMeta().hasOptimisticLock()) {
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
                final DBMeta dbmeta = entity.asDBMeta();
                final ColumnInfo columnInfo = dbmeta.findColumnInfo(prop);
                final Object value = columnInfo.read(entity); // already checked in update process
                cb.localCQ().invokeQueryEqual(columnInfo.getColumnDbName(), value);
            }
        } else {
            cb.acceptPrimaryKeyMap(asDBMeta().extractPrimaryKeyMap(entity));
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
    /** {@inheritDoc} */
    public void createOrModify(Entity entity, InsertOption<? extends ConditionBean> insertOption,
            UpdateOption<? extends ConditionBean> updateOption) {
        doCreateOrModify(entity, insertOption, updateOption);
    }

    protected void doCreateOrModify(Entity entity, InsertOption<? extends ConditionBean> insertOption,
            UpdateOption<? extends ConditionBean> updateOption) {
        doInsertOrUpdate(downcast(entity), downcast(insertOption), downcast(updateOption));
    }

    /** {@inheritDoc} */
    public void createOrModifyNonstrict(Entity entity, InsertOption<? extends ConditionBean> insertOption,
            UpdateOption<? extends ConditionBean> updateOption) {
        doCreateOrModifyNonstrict(entity, insertOption, updateOption);
    }

    protected void doCreateOrModifyNonstrict(Entity entity, InsertOption<? extends ConditionBean> insertOption,
            UpdateOption<? extends ConditionBean> updateOption) {
        if (asDBMeta().hasOptimisticLock()) {
            doInsertOrUpdateNonstrict(downcast(entity), downcast(insertOption), downcast(updateOption));
        } else {
            doInsertOrUpdate(downcast(entity), downcast(insertOption), downcast(updateOption));
        }
    }

    // -----------------------------------------------------
    //                                                Delete
    //                                                ------
    protected void doDelete(ENTITY entity, DeleteOption<CB> option) {
        prepareEntityDelete(entity, option);
        helpDeleteInternally(entity, option);
    }

    protected void doDeleteNonstrict(ENTITY entity, DeleteOption<CB> option) {
        prepareEntityDelete(entity, option);
        helpDeleteNonstrictInternally(entity, option);
    }

    protected void prepareEntityDelete(ENTITY entity, DeleteOption<CB> option) {
        assertEntityNotNull(entity);
        prepareDeleteOption(option);
        prepareEntityDeleteOption(entity, option);
    }

    protected void prepareDeleteOption(DeleteOption<CB> option) { // all delete commands
        if (option != null) {
            assertDeleteOptionStatus(option);
        }
    }

    protected void prepareEntityDeleteOption(ENTITY entity, DeleteOption<CB> option) { // only for entity delete
        if (option == null) {
            return;
        }
        if (option.hasUniqueByUniqueInfo()) {
            reflectUniqueDriven(entity, option.getUniqueByUniqueInfo());
        }
    }

    protected <RESULT extends ENTITY> void helpDeleteInternally(RESULT entity, DeleteOption<? extends ConditionBean> option) {
        assertEntityNotNull(entity);
        assertEntityHasOptimisticLockValue(entity);
        final int deletedCount = delegateDelete(entity, option);
        if (deletedCount == 0) {
            throwUpdateEntityAlreadyDeletedException(entity);
        } else if (deletedCount > 1) {
            throwUpdateEntityDuplicatedException(entity, deletedCount);
        }
    }

    protected <RESULT extends ENTITY> void helpDeleteNonstrictInternally(RESULT entity, DeleteOption<? extends ConditionBean> option) {
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

    protected DeleteOption<CB> createDeleteOption(WritableOptionCall<CB, DeleteOption<CB>> opCall) {
        assertDeleteOpCallNotNull(opCall);
        final DeleteOption<CB> op = newDeleteOption();
        opCall.callback(op);
        return op;
    }

    protected DeleteOption<CB> newDeleteOption() {
        return new DeleteOption<CB>();
    }

    protected void assertDeleteOpCallNotNull(WritableOptionCall<CB, DeleteOption<CB>> opCall) { // for varyingDelete()
        assertObjectNotNull("opLambda (for delete)", opCall);
    }

    // -----------------------------------------------------
    //                                                Remove
    //                                                ------
    /** {@inheritDoc} */
    public void remove(Entity entity, DeleteOption<? extends ConditionBean> option) {
        doRemove(entity, option);
    }

    protected void doRemove(Entity entity, DeleteOption<? extends ConditionBean> option) {
        doDelete(downcast(entity), downcast(option));
    }

    /** {@inheritDoc} */
    public void removeNonstrict(Entity entity, DeleteOption<? extends ConditionBean> option) {
        doRemoveNonstrict(entity, option);
    }

    protected void doRemoveNonstrict(Entity entity, DeleteOption<? extends ConditionBean> option) {
        if (asDBMeta().hasOptimisticLock()) {
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
        return newInsertOption();
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
    /** {@inheritDoc} */
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
    /** {@inheritDoc} */
    public int[] lumpModify(List<? extends Entity> entityList, UpdateOption<? extends ConditionBean> option) {
        @SuppressWarnings("unchecked")
        final List<Entity> castList = (List<Entity>) entityList;
        return doLumpModify(castList, option);
    }

    protected int[] doLumpModify(List<Entity> entityList, UpdateOption<? extends ConditionBean> option) {
        return doBatchUpdate(downcast(entityList), downcast(option));
    }

    /** {@inheritDoc} */
    public int[] lumpModifyNonstrict(List<? extends Entity> entityList, UpdateOption<? extends ConditionBean> option) {
        @SuppressWarnings("unchecked")
        final List<Entity> castList = (List<Entity>) entityList;
        return doLumpModifyNonstrict(castList, option);
    }

    protected int[] doLumpModifyNonstrict(List<Entity> entityList, UpdateOption<? extends ConditionBean> option) {
        if (asDBMeta().hasOptimisticLock()) {
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
    /** {@inheritDoc} */
    public int[] lumpRemove(List<? extends Entity> entityList, DeleteOption<? extends ConditionBean> option) {
        @SuppressWarnings("unchecked")
        final List<Entity> castList = (List<Entity>) entityList;
        return doLumpRemove(castList, option);
    }

    protected int[] doLumpRemove(List<Entity> entityList, DeleteOption<? extends ConditionBean> option) {
        return doBatchDelete(downcast(entityList), downcast(option));
    }

    /** {@inheritDoc} */
    public int[] lumpRemoveNonstrict(List<? extends Entity> entityList, DeleteOption<? extends ConditionBean> option) {
        @SuppressWarnings("unchecked")
        final List<Entity> castList = (List<Entity>) entityList;
        return doLumpRemoveNonstrict(castList, option);
    }

    protected int[] doLumpRemoveNonstrict(List<Entity> entityList, DeleteOption<? extends ConditionBean> option) {
        if (asDBMeta().hasOptimisticLock()) {
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
    /** {@inheritDoc} */
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
        assertEntityNotNull(entity);
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
    /** {@inheritDoc} */
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
    /** {@inheritDoc} */
    public int rangeRemove(ConditionBean cb, DeleteOption<? extends ConditionBean> option) {
        return doRangeRemove(cb, option);
    }

    protected int doRangeRemove(ConditionBean cb, DeleteOption<? extends ConditionBean> option) {
        return doQueryDelete(downcast(cb), downcast(option));
    }

    // ===================================================================================
    //                                                                 Delegate to Command
    //                                                                 ===================
    // -----------------------------------------------------
    //                                         Entity Update
    //                                         -------------
    protected int delegateInsert(Entity entity, InsertOption<? extends ConditionBean> option) {
        final OptionalThing<InsertOption<? extends ConditionBean>> optOption = createOptionalInsertOption(option);
        adjustEntityBeforeInsert(entity, optOption);
        final InsertEntityCommand command = createInsertEntityCommand(entity, option);
        RuntimeException cause = null;
        try {
            hookBeforeInsert(command, entity, emptyOpt(), optOption);
            return invoke(command);
        } catch (RuntimeException e) {
            cause = e;
            throw e;
        } finally {
            hookFinallyInsert(command, entity, emptyOpt(), optOption, createOptionalCause(cause));
        }
    }

    protected OptionalThing<RuntimeException> createOptionalCause(RuntimeException cause) {
        return OptionalThing.ofNullable(cause, () -> {
            throw new IllegalStateException("Not found the cause exception.");
        });
    }

    protected <ELEMENT> OptionalThing<ELEMENT> emptyOpt() {
        return OptionalThing.empty();
    }

    protected int delegateUpdate(Entity entity, UpdateOption<? extends ConditionBean> option) {
        final OptionalThing<UpdateOption<? extends ConditionBean>> optOption = createOptionalUpdateOption(option);
        adjustEntityBeforeUpdate(entity, optOption);
        if (asDBMeta().hasOptimisticLock()) {
            final UpdateEntityCommand command = createUpdateEntityCommand(entity, option);
            RuntimeException cause = null;
            try {
                hookBeforeUpdate(command, entity, emptyOpt(), optOption);
                return invoke(command);
            } catch (RuntimeException e) {
                cause = e;
                throw e;
            } finally {
                hookFinallyUpdate(command, entity, emptyOpt(), optOption, createOptionalCause(cause));
            }
        } else {
            return delegateUpdateNonstrict(entity, option);
        }
    }

    protected int delegateUpdateNonstrict(Entity entity, UpdateOption<? extends ConditionBean> option) {
        final OptionalThing<UpdateOption<? extends ConditionBean>> optOption = createOptionalUpdateOption(option);
        adjustEntityBeforeUpdate(entity, optOption);
        final UpdateNonstrictEntityCommand command = createUpdateNonstrictEntityCommand(entity, option);
        RuntimeException cause = null;
        try {
            hookBeforeUpdate(command, entity, emptyOpt(), optOption);
            return invoke(command);
        } catch (RuntimeException e) {
            cause = e;
            throw e;
        } finally {
            hookFinallyUpdate(command, entity, emptyOpt(), optOption, createOptionalCause(cause));
        }
    }

    protected OptionalThing<UpdateOption<? extends ConditionBean>> createOptionalUpdateOption(
            UpdateOption<? extends ConditionBean> option) {
        return OptionalThing.ofNullable(option, () -> {
            throw new IllegalStateException("Not found the update option.");
        });
    }

    protected int delegateDelete(Entity entity, DeleteOption<? extends ConditionBean> option) {
        final OptionalThing<DeleteOption<? extends ConditionBean>> optOption = createOptionalDeleteOption(option);
        adjustEntityBeforeDelete(entity, optOption);
        if (asDBMeta().hasOptimisticLock()) {
            final DeleteEntityCommand command = createDeleteEntityCommand(entity, option);
            final OptionalThing<Object> optEntity = OptionalThing.of(entity);
            RuntimeException cause = null;
            try {
                hookBeforeDelete(command, optEntity, emptyOpt(), optOption);
                return invoke(command);
            } catch (RuntimeException e) {
                cause = e;
                throw e;
            } finally {
                hookFinallyDelete(command, optEntity, emptyOpt(), optOption, createOptionalCause(cause));
            }
        } else {
            return delegateDeleteNonstrict(entity, option);
        }
    }

    protected int delegateDeleteNonstrict(Entity entity, DeleteOption<? extends ConditionBean> option) {
        final OptionalThing<DeleteOption<? extends ConditionBean>> optOption = createOptionalDeleteOption(option);
        adjustEntityBeforeDelete(entity, optOption);
        final DeleteNonstrictEntityCommand command = createDeleteNonstrictEntityCommand(entity, option);
        final OptionalThing<Object> optEntity = OptionalThing.of(entity);
        RuntimeException cause = null;
        try {
            hookBeforeDelete(command, optEntity, emptyOpt(), optOption);
            return invoke(command);
        } catch (RuntimeException e) {
            cause = e;
            throw e;
        } finally {
            hookFinallyDelete(command, optEntity, emptyOpt(), optOption, createOptionalCause(cause));
        }
    }

    protected OptionalThing<DeleteOption<? extends ConditionBean>> createOptionalDeleteOption(
            DeleteOption<? extends ConditionBean> option) {
        return OptionalThing.ofNullable(option, () -> {
            throw new IllegalStateException("Not found the delete option.");
        });
    }

    // -----------------------------------------------------
    //                                          Batch Update
    //                                          ------------
    protected int[] delegateBatchInsert(List<? extends Entity> entityList, InsertOption<? extends ConditionBean> option) {
        if (entityList.isEmpty()) {
            return EMPTY_INT_ARRAY;
        }
        final OptionalThing<InsertOption<? extends ConditionBean>> optOption = createOptionalInsertOption(option);
        adjustEntityListBeforeBatchInsert(entityList, option);
        if (entityList.isEmpty()) { // might be filtered
            return EMPTY_INT_ARRAY;
        }
        final BatchInsertCommand command = createBatchInsertCommand(entityList, option);
        RuntimeException cause = null;
        try {
            hookBeforeInsert(command, entityList, emptyOpt(), optOption);
            return invoke(command);
        } catch (RuntimeException e) {
            cause = e;
            throw e;
        } finally {
            hookFinallyInsert(command, entityList, emptyOpt(), optOption, createOptionalCause(cause));
        }
    }

    protected int[] delegateBatchUpdate(List<? extends Entity> entityList, UpdateOption<? extends ConditionBean> option) {
        if (entityList.isEmpty()) {
            return EMPTY_INT_ARRAY;
        }
        if (asDBMeta().hasOptimisticLock()) {
            adjustEntityListBeforeBatchUpdate(entityList, option, false);
            if (entityList.isEmpty()) { // might be filtered
                return EMPTY_INT_ARRAY;
            }
            final BatchUpdateCommand command = createBatchUpdateCommand(entityList, option);
            final OptionalThing<UpdateOption<? extends ConditionBean>> optOption = createOptionalUpdateOption(option);
            RuntimeException cause = null;
            try {
                hookBeforeUpdate(command, entityList, emptyOpt(), optOption);
                return invoke(command);
            } catch (RuntimeException e) {
                cause = e;
                throw e;
            } finally {
                hookFinallyUpdate(command, entityList, emptyOpt(), optOption, createOptionalCause(cause));
            }
        } else {
            return delegateBatchUpdateNonstrict(entityList, option);
        }
    }

    protected int[] delegateBatchUpdateNonstrict(List<? extends Entity> entityList, UpdateOption<? extends ConditionBean> option) {
        if (entityList.isEmpty()) {
            return EMPTY_INT_ARRAY;
        }
        final OptionalThing<UpdateOption<? extends ConditionBean>> optOption = createOptionalUpdateOption(option);
        adjustEntityListBeforeBatchUpdate(entityList, option, true);
        if (entityList.isEmpty()) { // might be filtered
            return EMPTY_INT_ARRAY;
        }
        final BatchUpdateNonstrictCommand command = createBatchUpdateNonstrictCommand(entityList, option);
        RuntimeException cause = null;
        try {
            hookBeforeUpdate(command, entityList, emptyOpt(), optOption);
            return invoke(command);
        } catch (RuntimeException e) {
            cause = e;
            throw e;
        } finally {
            hookFinallyUpdate(command, entityList, emptyOpt(), optOption, createOptionalCause(cause));
        }
    }

    protected int[] delegateBatchDelete(List<? extends Entity> entityList, DeleteOption<? extends ConditionBean> option) {
        if (entityList.isEmpty()) {
            return EMPTY_INT_ARRAY;
        }
        if (asDBMeta().hasOptimisticLock()) {
            final List<? extends Entity> deletedList = adjustEntityListBeforeBatchDelete(entityList, option, false);
            if (deletedList.isEmpty()) { // might be filtered
                return EMPTY_INT_ARRAY;
            }
            final BatchDeleteCommand command = createBatchDeleteCommand(deletedList, option);
            final OptionalThing<Object> optEntityList = OptionalThing.of(entityList);
            final OptionalThing<DeleteOption<? extends ConditionBean>> optOption = createOptionalDeleteOption(option);
            RuntimeException cause = null;
            try {
                hookBeforeDelete(command, optEntityList, emptyOpt(), optOption);
                return invoke(command);
            } catch (RuntimeException e) {
                cause = e;
                throw e;
            } finally {
                hookFinallyDelete(command, optEntityList, emptyOpt(), optOption, createOptionalCause(cause));
            }
        } else {
            return delegateBatchDeleteNonstrict(entityList, option);
        }
    }

    protected int[] delegateBatchDeleteNonstrict(List<? extends Entity> entityList, DeleteOption<? extends ConditionBean> option) {
        if (entityList.isEmpty()) {
            return EMPTY_INT_ARRAY;
        }
        final List<? extends Entity> deletedList = adjustEntityListBeforeBatchDelete(entityList, option, true);
        if (deletedList.isEmpty()) { // might be filtered
            return EMPTY_INT_ARRAY;
        }
        final BatchDeleteNonstrictCommand command = createBatchDeleteNonstrictCommand(deletedList, option);
        final OptionalThing<Object> optEntityList = OptionalThing.of(entityList);
        final OptionalThing<DeleteOption<? extends ConditionBean>> optOption = createOptionalDeleteOption(option);
        RuntimeException cause = null;
        try {
            hookBeforeDelete(command, optEntityList, emptyOpt(), optOption);
            return invoke(command);
        } catch (RuntimeException e) {
            cause = e;
            throw e;
        } finally {
            hookFinallyDelete(command, optEntityList, emptyOpt(), optOption, createOptionalCause(cause));
        }
    }

    // -----------------------------------------------------
    //                                          Query Update
    //                                          ------------
    protected int delegateQueryInsert(Entity entity, ConditionBean inCB, ConditionBean resCB,
            InsertOption<? extends ConditionBean> option) {
        final OptionalThing<InsertOption<? extends ConditionBean>> optOption = createOptionalInsertOption(option);
        adjustEntityBeforeQueryInsert(entity, inCB, resCB, optOption);
        final QueryInsertCBCommand command = createQueryInsertCBCommand(entity, inCB, resCB, option);
        final OptionalThing<ConditionBean> optInCB = OptionalThing.of(inCB);
        RuntimeException cause = null;
        try {
            hookBeforeInsert(command, entity, optInCB, optOption);
            return invoke(command);
        } catch (RuntimeException e) {
            cause = e;
            throw e;
        } finally {
            hookFinallyInsert(command, entity, optInCB, optOption, createOptionalCause(cause));
        }
    }

    protected int delegateQueryUpdate(Entity entity, ConditionBean cb, UpdateOption<? extends ConditionBean> option) {
        final OptionalThing<UpdateOption<? extends ConditionBean>> optOption = createOptionalUpdateOption(option);
        adjustEntityBeforeQueryUpdate(entity, cb, optOption);
        final QueryUpdateCBCommand command = createQueryUpdateCBCommand(entity, cb, option);
        final OptionalThing<ConditionBean> optCB = OptionalThing.of(cb);
        RuntimeException cause = null;
        try {
            hookBeforeUpdate(command, entity, optCB, optOption);
            return invoke(command);
        } catch (RuntimeException e) {
            cause = e;
            throw e;
        } finally {
            hookFinallyUpdate(command, entity, optCB, optOption, createOptionalCause(cause));
        }
    }

    protected int delegateQueryDelete(ConditionBean cb, DeleteOption<? extends ConditionBean> option) {
        final OptionalThing<DeleteOption<? extends ConditionBean>> optOption = createOptionalDeleteOption(option);
        adjustEntityBeforeQueryDelete(cb, optOption);
        final QueryDeleteCBCommand command = createQueryDeleteCBCommand(cb, option);
        final OptionalThing<ConditionBean> optCB = OptionalThing.of(cb);
        RuntimeException cause = null;
        try {
            hookBeforeDelete(command, emptyOpt(), optCB, optOption);
            return invoke(command);
        } catch (RuntimeException e) {
            cause = e;
            throw e;
        } finally {
            hookFinallyDelete(command, emptyOpt(), optCB, optOption, createOptionalCause(cause));
        }
    }

    // =====================================================================================
    //                                                                           Hook Method
    //                                                                           ===========
    // -----------------------------------------------------
    //                                                Insert
    //                                                ------
    /**
     * Adjust entity before insert. <br>
     * Called when entity insert, batch insert and also contains varying methods <br>
     * But attention: plural calls in batch insert, per one entity.
     * @param entity The entity for insert. (NotNull)
     * @param option The optional option of insert. (NotNull, EmptyAllowed: when no option)
     */
    protected void adjustEntityBeforeInsert(Entity entity, OptionalThing<InsertOption<? extends ConditionBean>> option) {
        assertEntityNotNull(entity); // primary key is checked later
        frameworkFilterEntityOfInsert(entity, option);
        filterEntityOfInsert(entity, option);
        assertEntityOfInsert(entity, option);
        // check primary key after filtering at an insert process
        // because a primary key value may be set in filtering process
        // (for example, sequence)
        if (!entity.asDBMeta().hasIdentity()) { // identity does not need primary key value here
            assertEntityNotNullAndHasPrimaryKeyValue(entity);
        }
    }

    /**
     * Adjust entity before query-insert.
     * @param entity The entity for query-insert. (NotNull)
     * @param intoCB The condition-bean for inserted table. (NotNull)
     * @param resourceCB The condition-bean for resource table. (NotNull)
     * @param option The optional option of insert. (NotNull, EmptyAllowed: when no option)
     */
    protected void adjustEntityBeforeQueryInsert(Entity entity, ConditionBean intoCB, ConditionBean resourceCB,
            OptionalThing<InsertOption<? extends ConditionBean>> option) {
        assertEntityNotNull(entity); // query-insert doesn't need to check primary key
        assertObjectNotNull("intoCB", intoCB);
        if (resourceCB == null) {
            String msg = "The set-upper of query-insert should return condition-bean for resource table: " + entity.asTableDbName();
            throw new IllegalConditionBeanOperationException(msg);
        }
        frameworkFilterEntityOfInsert(entity, option);
        setupExclusiveControlColumnOfQueryInsert(entity);
        filterEntityOfInsert(entity, option);
        assertEntityOfInsert(entity, option);
    }

    protected void setupExclusiveControlColumnOfQueryInsert(Entity entity) {
        final DBMeta dbmeta = asDBMeta();
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
     * @param option The optional option of insert. (NotNull, EmptyAllowed: when no option)
     */
    @Override
    protected void frameworkFilterEntityOfInsert(Entity entity, OptionalThing<InsertOption<? extends ConditionBean>> option) {
        injectSequenceToPrimaryKeyIfNeeds(entity);
        setupCommonColumnOfInsertIfNeeds(entity, option);
    }

    /**
     * Set up common columns of insert if it needs.
     * @param entity The entity for insert. (NotNull)
     * @param option The optional option of insert. (NotNull, EmptyAllowed: when no option)
     */
    protected void setupCommonColumnOfInsertIfNeeds(Entity entity, OptionalThing<InsertOption<? extends ConditionBean>> option) {
        if (option.filter(op -> op.isCommonColumnAutoSetupDisabled()).isPresent()) {
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
        br.addElement("Behavior for " + asTableDbName());
        br.addItem("Attribute");
        br.addElement("behaviorCommandInvoker   : " + _behaviorCommandInvoker);
        br.addElement("behaviorSelector         : " + _behaviorSelector);
        br.addElement("commonColumnAutoSetupper : " + _commonColumnAutoSetupper);
        final String msg = br.buildExceptionMessage();
        throw new IllegalBehaviorStateException(msg);
    }

    @Override
    protected void filterEntityOfInsert(Entity entity, OptionalThing<InsertOption<? extends ConditionBean>> option) {
    }

    /**
     * Assert the entity of insert. (for extension)
     * @param entity The entity for insert. (NotNull)
     * @param option The optional option of insert. (NotNull, EmptyAllowed: when no option)
     */
    protected void assertEntityOfInsert(Entity entity, OptionalThing<InsertOption<? extends ConditionBean>> option) {
    }

    /**
     * Hook before insert, contains entity insert, batch insert, query insert, also varying. (for extension) <br>
     * As best you can, your overriding code needs not to depends on internal specification. (might be changed) <br>
     * Finally process is called even if exception in before process.
     * @param command The command meta of behavior for insert. (NotNull)
     * @param entityResource The resource of entity for insert, entity or list. (NotNull)
     * @param cbResource The optional resource of condition-bean for insert. (NotNull, EmptyAllowed: except query-insert)
     * @param option The optional option of insert. (NotNull, EmptyAllowed: when no option)
     */
    protected void hookBeforeInsert(BehaviorCommandMeta command, Object entityResource, OptionalThing<ConditionBean> cbResource,
            OptionalThing<InsertOption<? extends ConditionBean>> option) {
    }

    /**
     * Hook finally of insert, contains entity insert, batch insert, query insert, also varying. (for extension) <br>
     * As best you can, your overriding code needs not to depends on internal specification. (might be changed) <br>
     * Finally process is called even if exception in before process. <br>
     * And called when both success and exception.
     * @param command The command meta of behavior for insert. (NotNull)
     * @param entityResource The resource of entity for insert, entity or list. (NotNull)
     * @param cbResource The optional resource of condition-bean for insert. (NotNull, EmptyAllowed: except query-insert)
     * @param option The optional option of insert. (NotNull, EmptyAllowed: when no option)
     * @param cause The optional cause exception from insert. (NotNull, EmptyAllowed: when no failure)
     */
    protected void hookFinallyInsert(BehaviorCommandMeta command, Object entityResource, OptionalThing<ConditionBean> cbResource,
            OptionalThing<InsertOption<? extends ConditionBean>> option, OptionalThing<RuntimeException> cause) {
    }

    // -----------------------------------------------------
    //                                                Update
    //                                                ------
    /**
     * Adjust entity before update.
     * @param entity The entity for update that has primary key. (NotNull)
     * @param option The optional option of update. (NotNull, EmptyAllowed: when no option)
     */
    protected void adjustEntityBeforeUpdate(Entity entity, OptionalThing<UpdateOption<? extends ConditionBean>> option) {
        assertEntityNotNullAndHasPrimaryKeyValue(entity);
        frameworkFilterEntityOfUpdate(entity, option);
        filterEntityOfUpdate(entity, option);
        assertEntityOfUpdate(entity, option);
    }

    /**
     * Adjust entity before query-update.
     * @param entity The entity for update that is not needed primary key. (NotNull)
     * @param cb The condition-bean for query. (NotNull)
     * @param option The optional option of update. (NotNull, EmptyAllowed: when no option)
     */
    protected void adjustEntityBeforeQueryUpdate(Entity entity, ConditionBean cb,
            OptionalThing<UpdateOption<? extends ConditionBean>> option) {
        assertEntityNotNull(entity); // query-update doesn't need to check primary key
        assertCBStateValid(cb);
        frameworkFilterEntityOfUpdate(entity, option);
        filterEntityOfUpdate(entity, option);
        assertEntityOfUpdate(entity, option);
        assertQueryUpdateStatus(entity, cb, option);
    }

    /**
     * {Framework Method} Filter the entity of update.
     * @param entity The entity for update. (NotNull)
     * @param option The optional option of update. (NotNull, EmptyAllowed: when no option)
     */
    protected void frameworkFilterEntityOfUpdate(Entity entity, OptionalThing<UpdateOption<? extends ConditionBean>> option) {
        setupCommonColumnOfUpdateIfNeeds(entity, option);
    }

    /**
     * Set up common columns of update if it needs.
     * @param entity The entity for update. (NotNull)
     * @param option The optional option of update. (NotNull, EmptyAllowed: when no option)
     */
    protected void setupCommonColumnOfUpdateIfNeeds(Entity entity, OptionalThing<UpdateOption<? extends ConditionBean>> option) {
        if (option.filter(op -> op.isCommonColumnAutoSetupDisabled()).isPresent()) {
            return;
        }
        final CommonColumnAutoSetupper setupper = getCommonColumnAutoSetupper();
        assertCommonColumnAutoSetupperNotNull();
        setupper.handleCommonColumnOfUpdateIfNeeds(entity);
    }

    /**
     * Filter the entity of update. (for extension)
     * @param entity The entity for update. (NotNull)
     * @param option The optional option of update. (NotNull, EmptyAllowed: when no option)
     */
    protected void filterEntityOfUpdate(Entity entity, OptionalThing<UpdateOption<? extends ConditionBean>> option) {
    }

    /**
     * Assert the entity of update. (for extension)
     * @param entity The entity for update. (NotNull)
     * @param option The optional option of update. (NotNull, EmptyAllowed: when no option)
     */
    protected void assertEntityOfUpdate(Entity entity, OptionalThing<UpdateOption<? extends ConditionBean>> option) {
    }

    /**
     * Assert that the query-update is legal status.
     * @param entity The entity for query-update. (NotNull)
     * @param cb The condition-bean for query-update. (NotNull)
     * @param option The optional option of update. (NotNull, EmptyAllowed: when no option)
     */
    protected void assertQueryUpdateStatus(Entity entity, ConditionBean cb, OptionalThing<UpdateOption<? extends ConditionBean>> option) {
        if (option.filter(op -> op.isNonQueryUpdateAllowed()).isPresent()) {
            return;
        }
        if (cb.hasSelectAllPossible()) {
            createBhvExThrower().throwNonQueryUpdateNotAllowedException(entity, cb, option.orElse(null));
        }
    }

    /**
     * Hook before update, contains entity update, batch update, query update, also varying. (for extension) <br>
     * As best you can, your overriding code needs not to depends on internal specification. (might be changed) <br>
     * Finally process is called even if exception in before process.
     * @param command The command meta of behavior for update. (NotNull)
     * @param entityResource The resource of entity for update, entity or list. (NotNull)
     * @param cbResource The optional resource of condition-bean for update. (NotNull, EmptyAllowed: except query-update)
     * @param option The optional option of update. (NotNull, EmptyAllowed: when no option)
     */
    protected void hookBeforeUpdate(BehaviorCommandMeta command, Object entityResource, OptionalThing<ConditionBean> cbResource,
            OptionalThing<UpdateOption<? extends ConditionBean>> option) {
    }

    /**
     * Hook finally of update, contains entity update, batch update, query update, also varying. (for extension) <br>
     * As best you can, your overriding code needs not to depends on internal specification. (might be changed) <br>
     * Finally process is called even if exception in before process. <br>
     * And called when both success and exception. <br>
     * @param command The command meta of behavior for update. (NotNull)
     * @param entityResource The resource of entity for update, entity or list. (NotNull)
     * @param cbResource The optional resource of condition-bean for update. (NotNull, EmptyAllowed: except query-update)
     * @param option The optional option of update. (NotNull, EmptyAllowed: when no option)
     * @param cause The optional cause exception from update, but not contains update count check. (NotNull, EmptyAllowed: when no failure)
     */
    protected void hookFinallyUpdate(BehaviorCommandMeta command, Object entityResource, OptionalThing<ConditionBean> cbResource,
            OptionalThing<UpdateOption<? extends ConditionBean>> option, OptionalThing<RuntimeException> cause) {
    }

    // -----------------------------------------------------
    //                                                Delete
    //                                                ------
    /**
     * Adjust entity before delete.
     * @param entity The entity for delete that has primary key. (NotNull)
     * @param option The optional option of delete. (NotNull, EmptyAllowed: when no option)
     */
    protected void adjustEntityBeforeDelete(Entity entity, OptionalThing<DeleteOption<? extends ConditionBean>> option) {
        assertEntityNotNullAndHasPrimaryKeyValue(entity);
        frameworkFilterEntityOfDelete(entity, option);
        filterEntityOfDelete(entity, option);
        assertEntityOfDelete(entity, option);
    }

    /**
     * Ready entity before query-delete.
     * @param cb The condition-bean for query. (NotNull)
     * @param option The optional option of delete. (NotNull, EmptyAllowed: when no option)
     */
    protected void adjustEntityBeforeQueryDelete(ConditionBean cb, OptionalThing<DeleteOption<? extends ConditionBean>> option) {
        assertCBStateValid(cb);
        assertQueryDeleteStatus(cb, option);
    }

    /**
     * {Framework Method} Filter the entity of delete. {not called if query-delete}
     * @param entity The entity for delete that has primary key. (NotNull)
     * @param option The optional option of delete. (NotNull, EmptyAllowed: when no option)
     */
    protected void frameworkFilterEntityOfDelete(Entity entity, OptionalThing<DeleteOption<? extends ConditionBean>> option) {
    }

    /**
     * Filter the entity of delete. (for extension) {not called if query-delete}
     * @param entity The entity for delete that has primary key. (NotNull)
     * @param option The optional option of delete. (NotNull, EmptyAllowed: when no option)
     */
    protected void filterEntityOfDelete(Entity entity, OptionalThing<DeleteOption<? extends ConditionBean>> option) {
    }

    /**
     * Assert the entity of delete. (for extension) {not called if query-delete}
     * @param entity The entity for delete that has primary key. (NotNull)
     * @param option The optional option of delete. (NotNull, EmptyAllowed: when no option)
     */
    protected void assertEntityOfDelete(Entity entity, OptionalThing<DeleteOption<? extends ConditionBean>> option) {
    }

    /**
     * Assert that the query-delete is legal status.
     * @param cb The condition-bean for query-delete. (NotNull)
     * @param option The optional option of delete. (NotNull, EmptyAllowed: when no option)
     */
    protected void assertQueryDeleteStatus(ConditionBean cb, OptionalThing<DeleteOption<? extends ConditionBean>> option) {
        if (option.filter(op -> op.isNonQueryDeleteAllowed()).isPresent()) {
            return;
        }
        if (cb.hasSelectAllPossible()) {
            createBhvExThrower().throwNonQueryDeleteNotAllowedException(cb, option.orElse(null));
        }
    }

    /**
     * Hook before delete, contains entity delete, batch delete, query delete, also varying. (for extension) <br>
     * As best you can, your overriding code needs not to depends on internal specification. (might be changed) <br>
     * Finally process is called even if exception in before process.
     * @param command The command meta of behavior for delete. (NotNull)
     * @param entityResource The optional resource of entity for delete, entity or list. (NotNull, EmptyAllowed: when query-delete)
     * @param cbResource The optional resource of condition-bean for delete. (NotNull, EmptyAllowed: except query-delete)
     * @param option The optional option of delete. (NotNull, EmptyAllowed: when no option)
     */
    protected void hookBeforeDelete(BehaviorCommandMeta command, OptionalThing<Object> entityResource,
            OptionalThing<ConditionBean> cbResource, OptionalThing<DeleteOption<? extends ConditionBean>> option) {
    }

    /**
     * Hook finally of delete, contains entity delete, batch delete, query delete, also varying. (for extension) <br>
     * As best you can, your overriding code needs not to depends on internal specification. (might be changed) <br>
     * Finally process is called even if exception in before process. <br>
     * And called when both success and exception.
     * @param command The command meta of behavior for delete. (NotNull)
     * @param entityResource The optional resource of entity for delete, entity or list. (NotNull, EmptyAllowed: when query-delete)
     * @param cbResource The optional resource of condition-bean for delete. (NotNull, EmptyAllowed: except query-update)
     * @param option The optional option of delete. (NotNull, EmptyAllowed: when no option)
     * @param cause The optional cause exception from delete, but not contains update count check. (NotNull, EmptyAllowed: when no failure)
     */
    protected void hookFinallyDelete(BehaviorCommandMeta command, OptionalThing<Object> entityResource,
            OptionalThing<ConditionBean> cbResource, OptionalThing<DeleteOption<? extends ConditionBean>> option,
            OptionalThing<RuntimeException> cause) {
    }

    // -----------------------------------------------------
    //                                                Common
    //                                                ------
    protected void injectSequenceToPrimaryKeyIfNeeds(Entity entity) {
        final DBMeta dbmeta = entity.asDBMeta();
        if (!dbmeta.hasSequence() || dbmeta.hasCompoundPrimaryKey() || entity.hasPrimaryKeyValue()) {
            return;
        }
        // basically property(column) type is same as next value type
        // so there is NOT type conversion cost when writing to the entity
        dbmeta.getPrimaryInfo().getFirstColumn().write(entity, readNextVal());
    }

    protected void assertEntityHasOptimisticLockValue(Entity entity) {
        assertEntityHasVersionNoValue(entity);
        assertEntityHasUpdateDateValue(entity);
    }

    protected void assertEntityHasVersionNoValue(Entity entity) {
        if (!asDBMeta().hasVersionNo()) {
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
        if (!asDBMeta().hasUpdateDate()) {
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
    protected <ELEMENT extends Entity> void adjustEntityListBeforeBatchInsert(List<ELEMENT> entityList,
            InsertOption<? extends ConditionBean> option) {
        assertObjectNotNull("entityList", entityList);
        final OptionalThing<InsertOption<? extends ConditionBean>> optOption = createOptionalInsertOption(option);
        for (ELEMENT entity : entityList) {
            adjustEntityBeforeInsert(entity, optOption);
        }
    }

    protected <ELEMENT extends Entity> void adjustEntityListBeforeBatchUpdate(List<ELEMENT> entityList,
            UpdateOption<? extends ConditionBean> option, boolean nonstrict) {
        assertObjectNotNull("entityList", entityList);
        final OptionalThing<UpdateOption<? extends ConditionBean>> optOption = createOptionalUpdateOption(option);
        for (ELEMENT entity : entityList) {
            adjustEntityBeforeUpdate(entity, optOption);
            if (!nonstrict) {
                assertEntityHasOptimisticLockValue(entity);
            }
        }
    }

    protected <ELEMENT extends Entity> List<ELEMENT> adjustEntityListBeforeBatchDelete(List<ELEMENT> entityList,
            DeleteOption<? extends ConditionBean> option, boolean nonstrict) {
        assertObjectNotNull("entityList", entityList);
        final OptionalThing<DeleteOption<? extends ConditionBean>> optOption = createOptionalDeleteOption(option);
        final List<ELEMENT> filteredList = new ArrayList<ELEMENT>(entityList.size());
        for (ELEMENT entity : entityList) {
            adjustEntityBeforeDelete(entity, optOption);
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

    protected UpdateNonstrictEntityCommand createUpdateNonstrictEntityCommand(Entity entity, UpdateOption<? extends ConditionBean> option) {
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

    protected DeleteNonstrictEntityCommand createDeleteNonstrictEntityCommand(Entity entity, DeleteOption<? extends ConditionBean> option) {
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
    protected BatchInsertCommand createBatchInsertCommand(List<? extends Entity> entityList, InsertOption<? extends ConditionBean> option) {
        assertBehaviorCommandInvoker("createBatchInsertCommand");
        final BatchInsertCommand cmd = newBatchInsertCommand();
        setupListEntityCommand(cmd, entityList);
        cmd.setInsertOption(option);
        return cmd;
    }

    protected BatchInsertCommand newBatchInsertCommand() {
        return new BatchInsertCommand();
    }

    protected BatchUpdateCommand createBatchUpdateCommand(List<? extends Entity> entityList, UpdateOption<? extends ConditionBean> option) {
        assertBehaviorCommandInvoker("createBatchUpdateCommand");
        final BatchUpdateCommand cmd = newBatchUpdateCommand();
        setupListEntityCommand(cmd, entityList);
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
        setupListEntityCommand(cmd, entityList);
        cmd.setUpdateOption(option);
        return cmd;
    }

    protected BatchUpdateNonstrictCommand newBatchUpdateNonstrictCommand() {
        return new BatchUpdateNonstrictCommand();
    }

    protected BatchDeleteCommand createBatchDeleteCommand(List<? extends Entity> entityList, DeleteOption<? extends ConditionBean> option) {
        assertBehaviorCommandInvoker("createBatchDeleteCommand");
        final BatchDeleteCommand cmd = newBatchDeleteCommand();
        setupListEntityCommand(cmd, entityList);
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
        setupListEntityCommand(cmd, entityList);
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
    protected void setupListEntityCommand(AbstractBatchUpdateCommand command, List<? extends Entity> entityList) {
        if (entityList.isEmpty()) {
            String msg = "The argument 'entityList' should not be empty: " + entityList;
            throw new IllegalStateException(msg);
        }
        command.setTableDbName(asTableDbName());
        _behaviorCommandInvoker.injectComponentProperty(command);
        command.setEntityType(entityList.get(0).getClass()); // *The list should not be empty!
        command.setEntityList(entityList);
    }

    // -----------------------------------------------------
    //                                                 Query
    //                                                 -----
    protected QueryInsertCBCommand createQueryInsertCBCommand(Entity entity, ConditionBean intoCB, ConditionBean resourceCB,
            InsertOption<? extends ConditionBean> option) {
        assertBehaviorCommandInvoker("createQueryInsertCBCommand");
        final QueryInsertCBCommand cmd = new QueryInsertCBCommand();
        cmd.setTableDbName(asTableDbName());
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
        cmd.setTableDbName(asTableDbName());
        _behaviorCommandInvoker.injectComponentProperty(cmd);
        cmd.setEntity(entity);
        cmd.setConditionBean(cb);
        cmd.setUpdateOption(option);
        return cmd;
    }

    protected QueryDeleteCBCommand createQueryDeleteCBCommand(ConditionBean cb, DeleteOption<? extends ConditionBean> option) {
        assertBehaviorCommandInvoker("createQueryDeleteCBCommand");
        final QueryDeleteCBCommand cmd = new QueryDeleteCBCommand();
        cmd.setTableDbName(asTableDbName());
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
    protected QueryInsertSetupper<ENTITY, CB> downcast(QueryInsertSetupper<? extends Entity, ? extends ConditionBean> setupper) {
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
