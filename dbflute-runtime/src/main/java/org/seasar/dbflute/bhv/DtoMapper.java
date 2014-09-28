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
package org.seasar.dbflute.bhv;

import java.util.List;

import org.seasar.dbflute.Entity;

/**
 * The interface of DTO mapper. 
 * @param <ENTITY> The type of entity.
 * @param <DTO> The type of DTO.
 * @author jflute
 */
public interface DtoMapper<ENTITY extends Entity, DTO> {

    /**
     * Do mapping from an entity to a DTO with relation data.
     * @param entity The entity as mapping resource. (NullAllowed: if null, returns null)
     * @return The mapped DTO. (NotNull)
     */
    DTO mappingToDto(ENTITY entity);

    /**
     * Do mapping from an entity list to a DTO list with relation data. <br />
     * This calls this.mappingToDto() in a loop of the list.
     * @param entityList The list of entity as mapping resource. (NotNull: null elements are inherited)
     * @return The list of mapped DTO. (NotNull)
     */
    List<DTO> mappingToDtoList(List<ENTITY> entityList);

    /**
     * Do mapping from a DTO to an entity with relation data. <br />
     * A setter of an entity is called under the rule of this.needsMapping().
     * @param dto The DTO as mapping resource. (NullAllowed: if null, returns null)
     * @return The mapped entity. (NotNull)
     */
    ENTITY mappingToEntity(DTO dto);

    /**
     * Do mapping from a DTO list to an entity list with relation data. <br />
     * This calls this.mappingToEntity() in loop of the list.
     * @param dtoList The list of DTO as mapping resource. (NotNull: null elements are inherited)
     * @return The list of mapped entity. (NotNull)
     */
    List<ENTITY> mappingToEntityList(List<DTO> dtoList);

    /**
     * Set the option whether base-only mapping or not.
     * @param baseOnlyMapping Does the mapping ignore all references? (true: base-only mapping, false: all relations are valid)
     */
    void setBaseOnlyMapping(boolean baseOnlyMapping);

    /**
     * Set the option whether common column is except or not.
     * @param exceptCommonColumn Does the mapping except common column? (true: no mapping of common column)
     */
    void setExceptCommonColumn(boolean exceptCommonColumn);

    /**
     * Set the option whether reverse reference or not.
     * @param reverseReference Does the mapping contain reverse references? (true: reverse reference, false: one-way reference)
     */
    void setReverseReference(boolean reverseReference);
}
