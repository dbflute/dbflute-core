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

import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.properties.DfLittleAdjustmentProperties;
import org.seasar.dbflute.util.DfCollectionUtil;

/**
 * @author jflute
 * @since 0.9.7.1 (2010/06/06 Sunday)
 */
public class DfTableDiff extends DfAbstractDiff implements DfNestDiff {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    protected final String _tableName;
    protected final DfDiffType _diffType;

    // -----------------------------------------------------
    //                                             Diff Item
    //                                             ---------
    protected DfNextPreviousDiff _unifiedSchemaDiff;
    protected DfNextPreviousDiff _objectTypeDiff;
    protected DfNextPreviousDiff _columnDefOrderDiff;
    protected DfNextPreviousDiff _tableCommentDiff;

    protected List<NextPreviousHandler> _nextPreviousItemList = DfCollectionUtil.newArrayList();
    {
        _nextPreviousItemList.add(new NextPreviousHandlerBase() {
            public String titleName() {
                return "Schema";
            }

            public String propertyName() {
                return "unifiedSchemaDiff";
            }

            public DfNextPreviousDiff provide() {
                return _unifiedSchemaDiff;
            }

            public void restore(Map<String, Object> tableDiffMap) {
                _unifiedSchemaDiff = restoreNextPreviousDiff(tableDiffMap, propertyName());
            }
        });
        _nextPreviousItemList.add(new NextPreviousHandlerBase() {
            public String titleName() {
                return "Object Type";
            }

            public String propertyName() {
                return "objectTypeDiff";
            }

            public DfNextPreviousDiff provide() {
                return _objectTypeDiff;
            }

            public void restore(Map<String, Object> tableDiffMap) {
                _objectTypeDiff = restoreNextPreviousDiff(tableDiffMap, propertyName());
            }
        });
        _nextPreviousItemList.add(new NextPreviousHandlerBase() {
            public String titleName() {
                return "Column-Def Order";
            }

            public String propertyName() {
                return "columnDefOrderDiff";
            }

            public DfNextPreviousDiff provide() {
                return _columnDefOrderDiff;
            }

            public void restore(Map<String, Object> tableDiffMap) {
                _columnDefOrderDiff = restoreNextPreviousDiff(tableDiffMap, propertyName());
            }
        });
        _nextPreviousItemList.add(new NextPreviousHandlerBase() {
            public String titleName() {
                return "Comment";
            }

            public String propertyName() {
                return "tableCommentDiff";
            }

            public DfNextPreviousDiff provide() {
                return _tableCommentDiff;
            }

            @Override
            protected Map<String, String> createSavedNextPreviousDiffMap() {
                return provide().createNextPreviousDiffQuotedMap();
            }

            public void restore(Map<String, Object> tableDiffMap) {
                _tableCommentDiff = restoreNextPreviousDiffUnquote(tableDiffMap, propertyName());
                quoteDispIfNeeds();
            }
        });
    }

    // -----------------------------------------------------
    //                                           Column Diff
    //                                           -----------
    protected final List<DfColumnDiff> _columnDiffAllList = DfCollectionUtil.newArrayList();
    protected final List<DfColumnDiff> _addedColumnDiffList = DfCollectionUtil.newArrayList();
    protected final List<DfColumnDiff> _changedColumnDiffList = DfCollectionUtil.newArrayList();
    protected final List<DfColumnDiff> _deletedColumnDiffList = DfCollectionUtil.newArrayList();

    // -----------------------------------------------------
    //                                       PrimaryKey Diff
    //                                       ---------------
    protected final List<DfPrimaryKeyDiff> _primaryKeyDiffAllList = DfCollectionUtil.newArrayList();
    protected final List<DfPrimaryKeyDiff> _addedPrimaryKeyDiffList = DfCollectionUtil.newArrayList();
    protected final List<DfPrimaryKeyDiff> _changedPrimaryKeyDiffList = DfCollectionUtil.newArrayList();
    protected final List<DfPrimaryKeyDiff> _deletedPrimaryKeyDiffList = DfCollectionUtil.newArrayList();

