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
package org.seasar.dbflute.helper.stacktrace;

/**
 * @author jflute
 */
public interface InvokeNameExtractingResource {

    /**
     * Is the class extracting target? e.g. endsWith("Bhv")
     * @param className The class name with package. (NotNull)
     * @param methodName The method name without '()'. (NotNull)
     * @return The determination, true or false.
     */
    boolean isTargetElement(String className, String methodName);

    /**
     * Filter the simple class name. e.g. BsMemberBhv to MemberBhv
     * @param simpleClassName The class name without package. (NotNull)
     * @return The filtered string. (NotNull)
     */
    String filterSimpleClassName(String simpleClassName);

    /**
     * Does it use additional info? e.g. line number
     * @return The determination, true or false.
     */
    boolean isUseAdditionalInfo();

    /**
     * Get the start index for stack trace searching.
     * @return The number as start index. (NotMinus: if minus, returns empty result when extracting)
     */
    int getStartIndex();

    /**
     * Get the loop size from start index for stack trace searching.
     * @return The number as loop size. (NotMinus: if minus, returns empty result when extracting)
     */
    int getLoopSize();
}