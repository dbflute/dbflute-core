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
package org.seasar.dbflute.logic.jdbc.metadata.sequence;

import java.util.Map;

import org.seasar.dbflute.logic.jdbc.metadata.info.DfSequenceMeta;

/**
 * @author jflute
 * @since 0.9.6.4 (2010/01/16 Saturday)
 */
public interface DfSequenceExtractor {

    /**
     * @return The map of sequence meta. The key is sequence full name: e.g. EXAMPLEDB.SP_FOO
     */
    Map<String, DfSequenceMeta> extractSequenceMap();
}
