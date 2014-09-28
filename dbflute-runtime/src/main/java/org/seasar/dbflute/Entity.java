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
package org.seasar.dbflute;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.exception.SpecifyDerivedReferrerUnknownAliasNameException;
import org.seasar.dbflute.exception.UndefinedClassificationCodeException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.jdbc.Classification;
import org.seasar.dbflute.jdbc.ClassificationMeta;
import org.seasar.dbflute.jdbc.ClassificationUndefinedHandlingType;
import org.seasar.dbflute.jdbc.ParameterUtil;
import org.seasar.dbflute.util.DfTypeUtil;

/**
 * The interface of entity.
 * @author jflute
 */
public interface Entity {

    // ===================================================================================
    //                                                                              DBMeta
    //                                                                              ======
    /**
     * Get the target DB meta.
     * @return The instance of DBMeta type. (NotNull)
     */
    DBMeta getDBMeta();

    // ===================================================================================
    //                                                                          Table Name
    //                                                                          ==========
    /**
     * Get table DB name.
     * @return The string for name. (NotNull)
     */
    String getTableDbName();

    /**
     * Get table property name according to Java Beans rule.
     * @return The string for name. (NotNull)
     */
    String getTablePropertyName();

    // ===================================================================================
    //                                                                         Primary Key
    //                                                                         ===========
    /**
     * Does it have the value of primary keys?
     * @return The determination, true or false. (if all PK values are not null, returns true)
     */
    boolean hasPrimaryKeyValue();

    /**
     * Get the properties of specified unique columns as unique-driven.
     * @return The set of property name for specified unique columns. (NotNull)
     */
    Set<String> myuniqueDrivenProperties(); // prefix 'my' not to show when uniqueBy() completion

    /**
     * The unique-driven properties of entity. (basically for Framework)
     */
    public static class EntityUniqueDrivenProperties implements Serializable {

        /** Serial version UID. (Default) */
        private static final long serialVersionUID = 1L;

        /** The set of property names. (NullAllowed: lazy-loaded) */
        protected Set<String> _propertyNameSet;

        /**
         * Add property name. (according to Java Beans rule)
         * @param propertyName The string for name. (NotNull)
         */
        public void addPropertyName(String propertyName) {
            getPropertyNameSet().add(propertyName);
        }

        /**
         * Get the set of properties.
         * @return The set of properties. (NotNull)
         */
        public Set<String> getPropertyNames() {
            return getPropertyNameSet();
        }

        /**
         * Is the set of properties empty?
         * @return The determination, true or false.
         */
        public boolean isEmpty() {
            return getPropertyNameSet().isEmpty();
        }

        /**
         * Clear the set of properties.
         */
        public void clear() {
            getPropertyNameSet().clear();
        }

        /**
         * Remove property name from the set. (according to Java Beans rule)
         * @param propertyName The string for name. (NotNull)
         */
        public void remove(String propertyName) {
            getPropertyNameSet().remove(propertyName);
        }

        /**
         * Accept specified properties. (after clearing this properties)
         * @param properties The properties as copy-resource. (NotNull)
         */
        public void accept(EntityModifiedProperties properties) {
            clear();
            for (String propertyName : properties.getPropertyNames()) {
                addPropertyName(propertyName);
            }
        }

        protected Set<String> getPropertyNameSet() {
            if (_propertyNameSet == null) {
                _propertyNameSet = new LinkedHashSet<String>(2);
            }
            return _propertyNameSet;
        }
    }

    // ===================================================================================
    //                                                                 Modified Properties
    //                                                                 ===================
    /**
     * Get the set of modified properties. (basically for Framework) <br />
     * The properties needs to be according to Java Beans rule.
     * @return The set of property name for modified columns. (NotNull)
     */
    Set<String> modifiedProperties();

    /**
     * Clear the information of modified properties. (basically for Framework)
     */
    void clearModifiedInfo();

    /**
     * Does it have modifications of property names. (basically for Framework)
     * @return The determination, true or false.
     */
    boolean hasModification();

    /**
     * The modified properties of entity. (basically for Framework)
     */
    public static class EntityModifiedProperties implements Serializable {

        /** Serial version UID. (Default) */
        private static final long serialVersionUID = 1L;

        /** The set of property names. (NullAllowed: lazy-loaded) */
        protected Set<String> _propertyNameSet;

