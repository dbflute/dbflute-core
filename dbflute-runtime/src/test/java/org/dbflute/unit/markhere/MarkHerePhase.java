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
package org.dbflute.unit.markhere;

/**
 * @author jflute
 * @since 0.4.0 (2014/03/16 Sunday)
 */
public class MarkHerePhase {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _mark;
    protected final Integer _phaseNumber;
    protected int _markedCount;
    protected boolean _asserted;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public MarkHerePhase(String mark, Integer phaseNumber) {
        _mark = mark;
        _phaseNumber = phaseNumber;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("phase:{");
        sb.append(_phaseNumber);
        sb.append(", marked=").append(_markedCount);
        sb.append(", asserted=").append(_asserted);
        sb.append("}");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getMark() {
        return _mark;
    }

    public int getMarkedCount() {
        return _markedCount;
    }

    public void incrementMarkedCount() {
        _markedCount++;
    }

    public boolean isAsserted() {
        return _asserted;
    }

    public void finishAssert() {
        _asserted = true;
    }
}
