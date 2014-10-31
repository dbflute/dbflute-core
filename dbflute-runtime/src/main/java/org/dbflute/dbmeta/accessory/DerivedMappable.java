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

import org.dbflute.exception.SpecifyDerivedReferrerInvalidAliasNameException;
import org.dbflute.exception.SpecifyDerivedReferrerUnknownAliasNameException;
import org.dbflute.exception.SpecifyDerivedReferrerUnmatchedPropertyTypeException;
import org.dbflute.optional.OptionalScalar;

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
     * Find the derived value from derived map by alias name (starts with '$'). <br>
     * It needs to downcast process for getting the value so don't mistake it.s
     * <pre>
     * mapping type:
     *  count()      : <span style="color: #994747">Integer</span>
     *  max(), min() : <span style="color: #994747">(same as property type of the column)</span>
     *  sum(), avg() : <span style="color: #994747">BigDecimal</span>
     *
     * e.g. use count()
     *  member.<span style="color: #CC4747">derived</span>(<span style="color: #2A00FF">"$LOGIN_COUNT"</span>, <span style="color: #994747">Integer.class</span>).alwaysPresent(<span style="color: #553000">loginCount</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *      log(<span style="color: #553000">loginCount</span>.getClass()); <span style="color: #3F7E5E">// is Integer</span>
     *      ...
     *  });
     *
     * e.g. use max()
     *  member.<span style="color: #CC4747">derived</span>(<span style="color: #2A00FF">"$LATEST_PURCHASE_DATETIME"</span>, <span style="color: #994747">LocalDateTime.class</span>).ifPresent(<span style="color: #553000">latestPurchaseDatetime</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *      log(<span style="color: #553000">latestPurchaseDatetime</span>.getClass()); <span style="color: #3F7E5E">// is LocalDateTime</span>
     *      ...
     *  });
     *
     * e.g. overview
     *  String <span style="color: #553000">highestAlias</span> = <span style="color: #2A00FF">"$HIGHEST_PURCHASE_PRICE"</span>;
     *  <span style="color: #0000C0">memberBhv</span>.<span style="color: #994747">selectEntity</span>(<span style="color: #553000">cb</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *      <span style="color: #553000">cb</span>.specify().<span style="color: #994747">derivedPurchase()</span>.max(<span style="color: #553000">purchaseCB</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *          <span style="color: #553000">purchaseCB</span>.specify().columnPurchasePrice();
     *          <span style="color: #553000">purchaseCB</span>.query()...
     *      }, <span style="color: #553000">highestAlias</span>);
     *      <span style="color: #553000">cb</span>.query().setMemberId_Equal(1);
     *      ...
     *  }).alwaysPresent(<span style="color: #553000">member</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *      ... = <span style="color: #553000">member</span>.getMemberName();
     *      member.<span style="color: #CC4747">derived</span>(<span style="color: #553000">highestAlias</span>, <span style="color: #994747">Integer.class</span>).ifPresent(<span style="color: #553000">highestPurchasePrice</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *          log(<span style="color: #553000">highestPurchasePrice</span>);
     *          ...
     *      });
     *  });
     * </pre>
     * @param <VALUE> The type of the value.
     * @param aliasName The alias name of derived-referrer, should start with '$'. (NotNull)
     * @param propertyType The type of the derived property, should match as rule. (NotNull)
     * @return The optional property for derived value found in the map. (NotNull, EmptyAllowed: when null selected)
     * @throws SpecifyDerivedReferrerInvalidAliasNameException When the alias name does not start with '$'.
     * @throws SpecifyDerivedReferrerUnknownAliasNameException When the alias name is unknown, no derived.
     * @throws SpecifyDerivedReferrerUnmatchedPropertyTypeException When the property type is unmatched with actual type.
     */
    <VALUE> OptionalScalar<VALUE> derived(String aliasName, Class<VALUE> propertyType);
}
