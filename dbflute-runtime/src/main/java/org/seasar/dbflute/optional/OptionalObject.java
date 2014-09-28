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

import org.seasar.dbflute.exception.EntityAlreadyDeletedException;

/**
 * @param <OBJ> The type of object.
 * @author jflute
 * @since 1.0.5F (2014/05/05 Monday)
 */
public class OptionalObject<OBJ> extends BaseOptional<OBJ> {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final OptionalObject<Object> EMPTY_INSTANCE;
    static {
        EMPTY_INSTANCE = new OptionalObject<Object>(null, new OptionalObjectExceptionThrower() {
            public void throwNotFoundException() {
                String msg = "The empty optional so the value is null.";
                throw new IllegalStateException(msg);
            }
        });
    }
    protected static final OptionalObjectExceptionThrower NOWAY_THROWER = new OptionalObjectExceptionThrower() {
        public void throwNotFoundException() {
            throw new IllegalStateException("no way");
        }
    };

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * @param object The wrapped instance of object. (NullAllowed)
     * @param thrower The exception thrower when illegal access. (NotNull)
     */
    public OptionalObject(OBJ object, OptionalObjectExceptionThrower thrower) { // basically called by DBFlute
        super(object, thrower);
    }

    /**
     * @return The fixed instance as empty. (NotNull)
     */
    @SuppressWarnings("unchecked")
    public static <EMPTY> OptionalObject<EMPTY> empty() {
        return (OptionalObject<EMPTY>) EMPTY_INSTANCE;
    }

    /**
     * @param object The wrapped object for the optional object. (NotNull)
     * @return The new-created instance as existing optional object. (NotNull)
     */
    public static <ENTITY> OptionalObject<ENTITY> of(ENTITY object) {
        if (object == null) {
            String msg = "The argument 'object' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        return new OptionalObject<ENTITY>(object, NOWAY_THROWER);
    }

    /**
     * @param object The wrapped instance or object. (NullAllowed)
     * @param thrower The exception thrower when illegal access. (NotNull)
     * @return The new-created instance as existing or empty optional object. (NotNull)
     */
    public static <ENTITY> OptionalObject<ENTITY> ofNullable(ENTITY object, OptionalObjectExceptionThrower thrower) {
        if (object != null) {
            return of(object);
        } else {
            return new OptionalObject<ENTITY>(object, thrower);
        }
    }

    // ===================================================================================
    //                                                                     Object Handling
    //                                                                     ===============
    /**
     * Get the object or exception if null.
     * @return The object instance wrapped in this optional object. (NotNull)
     * @exception EntityAlreadyDeletedException When the object instance wrapped in this optional object is null, which means object has already been deleted (point is not found).
     */
    public OBJ get() {
        return directlyGet();
    }

    /**
     * Handle the object in the optional object if the object is present. <br />
     * You should call this if null object handling is unnecessary (do nothing if null). <br />
     * If exception is preferred when null object, use required().
     * @param consumer The callback interface to consume the optional value. (NotNull)
     */
    public void ifPresent(OptionalObjectConsumer<OBJ> consumer) {
        callbackIfPresent(consumer);
    }

    /**
     * Is the object instance present? (existing?)
     * @return The determination, true or false.
     */
    public boolean isPresent() {
        return exists();
    }

    /**
     * Filter the object by the predicate.
     * @param predicate The callback to predicate whether the object is remained. (NotNull)
     * @return The filtered optional object, might be empty. (NotNull)
     */
    public OptionalObject<OBJ> filter(OptionalObjectPredicate<OBJ> predicate) {
        return (OptionalObject<OBJ>) callbackFilter(predicate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected <ARG> OptionalObject<ARG> createOptionalFilteredObject(ARG obj) {
        return new OptionalObject<ARG>(obj, _thrower);
    }

    /**
     * Apply the mapping of object to result object.
     * @param mapper The callback interface to apply. (NotNull)
     * @return The optional object as mapped result. (NotNull, EmptyOptionalAllowed: if not present or callback returns null)
     */
    public <RESULT> OptionalObject<RESULT> map(OptionalObjectFunction<? super OBJ, ? extends RESULT> mapper) {
        return (OptionalObject<RESULT>) callbackMapping(mapper); // downcast allowed because factory is overridden
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected <ARG> OptionalObject<ARG> createOptionalMappedObject(ARG obj) {
        return new OptionalObject<ARG>(obj, _thrower);
    }

    /**
     * Apply the flat-mapping of object to result object.
     * @param mapper The callback interface to apply. (NotNull)
     * @return The optional object as mapped result. (NotNull, EmptyOptionalAllowed: if not present or callback returns null)
     */
    public <RESULT> OptionalObject<RESULT> flatMap(OptionalObjectFunction<? super OBJ, OptionalObject<RESULT>> mapper) {
        return callbackFlatMapping(mapper);
    }

    /**
     * @param other The object instance to be returned when the optional is empty. (NullAllowed)
     * @return The wrapped instance or specified other object. (NullAllowed:)
     */
    public OBJ orElse(OBJ other) {
        return directlyGetOrElse(other);
    }

    /**
     * Get the object instance or null if not present.
     * @return The object instance wrapped in this optional object or null. (NullAllowed: if not present)
     */
    public OBJ orElseNull() {
        return directlyGetOrElse(null);
    }

    /**
     * Handle the object in the optional object or exception if not present.
     * @param consumer The callback interface to consume the optional value. (NotNull)
     * @exception EntityAlreadyDeletedException When the object instance wrapped in this optional object is null, which means object has already been deleted (point is not found).
     */
    public void required(OptionalObjectConsumer<OBJ> consumer) {
        callbackRequired(consumer);
    }
}
