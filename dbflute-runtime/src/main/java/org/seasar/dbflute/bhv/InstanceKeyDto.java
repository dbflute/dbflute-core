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

/**
 * @author jflute
 * @since 0.9.9.7C (2012/08/13 Monday)
 */
public class InstanceKeyDto {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Object _dto;
    protected final int _instanceHash;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public InstanceKeyDto(Object dto, int instanceHash) {
        _dto = dto;
        _instanceHash = instanceHash;
    }

    // ===================================================================================
    //                                                                   Instance Identity
    //                                                                   =================
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof InstanceKeyDto)) {
            return false;
        }
        return _dto == ((InstanceKeyDto) obj)._dto;
    }

    @Override
    public int hashCode() {
        return _instanceHash;
    }

    @Override
    public String toString() {
        return _dto.toString();
    }
}
