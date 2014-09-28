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
package org.seasar.dbflute.logic.sql2entity.outsidesql;

import org.seasar.dbflute.logic.sql2entity.analyzer.DfOutsideSqlNameResolver;
import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 * @since 0.9.5 (2009/04/10 Friday)
 */
public class SqlFileNameResolverTest extends PlainTestCase {

    // ===================================================================================
    //                                                                              Entity
    //                                                                              ======
    public void test_resolveObjectNameIfNeeds_entity_basic() {
        // ## Arrange ##
        DfOutsideSqlNameResolver resolver = new DfOutsideSqlNameResolver();
        String className = DfOutsideSqlNameResolver.ENTITY_MARK;
        String fileName = "MemberBhv_selectSimpleMember.sql";

        // ## Act ##
        String actual = resolver.resolveEntityNameIfNeeds(className, fileName);

        // ## Assert ##
        log(actual);
        assertEquals("SimpleMember", actual);
    }

    public void test_resolveObjectNameIfNeeds_entity_with_DBName() {
        // ## Arrange ##
        DfOutsideSqlNameResolver resolver = new DfOutsideSqlNameResolver();
        String className = DfOutsideSqlNameResolver.ENTITY_MARK;
        String fileName = "MemberBhv_selectSimpleMember_oracle.sql";

        // ## Act ##
        String actual = resolver.resolveEntityNameIfNeeds(className, fileName);

        // ## Assert ##
        log(actual);
        assertEquals("SimpleMember", actual);
    }

    public void test_resolveObjectNameIfNeeds_entity_nonPrefix() {
        // ## Arrange ##
        DfOutsideSqlNameResolver resolver = new DfOutsideSqlNameResolver();
        String className = DfOutsideSqlNameResolver.ENTITY_MARK;
        String fileName = "MemberBhv_SimpleMember.sql";

        // ## Act ##
        String actual = resolver.resolveEntityNameIfNeeds(className, fileName);

        // ## Assert ##
        log(actual);
        assertEquals("SimpleMember", actual);
    }

    public void test_resolveObjectNameIfNeeds_entity_initCap() {
        // ## Arrange ##
        DfOutsideSqlNameResolver resolver = new DfOutsideSqlNameResolver();
        String className = DfOutsideSqlNameResolver.ENTITY_MARK;
        String fileName = "MemberBhv_SelectSimpleMember.sql";

        // ## Act ##
        String actual = resolver.resolveEntityNameIfNeeds(className, fileName);

        // ## Assert ##
        log(actual);
        assertEquals("SelectSimpleMember", actual);
    }

    public void test_resolveObjectNameIfNeeds_entity_noCap() {
        // ## Arrange ##
        DfOutsideSqlNameResolver resolver = new DfOutsideSqlNameResolver();
        String className = DfOutsideSqlNameResolver.ENTITY_MARK;
        String fileName = "MemberBhv_selectsimplemember.sql";

        // ## Act ##
        String actual = resolver.resolveEntityNameIfNeeds(className, fileName);

        // ## Assert ##
        log(actual);
        assertEquals("Selectsimplemember", actual);
    }

    public void test_resolveObjectNameIfNeeds_entity_fullPath_by_slash() {
        // ## Arrange ##
        DfOutsideSqlNameResolver resolver = new DfOutsideSqlNameResolver();
        String className = DfOutsideSqlNameResolver.ENTITY_MARK;
        String fileName = "foo/bar/MemberBhv_selectSimpleMember.sql";

        // ## Act ##
        String actual = resolver.resolveEntityNameIfNeeds(className, fileName);

        // ## Assert ##
        log(actual);
        assertEquals("SimpleMember", actual);
    }

    public void test_resolveObjectNameIfNeeds_entity_fullPath_by_backSlash() {
        // ## Arrange ##
        DfOutsideSqlNameResolver resolver = new DfOutsideSqlNameResolver();
        String className = DfOutsideSqlNameResolver.ENTITY_MARK;
        String fileName = "foo\\bar\\MemberBhv_selectSimpleMember.sql";

        // ## Act ##
        String actual = resolver.resolveEntityNameIfNeeds(className, fileName);

        // ## Assert ##
        log(actual);
        assertEquals("SimpleMember", actual);
    }

    public void test_resolveObjectNameIfNeeds_entity_startsWithUnderScore() {
        // ## Arrange ##
        DfOutsideSqlNameResolver resolver = new DfOutsideSqlNameResolver();
        String className = DfOutsideSqlNameResolver.ENTITY_MARK;
        String fileName = "MemberBhv__selectSimpleMember.sql";

        // ## Act ##
        try {
            resolver.resolveEntityNameIfNeeds(className, fileName);

            // ## Assert ##
            fail();
        } catch (IllegalStateException e) {
            // OK
            log(e.getMessage());
        }
    }

