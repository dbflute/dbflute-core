/*
 * Copyright 2014-2015 the original author or authors.
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
import java.util.Map.Entry;

import junit.framework.AssertionFailedError;

import org.dbflute.helper.message.ExceptionMessageBuilder;

/**
 * @author jflute
 * @since 0.4.0 (2014/03/16 Sunday)
 */
public class MarkHereManager {

    /** The map of mark to assert that it goes through the road. (NullAllowed: when no mark, so lazy-loaded) */
    protected Map<String, MarkHereInfo> _xmarkMap;

    /**
     * Mark here to assert that it goes through the road.
     * @param mark The your original mark expression as string. (NotNull)
     */
    public void mark(String mark) {
        if (_xmarkMap == null) {
            _xmarkMap = new LinkedHashMap<String, MarkHereInfo>();
        }
        MarkHereInfo info = _xmarkMap.get(mark);
        if (info == null) {
            info = new MarkHereInfo(mark);
            _xmarkMap.put(mark, info);
        }
        info.markPhase();
    }

    /**
     * Assert the mark is marked. (found in existing marks)
     * @param mark The your original mark expression as string. (NotNull)
     */
    public void assertMarked(String mark) {
        boolean existsMark = false;
        if (_xmarkMap != null) {
            final MarkHereInfo info = _xmarkMap.get(mark);
            if (isFoundMark(info)) {
                existsMark = true;
                info.finishPhase();
            }
        }
        if (!existsMark) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("The mark was NOT marked. (not found)");
            br.addItem("NotFound Mark");
            br.addElement(mark);
            br.addItem("Mark Map");
            if (_xmarkMap != null && !_xmarkMap.isEmpty()) {
                for (Entry<String, MarkHereInfo> entry : _xmarkMap.entrySet()) {
                    br.addElement(entry.getValue());
                }
            } else {
                br.addItem("*no mark");
            }
            final String msg = br.buildExceptionMessage();
            throw new AssertionFailedError(msg);
        }
    }

    protected boolean isFoundMark(MarkHereInfo info) {
        // current phase is basically non-asserted but just in case
        return info != null && info.isNonAssertedPhase();
    }

    /**
     * Is the mark marked? (found the mark in existing marks?)
     * @param mark The your original mark expression as string. (NotNull)
     * @return The determination, true or false.
     */
    public boolean isMarked(String mark) {
        return _xmarkMap != null && _xmarkMap.get(mark) != null;
    }

    public void checkNonAssertedMark() {
        if (_xmarkMap == null) {
            return;
        }
        MarkHereInfo nonAssertedInfo = null;
        for (Entry<String, MarkHereInfo> entry : _xmarkMap.entrySet()) {
            final MarkHereInfo info = entry.getValue();
            if (info.isNonAssertedPhase()) {
                nonAssertedInfo = info;
                break;
            }
        }
        if (nonAssertedInfo != null) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("Found the non-asserted mark.");
            br.addItem("Advice");
            br.addElement("The mark should be asserted like this:");
            br.addElement("  (x):");
            br.addElement("    markHere(\"foo\");");
            br.addElement("    markHere(\"bar\");");
            br.addElement("    ...");
            br.addElement("    assertMarked(\"foo\");");
            br.addElement("  (o):");
            br.addElement("    markHere(\"foo\");");
            br.addElement("    markHere(\"bar\");");
            br.addElement("    ...");
            br.addElement("    assertMarked(\"foo\");");
            br.addElement("    assertMarked(\"bar\");");
            br.addItem("Non-Asserted Mark");
            br.addElement(nonAssertedInfo.getMark());
            br.addItem("Mark Map");
            for (Entry<String, MarkHereInfo> entry : _xmarkMap.entrySet()) {
                br.addElement(entry.getValue());
            }
            final String msg = br.buildExceptionMessage();
            throw new AssertionFailedError(msg);
        }
    }

    public void clearMarkMap() {
        if (_xmarkMap != null) {
            _xmarkMap.clear();
            _xmarkMap = null;
        }
    }
}
