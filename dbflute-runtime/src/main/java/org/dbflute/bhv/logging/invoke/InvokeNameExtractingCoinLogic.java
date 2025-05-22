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
package org.dbflute.bhv.logging.invoke;

import java.util.List;

import org.dbflute.dbmeta.DBMeta;
import org.dbflute.helper.stacktrace.InvokeNameExtractingResource;
import org.dbflute.helper.stacktrace.InvokeNameExtractor;
import org.dbflute.helper.stacktrace.InvokeNameResult;

/**
 * @author jflute
 * @since 1.0.4D (2013/06/30 Sunday)
 */
public class InvokeNameExtractingCoinLogic {

    // ===================================================================================
    //                                                                 Extract Invoke Name
    //                                                                 ===================
    /**
     * @param resource the call-back resource for invoke-name-extracting. (NotNull)
     * @param stackTrace Stack log. (NotNull)
     * @return The list of result of invoke name. (NotNull: If not found, returns empty string.)
     */
    public List<InvokeNameResult> extractInvokeName(InvokeNameExtractingResource resource, StackTraceElement[] stackTrace) {
        final InvokeNameExtractor extractor = createInvokeNameExtractor(stackTrace);
        return extractor.extractInvokeName(resource);
    }

    public InvokeNameExtractor createInvokeNameExtractor(StackTraceElement[] stackTrace) {
        return new InvokeNameExtractor(stackTrace);
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    public boolean isClassNameEndsWith(String className, List<String> suffixList) {
        for (String suffix : suffixList) {
            if (className.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    public boolean isClassNameContains(String className, List<String> keywordList) {
        for (String keyword : keywordList) {
            if (className.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param dbmeta The DB meta for the table. (NotNull)
     * @param simpleClassName The simple class name. (NotNull)
     * @return The simple class name removed the base prefix. (NotNull)
     */
    public String removeBasePrefix(DBMeta dbmeta, String simpleClassName) {
        final String projectPrefix = dbmeta.getProjectPrefix();
        final String basePrefix = projectPrefix + dbmeta.getGenerationGapBasePrefix();
        if (!simpleClassName.startsWith(basePrefix)) {
            return simpleClassName;
        }
        final int prefixLength = basePrefix.length();
        if (!Character.isUpperCase(simpleClassName.substring(prefixLength).charAt(0))) {
            return simpleClassName;
        }
        if (simpleClassName.length() <= prefixLength) {
            return simpleClassName;
        }
        return projectPrefix + simpleClassName.substring(prefixLength);
    }
}
