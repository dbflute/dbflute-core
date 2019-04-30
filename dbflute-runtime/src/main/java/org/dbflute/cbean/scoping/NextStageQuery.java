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

import org.dbflute.cbean.ConditionBean;

/**
 * The interface of next-stage query, that means it sets up condition for the next select statement. <br>
 * It contains sub-query, union-query. <br>
 * You can handle the query without their differences by this interface.
 * @author jflute
 * @param <CB> The type of condition-bean.
 */
@FunctionalInterface
public interface NextStageQuery<CB extends ConditionBean> extends SubQuery<CB>, UnionQuery<CB> {
}
