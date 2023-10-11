/*
 * Copyright 2014-2023 the original author or authors.
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
import java.util.Collections;
import java.util.List;

import org.dbflute.cbean.cipher.GearedCipherManager;
import org.dbflute.cbean.dream.SpecifiedColumn;
import org.dbflute.cbean.sqlclause.query.QueryClauseArranger;
import org.dbflute.dbway.topic.ExtensionOperand;
import org.dbflute.dbway.topic.OnQueryStringConnector;
import org.dbflute.twowaysql.node.FilteringBindOption;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfTypeUtil;

// very memorable code for me
/**
 * The condition option of like-search.
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
    // -----------------------------------------------------
    //                                                 Basic
    //                                                 -----
    protected String _like; // null allowed
    protected String _escape; // null allowed
    protected boolean _asOrSplit;

    // -----------------------------------------------------
    //                                       Compound Column
    //                                       ---------------
    protected List<SpecifiedColumn> _compoundColumnList; // null allowed
    protected List<Integer> _compoundColumnSizeList; // null allowed
    protected boolean _nullCompoundedAsEmpty;
    protected OnQueryStringConnector _stringConnector; // null allowed

    // -----------------------------------------------------
    //                                     Clause Adjustment
    //                                     -----------------
    protected List<String> _originalWildCardList; // null allowed
    protected ExtensionOperand _extensionOperand; // null allowed, since 1.2.7
    protected QueryClauseArranger _whereClauseArranger; // null allowed, since 1.2.7
    protected GearedCipherManager _cipherManager; // null allowed

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
        return _compoundColumnList != null ? Collections.unmodifiableList(_compoundColumnList) : DfCollectionUtil.emptyList();
    }

    /**
     * Use coalesce() for compound columns to filter null as empty.
     * @return this. (NotNull)
     */
    public LikeSearchOption compoundsNullAsEmpty() {
        _nullCompoundedAsEmpty = true;
        return this;
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
     * <pre>
     * e.g. SEA_MYSTIC char(9), LAND_ONEMAN char(20)
     *  cb.query().setSeaMystic_LikeSearch("StojkovicPix", op -&gt; {
     *      op.likePrefix();
     *      op.addCompoundColumn(dreamCruiseCB.specify().columnLandOneman());
     *      op.optimizeCompoundColumnByFixedSize(9, 20);
     *  });
     * // where dfloc.SEA_MYSTIC = 'Stojkovic'
     * //   and dfloc.LAND_ONEMAN like 'Pix%' escape '|'
     * </pre>
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

    public List<Integer> getCompoundColumnSizeList() { // read-only
        return _compoundColumnSizeList != null ? Collections.unmodifiableList(_compoundColumnSizeList) : DfCollectionUtil.emptyList();
    }

    // -----------------------------------------------------
    //                                       StringConnector
    //                                       ---------------
    // should be called after being set to condition-query or parameter-bean
    // for DBMS that has original string connection way
    //  e.g. MySQL, SQLServer
    public LikeSearchOption acceptStringConnector(OnQueryStringConnector stringConnector) {
        _stringConnector = stringConnector;
        return this;
    }

    @Override
    public boolean hasStringConnector() {
        return _stringConnector != null;
    }

    @Override
    public OnQueryStringConnector getStringConnector() {
        return _stringConnector;
    }

    // ===================================================================================
    //                                                                   Clause Adjustment
    //                                                                   =================
    // -----------------------------------------------------
    //                                      Orginal WildCard
    //                                      ----------------
    // should be called after being set to condition-query or parameter-bean
    // for DBMS that does not ignore an unused escape character
    //  e.g. Oracle, Apache Derby
    /**
     * Accept the list of your original wild-card for e.g. DBMS customization.
     * @param originalWildCardList The list of string wild-card. (NullAllowed)
     * @return this. (NotNull)
     */
    public LikeSearchOption acceptOriginalWildCardList(List<String> originalWildCardList) {
        _originalWildCardList = originalWildCardList;
        return this;
    }

    public List<String> getOriginalWildCardList() { // read-only
        return _originalWildCardList != null ? Collections.unmodifiableList(_originalWildCardList) : DfCollectionUtil.emptyList();
    }

    // -----------------------------------------------------
    //                                     Extension Operand
    //                                     -----------------
    /**
     * Accept your original operand as application extension instead of "like".
     * @param extensionOperand The interface that provides your operand. (NullAllowed)
     * @return this. (NotNull)
     */
    public LikeSearchOption acceptExtensionOperand(ExtensionOperand extensionOperand) { // since 1.2.7
        _extensionOperand = extensionOperand;
        return this;
    }

    @Override
    public ExtensionOperand getExtensionOperand() {
        return _extensionOperand;
    }

    // -----------------------------------------------------
    //                                 Where Clause Arranger
    //                                 ---------------------
    /**
     * Accept arranger of query clause (like clause) for e.g. collate.
     * @param whereClauseArranger The interface that arranges your clause. (NullAllowed)
     * @return this. (NotNull)
     */
    public LikeSearchOption acceptWhereClauseArranger(QueryClauseArranger whereClauseArranger) { // since 1.2.7
        _whereClauseArranger = whereClauseArranger;
        return this;
    }

    @Override
    public QueryClauseArranger getWhereClauseArranger() {
        return _whereClauseArranger;
    }

    // -----------------------------------------------------
    //                                         Geared Cipher
    //                                         -------------
    /**
     * Accept the manager of geared cipher. (basically for compound columns)
     * @param cipherManager The manager of geared cipher. (NullAllowed)
     * @return this. (NotNull)
     */
    public LikeSearchOption acceptGearedCipherManager(GearedCipherManager cipherManager) {
        _cipherManager = cipherManager;
        return this;
    }

    @Override
    public GearedCipherManager getGearedCipherManager() {
        return _cipherManager;
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

    // ===================================================================================
    //                                                                           Deep Copy
    //                                                                           =========
    @Override
    public LikeSearchOption createDeepCopy() {
        final LikeSearchOption copy = (LikeSearchOption) super.createDeepCopy();

        // basic
        copy._like = _like;
        copy._escape = _escape;
        copy._asOrSplit = _asOrSplit;

        // compound column
        if (_compoundColumnList != null) {
            copy._compoundColumnList = new ArrayList<SpecifiedColumn>(_compoundColumnList);
        }
        if (_compoundColumnSizeList != null) {
            copy._compoundColumnSizeList = new ArrayList<Integer>(_compoundColumnSizeList);
        }
        copy._stringConnector = _stringConnector;

        // clause adjustment
        if (_originalWildCardList != null) {
            copy._originalWildCardList = new ArrayList<String>(_originalWildCardList);
        } else {
            copy._originalWildCardList = null;
        }
        copy._extensionOperand = _extensionOperand;
        copy._whereClauseArranger = _whereClauseArranger;
        copy._cipherManager = _cipherManager;

        return copy;
    }

    @Override
    protected LikeSearchOption newDeepCopyInstance() {
        return new LikeSearchOption();
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() { // main items only
        final String title = DfTypeUtil.toClassTitle(this);
        final String split = (isSplit() ? (_asOrSplit ? "true(or)" : "true(and)") : "false");
        return title + ":{like=" + _like + ", escape=" + _escape + ", split=" + split + "}";
    }
}
