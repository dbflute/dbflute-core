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
package org.dbflute.jdbc;

import java.util.Map;
import java.util.Set;

/**
 * The basic interface of classification. <br>
 * It's an internal interface for DBFlute runtime.
 * @author jflute
 */
public interface Classification {

    /**
     * Get the code of the classification.
     * @return The code of the classification. (NotNull)
     */
    String code();

    /**
     * Get the name, means identity name, of the classification.
     * @return The name of the classification. (NotNull)
     */
    String name();

    /**
     * Get the alias, means display name, of the classification.
     * @return The code of the classification. (NullAllowed: when an alias is not specified in its setting)
     */
    String alias();

    /**
     * Get the set of sisters (alternate codes) for the classification.
     * @return The read-only set of sister code for the classification. (NotNull, EmptyAllowed)
     */
    Set<String> sisterSet();

    /**
     * Is the classification in the group?
     * @param groupName The string of group name, which is case-sensitive. (NullAllowed: if null, returns false)
     * @return The determination, true or false. (true: this classification is in the group)
     */
    boolean inGroup(String groupName);

    /**
     * Get the map of sub items that are your original attributes.
     * @return The read-only map of sub-items. (NotNull, EmptyAllowed)
     */
    Map<String, Object> subItemMap();

    /**
     * Get the meta of the classification.
     * @return The meta of the classification. (NotNull)
     */
    ClassificationMeta meta();
}
