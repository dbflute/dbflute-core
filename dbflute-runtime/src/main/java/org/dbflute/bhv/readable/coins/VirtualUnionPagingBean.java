/*
 * Copyright 2014-2015 the original author or authors.
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
package org.dbflute.bhv.readable.coins;

import java.util.ArrayList;
import java.util.List;

import org.dbflute.cbean.result.PagingResultBean;
import org.dbflute.cbean.result.ResultBeanBuilder;

/**
 * The handler of entity row.
 * @param <ID> The type of identity.
 * @param <RESULT> The type of mapped result.
 * @author jflute
 * @since 1.1.0 (2014/12/30 Tuesday)
 */
public class VirtualUnionPagingBean<ID, RESULT> {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final List<VirtualUnionPagingMeta> _metaMap = new ArrayList<VirtualUnionPagingMeta>();
    protected int _pageSize;
    protected int _pageNumber;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public VirtualUnionPagingBean(VirtualUnionPagingIdSelector<ID> noArgLambda, VirtualUnionPagingDataMapper<ID, RESULT> oneArgLambda) {
        unionAll(noArgLambda, oneArgLambda);
    }

    // ===================================================================================
    //                                                                               Union
    //                                                                               =====
    // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
    // cannot order through all records
    // and no union(), you should remove duplicate records after select manually
    // _/_/_/_/_/_/_/_/_/_/
    public VirtualUnionPagingBean<ID, RESULT> unionAll(VirtualUnionPagingIdSelector<ID> noArgLambda,
            VirtualUnionPagingDataMapper<ID, RESULT> oneArgLambda) {
        _metaMap.add(newVirtualUnionPagingMetaInfo(noArgLambda, oneArgLambda));
        return this;
    }

    protected VirtualUnionPagingMeta newVirtualUnionPagingMetaInfo(VirtualUnionPagingIdSelector<ID> idSelector,
            VirtualUnionPagingDataMapper<ID, RESULT> dataMapper) {
        return new VirtualUnionPagingMeta(idSelector, dataMapper);
    }

    // ===================================================================================
    //                                                                              Paging
    //                                                                              ======
    public VirtualUnionPagingSelector<RESULT> paging(int pageSize, int pageNumber) {
        if (pageSize < 1) {
            throw new IllegalArgumentException("The pageSize should not be minus or zero: " + pageSize);
        }
        if (pageNumber < 1) {
            throw new IllegalArgumentException("The pageNumber should not be minus or zero: " + pageNumber);
        }
        _pageSize = pageSize;
        _pageNumber = pageNumber;
        return new VirtualUnionPagingSelector<RESULT>() {
            public PagingResultBean<RESULT> selectPage() {
                return doSelectPage();
            }
        };
    }

    protected PagingResultBean<RESULT> doSelectPage() {
        final PagingResultBean<RESULT> page = actuallySelectPage(_pageSize, _pageNumber);
        if (page.getCurrentPageNumber() > 1 && page.isEmpty()) {
            return actuallySelectPage(_pageSize, page.getAllPageCount()); // retry
        }
        return page;
    }

