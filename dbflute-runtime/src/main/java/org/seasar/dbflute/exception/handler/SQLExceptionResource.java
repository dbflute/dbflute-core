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
package org.seasar.dbflute.exception.handler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.seasar.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 * @since 1.0.1 (2012/12/16 Sunday)
 */
public class SQLExceptionResource {

    protected final List<String> _noticeList = new ArrayList<String>(2);
    protected final Map<String, List<Object>> _resourceMap = new LinkedHashMap<String, List<Object>>(4);
    protected String _executedSql;
    protected String _displaySql;
    protected boolean _uniqueConstraintHandling;
    protected boolean _displaySqlPartHandling;

    public List<String> getNoticeList() {
        return _noticeList;
    }

    public void setNotice(String notice) {
        _noticeList.add(notice);
    }

    public void addResource(String item, Object... elements) {
        _resourceMap.put(item, DfCollectionUtil.newArrayList(elements));
    }

    public Map<String, List<Object>> getResourceMap() {
        return _resourceMap;
    }

    public String getExecutedSql() {
        return _executedSql;
    }

    public void setExecutedSql(String executedSql) {
        _executedSql = executedSql;
    }

    public String getDisplaySql() {
        return _displaySql;
    }

    public void setDisplaySql(String displaySql) {
        _displaySql = displaySql;
    }

    public boolean isUniqueConstraintHandling() {
        return _uniqueConstraintHandling;
    }

    public void enableUniqueConstraintHandling() {
        _uniqueConstraintHandling = true;
    }

    public boolean isDisplaySqlPartHandling() {
        return _displaySqlPartHandling;
    }

    public void enableDisplaySqlPartHandling() {
        _displaySqlPartHandling = true;
    }
}
