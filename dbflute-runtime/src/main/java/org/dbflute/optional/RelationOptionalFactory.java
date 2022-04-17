/*
 * Copyright 2014-2022 the original author or authors.
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
 * @author jflute
 * @since 1.0.5J (2014/06/22 Sunday)
 */
public class RelationOptionalFactory {

    // ===================================================================================
    //                                                                         Null Entity
    //                                                                         ===========
    /**
     * Create optional null entity.
     * @param thrower The exception thrower for optional object. (NotNull: if needs)
     * @return The optional object for the relation. (NotNull)
     */
    public Object createOptionalNullEntity(OptionalThingExceptionThrower thrower) { // #ext_point, object for override
        return OptionalEntity.ofNullable(null, thrower);
    }

    // ===================================================================================
    //                                                                      Present Entity
    //                                                                      ==============
    /**
     * Create optional present entity.
     * @param relationRow The row instance of relation entity. (NotNull)
     * @return The optional object for the relation. (NotNull)
     */
    public Object createOptionalPresentEntity(Object relationRow) { // #ext_point, object for override
        return OptionalEntity.of(relationRow);
    }

    // ===================================================================================
    //                                                                 OptionalEntity Type
    //                                                                 ===================
    /**
     * Get the type of optional entity for relation.
     * @return The class type of optional entity. (NotNull)
     */
    public Class<?> getOptionalEntityType() { // #ext_point
        return OptionalEntity.class;
    }

    // ===================================================================================
    //                                                                   Optional Handling
    //                                                                   =================
    /**
     * Extract wrapped entity instance or null. 
     * @param optional The optional object as object. (NotNull)
     * @return The extracted entity or null. (NullAllowed)
     */
    @SuppressWarnings("unchecked")
    public Object orElseNull(Object optional) { // #ext_point, object for override
        final OptionalEntity<Object> optObj = ((OptionalEntity<Object>) optional);
        return optObj.isPresent() ? optObj.get() : null;
    }

    /**
     * Is the relation row wrapped optional object? 
     * @param relationRow The row of relation, might be optional. (NullAllowed: if null, returns false)
     * @return The determination, true or false.
     */
    public boolean isOptional(Object relationRow) {
        return relationRow != null && isOptionalType(relationRow.getClass());
    }

    /**
     * Is the relation type optional type? 
     * @param relationType The type of relation, might be optional. (NullAllowed: if null, returns false)
     * @return The determination, true or false.
     */
    public boolean isOptionalType(Class<?> relationType) {
        return getOptionalEntityType().isAssignableFrom(relationType);
    }
}
