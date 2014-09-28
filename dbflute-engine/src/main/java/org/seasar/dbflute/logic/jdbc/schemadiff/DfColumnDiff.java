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
package org.seasar.dbflute.logic.jdbc.schemadiff;

import java.util.List;
import java.util.Map;

import org.seasar.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 * @since 0.9.7.1 (2010/06/06 Sunday)
 */
public class DfColumnDiff extends DfAbstractDiff implements DfNestDiff {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    protected final String _columnName;
    protected final DfDiffType _diffType;

    // -----------------------------------------------------
    //                                             Diff Item
    //                                             ---------
    protected DfNextPreviousDiff _dbTypeDiff;
    protected DfNextPreviousDiff _columnSizeDiff;
    protected DfNextPreviousDiff _defaultValueDiff;
    protected DfNextPreviousDiff _notNullDiff;
    protected DfNextPreviousDiff _autoIncrementDiff;
    protected DfNextPreviousDiff _columnCommentDiff;

    protected List<NextPreviousHandler> _nextPreviousItemList = DfCollectionUtil.newArrayList();
    {
        _nextPreviousItemList.add(new NextPreviousHandlerBase() {
            public String titleName() {
                return "Type";
            }

            public String propertyName() {
                return "dbTypeDiff";
            }

            public DfNextPreviousDiff provide() {
                return _dbTypeDiff;
            }

            public void restore(Map<String, Object> columnDiffMap) {
                _dbTypeDiff = restoreNextPreviousDiff(columnDiffMap, propertyName());
            }
        });
        _nextPreviousItemList.add(new NextPreviousHandlerBase() {
            public String titleName() {
                return "Size";
            }

            public String propertyName() {
                return "columnSizeDiff";
            }

            public DfNextPreviousDiff provide() {
                return _columnSizeDiff;
            }

            public void restore(Map<String, Object> columnDiffMap) {
                _columnSizeDiff = restoreNextPreviousDiff(columnDiffMap, propertyName());
            }
        });
        _nextPreviousItemList.add(new NextPreviousHandlerBase() {
            public String titleName() {
                return "Default";
            }

            public DfNextPreviousDiff provide() {
                return _defaultValueDiff;
            }

            public String propertyName() {
                return "defaultValueDiff";
            }

            // trimmed-string process here
            // but DBFlute gets default values with trimming
            // so it does not work

            @Override
            protected Map<String, String> createSavedNextPreviousDiffMap() {
                return provide().createNextPreviousDiffQuotedMap();
            }

            public void restore(Map<String, Object> columnDiffMap) {
                _defaultValueDiff = restoreNextPreviousDiffUnquote(columnDiffMap, propertyName());
                quoteDispIfNeeds();
            }
        });
        _nextPreviousItemList.add(new NextPreviousHandlerBase() {
            public String titleName() {
                return "Not Null";
            }

            public String propertyName() {
                return "notNullDiff";
            }

            public DfNextPreviousDiff provide() {
                return _notNullDiff;
            }

            public void restore(Map<String, Object> columnDiffMap) {
                _notNullDiff = restoreNextPreviousDiff(columnDiffMap, propertyName());
            }
        });
        _nextPreviousItemList.add(new NextPreviousHandlerBase() {
            public String titleName() {
                return "Identity";
            }

            public String propertyName() {
                return "autoIncrementDiff";
            }

            public DfNextPreviousDiff provide() {
                return _autoIncrementDiff;
            }

            public void restore(Map<String, Object> columnDiffMap) {
                _autoIncrementDiff = restoreNextPreviousDiff(columnDiffMap, propertyName());
            }
        });
        _nextPreviousItemList.add(new NextPreviousHandlerBase() {
            public String titleName() {
                return "Comment";
            }

            public String propertyName() {
                return "columnCommentDiff";
            }

            public DfNextPreviousDiff provide() {
                return _columnCommentDiff;
            }

            @Override
            protected Map<String, String> createSavedNextPreviousDiffMap() {
                return provide().createNextPreviousDiffQuotedMap();
            }

            public void restore(Map<String, Object> columnDiffMap) {
                _columnCommentDiff = restoreNextPreviousDiffUnquote(columnDiffMap, propertyName());
                quoteDispIfNeeds();
            }
        });
    }

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    protected DfColumnDiff(String columnName, DfDiffType diffType) {
        _columnName = columnName;
        _diffType = diffType;
    }

    protected DfColumnDiff(Map<String, Object> columnDiffMap) {
        _columnName = (String) columnDiffMap.get("columnName");
        assertColumnNameExists(_columnName, columnDiffMap);
        _diffType = DfDiffType.valueOf((String) columnDiffMap.get("diffType"));
        acceptDiffMap(columnDiffMap);
    }

    protected void assertColumnNameExists(String columnName, Map<String, Object> columnDiffMap) {
        if (columnName == null) { // basically no way
            String msg = "The columnName is required in column diff-map:";
            msg = msg + " columnDiffMap=" + columnDiffMap;
            throw new IllegalStateException(msg);
        }
    }

