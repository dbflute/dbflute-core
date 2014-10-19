/*
 * Copyright 2014-2014 the original author or authors.
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
package org.dbflute.dbmeta.accessory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.dbflute.exception.SpecifyDerivedReferrerUnknownAliasNameException;
import org.dbflute.helper.message.ExceptionMessageBuilder;

/**
 * The derived map of entity. (basically for Framework)
 * @author jflute
 * @since 1.1.0 (2014/10/29 Monday)
 */
public class EntityDerivedMap implements Serializable {

    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    /** The map of derived value. map:{alias-name = value} (NullAllowed: lazy-loaded) */
    protected Map<String, Object> _derivedMap;

    /**
     * Register the derived value to the map.
     * @param aliasName The alias name of derived-referrer. (NotNull)
     * @param selectedValue The derived value selected from database. (NullAllowed: when null selected)
     */
    public void registerDerivedValue(String aliasName, Object selectedValue) {
        getDerivedMap().put(aliasName, selectedValue);
    }

    /**
     * Find the derived value in the map.
     * @param <VALUE> The type of derived value.
     * @param aliasName The alias name of derived-referrer. (NotNull)
     * @return The derived value found in the map. (NullAllowed: when null selected)
     */
    public <VALUE> VALUE findDerivedValue(String aliasName) {
        if (aliasName == null) {
            throw new IllegalArgumentException("The argument 'aliasName' should not be null.");
        }
        final Map<String, Object> derivedMap = getDerivedMap();
        if (!derivedMap.containsKey(aliasName)) {
            throwUnknownAliasNameException(aliasName, derivedMap);
        }
        @SuppressWarnings("unchecked")
        final VALUE found = (VALUE) derivedMap.get(aliasName);
        return found;
    }

    protected void throwUnknownAliasNameException(String aliasName, final Map<String, Object> derivedMap) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the alias name in the derived map");
        br.addItem("Advice");
        br.addElement("Make sure your alias name to find the derived value.");
        br.addElement("You should specify the name specified as DerivedReferrer.");
        br.addElement("For example:");
        br.addElement("  (o):");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    cb.specify().derivedPurchaseList().max(purchaseCB -> {");
        br.addElement("        purchaseCB.specify().columnPurchasePrice();");
        br.addElement("    }, Member.ALIAS_highestPurchasePrice);");
        br.addElement("    ...");
        br.addElement("    Member member = ...");
        br.addElement("    Integer price = member.derived(Member.ALIAS_dynamicPurchasePanther); // *NG");
        br.addElement("  (o):");
        br.addElement("    MemberCB cb = new MemberCB();");
        br.addElement("    cb.specify().derivedPurchaseList().max(purchaseCB -> {");
        br.addElement("        purchaseCB.specify().columnPurchasePrice();");
        br.addElement("    }, Member.ALIAS_highestPurchasePrice);");
        br.addElement("    ...");
        br.addElement("    Member member = ...");
        br.addElement("    Integer price = member.derived(Member.ALIAS_highestPurchasePrice); // OK");
        br.addItem("Alias Name");
        br.addElement(aliasName);
        br.addItem("Derived Map");
        br.addElement(derivedMap.keySet());
        final String msg = br.buildExceptionMessage();
        throw new SpecifyDerivedReferrerUnknownAliasNameException(msg);
    }

    /**
     * Is the derived map empty?
     * @return The determination, true or false.
     */
    public boolean isEmpty() {
        return getDerivedMap().isEmpty();
    }

    /**
     * Clear the derived map.
     */
    public void clear() {
        getDerivedMap().clear();
    }

    /**
     * Remove the derived value from the map.
     * @param aliasName The alias name of derived-referrer. (NotNull)
     */
    public void remove(String aliasName) {
        getDerivedMap().remove(aliasName);
    }

    protected Map<String, Object> getDerivedMap() {
        if (_derivedMap == null) {
            _derivedMap = new HashMap<String, Object>();
        }
        return _derivedMap;
    }

    @Override
    public String toString() {
        return "derivedMap:" + _derivedMap;
    }
}
