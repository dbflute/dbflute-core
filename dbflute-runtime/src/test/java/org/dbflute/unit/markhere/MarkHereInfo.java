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
package org.dbflute.unit.markhere;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author jflute
 * @since 0.4.0 (2014/03/16 Sunday)
 */
public class MarkHereInfo {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _mark;
    protected Integer _currentPhaseNumber = 1;
    protected final Map<Integer, MarkHerePhase> _markedPhaseMap = new LinkedHashMap<Integer, MarkHerePhase>();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public MarkHereInfo(String mark) {
        _mark = mark;
    }

    // ===================================================================================
    //                                                                      Phase Handling
    //                                                                      ==============
    public void markPhase() {
        MarkHerePhase phase = getCurrentPhase();
        if (phase == null) {
            phase = createPhase(_mark, _currentPhaseNumber);
            _markedPhaseMap.put(_currentPhaseNumber, phase);
        }
        phase.incrementMarkedCount();
    }

    protected MarkHerePhase createPhase(String mark, Integer phaseNumber) {
        return new MarkHerePhase(mark, phaseNumber);
    }

    public boolean isNonAssertedPhase() {
        final MarkHerePhase phase = getCurrentPhase();
        return phase != null && !phase.isAsserted();
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("mark").append(":{");
        sb.append(_mark);
        final int phaseSize = _markedPhaseMap.size();
        sb.append(", has ").append(phaseSize).append(" ").append(phaseSize > 1 ? "phases" : "phase ");
        final MarkHerePhase phase = getCurrentPhase();
        sb.append(", current=").append(phase).append(phase == null ? ":(no mark)" : "");
        sb.append("}");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public String getMark() {
        return _mark;
    }

    public Integer getCurrentPhaseNumber() {
        return _currentPhaseNumber;
    }

    public void finishPhase() {
        ++_currentPhaseNumber;
    }

    public MarkHerePhase getCurrentPhase() {
        return _markedPhaseMap.get(_currentPhaseNumber);
    }
}
