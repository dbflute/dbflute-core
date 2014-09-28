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
package org.seasar.dbflute.bhv.logging.invoke;

import java.util.Arrays;
import java.util.List;

import org.seasar.dbflute.bhv.BehaviorReadable;
import org.seasar.dbflute.bhv.BehaviorWritable;
import org.seasar.dbflute.cbean.PagingInvoker;
import org.seasar.dbflute.dbmeta.DBMeta;
import org.seasar.dbflute.helper.stacktrace.InvokeNameExtractingResource;
import org.seasar.dbflute.helper.stacktrace.InvokeNameResult;
import org.seasar.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 * @since 1.0.4D (2013/06/30 Sunday)
 */
public class BehaviorInvokeNameExtractor {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String _readableName;
    protected static final String _writableName;
    protected static final String _pagingInvokerName;
    protected static final List<String> _suffixList;
    protected static final List<String> _keywordList;
    protected static final List<String> _ousideSqlMarkList;
    protected static final List<String> _ousideSqlExecutorSuffixList;
    protected static final List<String> _ousideSqlExecutorInnerList;
    static {
        _readableName = DfTypeUtil.toClassTitle(BehaviorReadable.class);
        _writableName = DfTypeUtil.toClassTitle(BehaviorWritable.class);
        _pagingInvokerName = DfTypeUtil.toClassTitle(PagingInvoker.class);
        _suffixList = Arrays.asList("Bhv", "BhvAp", _readableName, _writableName, _pagingInvokerName);
        _keywordList = Arrays.asList("Bhv$", _readableName + "$", _writableName + "$");
        _ousideSqlMarkList = Arrays.asList(new String[] { "OutsideSql" });
        _ousideSqlExecutorSuffixList = Arrays.asList(new String[] { "Executor" });
        _ousideSqlExecutorInnerList = Arrays.asList(new String[] { "Executor$" });
    }

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DBMeta _dbmeta;
    protected final Class<?> _outsideSqlResultType;
    protected final boolean _outsideSqlAutoPaging;
    protected final InvokeNameExtractingCoinLogic _coinLogic = createInvokeNameExtractingCoinLogic();

    protected InvokeNameExtractingCoinLogic createInvokeNameExtractingCoinLogic() {
        return new InvokeNameExtractingCoinLogic();
    }

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public BehaviorInvokeNameExtractor(DBMeta dbmeta, Class<?> outsideSqlResultType, boolean outsideSqlAutoPaging) {
        _dbmeta = dbmeta;
        _outsideSqlResultType = outsideSqlResultType;
        _outsideSqlAutoPaging = outsideSqlAutoPaging;
    }

    // ===================================================================================
    //                                                                    Extract Behavior
    //                                                                    ================
    /**
     * @param stackTrace The stack trace to extract invocation. (NotNull)
     * @return The result of invoke name for behavior. (NotNull: if not found, head result is null)
     */
    public BehaviorInvokeNameResult extractBehaviorInvoke(StackTraceElement[] stackTrace) {
        final InvokeNameExtractingResource resource = createResource();
        final List<InvokeNameResult> resultList = extractInvokeName(resource, stackTrace);
        filterBehaviorResult(resultList);

        final InvokeNameResult headResult;
        final String invokeClassName;
        final String invokeMethodName;
        if (!resultList.isEmpty()) {
            headResult = findHeadInvokeResult(resultList);
            invokeClassName = headResult.getSimpleClassName();
            invokeMethodName = headResult.getMethodName();
        } else {
            headResult = null;
            invokeClassName = _dbmeta.getTableDbName();
            invokeMethodName = "unknown";
        }
        final String expNoMethodSuffix = buildInvocationExpNoMethodSuffix(invokeClassName, invokeMethodName);
        final String invocationExp = expNoMethodSuffix + "()";
        return new BehaviorInvokeNameResult(invocationExp, expNoMethodSuffix, headResult, resultList);
    }

    // ===================================================================================
    //                                                                 Extracting Resource
    //                                                                 ===================
    protected InvokeNameExtractingResource createResource() {
        final String readableName = getReadableName();
        final String writableName = getWritableName();
        final List<String> suffixList = getSuffixList();
        final List<String> keywordList = getKeywordList();
        final List<String> ousideSqlMarkList = getOusideSqlMarkList();
        final List<String> ousideSqlExecutorSuffixList = getOusideSqlExecutorSuffixList();
        final List<String> ousideSqlExecutorInnerList = getOusideSqlExecutorInnerList();
        return new InvokeNameExtractingResource() {
            public boolean isTargetElement(String className, String methodName) {
                if (isClassNameEndsWith(className, suffixList)) {
                    return true;
                }
                if (isClassNameContains(className, keywordList)) {
                    return true;
                }
                if (isOutsideSqlElement(className)) {
                    return true;
                }
                return false;
            }

            protected boolean isOutsideSqlElement(String className) {
                if (!isClassNameContains(className, ousideSqlMarkList)) {
                    return false;
                }
                // contains e.g. OutsideSql here
                return isClassNameEndsWith(className, ousideSqlExecutorSuffixList)
                        || isClassNameContains(className, ousideSqlExecutorInnerList);
            }

            public String filterSimpleClassName(String simpleClassName) {
                if (simpleClassName.endsWith(readableName)) {
                    return readableName;
                } else if (simpleClassName.endsWith(writableName)) {
                    return writableName;
                } else {
                    return removeBasePrefix(simpleClassName);
                }
            }

            public boolean isUseAdditionalInfo() {
                return false;
            }

            public int getStartIndex() {
                return 0;
            }

            public int getLoopSize() {
                return getInvocationExtractingMaxLoopSize();
            }
        };
    }

