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
package org.dbflute.logic.doc.decomment.glance;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.Srl;
import org.dbflute.util.Srl.ScopeInfo;

/**
 * @author jflute
 * @since 1.3.0 (2025/07/21 Monday at ichihara)
 */
public class DfDecommentAliasHandler {

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfDecommentAliasHandler() {
    }

    // ===================================================================================
    //                                                                      Find Decomment
    //                                                                      ==============
    public Set<String> findDecommentTableAliasSet(String tableDbName) { // not null, empty allowed
        final DfDecommentGlancePickup glancePickup = DfDecommentGlancePickup.getInstance();
        final String unifiedDecomment = glancePickup.findUnifiedTableDecomment(tableDbName);
        return doFindDecommentAliasSet(unifiedDecomment);
    }

    public Set<String> findDecommentColumnAliasSet(String tableDbName, String columnDbName) { // not null, empty allowed
        final DfDecommentGlancePickup glancePickup = DfDecommentGlancePickup.getInstance();
        final String unifiedDecomment = glancePickup.findUnifiedColumnDecomment(tableDbName, columnDbName);
        return doFindDecommentAliasSet(unifiedDecomment);
    }

    protected Set<String> doFindDecommentAliasSet(String unifiedDecomment) {
        if (Srl.is_Null_or_TrimmedEmpty(unifiedDecomment)) { // no decomment for the table
            return DfCollectionUtil.emptySet(); // not found
        }

        // conflicted decomments may have each aliases
        final String aliasBeginMark = "dfalias:{";
        final String aliasEndMark = "}";
        final List<ScopeInfo> scopeList = Srl.extractScopeList(unifiedDecomment, aliasBeginMark, aliasEndMark); // not null
        if (scopeList.isEmpty()) {
            return DfCollectionUtil.emptySet(); // not found
        }

        // using set to avoid same alias, conflicted decomments may have same alias
        final Set<String> aliasSet = DfCollectionUtil.newLinkedHashSet();
        for (ScopeInfo scopeInfo : scopeList) {
            final String content = scopeInfo.getContent(); // not null
            if (Srl.is_NotNull_and_NotTrimmedEmpty(content)) { // except e.g. dfalias:{}
                aliasSet.add(content.trim()); // e.g. dfalias:{ sea } => "sea"
            }
        }

        return Collections.unmodifiableSet(aliasSet);
    }

    // ===================================================================================
    //                                                                      Display String
    //                                                                      ==============
    // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
    // 1. register decomment alias via DBFlute Intro
    //  o immediately switch alias by SchemaHTML script
    //  o plain SchemaHTML does not show it yet
    //
    // 2. Doc task executed
    //  o SchemaHTML fixedly show decomment alias
    //  o and pieces having the decomment alias moved to pickup file by pickup process
    //
    // 3. Generate task executed
    //  o the decomment alias is used on javadoc
    // _/_/_/_/_/_/_/_/
    public String buildConflictedAliasesDisp(Set<String> aliasSet) { // null allowed if empty
        return !aliasSet.isEmpty() ? aliasSet.stream().collect(Collectors.joining(" or ")) : null;
    }
}
