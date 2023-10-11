/*
 * Copyright 2014-2023 the original author or authors.
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
package org.dbflute;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.dbflute.dbmeta.accessory.EntityModifiedProperties;
import org.dbflute.exception.CharParameterShortSizeException;
import org.dbflute.exception.NonSpecifiedColumnAccessException;
import org.dbflute.exception.UndefinedClassificationCodeException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.jdbc.Classification;
import org.dbflute.jdbc.ClassificationMeta;
import org.dbflute.jdbc.ClassificationUndefinedHandlingType;
import org.dbflute.jdbc.ShortCharHandlingMode;
import org.dbflute.system.DBFluteSystem;
import org.dbflute.util.DfTypeUtil;
import org.dbflute.util.Srl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The FunCustodial provides small services to your entities. <br>
 * DBFluteConfig uses this so don't rename this class easyly.
 * @author jflute
 * @since 1.1.0 (2014/10/18 Saturday)
 */
public class FunCustodial {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** The instance of logging object for general use. (NotNull) */
    private static final Logger _log = LoggerFactory.getLogger(FunCustodial.class);

    /** The instance of logging object for classification meta. (NotNull) */
    private static final Logger _clsMetaLog = LoggerFactory.getLogger(ClassificationMeta.class);

    // -----------------------------------------------------
    //                                                Option
    //                                                ------
    // no volatile, because basically it changes before application open (and not severe logic)
    protected static boolean _nonSpecifiedColumnAccessWarningOnly; // since 1.2.7
    protected static boolean _locked = true; // since 1.2.7

    // ===================================================================================
    //                                                                    Value Conversion
    //                                                                    ================
    public static String toClassTitle(Object entity) {
        return DfTypeUtil.toClassTitle(entity);
    }

    @SuppressWarnings("unchecked")
    public static <NUMBER extends Number> NUMBER toNumber(Object obj, Class<NUMBER> type) {
        return (NUMBER) DfTypeUtil.toNumber(obj, type);
    }

    public static Boolean toBoolean(Object obj) {
        return DfTypeUtil.toBoolean(obj);
    }

    public static String toStringDate(Date date, TimeZone timeZone, String pattern) {
        if (date == null) {
            return null;
        }
        final TimeZone realZone = timeZone != null ? timeZone : DBFluteSystem.getFinalTimeZone();
        final Locale realLocale = DBFluteSystem.getFinalLocale(); // no specified because of basically debug string
        final String str = DfTypeUtil.toStringDate(date, realZone, pattern, realLocale);
        return (DfTypeUtil.isDateBC(date) ? "BC" : "") + str;
    }

    public static String toStringBytes(byte[] bytes) {
        return "byte[" + (bytes != null ? String.valueOf(bytes.length) : "null") + "]";
    }

    public static String convertEmptyToNull(String value) {
        return (value != null && value.length() == 0) ? null : value;
    }

    // -----------------------------------------------------
    //                                       Short Character
    //                                       ---------------
    /**
     * @param parameterName The name of the parameter. (NotNull)
     * @param value The value of the parameter. (NullAllowed)
     * @param size The size of parameter type. (NullAllowed)
     * @param mode The handling mode. (NotNull)
     * @return The filtered value. (NullAllowed)
     */
    public static String handleShortChar(String parameterName, String value, Integer size, ShortCharHandlingMode mode) {
        if (parameterName == null || parameterName.trim().length() == 0) {
            String msg = "The argument 'parameterName' should not be null or empty:";
            msg = msg + " value=" + value + " size=" + size + " mode=" + mode;
            throw new IllegalArgumentException(msg);
        }
        if (mode == null) {
            String msg = "The argument 'mode' should not be null:";
            msg = msg + " parameterName=" + parameterName + " value=" + value + " size=" + size;
            throw new IllegalArgumentException(msg);
        }
        if (value == null) {
            return null;
        }
        if (size == null) {
            return value;
        }
        if (value.length() >= size) {
            return value;
        }
        if (mode.equals(ShortCharHandlingMode.RFILL)) {
            return Srl.rfill(value, size);
        } else if (mode.equals(ShortCharHandlingMode.LFILL)) {
            return Srl.lfill(value, size);
        } else if (mode.equals(ShortCharHandlingMode.EXCEPTION)) {
            String msg = "The size of the parameter '" + parameterName + "' should be " + size + ":";
            msg = msg + " value=[" + value + "] size=" + value.length();
            throw new CharParameterShortSizeException(msg);
        } else {
            return value;
        }
    }

