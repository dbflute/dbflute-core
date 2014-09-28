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
package org.seasar.dbflute.jdbc;

import org.seasar.dbflute.util.DfTraceViewUtil;

/**
 * The information of execution time.
 * @author jflute
 */
public class ExecutionTimeInfo {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Long _commandBeforeTimeMillis;
    protected final Long _commandAfterTimeMillis;
    protected final Long _sqlBeforeTimeMillis;
    protected final Long _sqlAfterTimeMillis;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ExecutionTimeInfo(Long commandBeforeTimeMillis, Long commandAfterTimeMillis, Long sqlBeforeTimeMillis,
            Long sqlAfterTimeMillis) {
        _commandBeforeTimeMillis = commandBeforeTimeMillis;
        _commandAfterTimeMillis = commandAfterTimeMillis;
        _sqlBeforeTimeMillis = sqlBeforeTimeMillis;
        _sqlAfterTimeMillis = sqlAfterTimeMillis;
    }

    // ===================================================================================
    //                                                                                View
    //                                                                                ====
    /**
     * The performance view of command invoking. e.g. 01m40s012ms <br />
     * @return The view string of command invoking. (NotNull: if command failure, in SqlFireHook, and so on..., returns "*No time") 
     */
    public String toCommandPerformanceView() {
        if (hasCommandTimeMillis()) {
            return convertToPerformanceView(_commandAfterTimeMillis - _commandBeforeTimeMillis);
        } else {
            return "*No time";
        }
    }

    /**
     * The performance view of SQL fire. e.g. 01m40s012ms <br />
     * (before building SQL clause after mapping to entity). <br />
     * When batch execution, all statements is contained to the time.
     * @return The view string of SQL fire. (NotNull: if no-modified-column update, SQL failure, and so on..., returns "*No time")
     */
    public String toSqlPerformanceView() {
        if (hasSqlTimeMillis()) {
            return convertToPerformanceView(_sqlAfterTimeMillis - _sqlBeforeTimeMillis);
        } else {
            return "*No time";
        }
    }

    /**
     * Convert to performance view.
     * @param after_minus_before The difference between before time and after time.
     * @return The view string to show performance. e.g. 01m40s012ms (NotNull)
     */
    protected String convertToPerformanceView(long after_minus_before) {
        return DfTraceViewUtil.convertToPerformanceView(after_minus_before);
    }

    // ===================================================================================
    //                                                                              Status
    //                                                                              ======
    /**
     * Does it have the time of behavior command. <br />
     * @return The determination, true or false. (basically true but no guarantee)
     */
    public boolean hasCommandTimeMillis() {
        return _commandAfterTimeMillis != null && _commandBeforeTimeMillis != null;
    }

    /**
     * Does it have the time of SQL fire. <br />
     * Basically it returns true but no guarantee, because this is additional info. <br />
     * For example, no-modified-column update execution does not have its SQL fire.
     * @return The determination, true or false. (basically true but no guarantee)
     */
    public boolean hasSqlTimeMillis() {
        return _sqlAfterTimeMillis != null && _sqlBeforeTimeMillis != null;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("commandBefore=").append(_commandBeforeTimeMillis);
        sb.append(", commandAfter=").append(_commandAfterTimeMillis);
        sb.append(", sqlBefore=").append(_sqlBeforeTimeMillis);
        sb.append(", sqlAfter=").append(_sqlAfterTimeMillis);
        sb.append("}");
        return sb.toString();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    /**
     * Get the time as millisecond before command invoking (before building SQL clause).
     * @return The long value of millisecond. (NullAllowed: when command failure, in SqlFireHook, and so on...)
     */
    public Long getCommandBeforeTimeMillis() {
        return _commandBeforeTimeMillis;
    }

    /**
     * Get the time as millisecond after command invoking (after mapping to entity).
     * @return The long value of millisecond. (NullAllowed: when command failure, in SqlFireHook, and so on...)
     */
    public Long getCommandAfterTimeMillis() {
        return _commandAfterTimeMillis;
    }

    /**
     * Get the time as millisecond before SQL fire (after building SQL clause). <br />
     * (before building SQL clause after mapping to entity). <br />
     * When batch execution, all statements is contained to the time.
     * @return The long value of millisecond. (NullAllowed: when no-modified-column update, SQL failure, and so on...)
     */
    public Long getSqlBeforeTimeMillis() {
        return _sqlBeforeTimeMillis;
    }

    /**
     * Get the time as millisecond after SQL fire (before mapping to entity). <br />
     * (before building SQL clause after mapping to entity). <br />
     * When batch execution, all statements is contained to the time.
     * @return The long value of millisecond. (NullAllowed: when no-modified-column update, SQL failure, and so on...)
     */
    public Long getSqlAfterTimeMillis() {
        return _sqlAfterTimeMillis;
    }
}
