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
package org.dbflute.s2dao.jdbc;

import org.dbflute.jdbc.ValueType;
import org.dbflute.s2dao.extension.TnRelationRowCreatorExtension;
import org.dbflute.s2dao.extension.TnRelationRowOptionalHandler;
import org.dbflute.s2dao.extension.TnRowCreatorExtension;
import org.dbflute.s2dao.metadata.TnBeanMetaData;
import org.dbflute.s2dao.rshandler.TnBeanCursorResultSetHandler;
import org.dbflute.s2dao.rshandler.TnBeanListResultSetHandler;
import org.dbflute.s2dao.rshandler.TnBeanOneResultSetHandler;
import org.dbflute.s2dao.rshandler.TnScalarDynamicResultSetHandler;
import org.dbflute.s2dao.rshandler.TnScalarListResultSetHandler;
import org.dbflute.s2dao.rshandler.TnScalarResultSetHandler;
import org.dbflute.s2dao.valuetype.TnValueTypes;

/**
 * @author jflute
 * @since 1.1.0 (2014/11/17 Monday)
 */
public class TnResultSetHandlerFactoryImpl implements TnResultSetHandlerFactory {

    // ===================================================================================
    //                                                                  Result Set Handler
    //                                                                  ==================
    // -----------------------------------------------------
    //                                             Bean List
    //                                             ---------
    public TnResultSetHandler createBeanListResultSetHandler(TnBeanMetaData bmd, TnRelationRowOptionalHandler optionalHandler) {
        final TnRowCreatorExtension rowCreator = createRowCreator(bmd);
        final TnRelationRowCreatorExtension relationRowCreator = createRelationRowCreator(bmd, optionalHandler);
        return new TnBeanListResultSetHandler(bmd, rowCreator, relationRowCreator);
    }

    public TnResultSetHandler createBeanOneResultSetHandler(TnBeanMetaData bmd, TnRelationRowOptionalHandler optionalHandler,
            Object searchKey) {
        final TnRowCreatorExtension rowCreator = createRowCreator(bmd);
        final TnRelationRowCreatorExtension relationRowCreator = createRelationRowCreator(bmd, optionalHandler);
        return new TnBeanOneResultSetHandler(bmd, rowCreator, relationRowCreator, searchKey);
    }

    public TnResultSetHandler createBeanCursorResultSetHandler(TnBeanMetaData bmd, TnRelationRowOptionalHandler optionalHandler) {
        final TnRowCreatorExtension rowCreator = createRowCreator(bmd);
        final TnRelationRowCreatorExtension relationRowCreator = createRelationRowCreator(bmd, optionalHandler);
        return new TnBeanCursorResultSetHandler(bmd, rowCreator, relationRowCreator);
    }

    // -----------------------------------------------------
    //                                                Scalar
    //                                                ------
    public TnResultSetHandler createScalarResultSetHandler(Class<?> objectType) {
        final ValueType valueType = TnValueTypes.getValueType(objectType);
        return new TnScalarResultSetHandler(valueType);
    }

    public TnResultSetHandler createDynamicScalarResultSetHandler(Class<?> objectType) {
        final ValueType valueType = TnValueTypes.getValueType(objectType);
        return new TnScalarDynamicResultSetHandler(valueType);
    }

    public TnResultSetHandler createScalarListResultSetHandler(Class<?> objectType) {
        final ValueType valueType = TnValueTypes.getValueType(objectType);
        return newScalarListResultSetHandler(valueType);
    }

    public TnResultSetHandler createScalarListResultSetHandler(ValueType valueType) {
        return newScalarListResultSetHandler(valueType);
    }

    protected TnScalarListResultSetHandler newScalarListResultSetHandler(ValueType valueType) {
        return new TnScalarListResultSetHandler(valueType);
    }

    // ===================================================================================
    //                                                                         Row Creator
    //                                                                         ===========
    protected TnRowCreatorExtension createRowCreator(TnBeanMetaData bmd) {
        final Class<?> clazz = bmd != null ? bmd.getBeanClass() : null;
        return TnRowCreatorExtension.createRowCreator(clazz);
    }

    protected TnRelationRowCreatorExtension createRelationRowCreator(TnBeanMetaData bmd, TnRelationRowOptionalHandler optionalHandler) {
        return TnRelationRowCreatorExtension.createRelationRowCreator(optionalHandler);
    }
}
