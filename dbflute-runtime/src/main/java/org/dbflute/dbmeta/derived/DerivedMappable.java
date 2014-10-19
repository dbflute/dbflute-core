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
package org.dbflute.dbmeta.derived;

/**
 * The interface of derived mappable object (basically entity), for (Specify)DerivedReferrer.
 * @author jflute
 * @since 1.0.5J (2014/06/18 Wednesday)
 */
public interface DerivedMappable {

    /** The prefix mark for derived mapping alias. */
    String MAPPING_ALIAS_PREFIX = "$";

    /**
     * Register value derived by (Specify)DerivedReferrer.
     * @param aliasName The alias name of derived-referrer. (NotNull)
     * @param selectedValue The selected value from database. (NullAllowed)
     */
    void registerDerivedValue(String aliasName, Object selectedValue);

    /**
     * Find the derived value from derived map.
     * <pre>
     * mapping type:
     *  count()      : Integer
     *  max(), min() : (same as property type of the column)
     *  sum(), avg() : BigDecimal
     *
     * e.g. use count()
     *  Integer loginCount = member.derived("$LOGIN_COUNT");
     * </pre>
     * @param <VALUE> The type of the value.
     * @param aliasName The alias name of derived-referrer. (NotNull)
     * @return The derived value found in the map. (NullAllowed: when null selected)
     */
    <VALUE> VALUE derived(String aliasName);
}
