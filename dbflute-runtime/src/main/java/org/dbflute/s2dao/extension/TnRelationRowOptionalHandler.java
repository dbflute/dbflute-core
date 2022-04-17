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
package org.dbflute.s2dao.extension;

import org.dbflute.bhv.core.context.InternalMapContext;
import org.dbflute.helper.beans.DfPropertyDesc;
import org.dbflute.optional.OptionalThingExceptionThrower;
import org.dbflute.optional.RelationOptionalFactory;
import org.dbflute.s2dao.metadata.TnRelationPropertyType;

/**
 * @author jflute
 * @since 1.0.5G (2014/05/20 Tuesday)
 */
public class TnRelationRowOptionalHandler {

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
     * @param row The local table's row, which is previous relation row. (NotNull)
     * @param rpt The property type for the relation. (NotNull)
     * @return The optional object for the relation. (NotNull)
     */
    protected Object createOptionalNullEntity(Object row, TnRelationPropertyType rpt) { // object for override
        return _relationOptionalFactory.createOptionalNullEntity(createOptionalNullThrower(row, rpt));
    }

    protected OptionalThingExceptionThrower createOptionalNullThrower(Object row, TnRelationPropertyType rpt) {
        final String propertyName = rpt.getPropertyDesc().getPropertyName(); // not null
        final String invokePath = InternalMapContext.getSavedInvokePath(); // null allowed
        final String sql = InternalMapContext.getDisplaySqlResourceSql(); // not null (if CB)
        final Object[] args = InternalMapContext.getDisplaySqlResourceParams(); // not null (if CB)
        return newRelationRowOptionalNullThrower(row, propertyName, invokePath, sql, args);
    }

    protected TnRelationRowOptionalNullThrower newRelationRowOptionalNullThrower(Object row, String propertyName, String invokePath,
            String sql, Object[] args) {
        return new TnRelationRowOptionalNullThrower(row, propertyName, invokePath, sql, args);
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
