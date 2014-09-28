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

import java.util.List;

import org.seasar.dbflute.Entity;
import org.seasar.dbflute.bhv.DeleteOption;
import org.seasar.dbflute.bhv.UpdateOption;
import org.seasar.dbflute.bhv.WritableOption;
import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.cbean.PagingBean;
import org.seasar.dbflute.cbean.chelper.HpInvalidQueryInfo;
import org.seasar.dbflute.exception.DangerousResultSizeException;
import org.seasar.dbflute.exception.EntityAlreadyDeletedException;
import org.seasar.dbflute.exception.EntityDuplicatedException;
import org.seasar.dbflute.exception.EntityPrimaryKeyNotFoundException;
import org.seasar.dbflute.exception.EntityUniqueKeyNotFoundException;
import org.seasar.dbflute.exception.FetchingOverSafetySizeException;
import org.seasar.dbflute.exception.NonQueryDeleteNotAllowedException;
import org.seasar.dbflute.exception.NonQueryUpdateNotAllowedException;
import org.seasar.dbflute.exception.OptimisticLockColumnValueNullException;
import org.seasar.dbflute.exception.PagingCountSelectNotCountException;
import org.seasar.dbflute.exception.SelectEntityConditionNotFoundException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.jdbc.FetchBean;
import org.seasar.dbflute.resource.DBFluteSystem;
import org.seasar.dbflute.util.DfTypeUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 */
public class BehaviorExceptionThrower {

    // ===================================================================================
    //                                                                              Select
    //                                                                              ======
    public void throwSelectEntityAlreadyDeletedException(Object searchKey) {
        final ExceptionMessageBuilder br = createExceptionMessageBuilder();
        br.addNotice("The selected entity was NOT found! It has already been deleted.");
        br.addItem("Advice");
        br.addElement("Please confirm the existence of your target record on your database.");
        br.addElement("Does the target record really created before this operation?");
        br.addElement("Has the target record been deleted by other thread?");
        br.addElement("It is precondition that the record exists on your database.");
        setupSearchKeyElement(br, searchKey);
        final String msg = br.buildExceptionMessage();
        throw new EntityAlreadyDeletedException(msg); // basically treated as application exception
    }

    public void throwSelectEntityDuplicatedException(String resultCountExp, Object searchKey, Throwable cause) {
        final ExceptionMessageBuilder br = createExceptionMessageBuilder();
        br.addNotice("The selected entity was duplicated! It should be the only one.");
        br.addItem("Advice");
        br.addElement("Confirm your search condition. Does it really select the only one?");
        br.addElement("And confirm your database. Does it really exist the only one?");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    cb.query().setMemberName_PrefisSearch(\"S\");");
        br.addElement("    ... = memberBhv.selectEntity(cb);");
        br.addElement("  (o):");
        br.addElement("    cb.query().setMemberId_Equal(3);");
        br.addElement("    ... = memberBhv.selectEntity(cb);");
        br.addItem("Result Count");
        br.addElement(resultCountExp);
        setupSearchKeyElement(br, searchKey);
        final String msg = br.buildExceptionMessage();
        if (cause != null) {
            throw new EntityDuplicatedException(msg, cause);
        } else {
            throw new EntityDuplicatedException(msg);
        }
    }

