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
package org.seasar.dbflute.jdbc;

import java.util.List;

/**
 * The meta of classification. <br />
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
     * Get classification by the code.
     * @param code The value of code, which is case-insensitive. (NullAllowed: if null, returns null)
     * @return The instance of the classification. (NullAllowed: when not found and code is null)
     */
    Classification codeOf(Object code);

    /**
     * Get classification by the name.
     * @param name The string of name, which is case-sensitive. (NullAllowed: if null, returns null)
     * @return The instance of the classification. (NullAllowed: when not found and name is null)
     */
    Classification nameOf(String name);

    /**
     * Get the list of all classification elements. (returns new copied list)
     * @return The list of classification elements. (NotNull)
     */
    List<Classification> listAll();

    /**
     * Get the list of group classification elements. (returns new copied list)
     * @param groupName The string of group name, which is case-sensitive. (NullAllowed: if null, returns empty list)
     * @return The list of classification elements. (NotNull)
     */
    List<Classification> groupOf(String groupName);

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
