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

import java.lang.reflect.Method;

import org.seasar.dbflute.dbmeta.DBMeta.OptimisticLockType;
import org.seasar.dbflute.dbmeta.PropertyGateway;
import org.seasar.dbflute.dbmeta.PropertyMethodFinder;
import org.seasar.dbflute.dbmeta.info.ColumnInfo;

/**
 * @author jflute
 */
public class MockColumnInfo extends ColumnInfo {

    public MockColumnInfo() {
        super(new MockDBMeta(), "mock", "mock", null, "mock", Integer.class, "mock", Integer.class, true, false, true,
                "INTEGER", 3, 0, null, false, OptimisticLockType.NONE, "mock", null, null, null,
                createPropertyMethodFinder());
    }

    protected static PropertyMethodFinder createPropertyMethodFinder() {
        return new PropertyMethodFinder() {
            public Method findWriteMethod(Class<?> beanType, String propertyName, Class<?> propertyType) {
                return null;
            }

            public Method findReadMethod(Class<?> beanType, String propertyName, Class<?> propertyType) {
                return null;
            }
        };
    }

    @Override
    protected PropertyGateway findPropertyGateway() {
        return null;
    }

    @Override
    protected Method findReadMethod() {
        return null;
    }

    @Override
    protected Method findWriteMethod() {
        return null;
    }
}
