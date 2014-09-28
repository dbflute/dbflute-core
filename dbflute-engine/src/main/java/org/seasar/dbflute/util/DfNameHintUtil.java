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
package org.seasar.dbflute.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jflute
 */
public class DfNameHintUtil {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String PREFIX_MARK = "prefix:";
    public static final String SUFFIX_MARK = "suffix:";
    public static final String CONTAIN_MARK = "contain:";
    public static final List<String> _markList = new ArrayList<String>();
    static {
        _markList.add(PREFIX_MARK);
        _markList.add(SUFFIX_MARK);
        _markList.add(CONTAIN_MARK);
    }

    public static final List<String> getMarkList() {
        return _markList;
    }

    // ===================================================================================
    //                                                                              Target
    //                                                                              ======
    public static boolean isTargetByHint(String name, List<String> targetList, List<String> exceptList) {
        if (targetList == null) {
            throw new IllegalArgumentException("The argument 'targetList' should not be null.");
        }
        if (exceptList == null) {
            throw new IllegalArgumentException("The argument 'exceptList' should not be null.");
        }
        if (!targetList.isEmpty()) {
            return isHitByTargetList(name, targetList);
        }
        for (String tableHint : exceptList) {
            if (isHitByTheHint(name, tableHint)) {
                return false;
            }
        }
        return true;
    }

    protected static boolean isHitByTargetList(String name, List<String> targetList) {
        for (String tableHint : targetList) {
            if (isHitByTheHint(name, tableHint)) {
                return true;
            }
        }
        return false;
    }

    // ===================================================================================
    //                                                                               Basic
    //                                                                               =====
    /**
     * Does it hit the target name by the hint. {CaseInsensitive}
     * @param name The target name. (NotNull)
     * @param hint The hint of the name. (NotNull)
     * @return The determination, true or false.
     */
    public static boolean isHitByTheHint(String name, String hint) {
        final String prefixMark = PREFIX_MARK;
        final String suffixMark = SUFFIX_MARK;
        final String containMark = CONTAIN_MARK;

        if (hint.toLowerCase().startsWith(prefixMark.toLowerCase())) {
            final String pureHint = hint.substring(prefixMark.length(), hint.length());
            if (name.toLowerCase().startsWith(pureHint.toLowerCase())) {
                return true;
            }
        } else if (hint.toLowerCase().startsWith(suffixMark.toLowerCase())) {
            final String pureHint = hint.substring(suffixMark.length(), hint.length());
            if (name.toLowerCase().endsWith(pureHint.toLowerCase())) {
                return true;
            }
        } else if (hint.toLowerCase().startsWith(containMark.toLowerCase())) {
            final String pureHint = hint.substring(containMark.length(), hint.length());
            if (name.toLowerCase().contains(pureHint.toLowerCase())) {
                return true;
            }
        } else {
            // equals as flexible name
            if (Srl.replace(name, "_", "").equalsIgnoreCase(Srl.replace(hint, "_", ""))) {
                return true;
            }
        }
        return false;
    }
}