    // ===================================================================================
    //                                                                Specified Properties
    //                                                                ====================
    public static void checkSpecifiedProperty(Entity entity, String propertyName, EntityModifiedProperties specifiedProperties) {
        if (specifiedProperties == null) { // means no need to check (e.g. all columns are selected)
            return;
        }
        if (!entity.createdBySelect()) { // basically no way, selected when the properties exists but just in case
            return;
        }
        // SpecifyColumn is used for the entity
        if (specifiedProperties.isModifiedProperty(propertyName)) { // yes, setter is called when select mapping
            return; // OK, OK
        }
        // no, you get the non-specified column, throws exception
        throwNonSpecifiedColumnAccessException(entity, propertyName, specifiedProperties);
    }

    protected static void throwNonSpecifiedColumnAccessException(Entity entity, String propertyName,
            EntityModifiedProperties specifiedProperties) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Non-specified column was accessed.");
        br.addItem("Advice");
        br.addElement("To get non-specified column from entity is not allowd.");
        br.addElement(" #nonSpecifiedColumn_access");
        br.addElement("Non-specified means using SpecifyColumn but the column is non-specified.");
        br.addElement("Mistake? Or specify the column by your condition-bean if you need it.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    memberBhv.selectEntity(cb -> {");
        br.addElement("        cb.specify().columnMemberName();");
        br.addElement("        cb.query().set...");
        br.addElement("    }).alwaysPresent(member -> {");
        br.addElement("        ... = member.getMemberName();");
        br.addElement("        ... = member.getBirthdate(); // *NG: exception");
        br.addElement("    });");
        br.addElement("  (o):");
        br.addElement("    memberBhv.selectEntity(cb -> {");
        br.addElement("        cb.specify().columnMemberName();");
        br.addElement("        cb.specify().columnBirthdate(); // *Point");
        br.addElement("        cb.query().set...");
        br.addElement("    }).alwaysPresent(member -> {");
        br.addElement("        ... = member.getMemberName();");
        br.addElement("        ... = member.getBirthdate(); // OK");
        br.addElement("    });");
        br.addElement("");
        br.addElement("While, reluctantly you need to get the column without change conditions,");
        br.addElement("you can enable non-specified column access by the condition-bean option.");
        br.addElement("The method is cb.enable...()");
        buildExceptionTableInfo(br, entity);
        br.addItem("Non-Specified and Accessed");
        br.addElement(propertyName);
        br.addItem("Specified Column in the Table");
        br.addElement(specifiedProperties);
        final String msg = br.buildExceptionMessage();
        final NonSpecifiedColumnAccessException ex = new NonSpecifiedColumnAccessException(msg);
        if (_nonSpecifiedColumnAccessWarningOnly) { // since 1.2.7
            // #for_now jflute too difficult to implement the option by smart way (2023/07/17)
            _log.warn("Treated as warning only. See the exception message.", ex);
        } else {
            throw ex;
        }
    }

    protected static void buildExceptionTableInfo(ExceptionMessageBuilder br, Entity entity) {
        br.addItem("Table");
        br.addElement(entity.asTableDbName());
        try {
            br.addElement(entity.asDBMeta().extractPrimaryKeyMap(entity));
        } catch (RuntimeException continued) { // just in case
            br.addElement("*Failed to get PK info:");
            br.addElement(continued.getMessage());
        }
    }

    // ===================================================================================
    //                                                                      Classification
    //                                                                      ==============
    public static void checkClassificationCode(Entity entity, String columnDbName, ClassificationMeta meta, Object code) {
        if (code == null) {
            return;
        }
        final ClassificationUndefinedHandlingType undefinedHandlingType = meta.undefinedHandlingType();
        if (!undefinedHandlingType.isChecked()) { // basically no way (not called if no check)
            return;
        }
        if (meta.of(code).isPresent()) {
            return;
        }
        final boolean allowedByOption = entity.myundefinedClassificationAccessAllowed();
        handleUndefinedClassificationCode(entity.asTableDbName(), columnDbName, meta, code, allowedByOption);
    }

    public static void handleUndefinedClassificationCode(String tableDbName, String columnDbName, ClassificationMeta meta, Object code,
            boolean allowedByOption) {
        final ClassificationUndefinedHandlingType undefinedHandlingType = meta.undefinedHandlingType();
        if (ClassificationUndefinedHandlingType.EXCEPTION.equals(undefinedHandlingType)) {
            if (allowedByOption) { // e.g. ConditionBean's option
                showUndefinedClassificationCodeMessage(tableDbName, columnDbName, meta, code); // logging at least
            } else { // normally here
                throwUndefinedClassificationCodeException(tableDbName, columnDbName, meta, code);
            }
        } else if (ClassificationUndefinedHandlingType.LOGGING.equals(undefinedHandlingType)) {
            showUndefinedClassificationCodeMessage(tableDbName, columnDbName, meta, code);
        }
        // else means ALLOWED
    }

    protected static void throwUndefinedClassificationCodeException(String tableDbName, String columnDbName, ClassificationMeta meta,
            Object code) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Undefined classification code was set to the entity.");
        br.addItem("Advice");
        br.addElement("Confirm the value of the classification column on your database,");
        br.addElement("or setting value to your entity.");
        br.addElement(" #undefined_cls");
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

    public static void showUndefinedClassificationCodeMessage(String tableDbName, String columnDbName, ClassificationMeta meta,
            Object code) {
        if (_clsMetaLog.isInfoEnabled()) {
            final String classificationName = meta.classificationName();
            final String exp = classificationName + "." + code + " of " + tableDbName + "." + columnDbName;
            _clsMetaLog.info("*Undefined classification code #undefined_cls was set: " + exp); // one line because of many called
        }
    }

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    //                                          ------------
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

    // ===================================================================================
    //                                                                   Option Adjustment
    //                                                                   =================
    // -----------------------------------------------------
    //                                  Non-Specified Column
    //                                  --------------------
    // since 1.2.7
    public static boolean isNonSpecifiedColumnAccessWarningOnly() {
        return _nonSpecifiedColumnAccessWarningOnly;
    }

    public static void setNonSpecifiedColumnAccessWarningOnly(boolean nonSpecifiedColumnAccessWarningOnly) {
        assertUnlocked();
        if (_log.isInfoEnabled()) {
            _log.info("...Setting nonSpecifiedColumnAccessWarningOnly: " + nonSpecifiedColumnAccessWarningOnly);
        }
        _nonSpecifiedColumnAccessWarningOnly = nonSpecifiedColumnAccessWarningOnly;
        lock(); // auto-lock here, because of deep world
    }

    // ===================================================================================
    //                                                                         Option Lock
    //                                                                         ===========
    // since 1.2.7
    public static boolean isLocked() {
        return _locked;
    }

    public static void lock() {
        if (_locked) {
            return;
        }
        if (_log.isInfoEnabled()) {
            _log.info("...Locking the object for fun-custodial!");
        }
        _locked = true;
    }

    public static void unlock() {
        if (!_locked) {
            return;
        }
        if (_log.isInfoEnabled()) {
            _log.info("...Unlocking the object for fun-custodial!");
        }
        _locked = false;
    }

    protected static void assertUnlocked() {
        if (!isLocked()) {
            return;
        }
        throw new IllegalStateException("The fun-custodial is locked.");
    }
}
