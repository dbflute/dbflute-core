/*
 * Copyright 2014-2024 the original author or authors.
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
package org.dbflute.jdbc;

import java.util.List;

import org.dbflute.exception.ClassificationNotFoundException;
import org.dbflute.optional.OptionalThing;

/**
 * The meta of classification. <br>
 * It's an internal interface for DBFlute runtime.
 * @author jflute
 */
public interface ClassificationMeta {

    /**
     * Get classification name of this meta.
     * @return The name of the classification.
     */
    String classificationName();

    /**
     * Get the classification of the code. (CaseInsensitive)
     * @param code The value of code, which is case-insensitive. (NullAllowed: if null, returns empty)
     * @return The optional classification corresponding to the code. (NotNull, EmptyAllowed: if not found, returns empty)
     */
    OptionalThing<? extends Classification> of(Object code);

    /**
     * Find the classification by the name. (CaseInsensitive)
     * @param name The string of name, which is case-insensitive. (NotNull)
     * @return The optional classification corresponding to the name. (NotNull, EmptyAllowed: if not found, returns empty)
     */
    OptionalThing<? extends Classification> byName(String name);

    /**
     * (old style)
     * @param code The value of code, which is case-insensitive. (NullAllowed: if null, returns null)
     * @return The instance of the classification. (NullAllowed: when not found and code is null)
     */
    Classification codeOf(Object code); // old style

    /**
     * (old style)
     * @param name The string of name, which is case-insensitive. (NullAllowed: if null, returns null)
     * @return The instance of the classification. (NullAllowed: when not found and name is null)
     */
    Classification nameOf(String name); // old style

    /**
     * Get the list of all classification elements. (returns new copied list)
     * @return The snapshot list of classification elements. (NotNull, NotEmpty)
     */
    List<Classification> listAll(); // hope to change unmodifiable but for compatible

    /**
     * Get the list of group classification elements. (returns new copied list)
     * @param groupName The string of group name, which is case-insensitive. (NotNull)
     * @return The snapshot list of classification elements. (NotNull)
     * @throws ClassificationNotFoundException When the group is not found.
     */
    List<Classification> listByGroup(String groupName);

    /**
     * (old style)
     * @param groupName The string of group name, which is case-insensitive. (NullAllowed: if null, returns empty list)
     * @return The snapshot list of classification elements. (NotNull, EmptyAllowed: if the group is not found)
     */
    List<Classification> groupOf(String groupName); // old style

    /**
     * Get the code type of the classification. e.g. String, Number
     * @return The instance of the code type for the classification. (NotNull)
     */
    ClassificationCodeType codeType();

    /**
     * Get the handling type of undefined classification code.
     * @return The enumeration of the handling type. (NotNull)
     */
    ClassificationUndefinedHandlingType undefinedHandlingType();
}
