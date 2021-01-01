/*
 * Copyright 2014-2021 the original author or authors.
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
package org.dbflute.twowaysql.node;

import java.util.List;
import java.util.Map;

import org.dbflute.cbean.coption.LikeSearchOption;
import org.dbflute.twowaysql.exception.BindVariableCommentListIndexNotNumberException;
import org.dbflute.twowaysql.exception.BindVariableCommentListIndexOutOfBoundsException;
import org.dbflute.twowaysql.exception.BindVariableCommentNotFoundPropertyException;
import org.dbflute.twowaysql.exception.ForCommentNotFoundPropertyException;
import org.dbflute.twowaysql.exception.ForCommentPropertyReadFailureException;
import org.dbflute.twowaysql.pmbean.SimpleMapPmb;
import org.dbflute.unit.RuntimeTestCase;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;

/**
 * @author jflute
 */
public class BoundValueTracerTest extends RuntimeTestCase {

    // ===================================================================================
    //                                                                                Bean
    //                                                                                ====
    public void test_setupValueAndType_bean_basic() {
        // ## Arrange ##
        BoundValueTracer setupper = createTargetAsBind("pmb.memberId");
        MockPmb pmb = new MockPmb();
        pmb.setMemberId(3);
        BoundValue valueAndType = createTargetAndType(pmb);

        // ## Act ##
        setupper.trace(valueAndType);

        // ## Assert ##
        assertEquals(3, valueAndType.getTargetValue());
        assertEquals(Integer.class, valueAndType.getTargetType());
        assertNull(valueAndType.getFilteringBindOption());
    }

    public void test_setupValueAndType_bean_likeSearch() {
        // ## Arrange ##
        BoundValueTracer setupper = createTargetAsBind("pmb.memberName");
        MockPmb pmb = new MockPmb();
        pmb.setMemberName("f|o%o");
        pmb.setMemberNameInternalLikeSearchOption(new LikeSearchOption().likePrefix());
        BoundValue valueAndType = createTargetAndType(pmb);

        // ## Act ##
        setupper.trace(valueAndType);
        valueAndType.filterValueByOptionIfNeeds();

        // ## Assert ##
        assertEquals("f||o|%o%", valueAndType.getTargetValue());
        assertEquals(String.class, valueAndType.getTargetType());
        assertEquals(" escape '|'", valueAndType.getFilteringBindOption().getRearOption());
    }

    public void test_setupValueAndType_bean_likeSearch_notFound() {
        // ## Arrange ##
        BoundValueTracer setupper = createTargetAsBind("pmb.memberName");
        MockPmb pmb = new MockPmb();
        pmb.setMemberName("f|o%o");
        //pmb.setMemberNameInternalLikeSearchOption(new LikeSearchOption().likePrefix());
        BoundValue valueAndType = createTargetAndType(pmb);

        // ## Act ##
        setupper.trace(valueAndType);

        // ## Assert ##
        assertEquals("f|o%o", valueAndType.getTargetValue());
        assertEquals(String.class, valueAndType.getTargetType());
    }

    public void test_setupValueAndType_bean_likeSearch_split() {
        // ## Arrange ##
        BoundValueTracer setupper = createTargetAsBind("pmb.memberName");
        MockPmb pmb = new MockPmb();
        pmb.setMemberName("f|o%o");
        pmb.setMemberNameInternalLikeSearchOption(new LikeSearchOption().likePrefix().splitByPipeLine());
        BoundValue valueAndType = createTargetAndType(pmb);

        // ## Act ##
        setupper.trace(valueAndType); // no check here
        valueAndType.filterValueByOptionIfNeeds();

        // ## Assert ##
        assertEquals("f||o|%o%", valueAndType.getTargetValue());
        assertEquals(String.class, valueAndType.getTargetType());
    }

    public void test_setupValueAndType_bean_nest() {
        // ## Arrange ##
        BoundValueTracer setupper = createTargetAsBind("pmb.nestPmb.memberId");
        MockPmb nestPmb = new MockPmb();
        nestPmb.setMemberId(3);
        MockPmb pmb = new MockPmb();
        pmb.setNestPmb(nestPmb);
        BoundValue valueAndType = createTargetAndType(pmb);

        // ## Act ##
        setupper.trace(valueAndType);

        // ## Assert ##
        assertEquals(3, valueAndType.getTargetValue());
        assertEquals(Integer.class, valueAndType.getTargetType());
        assertNull(valueAndType.getFilteringBindOption());
    }

