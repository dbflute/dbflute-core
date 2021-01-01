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
package org.dbflute.logic.doc.arrqy;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jflute
 * @since 1.1.9 (2018/12/31 Monday)
 */
public class DfArrangeQueryTable {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _tableDbName;
    protected final String _beanClassName;
    protected final String _queryClassName;
    protected final List<DfArrangeQueryMethod> _beanMethodList = new ArrayList<DfArrangeQueryMethod>();
    protected final List<DfArrangeQueryMethod> _queryMethodList = new ArrayList<DfArrangeQueryMethod>();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfArrangeQueryTable(String tableDbName, String beanClassName, String queryClassName) {
        _tableDbName = tableDbName;
        _beanClassName = beanClassName;
        _queryClassName = queryClassName;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getTableDbName() {
        return _tableDbName;
    }

    public String getBeanClassName() {
        return _beanClassName;
    }

    public String getQueryClassName() {
        return _queryClassName;
    }

    public void addBeanMethod(DfArrangeQueryMethod meta) {
        _beanMethodList.add(meta);
    }

    public List<DfArrangeQueryMethod> getBeanMethodList() {
        return _beanMethodList;
    }

    public void addQueryMethod(DfArrangeQueryMethod meta) {
        _queryMethodList.add(meta);
    }

    public List<DfArrangeQueryMethod> getQueryMethodList() {
        return _queryMethodList;
    }
}