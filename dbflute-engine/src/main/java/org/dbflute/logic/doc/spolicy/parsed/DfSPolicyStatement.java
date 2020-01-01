/*
 * Copyright 2014-2020 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author jflute
 * @since 1.1.2 (2016/12/31 Saturday)
 */
public class DfSPolicyStatement {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _nativeExp;
    protected final DfSPolicyIfClause _ifClause;
    protected final DfSPolicyThenClause _thenClause;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfSPolicyStatement(String nativeExp, DfSPolicyIfClause ifClause, DfSPolicyThenClause thenClause) {
        _nativeExp = nativeExp;
        _ifClause = ifClause;
        _thenClause = thenClause;
    }

    // ===================================================================================
    //                                                                           If Clause
    //                                                                           =========
    public static class DfSPolicyIfClause {

        protected final List<DfSPolicyIfPart> _ifPartList; // at least one element
        protected final boolean _connectedByOr;

        public DfSPolicyIfClause(List<DfSPolicyIfPart> ifPartList, boolean connectedByOr) {
            _ifPartList = ifPartList;
            _connectedByOr = connectedByOr;
        }

        public boolean evaluate(Predicate<DfSPolicyIfPart> ifPartDeterminer) {
            boolean condition = false;
            for (DfSPolicyIfPart ifPart : _ifPartList) { // at least one loop
                if (_connectedByOr) {
                    if (ifPartDeterminer.test(ifPart)) {
                        condition = true;
                        break;
                    } else {
                        condition = false;
                    }
                } else { // and
                    if (ifPartDeterminer.test(ifPart)) {
                        condition = true;
                    } else {
                        condition = false;
                        break;
                    }
                }
            }
            return condition;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("{");
            final StringBuilder partSb = new StringBuilder();
            for (DfSPolicyIfPart ifPart : _ifPartList) {
                if (partSb.length() > 0) {
                    partSb.append(_connectedByOr ? " or " : " and ");
                }
                partSb.append(ifPart);
            }
            sb.append(partSb);
            sb.append("}");
            return sb.toString();
        }

        public List<DfSPolicyIfPart> getIfPartList() {
            return _ifPartList;
        }

        public boolean isConnectedByOr() {
            return _connectedByOr;
        }
    }

    public static class DfSPolicyIfPart {

        protected final String _ifItem;
        protected final String _ifValue;
        protected final boolean _notIfValue;

        public DfSPolicyIfPart(String ifItem, String ifValue, boolean notIfValue) {
            _ifItem = ifItem;
            _ifValue = ifValue;
            _notIfValue = notIfValue;
        }

        @Override
        public String toString() {
            return "if:{" + _ifItem + " is " + (_notIfValue ? "not " : "") + _ifValue + "}";
        }

        public String getIfItem() {
            return _ifItem;
        }

        public String getIfValue() {
            return _ifValue;
        }

        public boolean isNotIfValue() {
            return _notIfValue;
        }
    }

    // ===================================================================================
    //                                                                         Then Clause
    //                                                                         ===========
    public static class DfSPolicyThenClause {

        protected final String _thenTheme; // null allowed
        protected final boolean _notThenTheme;
        protected final List<DfSPolicyThenPart> _thenPartList; // not null, at least one element if non-theme
        protected final boolean _connectedByOr;
        protected final String _supplement; // for "=> {}", null allowed

        public DfSPolicyThenClause(String thenTheme, boolean notThenTheme, List<DfSPolicyThenPart> thenPartList, boolean connectedByOr,
                String supplement) {
            _thenTheme = thenTheme;
            _notThenTheme = notThenTheme;
            _thenPartList = thenPartList;
            _connectedByOr = connectedByOr;
            _supplement = supplement;
        }

        public List<String> evaluate(Function<DfSPolicyThenPart, String> thenPartEvaluator) {
            final List<String> violationList = new ArrayList<String>();
            for (DfSPolicyThenPart thenPart : _thenPartList) { // at least one loop
                final String violation = thenPartEvaluator.apply(thenPart);
                if (_connectedByOr) {
                    if (violation != null) {
                        violationList.add(violation);
                    } else {
                        violationList.clear(); // next condition may be true
                        break;
                    }
                } else { // and
                    if (violation != null) {
                        violationList.add(violation);
                        break;
                    }
                }
            }
            return violationList;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("{");
            if (_thenTheme != null) {
                sb.append("then ").append(_notThenTheme ? "not " : "").append(_thenTheme);
            } else {
                final StringBuilder partSb = new StringBuilder();
                for (DfSPolicyThenPart thenPart : _thenPartList) {
                    if (partSb.length() > 0) {
                        partSb.append(_connectedByOr ? " or " : " and ");
                    }
                    partSb.append(thenPart);
                }
                sb.append(partSb);
            }
            if (_supplement != null) {
                sb.append(" => ").append(_supplement);
            }
            sb.append("}");
            return sb.toString();
        }

        public String getThenTheme() {
            return _thenTheme;
        }

        public boolean isNotThenTheme() {
            return _notThenTheme;
        }

        public List<DfSPolicyThenPart> getThenPartList() {
            return _thenPartList;
        }

        public boolean isConnectedByOr() {
            return _connectedByOr;
        }

        public String getSupplement() {
            return _supplement;
        }
    }

    public static class DfSPolicyThenPart {

        protected final String _thenItem;
        protected final String _thenValue;
        protected final boolean _notThenValue;

        public DfSPolicyThenPart(String thenItem, String thenValue, boolean notThenValue) {
            _thenItem = thenItem;
            _thenValue = thenValue;
            _notThenValue = notThenValue;
        }

        @Override
        public String toString() {
            return "then:{" + _thenItem + " is " + (_notThenValue ? "not " : "") + _thenValue + "}";
        }

        public String getThenItem() {
            return _thenItem;
        }

        public String getThenValue() {
            return _thenValue;
        }

        public boolean isNotThenValue() {
            return _notThenValue;
        }
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "statement:{" + _nativeExp + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getNativeExp() {
        return _nativeExp;
    }

    public DfSPolicyIfClause getIfClause() {
        return _ifClause;
    }

    public DfSPolicyThenClause getThenClause() {
        return _thenClause;
    }
}
