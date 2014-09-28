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
package org.seasar.dbflute.util;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 * @since 0.9.6.7 (2010/03/30 Tuesday)
 */
public class DfReflectionUtilTest extends PlainTestCase {

    // ===================================================================================
    //                                                                              Method
    //                                                                              ======
    public void test_getPublicMethod_basic() throws Exception {
        // ## Arrange ##
        String methodName = "fooNoArg";

        // ## Act ##
        Method method = DfReflectionUtil.getPublicMethod(FooTarget.class, methodName, null);

        // ## Assert ##
        assertNotNull(method);
        assertEquals(methodName, method.getName());
    }

    public void test_getPublicMethod_args() throws Exception {
        // ## Arrange ##
        String methodName = "fooDateArg";

        // ## Act ##
        Method method = DfReflectionUtil.getPublicMethod(FooTarget.class, methodName, new Class<?>[] { Date.class });

        // ## Assert ##
        assertNotNull(method);
        assertEquals(methodName, method.getName());
    }

    public void test_getPublicMethod_args_SubClass() throws Exception {
        // ## Arrange ##
        Class<?> clazz = FooTarget.class;
        String methodName = "fooDateArg";
        Class<?>[] argType = new Class<?>[] { Timestamp.class };

        // ## Act & Assert ##
        assertNull(DfReflectionUtil.getPublicMethod(clazz, methodName, argType));
        assertNotNull(DfReflectionUtil.getPublicMethodFlexibly(clazz, methodName, argType));
    }

    public void test_getPublicMethod_args_SuperClass() throws Exception {
        // ## Arrange ##
        Class<?> clazz = FooTarget.class;
        String methodName = "fooTimestampArg";
        Class<?>[] argType = new Class<?>[] { Date.class };

        // ## Act & Assert ##
        assertNull(DfReflectionUtil.getPublicMethod(clazz, methodName, argType));
        assertNull(DfReflectionUtil.getPublicMethodFlexibly(clazz, methodName, argType));
    }

    public void test_getPublicMethod_args_Interface_exists() throws Exception {
        // ## Arrange ##
        Class<?> clazz = FilenameFilter.class;
        String methodName = "accept";
        Class<?>[] argType = new Class<?>[] { File.class, String.class };

        // ## Act & Assert ##
        assertNotNull(DfReflectionUtil.getPublicMethod(clazz, methodName, argType));
        assertNotNull(DfReflectionUtil.getPublicMethodFlexibly(clazz, methodName, argType));
    }

    public void test_getPublicMethod_args_Interface_notExists() throws Exception {
        // ## Arrange ##
        Class<?> clazz = FilenameFilter.class;
        String methodName = "noexist"; // expect no exception
        Class<?>[] argType = new Class<?>[] { File.class, String.class };

        // ## Act & Assert ##
        assertNull(DfReflectionUtil.getPublicMethod(clazz, methodName, argType));
        assertNull(DfReflectionUtil.getPublicMethodFlexibly(clazz, methodName, argType));
    }

    public void test_invoke_basic() throws Exception {
        // ## Arrange ##
        String methodName = "fooNoArg";
        Method method = DfReflectionUtil.getPublicMethod(FooTarget.class, methodName, null);

        // ## Act ##
        Object result = DfReflectionUtil.invoke(method, new FooTarget(), null);

        // ## Assert ##
        assertEquals("foo", result);
    }

    public static class FooTarget {
        public String fooNoArg() {
            return "foo";
        }

        public String fooDateArg(Date date) {
            return "foo";
        }

        public String fooTimestampArg(Timestamp timestamp) {
            return "foo";
        }
    }

    // ===================================================================================
    //                                                                             Generic
    //                                                                             =======
    public void test_getElementType_List() throws Exception {
        // ## Arrange ##
        Type genericType = getListMethod().getGenericReturnType();

        // ## Act ##
        Class<?> elementType = DfReflectionUtil.getGenericType(genericType);

        // ## Assert ##
        log("genericType = " + genericType);
        log("elementType = " + elementType);
        assertEquals(String.class, elementType);
    }

    public void test_getElementType_Set() throws Exception {
        // ## Arrange ##
        Type genericType = getSetMethod().getGenericReturnType();

        // ## Act ##
        Class<?> elementType = DfReflectionUtil.getGenericType(genericType);

        // ## Assert ##
        log("genericType = " + genericType);
        log("elementType = " + elementType);
        assertEquals(String.class, elementType);
    }

    public void test_getElementType_Collection() throws Exception {
        // ## Arrange ##
        Type genericType = getCollectionMethod().getGenericReturnType();

        // ## Act ##
        Class<?> elementType = DfReflectionUtil.getGenericType(genericType);

        // ## Assert ##
        log("genericType = " + genericType);
        log("elementType = " + elementType);
        assertEquals(String.class, elementType);
    }

    public void test_getElementType_nestedList() throws Exception {
        // ## Arrange ##
        Type genericType = getNestedListMethod().getGenericReturnType();

        // ## Act ##
        Class<?> elementType = DfReflectionUtil.getGenericType(genericType);

        // ## Assert ##
        log("genericType = " + genericType);
        log("elementType = " + elementType);
        assertEquals(List.class, elementType);
    }

    public void test_getElementType_beanList() throws Exception {
        // ## Arrange ##
        Type genericType = getBeanListMethod().getGenericReturnType();

        // ## Act ##
        Class<?> elementType = DfReflectionUtil.getGenericType(genericType);

        // ## Assert ##
        log("genericType = " + genericType);
        log("elementType = " + elementType);
        assertEquals(FooGeneric.class, elementType);
    }

    public void test_getElementType_nonGeneric() throws Exception {
        // ## Arrange ##
        Type genericType = getNonGenericMethod().getGenericReturnType();

        // ## Act ##
        Class<?> elementType = DfReflectionUtil.getGenericType(genericType);

        // ## Assert ##
        log("genericType = " + genericType);
        log("elementType = " + elementType);
        assertNull(elementType);
    }

    protected Method getListMethod() throws Exception {
        return FooGeneric.class.getMethod("fooList", (Class<?>[]) null);
    }

    protected Method getSetMethod() throws Exception {
        return FooGeneric.class.getMethod("fooSet", (Class<?>[]) null);
    }

    protected Method getCollectionMethod() throws Exception {
        return FooGeneric.class.getMethod("fooCollection", (Class<?>[]) null);
    }

    protected Method getNestedListMethod() throws Exception {
        return FooGeneric.class.getMethod("fooNestedList", (Class<?>[]) null);
    }

    protected Method getBeanListMethod() throws Exception {
        return BarGeneric.class.getMethod("barBeanList", (Class<?>[]) null);
    }

    protected Method getNonGenericMethod() throws Exception {
        return FooGeneric.class.getMethod("fooNonGeneric", (Class<?>[]) null);
    }

    public static class FooGeneric {
        public List<String> fooList() {
            return new ArrayList<String>();
        }

        public Set<String> fooSet() {
            return new HashSet<String>();
        }

        public Collection<String> fooCollection() {
            return new ArrayList<String>();
        }

        public List<List<String>> fooNestedList() {
            return new ArrayList<List<String>>();
        }

        public String fooNonGeneric() {
            return "foo";
        }
    }

    public static class BarGeneric {
        public List<FooGeneric> barBeanList() {
            return new ArrayList<FooGeneric>();
        }
    }
}
