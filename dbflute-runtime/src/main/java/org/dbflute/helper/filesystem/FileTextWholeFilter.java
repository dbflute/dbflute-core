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
package org.dbflute.helper.filesystem;

/**
 * @author jflute
 * @since 1.0.5K (2014/08/15 Friday)
 */
@FunctionalInterface
public interface FileTextWholeFilter {

    /**
     * @param text The whole text of the file. (NotNull)
     * @return The filtered text. (NullAllowed: if null, treated as empty string)
     */
    String filter(String text);
}
