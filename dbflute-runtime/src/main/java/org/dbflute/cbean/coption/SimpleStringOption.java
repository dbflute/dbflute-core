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
package org.dbflute.cbean.coption;

import java.util.List;

import org.dbflute.cbean.cipher.GearedCipherManager;
import org.dbflute.cbean.coption.parts.SplitOptionParts;
import org.dbflute.cbean.dream.SpecifiedColumn;
import org.dbflute.cbean.sqlclause.query.QueryClauseArranger;
import org.dbflute.dbway.topic.ExtensionOperand;
import org.dbflute.dbway.topic.OnQueryStringConnector;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;

/**
 * The option of simple string, e.g. LikeSearch.
 * @author jflute
 */
public class SimpleStringOption implements ConditionOption {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected SplitOptionParts _splitOptionParts; // null allowed

    // ===================================================================================
    //                                                                               Split
    //                                                                               =====
    protected SimpleStringOption doSplitByBlank() {
        getSplitOptionParts().splitByBlank();
        return this;
    }

    protected SimpleStringOption doSplitBySpace() {
        getSplitOptionParts().splitBySpace();
        return this;
    }

    protected SimpleStringOption doSplitBySpaceContainsDoubleByte() {
        getSplitOptionParts().splitBySpaceContainsDoubleByte();
        return this;
    }

    protected SimpleStringOption doSplitBySpaceContainsDoubleByte(int splitLimitCount) {
        getSplitOptionParts().splitBySpaceContainsDoubleByte(splitLimitCount);
        return this;
    }

    protected SimpleStringOption doSplitByPipeLine() {
        getSplitOptionParts().splitByPipeLine();
        return this;
    }

    protected SimpleStringOption doSplitByVarious(List<String> delimiterList) {
        getSplitOptionParts().splitByVarious(delimiterList);
        return this;
    }

    protected SplitOptionParts getSplitOptionParts() {
        if (_splitOptionParts == null) {
            _splitOptionParts = createSplitOptionParts();
        }
        return _splitOptionParts;
    }

    protected SplitOptionParts createSplitOptionParts() {
        return new SplitOptionParts();
    }

    public boolean isSplit() {
        return getSplitOptionParts().isSplit();
    }

    public String[] generateSplitValueArray(String value) {
        return getSplitOptionParts().generateSplitValueArray(value);
    }

    protected SimpleStringOption doCutSplit(int splitLimitCount) {
        getSplitOptionParts().limitSplit(splitLimitCount);
        return this;
    }

    // ===================================================================================
    //                                                            Interface Implementation
    //                                                            ========================
    // -----------------------------------------------------
    //                                           Rear Option
    //                                           -----------
    public String getRearOption() {
        return ""; // as default (not null)
    }

    // -----------------------------------------------------
    //                                       Compound Column
    //                                       ---------------
    public boolean hasCompoundColumn() {
        return false;
    }

    public List<SpecifiedColumn> getCompoundColumnList() {
        return DfCollectionUtil.emptyList();
    }

    public boolean hasStringConnector() {
        return false;
    }

    public OnQueryStringConnector getStringConnector() {
        return null;
    }

    // -----------------------------------------------------
    //                                     Clause Adjustment
    //                                     -----------------
    public ExtensionOperand getExtensionOperand() {
        return null;
    }

    public QueryClauseArranger getWhereClauseArranger() {
        return null;
    }

    public GearedCipherManager getGearedCipherManager() {
        return null;
    }

    // ===================================================================================
    //                                                                          Real Value
    //                                                                          ==========
    public String generateRealValue(String value) {
        return value; // as default (no filter)
    }

    // ===================================================================================
    //                                                                           Deep Copy
    //                                                                           =========
    public SimpleStringOption createDeepCopy() {
        final SimpleStringOption deepCopy = newDeepCopyInstance();
        if (_splitOptionParts != null) {
            deepCopy._splitOptionParts = (SplitOptionParts) _splitOptionParts;
        }
        return deepCopy;
    }

    protected SimpleStringOption newDeepCopyInstance() {
        return new SimpleStringOption();
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String replace(String text, String fromText, String toText) {
        return Srl.replace(text, fromText, toText);
    }
}
