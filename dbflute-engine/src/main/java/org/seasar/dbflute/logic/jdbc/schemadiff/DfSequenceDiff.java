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
 * @since 0.9.9.7F (2012/08/20 Monday)
 */
public class DfSequenceDiff extends DfAbstractDiff implements DfNestDiff {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    protected final String _sequenceUniqueName;
    protected final DfDiffType _diffType;

    // -----------------------------------------------------
    //                                             Diff Item
    //                                             ---------
    protected DfNextPreviousDiff _unifiedSchemaDiff;
    protected DfNextPreviousDiff _minimumValueDiff;
    protected DfNextPreviousDiff _maximumValueDiff;
    protected DfNextPreviousDiff _incrementSizeDiff;
    protected DfNextPreviousDiff _sequenceCommentDiff;

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

            public void restore(Map<String, Object> sequenceDiffMap) {
                _unifiedSchemaDiff = restoreNextPreviousDiff(sequenceDiffMap, propertyName());
            }
        });
        _nextPreviousItemList.add(new NextPreviousHandlerBase() {
            public String titleName() {
                return "Minimum Value";
            }

            public String propertyName() {
                return "minimumValueDiff";
            }

            public DfNextPreviousDiff provide() {
                return _minimumValueDiff;
            }

            public void restore(Map<String, Object> sequenceDiffMap) {
                _minimumValueDiff = restoreNextPreviousDiff(sequenceDiffMap, propertyName());
            }
        });
        _nextPreviousItemList.add(new NextPreviousHandlerBase() {
            public String titleName() {
                return "Maximum Value";
            }

            public String propertyName() {
                return "maximumValueDiff";
            }

            public DfNextPreviousDiff provide() {
                return _maximumValueDiff;
            }

            public void restore(Map<String, Object> sequenceDiffMap) {
                _maximumValueDiff = restoreNextPreviousDiff(sequenceDiffMap, propertyName());
            }
        });
        _nextPreviousItemList.add(new NextPreviousHandlerBase() {
            public String titleName() {
                return "Increment Size";
            }

            public String propertyName() {
                return "incrementSizeDiff";
            }

            public DfNextPreviousDiff provide() {
                return _incrementSizeDiff;
            }

            public void restore(Map<String, Object> sequenceDiffMap) {
                _incrementSizeDiff = restoreNextPreviousDiff(sequenceDiffMap, propertyName());
            }
        });
        _nextPreviousItemList.add(new NextPreviousHandlerBase() {
            public String titleName() {
                return "Comment";
            }

            public String propertyName() {
                return "sequenceCommentDiff";
            }

            public DfNextPreviousDiff provide() {
                return _sequenceCommentDiff;
            }

            @Override
            protected Map<String, String> createSavedNextPreviousDiffMap() {
                return provide().createNextPreviousDiffQuotedMap();
            }

            public void restore(Map<String, Object> sequenceDiffMap) {
                _sequenceCommentDiff = restoreNextPreviousDiffUnquote(sequenceDiffMap, propertyName());
                quoteDispIfNeeds();
            }
        });
    }

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    protected DfSequenceDiff(String sequenceUniqueName, DfDiffType diffType) {
        _sequenceUniqueName = sequenceUniqueName;
        _diffType = diffType;
    }

    protected DfSequenceDiff(Map<String, Object> sequenceDiffMap) {
        _sequenceUniqueName = (String) sequenceDiffMap.get("sequenceName"); // it's a unique name
        assertSequenceNameExists(_sequenceUniqueName, sequenceDiffMap);
        _diffType = DfDiffType.valueOf((String) sequenceDiffMap.get("diffType"));
        assertDiffTypeExists(_sequenceUniqueName, sequenceDiffMap, _diffType);
        acceptDiffMap(sequenceDiffMap);
    }

    protected void assertSequenceNameExists(String sequenceUniqueName, Map<String, Object> sequenceDiffMap) {
        if (sequenceUniqueName == null) { // basically no way
            String msg = "The sequenceUniqueName is required in sequence diff-map:";
            msg = msg + " sequenceDiffMap=" + sequenceDiffMap;
            throw new IllegalStateException(msg);
        }
    }

    protected void assertDiffTypeExists(String sequenceUniqueName, Map<String, Object> sequenceDiffMap,
            DfDiffType diffType) {
        if (diffType == null) { // basically no way
            String msg = "The diffType is required in sequence diff-map:";
            msg = msg + " sequence=" + sequenceUniqueName + " sequenceDiffMap=" + sequenceDiffMap;
            throw new IllegalStateException(msg);
        }
    }

    public static DfSequenceDiff createAdded(String sequenceUniqueName) {
        return new DfSequenceDiff(sequenceUniqueName, DfDiffType.ADD);
    }

    public static DfSequenceDiff createChanged(String sequenceUniqueName) {
        return new DfSequenceDiff(sequenceUniqueName, DfDiffType.CHANGE);
    }

    public static DfSequenceDiff createDeleted(String sequenceUniqueName) {
        return new DfSequenceDiff(sequenceUniqueName, DfDiffType.DELETE);
    }

    public static DfSequenceDiff createFromDiffMap(Map<String, Object> sequenceDiffMap) {
        return new DfSequenceDiff(sequenceDiffMap);
    }

    // ===================================================================================
    //                                                                            Diff Map
    //                                                                            ========
    public Map<String, Object> createDiffMap() {
        final Map<String, Object> diffMap = DfCollectionUtil.newLinkedHashMap();
        diffMap.put("sequenceName", _sequenceUniqueName);
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
        return getSequenceName();
    }

    public String getSequenceName() {
        return _sequenceUniqueName != null ? _sequenceUniqueName : "";
    }

    public String getLowerSequenceName() {
        return getSequenceName().toLowerCase();
    }

    public String getSequenceDispName() {
        // no filter about upper case because no property for sequence
        return getSequenceName();
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

    public boolean hasMinimumValueDiff() {
        return _minimumValueDiff != null;
    }

    public DfNextPreviousDiff getMinimumValueDiff() {
        return _minimumValueDiff;
    }

    public void setMinimumValueDiff(DfNextPreviousDiff minimumValueDiff) {
        _minimumValueDiff = minimumValueDiff;
    }

    public boolean hasMaximumValueDiff() {
        return _maximumValueDiff != null;
    }

    public DfNextPreviousDiff getMaximumValueDiff() {
        return _maximumValueDiff;
    }

    public void setMaximumValueDiff(DfNextPreviousDiff maximumValueDiff) {
        _maximumValueDiff = maximumValueDiff;
    }

    public boolean hasIncrementSizeDiff() {
        return _incrementSizeDiff != null;
    }

    public DfNextPreviousDiff getIncrementSizeDiff() {
        return _incrementSizeDiff;
    }

    public void setIncrementSizeDiff(DfNextPreviousDiff incrementSizeDiff) {
        _incrementSizeDiff = incrementSizeDiff;
    }

    public boolean hasSequenceCommentDiff() {
        return _sequenceCommentDiff != null;
    }

    public DfNextPreviousDiff getSequenceCommentDiff() {
        return _sequenceCommentDiff;
    }

    public void setSequenceCommentDiff(DfNextPreviousDiff sequenceCommentDiff) {
        _sequenceCommentDiff = sequenceCommentDiff;
    }
}
