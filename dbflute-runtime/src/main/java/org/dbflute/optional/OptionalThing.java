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
package org.dbflute.optional;

import java.util.Optional;
import java.util.stream.Stream;

import org.dbflute.exception.EntityAlreadyDeletedException;
import org.dbflute.helper.function.IndependentProcessor;

/**
 * @param <THING> The type of thing.
 * @author jflute
 * @since 1.0.5F (2014/05/05 Monday)
 */
public interface OptionalThing<THING> {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    // using as object
    /**
     * @param <EMPTY> The type of empty optional thing.
     * @return The fixed instance as empty. (NotNull)
     */
    public static <EMPTY> OptionalThing<EMPTY> empty() {
        return OptionalObject.empty();
    }

    /**
     * @param <THING> The type of thing wrapped in the optional thing.
     * @param object The wrapped thing which is optional. (NotNull)
     * @return The new-created instance as existing optional thing. (NotNull)
     */
    public static <THING> OptionalThing<THING> of(THING object) {
        return OptionalObject.of(object);
    }

    /**
     * @param <THING> The type of thing wrapped in the optional thing.
     * @param object The wrapped instance or thing. (NullAllowed)
     * @param noArgLambda The callback for exception when illegal access. (NotNull)
     * @return The new-created instance as existing or empty optional thing. (NotNull)
     */
    public static <THING> OptionalThing<THING> ofNullable(THING object, OptionalThingExceptionThrower noArgLambda) {
        return OptionalObject.ofNullable(object, noArgLambda);
    }

    /**
     * @param <THING> The type of thing wrapped in the optional thing.
     * @param java8opt The optional instance as Java8 standard optional. (NotNull)
     * @param noArgLambda The callback for exception when illegal access. (NotNull)
     * @return The new-created instance as existing or empty optional thing. (NotNull)
     */
    public static <THING> OptionalThing<THING> migratedFrom(Optional<THING> java8opt, OptionalThingExceptionThrower noArgLambda) {
        return OptionalObject.ofNullable(java8opt.orElse(null), noArgLambda);
    }

    /**
     * @param <THING> The type of thing wrapped in the optional thing.
     * @param dfopt The optional instance as optional thing DBFlute provides. (NotNull)
     * @param noArgLambda The callback for exception when illegal access. (NotNull)
     * @return The new-created instance as existing or empty optional thing. (NotNull)
     */
    public static <THING> OptionalThing<THING> translatedFrom(OptionalThing<THING> dfopt, OptionalThingExceptionThrower noArgLambda) {
        return OptionalObject.ofNullable(dfopt.orElse(null), noArgLambda);
    }

    // ===================================================================================
    //                                                                   Standard Handling
    //                                                                   =================
    // -----------------------------------------------------
    //                                             ifPresent
    //                                             ---------
    /**
     * Handle the wrapped thing if it is present. <br>
     * You should call this if empty handling is unnecessary (do nothing if not present). <br>
     * If exception is preferred when null object, use alwaysPresent().
     * @param oneArgLambda The callback interface to consume the optional thing. (NotNull)
     * @return The handler of after process when if not present. (NotNull)
     */
    OptionalThingIfPresentAfter ifPresent(OptionalThingConsumer<? super THING> oneArgLambda);

    /**
     * Handle the wrapped thing if it is both present and empty.
     * @param oneArgLambda The present-case callback interface to consume the optional thing. (NotNull)
     * @param noArgLambda The empty-case callback interface to handle or-else. (NotNull)
     */
    void ifPresentOrElse(OptionalThingConsumer<? super THING> oneArgLambda, IndependentProcessor noArgLambda); // as java9

    // -----------------------------------------------------
    //                                         isPresent/get
    //                                         -------------
    /**
     * Is the object instance present? (existing?)
     * @return The determination, true or false.
     */
    boolean isPresent();

    /**
     * Is the object instance empty? (not existing?)
     * @return The determination, true or false.
     */
    boolean isEmpty(); // as java11

    /**
     * Get the thing or (detail message) exception if not present.
     * @return The instance of the wrapped thing. (NotNull)
     * @throws (exception from thrower) When the object instance wrapped in this optional thing is null, which means object has already been deleted (point is not found).
     */
    THING get();

    // -----------------------------------------------------
    //                                             or/orElse
    //                                             ---------
    /**
     * Switch this to other optional if not present.
     * @param noArgLambda The supplier of other optional if not present. (NotNull)
     * @return this or optional provided by supplier. (NotNull, EmptyAllowed: provided one may be empty)
     */
    OptionalThing<THING> or(OptionalThingSupplier<? extends OptionalThing<? extends THING>> noArgLambda); // as java9

    /**
     * Get the wrapped instance or returns the specified thing.
     * @param other The object instance to be returned when the optional is empty. (NullAllowed)
     * @return The wrapped instance or specified other object. (NullAllowed:)
     */
    THING orElse(THING other);

    /**
     * Get the thing or get from the supplier.
     * @param noArgLambda The supplier of other instance if not present. (NotNull)
     * @return The object instance wrapped in this optional thing or specified value. (NullAllowed: if null specified)
     */
    THING orElseGet(OptionalThingSupplier<? extends THING> noArgLambda);

