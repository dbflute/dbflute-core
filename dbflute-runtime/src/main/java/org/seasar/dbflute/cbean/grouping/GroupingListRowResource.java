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
package org.seasar.dbflute.cbean.grouping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The row resource of grouping list.
 * @param <ENTITY> The type of entity.
 * @author jflute
 */
public class GroupingListRowResource<ENTITY> implements Serializable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Serial version UID. (Default) */
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final List<ENTITY> _groupingEntityList = new ArrayList<ENTITY>();
    protected int _currentIndex;

    // ===================================================================================
    //                                                                         Easy-to-Use
    //                                                                         ===========
    /**
     * @return The entity of current index. (NotNull)
     */
    public ENTITY getCurrentEntity() {
        return _groupingEntityList.get(_currentIndex);
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    /**
     * @return The list of grouping entity. (NotNull and NotEmpty)
     */
    public List<ENTITY> getGroupingEntityList() {
        return _groupingEntityList;
    }

    /**
     * Add the element entity to the list of grouping entity. {INTERNAL METHOD}
     * @param entity The element entity to the list of grouping entity.
     */
    public void addGroupingEntity(ENTITY entity) {
        _groupingEntityList.add(entity);
    }

    /**
     * @return The index of current element.
     */
    public int getCurrentIndex() {
        return _currentIndex;
    }

    /**
     * Set the index of current element. {INTERNAL METHOD}
     * @param currentIndex The index of current element.
     */
    public void setCurrentIndex(int currentIndex) {
        _currentIndex = currentIndex;
    }

    /**
     * @return The index of next element.
     */
    public int getNextIndex() {
        return _currentIndex + 1;
    }
}