    public void test_setupValueAndType_bean_nest_likeSearch_basic() {
        // ## Arrange ##
        BoundValueTracer setupper = createTargetAsBind("pmb.nestLikePmb.memberName");
        MockPmb nestLikePmb = new MockPmb();
        nestLikePmb.setMemberName("f|o%o");
        MockPmb pmb = new MockPmb();
        pmb.setNestLikePmb(nestLikePmb);
        pmb.setNestLikePmbInternalLikeSearchOption(new LikeSearchOption().likePrefix());
        BoundValue valueAndType = createTargetAndType(pmb);

        // ## Act ##
        setupper.trace(valueAndType);
        valueAndType.filterValueByOptionIfNeeds();

        // ## Assert ##
        assertEquals("f||o|%o%", valueAndType.getTargetValue());
        assertEquals(String.class, valueAndType.getTargetType());
        assertEquals(" escape '|'", valueAndType.getFilteringBindOption().getRearOption());
    }

    public void test_setupValueAndType_bean_nest_likeSearch_override() {
        // ## Arrange ##
        BoundValueTracer setupper = createTargetAsBind("pmb.nestLikePmb.memberName");
        MockPmb nestLikePmb = new MockPmb();
        nestLikePmb.setMemberName("f|o%o");
        nestLikePmb.setMemberNameInternalLikeSearchOption(new LikeSearchOption().likeContain());
        MockPmb pmb = new MockPmb();
        pmb.setNestLikePmb(nestLikePmb);
        pmb.setNestLikePmbInternalLikeSearchOption(new LikeSearchOption().likePrefix());
        BoundValue valueAndType = createTargetAndType(pmb);

        // ## Act ##
        setupper.trace(valueAndType);
        valueAndType.filterValueByOptionIfNeeds();

        // ## Assert ##
        assertEquals("%f||o|%o%", valueAndType.getTargetValue());
        assertEquals(String.class, valueAndType.getTargetType());
        assertEquals(" escape '|'", valueAndType.getFilteringBindOption().getRearOption());
    }

    public void test_setupValueAndType_bean_propertyReadFailure() {
        // ## Arrange ##
        BoundValueTracer setupper = createTargetAsForComment("pmb.memberId");
        MockPmb pmb = new MockPmb() {
            @Override
            public Integer getMemberId() { // not accessible
                return super.getMemberId();
            }
        };
        pmb.setMemberId(3);
        BoundValue valueAndType = createTargetAndType(pmb);

        // ## Act ##
        try {
            setupper.trace(valueAndType);

            // ## Assert ##
            fail();
        } catch (ForCommentPropertyReadFailureException e) {
            // OK
            log(e.getMessage());
        }
    }

    public void test_setupValueAndType_bean_notFoundProperty() {
        // ## Arrange ##
        BoundValueTracer setupper = createTargetAsForComment("pmb.memberIo");
        MockPmb pmb = new MockPmb();
        pmb.setMemberId(3);
        BoundValue valueAndType = createTargetAndType(pmb);

        // ## Act ##
        try {
            setupper.trace(valueAndType);

            // ## Assert ##
            fail();
        } catch (ForCommentNotFoundPropertyException e) {
            // OK
            log(e.getMessage());
        }
    }

    // ===================================================================================
    //                                                                                List
    //                                                                                ====
    public void test_setupValueAndType_list_likeSearch() {
        // ## Arrange ##
        BoundValueTracer setupper = createTargetAsBind("pmb.memberNameList.get(1)");
        MockPmb pmb = new MockPmb();
        pmb.setMemberNameList(DfCollectionUtil.newArrayList("f|oo", "ba%r", "b|a%z"));
        pmb.setMemberNameListInternalLikeSearchOption(new LikeSearchOption().likePrefix());
        BoundValue valueAndType = createTargetAndType(pmb);

        // ## Act ##
        setupper.trace(valueAndType);
        valueAndType.filterValueByOptionIfNeeds();

        // ## Assert ##
        assertEquals("ba|%r%", valueAndType.getTargetValue());
        assertEquals(String.class, valueAndType.getTargetType());
        assertEquals(" escape '|'", valueAndType.getFilteringBindOption().getRearOption());
    }

