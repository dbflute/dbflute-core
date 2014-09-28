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
public abstract class DfConstraintDiff extends DfAbstractDiff implements DfNestDiff {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    protected final String _constraintName;
    protected final DfDiffType _diffType;

    // -----------------------------------------------------
    //                                             Diff Item
    //                                             ---------
    protected DfNextPreviousDiff _nameDiff;
    protected DfNextPreviousDiff _columnDiff;

    protected List<NextPreviousHandler> _nextPreviousItemList = DfCollectionUtil.newArrayList();
    {
        _nextPreviousItemList.add(new NextPreviousHandlerBase() {
            public String titleName() {
                return "Name";
            }

            public String propertyName() {
                return "nameDiff";
            }

            public DfNextPreviousDiff provide() {
                return _nameDiff;
            }

            public void restore(Map<String, Object> diffMap) {
                _nameDiff = restoreNextPreviousDiff(diffMap, propertyName());
            }
        });
        _nextPreviousItemList.add(new NextPreviousHandlerBase() {
            public String titleName() {
                return "Column";
            }

            public String propertyName() {
                return "columnDiff";
            }

            public DfNextPreviousDiff provide() {
                return _columnDiff;
            }

            public void restore(Map<String, Object> diffMap) {
                _columnDiff = restoreNextPreviousDiff(diffMap, propertyName());
            }
        });
    }

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    protected DfConstraintDiff(String constraintName, DfDiffType diffType) {
        _constraintName = constraintName;
        _diffType = diffType;
    }

    protected DfConstraintDiff(Map<String, Object> diffMap) {
        _constraintName = (String) diffMap.get("constraintName");
        assertConstraintNameExists(_constraintName, diffMap);
        _diffType = DfDiffType.valueOf((String) diffMap.get("diffType"));
        acceptDiffMap(diffMap);
    }

    protected void assertConstraintNameExists(String constraintName, Map<String, Object> diffMap) {
        if (constraintName == null) { // basically no way
            String msg = "The constraintName is required in diff-map:";
            msg = msg + " diffMap=" + diffMap;
            throw new IllegalStateException(msg);
        }
    }

    protected void assertDiffTypeExists(String constraintName, Map<String, Object> diffMap, DfDiffType diffType) {
        if (diffType == null) { // basically no way
            String msg = "The diffType is required in diff-map:";
            msg = msg + " constraintName=" + constraintName + " diffMap=" + diffMap;
            throw new IllegalStateException(msg);
        }
    }

    // ===================================================================================
    //                                                                            Diff Map
    //                                                                            ========
    public Map<String, Object> createDiffMap() {
        final Map<String, Object> map = DfCollectionUtil.newLinkedHashMap();
        map.put("constraintName", _constraintName);
        map.put("diffType", _diffType.toString());
        final List<NextPreviousHandler> nextPreviousItemList = _nextPreviousItemList;
        for (NextPreviousHandler provider : nextPreviousItemList) {
            final DfNextPreviousDiff nextPreviousDiff = provider.provide();
            if (nextPreviousDiff != null) {
                map.put(provider.propertyName(), nextPreviousDiff.createNextPreviousDiffMap());
            }
        }
        return map;
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
        return getConstraintName();
    }

    public String getConstraintName() {
        return _constraintName;
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

    public boolean hasNameDiff() {
        return _nameDiff != null;
    }

    public DfNextPreviousDiff getNameDiff() {
        return _nameDiff;
    }

    public void setNameDiff(DfNextPreviousDiff nameDiff) {
        _nameDiff = nameDiff;
    }

    public boolean hasColumnDiff() {
        return _columnDiff != null;
    }

    public DfNextPreviousDiff getColumnDiff() {
        return _columnDiff;
    }

    public void setColumnDiff(DfNextPreviousDiff columnDiff) {
        _columnDiff = columnDiff;
    }
}
