/*
 * Copyright 2014-2017 the original author or authors.
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
package org.dbflute.cbean.coption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dbflute.cbean.cipher.GearedCipherManager;
import org.dbflute.cbean.dream.SpecifiedColumn;
import org.dbflute.cbean.sqlclause.query.QueryClauseArranger;
import org.dbflute.dbway.ExtensionOperand;
import org.dbflute.dbway.OnQueryStringConnector;
import org.dbflute.twowaysql.node.FilteringBindOption;
import org.dbflute.util.DfTypeUtil;

/**
 * The option of like search.
 * <pre>
 * e.g.
 *  new LikeSearchOption().likePrefix()  : PrefixSearch
 *  new LikeSearchOption().likeContain() : ContainSearch
 *  new LikeSearchOption().likeSuffix()  : SuffixSearch
 * </pre>
 * @author jflute
 * @author h-funaki added setCompoundColumnNullAsEmpty()
 */
public class LikeSearchOption extends SimpleStringOption implements FilteringBindOption {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String LIKE_PREFIX = "prefix";
    protected static final String LIKE_SUFFIX = "suffix";
    protected static final String LIKE_CONTAIN = "contain";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String _like;
    protected String _escape;
    protected boolean _asOrSplit;
    protected List<String> _originalWildCardList;
    protected List<SpecifiedColumn> _compoundColumnList;
    protected List<Integer> _compoundColumnSizeList;
    protected boolean _nullCompoundedAsEmpty;
    protected OnQueryStringConnector _stringConnector;
    protected GearedCipherManager _cipherManager;;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Construct the option of like search normally.
     * <pre>
     * e.g.
     *  new LikeSearchOption().likePrefix()  : PrefixSearch
     *  new LikeSearchOption().likeContain() : ContainSearch
     *  new LikeSearchOption().likeSuffix()  : SuffixSearch
     * 
     *  new LikeSearchOption().likeContain().splitByBlank()
     *  new LikeSearchOption().likeContain().splitByBlank().asOrSplit()
     * </pre>
     */
    public LikeSearchOption() {
    }

    // ===================================================================================
    //                                                                         Rear Option
    //                                                                         ===========
    @Override
    public String getRearOption() {
        if (_escape == null || _escape.trim().length() == 0) {
            return "";
        }
        return " escape '" + _escape + "'";
    }

    // ===================================================================================
    //                                                                                Like
    //                                                                                ====
    /**
     * Set up prefix-search. e.g. like 'foo%' escape '|'
     * @return this. (NotNull)
     */
    public LikeSearchOption likePrefix() {
        _like = LIKE_PREFIX;
        doLikeAutoEscape();
        return this;
    }

    /**
     * Set up suffix-search. e.g. like '%foo' escape '|'
     * @return this. (NotNull)
     */
    public LikeSearchOption likeSuffix() {
        _like = LIKE_SUFFIX;
        doLikeAutoEscape();
        return this;
    }

    /**
     * Set up contain-search. e.g. like '%foo%' escape '|'
     * @return this. (NotNull)
     */
    public LikeSearchOption likeContain() {
        _like = LIKE_CONTAIN;
        doLikeAutoEscape();
        return this;
    }

    protected void doLikeAutoEscape() {
        escape();
    }

    public boolean isLikePrefix() {
        return LIKE_PREFIX.equals(_like);
    }

    public boolean isLikeSuffix() {
        return LIKE_SUFFIX.equals(_like);
    }

    public boolean isLikeContain() {
        return LIKE_CONTAIN.equals(_like);
    }

    // ===================================================================================
    //                                                                              Escape
    //                                                                              ======
    /**
     * Escape like search by pipe-line '|'.
     * @return The option of like search. (NotNull)
     */
    public LikeSearchOption escape() {
        return escapeByPipeLine();
    }

    public LikeSearchOption escapeByPipeLine() {
        _escape = "|";
        return this;
    }

    public LikeSearchOption escapeByAtMark() {
        _escape = "@";
        return this;
    }

    public LikeSearchOption escapeBySlash() {
        _escape = "/";
        return this;
    }

