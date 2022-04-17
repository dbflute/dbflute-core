/*
 * Copyright 2014-2022 the original author or authors.
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
package org.dbflute.dbmeta.property;

import org.dbflute.Entity;

/**
 * The interface of property gateway.
 * @author jflute
 * @since 1.1.0 (2014/10/24 Friday)
 */
public class DelegatingPropertyGateway implements PropertyGateway {

    protected final PropertyReader _reader;
    protected final PropertyWriter _writer;

    public DelegatingPropertyGateway(PropertyReader reader, PropertyWriter writer) {
        _reader = reader;
        _writer = writer;
    }

    /** {@inheritDoc} */
    public Object read(Entity entity) {
        return _reader.read(entity);
    }

    /** {@inheritDoc} */
    public void write(Entity entity, Object value) {
        _writer.write(entity, value);
    }
}
