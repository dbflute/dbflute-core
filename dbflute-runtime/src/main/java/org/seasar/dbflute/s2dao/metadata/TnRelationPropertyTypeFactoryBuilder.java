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

/**
 * @author modified by jflute (originated in S2Dao)
 */
public interface TnRelationPropertyTypeFactoryBuilder {

    /**
     * Build factory of relation property type.
     * @param localBeanClass The bean type of local entity for the relation. (NotNull)
     * @param localBeanMetaData The bean meta data of local entity for the relation. (NotNull)
     * @param beanAnnotationReader The reader of bean annotation. (NotNull)
     * @param dbMetaData The meta data of database. (NotNull)
     * @param relationNestLevel The nest level of relation. (NotMinus) 
     * @param stopRelationCreation Does it stop nest relation of the relation?
     * @param optionalEntityType The class type of optional entity for relation. (NotNull)
     * @return The created factory. (NotNull)
     */
    TnRelationPropertyTypeFactory build(Class<?> localBeanClass, TnBeanMetaData localBeanMetaData,
            TnBeanAnnotationReader beanAnnotationReader, DatabaseMetaData dbMetaData, int relationNestLevel,
            boolean stopRelationCreation, Class<?> optionalEntityType);
}