    /**
     * Get the thing or throw the embedded exception. <br>
     * <span style="color: #AD4747; font-size: 120%">Actually same as get(), similar to alwaysPresent().</span>
     * @return The object instance wrapped in this optional object. (NotNull: if not present, exception)
     * @throws (exception from thrower) When the value is null.
     */
    THING orElseThrow(); // as java10

    /**
     * Get the thing or throw the exception. <br>
     * <span style="color: #AD4747; font-size: 120%">However the orElseTranslatingThrow() is recommended instead this.</span>
     * @param <CAUSE> The type of cause.
     * @param noArgLambda The supplier of exception if not present. (NotNull)
     * @return The object instance wrapped in this optional object. (NotNull: if not present, exception)
     * @throws CAUSE When the value is null.
     */
    <CAUSE extends Throwable> THING orElseThrow(OptionalThingSupplier<? extends CAUSE> noArgLambda) throws CAUSE;

    /**
     * Get the thing or throw the exception with translating the cause.
     * <pre>
     * OptionalThing&lt;String&gt; <span style="color: #553000">optSea</span> = ...
     * String sea = <span style="color: #553000">optSea</span>.<span style="color: #CC4747">orElseTranslatingThrow</span>(<span style="color: #553000">cause</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #70226C">return new</span> YourBusinessException("...", <span style="color: #553000">cause</span>); <span style="color: #3F7E5E">// you can wrap the detail exception</span>
     * }
     * </pre>
     * @param <CAUSE> The type of original cause.
     * @param <TRANSLATED> The type of translated cause.
     * @param causeLambda The translator function of cause exception if not present, returning your exception. (NotNull)
     * @return The object instance wrapped in this optional object. (NotNull: if not present, exception)
     * @throws TRANSLATED When the value is null.
     */
    <CAUSE extends Throwable, TRANSLATED extends Throwable> THING orElseTranslatingThrow(
            OptionalThingFunction<CAUSE, TRANSLATED> causeLambda) throws TRANSLATED;

    // -----------------------------------------------------
    //                                            filter/map
    //                                            ----------
    /**
     * Filter the thing by the predicate.
     * @param oneArgLambda The callback to predicate whether the object is remained. (NotNull)
     * @return The filtered optional thing, might be empty. (NotNull)
     */
    OptionalThing<THING> filter(OptionalThingPredicate<? super THING> oneArgLambda);

    /**
     * Apply the mapping of thing to result thing.
     * @param <RESULT> The type of mapping result.
     * @param oneArgLambda The callback interface to apply, null return allowed as empty. (NotNull)
     * @return The optional thing as mapped result. (NotNull, EmptyOptionalAllowed: if not present or callback returns null)
     */
    <RESULT> OptionalThing<RESULT> map(OptionalThingFunction<? super THING, ? extends RESULT> oneArgLambda);

    /**
     * Apply the flat-mapping of thing to result thing.
     * @param <RESULT> The type of mapping result.
     * @param oneArgLambda The callback interface to apply, cannot return null. (NotNull)
     * @return The optional thing as mapped result. (NotNull, EmptyOptionalAllowed: if not present or callback returns null)
     */
    <RESULT> OptionalThing<RESULT> flatMap(OptionalThingFunction<? super THING, OptionalThing<RESULT>> oneArgLambda);

    // -----------------------------------------------------
    //                                                stream
    //                                                ------
    /**
     * Handle the optional thing as stream.
     * @return The stream for the optional thing, may be empty (if not present). (NotNull, EmptyAllowed: if not present)
     */
    Stream<THING> stream(); // as java9

    // ===================================================================================
    //                                                                   DBFlute Extension
    //                                                                   =================
    /**
     * Handle the thing in the optional thing or (detail message) exception if not present.
     * <pre>
     * <span style="color: #0000C0">memberBhv</span>.<span style="color: #994747">selectEntity</span>(<span style="color: #553000">cb</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> {
     *     <span style="color: #553000">cb</span>.setupSelect_MemberStatus();
     *     <span style="color: #553000">cb</span>.query().setMemberId_Equal(<span style="color: #2A00FF">1</span>);
     * }).<span style="color: #CC4747">alwaysPresent</span>(<span style="color: #553000">member</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> { <span style="color: #3F7E5E">// called if value exists, or exception</span>
     *     ... = <span style="color: #553000">member</span>.getMemberName();
     *     <span style="color: #553000">member</span>.getMemberStatus().<span style="color: #CC4747">alwaysPresent</span>(<span style="color: #553000">status</span> <span style="color: #90226C; font-weight: bold"><span style="font-size: 120%">-</span>&gt;</span> { <span style="color: #3F7E5E">// also relationship</span>
     *         ... = <span style="color: #553000">status</span>.getMemberStatusName();
     *     });
     * });
     * </pre>
     * @param oneArgLambda The callback interface to consume the optional thing. (NotNull)
     * @throws EntityAlreadyDeletedException When the object instance wrapped in this optional thing is null, which means object has already been deleted (point is not found).
     */
    void alwaysPresent(OptionalThingConsumer<? super THING> oneArgLambda);

    // ===================================================================================
    //                                                                 Convert to Optional
    //                                                                 ===================
    /**
     * Convert to Java standard optional class. <br>
     * For only when standard optional handling is needed, so basically you don't use this.
     * @return The new-created instance or empty. (NotNull)
     */
    Optional<THING> toOptional();
}
