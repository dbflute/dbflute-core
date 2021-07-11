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
package org.dbflute.properties.assistant.classification.element.acceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.dbflute.exception.DfClassificationRequiredAttributeNotFoundException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.properties.assistant.classification.DfClassificationElement;

/**
 * @author jflute
 * @since 1.2.5 split from DfClassificationTop (2021/07/04 Sunday at roppongi japanese)
 */
public class DfClsElementBasicItemAcceptor {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final String _classificationName; // not null
    protected final String _table; // null allowed (not required)
    protected final Map<?, ?> _elementMap;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfClsElementBasicItemAcceptor(String classificationName, String table, Map<?, ?> elementMap) {
        _classificationName = classificationName;
        _table = table;
        _elementMap = elementMap;
    }

    // ===================================================================================
    //                                                                               Code
    //                                                                              ======
    public String acceptCode() { // not null
        final String code = (String) _elementMap.get(DfClassificationElement.KEY_CODE);
        if (code == null) {
            throwClassificationRequiredAttributeNotFoundException();
        }
        return code;
    }

    protected void throwClassificationRequiredAttributeNotFoundException() {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The element map did not have a 'code' attribute.");
        br.addItem("Advice");
        br.addElement("An element map requires 'code' attribute like this:");
        br.addElement("  (o): map:{table=MEMBER_STATUS; code=MEMBER_STATUS_CODE; ...}");
        br.addItem("Classification");
        br.addElement(_classificationName);
        if (_table != null) {
            br.addItem("Table");
            br.addElement(_table);
        }
        br.addItem("ElementMap");
        br.addElement(_elementMap);
        final String msg = br.buildExceptionMessage();
        throw new DfClassificationRequiredAttributeNotFoundException(msg);
    }

    // ===================================================================================
    //                                                                               Name
    //                                                                              ======
    public String acceptName(String defaultName) { // not null if not-null default
        final String name = (String) _elementMap.get(DfClassificationElement.KEY_NAME);
        return name != null ? name : defaultName;
    }

    // ===================================================================================
    //                                                                               Alias
    //                                                                               =====
    public String acceptAlias(String defaultAlias) { // not null if not-null default
        final String alias = (String) _elementMap.get(DfClassificationElement.KEY_ALIAS);
        return alias != null ? alias : defaultAlias;
    }

    // ===================================================================================
    //                                                                             Comment
    //                                                                             =======
    public String acceptComment() { // null allowed (not required)
        return doAcceptComment();
    }

    public String acceptComment(String defaultComment) { // null allowed (not required)
        final String comment = doAcceptComment();
        return comment != null ? comment : defaultComment;
    }

    protected String doAcceptComment() {
        return (String) _elementMap.get(DfClassificationElement.KEY_COMMENT);
    }

    // ===================================================================================
    //                                                                         Sister Code
    //                                                                         ===========
    public String[] acceptSisters() { // not null, empty allowed (not required)
        return doAcceptSisters(() -> new String[] {});
    }

    public String[] acceptSisters(String[] defaultSisters) { // not null if not-null default
        return doAcceptSisters(() -> defaultSisters);
    }

    protected String[] doAcceptSisters(Supplier<String[]> defaultProvider) { // null allowed if not defined
        final Object sisterCodeObj = _elementMap.get(DfClassificationElement.KEY_SISTER_CODE);
        final String[] sisters;
        if (sisterCodeObj != null) {
            if (sisterCodeObj instanceof List<?>) {
                @SuppressWarnings("unchecked")
                final List<String> sisterCodeList = (List<String>) sisterCodeObj;
                sisters = sisterCodeList.toArray(new String[sisterCodeList.size()]);
            } else {
                sisters = new String[] { (String) sisterCodeObj };
            }
        } else { // not defined
            sisters = defaultProvider.get();
        }
        return sisters;
    }

    // ===================================================================================
    //                                                                         SubItem Map
    //                                                                         ===========
    public Map<String, Object> acceptSubItemMap() { // not null, empty allowed (not required)
        return doAcceptSubItemMap(() -> new HashMap<String, Object>(2)); // mutable just in case (2021/07/04)
    }

    public Map<String, Object> acceptSubItemMap(Map<String, Object> defaultSubItemMap) { // not null if not-null default
        return doAcceptSubItemMap(() -> defaultSubItemMap);
    }

    protected Map<String, Object> doAcceptSubItemMap(Supplier<Map<String, Object>> defaultProvider) { // null allowed if not defined
        // initialize dummy instance when no definition for velocity trap
        // (if null, variable in for-each is not overridden so previous loop's value is used)
        @SuppressWarnings("unchecked")
        final Map<String, Object> subItemMap = (Map<String, Object>) _elementMap.get(DfClassificationElement.KEY_SUB_ITEM_MAP);
        return subItemMap != null ? subItemMap : defaultProvider.get();
    }
}
