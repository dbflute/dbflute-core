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
package org.dbflute.bhv.core.context.logmask.parts;

import java.util.Map;

import org.dbflute.Entity;
import org.dbflute.dbmeta.DBMeta;
import org.dbflute.mock.MockDBMeta;
import org.dbflute.mock.MockEntity;
import org.dbflute.unit.RuntimeTestCase;

/**
 * @author jflute
 * @since 1.2.7 (2023/07/10 Sunday at roppongi japanese)
 */
public class AlreadyUpdatedBeanPKOnlyMaskManTest extends RuntimeTestCase {

    public void test_mask_basic() {
        // ## Arrange ##
        AlreadyUpdatedBeanPKOnlyMaskMan maskMan = new AlreadyUpdatedBeanPKOnlyMaskMan();

        // ## Act ##
        // ## Assert ##
        assertNull(maskMan.mask(null));
        {
            // #for_now jflute needs more rich mock Entity and DBMeta (2023/07/10)
            MockEntity entity = new MockEntity() {
                @Override
                public DBMeta asDBMeta() {
                    return new MockDBMeta() {
                        @Override
                        public Map<String, Object> extractPrimaryKeyMap(Entity entity) {
                            return newLinkedHashMap("pk", "sea");
                        }
                    };
                }
            };
            assertEquals("{pk=sea}", maskMan.mask(entity));
        }
        assertEquals("(unknown bean: Object)", maskMan.mask(new Object()));
    }
}
