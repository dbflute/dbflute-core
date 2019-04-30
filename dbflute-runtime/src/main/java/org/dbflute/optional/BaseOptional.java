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
package org.dbflute.optional;

import java.io.Serializable;
import java.util.Optional;

/**
 * The base class for optional object.
 * @param <OBJ> The type of wrapped object in the optional object.
 * @author jflute
 * @author pull/23 (2015/03/04 Wednesday)
 * @since 1.0.5F (2014/05/10 Saturday)
 */
public abstract class BaseOptional<OBJ> implements OptionalThing<OBJ>, Serializable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    // basically for optional entity of relation
    // if this is not serializable, must have default constructor even so cannot serialize
    private static final long serialVersionUID = 1L;

    protected static final OptionalThingIfPresentAfter IF_PRESENT_AFTER_EMPTY = noArgLambda -> {};
    protected static final OptionalThingIfPresentAfter IF_PRESENT_AFTER_NONE_FIRE = noArgLambda -> {
        if (noArgLambda == null) {
            String msg = "The argument 'noArgLambda' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        noArgLambda.process();
    };

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The wrapped object for this optional object. (NullAllowed) */
    protected final OBJ _obj;

    /** The exception thrower e.g. when wrapped object is not found. (NotNull) */
    protected final OptionalThingExceptionThrower _thrower;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public BaseOptional(OBJ obj, OptionalThingExceptionThrower thrower) { // basically called by DBFlute
        _obj = obj; // may be null
        if (thrower == null) {
            String msg = "The argument 'thrower' should not be null: obj=" + obj;
            throw new IllegalArgumentException(msg);
        }
        _thrower = thrower;
    }

    // ===================================================================================
    //                                                                     Object Handling
    //                                                                     ===============
    // -----------------------------------------------------
    //                                               Present
    //                                               -------
    /**
     * Is the wrapped object present? (existing?)
     * @return The determination, true or false.
     */
    protected boolean exists() {
        return _obj != null;
    }

    /**
     * @param objLambda The callback interface to consume the wrapped value. (NotNull)
     */
    protected void callbackAlwaysPresent(OptionalThingConsumer<OBJ> objLambda) {
        if (objLambda == null) {
            String msg = "The argument 'objLambda' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (_obj == null) {
            _thrower.throwNotFoundException();
        }
        objLambda.accept(_obj);
    }

    /**
     * @param consumer The callback interface to consume the wrapped object. (NotNull)
     * @return The handler of after process when if not present. (NotNull)
     */
    protected OptionalThingIfPresentAfter callbackIfPresent(OptionalThingConsumer<OBJ> consumer) {
        if (consumer == null) {
            String msg = "The argument 'consumer' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (exists()) {
            consumer.accept(_obj);
            return IF_PRESENT_AFTER_EMPTY;
        } else {
            return IF_PRESENT_AFTER_NONE_FIRE;
        }
    }

    // -----------------------------------------------------
    //                                                 get()
    //                                                 -----
    /**
     * @return The object instance wrapped in this optional object. (NotNull)
     */
    protected OBJ directlyGet() {
        if (!exists()) {
            _thrower.throwNotFoundException();
        }
        return _obj;
    }

    /**
     * @param other The other instance to be returned if null. (NullAllowed: if null, returns null when entity is null)
     * @return The object instance wrapped in this optional object or specified value. (NullAllowed: if null specified)
     */
    protected OBJ directlyGetOrElse(OBJ other) {
        return exists() ? _obj : other;
    }

    /**
     * @param supplier The supplier of other instance if null. (NotNull)
     * @return The object instance wrapped in this optional object or specified value. (NullAllowed: if null specified)
     */
    protected OBJ directlyGetOrElseGet(OptionalThingSupplier<OBJ> supplier) {
        if (supplier == null) {
            String msg = "The argument 'supplier' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        return exists() ? _obj : supplier.get();
    }

    /**
     * @param <CAUSE> The type of cause.
     * @param supplier The supplier of exception if null. (NotNull)
     * @return The object instance wrapped in this optional object. (NotNull: if null, exception)
     * @throws CAUSE When the value is null.
     */
    protected <CAUSE extends Throwable> OBJ directlyGetOrElseThrow(OptionalThingSupplier<? extends CAUSE> supplier) throws CAUSE {
        if (supplier == null) {
            String msg = "The argument 'supplier' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (exists()) {
            return directlyGet();
        } else {
            throw supplier.get();
        }
    }

    /**
     * @param <CAUSE> The type of original cause.
     * @param <TRANSLATED> The type of translated cause.
     * @param translator The translator function of exception if null. (NotNull)
     * @return The object instance wrapped in this optional object. (NotNull: if null, exception)
     * @throws TRANSLATED When the value is null.
     */
    protected <CAUSE extends Throwable, TRANSLATED extends Throwable> OBJ directlyGetOrElseTranslatingThrow(
            OptionalThingFunction<CAUSE, TRANSLATED> translator) throws TRANSLATED {
        if (translator == null) {
            String msg = "The argument 'supplier' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        try {
            return directlyGet();
        } catch (Throwable e) {
            @SuppressWarnings("unchecked")
            final CAUSE cause = (CAUSE) e;
            throw translator.apply(cause);
        }
    }

    // -----------------------------------------------------
    //                                              filter()
    //                                              --------
    /**
     * @param mapper The callback interface to apply. (NotNull)
     * @return The optional thing as mapped result. (NotNull, EmptyOptionalAllowed: if not present or callback returns null)
     */
    protected OptionalThing<OBJ> callbackFilter(OptionalThingPredicate<OBJ> mapper) {
        if (mapper == null) {
            String msg = "The argument 'mapper' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (exists()) {
            if (mapper.test(_obj)) {
                return this;
            } else {
                return createOptionalFilteredObject(null);
            }
        } else {
            return createOptionalFilteredObject(null);
        }
    }

    /**
     * @param <ARG> The type of value for optional thing.
     * @param obj The plain object for the optional thing. (NullAllowed: if null, return s empty optional)
     * @return The new-created instance of optional thing. (NotNull)
     */
    protected abstract <ARG> OptionalThing<ARG> createOptionalFilteredObject(ARG obj);

    // -----------------------------------------------------
    //                                                 map()
    //                                                 -----
    /**
     * @param <RESULT> The type of mapping result.
     * @param mapper The callback interface to apply, null return allowed as empty. (NotNull)
     * @return The optional object as mapped result. (NotNull, EmptyOptionalAllowed: if not present or callback returns null)
     */
    protected <RESULT> OptionalThing<RESULT> callbackMapping(OptionalThingFunction<? super OBJ, ? extends RESULT> mapper) {
        if (mapper == null) {
            String msg = "The argument 'mapper' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        final RESULT result = exists() ? mapper.apply(_obj) : null;
        return createOptionalMappedObject(result);
    }

    /**
     * @param <ARG> The type of value for optional thing.
     * @param obj The plain object for the optional thing. (NullAllowed: if null, return s empty optional)
     * @return The new-created instance of optional thing. (NotNull)
     */
    protected abstract <ARG> OptionalThing<ARG> createOptionalMappedObject(ARG obj);

    /**
     * @param <RESULT> The type of mapping result.
     * @param mapper The callback interface to apply, cannot return null. (NotNull)
     * @return The optional thing as flat-mapped result. (NotNull, EmptyOptionalAllowed: when not present)
     */
    protected <RESULT> OptionalThing<RESULT> callbackFlatMapping(OptionalThingFunction<? super OBJ, OptionalThing<RESULT>> mapper) {
        if (mapper == null) {
            String msg = "The argument 'mapper' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        // pull request, thanks! https://github.com/dbflute/dbflute-core/pull/23
        if (exists()) {
            final OptionalThing<RESULT> applied = mapper.apply(_obj);
            if (applied == null) { // same specification as official optional's flatMap
                String msg = "The function of your flatMap returned null: function=" + mapper;
                throw new IllegalStateException(msg);
            } else {
                return applied;
            }
        } else {
            return createOptionalFlatMappedObject(null);
        }
    }

    /**
     * @param <ARG> The type of value for optional thing.
     * @param obj The plain object for the optional thing. (NullAllowed: if null, return s empty optional)
     * @return The new-created instance of optional thing. (NotNull)
     */
    protected abstract <ARG> OptionalThing<ARG> createOptionalFlatMappedObject(ARG obj);

    // ===================================================================================
    //                                                                   Standard Optional
    //                                                                   =================
    /** {@inheritDoc} */
    public Optional<OBJ> toOptional() {
        return Optional.ofNullable(directlyGetOrElse(null));
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public int hashCode() {
        return _obj != null ? _obj.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass().isInstance(obj)) {
            final BaseOptional<?> other = (BaseOptional<?>) obj;
            if (_obj != null) {
                return _obj.equals(other.directlyGet());
            } else { // null v.s. null?
                return !other.exists();
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "opt:{" + (_obj != null ? _obj.toString() : "null") + "}";
    }
}