    // -----------------------------------------------------
    //                                       ForeignKey Diff
    //                                       ---------------
    protected final List<DfForeignKeyDiff> _foreignKeyDiffAllList = DfCollectionUtil.newArrayList();
    protected final List<DfForeignKeyDiff> _addedForeignKeyDiffList = DfCollectionUtil.newArrayList();
    protected final List<DfForeignKeyDiff> _changedForeignKeyDiffList = DfCollectionUtil.newArrayList();
    protected final List<DfForeignKeyDiff> _deletedForeignKeyDiffList = DfCollectionUtil.newArrayList();

    // -----------------------------------------------------
    //                                        UniqueKey Diff
    //                                        --------------
    protected final List<DfUniqueKeyDiff> _uniqueKeyDiffAllList = DfCollectionUtil.newArrayList();
    protected final List<DfUniqueKeyDiff> _addedUniqueKeyDiffList = DfCollectionUtil.newArrayList();
    protected final List<DfUniqueKeyDiff> _changedUniqueKeyDiffList = DfCollectionUtil.newArrayList();
    protected final List<DfUniqueKeyDiff> _deletedUniqueKeyDiffList = DfCollectionUtil.newArrayList();

    // -----------------------------------------------------
    //                                            Index Diff
    //                                            ----------
    protected final List<DfIndexDiff> _indexDiffAllList = DfCollectionUtil.newArrayList();
    protected final List<DfIndexDiff> _addedIndexDiffList = DfCollectionUtil.newArrayList();
    protected final List<DfIndexDiff> _changedIndexDiffList = DfCollectionUtil.newArrayList();
    protected final List<DfIndexDiff> _deletedIndexDiffList = DfCollectionUtil.newArrayList();

    // -----------------------------------------------------
    //                                             Nest Diff
    //                                             ---------
    protected List<NestDiffSetupper> _nestDiffList = DfCollectionUtil.newArrayList();
    {
        _nestDiffList.add(new NestDiffSetupper() {
            public String propertyName() {
                return "columnDiff";
            }

            public List<? extends DfNestDiff> provide() {
                return _columnDiffAllList;
            }

            public void setup(Map<String, Object> diff) {
                addColumnDiff(createColumnDiff(diff));
            }
        });
        _nestDiffList.add(new NestDiffSetupper() {
            public String propertyName() {
                return "primaryKeyDiff";
            }

            public List<? extends DfNestDiff> provide() {
                return _primaryKeyDiffAllList;
            }

            public void setup(Map<String, Object> diff) {
                addPrimaryKeyDiff(createPrimaryKeyDiff(diff));
            }
        });
        _nestDiffList.add(new NestDiffSetupper() {
            public String propertyName() {
                return "foreignKeyDiff";
            }

            public List<? extends DfNestDiff> provide() {
                return _foreignKeyDiffAllList;
            }

            public void setup(Map<String, Object> diff) {
                addForeignKeyDiff(createForeignKeyDiff(diff));
            }
        });
        _nestDiffList.add(new NestDiffSetupper() {
            public String propertyName() {
                return "uniqueKeyDiff";
            }

            public List<? extends DfNestDiff> provide() {
                return _uniqueKeyDiffAllList;
            }

            public void setup(Map<String, Object> diff) {
                addUniqueKeyDiff(createUniqueKeyDiff(diff));
            }
        });
        _nestDiffList.add(new NestDiffSetupper() {
            public String propertyName() {
                return "indexDiff";
            }

            public List<? extends DfNestDiff> provide() {
                return _indexDiffAllList;
            }

            public void setup(Map<String, Object> diff) {
                addIndexDiff(createIndexDiff(diff));
            }
        });
    }

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    protected DfTableDiff(String tableName, DfDiffType diffType) {
        _tableName = tableName;
        _diffType = diffType;
    }

