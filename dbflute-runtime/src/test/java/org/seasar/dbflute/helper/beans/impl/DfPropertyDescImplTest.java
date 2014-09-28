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
package org.seasar.dbflute.helper.beans.impl;

import org.seasar.dbflute.helper.beans.DfBeanDesc;
import org.seasar.dbflute.helper.beans.DfPropertyDesc;
import org.seasar.dbflute.helper.beans.exception.DfBeanIllegalPropertyException;
import org.seasar.dbflute.helper.beans.factory.DfBeanDescFactory;
import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 */
public class DfPropertyDescImplTest extends PlainTestCase {

    public void test_getValue_illegalProperty() throws Exception {
        // ## Arrange ##
        DfBeanDesc beanDesc = DfBeanDescFactory.getBeanDesc(MockBean.class);
        DfPropertyDesc pd = beanDesc.getPropertyDesc("writeOnlyName");
        MockBean bean = new MockBean();
        bean.setWriteOnlyName("foo");

        // ## Act ##
        try {
            pd.getValue(bean);

            // ## Assert ##
            fail();
        } catch (DfBeanIllegalPropertyException e) {
            // OK
            log(e.getMessage());
        }
    }

    public void test_setValue_illegalProperty() throws Exception {
        // ## Arrange ##
        DfBeanDesc beanDesc = DfBeanDescFactory.getBeanDesc(MockBean.class);
        DfPropertyDesc pd = beanDesc.getPropertyDesc("readOnlyName");
        MockBean bean = new MockBean();

        // ## Act ##
        try {
            pd.setValue(bean, "foo");

            // ## Assert ##
            fail();
        } catch (DfBeanIllegalPropertyException e) {
            // OK
            log(e.getMessage());
        }
    }

    protected static class MockBean {
        protected String _readOnlyName;
        protected String _writeOnlyName;

        public String getReadOnlyName() {
            return _readOnlyName;
        }

        public void setWriteOnlyName(String writeOnlyName) {
            this._writeOnlyName = writeOnlyName;
        }
    }
}
