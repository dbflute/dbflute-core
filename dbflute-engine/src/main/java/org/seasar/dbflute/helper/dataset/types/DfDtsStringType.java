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
package org.seasar.dbflute.helper.dataset.types;

import org.seasar.dbflute.util.DfTypeUtil;

/**
 * The string type for data set.
 * @author modified by jflute (originated in Seasar2)
 * @since 0.8.3 (2008/10/28 Tuesday)
 */
public class DfDtsStringType extends DfDtsObjectType {

    public DfDtsStringType() {
    }

    @Override
    public Object convert(Object value, String formatPattern) {
        return DfTypeUtil.toString(value, formatPattern);
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }
}