    protected DfTableDiff(Map<String, Object> tableDiffMap) {
        _tableName = (String) tableDiffMap.get("tableName");
        assertTableNameExists(_tableName, tableDiffMap);
        _diffType = DfDiffType.valueOf((String) tableDiffMap.get("diffType"));
        assertDiffTypeExists(_tableName, tableDiffMap, _diffType);
        acceptDiffMap(tableDiffMap);
    }

    protected void assertTableNameExists(String tableName, Map<String, Object> tableDiffMap) {
        if (tableName == null) { // basically no way
            String msg = "The tableName is required in table diff-map:";
            msg = msg + " tableDiffMap=" + tableDiffMap;
            throw new IllegalStateException(msg);
        }
    }

    protected void assertDiffTypeExists(String tableName, Map<String, Object> tableDiffMap, DfDiffType diffType) {
        if (diffType == null) { // basically no way
            String msg = "The diffType is required in table diff-map:";
            msg = msg + " table=" + tableName + " tableDiffMap=" + tableDiffMap;
            throw new IllegalStateException(msg);
        }
    }

    public static DfTableDiff createAdded(String tableName) {
        return new DfTableDiff(tableName, DfDiffType.ADD);
    }

    public static DfTableDiff createChanged(String tableName) {
        return new DfTableDiff(tableName, DfDiffType.CHANGE);
    }

    public static DfTableDiff createDeleted(String tableName) {
        return new DfTableDiff(tableName, DfDiffType.DELETE);
    }

    public static DfTableDiff createFromDiffMap(Map<String, Object> tableDiffMap) {
        return new DfTableDiff(tableDiffMap);
    }

