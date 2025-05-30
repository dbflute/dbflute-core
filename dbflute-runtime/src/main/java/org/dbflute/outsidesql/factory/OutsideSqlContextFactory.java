/*
 * Copyright 2014-2025 the original author or authors.
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
package org.dbflute.outsidesql.factory;

import org.dbflute.dbmeta.DBMetaProvider;
import org.dbflute.outsidesql.OutsideSqlContext;

/**
 * @author jflute
 */
public interface OutsideSqlContextFactory {

    /**
     * Create the context of outside-SQL.
     * @param dbmetaProvider The provider of DB meta. (NotNull)
     * @param outsideSqlPackage The package of outside-SQL. (NotNull)
     * @return The context of outside-SQL. (NotNull)
     */
    OutsideSqlContext createContext(DBMetaProvider dbmetaProvider, String outsideSqlPackage);
}
