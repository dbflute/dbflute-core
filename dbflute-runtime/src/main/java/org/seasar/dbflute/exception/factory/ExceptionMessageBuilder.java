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
package org.seasar.dbflute.exception.factory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.seasar.dbflute.resource.DBFluteSystem;

/**
 * @author jflute
 * @since 0.9.6.9 (2010/05/01 Saturday)
 */
public class ExceptionMessageBuilder {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final List<String> _noticeList = new ArrayList<String>(2);
    protected final Map<String, List<Object>> _elementMap = new LinkedHashMap<String, List<Object>>(8);
    protected List<Object> _currentList;

    // ===================================================================================
    //                                                                                 Add
    //                                                                                 ===
    public void addNotice(String notice) {
        _noticeList.add(notice);
    }

    public ExceptionMessageBuilder addItem(String item) {
        _currentList = new ArrayList<Object>(4);
        _elementMap.put(item, _currentList);
        return this;
    }

    public ExceptionMessageBuilder addElement(Object element) {
        if (_currentList == null) {
            addItem("*No Title");
        }
        _currentList.add(element);
        return this;
    }

    // ===================================================================================
    //                                                                               Build
    //                                                                               =====
    public String buildExceptionMessage() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Look! Read the message below.").append(ln());
        sb.append("/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *").append(ln());
        if (!_noticeList.isEmpty()) {
            for (String notice : _noticeList) {
                sb.append(notice).append(ln());
            }
        } else {
            sb.append("*No Notice").append(ln());
        }
        final Set<Entry<String, List<Object>>> entrySet = _elementMap.entrySet();
        for (Entry<String, List<Object>> entry : entrySet) {
            final String item = entry.getKey();
            sb.append(ln());
            sb.append("[").append(item).append("]").append(ln());
            final List<Object> elementList = entry.getValue();
            for (Object element : elementList) {
                sb.append(element).append(ln());
            }
        }
        sb.append("* * * * * * * * * */");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return DBFluteSystem.getBasicLn();
    }
}