    public void test_setupValueAndType_list_notNumber() {
        // ## Arrange ##
        BoundValueTracer setupper = createTargetAsBind("pmb.memberNameList.get(index)");
        MockPmb pmb = new MockPmb();
        pmb.setMemberNameList(DfCollectionUtil.newArrayList("f|oo", "ba%r", "b|a%z"));
        pmb.setMemberNameListInternalLikeSearchOption(new LikeSearchOption().likePrefix());
        BoundValue valueAndType = createTargetAndType(pmb);

        // ## Act ##
        try {
            setupper.trace(valueAndType);

            // ## Assert ##
            fail();
        } catch (BindVariableCommentListIndexNotNumberException e) {
            // OK
            log(e.getMessage());
        }
    }

    public void test_setupValueAndType_list_outOfBounds() {
        // ## Arrange ##
        BoundValueTracer setupper = createTargetAsBind("pmb.memberNameList.get(4)");
        MockPmb pmb = new MockPmb();
        pmb.setMemberNameList(DfCollectionUtil.newArrayList("f|oo", "ba%r", "b|a%z"));
        pmb.setMemberNameListInternalLikeSearchOption(new LikeSearchOption().likePrefix());
        BoundValue valueAndType = createTargetAndType(pmb);

        // ## Act ##
        try {
            setupper.trace(valueAndType);

            // ## Assert ##
            fail();
        } catch (BindVariableCommentListIndexOutOfBoundsException e) {
            // OK
            log(e.getMessage());
        }
    }

    // ===================================================================================
    //                                                                              MapPmb
    //                                                                              ======
    public void test_setupValueAndType_mappmb_basic() {
        // ## Arrange ##
        BoundValueTracer setupper = createTargetAsBind("pmb.memberId");
        SimpleMapPmb<Integer> pmb = new SimpleMapPmb<Integer>();
        pmb.addParameter("memberId", 3);
        BoundValue valueAndType = createTargetAndType(pmb);

        // ## Act ##
        setupper.trace(valueAndType);

        // ## Assert ##
        assertEquals(3, valueAndType.getTargetValue());
        assertEquals(Integer.class, valueAndType.getTargetType());
        assertNull(valueAndType.getFilteringBindOption());
    }

    public void test_setupValueAndType_mappmb_likeSearch() {
        // ## Arrange ##
        BoundValueTracer setupper = createTargetAsBind("pmb.memberName");
        SimpleMapPmb<Object> pmb = new SimpleMapPmb<Object>();
        pmb.addParameter("memberId", 3);
        pmb.addParameter("memberName", "f|o%o");
        pmb.addParameter("memberNameInternalLikeSearchOption", new LikeSearchOption().likePrefix());
        BoundValue valueAndType = createTargetAndType(pmb);

        // ## Act ##
        setupper.trace(valueAndType);
        valueAndType.filterValueByOptionIfNeeds();

        // ## Assert ##
        assertEquals("f||o|%o%", valueAndType.getTargetValue());
        assertEquals(String.class, valueAndType.getTargetType());
        assertEquals(" escape '|'", valueAndType.getFilteringBindOption().getRearOption());
    }

    public void test_setupValueAndType_mappmb_notKey() {
        // ## Arrange ##
        BoundValueTracer setupper = createTargetAsBind("pmb.memberId");
        SimpleMapPmb<Integer> pmb = new SimpleMapPmb<Integer>();
        BoundValue valueAndType = createTargetAndType(pmb);

        // ## Act ##
        try {
            setupper.trace(valueAndType);

            // ## Assert ##
            fail();
        } catch (BindVariableCommentNotFoundPropertyException e) {
            // OK
            log(e.getMessage());
        }
    }

    // ===================================================================================
    //                                                                                 Map
    //                                                                                 ===
    public void test_setupValueAndType_map_basic() {
        // ## Arrange ##
        BoundValueTracer setupper = createTargetAsBind("pmb.memberId");
        Map<String, Object> pmb = DfCollectionUtil.newHashMap();
        pmb.put("memberId", 3);
        BoundValue valueAndType = createTargetAndType(pmb);

        // ## Act ##
        setupper.trace(valueAndType);

        // ## Assert ##
        assertEquals(3, valueAndType.getTargetValue());
        assertEquals(Integer.class, valueAndType.getTargetType());
        assertNull(valueAndType.getFilteringBindOption());
    }

