/*
 * Copyright 2014-2023 the original author or authors.
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
package org.dbflute.twowaysql.node;

/**
 * @author jflute
 * @since 1.1.0 (2014/09/29 Monday)
 */
public enum ParameterCommentType {

    BIND("bind variable comment", "Bind Variable Comment") // bind
    , EMBEDDED("embedded variable comment", "Embedded Variable Comment") // embedded
    , FORCOMMENT("FOR comment", "FOR COMMENT") // for comment
    ;
    private String _textName;
    private String _titleName;

    private ParameterCommentType(String commentName, String titleName) {
        _textName = commentName;
        _titleName = titleName;
    }

    public String textName() {
        return _textName;
    }

    public String titleName() {
        return _titleName;
    }
}