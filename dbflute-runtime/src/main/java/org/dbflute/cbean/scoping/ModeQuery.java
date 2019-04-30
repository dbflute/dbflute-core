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
package org.dbflute.cbean.scoping;

/**
 * The interface of mode-query.
 * <pre>
 * cb.enableEmptyStringQuery(() -&gt; {
 *     cb.query().setMemberName_Equal(mightBeEmptyString);
 * });
 * </pre>
 * @author jflute
 * @since 1.1.0 (2014/10/17 Friday)
 */
@FunctionalInterface
public interface ModeQuery {

    /**
     * Set up your query condition in the mode.
     */
    void query();
}
