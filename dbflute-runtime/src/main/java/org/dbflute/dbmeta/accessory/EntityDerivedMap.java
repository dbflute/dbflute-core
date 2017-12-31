/*
 * Copyright 2014-2018 the original author or authors.
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
import java.util.Map.Entry;

import org.dbflute.Entity;
import org.dbflute.exception.SpecifyDerivedReferrerInvalidAliasNameException;
import org.dbflute.exception.SpecifyDerivedReferrerPropertyValueNotFoundException;
import org.dbflute.exception.SpecifyDerivedReferrerUnknownAliasNameException;
import org.dbflute.exception.SpecifyDerivedReferrerUnmatchedPropertyTypeException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.optional.OptionalScalar;

/**
 * The derived map of entity. (basically for Framework)
 * @author jflute
 * @since 1.1.0 (2014/10/29 Monday)
 */
public class EntityDerivedMap implements Serializable, Cloneable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The serial version UID for object serialization. (Default) */
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The map of derived value. map:{alias-name = value} (NullAllowed: lazy-loaded) */
    protected Map<String, Object> _derivedMap;

    // ===================================================================================
    //                                                                            Register
    //                                                                            ========
    /**
     * Register the derived value to the map.
     * @param aliasName The alias name of derived-referrer. (NotNull)
     * @param selectedValue The derived value selected from database. (NullAllowed: when null selected)
     */
    public void registerDerivedValue(String aliasName, Object selectedValue) {
        getDerivedMap().put(aliasName, selectedValue);
    }

    // ===================================================================================
    //                                                                               Find
    //                                                                              ======
    /**
     * Find the derived value in the map.
     * @param <VALUE> The type of derived value.
     * @param entity The entity that has the derived value, basically for logging. (NotNull)
     * @param aliasName The alias name of derived-referrer. (NotNull)
     * @param propertyType The type of the derived property, should match as rule. (NotNull)
     * @return The optional scalar for derived value found in the map. (NotNull, EmptyAllowed: when null selected)
     * @throws SpecifyDerivedReferrerInvalidAliasNameException When the alias name does not start with '$'.
     * @throws SpecifyDerivedReferrerUnknownAliasNameException When the alias name is unknown, no derived.
     * @throws SpecifyDerivedReferrerUnmatchedPropertyTypeException When the property type is unmatched with actual type.
     */
    public <VALUE> OptionalScalar<VALUE> findDerivedValue(Entity entity, String aliasName, Class<VALUE> propertyType) {
        if (aliasName == null) {
            throw new IllegalArgumentException("The argument 'aliasName' should not be null.");
        }
        if (propertyType == null) {
            throw new IllegalArgumentException("The argument 'propertyType' should not be null.");
        }
        if (aliasName.trim().length() == 0) {
            throw new IllegalArgumentException("The argument 'aliasName' should not be empty: [" + aliasName + "]");
        }
        if (!aliasName.startsWith(DerivedMappable.MAPPING_ALIAS_PREFIX)) {
            throwInvalidDerivedAliasNameException(entity, aliasName);
        }
        final Map<String, Object> derivedMap = getDerivedMap(); // lazy-loaded if nothing
        if (!derivedMap.containsKey(aliasName)) {
            throwUnknownDerivedAliasNameException(entity, aliasName, derivedMap);
        }
        try {
            final VALUE found = propertyType.cast(derivedMap.get(aliasName)); // also checking type
            final String tableDbName = entity.asTableDbName(); // not to have reference to entity in optional
            return OptionalScalar.ofNullable(found, () -> {
                throwDerivedPropertyValueNotFoundException(tableDbName, aliasName);
            }); // null allowed
        } catch (ClassCastException e) {
            throwUnmatchDerivedPropertyTypeException(entity, aliasName, propertyType, e);
            return null; // unreachable
        }
    }

    // ===================================================================================
    //                                                                              Others
    //                                                                              ======
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

    // ===================================================================================
    //                                                                         Derived Map
    //                                                                         ===========
    protected Map<String, Object> getDerivedMap() {
        if (_derivedMap == null) {
            _derivedMap = newDerivedMap();
        }
        return _derivedMap;
    }

    protected HashMap<String, Object> newDerivedMap() {
        return new HashMap<String, Object>();
    }

    // ===================================================================================
    //                                                                    Exception Helper
    //                                                                    ================
    protected void throwInvalidDerivedAliasNameException(Entity entity, String aliasName) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Illegal alias name (not start with '$') for the derived property.");
        br.addItem("Advice");
        br.addElement("Make sure your alias name to find the derived value.");
        br.addElement("You should specify the name that starts with '$'.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    cb.specify().derivedPurchaseList().max(purchaseCB -> {");
        br.addElement("        purchaseCB.specify().columnPurchasePrice();");
        br.addElement("    }, \"$HIGHEST_PURCHASE_PRICE\");");
        br.addElement("    ...");
        br.addElement("    Member member = ...");
        br.addElement("    ... = member.derived(\"HIGHEST_PURCHASE_PRICE\", Integer.class); // *NG");
        br.addElement("  (o):");
        br.addElement("    cb.specify().derivedPurchaseList().max(purchaseCB -> {");
        br.addElement("        purchaseCB.specify().columnPurchasePrice();");
        br.addElement("    }, \"$HIGHEST_PURCHASE_PRICE\");");
        br.addElement("    ...");
        br.addElement("    Member member = ...");
        br.addElement("    ... = member.derived(\"$HIGHEST_PURCHASE_PRICE\", Integer.class); // OK");
        br.addItem("Table");
        buildExceptionTableInfo(br, entity);
        br.addItem("Illegal Alias");
        br.addElement(aliasName);
        final String msg = br.buildExceptionMessage();
        throw new SpecifyDerivedReferrerInvalidAliasNameException(msg);
    }

    protected void throwUnknownDerivedAliasNameException(Entity entity, String aliasName, Map<String, Object> derivedMap) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the alias name in the derived map");
        br.addItem("Advice");
        br.addElement("Make sure your alias name to find the derived value.");
        br.addElement("You should specify the name specified as DerivedReferrer.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    String highestAlias = \"$HIGHEST_PURCHASE_PRICE\"");
        br.addElement("    cb.specify().derivedPurchaseList().max(purchaseCB -> {");
        br.addElement("        purchaseCB.specify().columnPurchasePrice();");
        br.addElement("    }, highestAlias);");
        br.addElement("    ...");
        br.addElement("    Member member = ...");
        br.addElement("    ... = member.derived(\"$BIG_SHOT\", Integer.class); // *NG");
        br.addElement("  (o):");
        br.addElement("    String highestAlias = \"$HIGHEST_PURCHASE_PRICE\"");
        br.addElement("    cb.specify().derivedPurchaseList().max(purchaseCB -> {");
        br.addElement("        purchaseCB.specify().columnPurchasePrice();");
        br.addElement("    }, highestAlias);");
        br.addElement("    ...");
        br.addElement("    Member member = ...");
        br.addElement("    ... = member.derived(highestAlias, Integer.class); // OK");
        br.addItem("Table");
        buildExceptionTableInfo(br, entity);
        br.addItem("Unknown Alias");
        br.addElement(aliasName);
        buildExceptionExistingDerivedMapInfo(br, derivedMap);
        final String msg = br.buildExceptionMessage();
        throw new SpecifyDerivedReferrerUnknownAliasNameException(msg);
    }

    protected void throwUnmatchDerivedPropertyTypeException(Entity entity, String aliasName, Class<?> propertyType,
            ClassCastException cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Illegal property type for the derived property.");
        br.addItem("Advice");
        br.addElement("Make sure your property type to find the derived value.");
        br.addElement("You should specify the matched type, it's rule is following:");
        br.addElement("  count()      : Integer");
        br.addElement("  max(), min() : (same as property type of the column)");
        br.addElement("  sum(), avg() : BigDecimal");
        br.addElement("");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    String highestAlias = \"$HIGHEST_PURCHASE_PRICE\"");
        br.addElement("    cb.specify().derivedPurchaseList().max(purchaseCB -> {");
        br.addElement("        purchaseCB.specify().columnPurchasePrice();");
        br.addElement("    }, highestAlias);");
        br.addElement("    ...");
        br.addElement("    Member member = ...");
        br.addElement("    ... = member.derived(highestAlias, LocalDate.class); // *NG");
        br.addElement("  (o):");
        br.addElement("    String highestAlias = \"$HIGHEST_PURCHASE_PRICE\"");
        br.addElement("    cb.specify().derivedPurchaseList().max(purchaseCB -> {");
        br.addElement("        purchaseCB.specify().columnPurchasePrice();");
        br.addElement("    }, highestAlias);");
        br.addElement("    ...");
        br.addElement("    Member member = ...");
        br.addElement("    ... = member.derived(highestAlias, Integer.class); // OK");
        buildExceptionTableInfo(br, entity);
        br.addItem("NotFound Alias");
        br.addElement(aliasName);
        br.addItem("Specified PropertyType");
        br.addElement(propertyType);
        buildExceptionExistingDerivedMapInfo(br, getDerivedMap());
        final String msg = br.buildExceptionMessage();
        throw new SpecifyDerivedReferrerUnmatchedPropertyTypeException(msg, cause);
    }

    protected void buildExceptionTableInfo(ExceptionMessageBuilder br, Entity entity) {
        br.addItem("Table");
        br.addElement(entity.asTableDbName());
        try {
            br.addElement(entity.asDBMeta().extractPrimaryKeyMap(entity));
        } catch (RuntimeException continued) { // just in case
            br.addElement("*Failed to get PK info:");
            br.addElement(continued.getMessage());
        }
    }

    protected void buildExceptionExistingDerivedMapInfo(ExceptionMessageBuilder br, Map<String, Object> derivedMap) {
        br.addItem("Existing DerivedMap");
        for (Entry<String, Object> entry : derivedMap.entrySet()) {
            final Object value = entry.getValue();
            br.addElement(entry.getKey() + " = " + (value != null ? value.getClass() : null));
        }
    }

    protected void throwDerivedPropertyValueNotFoundException(String tableDbName, String aliasName) { // embedded in optional
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the derived property value.");
        br.addItem("Advice");
        br.addElement("Please confirm the existence your property value.");
        br.addElement("Especially e.g. max(), sum(), ... might return null if no referrer data");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    String highestAlias = \"$HIGHEST_PURCHASE_PRICE\"");
        br.addElement("    cb.specify().derivedPurchaseList().max(purchaseCB -> {");
        br.addElement("        purchaseCB.specify().columnPurchasePrice();");
        br.addElement("    }, highestAlias);");
        br.addElement("    ...");
        br.addElement("    Member member = ...");
        br.addElement("    member.derived(highestAlias, Integer.class).alwaysPresent(...); // *NG");
        br.addElement("  (o):");
        br.addElement("    String highestAlias = \"$HIGHEST_PURCHASE_PRICE\"");
        br.addElement("    cb.specify().derivedPurchaseList().max(purchaseCB -> {");
        br.addElement("        purchaseCB.specify().columnPurchasePrice();");
        br.addElement("    }, highestAlias);");
        br.addElement("    ...");
        br.addElement("    Member member = ...");
        br.addElement("    member.derived(highestAlias, Integer.class).ifPresent(...); // OK");
        br.addElement("  (o):");
        br.addElement("    String highestAlias = \"$HIGHEST_PURCHASE_PRICE\"");
        br.addElement("    cb.specify().derivedPurchaseList().max(purchaseCB -> {");
        br.addElement("        purchaseCB.specify().columnPurchasePrice();");
        br.addElement("    }, highestAlias, op -> op.coalesce(0)); // *point");
        br.addElement("    ...");
        br.addElement("    Member member = ...");
        br.addElement("    member.derived(highestAlias, Integer.class).alwaysPresent(...); // OK");
        br.addItem("Table");
        br.addElement(tableDbName);
        br.addItem("DerivedProperty (Alias)");
        br.addElement(aliasName);
        // embedded in optional so no reference to derived map
        //buildExceptionExistingDerivedMapInfo(br, getDerivedMap());
        final String msg = br.buildExceptionMessage();
        throw new SpecifyDerivedReferrerPropertyValueNotFoundException(msg);
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "derivedMap:" + _derivedMap;
    }

    @Override
    public EntityDerivedMap clone() { // almost deep copy (value is shallow copy, not always immutable)
        try {
            final EntityDerivedMap cloned = (EntityDerivedMap) super.clone();
            if (_derivedMap != null) {
                final Map<String, Object> copied = newDerivedMap();
                copied.putAll(_derivedMap);
                cloned._derivedMap = copied;
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Failed to clone the derived map: " + toString(), e);
        }
    }
}
