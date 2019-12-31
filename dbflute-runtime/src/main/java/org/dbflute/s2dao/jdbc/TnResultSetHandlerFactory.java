/*
 * Copyright 2014-2020 the original author or authors.
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
import org.dbflute.s2dao.extension.TnRelationRowOptionalHandler;
import org.dbflute.s2dao.metadata.TnBeanMetaData;

/**
 * @author jflute
 * @since 1.1.0 (2014/11/17 Monday)
 */
public interface TnResultSetHandlerFactory {

    TnResultSetHandler createBeanListResultSetHandler(TnBeanMetaData bmd, TnRelationRowOptionalHandler optionalHandler);

    TnResultSetHandler createBeanOneResultSetHandler(TnBeanMetaData bmd, TnRelationRowOptionalHandler optionalHandler, Object searchKey);

    TnResultSetHandler createBeanCursorResultSetHandler(TnBeanMetaData bmd, TnRelationRowOptionalHandler optionalHandler);

    TnResultSetHandler createScalarResultSetHandler(Class<?> objectType);

    TnResultSetHandler createScalarListResultSetHandler(Class<?> objectType);

    TnResultSetHandler createScalarListResultSetHandler(ValueType valueType);

    TnResultSetHandler createDynamicScalarResultSetHandler(Class<?> objectType);
}
