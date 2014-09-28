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
public class DfCraftTitleDiff extends DfAbstractDiff implements DfNestDiff {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    protected final String _craftTitle;
    protected final List<DfCraftRowDiff> _craftRowDiffAllList = DfCollectionUtil.newArrayList();
    protected final List<DfCraftRowDiff> _addedCraftRowDiffList = DfCollectionUtil.newArrayList();
    protected final List<DfCraftRowDiff> _changedCraftRowDiffList = DfCollectionUtil.newArrayList();
    protected final List<DfCraftRowDiff> _deletedCraftRowDiffList = DfCollectionUtil.newArrayList();

    protected List<NestDiffSetupper> _nestDiffList = DfCollectionUtil.newArrayList();
    {
        _nestDiffList.add(new NestDiffSetupper() {
            public String propertyName() {
                return "craftRowDiff";
            }

            public List<? extends DfNestDiff> provide() {
                return _craftRowDiffAllList;
            }

            public void setup(Map<String, Object> diff) {
                addCraftRowDiff(createCraftRowDiff(diff));
            }
        });
    }

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    protected DfCraftTitleDiff(String craftTitle) {
        _craftTitle = craftTitle;
    }

    protected DfCraftTitleDiff(Map<String, Object> craftDiffMap) {
        _craftTitle = (String) craftDiffMap.get("craftTitle"); // it's a unique name
        assertCraftTitleExists(_craftTitle, craftDiffMap);
        acceptDiffMap(craftDiffMap);
    }

    protected void assertCraftTitleExists(String craftTitle, Map<String, Object> craftDiffMap) {
        if (craftTitle == null) { // basically no way
            String msg = "The craftTitle is required in craft diff-map:";
            msg = msg + " craftDiffMap=" + craftDiffMap;
            throw new IllegalStateException(msg);
        }
    }

    public static DfCraftTitleDiff create(String craftTitle) {
        return new DfCraftTitleDiff(craftTitle);
    }

    public static DfCraftTitleDiff createFromDiffMap(Map<String, Object> craftDiffMap) {
        return new DfCraftTitleDiff(craftDiffMap);
    }

    // ===================================================================================
    //                                                                            Diff Map
    //                                                                            ========
    public Map<String, Object> createDiffMap() {
        final Map<String, Object> diffMap = DfCollectionUtil.newLinkedHashMap();
        diffMap.put("craftTitle", _craftTitle);
        final List<NestDiffSetupper> nestDiffList = _nestDiffList;
        for (NestDiffSetupper setupper : nestDiffList) {
            final List<? extends DfNestDiff> diffAllList = setupper.provide();
            if (!diffAllList.isEmpty()) {
                final Map<String, Map<String, Object>> nestMap = DfCollectionUtil.newLinkedHashMap();
                for (DfNestDiff nestDiff : diffAllList) {
                    if (nestDiff.hasDiff()) {
                        nestMap.put(nestDiff.getKeyName(), nestDiff.createDiffMap());
                    }
                }
                diffMap.put(setupper.propertyName(), nestMap);
            }
        }
        return diffMap;
    }

    public void acceptDiffMap(Map<String, Object> craftDiffMap) {
        final List<NestDiffSetupper> nestDiffList = _nestDiffList;
        for (NestDiffSetupper setupper : nestDiffList) {
            restoreNestDiff(craftDiffMap, setupper);
        }
    }

    // ===================================================================================
    //                                                                              Status
    //                                                                              ======
    public boolean hasDiff() {
        final List<NestDiffSetupper> nestDiffList = _nestDiffList;
        for (NestDiffSetupper setupper : nestDiffList) {
            final List<? extends DfNestDiff> diffAllList = setupper.provide();
            for (DfNestDiff nestDiff : diffAllList) {
                if (nestDiff.hasDiff()) {
                    return true;
                }
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
        return getCraftTitle();
    }

    public String getCraftTitle() {
        return _craftTitle != null ? _craftTitle : "";
    }

    public String getLowerCraftTitle() {
        return getCraftTitle().toLowerCase();
    }

    public String getCraftDispTitle() {
        return getCraftTitle();
    }

    public DfDiffType getDiffType() { // basically unused
        return DfDiffType.CHANGE; // fixed
    }

    // -----------------------------------------------------
    //                                             Diff Item
    //                                             ---------
    public List<NextPreviousHandler> getNextPreviousDiffList() {
        return DfCollectionUtil.emptyList();
    }

    // -----------------------------------------------------
    //                                             CraftDiff
    //                                             ---------
    public List<DfCraftRowDiff> getCraftDiffRowAllList() {
        return _craftRowDiffAllList;
    }

    public List<DfCraftRowDiff> getAddedCraftRowDiffList() {
        return _addedCraftRowDiffList;
    }

    public List<DfCraftRowDiff> getChangedCraftRowDiffList() {
        return _changedCraftRowDiffList;
    }

    public List<DfCraftRowDiff> getDeletedCraftRowDiffList() {
        return _deletedCraftRowDiffList;
    }

    public void addCraftRowDiff(DfCraftRowDiff craftRowDiff) {
        _craftRowDiffAllList.add(craftRowDiff);
        if (craftRowDiff.isAdded()) {
            _addedCraftRowDiffList.add(craftRowDiff);
        } else if (craftRowDiff.isChanged()) {
            _changedCraftRowDiffList.add(craftRowDiff);
        } else if (craftRowDiff.isDeleted()) {
            _deletedCraftRowDiffList.add(craftRowDiff);
        } else { // no way
            String msg = "Unknown diff-type of craft: ";
            msg = msg + " diffType=" + craftRowDiff.getDiffType() + " craftRowDiff=" + craftRowDiff;
            throw new IllegalStateException(msg);
        }
    }
}