    protected void assertDiffTypeExists(String columnName, Map<String, Object> columnDiffMap, DfDiffType diffType) {
        if (diffType == null) { // basically no way
            String msg = "The diffType is required in column diff-map:";
            msg = msg + " column=" + columnName + " columnDiffMap=" + columnDiffMap;
            throw new IllegalStateException(msg);
        }
    }

    public static DfColumnDiff createAdded(String columnName) {
        return new DfColumnDiff(columnName, DfDiffType.ADD);
    }

    public static DfColumnDiff createChanged(String columnName) {
        return new DfColumnDiff(columnName, DfDiffType.CHANGE);
    }

    public static DfColumnDiff createDeleted(String columnName) {
        return new DfColumnDiff(columnName, DfDiffType.DELETE);
    }

    public static DfColumnDiff createFromDiffMap(Map<String, Object> columnDiffMap) {
        return new DfColumnDiff(columnDiffMap);
    }

    // ===================================================================================
    //                                                                            Diff Map
    //                                                                            ========
    public Map<String, Object> createDiffMap() {
        final Map<String, Object> diffMap = DfCollectionUtil.newLinkedHashMap();
        diffMap.put("columnName", _columnName);
        diffMap.put("diffType", _diffType.toString());
        final List<NextPreviousHandler> nextPreviousItemList = _nextPreviousItemList;
        for (NextPreviousHandler provider : nextPreviousItemList) {
            provider.save(diffMap);
        }
        return diffMap;
    }

    public void acceptDiffMap(Map<String, Object> diffMap) {
        final List<NextPreviousHandler> nextPreviousItemList = _nextPreviousItemList;
        for (NextPreviousHandler provider : nextPreviousItemList) {
            provider.restore(diffMap);
        }
    }

    // ===================================================================================
    //                                                                              Status
    //                                                                              ======
    public boolean hasDiff() {
        if (!DfDiffType.CHANGE.equals(_diffType)) {
            return true; // if not change, always different
        }
        final List<NextPreviousHandler> nextPreviousItemList = _nextPreviousItemList;
        for (NextPreviousHandler provider : nextPreviousItemList) {
            if (provider.provide() != null) {
                return true;
            }
        }
        return false;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    public String getKeyName() {
        return getColumnName();
    }

    public String getColumnName() {
        return _columnName;
    }

    public DfDiffType getDiffType() {
        return _diffType;
    }

    public boolean isAdded() {
        return DfDiffType.ADD.equals(_diffType);
    }

    public boolean isChanged() {
        return DfDiffType.CHANGE.equals(_diffType);
    }

    public boolean isDeleted() {
        return DfDiffType.DELETE.equals(_diffType);
    }

    // -----------------------------------------------------
    //                                             Diff Item
    //                                             ---------
    public List<NextPreviousHandler> getNextPreviousDiffList() {
        final List<NextPreviousHandler> previousItemList = _nextPreviousItemList;
        final List<NextPreviousHandler> validHandlerList = DfCollectionUtil.newArrayList();
        for (NextPreviousHandler handler : previousItemList) {
            final DfNextPreviousDiff nextPreviousDiff = handler.provide();
            if (nextPreviousDiff != null && nextPreviousDiff.hasDiff()) {
                validHandlerList.add(handler);
            }
        }
        return validHandlerList;
    }

    public boolean hasDbTypeDiff() {
        return _dbTypeDiff != null;
    }

    public DfNextPreviousDiff getDbTypeDiff() {
        return _dbTypeDiff;
    }

    public void setDbTypeDiff(DfNextPreviousDiff dbTypeDiff) {
        _dbTypeDiff = dbTypeDiff;
    }

    public boolean hasColumnSizeDiff() {
        return _columnSizeDiff != null;
    }

    public DfNextPreviousDiff getColumnSizeDiff() {
        return _columnSizeDiff;
    }

    public void setColumnSizeDiff(DfNextPreviousDiff columnSizeDiff) {
        _columnSizeDiff = columnSizeDiff;
    }

    public boolean hasDefaultValueDiff() {
        return _defaultValueDiff != null;
    }

    public DfNextPreviousDiff getDefaultValueDiff() {
        return _defaultValueDiff;
    }

    public void setDefaultValueDiff(DfNextPreviousDiff defaultValueDiff) {
        _defaultValueDiff = defaultValueDiff;
    }

    public boolean hasNotNullDiff() {
        return _notNullDiff != null;
    }

    public DfNextPreviousDiff getNotNullDiff() {
        return _notNullDiff;
    }

    public void setNotNullDiff(DfNextPreviousDiff notNullDiff) {
        _notNullDiff = notNullDiff;
    }

    public boolean hasAutoIncrementDiff() {
        return _autoIncrementDiff != null;
    }

    public DfNextPreviousDiff getAutoIncrementDiff() {
        return _autoIncrementDiff;
    }

    public void setAutoIncrementDiff(DfNextPreviousDiff autoIncrementDiff) {
        _autoIncrementDiff = autoIncrementDiff;
    }

    public boolean hasColumnCommentDiff() {
        return _columnCommentDiff != null;
    }

    public DfNextPreviousDiff getColumnCommentDiff() {
        return _columnCommentDiff;
    }

    public void setColumnCommentDiff(DfNextPreviousDiff columnCommentDiff) {
        _columnCommentDiff = columnCommentDiff;
    }
}
