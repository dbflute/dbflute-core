/*
 * Copyright 2014-2025 the original author or authors.
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
package org.dbflute.cbean.paging.numberlink;

/**
 * The callback interface of page-number link option.
 * @param <OP> The type of paging option to be set up.
 * @author jflute
 * @since 1.1.0 (2014/10/13 Monday)
 */
@FunctionalInterface
public interface PageNumberLinkOptionCall<OP extends PageNumberLinkOption> {

    /**
     * @param op The option of paging to be set up. (NotNull)
     */
    void callback(OP op);
}
