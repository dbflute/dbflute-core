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
package org.seasar.dbflute.mock;

import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.dbmeta.DBMetaProvider;

/**
 * @author jflute
 */
public class MockDBMetaProvider implements DBMetaProvider {

    public DBMeta provideDBMeta(String tableFlexibleName) {
        return new MockDBMeta();
    }

    public DBMeta provideDBMeta(Class<?> entityType) {
        return new MockDBMeta();
    }

    public DBMeta provideDBMetaChecked(String tableFlexibleName) {
        return new MockDBMeta();
    }

    public DBMeta provideDBMetaChecked(Class<?> entityType) {
        return new MockDBMeta();
    }
}