        /**
         * Add property name. (according to Java Beans rule)
         * @param propertyName The string for name. (NotNull)
         */
        public void addPropertyName(String propertyName) {
            getPropertyNameSet().add(propertyName);
        }

        /**
         * Get the set of properties.
         * @return The set of properties. (NotNull)
         */
        public Set<String> getPropertyNames() {
            return getPropertyNameSet();
        }

        /**
         * Is the set of properties empty?
         * @return The determination, true or false.
         */
        public boolean isEmpty() {
            return getPropertyNameSet().isEmpty();
        }

        /**
         * Clear the set of properties.
         */
        public void clear() {
            getPropertyNameSet().clear();
        }

        /**
         * Remove property name from the set. (according to Java Beans rule)
         * @param propertyName The string for name. (NotNull)
         */
        public void remove(String propertyName) {
            getPropertyNameSet().remove(propertyName);
        }

        /**
         * Accept specified properties. (after clearing this properties)
         * @param properties The properties as copy-resource. (NotNull)
         */
        public void accept(EntityModifiedProperties properties) {
            clear();
            for (String propertyName : properties.getPropertyNames()) {
                addPropertyName(propertyName);
            }
        }

        protected Set<String> getPropertyNameSet() {
            if (_propertyNameSet == null) {
                _propertyNameSet = new LinkedHashSet<String>();
            }
            return _propertyNameSet;
        }
    }

    // ===================================================================================
    //                                                                     Birthplace Mark
    //                                                                     ===============
    /**
     * Mark as select that means the entity is created by DBFlute select process. (basically for Framework)
     */
    void markAsSelect();

    /**
     * Is the entity created by DBFlute select process? (basically for Framework)
     * @return The determination, true or false.
     */
    boolean createdBySelect();

    // ===================================================================================
    //                                                                    Derived Mappable
    //                                                                    ================
    /**
     * The derived map of entity. (basically for Framework)
     */
    public static class EntityDerivedMap implements Serializable {

        /** Serial version UID. (Default) */
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
         * @param aliasName The alias name of derived-referrer. (NotNull)
         * * @return The derived value found in the map. (NullAllowed: when null selected)
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
    }

    // ===================================================================================
    //                                                                    Extension Method
    //                                                                    ================
    /**
     * Calculate the hash-code, which is a default hash code, to identify the instance.
     * @return The hash-code from super.hashCode().
     */
    int instanceHash();

    /**
     * Convert the entity to display string with relation information.
     * @return The display string of basic informations with one-nested relation values. (NotNull)
     */
    String toStringWithRelation();

    /**
     * Build display string flexibly.
     * @param name The name for display. (NullAllowed: If it's null, it does not have a name)
     * @param column Does it contains column values or not?
     * @param relation Does it contains relation existences or not?
     * @return The display string for this entity. (NotNull)
     */
    String buildDisplayString(String name, boolean column, boolean relation);

    // ===================================================================================
    //                                                                      Internal Class
    //                                                                      ==============
    public static final class FunCustodial {

        /** The instance of logging object for classification meta. */
        private static final Log _clsMetaLog = LogFactory.getLog(ClassificationMeta.class);

        @SuppressWarnings("unchecked")
        public static <NUMBER extends Number> NUMBER toNumber(Object obj, Class<NUMBER> type) {
            return (NUMBER) DfTypeUtil.toNumber(obj, type);
        }

        public static Boolean toBoolean(Object obj) {
            return DfTypeUtil.toBoolean(obj);
        }

        public static boolean isSameValue(Object value1, Object value2) {
            if (value1 == null && value2 == null) {
                return true;
            }
            if (value1 == null || value2 == null) {
                return false;
            }
            if (value1 instanceof byte[] && value2 instanceof byte[]) {
                return isSameValueBytes((byte[]) value1, (byte[]) value2);
            }
            return value1.equals(value2);
        }

        public static boolean isSameValueBytes(byte[] bytes1, byte[] bytes2) {
            if (bytes1 == null && bytes2 == null) {
                return true;
            }
            if (bytes1 == null || bytes2 == null) {
                return false;
            }
            if (bytes1.length != bytes2.length) {
                return false;
            }
            for (int i = 0; i < bytes1.length; i++) {
                if (bytes1[i] != bytes2[i]) {
                    return false;
                }
            }
            return true;
        }

