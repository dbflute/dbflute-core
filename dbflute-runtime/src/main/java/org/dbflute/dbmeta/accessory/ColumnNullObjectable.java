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
package org.dbflute.dbmeta.accessory;

/**
 * The interface of column null object.
 * @author jflute
 * @since 1.1.0 (2014/11/17 Monday)
 */
public interface ColumnNullObjectable {

    /**
     * Enable the handling of column null object. (default is disabled) <br>
     * The handling is available if the null object target column in selected entity.
     */
    void enableColumnNullObject();

    /**
     * Disable the handling of column null object. (back to default) <br>
     * You can get null from selected entity even if null object target column.
     */
    void disableColumnNullObject();
}
