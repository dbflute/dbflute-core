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
package org.seasar.dbflute.s2dao.extension;

import junit.framework.TestCase;

/**
 * @author jflute
 * @since 0.9.5 (2009/05/27 Wednesday)
 */
public class TnRowCreatorExtensionTest extends TestCase {

    public void test_isBeanAssignableFromEntity() {
        // ## Arrange & Act & Assert ##
        Class<?> entityType = ExCustomizeEntity.class;
        assertTrue(TnRowCreatorExtension.isCreatableByDBMeta(BsCustomizeEntity.class, entityType));
        assertTrue(TnRowCreatorExtension.isCreatableByDBMeta(ExCustomizeEntity.class, entityType));
        assertFalse(TnRowCreatorExtension.isCreatableByDBMeta(ManualCustomizeEntity.class, entityType));
    }

    protected static class BsCustomizeEntity {

    }

    protected static class ExCustomizeEntity extends BsCustomizeEntity {

    }

    protected static class ManualCustomizeEntity extends ExCustomizeEntity {

    }
}
