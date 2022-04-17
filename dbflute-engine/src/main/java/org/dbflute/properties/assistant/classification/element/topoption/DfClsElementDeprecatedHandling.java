/*
 * Copyright 2014-2022 the original author or authors.
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
package org.dbflute.properties.assistant.classification.element.topoption;

import org.dbflute.properties.assistant.classification.DfClassificationTop;
import org.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.2.5 split from DfClassificationElement (2021/07/03 Saturday at roppongi japanese)
 */
public class DfClsElementDeprecatedHandling {

    protected final DfClassificationTop _classificationTop; // null allowed
    protected final String _name; // null allowed, empty allowed

    public DfClsElementDeprecatedHandling(DfClassificationTop classificationTop, String name) {
        _classificationTop = classificationTop;
        _name = name;
    }

    public boolean isDeprecated() {
        if (_classificationTop == null) { // just in case
            return false;
        }
        return _name != null && _classificationTop.getDeprecatedMap().containsKey(_name);
    }

    public String getDeprecatedComment() {
        if (!isDeprecated()) {
            return "";
        }
        // #for_now jflute needs to escape for e.g. JavaDoc, SchemaHTML? (2021/07/05)
        final String comment = _name != null ? _classificationTop.getDeprecatedMap().get(_name) : null;
        return comment != null ? removeLineSeparator(comment) : "";
    }

    protected String removeLineSeparator(String str) {
        return Srl.replace(Srl.replace(str, "\r\n", "\n"), "\n", "");
    }
}
