/*
 * Copyright 2014-2024 the original author or authors.
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
package org.dbflute.logic.doc.spolicy.parsed;

import java.util.List;

/**
 * @author jflute
 * @since 1.1.2 (2017/1/03 Tuesday)
 */
public class DfSPolicyParsedPolicy { // also used by Velocity template (e.g. SchemaHTML)

    protected final DfSPolicyParsedPolicyPart _wholePolicyPart;
    protected final DfSPolicyParsedPolicyPart _tablePolicyPart;
    protected final DfSPolicyParsedPolicyPart _columnPolicyPart;

    public DfSPolicyParsedPolicy(DfSPolicyParsedPolicyPart wholePolicyPart, DfSPolicyParsedPolicyPart tablePolicyPart,
            DfSPolicyParsedPolicyPart columnPolicyPart) {
        _wholePolicyPart = wholePolicyPart;
        _tablePolicyPart = tablePolicyPart;
        _columnPolicyPart = columnPolicyPart;
    }

    public static class DfSPolicyParsedPolicyPart {

        protected final List<String> _themeList;
        protected final List<DfSPolicyStatement> _statementList;

        public DfSPolicyParsedPolicyPart(List<String> themeList, List<DfSPolicyStatement> statementList) {
            _themeList = themeList;
            _statementList = statementList;
        }

        @Override
        public String toString() {
            return "policyPart:{" + _themeList + ", " + _statementList + "}";
        }

        public List<String> getThemeList() {
            return _themeList;
        }

        public List<DfSPolicyStatement> getStatementList() {
            return _statementList;
        }
    }

    @Override
    public String toString() {
        return "parsedPolicy:{" + _wholePolicyPart + ", " + _tablePolicyPart + ", " + _columnPolicyPart + "}";
    }

    public DfSPolicyParsedPolicyPart getWholePolicyPart() {
        return _wholePolicyPart;
    }

    public DfSPolicyParsedPolicyPart getTablePolicyPart() {
        return _tablePolicyPart;
    }

    public DfSPolicyParsedPolicyPart getColumnPolicyPart() {
        return _columnPolicyPart;
    }
}
