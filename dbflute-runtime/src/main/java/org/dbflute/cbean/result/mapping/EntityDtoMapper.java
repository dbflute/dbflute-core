/*
 * Copyright 2014-2025 the original author or authors.
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
package org.dbflute.cbean.result.mapping;

/**
 * The map-per of entity to DTO.
 * @param <ENTITY> The type of entity.
 * @param <DTO> The type of DTO.
 * @author jflute
 */
@FunctionalInterface
public interface EntityDtoMapper<ENTITY, DTO> {

    /**
     * Map entity to data transfer object.
     * @param entity Entity. (NotNull)
     * @return The object for data transfer. (NotNull)
     */
    DTO map(ENTITY entity);
}
