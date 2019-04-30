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
package org.dbflute.immutable.outsidesql;

import org.dbflute.immutable.DBableEntity;
import org.dbflute.outsidesql.typed.EntityHandlingPmb;

/**
 * The parameter-bean for immutable entity handling.
 * @param <BEHAVIOR> The type of a corresponding behavior.
 * @param <IMMU> The type of immutable entity (may be scalar) for result.
 * @param <DBLE> The type of entity (may be scalar) for result.
 * @author jflute
 * @since 1.0.5J (2014/06/15 Sunday)
 */
public interface ImmutableEntityHandlingPmb<BEHAVIOR, IMMU, DBLE extends DBableEntity<IMMU>> extends EntityHandlingPmb<BEHAVIOR, DBLE> {
}