    public LikeSearchOption escapeByBackSlash() {
        _escape = "\\";
        return this;
    }

    public LikeSearchOption notEscape() {
        _escape = null;
        return this;
    }

    // ===================================================================================
    //                                                                               Split
    //                                                                               =====
    /**
     * Split a value as several condition by blank (space, full-width space, tab, CR, LF).
     * <pre>
     * e.g. 'and' condition
     * LikeSearchOption option = new LikeSearchOption();
     * option.likeContain().<span style="color: #CC4747">splitByBlank()</span>;
     * cb.query().setFoo_ContainSearch("ab g kl", option);
     * <span style="color: #3F7E5E">// FOO like '%ab%' and FOO like '%g%' and FOO like '%kl%'</span>
     * <span style="color: #3F7E5E">// (all conditions have escape statements)</span>
     * 
     * e.g. 'or' condition
     * LikeSearchOption option = new LikeSearchOption();
     * option.likeContain().splitByBlank().<span style="color: #CC4747">asOrSplit()</span>;
     * cb.query().setFoo_ContainSearch("ab g kl", option);
     * <span style="color: #3F7E5E">// FOO like '%ab%' or FOO like '%g%' or FOO like '%kl%'</span>
     * <span style="color: #3F7E5E">// (conditions have escape statements)</span>
     * </pre>
     * @return this. (NotNull)
     */
    public LikeSearchOption splitByBlank() {
        assertSplitByPrecondition();
        return (LikeSearchOption) doSplitByBlank();
    }

    // memorable codes...
    /**
     * Split a value as several condition by half-size space.
     * @return this. (NotNull)
     */
    public LikeSearchOption splitBySpace() {
        assertSplitByPrecondition();
        return (LikeSearchOption) doSplitBySpace();
    }

    /**
     * Split a value as several condition by space that contains full-width space.
     * @return this. (NotNull)
     */
    public LikeSearchOption splitBySpaceContainsDoubleByte() {
        assertSplitByPrecondition();
        return (LikeSearchOption) doSplitBySpaceContainsDoubleByte();
    }

    /**
     * Split a value as several condition by pipeline.
     * @return this. (NotNull)
     */
    public LikeSearchOption splitByPipeLine() {
        assertSplitByPrecondition();
        return (LikeSearchOption) doSplitByPipeLine();
    }

    /**
     * Split a value as several condition by specified various delimiters.
     * @param delimiterList The list of delimiter for split. (NotNull, NotEmpty)
     * @return this. (NotNull)
     */
    public LikeSearchOption splitByVarious(List<String> delimiterList) {
        assertSplitByPrecondition();
        return (LikeSearchOption) doSplitByVarious(delimiterList);
    }

    protected void assertSplitByPrecondition() {
        if (hasCompoundColumn()) {
            String msg = "The Split of LikeSearch is unsupported with CompoundColumn.";
            throw new IllegalStateException(msg);
        }
    }

    /**
     * Split as OR condition. <br >
     * You should call this with a splitByXxx method.
     * @return this. (NotNull)
     */
    public LikeSearchOption asOrSplit() {
        _asOrSplit = true;
        return this;
    }

    public boolean isAsOrSplit() {
        return _asOrSplit;
    }

    /**
     * Cut the large split (set limit count for split).
     * @param splitLimitCount The limit count of split. (NotZero, NotMinus)
     * @return this.
     */
    public LikeSearchOption cutSplit(int splitLimitCount) {
        doCutSplit(splitLimitCount);
        return this;
    }

