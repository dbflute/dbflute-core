/*
 * Copyright 2014-2015 the original author or authors.
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

/**
 * @param <SCALAR> The type of scalar.
 * @author jflute
 * @since 1.1.0 (2014/10/28 Tuesday)
 */
public class OptionalScalar<SCALAR> extends BaseOptional<SCALAR> {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final long serialVersionUID = 1L; // basically cannot use (for optional entity)

    protected static final OptionalScalar<Object> EMPTY_INSTANCE;
    static {
        EMPTY_INSTANCE = new OptionalScalar<Object>(null, () -> {
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
    public OptionalScalar(SCALAR thing, OptionalThingExceptionThrower thrower) { // basically called by DBFlute
        super(thing, thrower);
    }

    /**
     * @param <EMPTY> The type of empty optional scalar.
     * @return The fixed instance as empty. (NotNull)
     */
    @SuppressWarnings("unchecked")
    public static <EMPTY> OptionalScalar<EMPTY> empty() {
        return (OptionalScalar<EMPTY>) EMPTY_INSTANCE;
    }

    /**
     * @param <OBJ> The type of object wrapped in the optional scalar.
     * @param object The wrapped thing which is optional. (NotNull)
     * @return The new-created instance as existing optional scalar. (NotNull)
     */
    public static <OBJ> OptionalScalar<OBJ> of(OBJ object) {
        if (object == null) {
            String msg = "The argument 'object' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        return new OptionalScalar<OBJ>(object, NOWAY_THROWER);
    }

    /**
     * @param <OBJ> The type of object wrapped in the optional scalar.
     * @param object The wrapped instance or thing. (NullAllowed)
     * @param noArgLambda The callback for exception when illegal access. (NotNull)
     * @return The new-created instance as existing or empty optional object. (NotNull)
     */
    public static <OBJ> OptionalScalar<OBJ> ofNullable(OBJ object, OptionalThingExceptionThrower noArgLambda) {
        if (object != null) {
            return of(object);
        } else {
            return new OptionalScalar<OBJ>(object, noArgLambda);
        }
    }

    // ===================================================================================
    //                                                                   Standard Handling
    //                                                                   =================
    /** {@inheritDoc} */
    public OptionalThingIfPresentAfter ifPresent(OptionalThingConsumer<SCALAR> oneArgLambda) {
        assertOneArgLambdaNotNull(oneArgLambda);
        return callbackIfPresent(oneArgLambda);
    }

    /** {@inheritDoc} */
    public boolean isPresent() {
        return exists();
    }

    /** {@inheritDoc} */
    public SCALAR get() {
        return directlyGet();
    }

    /** {@inheritDoc} */
    public OptionalScalar<SCALAR> filter(OptionalThingPredicate<SCALAR> oneArgLambda) {
        assertOneArgLambdaNotNull(oneArgLambda);
        return (OptionalScalar<SCALAR>) callbackFilter(oneArgLambda);
    }

    /** {@inheritDoc} */
    @Override
    protected <ARG> OptionalScalar<ARG> createOptionalFilteredObject(ARG obj) {
        return new OptionalScalar<ARG>(obj, _thrower);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public <RESULT> OptionalThing<RESULT> map(OptionalThingFunction<? super SCALAR, ? extends RESULT> oneArgLambda) {
        assertOneArgLambdaNotNull(oneArgLambda);
        return (OptionalThing<RESULT>) callbackMapping(oneArgLambda); // downcast allowed because factory is overridden
    }

    /** {@inheritDoc} */
    @Override
    protected <ARG> OptionalScalar<ARG> createOptionalMappedObject(ARG obj) {
        return new OptionalScalar<ARG>(obj, _thrower);
    }

    /** {@inheritDoc} */
    public <RESULT> OptionalThing<RESULT> flatMap(OptionalThingFunction<? super SCALAR, OptionalThing<RESULT>> oneArgLambda) {
        assertOneArgLambdaNotNull(oneArgLambda);
        return callbackFlatMapping(oneArgLambda);
    }

    /** {@inheritDoc} */
    @Override
    protected <ARG> OptionalScalar<ARG> createOptionalFlatMappedObject(ARG obj) {
        return new OptionalScalar<ARG>(obj, _thrower);
    }

    /** {@inheritDoc} */
    public SCALAR orElse(SCALAR other) {
        return directlyGetOrElse(other);
    }

    /** {@inheritDoc} */
    public SCALAR orElseGet(OptionalThingSupplier<SCALAR> noArgLambda) {
        return directlyGetOrElseGet(noArgLambda);
    }

    /** {@inheritDoc} */
    @Override
    public <CAUSE extends Throwable> SCALAR orElseThrow(OptionalThingSupplier<? extends CAUSE> noArgLambda) throws CAUSE {
        return directlyGetOrElseThrow(noArgLambda);
    }

    // ===================================================================================
    //                                                                   DBFlute Extension
    //                                                                   =================
    /** {@inheritDoc} */
    public void alwaysPresent(OptionalThingConsumer<SCALAR> oneArgLambda) {
        assertOneArgLambdaNotNull(oneArgLambda);
        callbackAlwaysPresent(oneArgLambda);
    }

    /**
     * {@inheritDoc}
     * @deprecated basically use ifPresent() or use orElse(null)
     */
    public SCALAR orElseNull() {
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
