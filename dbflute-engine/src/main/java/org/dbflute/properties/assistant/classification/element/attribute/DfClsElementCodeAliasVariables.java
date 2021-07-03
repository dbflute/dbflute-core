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
package org.dbflute.properties.assistant.classification.element.attribute;

/**
 * @author jflute
 * @since 1.2.5 split from DfClassificationElement (2021/07/03 Saturday at roppongi japanese)
 */
public class DfClsElementCodeAliasVariables {

    protected final String _code; // not null, empty allowed
    protected final String _alias; // null allowed, empty allowed
    protected final String[] _sisters; // null allowed, empty allowed

    public DfClsElementCodeAliasVariables(String code, String alias, String[] sisters) {
        _code = code;
        _alias = alias;
        _sisters = sisters;
    }

    public String buildCodeAliasVariables() {
        final StringBuilder sb = new StringBuilder();
        final String code = _code;
        final String alias = _alias;
        sb.append("\"").append(code).append("\"");
        if (alias != null && alias.trim().length() > 0) {
            sb.append(", \"").append(alias).append("\"");
        } else {
            sb.append(", null");
        }
        return sb.toString();
    }

    public String buildCodeAliasSisterCodeVariables() {
        final StringBuilder sb = new StringBuilder();
        sb.append(buildCodeAliasVariables());
        final String[] sisters = _sisters;
        sb.append(", ");
        if (sisters != null && sisters.length > 0) {
            sb.append("new String[] {");
            if (sisters != null && sisters.length > 0) {
                int index = 0;
                for (String sister : sisters) {
                    if (index > 0) {
                        sb.append(", ");
                    }
                    sb.append("\"").append(sister).append("\"");
                    ++index;
                }
            }
            sb.append("}");
        } else {
            sb.append("emptyStrings()"); // changed from EMPTY_SISTERS since 1.1.2
        }
        return sb.toString();
    }
}
