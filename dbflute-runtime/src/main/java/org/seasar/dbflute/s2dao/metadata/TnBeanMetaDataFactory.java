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
package org.seasar.dbflute.s2dao.metadata;

import java.sql.DatabaseMetaData;

import org.seasar.dbflute.s2dao.extension.TnRelationRowOptionalHandler;

/**
 * The factory of bean meta data.
 * @author modified by jflute (originated in S2Dao)
 */
public interface TnBeanMetaDataFactory {

    /**
     * Create the bean meta data as relation nest level 0.
     * @param beanClass The type of bean. (NotNull)
     * @return The created bean meta data (or cached instance). (NotNull)
     */
    TnBeanMetaData createBeanMetaData(Class<?> beanClass);

    /**
     * Create the bean meta data for relation.
     * @param beanClass The type of bean. (NotNull)
     * @param relationNestLevel The nest level of relation. (NotMinus)
     * @return The created bean meta data (or cached instance). (NotNull)
     */
    TnBeanMetaData createBeanMetaData(Class<?> beanClass, int relationNestLevel);

    /**
     * Create the bean meta data for relation with specified database meta data. <br />
     * Other methods also use meta data but you can specify your own meta data by this.
     * @param dbMetaData The meta data of database. (NotNull)
     * @param beanClass The type of bean. (NotNull)
     * @param relationNestLevel The nest level of relation. (NotMinus)
     * @return The created bean meta data (or cached instance). (NotNull)
     */
    TnBeanMetaData createBeanMetaData(DatabaseMetaData dbMetaData, Class<?> beanClass, int relationNestLevel);

    /**
     * Get the factory for relation optional object. <br />
     * Basically always return the same instance.
     * @return The instance of factory. (NotNull)
     */
    TnRelationRowOptionalHandler getRelationRowOptionalHandler();
}
