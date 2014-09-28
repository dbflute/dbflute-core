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
 * @author awaawa
 * @author jflute
 * @since 0.9.9.8 (2012/09/04 Tuesday)
 */
public class DfCraftRowDiff extends DfAbstractDiff implements DfNestDiff {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    protected final String _craftKeyName;
    protected final DfDiffType _diffType;

    // -----------------------------------------------------
    //                                             Diff Item
    //                                             ---------
    protected DfNextPreviousDiff _craftValueDiff;

    protected List<NextPreviousHandler> _nextPreviousItemList = DfCollectionUtil.newArrayList();
    {
        _nextPreviousItemList.add(new NextPreviousHandlerBase() {
            public String titleName() {
                return "Value";
            }

            public String propertyName() {
                return "craftValueDiff";
            }

            public DfNextPreviousDiff provide() {
                return _craftValueDiff;
            }

            @Override
            protected Map<String, String> createSavedNextPreviousDiffMap() {
                return provide().createNextPreviousDiffQuotedMap();
            }

            public void restore(Map<String, Object> craftDiffMap) {
                _craftValueDiff = restoreNextPreviousDiffUnquote(craftDiffMap, propertyName());
                quoteDispIfNeeds();
            }
        });
    }

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    protected DfCraftRowDiff(String craftKeyName, DfDiffType diffType) {
        _craftKeyName = craftKeyName;
        _diffType = diffType;
    }

    protected DfCraftRowDiff(Map<String, Object> craftDiffMap) {
        _craftKeyName = (String) craftDiffMap.get("craftKeyName"); // it's a unique name
        assertCraftKeyNameExists(_craftKeyName, craftDiffMap);
        _diffType = DfDiffType.valueOf((String) craftDiffMap.get("diffType"));
        assertDiffTypeExists(_craftKeyName, craftDiffMap, _diffType);
        acceptDiffMap(craftDiffMap);
    }

    protected void assertCraftKeyNameExists(String craftKeyName, Map<String, Object> craftDiffMap) {
        if (craftKeyName == null) { // basically no way
            String msg = "The craftKeyName is required in craft diff-map:";
            msg = msg + " craftDiffMap=" + craftDiffMap;
            throw new IllegalStateException(msg);
        }
    }

    protected void assertDiffTypeExists(String craftKeyName, Map<String, Object> craftDiffMap, DfDiffType diffType) {
        if (diffType == null) { // basically no way
            String msg = "The diffType is required in craft diff-map:";
            msg = msg + " craftKey=" + craftKeyName + " craftDiffMap=" + craftDiffMap;
            throw new IllegalStateException(msg);
        }
    }

    public static DfCraftRowDiff createAdded(String craftKeyName) {
        return new DfCraftRowDiff(craftKeyName, DfDiffType.ADD);
    }

    public static DfCraftRowDiff createChanged(String craftKeyName) {
        return new DfCraftRowDiff(craftKeyName, DfDiffType.CHANGE);
    }

    public static DfCraftRowDiff createDeleted(String craftKeyName) {
        return new DfCraftRowDiff(craftKeyName, DfDiffType.DELETE);
    }

    public static DfCraftRowDiff createFromDiffMap(Map<String, Object> procedureDiffMap) {
        return new DfCraftRowDiff(procedureDiffMap);
    }

    // ===================================================================================
    //                                                                            Diff Map
    //                                                                            ========
    public Map<String, Object> createDiffMap() {
        final Map<String, Object> diffMap = DfCollectionUtil.newLinkedHashMap();
        diffMap.put("craftKeyName", _craftKeyName);
        diffMap.put("diffType", _diffType.toString());
        final List<NextPreviousHandler> nextPreviousItemList = _nextPreviousItemList;
        for (NextPreviousHandler handler : nextPreviousItemList) {
            handler.save(diffMap);
        }
        return diffMap;
    }

    public void acceptDiffMap(Map<String, Object> sequenceDiffMap) {
        final List<NextPreviousHandler> nextPreviousItemList = _nextPreviousItemList;
        for (NextPreviousHandler provider : nextPreviousItemList) {
            provider.restore(sequenceDiffMap);
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
    public String getKeyName() { // this 'key' means identity in the DBFlute process
        return getCraftKeyName();
    }

    public String getCraftKeyName() {
        return _craftKeyName != null ? _craftKeyName : "";
    }

    public String getLowerCraftKeyName() {
        return getCraftKeyName().toLowerCase();
    }

    public String getCraftKeyDispName() {
        return getCraftKeyName();
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
        final List<NextPreviousHandler> diffHandlerList = DfCollectionUtil.newArrayList();
        for (NextPreviousHandler handler : previousItemList) {
            final DfNextPreviousDiff nextPreviousDiff = handler.provide();
            if (nextPreviousDiff != null && nextPreviousDiff.hasDiff()) {
                diffHandlerList.add(handler);
            }
        }
        return diffHandlerList;
    }

    public boolean hasCraftValueDiff() {
        return _craftValueDiff != null;
    }

    public DfNextPreviousDiff getCraftValueDiff() {
        return _craftValueDiff;
    }

    public void setCraftValueDiff(DfNextPreviousDiff craftValueDiff) {
        _craftValueDiff = craftValueDiff;
    }
}