    // ===================================================================================
    //                                                                     Compound Column
    //                                                                     ===============
    // -----------------------------------------------------
    //                                          Dream Cruise
    //                                          ------------
    /**
     * Add compound column connected to main column. {Dream Cruise}
     * <pre>
     * e.g. LikeSearch: MEMBER_NAME || MEMBER_ACCOUNT like ...
     *  MemberCB cb = new MemberCB();
     *  LikeSearchOption option = new LikeSearchOption().likeContain()
     *      .<span style="color: #CC4747">addCompoundColumn</span>(cb.<span style="color: #CC4747">dreamCruiseCB()</span>.specify().columnMemberAccount());
     *  cb.query().setMemberName_LikeSearch("S", option);
     * 
     * If any selected value is null, the compounded value is also null as default (even others are not null).
     * If some of compounded columns are allowed to be null, 
     * setCompoundColumnNullAsEmpty() is recommended as LikeSearchOption.
     * </pre>
     * @param compoundColumn The compound column specified by Dream Cruise. (NotNull)
     * @return this. (NotNull)
     */
    public LikeSearchOption addCompoundColumn(SpecifiedColumn compoundColumn) {
        assertCompoundColumnPrecondition(compoundColumn);
        assertCompoundColumnSpecifiedColumn(compoundColumn);
        if (_compoundColumnList == null) {
            _compoundColumnList = new ArrayList<SpecifiedColumn>();
        }
        _compoundColumnList.add(compoundColumn);
        return this;
    }

    protected void assertCompoundColumnPrecondition(SpecifiedColumn compoundColumn) {
        if (isSplit()) {
            String msg = "The CompoundColumn of LikeSearch is unsupported with Split: " + compoundColumn;
            throw new IllegalStateException(msg);
        }
    }

