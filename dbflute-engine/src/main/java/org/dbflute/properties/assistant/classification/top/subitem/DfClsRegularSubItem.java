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
package org.dbflute.properties.assistant.classification.top.subitem;

/**
 * @author jflute
 * @since 1.2.5 split from DfClassificationTop (2021/07/03 Saturday at roppongi japanese)
 */
public class DfClsRegularSubItem { // directly used in templates

    // Object or String only supported
    public static final String TYPE_OBJECT = "Object";
    public static final String TYPE_STRING = "String";

    protected final String _subItemName;
    protected final String _subItemType;

    public DfClsRegularSubItem(String subItemName, String subItemType) {
        _subItemName = subItemName;
        _subItemType = subItemType;
    }

    public boolean isSubItemTypeObject() {
        return _subItemType.equals(TYPE_OBJECT);
    }

    public boolean isSubItemTypeString() {
        return _subItemType.equals(TYPE_STRING);
    }

    public String getSubItemName() {
        return _subItemName;
    }

    public String getSubItemType() {
        return _subItemType;
    }
}
