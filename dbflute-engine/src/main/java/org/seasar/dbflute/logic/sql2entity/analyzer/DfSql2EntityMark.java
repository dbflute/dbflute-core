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
package org.seasar.dbflute.logic.sql2entity.analyzer;

/**
 * 
 * @author jflute
 * @since 0.9.8.4 (2011/05/27 Friday)
 */
public class DfSql2EntityMark {

    protected String _content;
    protected String _comment;

    public String getContent() {
        return _content;
    }

    public void setContent(String content) {
        this._content = content;
    }

    public String getComment() {
        return _comment;
    }

    public void setComment(String comment) {
        this._comment = comment;
    }
}