        public static int calculateHashcode(int result, Object value) { // calculateHashcode()
            if (value == null) {
                return result;
            }
            return (31 * result) + (value instanceof byte[] ? ((byte[]) value).length : value.hashCode());
        }

        public static String convertEmptyToNull(String value) {
            return ParameterUtil.convertEmptyToNull(value);
        }

        public static String toClassTitle(Object entity) {
            return DfTypeUtil.toClassTitle(entity);
        }

        public static String toString(Date date, String pattern) {
            if (date == null) {
                return null;
            }
            final String str = DfTypeUtil.toString(date, pattern);
            return (DfTypeUtil.isDateBC(date) ? "BC" : "") + str;
        }

        public static String toString(byte[] bytes) {
            return "byte[" + (bytes != null ? String.valueOf(bytes.length) : "null") + "]";
        }

        public static void checkClassificationCode(Entity entity, String columnDbName, ClassificationMeta meta,
                Object code) {
            if (code == null) {
                return;
            }
            final ClassificationUndefinedHandlingType undefinedHandlingType = meta.undefinedHandlingType();
            if (!undefinedHandlingType.isChecked()) { // basically no way (not called if no check)
                return;
            }
            if (meta.codeOf(code) != null) {
                return;
            }
            handleUndefinedClassificationCode(entity.getTableDbName(), columnDbName, meta, code);
        }

        public static void handleUndefinedClassificationCode(String tableDbName, String columnDbName,
                ClassificationMeta meta, Object code) {
            final ClassificationUndefinedHandlingType undefinedHandlingType = meta.undefinedHandlingType();
            if (ClassificationUndefinedHandlingType.EXCEPTION.equals(undefinedHandlingType)) {
                throwUndefinedClassificationCodeException(tableDbName, columnDbName, meta, code);
            } else if (ClassificationUndefinedHandlingType.LOGGING.equals(undefinedHandlingType)) {
                showUndefinedClassificationCodeMessage(tableDbName, columnDbName, meta, code);
            }
            // else means ALLOWED
        }

        public static void throwUndefinedClassificationCodeException(String tableDbName, String columnDbName,
                ClassificationMeta meta, Object code) {
            final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
            br.addNotice("Undefined classification code was set to the entity.");
            br.addItem("Advice");
            br.addElement("Confirm the value of the classication column on your database,");
            br.addElement("or setting value to your entity.");
            br.addElement("The code is NOT one of classification code defined on DBFlute.");
            br.addElement("");
            br.addElement("_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/");
            br.addElement(" Use formal code!");
            br.addElement("  Or add the code to classification definition.");
            br.addElement("_/_/_/_/_/_/_/_/_/_/");
            br.addElement("");
            br.addElement("Or if you (reluctantly) need to allow it, change the option like this:");
            br.addElement("but *Deprecated");
            br.addElement("(classificationDefinitionMap.dfprop)");
            br.addElement("    ; [classification-name] = list:{");
            br.addElement("        ; map:{");
            br.addElement("            ; topComment=...; codeType=...");
            br.addElement("            ; undefinedHandlingType=ALLOWED");
            br.addElement("        }");
            br.addElement("        map:{...}");
            br.addElement("    }");
            br.addElement("*for your information, the default of undefinedHandlingType is LOGGING");
            br.addItem("Table");
            br.addElement(tableDbName);
            br.addItem("Column");
            br.addElement(columnDbName);
            br.addItem("Classification");
            br.addElement(meta.classificationName());
            final List<Classification> listAll = meta.listAll();
            final StringBuilder sb = new StringBuilder();
            for (Classification cls : listAll) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(cls.name()).append("(").append(cls.code()).append(")");
            }
            br.addElement(sb.toString());
            br.addItem("Undefined Code");
            br.addElement(code);
            final String msg = br.buildExceptionMessage();
            throw new UndefinedClassificationCodeException(msg);
        }

        public static void showUndefinedClassificationCodeMessage(String tableDbName, String columnDbName,
                ClassificationMeta meta, Object code) {
            if (_clsMetaLog.isInfoEnabled()) {
                final String classificationName = meta.classificationName();
                final String exp = tableDbName + "." + columnDbName + "->" + classificationName + "." + code;
                _clsMetaLog.info("*Undefined classification code was set: " + exp); // one line because of many called
            }
        }
    }
}
