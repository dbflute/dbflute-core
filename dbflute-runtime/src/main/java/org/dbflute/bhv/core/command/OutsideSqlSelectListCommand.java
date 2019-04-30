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
package org.dbflute.bhv.core.command;

import java.util.List;

import org.dbflute.jdbc.ValueType;
import org.dbflute.s2dao.jdbc.TnResultSetHandler;
import org.dbflute.s2dao.metadata.TnBeanMetaData;
import org.dbflute.s2dao.valuetype.TnValueTypes;

/**
 * The behavior command for OutsideSql.selectList().
 * @author jflute
 * @param <ENTITY> The type of entity.
 */
public class OutsideSqlSelectListCommand<ENTITY> extends AbstractOutsideSqlSelectCommand<List<ENTITY>> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The type of entity. (Required) */
    protected Class<ENTITY> _entityType;

    // ===================================================================================
    //                                                                   Basic Information
    //                                                                   =================
    public String getCommandName() {
        return "selectList";
    }

    public Class<?> getCommandReturnType() {
        return List.class;
    }

    // ===================================================================================
    //                                                                  OutsideSql Element
    //                                                                  ==================
    @Override
    protected Class<?> getResultType() {
        return _entityType;
    }

    // ===================================================================================
    //                                                               SqlExecution Handling
    //                                                               =====================
    @Override
    protected TnResultSetHandler createOutsideSqlSelectResultSetHandler() {
        final TnBeanMetaData bmd = createBeanMetaData();
        return createOutsideSqlBeanListResultSetHandler(bmd);
    }

    // ===================================================================================
    //                                                                             Factory
    //                                                                             =======
    // -----------------------------------------------------
    //                                          BeanMetaData
    //                                          ------------
    protected TnBeanMetaData createBeanMetaData() {
        assertEntityType("createBeanMetaData");
        return _beanMetaDataFactory.createBeanMetaData(_entityType);
    }

    // -----------------------------------------------------
    //                                      ResultSetHandler
    //                                      ----------------
    protected TnResultSetHandler createOutsideSqlBeanListResultSetHandler(TnBeanMetaData bmd) {
        final ValueType valueType = TnValueTypes.getValueType(_entityType);
        if (!valueType.equals(TnValueTypes.DEFAULT_OBJECT)) {
            return createScalarListResultSetHandler(valueType);
        }
        return createBeanListResultSetHandler(bmd);
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    protected void assertEntityType(String methodName) {
        if (_entityType == null) {
            throw new IllegalStateException(buildAssertMessage("_entityType", methodName));
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setEntityType(Class<ENTITY> entityType) {
        _entityType = entityType;
    }
}