    // *Properties is needed to test 
    //@Test
    //public void test_resolveObjectNameIfNeeds_entity_no_BehaviorQueryPath() {
    //    // ## Arrange ##
    //    DfOutsideSqlNameResolver resolver = new DfOutsideSqlNameResolver();
    //    String className = DfOutsideSqlNameResolver.ENTITY_MARK;
    //
    //    // ## Act ##
    //    String actual = resolver.resolveEntityNameIfNeeds(className, "selectSimpleMember.sql");
    //
    //    // ## Assert ##
    //    log(actual);
    //    assertEquals("SimpleMember", actual);
    //}
    //
    //@Test
    //public void test_resolveObjectNameIfNeeds_entity_no_BehaviorQueryPath_DBSuffix() {
    //    // ## Arrange ##
    //    DfOutsideSqlNameResolver resolver = new DfOutsideSqlNameResolver();
    //    String className = DfOutsideSqlNameResolver.ENTITY_MARK;
    //
    //    // ## Act ##
    //    String actual = resolver.resolveEntityNameIfNeeds(className, "selectSimpleMember_oracle.sql");
    //
    //    // ## Assert ##
    //    log(actual);
    //    assertEquals("SimpleMember", actual);
    //}
    //
    //@Test
    //public void test_resolveObjectNameIfNeeds_entity_no_BehaviorQueryPath_Unsupport() {
    //    // ## Arrange ##
    //    DfOutsideSqlNameResolver resolver = new DfOutsideSqlNameResolver();
    //    String className = DfOutsideSqlNameResolver.ENTITY_MARK;
    //
    //    // ## Act ##
    //    String actual = resolver.resolveEntityNameIfNeeds(className, "Member_selectSimpleMember.sql");
    //
    //    // ## Assert ##
    //    log(actual);
    //    assertEquals("Member", actual);
    //}

    public void test_resolveObjectNameIfNeeds_entity_no_BehaviorQueryPath_startsWithUnderScore() {
        // ## Arrange ##
        DfOutsideSqlNameResolver resolver = new DfOutsideSqlNameResolver();
        String className = DfOutsideSqlNameResolver.ENTITY_MARK;

        // ## Act ##
        try {
            resolver.resolveEntityNameIfNeeds(className, "_selectSimpleMember.sql");

            // ## Assert ##
            fail();
        } catch (IllegalStateException e) {
            // OK
            log(e.getMessage());
        }
    }

    public void test_resolveObjectNameIfNeeds_entity_no_SQLFile() {
        // ## Arrange ##
        DfOutsideSqlNameResolver resolver = new DfOutsideSqlNameResolver();
        String className = DfOutsideSqlNameResolver.ENTITY_MARK;

        // ## Act ##
        try {
            resolver.resolveEntityNameIfNeeds(className, "MemberBhv_selectSimpleMember");

            // ## Assert ##
            fail();
        } catch (IllegalStateException e) {
            // OK
            log(e.getMessage());
        }

        // ## Act ##
        try {
            resolver.resolveEntityNameIfNeeds(className, "MemberBhv_selectSimpleMember");

            // ## Assert ##
            fail();
        } catch (IllegalStateException e) {
            // OK
            log(e.getMessage());
        }
    }

    // ===================================================================================
    //                                                                       ParameterBean
    //                                                                       =============
    public void test_resolveObjectNameIfNeeds_pmb_basic() {
        // ## Arrange ##
        DfOutsideSqlNameResolver resolver = new DfOutsideSqlNameResolver();
        String className = DfOutsideSqlNameResolver.PMB_MARK;
        String fileName = "MemberBhv_selectSimpleMember.sql";

        // ## Act ##
        String actual = resolver.resolvePmbNameIfNeeds(className, fileName);

        // ## Assert ##
        log(actual);
        assertEquals("SimpleMemberPmb", actual);
    }

    public void test_resolveObjectNameIfNeeds_pmb_with_DBName() {
        // ## Arrange ##
        DfOutsideSqlNameResolver resolver = new DfOutsideSqlNameResolver();
        String className = DfOutsideSqlNameResolver.PMB_MARK;
        String fileName = "MemberBhv_selectSimpleMember_oracle.sql";

        // ## Act ##
        String actual = resolver.resolvePmbNameIfNeeds(className, fileName);

        // ## Assert ##
        log(actual);
        assertEquals("SimpleMemberPmb", actual);
    }

