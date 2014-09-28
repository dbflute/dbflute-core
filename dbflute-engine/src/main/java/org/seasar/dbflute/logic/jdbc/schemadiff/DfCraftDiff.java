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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.exception.DfCraftDiffIllegalCraftKeyNameException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.token.file.FileToken;
import org.seasar.dbflute.helper.token.file.FileTokenizingCallback;
import org.seasar.dbflute.helper.token.file.FileTokenizingHeaderInfo;
import org.seasar.dbflute.helper.token.file.FileTokenizingOption;
import org.seasar.dbflute.helper.token.file.FileTokenizingRowResource;
import org.seasar.dbflute.properties.DfDocumentProperties;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.9.9.8 (2012/09/04 Tuesday)
 */
public class DfCraftDiff extends DfAbstractDiff {

    // ===============================================================================
    //                                                                      Definition
    //                                                                      ==========
    private static final Log _log = LogFactory.getLog(DfCraftDiff.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Set<String> _craftTitleSet = DfCollectionUtil.newLinkedHashSet();
    protected final List<DfCraftTitleDiff> _craftTitleDiffList = DfCollectionUtil.newArrayList();

    // map:{craftTitle = map:{craftKey : craftMeta}}
    protected Map<String, Map<String, DfCraftValue>> _nextTitleCraftValueMap;
    protected Map<String, Map<String, DfCraftValue>> _previousTitleCraftValueMap;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfCraftDiff() {
    }

    protected static class DfCraftValue {
        protected final String _craftKeyName;
        protected final String _craftValue;

        public DfCraftValue(String craftKeyName, String craftValue) {
            _craftKeyName = craftKeyName;
            _craftValue = craftValue;
        }

        public String getCraftKeyName() {
            return _craftKeyName;
        }

        public String getCraftValue() {
            return _craftValue;
        }
    }

    // ===================================================================================
    //                                                                        Analyze Diff
    //                                                                        ============
    public void analyzeDiff(String metaDirPath) {
        loadMeta(metaDirPath);
        if (!existsCraftMeta()) {
            return;
        }
        processCraftDiff();
    }

    protected boolean existsCraftMeta() {
        return _nextTitleCraftValueMap != null && _previousTitleCraftValueMap != null;
    }

    // ===================================================================================
    //                                                                     Loading Process
    //                                                                     ===============
    protected void loadMeta(String craftMetaDir) {
        final File metaDir = new File(craftMetaDir);
        if (!metaDir.exists() || !metaDir.isDirectory()) {
            return;
        }
        final List<File> metaFileList = getCraftMetaFileList(craftMetaDir);
        if (metaFileList.isEmpty()) { // empty directory or no check
            return;
        }
        _log.info("...Loading craft meta: " + craftMetaDir);
        for (final File metaFile : metaFileList) {
            final String craftTitle = extractCraftTitle(metaFile);
            _craftTitleSet.add(craftTitle);
            final boolean next = isCraftDirectionNext(metaFile);
            final FileToken fileToken = new FileToken();
            final List<DfCraftValue> craftValueList = DfCollectionUtil.newArrayList();
            try {
                tokenize(metaFile, fileToken, craftValueList);
            } catch (IOException e) {
                String msg = "Failed to read the file: " + metaFile;
                throw new IllegalStateException(msg, e);
            }
            _log.info("  " + metaFile.getName() + ": rowCount=" + craftValueList.size());
            if (next) {
                registerNextMeta(craftTitle, craftValueList);
            } else {
                registerPreviousMeta(craftTitle, craftValueList);
            }
        }
    }

    protected void tokenize(final File metaFile, FileToken fileToken, final List<DfCraftValue> craftValueList)
            throws FileNotFoundException, IOException {
        fileToken.tokenize(new FileInputStream(metaFile), new FileTokenizingCallback() {
            private final Set<String> _craftKeyNameSet = DfCollectionUtil.newHashSet(); // for duplicate check

            public void handleRow(FileTokenizingRowResource resource) {
                final List<String> columnValueList = DfCollectionUtil.newArrayList(resource.getValueList()); // snapshot
                final String craftKeyName = columnValueList.remove(0); // it's the first item fixedly
                assertCraftKeyExists(craftKeyName, metaFile, resource);
                assertUniqueCraftKey(craftKeyName, metaFile, resource, _craftKeyNameSet);
                craftValueList.add(createCraftValue(craftKeyName, columnValueList));
            }
        }, new FileTokenizingOption().delimitateByTab().encodeAsUTF8().handleEmptyAsNull());
    }

    // -----------------------------------------------------
    //                                           Craft Value
    //                                           -----------
    protected DfCraftValue createCraftValue(String craftKeyName, List<String> craftValueList) {
        return new DfCraftValue(filterCraftKeyName(craftKeyName), buildCraftValue(craftValueList));
    }

    protected String filterCraftKeyName(String craftKeyName) {
        if (craftKeyName.equalsIgnoreCase("null")) {
            return Srl.quoteDouble(craftKeyName); // because map file treats 'null' as null
        }
        return craftKeyName;
    }

    protected String buildCraftValue(final List<String> craftValueList) {
        final StringBuilder sb = new StringBuilder();
        for (String craftValue : craftValueList) {
            if (sb.length() > 0) {
                sb.append("|");
            }
            if (craftValue != null && craftValue.equalsIgnoreCase("null")) {
                sb.append(Srl.quoteDouble(craftValue)); // null string
            } else {
                sb.append(craftValue); // null or normal string
            }
        }
        return sb.toString();
    }

    // -----------------------------------------------------
    //                                         Register Meta
    //                                         -------------
    protected void registerNextMeta(String craftTitle, List<DfCraftValue> valueList) {
        if (_nextTitleCraftValueMap == null) {
            _nextTitleCraftValueMap = DfCollectionUtil.newLinkedHashMap();
        }
        doRegisterMeta(craftTitle, valueList, _nextTitleCraftValueMap);
    }

    protected void registerPreviousMeta(String craftTitle, List<DfCraftValue> valueList) {
        if (_previousTitleCraftValueMap == null) {
            _previousTitleCraftValueMap = DfCollectionUtil.newLinkedHashMap();
        }
        doRegisterMeta(craftTitle, valueList, _previousTitleCraftValueMap);
    }

    protected void doRegisterMeta(String craftTitle, List<DfCraftValue> craftValueList,
            Map<String, Map<String, DfCraftValue>> titleValueMap) {
        Map<String, DfCraftValue> valueMap = titleValueMap.get(craftTitle);
        if (valueMap == null) {
            valueMap = DfCollectionUtil.newLinkedHashMap();
            titleValueMap.put(craftTitle, valueMap);
        }
        for (DfCraftValue craftValue : craftValueList) {
            valueMap.put(craftValue.getCraftKeyName(), craftValue);
        }
    }

    // -----------------------------------------------------
    //                                       Assert CraftKey
    //                                       ---------------
    protected void assertCraftKeyExists(String craftKeyName, File metaFile, FileTokenizingRowResource rowResource) {
        if (craftKeyName == null) {
            // basically no way because already checked when dump but just in case
            throwCraftDiffCraftKeyNameHasNullException(metaFile, rowResource);
        }
    }

    protected void throwCraftDiffCraftKeyNameHasNullException(File metaFile, FileTokenizingRowResource rowResource) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The craft key name has null.");
        final FileTokenizingHeaderInfo headerInfo = rowResource.getHeaderInfo();
        if (!headerInfo.isEmpty()) {
            br.addItem("CraftKey Column");
            br.addElement(headerInfo.getColumnNameList().get(0));
        }
        br.addItem("Line Number");
        br.addElement(rowResource.getLineNumber());
        br.addItem("Row Number");
        br.addElement(rowResource.getRowNumber());
        br.addItem("Row String");
        br.addElement(rowResource.getRowString());
        br.addItem("Meta File");
        br.addElement(metaFile.getPath());
        final String msg = br.buildExceptionMessage();
        throw new DfCraftDiffIllegalCraftKeyNameException(msg);
    }

