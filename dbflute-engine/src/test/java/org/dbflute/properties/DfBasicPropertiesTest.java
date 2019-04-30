/*
 * Copyright 2014-2019 the original author or authors.
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
package org.dbflute.properties;

import java.util.Properties;

import org.dbflute.properties.DfBasicProperties;
import org.dbflute.unit.EngineTestCase;

/**
 * @author jflute
 */
public class DfBasicPropertiesTest extends EngineTestCase {

    public void test_conditionBeanPackage() {
        // ## Arrange ##
        Properties prop = new Properties();
        prop.setProperty("torque.packageBase", "test.base");
        prop.setProperty("torque.conditionBeanPackage", "test.cbean");
        DfBasicProperties packageProperties = new DfBasicProperties(prop);

        // ## Act ##
        String conditionBeanPackage = packageProperties.getConditionBeanPackage();

        // ## Assert ##
        assertEquals("test.base.test.cbean", conditionBeanPackage);
    }

    public void test_extendedConditionBeanPackage_witn_conditionBeanPackage() {
        // ## Arrange ##
        Properties prop = new Properties();
        prop.setProperty("torque.packageBase", "test.base");
        prop.setProperty("torque.conditionBeanPackage", "test.cbean");
        prop.setProperty("torque.extendedConditionBeanPackage", "extended.cbean");
        DfBasicProperties packageProperties = new DfBasicProperties(prop);

        // ## Act ##
        String conditionBeanPackage = packageProperties.getConditionBeanPackage();
        String extendedConditionBeanPackage = packageProperties.getExtendedConditionBeanPackage();

        // ## Assert ##
        assertEquals("test.base.test.cbean", conditionBeanPackage);
        assertEquals("test.base.extended.cbean", extendedConditionBeanPackage);
    }

    public void test_extendedConditionBeanPackage_without_conditionBeanPackage() {
        // ## Arrange ##
        Properties prop = new Properties();
        prop.setProperty("torque.packageBase", "test.base");
        prop.setProperty("torque.extendedConditionBeanPackage", "extended.cbean");
        DfBasicProperties packageProperties = new DfBasicProperties(prop);

        // ## Act ##
        String extendedConditionBeanPackage = packageProperties.getExtendedConditionBeanPackage();

        // ## Assert ##
        assertEquals("test.base.extended.cbean", extendedConditionBeanPackage);
    }

    public void test_extendedConditionBeanPackage_default_same_as_conditionBeanPackage() {
        // ## Arrange ##
        Properties prop = new Properties();
        prop.setProperty("torque.packageBase", "test.base");
        prop.setProperty("torque.conditionBeanPackage", "test.cbean");
        DfBasicProperties packageProperties = new DfBasicProperties(prop);

        // ## Act ##
        String conditionBeanPackage = packageProperties.getConditionBeanPackage();
        String extendedConditionBeanPackage = packageProperties.getExtendedConditionBeanPackage();

        // ## Assert ##
        assertEquals("test.base.test.cbean", conditionBeanPackage);
        assertEquals(conditionBeanPackage, extendedConditionBeanPackage);
    }
}
