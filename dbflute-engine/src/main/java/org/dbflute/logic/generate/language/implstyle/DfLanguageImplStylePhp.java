/*
 * Copyright 2014-2019 the original author or authors.
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
package org.dbflute.logic.generate.language.implstyle;

import org.apache.torque.engine.database.model.Column;

/**
 * @author jflute
 * @since 1.0.5F (2014/05/04 Sunday)
 */
public class DfLanguageImplStylePhp implements DfLanguageImplStyle {

    public boolean isDBMetaColumnGetterProperty() {
        return false;
    }

    public boolean isIfCommentExpressionCheckEnabled() {
        return false;
    }

    public boolean isTypedParameterBeanEnabled() {
        return false;
    }

    public String adjustEntitySetMethodCall(String basicSetMethod, boolean calledByThis) {
        return basicSetMethod;
    }

    public String adjustEntitySetPropertyCall(String basicSetMethod, boolean calledByThis) {
        return basicSetMethod;
    }

    public String adjustConditionBeanLocalCQCall(String cb) {
        return cb + ".query()";
    }

    public String adjustConditionQuerySetMethodCall(String basicSetMethod) {
        return basicSetMethod;
    }

    public String getBasicOptionalEntityClass() {
        return null; // means DBFlute embedded
    }

    public String getRelationOptionalEntityClass() {
        return null; // means DBFlute embedded
    }

    public boolean isMakeImmutableEntity() {
        return false;
    }

    public String getEntityDBablePrefix() {
        return "";
    }

    public String getEntityMutablePrefix() {
        return "";
    }

    public boolean isImmutablePropertyOptional(Column column) {
        return !column.isNotNull();
    }

    public String adjustImmutablePropertyOptionalType(String immutablePropertyNative) {
        return "OptionalProperty<" + immutablePropertyNative + ">";
    }

    public String adjustImmutablePropertyOptionalValue(String nativeExp) {
        return "OptionalProperty.of(" + nativeExp + ")";
    }

    public String adjustImmutablePropertyOptionalOrElseNull(String immutablePropertyNative, String variable) {
        return variable + ".orElseNull()";
    }

    public boolean isCompatibleBeforeJava8() {
        return false;
    }

    public boolean isEntityDerivedMappable() {
        return false;
    }

    public boolean isOutsideSqlCursorGenericVoidable() {
        return false;
    }

    public boolean canUseJava8TimeLocalDate() {
        return false;
    }
}