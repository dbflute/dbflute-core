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

import java.util.stream.Stream;

import org.dbflute.helper.function.IndependentProcessor;

/**
 * @param <OBJ> The type of object.
 * @author jflute
 * @since 1.1.0-sp1 (2015/01/19 Monday)
 */
public class OptionalObject<OBJ> extends BaseOptional<OBJ> {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final long serialVersionUID = 1L; // basically cannot use (for optional entity)

    protected static final OptionalObject<Object> EMPTY_INSTANCE;
    static {
        EMPTY_INSTANCE = new OptionalObject<Object>(null, () -> {
            String msg = "The empty optional so the value is null.";
            throw new IllegalStateException(msg);
        });
    }
    protected static final OptionalThingExceptionThrower NOWAY_THROWER = () -> {
        throw new IllegalStateException("no way");
    };

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * @param thing The wrapped instance of thing. (NullAllowed)
     * @param thrower The exception thrower when illegal access. (NotNull)
     */
    public OptionalObject(OBJ thing, OptionalThingExceptionThrower thrower) { // basically called by DBFlute
        super(thing, thrower);
    }

    /**
     * @param <EMPTY> The type of empty optional object.
     * @return The fixed instance as empty. (NotNull)
     */
    @SuppressWarnings("unchecked")
    public static <EMPTY> OptionalObject<EMPTY> empty() {
        return (OptionalObject<EMPTY>) EMPTY_INSTANCE;
    }

    /**
     * @param <OBJ> The type of object wrapped in the optional object.
     * @param object The wrapped thing which is optional. (NotNull)
     * @return The new-created instance as existing optional object. (NotNull)
     */
    public static <OBJ> OptionalObject<OBJ> of(OBJ object) {
        if (object == null) {
            String msg = "The argument 'object' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        return new OptionalObject<OBJ>(object, NOWAY_THROWER);
    }

    /**
     * @param <OBJ> The type of object wrapped in the optional object.
     * @param object The wrapped instance or thing. (NullAllowed)
     * @param noArgLambda The callback for exception when illegal access. (NotNull)
     * @return The new-created instance as existing or empty optional object. (NotNull)
     */
    public static <OBJ> OptionalObject<OBJ> ofNullable(OBJ object, OptionalThingExceptionThrower noArgLambda) {
        if (object != null) {
            return of(object);
        } else {
            return new OptionalObject<OBJ>(object, noArgLambda);
        }
    }

    // ===================================================================================
    //                                                                   Standard Handling
    //                                                                   =================
    // -----------------------------------------------------
    //                                             ifPresent
    //                                             ---------
    /** {@inheritDoc} */
    public OptionalThingIfPresentAfter ifPresent(OptionalThingConsumer<? super OBJ> oneArgLambda) {
        assertOneArgLambdaNotNull(oneArgLambda);
        return callbackIfPresent(oneArgLambda);
    }

    /** {@inheritDoc} */
    public void ifPresentOrElse(OptionalThingConsumer<? super OBJ> oneArgLambda, IndependentProcessor noArgLambda) {
        assertOneArgLambdaNotNull(oneArgLambda);
        assertNoArgLambdaNotNull(noArgLambda);
        callbackIfPresentOrElse(oneArgLambda, noArgLambda);
    }

    // -----------------------------------------------------
    //                                         isPresent/get
    //                                         -------------
    /** {@inheritDoc} */
    public boolean isPresent() {
        return determinePresent();
    }

    /** {@inheritDoc} */
    public boolean isEmpty() {
        return determineEmpty();
    }

    /** {@inheritDoc} */
    public OBJ get() {
        return directlyGet();
    }

    // -----------------------------------------------------
    //                                             or/orElse
    //                                             ---------
    /** {@inheritDoc} */
    public OptionalThing<OBJ> or(OptionalThingSupplier<? extends OptionalThing<? extends OBJ>> noArgLambda) {
        assertNoArgLambdaNotNull(noArgLambda);
        return callbackOr(noArgLambda);
    }

    /** {@inheritDoc} */
    public OBJ orElse(OBJ other) {
        return directlyGetOrElse(other);
    }

    /** {@inheritDoc} */
    public OBJ orElseGet(OptionalThingSupplier<? extends OBJ> noArgLambda) {
        assertNoArgLambdaNotNull(noArgLambda);
        return callbackGetOrElseGet(noArgLambda);
    }

    /** {@inheritDoc} */
    @Override
    public <CAUSE extends Throwable> OBJ orElseThrow(OptionalThingSupplier<? extends CAUSE> noArgLambda) throws CAUSE {
        assertNoArgLambdaNotNull(noArgLambda);
        return callbackGetOrElseThrow(noArgLambda);
    }

    /** {@inheritDoc} */
    public <CAUSE extends Throwable, TRANSLATED extends Throwable> OBJ orElseTranslatingThrow(
            OptionalThingFunction<CAUSE, TRANSLATED> causeLambda) throws TRANSLATED {
        assertCauseLambdaNotNull(causeLambda);
        return callbackGetOrElseTranslatingThrow(causeLambda);
    }

    // -----------------------------------------------------
    //                                            filter/map
    //                                            ----------
    /** {@inheritDoc} */
    public OptionalObject<OBJ> filter(OptionalThingPredicate<? super OBJ> oneArgLambda) {
        assertOneArgLambdaNotNull(oneArgLambda);
        return (OptionalObject<OBJ>) callbackFilter(oneArgLambda);
    }

    /** {@inheritDoc} */
    @Override
    protected <ARG> OptionalObject<ARG> createOptionalFilteredObject(ARG obj) {
        return new OptionalObject<ARG>(obj, _thrower);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public <RESULT> OptionalThing<RESULT> map(OptionalThingFunction<? super OBJ, ? extends RESULT> oneArgLambda) {
        assertOneArgLambdaNotNull(oneArgLambda);
        return (OptionalThing<RESULT>) callbackMapping(oneArgLambda); // downcast allowed because factory is overridden
    }

    /** {@inheritDoc} */
    @Override
    protected <ARG> OptionalObject<ARG> createOptionalMappedObject(ARG obj) {
        return new OptionalObject<ARG>(obj, _thrower);
    }

    /** {@inheritDoc} */
    public <RESULT> OptionalThing<RESULT> flatMap(OptionalThingFunction<? super OBJ, OptionalThing<RESULT>> oneArgLambda) {
        assertOneArgLambdaNotNull(oneArgLambda);
        return callbackFlatMapping(oneArgLambda);
    }

    /** {@inheritDoc} */
    @Override
    protected <ARG> OptionalObject<ARG> createOptionalFlatMappedObject(ARG obj) {
        return new OptionalObject<ARG>(obj, _thrower);
    }

    // -----------------------------------------------------
    //                                                stream
    //                                                ------
    /** {@inheritDoc} */
    public Stream<OBJ> stream() {
        return convertToStream();
    }

    // ===================================================================================
    //                                                                   DBFlute Extension
    //                                                                   =================
    /** {@inheritDoc} */
    public void alwaysPresent(OptionalThingConsumer<? super OBJ> oneArgLambda) {
        assertOneArgLambdaNotNull(oneArgLambda);
        callbackAlwaysPresent(oneArgLambda);
    }
}
