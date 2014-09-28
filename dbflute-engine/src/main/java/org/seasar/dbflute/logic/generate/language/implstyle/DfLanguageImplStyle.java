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

/**
 * @author jflute
 * @since 1.0.5F (2014/05/04 Sunday)
 */
public interface DfLanguageImplStyle {

    /**
     * @return Is the column getter of DBMeta defined as property?
     */
    boolean isDBMetaColumnGetterProperty();

    boolean isIfCommentExpressionCheckEnabled();

    boolean isTypedParameterBeanEnabled();

    String adjustEntitySetMethodCall(String basicSetMethod, boolean calledByThis);

    String adjustEntitySetPropertyCall(String basicSetMethod, boolean calledByThis);

    String adjustConditionBeanLocalCQCall(String cb);

    String adjustConditionQuerySetMethodCall(String basicSetMethod);

    String getBasicOptionalEntityClass();

    String getRelationOptionalEntityClass();

    boolean isMakeImmutableEntity();

    String getEntityDBablePrefix();

    String getEntityMutablePrefix();

    boolean isImmutablePropertyOptional(Column column);

    String adjustImmutablePropertyOptionalType(String immutablePropertyNative);

    String adjustImmutablePropertyOptionalValue(String nativeExp);

    String adjustImmutablePropertyOptionalOrElseNull(String immutablePropertyNative, String variable);

    boolean isCompatibleBeforeJava8();

    boolean isEntityDerivedMappable(); // as default of DBFlute property

    boolean isOutsideSqlCursorGenericVoidable();
}