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
package org.dbflute.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
    public static final String PATTERN_MARK = "pattern:";
    public static final List<String> _markList = new ArrayList<String>();
    static {
        _markList.add(PREFIX_MARK);
        _markList.add(SUFFIX_MARK);
        _markList.add(CONTAIN_MARK);
        _markList.add(PATTERN_MARK);
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
     * Does it hit the target name by the hint. {CaseInsensitive, Flexible}
     * @param name The target name. (NotNull)
     * @param hint The hint of the name. (NotNull)
     * @return The determination, true or false.
     */
    public static boolean isHitByTheHint(String name, String hint) {
        final String prefixMark = PREFIX_MARK;
        final String suffixMark = SUFFIX_MARK;
        final String containMark = CONTAIN_MARK;
        final String patternMark = PATTERN_MARK;

        final String lowerHint = hint.toLowerCase();
        if (lowerHint.startsWith(prefixMark.toLowerCase())) {
            final String pureHint = hint.substring(prefixMark.length(), hint.length());
            if (name.toLowerCase().startsWith(pureHint.toLowerCase())) {
                return true;
            }
        } else if (lowerHint.startsWith(suffixMark.toLowerCase())) {
            final String pureHint = hint.substring(suffixMark.length(), hint.length());
            if (name.toLowerCase().endsWith(pureHint.toLowerCase())) {
                return true;
            }
        } else if (lowerHint.startsWith(containMark.toLowerCase())) {
            final String pureHint = hint.substring(containMark.length(), hint.length());
            if (name.toLowerCase().contains(pureHint.toLowerCase())) {
                return true;
            }
        } else if (lowerHint.startsWith(patternMark.toLowerCase())) {
            final String pureHint = hint.substring(patternMark.length(), hint.length());
            final Pattern pattern = Pattern.compile(pureHint);
            if (pattern.matcher(name).matches()) {
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