    protected void assertUniqueCraftKey(String craftKeyName, File metaFile, FileTokenizingRowResource rowResource,
            Set<String> craftKeyNameSet) {
        if (craftKeyNameSet.contains(craftKeyName)) {
            // basically no way because already checked when dump but just in case
            throwCraftDiffCraftKeyNameDuplicateException(craftKeyName, metaFile, rowResource);
        }
        craftKeyNameSet.add(craftKeyName);
    }

    protected void throwCraftDiffCraftKeyNameDuplicateException(String craftKeyName, File metaFile,
            FileTokenizingRowResource rowResource) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("The craft key name has duplicate entry.");
        final FileTokenizingHeaderInfo headerInfo = rowResource.getHeaderInfo();
        if (!headerInfo.isEmpty()) {
            br.addItem("CraftKey Column");
            br.addElement(headerInfo.getColumnNameList().get(0));
        }
        br.addItem("Duplicate Key");
        br.addElement(craftKeyName);
        br.addItem("Meta File");
        br.addElement(metaFile.getPath());
        final String msg = br.buildExceptionMessage();
        throw new DfCraftDiffIllegalCraftKeyNameException(msg);
    }

    // ===================================================================================
    //                                                                   CraftDiff Process
    //                                                                   =================
    protected void processCraftDiff() {
        for (Entry<String, Map<String, DfCraftValue>> entry : _nextTitleCraftValueMap.entrySet()) {
            final String craftTitle = entry.getKey();
            final DfCraftTitleDiff titleDiff = DfCraftTitleDiff.create(craftTitle);
            if (!hasPreviousCraftValue(titleDiff)) { // means no meta at previous time
                continue; // out of target
            }
            addCraftTitleDiff(titleDiff);
            doProcessCraftDiff(titleDiff);
        }
    }

    protected void doProcessCraftDiff(DfCraftTitleDiff titleDiff) {
        processAddedCraft(titleDiff);
        processChangedCraft(titleDiff);
        processDeletedCraft(titleDiff);
    }

    // -----------------------------------------------------
    //                                                 Added
    //                                                 -----
    protected void processAddedCraft(DfCraftTitleDiff titleDiff) {
        final Map<String, DfCraftValue> nextValueMap = findNextValueMap(titleDiff); // exists here
        for (Entry<String, DfCraftValue> entry : nextValueMap.entrySet()) {
            final DfCraftValue craftValue = entry.getValue();
            final DfCraftValue found = findPreviousCraftValue(titleDiff, craftValue);
            if (found == null || !isSameCraftKeyName(craftValue, found)) { // added
                titleDiff.addCraftRowDiff(DfCraftRowDiff.createAdded(craftValue.getCraftKeyName()));
            }
        }
    }

    protected Map<String, DfCraftValue> findNextValueMap(DfCraftTitleDiff titleDiff) {
        final String craftTitle = titleDiff.getKeyName();
        return _nextTitleCraftValueMap.get(craftTitle); // always exists here
    }

    // -----------------------------------------------------
    //                                               Changed
    //                                               -------
    protected void processChangedCraft(DfCraftTitleDiff titleDiff) {
        final Map<String, DfCraftValue> nextValueMap = findNextValueMap(titleDiff); // exists here
        for (Entry<String, DfCraftValue> entry : nextValueMap.entrySet()) {
            final DfCraftValue next = entry.getValue();
            final DfCraftValue previous = findPreviousCraftValue(titleDiff, next);
            if (previous == null || !isSameCraftKeyName(next, previous)) {
                continue;
            }
            // found
            final DfCraftRowDiff craftDiff = DfCraftRowDiff.createChanged(next.getCraftKeyName());

            // only one item
            processCraftValue(next, previous, craftDiff);

            if (craftDiff.hasDiff()) { // changed
                titleDiff.addCraftRowDiff(craftDiff);
            }
        }
    }

    protected void processCraftValue(DfCraftValue next, DfCraftValue previous, DfCraftRowDiff craftDiff) {
        diffNextPrevious(next, previous, craftDiff, new StringNextPreviousDiffer<DfCraftValue, DfCraftRowDiff>() {
            public String provide(DfCraftValue obj) {
                return obj.getCraftValue();
            }

            public void diff(DfCraftRowDiff diff, DfNextPreviousDiff nextPreviousDiff) {
                diff.setCraftValueDiff(nextPreviousDiff);
            }
        });
    }

    protected void diffNextPrevious(DfCraftValue next, DfCraftValue previous, DfCraftRowDiff diff,
            StringNextPreviousDiffer<DfCraftValue, DfCraftRowDiff> differ) {
        final String nextValue = differ.provide(next);
        final String previousValue = differ.provide(previous);
        final String nextDiffValue;
        final String previousDiffValue;
        if (needsToHash(nextValue, previousValue)) {
            nextDiffValue = nextValue != null ? convertToHash(nextValue) : null;
            previousDiffValue = previousValue != null ? convertToHash(previousValue) : null;
        } else {
            nextDiffValue = nextValue;
            previousDiffValue = previousValue;
        }
        doDiffNextPrevious(diff, differ, nextDiffValue, previousDiffValue);
    }

    protected void doDiffNextPrevious(DfCraftRowDiff diff,
            StringNextPreviousDiffer<DfCraftValue, DfCraftRowDiff> differ, final String nextValue,
            final String previousValue) {
        if (!differ.isMatch(nextValue, previousValue)) {
            final String nextDisp = differ.disp(nextValue, true);
            final String previousDisp = differ.disp(previousValue, false);
            differ.diff(diff, createNextPreviousDiff(nextDisp, previousDisp));
        }
    }

    // -----------------------------------------------------
    //                                               Deleted
    //                                               -------
    protected void processDeletedCraft(DfCraftTitleDiff titleDiff) {
        final Map<String, DfCraftValue> previousValueMap = findPreviousCraftValueMap(titleDiff); // exists here
        for (Entry<String, DfCraftValue> entry : previousValueMap.entrySet()) {
            final DfCraftValue craftValue = entry.getValue();
            final DfCraftValue found = findNextCraftDiffData(titleDiff, craftValue);
            if (found == null || !isSameCraftKeyName(craftValue, found)) { // deleted
                titleDiff.addCraftRowDiff(DfCraftRowDiff.createDeleted(craftValue.getCraftKeyName()));
            }
        }
    }

    // -----------------------------------------------------
    //                                         Assist Helper
    //                                         -------------
    protected boolean isSameCraftKeyName(DfCraftValue next, DfCraftValue previous) {
        return isSame(next.getCraftKeyName(), previous.getCraftKeyName());
    }

    // ===================================================================================
    //                                                                          Hash Logic
    //                                                                          ==========
    protected boolean needsToHash(String nextValue, String previousValue) {
        return containsLineSeparator(nextValue, previousValue) || isOverLength(nextValue, previousValue);
    }

    protected boolean containsLineSeparator(String... values) {
        for (String value : values) {
            if (value != null && value.contains("\n")) {
                return true;
            }
        }
        return false;
    }

    protected boolean isOverLength(String... values) {
        for (String value : values) {
            if (value != null && value.length() > 100) {
                return true;
            }
        }
        return false;
    }

    protected String convertToHash(String value) {
        final StringBuilder nextSb = new StringBuilder();
        nextSb.append(Srl.count(value, "\n") + 1).append(":"); // line
        nextSb.append(value.length()).append(":"); // length
        nextSb.append(Integer.toHexString(value.hashCode())); // hash
        return nextSb.toString();
    }

    // ===================================================================================
    //                                                                         Find Object
    //                                                                         ===========
    // -----------------------------------------------------
    //                                                  Next
    //                                                  ----
    protected boolean hasNextCraftValue(DfCraftTitleDiff titleDiff) {
        return findNextCraftValueMap(titleDiff) != null;
    }

    protected DfCraftValue findNextCraftDiffData(DfCraftTitleDiff titleDiff, DfCraftValue craftValue) {
        final Map<String, DfCraftValue> metaMap = _nextTitleCraftValueMap.get(titleDiff.getKeyName());
        return metaMap != null ? metaMap.get(craftValue.getCraftKeyName()) : null;
    }

    protected Map<String, DfCraftValue> findNextCraftValueMap(DfCraftTitleDiff titleDiff) {
        return _nextTitleCraftValueMap.get(titleDiff.getKeyName());
    }

    // -----------------------------------------------------
    //                                              Previous
    //                                              --------
    protected boolean hasPreviousCraftValue(DfCraftTitleDiff titleDiff) {
        return findPreviousCraftValueMap(titleDiff) != null;
    }

    protected DfCraftValue findPreviousCraftValue(DfCraftTitleDiff titleDiff, DfCraftValue craftValue) {
        final Map<String, DfCraftValue> metaMap = _previousTitleCraftValueMap.get(titleDiff.getKeyName());
        return metaMap != null ? metaMap.get(craftValue.getCraftKeyName()) : null;
    }

    protected Map<String, DfCraftValue> findPreviousCraftValueMap(DfCraftTitleDiff titleDiff) {
        return _previousTitleCraftValueMap.get(titleDiff.getKeyName());
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfDocumentProperties getDocumentProperties() {
        return getProperties().getDocumentProperties();
    }

    protected List<File> getCraftMetaFileList(String craftMetaDir) {
        return getDocumentProperties().getCraftMetaFileList(craftMetaDir);
    }

    protected String extractCraftTitle(File metaFile) {
        return getDocumentProperties().extractCraftTitle(metaFile);
    }

    protected boolean isCraftDirectionNext(File metaFile) {
        return getDocumentProperties().isCraftDirectionNext(metaFile);
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public List<DfCraftTitleDiff> getCraftTitleDiffList() {
        return _craftTitleDiffList;
    }

    public void addCraftTitleDiff(DfCraftTitleDiff craftTitleDiff) {
        _craftTitleDiffList.add(craftTitleDiff);
    }
}