    // ===================================================================================
    //                                                                     Settings Getter
    //                                                                     ===============
    // you can override these methods as you like
    protected String getReadableName() {
        return _readableName;
    }

    protected String getWritableName() {
        return _writableName;
    }

    protected List<String> getSuffixList() {
        return _suffixList;
    }

    protected List<String> getKeywordList() {
        return _keywordList;
    }

    protected List<String> getOusideSqlMarkList() {
        return _ousideSqlMarkList;
    }

    protected List<String> getOusideSqlExecutorSuffixList() {
        return _ousideSqlExecutorSuffixList;
    }

    protected List<String> getOusideSqlExecutorInnerList() {
        return _ousideSqlExecutorInnerList;
    }

    protected int getInvocationExtractingMaxLoopSize() {
        return 25; // should be over 20 because it might be called from SQLException handler
    }

    // ===================================================================================
    //                                                                 Extract Invoke Name
    //                                                                 ===================
    /**
     * @param resource the call-back resource for invoke-name-extracting. (NotNull)
     * @param stackTrace Stack log. (NotNull)
     * @return The list of result of invoke name. (NotNull: If not found, returns empty string.)
     */
    protected List<InvokeNameResult> extractInvokeName(InvokeNameExtractingResource resource,
            StackTraceElement[] stackTrace) {
        return _coinLogic.extractInvokeName(resource, stackTrace);
    }

    // ===================================================================================
    //                                                               Invocation Adjustment
    //                                                               =====================
    protected <RESULT> void filterBehaviorResult(List<InvokeNameResult> behaviorResultList) {
        for (InvokeNameResult behaviorResult : behaviorResultList) {
            final String simpleClassName = behaviorResult.getSimpleClassName();
            if (simpleClassName == null) {
                return;
            }
            if (simpleClassName.contains("Behavior") && simpleClassName.endsWith("$SLFunction")) {
                final String behaviorClassName = findBehaviorClassNameFromDBMeta();
                behaviorResult.setSimpleClassName(behaviorClassName);
                behaviorResult.setMethodName("scalarSelect()." + behaviorResult.getMethodName());
            }
        }
    }

    protected InvokeNameResult findHeadInvokeResult(List<InvokeNameResult> resultList) {
        if (!resultList.isEmpty()) {
            return resultList.get(resultList.size() - 1); // the latest element is the very head invoking.
        }
        return null;
    }

    protected <RESULT> String buildInvocationExpNoMethodSuffix(String invokeClassName, String invokeMethodName) {
        final String resolvedClassName = resolveOutsideSqlExecutor(invokeClassName);
        final String callerExpNoMethodSuffix = resolvedClassName + "." + invokeMethodName;
        return resolveOutsideSqlPaging(invokeMethodName, callerExpNoMethodSuffix);
    }

    protected String resolveOutsideSqlExecutor(String invokeClassName) {
        if (!isOutsideSqlExecutor(invokeClassName)) {
            return invokeClassName;
        }
        final String resolved;
        if (_outsideSqlResultType != null) {
            final String behaviorClassName = findBehaviorClassNameFromDBMeta();
            final String outsideSqlCall = behaviorClassName + ".outsideSql()";
            if (invokeClassName.contains("Entity")) {
                resolved = outsideSqlCall + ".entityHandling()";
            } else if (invokeClassName.contains("Paging")) {
                if (_outsideSqlAutoPaging) {
                    resolved = outsideSqlCall + ".autoPaging()";
                } else {
                    resolved = outsideSqlCall + ".manualPaging()";
                }
            } else if (invokeClassName.contains("Cursor")) {
                resolved = outsideSqlCall + ".cursorHandling()";
            } else {
                resolved = outsideSqlCall;
            }
        } else {
            resolved = "OutsideSql";
        }
        return resolved;
    }

    protected boolean isOutsideSqlExecutor(String invokeClassName) {
        return invokeClassName.contains("OutsideSql") && invokeClassName.endsWith("Executor");
    }

    protected String resolveOutsideSqlPaging(String invokeMethodName, String callerExpNoMethodSuffix) {
        final String resolved;
        if (_outsideSqlResultType != null && "selectPage".equals(invokeMethodName)) {
            if (Integer.class.isAssignableFrom(_outsideSqlResultType)) {
                resolved = callerExpNoMethodSuffix + "():count";
            } else {
                resolved = callerExpNoMethodSuffix + "():paging";
            }
        } else {
            resolved = callerExpNoMethodSuffix;
        }
        return resolved;
    }

    protected String findBehaviorClassNameFromDBMeta() {
        final String behaviorTypeName = _dbmeta.getBehaviorTypeName();
        final int dotIndex = behaviorTypeName.lastIndexOf(".");
        final String behaviorClassName;
        if (dotIndex < 0) { // basically no way
            behaviorClassName = behaviorTypeName;
        } else {
            final int simpleNameIndex = dotIndex + ".".length();
            behaviorClassName = behaviorTypeName.substring(simpleNameIndex);
        }
        return removeBasePrefix(behaviorClassName);
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected boolean isClassNameEndsWith(String className, List<String> suffixList) {
        return _coinLogic.isClassNameEndsWith(className, suffixList);
    }

    protected boolean isClassNameContains(String className, List<String> keywordList) {
        return _coinLogic.isClassNameContains(className, keywordList);
    }

    protected String removeBasePrefix(String simpleClassName) {
        return _coinLogic.removeBasePrefix(simpleClassName);
    }
}
