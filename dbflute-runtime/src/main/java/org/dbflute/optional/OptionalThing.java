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
package org.dbflute.optional;

import org.dbflute.exception.EntityAlreadyDeletedException;

/**
 * @param <THING> The type of thing.
 * @author jflute
 * @since 1.0.5F (2014/05/05 Monday)
 */
public class OptionalThing<THING> extends BaseOptional<THING> {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final OptionalThing<Object> EMPTY_INSTANCE;
    static {
        EMPTY_INSTANCE = new OptionalThing<Object>(null, new OptionalThingExceptionThrower() {
            public void throwNotFoundException() {
                String msg = "The empty optional so the value is null.";
                throw new IllegalStateException(msg);
            }
        });
    }
    protected static final OptionalThingExceptionThrower NOWAY_THROWER = new OptionalThingExceptionThrower() {
        public void throwNotFoundException() {
            throw new IllegalStateException("no way");
        }
    };

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * @param thing The wrapped instance of thing. (NullAllowed)
     * @param thrower The exception thrower when illegal access. (NotNull)
     */
    public OptionalThing(THING thing, OptionalThingExceptionThrower thrower) { // basically called by DBFlute
        super(thing, thrower);
    }

    /**
     * @param <EMPTY> The type of empty optional thing.
     * @return The fixed instance as empty. (NotNull)
     */
    @SuppressWarnings("unchecked")
    public static <EMPTY> OptionalThing<EMPTY> empty() {
        return (OptionalThing<EMPTY>) EMPTY_INSTANCE;
    }

    /**
     * @param <OBJ> The type of object wrapped in the optional thing.
     * @param object The wrapped thing which is optional. (NotNull)
     * @return The new-created instance as existing optional thing. (NotNull)
     */
    public static <OBJ> OptionalThing<OBJ> of(OBJ object) {
        if (object == null) {
            String msg = "The argument 'object' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        return new OptionalThing<OBJ>(object, NOWAY_THROWER);
    }

    /**
     * @param <OBJ> The type of object wrapped in the optional thing.
     * @param object The wrapped instance or thing. (NullAllowed)
     * @param noArgLambda The callback for exception when illegal access. (NotNull)
     * @return The new-created instance as existing or empty optional object. (NotNull)
     */
    public static <OBJ> OptionalThing<OBJ> ofNullable(OBJ object, OptionalThingExceptionThrower noArgLambda) {
        if (object != null) {
            return of(object);
        } else {
            return new OptionalThing<OBJ>(object, noArgLambda);
        }
    }

    // ===================================================================================
    //                                                                   Standard Handling
    //                                                                   =================
    /**
     * Handle the wrapped thing if it is present. <br />
     * You should call this if null object handling is unnecessary (do nothing if null). <br />
     * If exception is preferred when null object, use required().
     * @param oneArgLambda The callback interface to consume the optional object. (NotNull)
     * @return The handler of after process when if not present. (NotNull)
     */
    public OptionalThingIfPresentAfter ifPresent(OptionalThingConsumer<THING> oneArgLambda) {
        assertOneArgLambdaNotNull(oneArgLambda);
        return callbackIfPresent(oneArgLambda);
    }

    /**
     * Is the object instance present? (existing?)
     * @return The determination, true or false.
     */
    public boolean isPresent() {
        return exists();
    }

    /**
     * Get the thing or exception if null.
     * @return The instance of the wrapped thing. (NotNull)
     * @exception EntityAlreadyDeletedException When the object instance wrapped in this optional object is null, which means object has already been deleted (point is not found).
     */
    public THING get() {
        return directlyGet();
    }

    /**
     * @param other The object instance to be returned when the optional is empty. (NullAllowed)
     * @return The wrapped instance or specified other object. (NullAllowed:)
     */
    public THING orElse(THING other) {
        return directlyGetOrElse(other);
    }

    /**
     * Filter the object by the predicate.
     * @param oneArgLambda The callback to predicate whether the object is remained. (NotNull)
     * @return The filtered optional object, might be empty. (NotNull)
     */
    public OptionalThing<THING> filter(OptionalThingPredicate<THING> oneArgLambda) {
        assertOneArgLambdaNotNull(oneArgLambda);
        return (OptionalThing<THING>) callbackFilter(oneArgLambda);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected <ARG> OptionalThing<ARG> createOptionalFilteredObject(ARG obj) {
        return new OptionalThing<ARG>(obj, _thrower);
    }

    /**
     * Apply the mapping of object to result object.
     * @param <RESULT> The type of mapping result.
     * @param oneArgLambda The callback interface to apply. (NotNull)
     * @return The optional object as mapped result. (NotNull, EmptyOptionalAllowed: if not present or callback returns null)
     */
    @SuppressWarnings("unchecked")
    public <RESULT> OptionalThing<RESULT> map(OptionalThingFunction<? super THING, ? extends RESULT> oneArgLambda) {
        assertOneArgLambdaNotNull(oneArgLambda);
        return (OptionalThing<RESULT>) callbackMapping(oneArgLambda); // downcast allowed because factory is overridden
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected <ARG> OptionalThing<ARG> createOptionalMappedObject(ARG obj) {
        return new OptionalThing<ARG>(obj, _thrower);
    }

    /**
     * Apply the flat-mapping of object to result object.
     * @param <RESULT> The type of mapping result.
     * @param oneArgLambda The callback interface to apply. (NotNull)
     * @return The optional object as mapped result. (NotNull, EmptyOptionalAllowed: if not present or callback returns null)
     */
    public <RESULT> OptionalThing<RESULT> flatMap(OptionalThingFunction<? super THING, OptionalThing<RESULT>> oneArgLambda) {
        assertOneArgLambdaNotNull(oneArgLambda);
        return callbackFlatMapping(oneArgLambda);
    }

    // TODO jflute impl: OptionalThing.orElseThrow(Supplier) and others...

    // ===================================================================================
    //                                                                   DBFlute Extension
    //                                                                   =================
    /**
     * Handle the object in the optional thing or exception if not present.
     * @param oneArgLambda The callback interface to consume the optional object. (NotNull)
     * @exception EntityAlreadyDeletedException When the object instance wrapped in this optional object is null, which means object has already been deleted (point is not found).
     */
    public void alwaysPresent(OptionalThingConsumer<THING> oneArgLambda) {
        assertOneArgLambdaNotNull(oneArgLambda);
        callbackAlwaysPresent(oneArgLambda);
    }

    /**
     * Get the object instance or null if not present.
     * @return The object instance wrapped in this optional object or null. (NullAllowed: if not present)
     * @deprecated basically use ifPresent() if might be not present, this is for emergency
     */
    public THING orElseNull() {
        return directlyGetOrElse(null);
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    protected void assertOneArgLambdaNotNull(Object oneArgLambda) {
        if (oneArgLambda == null) {
            throw new IllegalArgumentException("The argument 'oneArgLambda' should not be null.");
        }
    }
}
