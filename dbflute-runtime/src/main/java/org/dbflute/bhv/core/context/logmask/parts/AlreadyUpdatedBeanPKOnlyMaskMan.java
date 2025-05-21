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
package org.dbflute.bhv.core.context.logmask.parts;

import java.util.Map;

import org.dbflute.Entity;
import org.dbflute.dbmeta.DBMeta;
import org.dbflute.exception.EntityAlreadyUpdatedException.AlreadyUpdatedBeanMaskMan;
import org.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 * @since 1.2.7 (2023/07/10 Sunday at roppongi japanese)
 */
public class AlreadyUpdatedBeanPKOnlyMaskMan implements AlreadyUpdatedBeanMaskMan {

    @Override
    public String mask(Object bean) {
        if (bean == null) { // basically no way, just in case
            return null;
        }
        if (bean instanceof Entity) { // mainly here
            final Entity entity = (Entity) bean;
            final DBMeta dbmeta = entity.asDBMeta();
            final Map<String, Object> pkMap = dbmeta.extractPrimaryKeyMap(entity);
            return pkMap.toString();
        } else { // basically no way, just in case
            return "(unknown bean: " + DfTypeUtil.toClassTitle(bean) + ")";
        }
    }
}
