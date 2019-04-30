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
package org.dbflute.logic.jdbc.schemadiff;

import java.util.List;
import java.util.Map;

import org.dbflute.logic.jdbc.schemadiff.DfAbstractDiff.NextPreviousHandler;

/**
 * @author jflute
 * @since 0.9.7.1 (2010/06/06 Sunday)
 */
public interface DfNestDiff {

    DfDiffType getDiffType();

    String getKeyName();

    boolean hasDiff();

    Map<String, Object> createDiffMap();

    void acceptDiffMap(Map<String, Object> diffMap);

    List<NextPreviousHandler> getNextPreviousDiffList();
}
