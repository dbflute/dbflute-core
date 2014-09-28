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
package org.seasar.dbflute.properties.assistant.classification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.helper.token.line.LineToken;
import org.seasar.dbflute.helper.token.line.LineTokenizingOption;
import org.seasar.dbflute.properties.DfLittleAdjustmentProperties;
import org.seasar.dbflute.util.DfNameHintUtil;
import org.seasar.dbflute.util.DfStringUtil;
import org.seasar.dbflute.util.Srl;
import org.seasar.dbflute.util.Srl.ScopeInfo;

/**
 * @author jflute
 * @since 0.8.2 (2008/10/22 Wednesday)
 */
public class DfClassificationResourceAnalyzer {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Log _log = LogFactory.getLog(DfClassificationResourceAnalyzer.class);

    protected static final String DEFAULT_ENCODING = "UTF-8";
    protected static final String LN_MARK_PLAIN = "\\n";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected List<String> _additionalLineSeparatorList;
    {
        final List<String> additionalLineSeparatorList = new ArrayList<String>();
        // base-16
        additionalLineSeparatorList.add("&#x0D;&#x0A;");
        additionalLineSeparatorList.add("&#x0A;");
        additionalLineSeparatorList.add("&#x0D;");
        additionalLineSeparatorList.add("&#xD;&#xA;");
        additionalLineSeparatorList.add("&#xA;");
        additionalLineSeparatorList.add("&#xD;");

        // base-10
        additionalLineSeparatorList.add("&#13;&#10;");
        additionalLineSeparatorList.add("&#13;");
        additionalLineSeparatorList.add("&#10;");

        _additionalLineSeparatorList = additionalLineSeparatorList;
    }

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfClassificationResourceAnalyzer() {
    }

    // ===================================================================================
    //                                                                             Analyze
    //                                                                             =======
    public List<DfClassificationTop> analyze(final String dirName, final String resourceName, final String extension) {
        final File dir = new File(dirName);
        if (!dir.exists()) {
            return new ArrayList<DfClassificationTop>();
        }
        if (!dir.isDirectory()) {
            return new ArrayList<DfClassificationTop>();
        }
        final File[] listFiles = dir.listFiles(new FileFilter() {
            public boolean accept(File currentFile) {
                if (currentFile.isDirectory()) {
                    return false;
                }
                final String currentFileName = currentFile.getName();
                if (!currentFileName.startsWith(resourceName)) {
                    return false;
                }
                if (!currentFileName.endsWith(extension)) {
                    return false;
                }
                return true;
            }
        });
        final List<DfClassificationTop> topList = new ArrayList<DfClassificationTop>();
        for (File file : listFiles) {
            final List<String> lineList;
            try {
                String encoding = extractEncoding(file);
                if (encoding == null) {
                    encoding = DEFAULT_ENCODING;
                }
                _log.info("...Analyzing classification in resource file: encoding=" + encoding);
                lineList = createLineList(file, encoding);
            } catch (RuntimeException ignored) {
                String msg = "Failed to analyze classification in resource file: ";
                msg = msg + " " + dirName + "/" + resourceName + "." + extension;
                _log.info(msg, ignored);
                continue;
            }
            final List<DfClassificationTop> classificationTopList = analyze(lineList);
            if (!classificationTopList.isEmpty()) {
                for (DfClassificationTop top : classificationTopList) {
                    _log.info("    " + top.getClassificationName() + ", " + top.getTopCommentDisp() + ", "
                            + top.isCheckClassificationCode() + ", " + top.getUndefinedHandlingType() + ", "
                            + top.isCheckImplicitSet() + ", " + top.isCheckSelectedClassification() + ", "
                            + top.isForceClassificationSetting());
                }
            } else {
                _log.info(" -> no classification in resource file");
            }
            topList.addAll(classificationTopList);
        }
        return topList;
    }

    protected List<DfClassificationTop> analyze(final List<String> lineList) {
        final List<DfClassificationTop> classificationList = new ArrayList<DfClassificationTop>();
        AnalyzedTitleLine titleLine = null;
        boolean inGroup = false;
        final int size = lineList.size();
        int index = -1;
        for (String line : lineList) {
            ++index;
            if (inGroup) {
                if (isTopLine(line)) {
                    final DfClassificationTop classificationTop = extractClassificationTop(titleLine, line);
                    classificationList.add(classificationTop);
                    if (titleLine != null) {
                        classificationTop.setRelatedColumnName(titleLine.getRelatedColumnName());
                        final String codeType = titleLine.getCodeType();
                        if (codeType != null) {
                            classificationTop.setCodeType(codeType);
                        }
                        classificationTop.setCheckImplicitSet(titleLine.isCheckImplicitSet());
                    }
                    continue;
                } else if (isElementLine(line)) {
                    final DfClassificationElement classificationElement = extractClassificationElement(line);
                    final DfClassificationTop classificationTop = classificationList.get(classificationList.size() - 1);
                    classificationTop.addClassificationElement(classificationElement);
                    continue;
                } else {
                    inGroup = false;
                    continue;
                }
            }
            if (!isTitleLine(line)) {
                continue;
            }
            final int nextIndex = index + 1;
            if (nextIndex >= size) {
                break;
            }
            final String nextLine = lineList.get(nextIndex);
            if (!isTopLine(nextLine)) {
                continue;
            }
            final int nextNextIndex = nextIndex + 1;
            if (nextNextIndex >= size) {
                break;
            }
            final String nextNextLine = lineList.get(nextNextIndex);
            if (!isElementLine(nextNextLine)) {
                continue;
            }
            titleLine = extractRelatedColumnNameFronTitleLine(line);
            inGroup = true;
        }
        return classificationList;
    }

