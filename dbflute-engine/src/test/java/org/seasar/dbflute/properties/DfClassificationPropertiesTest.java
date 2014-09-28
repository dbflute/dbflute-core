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
package org.seasar.dbflute.properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.seasar.dbflute.properties.assistant.classification.DfClassificationElement;
import org.seasar.dbflute.properties.assistant.classification.DfClassificationTop;

/**
 * @author jflute
 * @since 0.6.2 (2008/01/26 Saturday)
 */
public class DfClassificationPropertiesTest {

    protected static final String ALL_MARK = DfClassificationProperties.MARK_allColumnClassification;

    public void test_hasClassification_SameCase() throws Exception {
        // ## Arrange ##
        final Map<String, Map<String, String>> deploymentMap = new LinkedHashMap<String, Map<String, String>>();
        {
            final Map<String, String> columnClassificationMap = new LinkedHashMap<String, String>();
            columnClassificationMap.put("MEMBER_STATUS_CODE", "MemberStatus");
            deploymentMap.put("MEMBER_STATUS", columnClassificationMap);
        }
        final DfClassificationProperties prop = createClassificationProperties(deploymentMap);

        // ## Act ##
        final boolean actual = prop.hasClassification("MEMBER_STATUS", "MEMBER_STATUS_CODE");

        // ## Assert ##
        Assert.assertTrue(actual);
    }

    public void test_isAllClassificationColumn_ByPinpoint() {
        // ## Arrange ##
        final Map<String, Map<String, String>> deploymentMap = new LinkedHashMap<String, Map<String, String>>();
        {
            final Map<String, String> columnClassificationMap = new LinkedHashMap<String, String>();
            columnClassificationMap.put("ABC", "abc");
            columnClassificationMap.put("DELETE_FLG", "Flg");
            columnClassificationMap.put("DEF", "def");
            deploymentMap.put(ALL_MARK, columnClassificationMap);
        }
        final DfClassificationProperties prop = createClassificationProperties(deploymentMap);

        // ## Act & Assert ##
        assertTrue(prop.isAllClassificationColumn("DELETE_FLG"));
        assertTrue(prop.isAllClassificationColumn("delete_flg"));
        assertFalse(prop.isAllClassificationColumn("VALID_FLG"));
        assertFalse(prop.isAllClassificationColumn("valid_flg"));
    }

    public void test_isAllClassificationColumn_ByHint() {
        // ## Arrange ##
        final Map<String, Map<String, String>> deploymentMap = new LinkedHashMap<String, Map<String, String>>();
        {
            final Map<String, String> columnClassificationMap = new LinkedHashMap<String, String>();
            columnClassificationMap.put("ABC", "abc");
            columnClassificationMap.put("suffix:_FLG", "Flg");
            columnClassificationMap.put("DEF", "def");
            deploymentMap.put(ALL_MARK, columnClassificationMap);
        }
        final DfClassificationProperties prop = createClassificationProperties(deploymentMap);

        // ## Act & Assert ##
        assertTrue(prop.isAllClassificationColumn("DELETE_FLG"));
        assertTrue(prop.isAllClassificationColumn("delete_flg"));
        assertTrue(prop.isAllClassificationColumn("VALID_FLG"));
        assertTrue(prop.isAllClassificationColumn("valid_flg"));
    }

    public void test_getAllClassificationName_ByPinpoint() {
        // ## Arrange ##
        final Map<String, Map<String, String>> deploymentMap = new LinkedHashMap<String, Map<String, String>>();
        {
            final Map<String, String> columnClassificationMap = new LinkedHashMap<String, String>();
            columnClassificationMap.put("ABC", "abc");
            columnClassificationMap.put("DELETE_FLG", "Flg");
            columnClassificationMap.put("DEF", "def");
            deploymentMap.put(ALL_MARK, columnClassificationMap);
        }
        final DfClassificationProperties prop = createClassificationProperties(deploymentMap);

        // ## Act & Assert ##
        assertEquals("Flg", prop.getAllClassificationName("DELETE_FLG"));
        assertEquals("Flg", prop.getAllClassificationName("delete_flg"));
        assertNull(prop.getAllClassificationName("VALID_FLG"));
        assertNull(prop.getAllClassificationName("valid_flg"));
    }

