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
 * @since 0.9.9.7F (2012/08/20 Monday)
 */
public class DfProcedureDiff extends DfAbstractDiff implements DfNestDiff {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    protected final String _procedureUniqueName;
    protected final DfDiffType _diffType;

    // -----------------------------------------------------
    //                                             Diff Item
    //                                             ---------
    protected DfNextPreviousDiff _unifiedSchemaDiff;
    protected DfNextPreviousDiff _sourceLineDiff;
    protected DfNextPreviousDiff _sourceSizeDiff;
    protected DfNextPreviousDiff _sourceHashDiff;
    protected DfNextPreviousDiff _procedureCommentDiff;

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

            public void restore(Map<String, Object> procedureDiffMap) {
                _unifiedSchemaDiff = restoreNextPreviousDiff(procedureDiffMap, propertyName());
            }
        });
        _nextPreviousItemList.add(new NextPreviousHandlerBase() {
            public String titleName() {
                return "SourceLine";
            }

            public String propertyName() {
                return "sourceLineDiff";
            }

            public DfNextPreviousDiff provide() {
                return _sourceLineDiff;
            }

            @Override
            protected Map<String, String> createSavedNextPreviousDiffMap() {
                return provide().createNextPreviousDiffQuotedMap();
            }

            public void restore(Map<String, Object> procedureDiffMap) {
                _sourceLineDiff = restoreNextPreviousDiffUnquote(procedureDiffMap, propertyName());
                quoteDispIfNeeds();
            }
        });
        _nextPreviousItemList.add(new NextPreviousHandlerBase() {
            public String titleName() {
                return "SourceSize";
            }

            public String propertyName() {
                return "sourceSizeDiff";
            }

            public DfNextPreviousDiff provide() {
                return _sourceSizeDiff;
            }

            @Override
            protected Map<String, String> createSavedNextPreviousDiffMap() {
                return provide().createNextPreviousDiffQuotedMap();
            }

            public void restore(Map<String, Object> procedureDiffMap) {
                _sourceSizeDiff = restoreNextPreviousDiffUnquote(procedureDiffMap, propertyName());
                quoteDispIfNeeds();
            }
        });
        _nextPreviousItemList.add(new NextPreviousHandlerBase() {
            public String titleName() {
                return "SourceHash";
            }

            public String propertyName() {
                return "sourceHashDiff";
            }

            public DfNextPreviousDiff provide() {
                return _sourceHashDiff;
            }

            @Override
            protected Map<String, String> createSavedNextPreviousDiffMap() {
                return provide().createNextPreviousDiffQuotedMap();
            }

            public void restore(Map<String, Object> procedureDiffMap) {
                _sourceHashDiff = restoreNextPreviousDiffUnquote(procedureDiffMap, propertyName());
                quoteDispIfNeeds();
            }
        });
        _nextPreviousItemList.add(new NextPreviousHandlerBase() {
            public String titleName() {
                return "Comment";
            }

            public String propertyName() {
                return "procedureCommentDiff";
            }

            public DfNextPreviousDiff provide() {
                return _procedureCommentDiff;
            }

            @Override
            protected Map<String, String> createSavedNextPreviousDiffMap() {
                return provide().createNextPreviousDiffQuotedMap();
            }

            public void restore(Map<String, Object> procedureDiffMap) {
                _procedureCommentDiff = restoreNextPreviousDiffUnquote(procedureDiffMap, propertyName());
                quoteDispIfNeeds();
            }
        });
    }

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    protected DfProcedureDiff(String procedureUniqueName, DfDiffType diffType) {
        _procedureUniqueName = procedureUniqueName;
        _diffType = diffType;
    }

    protected DfProcedureDiff(Map<String, Object> procedureDiffMap) {
        _procedureUniqueName = (String) procedureDiffMap.get("procedureName"); // it's a unique name
        assertProcedureNameExists(_procedureUniqueName, procedureDiffMap);
        _diffType = DfDiffType.valueOf((String) procedureDiffMap.get("diffType"));
        assertDiffTypeExists(_procedureUniqueName, procedureDiffMap, _diffType);
        acceptDiffMap(procedureDiffMap);
    }

    protected void assertProcedureNameExists(String procedureUniqueName, Map<String, Object> procedureDiffMap) {
        if (procedureUniqueName == null) { // basically no way
            String msg = "The procedureUniqueName is required in procedure diff-map:";
            msg = msg + " procedureDiffMap=" + procedureDiffMap;
            throw new IllegalStateException(msg);
        }
    }

    protected void assertDiffTypeExists(String procedureUniqueName, Map<String, Object> procedureDiffMap,
            DfDiffType diffType) {
        if (diffType == null) { // basically no way
            String msg = "The diffType is required in procedure diff-map:";
            msg = msg + " procedure=" + procedureUniqueName + " procedureDiffMap=" + procedureDiffMap;
            throw new IllegalStateException(msg);
        }
    }

    public static DfProcedureDiff createAdded(String procedureUniqueName) {
        return new DfProcedureDiff(procedureUniqueName, DfDiffType.ADD);
    }

    public static DfProcedureDiff createChanged(String procedureUniqueName) {
        return new DfProcedureDiff(procedureUniqueName, DfDiffType.CHANGE);
    }

    public static DfProcedureDiff createDeleted(String procedureUniqueName) {
        return new DfProcedureDiff(procedureUniqueName, DfDiffType.DELETE);
    }

    public static DfProcedureDiff createFromDiffMap(Map<String, Object> procedureDiffMap) {
        return new DfProcedureDiff(procedureDiffMap);
    }

    // ===================================================================================
    //                                                                            Diff Map
    //                                                                            ========
    public Map<String, Object> createDiffMap() {
        final Map<String, Object> diffMap = DfCollectionUtil.newLinkedHashMap();
        diffMap.put("procedureName", _procedureUniqueName);
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
    public String getKeyName() {
        return getProcedureName();
    }

    public String getProcedureName() {
        return _procedureUniqueName != null ? _procedureUniqueName : "";
    }

    public String getLowerProcedureName() {
        return getProcedureName().toLowerCase();
    }

    public String getProcedureDispName() {
        // no filter about upper case because no property for procedure
        return getProcedureName();
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

    public boolean hasSourceLineDiff() {
        return _sourceLineDiff != null;
    }

    public DfNextPreviousDiff getSourceLineDiff() {
        return _sourceLineDiff;
    }

    public void setSourceLineDiff(DfNextPreviousDiff sourceLineDiff) {
        _sourceLineDiff = sourceLineDiff;
    }

    public boolean hasSourceSizeDiff() {
        return _sourceSizeDiff != null;
    }

    public DfNextPreviousDiff getSourceSizeDiff() {
        return _sourceSizeDiff;
    }

    public void setSourceSizeDiff(DfNextPreviousDiff sourceSizeDiff) {
        _sourceSizeDiff = sourceSizeDiff;
    }

    public boolean hasSourceHashDiff() {
        return _sourceHashDiff != null;
    }

    public DfNextPreviousDiff getSourceHashDiff() {
        return _sourceHashDiff;
    }

    public void setSourceHashDiff(DfNextPreviousDiff sourceHashDiff) {
        _sourceHashDiff = sourceHashDiff;
    }

    public boolean hasProcedureCommentDiff() {
        return _procedureCommentDiff != null;
    }

    public DfNextPreviousDiff getProcedureCommentDiff() {
        return _procedureCommentDiff;
    }

    public void setProcedureCommentDiff(DfNextPreviousDiff procedureCommentDiff) {
        _procedureCommentDiff = procedureCommentDiff;
    }
}
