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
package org.seasar.dbflute.outsidesql.typed;

import org.seasar.dbflute.twowaysql.pmbean.ParameterBean;

/**
 * The interface of typed parameter-bean.
 * @param <BEHAVIOR> The type of a corresponding behavior.
 * @author jflute
 */
public interface TypedParameterBean<BEHAVIOR> extends ParameterBean {

    /**
     * Get the path of the outside-SQL (basically as behavior query path).
     * @return The string expression of path. (NotNull)
     */
    String getOutsideSqlPath();
}