    public void test_getAllClassificationName_ByHint() {
        // ## Arrange ##
        final Map<String, Map<String, String>> deploymentMap = new LinkedHashMap<String, Map<String, String>>();
        {
            final Map<String, String> columnClassificationMap = new LinkedHashMap<String, String>();
            columnClassificationMap.put("ABC", "abc");
            columnClassificationMap.put("suffix:_FLG", "Flg");
            columnClassificationMap.put("DEF", "def");
            deploymentMap.put(ALL_MARK, columnClassificationMap);
        }
        final DfClassificationProperties prop = createClassificationProperties(deploymentMap);

        // ## Act & Assert ##
        assertEquals("Flg", prop.getAllClassificationName("DELETE_FLG"));
        assertEquals("Flg", prop.getAllClassificationName("delete_flg"));
        assertEquals("Flg", prop.getAllClassificationName("VALID_FLG"));
        assertEquals("Flg", prop.getAllClassificationName("valid_flg"));
    }

    public void test_isElementMapClassificationMeta() {
        // ## Arrange ##
        final Map<String, Map<String, String>> deploymentMap = new LinkedHashMap<String, Map<String, String>>();
        final DfClassificationProperties prop = createClassificationProperties(deploymentMap);
        final Map<String, String> elementMap = new HashMap<String, String>();

        // ## Act & Assert ##
        assertFalse(prop.isElementMapClassificationTop(elementMap));
        elementMap.put(DfClassificationTop.KEY_CODE_TYPE, "foo");
        assertFalse(prop.isElementMapClassificationTop(elementMap));
        elementMap.put(DfClassificationTop.KEY_TOP_COMMENT, "bar");
        assertTrue(prop.isElementMapClassificationTop(elementMap));
        elementMap.put(DfClassificationTop.KEY_CODE_TYPE, null);
        assertTrue(prop.isElementMapClassificationTop(elementMap));
        elementMap.put(DfClassificationTop.KEY_TOP_COMMENT, null);
        assertFalse(prop.isElementMapClassificationTop(elementMap));
        elementMap.put(DfClassificationElement.KEY_CODE, "foo");
        assertFalse(prop.isElementMapClassificationTop(elementMap));
        elementMap.put(DfClassificationTop.KEY_TOP_COMMENT, "foo");
        assertTrue(prop.isElementMapClassificationTop(elementMap));
    }

    public void test_isTableClassificationSuppressAutoDeploy() {
        // ## Arrange ##
        final Map<String, Map<String, String>> deploymentMap = new LinkedHashMap<String, Map<String, String>>();
        final DfClassificationProperties prop = createClassificationProperties(deploymentMap);
        final Map<String, String> elementMap = new HashMap<String, String>();

        // ## Act & Assert ##
        assertFalse(prop.isElementMapSuppressAutoDeploy(elementMap));
        elementMap.put("suppressAutoDeploy", "false");
        assertFalse(prop.isElementMapSuppressAutoDeploy(elementMap));
        elementMap.put("suppressAutoDeploy", "true");
        assertTrue(prop.isElementMapSuppressAutoDeploy(elementMap));
        elementMap.put("suppressAutoDeploy", "True");
        assertTrue(prop.isElementMapSuppressAutoDeploy(elementMap));
    }

    protected DfClassificationProperties createClassificationProperties(Map<String, Map<String, String>> deploymentMap) {
        final DfClassificationProperties prop = new DfClassificationProperties(new Properties());
        prop._classificationDeploymentMap = deploymentMap;
        return prop;
    }
}