    protected List<String> createLineList(File file, String encoding) {
        BufferedReader reader = null;
        final List<String> lineList = new ArrayList<String>();
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                line = line.trim();
                if (containsLineSeparatorMark(line)) {
                    List<String> nestedLineList = tokenizedLineSeparatorMark(line);
                    lineList.addAll(nestedLineList);
                    continue;
                } else {
                    lineList.add(line);
                }
            }
            return lineList;
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                    ignored.printStackTrace();
                }
            }
        }
    }

    protected String extractEncoding(File file) {
        final String encodingBegin = "encoding=\"";
        final String encodingEnd = "\"";
        BufferedReader reader = null;
        try {
            final String temporaryEncoding = "UTF-8";
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), temporaryEncoding));
            String encoding = null;
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (line.trim().length() == 0) {
                    continue;
                }
                if (!line.contains(encodingBegin)) {
                    break;
                }
                line = line.substring(line.indexOf(encodingBegin) + encodingBegin.length());
                if (!line.contains(encodingEnd)) {
                    break;
                }
                encoding = line.substring(0, line.indexOf(encodingEnd)).trim();
                break;
            }
            return encoding;
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                    ignored.printStackTrace();
                }
            }
        }
    }

    // ===================================================================================
    //                                                             Classification Analyzer
    //                                                             =======================
    protected boolean isTitleLine(String line) {
        return line.contains("[") && line.contains("]") && line.indexOf("[") + 1 < line.indexOf("]");
    }

    protected boolean isTopLine(String line) {
        return line.contains("$ ");
    }

    protected boolean isElementLine(String line) {
        return line.contains("- ") && line.contains(",") && line.indexOf("- ") + 1 < line.indexOf(",");
    }

    protected AnalyzedTitleLine extractRelatedColumnNameFronTitleLine(String line) {
        if (!isTitleLine(line)) {
            String msg = "The line should be title line: line=" + line;
            throw new IllegalArgumentException(msg);
        }
        final String connectBeginMark = "[";
        final String connectEndMark = "]:";
        final String wildCard = "*";
        final String prefixMark = DfNameHintUtil.PREFIX_MARK;
        final String suffixMark = DfNameHintUtil.SUFFIX_MARK;
        if (!Srl.containsAll(line, connectBeginMark, connectEndMark)) {
            return null;
        }
        line = line.trim();
        line = removeRearXmlEndIfNeeds(line);
        final AnalyzedTitleLine titleLine = new AnalyzedTitleLine();
        final ScopeInfo scopeFirst = Srl.extractScopeFirst(line, connectBeginMark, connectEndMark);
        if (scopeFirst == null) { // basically no way
            return null;
        }
        titleLine.setTitle(scopeFirst.getContent().trim());
        final String relatedColumnName;
        final String option;
        {
            String pureValue = Srl.substringFirstRear(line, connectEndMark).trim();
            if (pureValue.startsWith(wildCard)) { // *_FLG
                pureValue = suffixMark + pureValue.substring(wildCard.length());
            } else if (pureValue.endsWith(wildCard)) { // LD_*
                pureValue = pureValue.substring(0, pureValue.lastIndexOf(wildCard));
                pureValue = prefixMark + pureValue;
            }
            if (pureValue.contains("|")) {
                relatedColumnName = Srl.substringFirstFront(pureValue, "|").trim();
                option = Srl.substringFirstRear(pureValue, "|").trim();
            } else {
                relatedColumnName = pureValue;
                option = null;
            }
        }
        titleLine.setRelatedColumnName(relatedColumnName);
        if (Srl.is_NotNull_and_NotTrimmedEmpty(option)) {
            final String codeTypeNumber = DfClassificationTop.CODE_TYPE_NUMBER;
            if (Srl.containsIgnoreCase(option, codeTypeNumber)) {
                titleLine.setCodeType(codeTypeNumber);
            }
            titleLine.setCheckImplicitSet(Srl.containsIgnoreCase(option, "check"));
        }
        return titleLine;
    }

    protected static class AnalyzedTitleLine {
        protected String _title;
        protected String _relatedColumnName;
        protected String _codeType;
        protected boolean _checkImplicitSet;

        public String getTitle() {
            return _title;
        }

        public void setTitle(String title) {
            this._title = title;
        }

        public String getRelatedColumnName() {
            return _relatedColumnName;
        }

        public void setRelatedColumnName(String relatedColumnName) {
            this._relatedColumnName = relatedColumnName;
        }

        public String getCodeType() {
            return _codeType;
        }

        public void setCodeType(String codeType) {
            this._codeType = codeType;
        }

        public boolean isCheckImplicitSet() {
            return _checkImplicitSet;
        }

        public void setCheckImplicitSet(boolean checkImplicitSet) {
            this._checkImplicitSet = checkImplicitSet;
        }
    }

    protected DfClassificationTop extractClassificationTop(AnalyzedTitleLine titleLine, String line) {
        if (!isTopLine(line)) {
            String msg = "The line should be top line: line=" + line;
            throw new IllegalArgumentException(msg);
        }
        line = line.trim();
        line = removeRearXmlEndIfNeeds(line);
        line = line.substring(line.indexOf("$ ") + "$ ".length());

        final String classificationName;
        final String topDesc;
        if (line.contains(",")) {
            classificationName = line.substring(0, line.indexOf(",")).trim();
            topDesc = line.substring(line.indexOf(",") + ",".length()).trim();
        } else {
            classificationName = line.trim();
            topDesc = null;
        }
        final DfClassificationTop classificationTop = new DfClassificationTop();
        classificationTop.setClassificationName(classificationName);
        final String title = titleLine != null ? titleLine.getTitle() : null;
        final String topComment;
        if (topDesc != null) {
            if (Srl.is_NotNull_and_NotTrimmedEmpty(title) && !topDesc.startsWith(title)) {
                topComment = title + ": " + topDesc;
            } else { // title is not found or topComment starts with same word as title
                topComment = topDesc;
            }
        } else {
            topComment = title;
        }
        if (Srl.is_NotNull_and_NotTrimmedEmpty(topComment)) {
            classificationTop.setTopComment(topComment);
        }
        final DfLittleAdjustmentProperties prop = getLittleAdjustmentProperties();
        classificationTop.setCheckClassificationCode(prop.isPlainCheckClassificationCode());
        classificationTop.setUndefinedHandlingType(prop.getClassificationUndefinedHandlingType());
        // analyzed after
        //classificationTop.setCheckImplicitSet(false);
        classificationTop.setCheckSelectedClassification(prop.isCheckSelectedClassification());
        classificationTop.setForceClassificationSetting(prop.isForceClassificationSetting());
        return classificationTop;
    }

    protected DfLittleAdjustmentProperties getLittleAdjustmentProperties() {
        return DfBuildProperties.getInstance().getLittleAdjustmentProperties();
    }

    protected DfClassificationElement extractClassificationElement(String line) {
        if (!isElementLine(line)) {
            String msg = "The line should be element line: line=" + line;
            throw new IllegalArgumentException(msg);
        }
        line = line.trim();
        line = removeRearXmlEndIfNeeds(line);
        line = line.substring(line.indexOf("- ") + "- ".length());

        final String code = line.substring(0, line.indexOf(",")).trim();
        line = line.substring(line.indexOf(",") + ",".length());
        final String name;
        String alias = null;
        String comment = null;
        if (line.contains(",")) {
            name = line.substring(0, line.indexOf(",")).trim();
            line = line.substring(line.indexOf(",") + ",".length());
            if (line.contains(",")) {
                alias = line.substring(0, line.indexOf(",")).trim();
                line = line.substring(line.indexOf(",") + ",".length());
                comment = line.substring(0).trim();
            } else {
                alias = line.substring(0).trim();
            }
        } else {
            name = line.substring(0).trim();
        }
        final DfClassificationElement classificationElement = new DfClassificationElement();
        classificationElement.setCode(code);
        classificationElement.setName(name);
        classificationElement.setAlias(alias);
        classificationElement.setComment(comment);
        classificationElement.setSisters(new String[] {}); // unsupported here
        return classificationElement;
    }

    protected String removeRearXmlEndIfNeeds(String line) {
        final String endMark = "\"/>";
        if (line.endsWith(endMark)) {
            line = line.substring(0, line.lastIndexOf(endMark));
        }
        return line;
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected boolean containsLineSeparatorMark(String line) {
        if (line.contains(LN_MARK_PLAIN)) {
            return true;
        }
        for (String lineSeparator : _additionalLineSeparatorList) {
            if (line.contains(lineSeparator)) {
                return true;
            }
        }
        return false;
    }

    protected List<String> tokenizedLineSeparatorMark(String line) {
        final String baseMark = LN_MARK_PLAIN;
        for (String lineSeparator : _additionalLineSeparatorList) {
            line = DfStringUtil.replace(line, lineSeparator, baseMark);
        }
        return tokenize(line, baseMark);
    }

    protected List<String> tokenize(String value, String delimiter) {
        final LineToken lineToken = new LineToken();
        final LineTokenizingOption lineTokenizingOption = new LineTokenizingOption();
        lineTokenizingOption.setDelimiter(delimiter);
        return lineToken.tokenize(value, lineTokenizingOption);
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public void setAdditionalLineSeparatorList(List<String> additionalLineSeparatorList) {
        if (additionalLineSeparatorList == null) {
            String msg = "The argument [additionalLineSeparatorList] should not be null!";
            throw new IllegalArgumentException(msg);
        }
        this._additionalLineSeparatorList = additionalLineSeparatorList;
    }
}
