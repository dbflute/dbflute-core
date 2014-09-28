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
package org.seasar.dbflute.bhv.logging.result;

import java.util.List;

import org.seasar.dbflute.Entity;
import org.seasar.dbflute.util.DfTraceViewUtil;

/**
 * @author jflute
 * @since 1.0.4D (2013/06/30 Sunday)
 */
public class BehaviorResultBuilder {

    public <RESULT> String buildResultExp(Class<?> retType, Object ret, long before, long after) {
        try {
            return doBuildResultExp(retType, ret, before, after);
        } catch (RuntimeException e) {
            String msg = "Failed to build result expression for logging: retType=" + retType + " ret=" + ret;
            throw new IllegalStateException(msg, e);
        }
    }

    protected String doBuildResultExp(Class<?> retType, Object ret, long before, long after) {
        final String resultExp;
        final String prefix = "===========/ [" + DfTraceViewUtil.convertToPerformanceView(after - before) + " ";
        if (List.class.isAssignableFrom(retType)) {
            if (ret == null) {
                resultExp = prefix + "(null)]";
            } else {
                final List<?> ls = (java.util.List<?>) ret;
                if (ls.isEmpty()) {
                    resultExp = prefix + "(0)]";
                } else if (ls.size() == 1) {
                    resultExp = prefix + "(1) result=" + buildEntityExp(ls.get(0)) + "]";
                } else {
                    resultExp = prefix + "(" + ls.size() + ") first=" + buildEntityExp(ls.get(0)) + "]";
                }
            }
        } else if (Entity.class.isAssignableFrom(retType)) {
            if (ret == null) {
                resultExp = prefix + "(null)" + "]";
            } else {
                final Entity entity = (Entity) ret;
                resultExp = prefix + "(1) result=" + buildEntityExp(entity) + "]";
            }
        } else if (int[].class.isAssignableFrom(retType)) {
            if (ret == null) { // basically not come here
                resultExp = prefix + "(null)" + "]";
            } else {
                final int[] resultArray = (int[]) ret;
                resultExp = buildBatchUpdateResultExp(prefix, resultArray);
            }
        } else {
            resultExp = prefix + "result=" + ret + "]";
        }
        return resultExp;
    }

    protected String buildEntityExp(Object obj) {
        if (obj instanceof Entity) {
            Entity entity = (Entity) obj;

            // The name for display is null
            // because you can know it other execute status logs.
            return entity.buildDisplayString(null, true, true);
        } else {
            return obj != null ? obj.toString() : "null";
        }
    }

    protected String buildBatchUpdateResultExp(String prefix, int[] resultArray) {
        if (resultArray.length == 0) {
            return prefix + "all-updated=(0)]";
        }
        final StringBuilder sb = new StringBuilder();
        boolean resultExpressionScope = true;
        int resultCount = 0;
        int loopCount = 0;
        for (int element : resultArray) {
            resultCount = resultCount + element;
            if (resultExpressionScope) {
                if (loopCount <= 10) {
                    if (sb.length() == 0) {
                        sb.append(element);
                    } else {
                        sb.append(",").append(element);
                    }
                } else {
                    sb.append(",").append("...");
                    resultExpressionScope = false;
                }
            }
            ++loopCount;
        }
        sb.insert(0, "{").append("}");
        final String result = sb.toString();
        final String returnExp;
        if (resultCount >= 0) {
            returnExp = prefix + "all-updated=(" + resultCount + ") result=" + result + "]";
        } else { // minus
            returnExp = prefix + "result=" + result + "]"; // for example, Oracle
        }
        return returnExp;
    }
}