    public void test_setupValueAndType_map_likeSearch() {
        // ## Arrange ##
        BoundValueTracer setupper = createTargetAsBind("pmb.memberName");
        Map<String, Object> pmb = DfCollectionUtil.newHashMap();
        pmb.put("memberId", 3);
        pmb.put("memberName", "f|o%o");
        pmb.put("memberNameInternalLikeSearchOption", new LikeSearchOption().likePrefix());
        BoundValue valueAndType = createTargetAndType(pmb);

        // ## Act ##
        setupper.trace(valueAndType);
        valueAndType.filterValueByOptionIfNeeds();

        // ## Assert ##
        assertEquals("f||o|%o%", valueAndType.getTargetValue());
        assertEquals(String.class, valueAndType.getTargetType());
        assertEquals(" escape '|'", valueAndType.getFilteringBindOption().getRearOption());
    }

    public void test_setupValueAndType_map_notKey() {
        // ## Arrange ##
        BoundValueTracer setupper = createTargetAsBind("pmb.memberId");
        Map<String, Object> pmb = DfCollectionUtil.newHashMap();
        BoundValue valueAndType = createTargetAndType(pmb);

        // ## Act ##
        setupper.trace(valueAndType);

        // ## Assert ##
        assertEquals(null, valueAndType.getTargetValue());
        assertEquals(null, valueAndType.getTargetType());
        assertNull(valueAndType.getFilteringBindOption());
    }

    // ===================================================================================
    //                                                                         Test Helper
    //                                                                         ===========
    protected BoundValueTracer createTargetAsBind(String expression) {
        ParameterCommentType type = ParameterCommentType.BIND;
        return new BoundValueTracer(Srl.splitList(expression, "."), expression, "select * from ...", type);
    }

    protected BoundValueTracer createTargetAsForComment(String expression) {
        ParameterCommentType type = ParameterCommentType.FORCOMMENT;
        return new BoundValueTracer(Srl.splitList(expression, "."), expression, "select * from ...", type);
    }

    protected BoundValue createTargetAndType(Object value) {
        BoundValue valueAndType = new BoundValue();
        valueAndType.setFirstValue(value);
        valueAndType.setFirstType(value.getClass());
        return valueAndType;
    }

    protected static class MockPmb {
        protected Integer _memberId;
        protected String _memberName;
        protected LikeSearchOption _memberNameInternalLikeSearchOption;
        protected List<String> _memberNameList;
        protected LikeSearchOption _memberNameListInternalLikeSearchOption;
        protected MockPmb _nestPmb;
        protected MockPmb _nestLikePmb;
        protected LikeSearchOption _nestLikePmbInternalLikeSearchOption;

        public Integer getMemberId() {
            return _memberId;
        }

        public void setMemberId(Integer memberId) {
            this._memberId = memberId;
        }

        public String getMemberName() {
            return _memberName;
        }

        public void setMemberName(String memberName) {
            this._memberName = memberName;
        }

        public LikeSearchOption getMemberNameInternalLikeSearchOption() {
            return _memberNameInternalLikeSearchOption;
        }

        public void setMemberNameInternalLikeSearchOption(LikeSearchOption memberNameInternalLikeSearchOption) {
            this._memberNameInternalLikeSearchOption = memberNameInternalLikeSearchOption;
        }

        public List<String> getMemberNameList() {
            return _memberNameList;
        }

        public void setMemberNameList(List<String> memberNameList) {
            this._memberNameList = memberNameList;
        }

        public LikeSearchOption getMemberNameListInternalLikeSearchOption() {
            return _memberNameListInternalLikeSearchOption;
        }

        public void setMemberNameListInternalLikeSearchOption(LikeSearchOption memberNameListInternalLikeSearchOption) {
            this._memberNameListInternalLikeSearchOption = memberNameListInternalLikeSearchOption;
        }

        public MockPmb getNestPmb() {
            return _nestPmb;
        }

        public void setNestPmb(MockPmb nestPmb) {
            this._nestPmb = nestPmb;
        }

        public MockPmb getNestLikePmb() {
            return _nestLikePmb;
        }

        public void setNestLikePmb(MockPmb nestLikePmb) {
            this._nestLikePmb = nestLikePmb;
        }

        public LikeSearchOption getNestLikePmbInternalLikeSearchOption() {
            return _nestLikePmbInternalLikeSearchOption;
        }

        public void setNestLikePmbInternalLikeSearchOption(LikeSearchOption nestLikePmbInternalLikeSearchOption) {
            this._nestLikePmbInternalLikeSearchOption = nestLikePmbInternalLikeSearchOption;
        }
    }
}
