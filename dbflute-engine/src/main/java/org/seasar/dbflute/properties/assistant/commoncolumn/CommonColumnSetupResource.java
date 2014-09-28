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
package org.seasar.dbflute.properties.assistant.commoncolumn;

import org.seasar.dbflute.util.DfStringUtil;

/**
 * @author jflute
 */
public class CommonColumnSetupResource {

    protected String className;
    protected String propertyName;
    protected String variablePrefix;

    public CommonColumnSetupResource(String variablePrefix) {
        this.variablePrefix = variablePrefix;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getPropertyNameInitCap() {
        return DfStringUtil.initCapTrimmed(propertyName);
    }

    public String getPropertyVariableName() {
        return variablePrefix + propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
}
