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
package org.dbflute.bhv;

/**
 * The interface of behavior-selector.
 * @author jflute
 */
public interface BehaviorSelector {

    /**
     * Initialize condition-bean meta data internally.
     */
    void initializeConditionBeanMetaData();

    /**
     * Select a behavior instance by its type.
     * @param <BEHAVIOR> The type of behavior.
     * @param behaviorType The type of behavior. (NotNull)
     * @return The instance as behavior-readable. (NotNull)
     */
    <BEHAVIOR extends BehaviorReadable> BEHAVIOR select(Class<BEHAVIOR> behaviorType);

    /**
     * Select a behavior instance by table name.
     * @param tableFlexibleName The flexible name of table. (NotNull)
     * @return The instance as behavior-readable. (NotNull)
     * @throws org.dbflute.exception.DBMetaNotFoundException When the table is not found.
     * @throws org.dbflute.exception.IllegalBehaviorStateException When the behavior class is suppressed.
     */
    BehaviorReadable byName(String tableFlexibleName);
}
