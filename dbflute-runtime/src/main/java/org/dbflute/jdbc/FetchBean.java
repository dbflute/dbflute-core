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

/**
 * The bean for fetch.
 * @author jflute
 */
public interface FetchBean {

    /**
     * Check whether the result size is safety or not. <br>
     * If the result size is in Danger Zone, the select method throws DangerousResultSizeException.
     * @param safetyMaxResultSize The max size of safety result. (if zero or minus, checking is invalid)
     */
    void checkSafetyResult(int safetyMaxResultSize);

    /**
     * Get the max size of safety result.
     * @return The max size of safety result. (zero means no check)
     */
    int getSafetyMaxResultSize();
}