    protected void assertCompoundColumnSpecifiedColumn(SpecifiedColumn compoundColumn) {
        if (compoundColumn == null) {
            String msg = "The argument 'compoundColumn' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!compoundColumn.getColumnInfo().isObjectNativeTypeString()) {
            String msg = "The type of the compound column should be String: " + compoundColumn;
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    public boolean hasCompoundColumn() {
        return _compoundColumnList != null && !_compoundColumnList.isEmpty();
    }

    @Override
    public List<SpecifiedColumn> getCompoundColumnList() {
        return _compoundColumnList;
    }

    public void compoundsNullAsEmpty() {
        _nullCompoundedAsEmpty = true;
    }

    @Override
    public boolean isNullCompoundedAsEmpty() {
        return _nullCompoundedAsEmpty;
    }

    public void clearCompoundColumn() {
        if (_compoundColumnList != null) {
            _compoundColumnList.clear();
        }
        if (_compoundColumnSizeList != null) {
            _compoundColumnSizeList.clear();
        }
    }

    // -----------------------------------------------------
    //                                          Optimization
    //                                          ------------
    /**
     * Optimize compound columns by fixed size. <br>
     * The columns specified their sizes should be fixed-size string type 'char'. (but no check so attention)
     * @param sizes The array of column size for main column and compound columns. (NotNull)
     * @return this. (NotNull)
     */
    public LikeSearchOption optimizeCompoundColumnByFixedSize(Integer... sizes) {
        if (!hasCompoundColumn()) {
            String msg = "The CompoundColumnOptimization needs CompoundColumn.";
            throw new IllegalStateException(msg);
        }
        if (sizes.length > (_compoundColumnList.size() + 1)) {
            String msg = "The length of argument 'sizes' should be less or equal count of compound columns + 1.";
            msg = msg + " sizes.length=" + sizes.length;
            msg = msg + " compoundColumnList.size()=" + _compoundColumnList.size();
            throw new IllegalArgumentException(msg);
        }
        _compoundColumnSizeList = Arrays.asList(sizes);
        return this;
    }

    public boolean canOptimizeCompoundColumnLikePrefix() {
        return hasCompoundColumn() && hasCompoundColumnOptimization() && isLikePrefix();
    }

    public boolean hasCompoundColumnOptimization() {
        return _compoundColumnSizeList != null && !_compoundColumnSizeList.isEmpty();
    }

    public List<Integer> getCompoundColumnSizeList() {
        return _compoundColumnSizeList;
    }

    // -----------------------------------------------------
    //                                       StringConnector
    //                                       ---------------
    // called after being set to condition-query or parameter-bean
    // for DBMS that has original string connection way
    //  e.g. MySQL, SQLServer

    public LikeSearchOption acceptStringConnector(OnQueryStringConnector stringConnector) {
        _stringConnector = stringConnector;
        return this;
    }

    public boolean hasStringConnector() {
        return _stringConnector != null;
    }

    public OnQueryStringConnector getStringConnector() {
        return _stringConnector;
    }

    // ===================================================================================
    //                                                                          Real Value
    //                                                                          ==========
    @Override
    public String generateRealValue(String value) {
        value = super.generateRealValue(value);

        // escape wild-cards
        if (_escape != null && _escape.trim().length() != 0) {
            String tmp = replace(value, _escape, _escape + _escape);

            // basic wild-cards
            tmp = filterEscape(tmp, "%");
            tmp = filterEscape(tmp, "_");

            if (_originalWildCardList != null) {
                for (String wildCard : _originalWildCardList) {
                    tmp = filterEscape(tmp, wildCard);
                }
            }

            value = tmp;
        }
        final String wildCard = "%";
        if (_like == null || _like.trim().length() == 0) {
            return value;
        } else if (_like.equals(LIKE_PREFIX)) {
            return value + wildCard;
        } else if (_like.equals(LIKE_SUFFIX)) {
            return wildCard + value;
        } else if (_like.equals(LIKE_CONTAIN)) {
            return wildCard + value + wildCard;
        } else {
            String msg = "The like was wrong string: " + _like;
            throw new IllegalStateException(msg);
        }
    }

    protected String filterEscape(String target, String wildCard) {
        return replace(target, wildCard, _escape + wildCard);
    }

    // called after being set to condition-query or parameter-bean
    // for DBMS that does not ignore an unused escape character
    //  e.g. Oracle, Apache Derby

    public LikeSearchOption acceptOriginalWildCardList(List<String> originalWildCardList) {
        _originalWildCardList = originalWildCardList;
        return this;
    }

    // ===================================================================================
    //                                                                           Deep Copy
    //                                                                           =========
    @Override
    public LikeSearchOption createDeepCopy() {
        final LikeSearchOption copy = (LikeSearchOption) super.createDeepCopy();
        copy._like = _like;
        copy._escape = _escape;
        copy._asOrSplit = _asOrSplit;
        if (_originalWildCardList != null) {
            copy._originalWildCardList = new ArrayList<String>(_originalWildCardList);
        }
        if (_compoundColumnList != null) {
            copy._compoundColumnList = new ArrayList<SpecifiedColumn>(_compoundColumnList);
        }
        if (_compoundColumnSizeList != null) {
            copy._compoundColumnSizeList = new ArrayList<Integer>(_compoundColumnSizeList);
        }
        copy._stringConnector = _stringConnector;
        return copy;
    }

    @Override
    protected LikeSearchOption newDeepCopyInstance() {
        return new LikeSearchOption();
    }

    // ===================================================================================
    //                                                                   Extension Operand
    //                                                                   =================
    /**
     * Get the operand for extension.
     * @return The operand for extension. (NullAllowed: If the value is null, it means no extension)
     */
    @Override
    public ExtensionOperand getExtensionOperand() { // for application extension
        return null; // as default
    }

    // ===================================================================================
    //                                                               Where Clause Arranger
    //                                                               =====================
    /**
     * Get the arranger of where clause.
     * @return The arranger of where clause. (NullAllowed: If the value is null, it means no arrangement)
     */
    @Override
    public QueryClauseArranger getWhereClauseArranger() { // for application extension
        return null; // as default
    }

    // ===================================================================================
    //                                                                       Geared Cipher
    //                                                                       =============
    /**
     * Accept the manager of geared cipher. (basically for compound columns)
     * @param cipherManager The manager of geared cipher. (NullAllowed)
     */
    public void acceptGearedCipherManager(GearedCipherManager cipherManager) {
        _cipherManager = cipherManager;
    }

    @Override
    public GearedCipherManager getGearedCipherManager() {
        return _cipherManager;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        final String title = DfTypeUtil.toClassTitle(this);
        final String split = (isSplit() ? (_asOrSplit ? "true(or)" : "true(and)") : "false");
        return title + ":{like=" + _like + ", escape=" + _escape + ", split=" + split + "}";
    }
}
