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
package org.seasar.dbflute.s2dao.extension;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.Entity;
import org.seasar.dbflute.cbean.ConditionBean;
import org.seasar.dbflute.cbean.ConditionBeanContext;
import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.exception.RelationEntityNotFoundException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.beans.DfPropertyDesc;
import org.seasar.dbflute.optional.OptionalObjectExceptionThrower;
import org.seasar.dbflute.optional.RelationOptionalFactory;
import org.seasar.dbflute.outsidesql.OutsideSqlContext;
import org.seasar.dbflute.s2dao.metadata.TnRelationPropertyType;

/**
 * @author jflute
 * @since 1.0.5G (2014/05/20 Tuesday)
 */
public class TnRelationRowOptionalHandler {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log _log = LogFactory.getLog(TnRelationRowOptionalHandler.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final RelationOptionalFactory _relationOptionalFactory;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public TnRelationRowOptionalHandler(RelationOptionalFactory relationOptionalFactory) {
        _relationOptionalFactory = relationOptionalFactory;
    }

    // ===================================================================================
    //                                                                           Filtering
    //                                                                           =========
    /**
     * Filter the relation row as optional object if it needs.
     * @param row The base point row, which is previous relation row. (NotNull)
     * @param rpt The property type for the relation. (NotNull)
     * @param relationRow The row instance of relation entity. (NullAllowed)
     * @return The filtered instance of relation entity. (NullAllowed)
     */
    public Object filterOptionalRelationRowIfNeeds(Object row, TnRelationPropertyType rpt, Object relationRow) {
        final Class<?> optionalType = getOptionalEntityType();
        final DfPropertyDesc pd = rpt.getPropertyDesc();
        if (optionalType.isAssignableFrom(pd.getPropertyType())) {
            if (relationRow == null) {
                return createOptionalNullEntity(row, rpt);
            }
            if (!optionalType.isInstance(relationRow)) {
                return createOptionalPresentEntity(relationRow);
            }
        }
        return relationRow;
    }

    // ===================================================================================
    //                                                                         Null Entity
    //                                                                         ===========
    /**
     * Create optional null entity.
     * @param row The base point row, which is previous relation row. (NotNull)
     * @param rpt The property type for the relation. (NotNull)
     * @return The optional object for the relation. (NotNull)
     */
    protected Object createOptionalNullEntity(Object row, TnRelationPropertyType rpt) { // object for override
        return _relationOptionalFactory.createOptionalNullEntity(createOptionalNullableThrower(row, rpt));
    }

    protected OptionalObjectExceptionThrower createOptionalNullableThrower(final Object row, TnRelationPropertyType rpt) {
        final String propertyName = rpt.getPropertyDesc().getPropertyName();
        final DBMeta localDBMeta = rpt.getMyBeanMetaData().getDBMeta();
        final ConditionBean cb;
        if (ConditionBeanContext.isExistConditionBeanOnThread()) {
            cb = ConditionBeanContext.getConditionBeanOnThread();
        } else {
            cb = null;
        }
        final String outsideSqlPath;
        final Object parameterBean;
        if (OutsideSqlContext.isExistOutsideSqlContextOnThread()) {
            final OutsideSqlContext context = OutsideSqlContext.getOutsideSqlContextOnThread();
            outsideSqlPath = context.getOutsideSqlPath();
            parameterBean = context.getParameterBean();
        } else {
            outsideSqlPath = null;
            parameterBean = null;
        }
        return new OptionalObjectExceptionThrower() {
            public void throwNotFoundException() {
                throwRelationEntityNotFoundException(row, propertyName, localDBMeta, cb, outsideSqlPath, parameterBean);
            }
        };
    }

    protected void throwRelationEntityNotFoundException(Object row, String propertyName, DBMeta localDBMeta,
            ConditionBean cb, String outsideSqlPath, Object parameterBean) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The relation entity was NOT found.");
        br.addItem("Advice");
        br.addElement("Confirm the existence in your business rule.");
        br.addElement("If the relation entity might not exist, ...");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    cb.setupSelect_MemberServiceAsOne();");
        br.addElement("    List<Member> memberList = memberBhv.selectList(cb);");
        br.addElement("    for (Member member : memberList) {");
        br.addElement("        ... = member.getMemberServiceAsOne().get()...; // *No");
        br.addElement("    }");
        br.addElement("  (o):");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    cb.setupSelect_MemberServiceAsOne();");
        br.addElement("    List<Member> memberList = memberBhv.selectList(cb);");
        br.addElement("    for (Member member : memberList) {");
        br.addElement("        member.getMemberServiceAsOne().ifPresent(service -> {");
        br.addElement("            ... = service.getServicePointCount(); // OK");
        br.addElement("        });");
        br.addElement("        // and you can use isPresent(), map(), orElseNull(), ...");
        br.addElement("    }");
        br.addItem("Your Operation");
        final String localTable = localDBMeta.getTableDbName();
        final String localSuffix;
        if (row instanceof Entity) { // basically here
            final Map<String, Object> pkMap = localDBMeta.extractPrimaryKeyMap((Entity) row);
            localSuffix = pkMap.toString();
        } else {
            localSuffix = "{" + row + "}";
        }
        br.addElement(localTable + ":" + localSuffix + " -> " + propertyName);
        // cannot get it because this exception is after behavior command
        // (thread locals are destroyed at that time)
        //final InvokePathProvider invokePathProvider = InternalMapContext.getInvokePathProvider();
        //if (invokePathProvider != null) {
        //    final String invokePath = invokePathProvider.provide();
        //    br.addItem("Behavior");
        //    br.addElement(invokePath);
        //}
        if (cb != null) {
            try {
                final String displaySql = cb.toDisplaySql();
                br.addItem("ConditionBean");
                br.addElement(displaySql);
            } catch (RuntimeException continued) {
                if (_log.isDebugEnabled()) {
                    final String tableDbName = cb.getTableDbName();
                    _log.debug("Failed to get display SQL from the condition-bean for debug: " + tableDbName);
                }
            }
        }
        if (outsideSqlPath != null) {
            br.addItem("OutsideSql");
            br.addElement("path : " + outsideSqlPath);
            br.addElement("pmb  : " + parameterBean);
        }
        final String msg = br.buildExceptionMessage();
        throw new RelationEntityNotFoundException(msg);
    }

    // ===================================================================================
    //                                                                      Present Entity
    //                                                                      ==============
    /**
     * Create optional present entity.
     * @param relationRow The row instance of relation entity. (NullAllowed)
     * @return The optional object for the relation. (NotNull)
     */
    protected Object createOptionalPresentEntity(Object relationRow) { // object for override
        return _relationOptionalFactory.createOptionalPresentEntity(relationRow);
    }

    // ===================================================================================
    //                                                                 OptionalEntity Type
    //                                                                 ===================
    /**
     * Get the type of optional entity for relation.
     * @return The class type of optional entity. (NotNull)
     */
    public Class<?> getOptionalEntityType() {
        return _relationOptionalFactory.getOptionalEntityType();
    }
}
