/*
 * Copyright 2014-2014 the original author or authors.
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
package org.dbflute.dbmeta;

import org.dbflute.Entity;

/**
 * The provider of DB meta.
 * @author jflute
 */
public interface DBMetaProvider {

    /**
     * Provide the DB meta by name.
     * @param tableFlexibleName The flexible name of table. (NotNull)
     * @return The instance of DB meta. (NullAllowed: If the DB meta is not found, it returns null)
     */
    DBMeta provideDBMeta(String tableFlexibleName);

    /**
     * Provide the DB meta by type. <br />
     * The generic type of the entity type is wild-card because generic for class type is hard to use.
     * @param entityType The entity type of table, which should implement the {@link Entity} interface. (NotNull)
     * @return The instance of DB meta. (NullAllowed: If the DB meta is not found, it returns null)
     */
    DBMeta provideDBMeta(Class<?> entityType);

    /**
     * Provide the DB meta by name.
     * @param tableFlexibleName The flexible name of table. (NotNull)
     * @return The instance of DB meta. (NotNull)
     * @throws org.dbflute.exception.DBMetaNotFoundException When the DB meta is not found.
     */
    DBMeta provideDBMetaChecked(String tableFlexibleName);

    /**
     * Provide the DB meta by type. <br />
     * The generic type of the entity type is wild-card because generic for class type is hard to use.
     * @param entityType The entity type of table, which should implement the {@link Entity} interface. (NotNull)
     * @return The instance of DB meta. (NotNull)
     * @throws org.dbflute.exception.DBMetaNotFoundException When the DB meta is not found.
     */
    DBMeta provideDBMetaChecked(Class<?> entityType);
}