    // ===================================================================================
    //                                                                            Diff Map
    //                                                                            ========
    public Map<String, Object> createDiffMap() {
        final Map<String, Object> diffMap = DfCollectionUtil.newLinkedHashMap();
        diffMap.put("tableName", _tableName);
        diffMap.put("diffType", _diffType.toString());
        final List<NextPreviousHandler> nextPreviousItemList = _nextPreviousItemList;
        for (NextPreviousHandler handler : nextPreviousItemList) {
            handler.save(diffMap);
        }
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

    public void acceptDiffMap(Map<String, Object> tableDiffMap) {
        final List<NextPreviousHandler> nextPreviousItemList = _nextPreviousItemList;
        for (NextPreviousHandler provider : nextPreviousItemList) {
            provider.restore(tableDiffMap);
        }
        final List<NestDiffSetupper> nestDiffList = _nestDiffList;
        for (NestDiffSetupper setupper : nestDiffList) {
            restoreNestDiff(tableDiffMap, setupper);
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
    public String getKeyName() {
        return getTableName();
    }

    public String getTableName() {
        return _tableName != null ? _tableName : "";
    }

    public String getLowerTableName() {
        return getTableName().toLowerCase();
    }

    public String getTableDispName() {
        // An implementation for the display name option is so easy here.
        // The best way is to save display names on a DiffMap file,
        // but DiffMap files are looked directly by human at AlterCheck and so on.
        // (now, AlterCheck has HTML for its result but no action about this)
        final DfLittleAdjustmentProperties prop = DfBuildProperties.getInstance().getLittleAdjustmentProperties();
        return prop.filterTableDispNameIfNeeds(getTableName());
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

    public boolean hasUnifiedSchemaDiff() {
        return _unifiedSchemaDiff != null;
    }

    public DfNextPreviousDiff getUnifiedSchemaDiff() {
        return _unifiedSchemaDiff;
    }

    public void setUnifiedSchemaDiff(DfNextPreviousDiff unifiedSchemaDiff) {
        _unifiedSchemaDiff = unifiedSchemaDiff;
    }

    public boolean hasObjectTypeDiff() {
        return _objectTypeDiff != null;
    }

    public DfNextPreviousDiff getObjectTypeDiff() {
        return _objectTypeDiff;
    }

    public void setObjectTypeDiff(DfNextPreviousDiff objectTypeDiff) {
        _objectTypeDiff = objectTypeDiff;
    }

    public boolean hasColumnDefOrderDiff() {
        return _columnDefOrderDiff != null;
    }

    public DfNextPreviousDiff getColumnDefOrderDiff() {
        return _columnDefOrderDiff;
    }

    public void setColumnDefOrderDiff(DfNextPreviousDiff columnDefOrderDiff) {
        _columnDefOrderDiff = columnDefOrderDiff;
    }

    public boolean hasTableCommentDiff() {
        return _tableCommentDiff != null;
    }

    public DfNextPreviousDiff getTableCommentDiff() {
        return _tableCommentDiff;
    }

    public void setTableCommentDiff(DfNextPreviousDiff tableCommentDiff) {
        _tableCommentDiff = tableCommentDiff;
    }

    public List<DfNestDiffContent> getNestDiffContentOrderedList() {
        final List<DfNestDiffContent> contentList = DfCollectionUtil.newArrayList();
        {
            final String typeName = "Column";
            setupNestDiffList(contentList, "Add " + typeName, _addedColumnDiffList);
            setupNestDiffList(contentList, "Change " + typeName, _changedColumnDiffList);
            setupNestDiffList(contentList, "Delete " + typeName, _deletedColumnDiffList);
        }
        {
            final String typeName = "PK";
            setupNestDiffList(contentList, "Add " + typeName, _addedPrimaryKeyDiffList);
            setupNestDiffList(contentList, "Change " + typeName, _changedPrimaryKeyDiffList);
            setupNestDiffList(contentList, "Delete " + typeName, _deletedPrimaryKeyDiffList);
        }
        {
            final String typeName = "FK";
            setupNestDiffList(contentList, "Add " + typeName, _addedForeignKeyDiffList);
            setupNestDiffList(contentList, "Change " + typeName, _changedForeignKeyDiffList);
            setupNestDiffList(contentList, "Delete " + typeName, _deletedForeignKeyDiffList);
        }
        {
            final String typeName = "UQ";
            setupNestDiffList(contentList, "Add " + typeName, _addedUniqueKeyDiffList);
            setupNestDiffList(contentList, "Change " + typeName, _changedUniqueKeyDiffList);
            setupNestDiffList(contentList, "Delete " + typeName, _deletedUniqueKeyDiffList);
        }
        {
            final String typeName = "Index";
            setupNestDiffList(contentList, "Add " + typeName, _addedIndexDiffList);
            setupNestDiffList(contentList, "Change " + typeName, _changedIndexDiffList);
            setupNestDiffList(contentList, "Delete " + typeName, _deletedIndexDiffList);
        }
        return contentList;
    }

    protected void setupNestDiffList(List<DfNestDiffContent> contentList, String title,
            List<? extends DfNestDiff> nestDiffList) {
        if (!nestDiffList.isEmpty()) {
            final DfNestDiffContent content = new DfNestDiffContent();
            content.setTitleName(title);
            content.setNestDiffList(nestDiffList);
            content.setChange(DfDiffType.CHANGE.equals(nestDiffList.get(0).getDiffType()));
            contentList.add(content);
        }
    }

    public static class DfNestDiffContent {
        protected String _titleName;
        protected boolean _change;

        protected List<? extends DfNestDiff> _nestDiffList;

        public String getTitleName() {
            return _titleName;
        }

        public void setTitleName(String titleName) {
            _titleName = titleName;
        }

        public boolean isChange() {
            return _change;
        }

        public void setChange(boolean change) {
            this._change = change;
        }

        public List<? extends DfNestDiff> getNestDiffList() {
            return _nestDiffList;
        }

        public void setNestDiffList(List<? extends DfNestDiff> nestDiffList) {
            _nestDiffList = nestDiffList;
        }
    }

    // -----------------------------------------------------
    //                                           Column Diff
    //                                           -----------
    public boolean hasColumnDiff() {
        return !_columnDiffAllList.isEmpty();
    }

    public boolean hasAddedOrDeletedColumnDiff() {
        return !_addedColumnDiffList.isEmpty() || !_deletedColumnDiffList.isEmpty();
    }

    public List<DfColumnDiff> getColumnDiffAllList() {
        return _columnDiffAllList;
    }

    public List<DfColumnDiff> getAddedColumnDiffList() {
        return _addedColumnDiffList;
    }

    public List<DfColumnDiff> getChangedColumnDiffList() {
        return _changedColumnDiffList;
    }

    public List<DfColumnDiff> getDeletedColumnDiffList() {
        return _deletedColumnDiffList;
    }

    public void addColumnDiff(DfColumnDiff columnDiff) {
        _columnDiffAllList.add(columnDiff);
        if (columnDiff.isAdded()) {
            _addedColumnDiffList.add(columnDiff);
        } else if (columnDiff.isChanged()) {
            _changedColumnDiffList.add(columnDiff);
        } else if (columnDiff.isDeleted()) {
            _deletedColumnDiffList.add(columnDiff);
        } else {
            String msg = "Unknown diff-type of column: ";
            msg = msg + " diffType=" + columnDiff.getDiffType();
            msg = msg + " columnDiff=" + columnDiff;
            throw new IllegalStateException(msg);
        }
    }

    // -----------------------------------------------------
    //                                       PrimaryKey Diff
    //                                       ---------------
    public boolean hasPrimaryKeyDiff() {
        return !_primaryKeyDiffAllList.isEmpty();
    }

    public List<DfPrimaryKeyDiff> getPrimaryKeyDiffAllList() {
        return _primaryKeyDiffAllList;
    }

    public List<DfPrimaryKeyDiff> getAddedPrimaryKeyDiffList() {
        return _addedPrimaryKeyDiffList;
    }

    public List<DfPrimaryKeyDiff> getChangedPrimaryKeyDiffList() {
        return _changedPrimaryKeyDiffList;
    }

    public List<DfPrimaryKeyDiff> getDeletedPrimaryKeyDiffList() {
        return _deletedPrimaryKeyDiffList;
    }

    public void addPrimaryKeyDiff(DfPrimaryKeyDiff primaryKeyDiff) {
        _primaryKeyDiffAllList.add(primaryKeyDiff);
        if (primaryKeyDiff.isAdded()) {
            _addedPrimaryKeyDiffList.add(primaryKeyDiff);
        } else if (primaryKeyDiff.isChanged()) {
            _changedPrimaryKeyDiffList.add(primaryKeyDiff);
        } else if (primaryKeyDiff.isDeleted()) {
            _deletedPrimaryKeyDiffList.add(primaryKeyDiff);
        } else {
            String msg = "Unknown diff-type of column: ";
            msg = msg + " diffType=" + primaryKeyDiff.getDiffType();
            msg = msg + " primaryKeyDiff=" + primaryKeyDiff;
            throw new IllegalStateException(msg);
        }
    }

    // -----------------------------------------------------
    //                                       ForeignKey Diff
    //                                       ---------------
    public boolean hasForeignKeyDiff() {
        return !_foreignKeyDiffAllList.isEmpty();
    }

    public List<DfForeignKeyDiff> getForeignKeyDiffAllList() {
        return _foreignKeyDiffAllList;
    }

    public List<DfForeignKeyDiff> getAddedForeignKeyDiffList() {
        return _addedForeignKeyDiffList;
    }

    public List<DfForeignKeyDiff> getChangedForeignKeyDiffList() {
        return _changedForeignKeyDiffList;
    }

    public List<DfForeignKeyDiff> getDeletedForeignKeyDiffList() {
        return _deletedForeignKeyDiffList;
    }

    public void addForeignKeyDiff(DfForeignKeyDiff foreignKeyDiff) {
        _foreignKeyDiffAllList.add(foreignKeyDiff);
        if (foreignKeyDiff.isAdded()) {
            _addedForeignKeyDiffList.add(foreignKeyDiff);
        } else if (foreignKeyDiff.isChanged()) {
            _changedForeignKeyDiffList.add(foreignKeyDiff);
        } else if (foreignKeyDiff.isDeleted()) {
            _deletedForeignKeyDiffList.add(foreignKeyDiff);
        } else {
            String msg = "Unknown diff-type of column: ";
            msg = msg + " diffType=" + foreignKeyDiff.getDiffType();
            msg = msg + " foreignKeyDiff=" + foreignKeyDiff;
            throw new IllegalStateException(msg);
        }
    }

    // -----------------------------------------------------
    //                                       UniqueKey Diff
    //                                       ---------------
    public boolean hasUniqueKeyDiff() {
        return !_uniqueKeyDiffAllList.isEmpty();
    }

    public List<DfUniqueKeyDiff> getUniqueKeyDiffAllList() {
        return _uniqueKeyDiffAllList;
    }

    public List<DfUniqueKeyDiff> getAddedUniqueKeyDiffList() {
        return _addedUniqueKeyDiffList;
    }

    public List<DfUniqueKeyDiff> getChangedUniqueKeyDiffList() {
        return _changedUniqueKeyDiffList;
    }

    public List<DfUniqueKeyDiff> getDeletedUniqueKeyDiffList() {
        return _deletedUniqueKeyDiffList;
    }

    public void addUniqueKeyDiff(DfUniqueKeyDiff uniqueKeyDiff) {
        _uniqueKeyDiffAllList.add(uniqueKeyDiff);
        if (uniqueKeyDiff.isAdded()) {
            _addedUniqueKeyDiffList.add(uniqueKeyDiff);
        } else if (uniqueKeyDiff.isChanged()) {
            _changedUniqueKeyDiffList.add(uniqueKeyDiff);
        } else if (uniqueKeyDiff.isDeleted()) {
            _deletedUniqueKeyDiffList.add(uniqueKeyDiff);
        } else {
            String msg = "Unknown diff-type of column: ";
            msg = msg + " diffType=" + uniqueKeyDiff.getDiffType();
            msg = msg + " uniqueKeyDiff=" + uniqueKeyDiff;
            throw new IllegalStateException(msg);
        }
    }

    // -----------------------------------------------------
    //                                            Index Diff
    //                                            ----------
    public boolean hasIndexDiff() {
        return !_indexDiffAllList.isEmpty();
    }

    public List<DfIndexDiff> getIndexDiffAllList() {
        return _indexDiffAllList;
    }

    public List<DfIndexDiff> getAddedIndexDiffList() {
        return _addedIndexDiffList;
    }

    public List<DfIndexDiff> getChangedIndexDiffList() {
        return _changedIndexDiffList;
    }

    public List<DfIndexDiff> getDeletedIndexDiffList() {
        return _deletedIndexDiffList;
    }

    public void addIndexDiff(DfIndexDiff indexDiff) {
        _indexDiffAllList.add(indexDiff);
        if (indexDiff.isAdded()) {
            _addedIndexDiffList.add(indexDiff);
        } else if (indexDiff.isChanged()) {
            _changedIndexDiffList.add(indexDiff);
        } else if (indexDiff.isDeleted()) {
            _deletedIndexDiffList.add(indexDiff);
        } else {
            String msg = "Unknown diff-type of column: ";
            msg = msg + " diffType=" + indexDiff.getDiffType();
            msg = msg + " indexDiff=" + indexDiff;
            throw new IllegalStateException(msg);
        }
    }
}