    protected void setupSearchKeyElement(ExceptionMessageBuilder br, Object searchKey) {
        if (searchKey instanceof Object[]) {
            final Object[] ary = (Object[]) searchKey;
            if (ary.length == 1) {
                searchKey = ary[0];
            } else {
                final StringBuilder sb = new StringBuilder();
                for (Object obj : ary) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(obj);
                }
                sb.insert(0, "{").append("}");
                searchKey = sb.toString();
            }
        }
        if (searchKey instanceof ConditionBean) {
            final ConditionBean cb = (ConditionBean) searchKey;
            setupInvalidQueryElement(br, cb);
            setupDisplaySqlElement(br, cb);
        } else {
            br.addItem("Search Condition");
            br.addElement(searchKey);
        }
    }

    public void throwSelectEntityConditionNotFoundException(ConditionBean cb) {
        final ExceptionMessageBuilder br = createExceptionMessageBuilder();
        br.addNotice("The condition for selecting an entity was not found!");
        br.addItem("Advice");
        br.addElement("Confirm your search condition. Does it really select the only one?");
        br.addElement("You have to set a valid query or fetch-first as 1.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    MemberCB cb = MemberCB();");
        br.addElement("    ... = memberBhv.selectEntity(cb); // exception");
        br.addElement("  (x):");
        br.addElement("    MemberCB cb = MemberCB();");
        br.addElement("    cb.query().setMemberId_Equal(null);");
        br.addElement("    ... = memberBhv.selectEntity(cb); // exception");
        br.addElement("  (o):");
        br.addElement("    MemberCB cb = MemberCB();");
        br.addElement("    cb.query().setMemberId_Equal(3);");
        br.addElement("    ... = memberBhv.selectEntity(cb);");
        br.addElement("  (o):");
        br.addElement("    MemberCB cb = MemberCB();");
        br.addElement("    cb.fetchFirst(1);");
        br.addElement("    ... = memberBhv.selectEntity(cb);");
        setupInvalidQueryElement(br, cb);
        setupFetchSizeElement(br, cb);
        setupDisplaySqlElement(br, cb);
        final String msg = br.buildExceptionMessage();
        throw new SelectEntityConditionNotFoundException(msg);
    }

    public void throwDangerousResultSizeException(FetchBean fetchBean, Throwable cause) {
        final int safetyMaxResultSize = fetchBean.getSafetyMaxResultSize();
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("You've already been in DANGER ZONE. (check is working)");
        br.addItem("Advice");
        br.addElement("The selected size is over the specified safety size.");
        br.addElement("Confirm your conditions and table records.");
        br.addItem("Safety Max Result Size");
        br.addElement(safetyMaxResultSize);
        if (fetchBean instanceof ConditionBean) {
            final ConditionBean cb = ((ConditionBean) fetchBean);
            setupInvalidQueryElement(br, cb);
            setupFetchSizeElement(br, cb);
            setupDisplaySqlElement(br, cb);
        } else {
            br.addItem("Fetch Bean");
            br.addElement(fetchBean);
            if (cause instanceof FetchingOverSafetySizeException) {
                final String sql = ((FetchingOverSafetySizeException) cause).getDangerousDisplaySql();
                if (sql != null) {
                    br.addItem("Dangerous SQL");
                    br.addElement(sql);
                }
            }
        }
        final String msg = br.buildExceptionMessage();
        throw new DangerousResultSizeException(msg, cause, safetyMaxResultSize);
    }

    protected void setupInvalidQueryElement(ExceptionMessageBuilder br, ConditionBean cb) {
        br.addItem("Invalid Query");
        final List<HpInvalidQueryInfo> invalidQueryList = cb.getSqlClause().getInvalidQueryList();
        if (invalidQueryList != null && !invalidQueryList.isEmpty()) {
            for (HpInvalidQueryInfo invalidQueryInfo : invalidQueryList) {
                br.addElement(invalidQueryInfo.buildDisplay());
            }
        } else {
            br.addElement("*no invalid");
        }
    }

    protected void setupFetchSizeElement(ExceptionMessageBuilder br, ConditionBean cb) {
        br.addItem("Fetch Size");
        br.addElement(cb.getFetchSize());
    }

    protected void setupDisplaySqlElement(ExceptionMessageBuilder br, ConditionBean cb) {
        br.addItem("Display SQL");
        br.addElement(cb.toDisplaySql());
    }

    public <ENTITY> void throwPagingCountSelectNotCountException(String tableDbName, String path, PagingBean pmb,
            Class<ENTITY> entityType, EntityDuplicatedException e) { // for OutsideSql
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The count select for paging could not get a count.");
        br.addItem("Advice");
        br.addElement("A select clause of OutsideSql paging should be switchable like this:");
        br.addElement("For example:");
        br.addElement("  /*IF pmb.isPaging()*/");
        br.addElement("  select member.MEMBER_ID");
        br.addElement("       , member.MEMBER_NAME");
        br.addElement("       , ...");
        br.addElement("  -- ELSE select count(*)");
        br.addElement("  /*END*/");
        br.addElement("    from ...");
        br.addElement("   where ...");
        br.addElement("");
        br.addElement("This specificaton is for both ManualPaging and AutoPaging.");
        br.addElement("(AutoPaging is only allowed to omit a paging condition)");
        br.addItem("Table");
        br.addElement(tableDbName);
        br.addItem("OutsideSql");
        br.addElement(path);
        br.addItem("ParameterBean");
        br.addElement(pmb);
        br.addItem("Entity Type");
        br.addElement(entityType);
        final String msg = br.buildExceptionMessage();
        throw new PagingCountSelectNotCountException(msg, e);
    }

    // ===================================================================================
    //                                                                              Update
    //                                                                              ======
    public void throwEntityPrimaryKeyNotFoundException(Entity entity) {
        final String classTitle = DfTypeUtil.toClassTitle(entity);
        final String behaviorName = Srl.substringLastRear(entity.getDBMeta().getBehaviorTypeName(), ".");
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The primary-key value in the entity was not found.");
        br.addItem("Advice");
        br.addElement("An entity should have its primary-key value");
        br.addElement("when e.g. insert(), update().");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    " + classTitle + " entity = new " + classTitle + "();");
        br.addElement("    entity.setFooName(...);");
        br.addElement("    entity.setFooDate(...);");
        br.addElement("    " + behaviorName + ".updateNonstrict(entity);");
        br.addElement("  (o):");
        br.addElement("    " + classTitle + " entity = new " + classTitle + "();");
        br.addElement("    entity.setFooId(...); // *Point");
        br.addElement("    entity.setFooName(...);");
        br.addElement("    entity.setFooDate(...);");
        br.addElement("    " + behaviorName + ".updateNonstrict(entity);");
        br.addElement("Or if your process is insert(), you might expect identity.");
        br.addElement("Confirm the primary-key's identity setting.");
        br.addItem("Entity");
        br.addElement(entity);
        final String msg = br.buildExceptionMessage();
        throw new EntityPrimaryKeyNotFoundException(msg);
    }

    public void throwEntityUniqueKeyNotFoundException(Entity entity) {
        final String classTitle = DfTypeUtil.toClassTitle(entity);
        final String behaviorName = Srl.substringLastRear(entity.getDBMeta().getBehaviorTypeName(), ".");
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The unique-key value in the entity was not found.");
        br.addItem("Advice");
        br.addElement("An entity should have its unique-key value");
        br.addElement("when e.g. update(), delete() if you call uniqueByXxx().");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    " + classTitle + " entity = new " + classTitle + "();");
        br.addElement("    entity.setFooName(...);");
        br.addElement("    entity.setFooDate(...);");
        br.addElement("    entity.uniqueByFooAccount(...);");
        br.addElement("    " + behaviorName + ".updateNonstrict(entity);");
        br.addElement("  (o):");
        br.addElement("    " + classTitle + " entity = new " + classTitle + "();");
        br.addElement("    entity.setFooAccount(...); // *Point");
        br.addElement("    entity.setFooName(...);");
        br.addElement("    entity.setFooDate(...);");
        br.addElement("    entity.uniqueByFooAccount(...);");
        br.addElement("    " + behaviorName + ".updateNonstrict(entity);");
        br.addItem("Entity");
        br.addElement(entity);
        final String msg = br.buildExceptionMessage();
        throw new EntityUniqueKeyNotFoundException(msg);
    }

    public <ENTITY extends Entity> void throwUpdateEntityAlreadyDeletedException(ENTITY entity) {
        final ExceptionMessageBuilder br = createExceptionMessageBuilder();
        br.addNotice("The updated entity was not found! it has already been deleted.");
        setupEntityElement(br, entity);
        final String msg = br.buildExceptionMessage();
        throw new EntityAlreadyDeletedException(msg); // basically treated as application exception
    }

    public <ENTITY extends Entity> void throwUpdateEntityDuplicatedException(ENTITY entity, int count) {
        final ExceptionMessageBuilder br = createExceptionMessageBuilder();
        br.addNotice("The updated entity was duplicated. It should be the only one!");
        br.addItem("Count");
        br.addElement(count);
        setupEntityElement(br, entity);
        final String msg = br.buildExceptionMessage();
        throw new EntityDuplicatedException(msg); // basically no way if you use PK constraint
    }

    public void throwVersionNoValueNullException(Entity entity) {
        final ExceptionMessageBuilder br = createExceptionMessageBuilder();
        br.addNotice("Not found the value of 'version no' on the entity!");
        br.addItem("Advice");
        br.addElement("Please confirm the existence of the value of 'version no' on the entity.");
        br.addElement("You called the method in which the check for optimistic lock is indispensable.");
        br.addElement("So 'version no' is required on the entity.");
        br.addElement("In addition, please confirm the necessity of optimistic lock.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    Member member = new Member();");
        br.addElement("    member.setMemberId(3);");
        br.addElement("    member.setMemberName(\"Pixy\");");
        br.addElement("    memberBhv.update(member);");
        br.addElement("  (o): (Optimistic Lock)");
        br.addElement("    Member member = new Member();");
        br.addElement("    member.setMemberId(3);");
        br.addElement("    member.setMemberName(\"Pixy\");");
        br.addElement("    member.setVersionNo(...); // *Point");
        br.addElement("    memberBhv.update(member);");
        br.addElement("  (o): (Nonstrict)");
        br.addElement("    Member member = new Member();");
        br.addElement("    member.setMemberId(3);");
        br.addElement("    member.setMemberName(\"Pixy\");");
        br.addElement("    memberBhv.updateNonstrict(member); // *Point");
        setupEntityElement(br, entity);
        final String msg = br.buildExceptionMessage();
        throw new OptimisticLockColumnValueNullException(msg);
    }

    public void throwUpdateDateValueNullException(Entity entity) {
        final ExceptionMessageBuilder br = createExceptionMessageBuilder();
        br.addNotice("Not found the value of 'update date' on the entity!");
        br.addItem("Advice");
        br.addElement("Please confirm the existence of the value of 'update date' on the entity.");
        br.addElement("You called the method in which the check for optimistic lock is indispensable.");
        br.addElement("So 'update date' is required on the entity.");
        br.addElement("In addition, please confirm the necessity of optimistic lock.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    Member member = new Member();");
        br.addElement("    member.setMemberId(3);");
        br.addElement("    member.setMemberName(\"Pixy\");");
        br.addElement("    memberBhv.update(member);");
        br.addElement("  (o): (Optimistic Lock)");
        br.addElement("    Member member = new Member();");
        br.addElement("    member.setMemberId(3);");
        br.addElement("    member.setMemberName(\"Pixy\");");
        br.addElement("    member.setUpdateDatetime(updateDatetime); // *Point");
        br.addElement("    memberBhv.update(member);");
        br.addElement("  (o): (Nonstrict)");
        br.addElement("    Member member = new Member();");
        br.addElement("    member.setMemberId(3);");
        br.addElement("    member.setMemberName(\"Pixy\");");
        br.addElement("    memberBhv.updateNonstrict(member); // *Point");
        setupEntityElement(br, entity);
        final String msg = br.buildExceptionMessage();
        throw new OptimisticLockColumnValueNullException(msg);
    }

    public <ENTITY extends Entity> void throwNonQueryUpdateNotAllowedException(ENTITY entity, ConditionBean cb,
            UpdateOption<? extends ConditionBean> option) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The query-update without a query condition is not allowed.");
        br.addItem("Advice");
        br.addElement("Confirm your condition values for queryUpdate().");
        br.addElement("If you want to update all records, use varyingQueryUpdate().");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    Member member = new Member();");
        br.addElement("    member.setMemberStatusCode_Formalized();");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    memberBhv.queryUpdate(member, cb);");
        br.addElement("  (o): (exists query)");
        br.addElement("    Member member = new Member();");
        br.addElement("    member.setMemberStatusCode_Formalized();");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    cb.query().setBirthdate_LessThan(...); // *OK");
        br.addElement("    memberBhv.queryUpdate(member, cb);");
        br.addElement("  (o): (non query)");
        br.addElement("    Member member = new Member();");
        br.addElement("    member.setMemberStatusCode_Formalized();");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    UpdateOption<MemberCB> option = new UpdateOption<MemberCB>().allowNonQueryUpdate();");
        br.addElement("    memberBhv.varyingQueryUpdate(member, cb, option); // *OK");
        setupEntityElement(br, entity);
        setupOptionElement(br, option);
        setupInvalidQueryElement(br, cb);
        final String msg = br.buildExceptionMessage();
        throw new NonQueryUpdateNotAllowedException(msg);
    }

    public <ENTITY extends Entity> void throwNonQueryDeleteNotAllowedException(ConditionBean cb,
            DeleteOption<? extends ConditionBean> option) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The query-delete without a query condition is not allowed.");
        br.addItem("Advice");
        br.addElement("Confirm your condition values for queryDelete().");
        br.addElement("If you want to delete all records, use varyingQueryDelete().");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    memberBhv.queryDelete(cb);");
        br.addElement("  (o): (exists query)");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    cb.query().setBirthdate_LessThan(...); // *OK");
        br.addElement("    memberBhv.queryDelete(cb);");
        br.addElement("  (o): (non query)");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    DeleteOption<MemberCB> option = new DeleteOption<MemberCB>().allowNonQueryDelete();");
        br.addElement("    memberBhv.varyingQueryDelete(cb, option); // *OK");
        setupOptionElement(br, option);
        setupInvalidQueryElement(br, cb);
        final String msg = br.buildExceptionMessage();
        throw new NonQueryDeleteNotAllowedException(msg);
    }

    protected void setupEntityElement(ExceptionMessageBuilder br, Entity entity) {
        br.addItem("Entity");
        br.addElement(entity);
    }

    protected void setupOptionElement(ExceptionMessageBuilder br, WritableOption<? extends ConditionBean> option) {
        br.addItem("Option");
        br.addElement(option);
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