    public void test_resolveObjectNameIfNeeds_pmb_nonPrefix() {
        // ## Arrange ##
        DfOutsideSqlNameResolver resolver = new DfOutsideSqlNameResolver();
        String className = DfOutsideSqlNameResolver.PMB_MARK;
        String fileName = "MemberBhv_SimpleMember.sql";

        // ## Act ##
        String actual = resolver.resolvePmbNameIfNeeds(className, fileName);

        // ## Assert ##
        log(actual);
        assertEquals("SimpleMemberPmb", actual);
    }

    public void test_resolveObjectNameIfNeeds_pmb_initCap() {
        // ## Arrange ##
        DfOutsideSqlNameResolver resolver = new DfOutsideSqlNameResolver();
        String className = DfOutsideSqlNameResolver.PMB_MARK;
        String fileName = "MemberBhv_SelectSimpleMember.sql";

        // ## Act ##
        String actual = resolver.resolvePmbNameIfNeeds(className, fileName);

        // ## Assert ##
        log(actual);
        assertEquals("SelectSimpleMemberPmb", actual);
    }

    public void test_resolveObjectNameIfNeeds_pmb_noCap() {
        // ## Arrange ##
        DfOutsideSqlNameResolver resolver = new DfOutsideSqlNameResolver();
        String className = DfOutsideSqlNameResolver.PMB_MARK;
        String fileName = "MemberBhv_selectsimplemember.sql";

        // ## Act ##
        String actual = resolver.resolvePmbNameIfNeeds(className, fileName);

        // ## Assert ##
        log(actual);
        assertEquals("SelectsimplememberPmb", actual);
    }

    public void test_resolveObjectNameIfNeeds_pmb_fullPath_by_slash() {
        // ## Arrange ##
        DfOutsideSqlNameResolver resolver = new DfOutsideSqlNameResolver();
        String className = DfOutsideSqlNameResolver.PMB_MARK;
        String fileName = "foo/bar/MemberBhv_selectSimpleMember.sql";

        // ## Act ##
        String actual = resolver.resolvePmbNameIfNeeds(className, fileName);

        // ## Assert ##
        log(actual);
        assertEquals("SimpleMemberPmb", actual);
    }

    public void test_resolveObjectNameIfNeeds_pmb_fullPath_by_backSlash() {
        // ## Arrange ##
        DfOutsideSqlNameResolver resolver = new DfOutsideSqlNameResolver();
        String className = DfOutsideSqlNameResolver.PMB_MARK;
        String fileName = "foo\\bar\\MemberBhv_selectSimpleMember.sql";

        // ## Act ##
        String actual = resolver.resolvePmbNameIfNeeds(className, fileName);

        // ## Assert ##
        log(actual);
        assertEquals("SimpleMemberPmb", actual);
    }

    // *Properties is needed to test
    //@Test
    //public void test_resolveObjectNameIfNeeds_pmb_no_BehaviorQueryPath() {
    //    // ## Arrange ##
    //    DfOutsideSqlNameResolver resolver = new DfOutsideSqlNameResolver();
    //    String className = DfOutsideSqlNameResolver.PMB_MARK;
    //
    //    // ## Act ##
    //    String actual = resolver.resolvePmbNameIfNeeds(className, "selectSimpleMember.sql");
    //
    //    // ## Assert ##
    //    log(actual);
    //    assertEquals("SimpleMemberPmb", actual);
    //}

    public void test_resolveObjectNameIfNeeds_pmb_no_SQLFile() {
        // ## Arrange ##
        DfOutsideSqlNameResolver resolver = new DfOutsideSqlNameResolver();
        String className = DfOutsideSqlNameResolver.PMB_MARK;

        // ## Act ##
        try {
            resolver.resolvePmbNameIfNeeds(className, "MemberBhv_selectSimpleMember");

            // ## Assert ##
            fail();
        } catch (IllegalStateException e) {
            // OK
            log(e.getMessage());
        }

        // ## Act ##
        try {
            resolver.resolvePmbNameIfNeeds(className, "MemberBhv_selectSimpleMember");

            // ## Assert ##
            fail();
        } catch (IllegalStateException e) {
            // OK
            log(e.getMessage());
        }
    }

    // ===================================================================================
    //                                                                           Irregular
    //                                                                           =========
    public void test_resolveObjectNameIfNeeds_nonTarget() {
        // ## Arrange ##
        DfOutsideSqlNameResolver resolver = new DfOutsideSqlNameResolver();
        String className = "NormalName";
        String fileName = "MemberBhv_selectSimpleMember.sql";

        // ## Act ##
        String actual = resolver.resolveEntityNameIfNeeds(className, fileName);

        // ## Assert ##
        log(actual);
        assertEquals("NormalName", actual);
    }
}
