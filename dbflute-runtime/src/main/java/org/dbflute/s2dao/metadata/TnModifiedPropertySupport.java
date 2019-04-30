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
package org.dbflute.s2dao.metadata;

import java.util.Set;

/**
 * @author modified by jflute (originated in S2Dao)
 */
public interface TnModifiedPropertySupport {

    /**
     * Get the name set of modified properties from the bean.
     * @param bean The target bean object. (NotNull)
     * @return The name set of modified properties for the bean. (NotNull)
     */
    Set<String> getModifiedPropertyNames(Object bean);
}
