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
package org.seasar.dbflute.cbean;

import org.seasar.dbflute.mock.MockColumnInfo;
import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 * @since 0.9.5 (2009/05/27 Wednesday)
 */
public class ResultBeanBuilderTest extends PlainTestCase {

    public void test_buildEmptyListResultBean() {
        // ## Arrange ##
        SimplePagingBean pb = new SimplePagingBean();
        pb.fetchFirst(30);
        pb.getSqlClause().registerOrderBy("aaa", true, new MockColumnInfo());
        ResultBeanBuilder<String> tgt = createTarget();

        // ## Act ##
        ListResultBean<String> actualList = tgt.buildEmptyListResultBean(pb);

        // ## Assert ##
        assertEquals(0, actualList.size());
        assertEquals(0, actualList.getSelectedList().size());
        assertEquals("dummy", actualList.getTableDbName());
        assertTrue(actualList.getOrderByClause().isSameAsFirstElementColumnName("aaa"));
    }

    protected ResultBeanBuilder<String> createTarget() {
        return new ResultBeanBuilder<String>("dummy");
    }
}