    protected PagingResultBean<RESULT> actuallySelectPage(int pageSize, int pageNumber) {
        final int allRecordCount = prepareIdList();
        final List<RESULT> resultList = new ArrayList<RESULT>();
        final boolean usePaging = pageSize > 0 && pageNumber > 0;
        final int limit = usePaging ? pageSize * pageNumber : -1;
        final int offset = usePaging ? pageSize * (pageNumber - 1) : -1;
        int skippedCount = 0;
        boolean overOffset = false;
        int actualCount = 0;
        for (VirtualUnionPagingMeta meta : _metaMap) {
            final VirtualUnionPagingDataMapper<ID, RESULT> dataMapper = meta.getDataMapper();
            List<ID> idList = meta.getIdList();
            if (idList.isEmpty()) {
                continue;
            }
            if (usePaging) {
                if (!overOffset) {
                    final int scheduledSkippedCount = skippedCount + idList.size();
                    if (scheduledSkippedCount < offset) {
                        skippedCount = skippedCount + idList.size();
                        continue;
                    }
                    overOffset = true;
                    idList = filterIdListByOffset(idList, skippedCount, offset);
                }
                idList = filterIdListByLimit(idList, actualCount, limit, offset);
            }
            final List<RESULT> mappedList = dataMapper.selectMappedList(idList);
            final int mappedSize = mappedList.size();
            assertMappedSizeValidForIdList(idList, mappedSize);
            resultList.addAll(mappedList);
            actualCount = actualCount + mappedSize;
            if (usePaging && actualCount >= limit) {
                break;
            }
        }
        final ResultBeanBuilder<RESULT> builder = new ResultBeanBuilder<RESULT>("UNION_PAGING");
        return builder.buildPagingSimply(pageSize, pageNumber, allRecordCount, resultList);
    }

    protected int prepareIdList() {
        int count = 0;
        for (VirtualUnionPagingMeta meta : _metaMap) {
            final VirtualUnionPagingIdSelector<ID> idSelector = meta.getIdSelector();
            final List<ID> idList = idSelector.selectIdList();
            meta.setIdList(idList);
            count = count + idList.size();
        }
        return count;
    }

    protected List<ID> filterIdListByOffset(List<ID> idList, int skippedCount, int offset) {
        return idList.subList(offset - skippedCount, idList.size());
    }

    protected List<ID> filterIdListByLimit(List<ID> idList, int actualCount, int limit, int offset) {
        final int scheduledCount = actualCount + idList.size();
        final int pageSize = limit - offset; // not use instance variable for independent
        if (scheduledCount > pageSize) { // mean last
            if (actualCount == 0) {
                idList = idList.subList(0, pageSize);
            } else {
                final int remainderSize = pageSize - actualCount;
                idList = remainderSize < idList.size() ? idList.subList(0, remainderSize) : idList;
            }
        }
        return idList;
    }

    protected void assertMappedSizeValidForIdList(List<ID> idList, final int mappedSize) {
        if (mappedSize > idList.size()) {
            String msg = "mapped list is over the ID list: " + mappedSize + ", " + idList;
            throw new IllegalStateException(msg);
        }
    }

    // ===================================================================================
    //                                                                        Helper Class
    //                                                                        ============
    public interface VirtualUnionPagingSelector<RESULT> { // not function interface, for chain
        PagingResultBean<RESULT> selectPage();
    }

    public class VirtualUnionPagingMeta {

        protected VirtualUnionPagingIdSelector<ID> _idSelector;
        protected VirtualUnionPagingDataMapper<ID, RESULT> _dataMapper;
        protected List<ID> _idList; // set later

        public VirtualUnionPagingMeta(VirtualUnionPagingIdSelector<ID> idSelector, VirtualUnionPagingDataMapper<ID, RESULT> dataMapper) {
            _idSelector = idSelector;
            _dataMapper = dataMapper;
        }

        public VirtualUnionPagingIdSelector<ID> getIdSelector() {
            return _idSelector;
        }

        public void setIdSelector(VirtualUnionPagingIdSelector<ID> idSelector) {
            _idSelector = idSelector;
        }

        public VirtualUnionPagingDataMapper<ID, RESULT> getDataMapper() {
            return _dataMapper;
        }

        public void setDataMapper(VirtualUnionPagingDataMapper<ID, RESULT> dataMapper) {
            _dataMapper = dataMapper;
        }

        public List<ID> getIdList() {
            return _idList;
        }

        public void setIdList(List<ID> idList) {
            _idList = idList;
        }
    }

    @FunctionalInterface
    public interface VirtualUnionPagingIdSelector<ID> {
        List<ID> selectIdList();
    }

    @FunctionalInterface
    public interface VirtualUnionPagingDataMapper<ID, RESULT> {
        List<RESULT> selectMappedList(List<ID> idList);
    }
}
