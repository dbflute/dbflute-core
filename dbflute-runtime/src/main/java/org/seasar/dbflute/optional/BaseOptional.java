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
package org.seasar.dbflute.optional;

/**
 * The base class for optional object.
 * @param <OBJ> The type of wrapped object in the optional object.
 * @author jflute
 * @since 1.0.5F (2014/05/10 Saturday)
 */
public abstract class BaseOptional<OBJ> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The wrapped object for this optional object. (NullAllowed) */
    protected final OBJ _obj;

    /** The exception thrower e.g. when wrapped object is not found. (NotNull) */
    protected final OptionalObjectExceptionThrower _thrower;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public BaseOptional(OBJ obj, OptionalObjectExceptionThrower thrower) { // basically called by DBFlute
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
     * @param consumer The callback interface to consume the wrapped object. (NotNull)
     */
    protected void callbackIfPresent(OptionalObjectConsumer<OBJ> consumer) {
        if (consumer == null) {
            String msg = "The argument 'consumer' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (exists()) {
            consumer.accept(_obj);
        }
    }

    /**
     * Is the wrapped object present? (existing?)
     * @return The determination, true or false.
     */
    protected boolean exists() {
        return _obj != null;
    }

    /**
     * @param mapper The callback interface to apply. (NotNull)
     * @return The optional object as mapped result. (NotNull, EmptyOptionalAllowed: if not present or callback returns null)
     */
    protected BaseOptional<OBJ> callbackFilter(OptionalObjectPredicate<OBJ> mapper) {
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
     * @param <ARG> The type of value for optional object.
     * @param obj The plain object for the optional object. (NullAllowed: if null, return s empty optional)
     * @return The new-created instance of optional object. (NotNull)
     */
    protected abstract <ARG> BaseOptional<ARG> createOptionalFilteredObject(ARG obj);

    /**
     * @param mapper The callback interface to apply. (NotNull)
     * @return The optional object as mapped result. (NotNull, EmptyOptionalAllowed: if not present or callback returns null)
     */
    protected <RESULT> BaseOptional<RESULT> callbackMapping(OptionalObjectFunction<? super OBJ, ? extends RESULT> mapper) {
        if (mapper == null) {
            String msg = "The argument 'mapper' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        final RESULT result = exists() ? mapper.apply(_obj) : null;
        return createOptionalMappedObject(result);
    }

    /**
     * @param <ARG> The type of value for optional object.
     * @param obj The plain object for the optional object. (NullAllowed: if null, return s empty optional)
     * @return The new-created instance of optional object. (NotNull)
     */
    protected abstract <ARG> BaseOptional<ARG> createOptionalMappedObject(ARG obj);

    /**
     * @param mapper The callback interface to apply. (NotNull)
     * @return The optional object as flat-mapped result. (NotNull, EmptyOptionalAllowed: if not present or callback returns null)
     */
    protected <RESULT> OptionalObject<RESULT> callbackFlatMapping(
            OptionalObjectFunction<? super OBJ, OptionalObject<RESULT>> mapper) {
        if (mapper == null) {
            String msg = "The argument 'mapper' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        return exists() ? mapper.apply(_obj) : null;
    }

    /**
     * @param other The other instance to be returned if null. (NullAllowed: if null, returns null when entity is null)
     * @return The object instance wrapped in this optional object or specified value. (NullAllowed: if null specified)
     */
    protected OBJ directlyGetOrElse(OBJ other) {
        return exists() ? _obj : other;
    }

    /**
     * @param consumer The callback interface to consume the wrapped value. (NotNull)
     */
    protected void callbackRequired(OptionalObjectConsumer<OBJ> consumer) {
        if (consumer == null) {
            String msg = "The argument 'consumer' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (_obj == null) {
            _thrower.throwNotFoundException();
        }
        consumer.accept(_obj);
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
