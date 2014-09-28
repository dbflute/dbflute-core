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
package org.seasar.dbflute.logic.generate.language.implstyle;

import org.apache.torque.engine.database.model.Column;
import org.seasar.dbflute.logic.generate.language.typemapping.DfLanguageTypeMappingScala;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 1.0.5F (2014/05/04 Sunday)
 */
public class DfLanguageImplStyleScala implements DfLanguageImplStyle {

    public boolean isDBMetaColumnGetterProperty() {
        return false;
    }

    public boolean isIfCommentExpressionCheckEnabled() {
        return true;
    }

    public boolean isTypedParameterBeanEnabled() {
        return true;
    }

    public String adjustEntitySetMethodCall(String basicSetMethod, boolean calledByThis) {
        return basicSetMethod;
    }

    public String adjustEntitySetPropertyCall(String basicSetMethod, boolean calledByThis) {
        final String removedPrefix = Srl.substringFirstRear(basicSetMethod, "set");
        final String propertyName = Srl.substringFirstFront(removedPrefix, "(");
        final String arg = Srl.extractScopeWide(removedPrefix, "(", ")").getContent();
        return propertyName + " = " + arg;
    }

    public String adjustConditionBeanLocalCQCall(String cb) {
        return cb + ".query()";
    }

    public String adjustConditionQuerySetMethodCall(String basicSetMethod) {
        return basicSetMethod;
    }

    public String getBasicOptionalEntityClass() {
        return "Option";
    }

    public String getRelationOptionalEntityClass() {
        return "Option";
    }

    public boolean isMakeImmutableEntity() {
        return true;
    }

    public String getEntityDBablePrefix() {
        return "Dble";
    }

    public String getEntityMutablePrefix() {
        return "Mble";
    }

    public boolean isImmutablePropertyOptional(Column column) {
        return !column.isNotNull();
    }

    public String adjustImmutablePropertyOptionalType(String immutablePropertyNative) {
        return "Option[" + immutablePropertyNative + "]";
    }

    public String adjustImmutablePropertyOptionalValue(String nativeExp) {
        return "Option(" + nativeExp + ")";
    }

    public String adjustImmutablePropertyOptionalOrElseNull(String immutablePropertyNative, String variable) {
        final String exp;
        if (immutablePropertyNative.equals(DfLanguageTypeMappingScala.SCALA_NATIVE_INTEGER)) {
            exp = variable + ".map(int2Integer(_)).orNull";
        } else if (immutablePropertyNative.equals(DfLanguageTypeMappingScala.SCALA_NATIVE_LONG)) {
            exp = variable + ".map(long2Long(_)).orNull";
        } else if (immutablePropertyNative.equals(DfLanguageTypeMappingScala.SCALA_NATIVE_BOOLEAN)) {
            exp = variable + ".map(boolean2Boolean(_)).orNull";
        } else {
            exp = variable + ".orNull";
        }
        return exp;
    }

    public boolean isCompatibleBeforeJava8() {
        return false;
    }

    public boolean isEntityDerivedMappable() {
        return true;
    }

    public boolean isOutsideSqlCursorGenericVoidable() {
        return false;
    }
}