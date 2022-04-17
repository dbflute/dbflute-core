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
package org.dbflute.logic.replaceschema.takefinally.sequence;

import java.util.Map;

import org.dbflute.helper.jdbc.context.DfSchemaSource;
import org.dbflute.logic.replaceschema.process.DfAbstractRepsProcess;
import org.dbflute.logic.replaceschema.takefinally.sequence.factory.DfRepsSequenceHandlerFactory;
import org.dbflute.properties.DfDatabaseProperties;
import org.dbflute.properties.DfSequenceIdentityProperties;
import org.dbflute.properties.facade.DfDatabaseTypeFacadeProp;

/**
 * @author jflute
 * @since 1.1.7 (2018/03/17 Saturday)
 */
public class DfRepsSequenceIncrementer extends DfAbstractRepsProcess {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final DfSchemaSource _dataSource;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfRepsSequenceIncrementer(DfSchemaSource dataSource) {
        _dataSource = dataSource;
    }

    // ===================================================================================
    //                                                                  Increment Sequence
    //                                                                  ==================
    public void incrementSequenceToDataMax() {
        final DfSequenceIdentityProperties sequenceProp = getProperties().getSequenceIdentityProperties();
        final Map<String, String> tableSequenceMap = sequenceProp.getTableSequenceMap();
        final DfDatabaseTypeFacadeProp dbTypeProp = getDatabaseTypeFacadeProp();
        final DfDatabaseProperties databaseProp = getDatabaseProperties();
        final DfRepsSequenceHandlerFactory factory = new DfRepsSequenceHandlerFactory(_dataSource, dbTypeProp, databaseProp);
        final DfRepsSequenceHandler sequenceHandler = factory.createSequenceHandler();
        if (sequenceHandler == null) {
            String databaseType = dbTypeProp.getTargetDatabase();
            String msg = "Unsupported isIncrementSequenceToDataMax at " + databaseType;
            throw new UnsupportedOperationException(msg);
        }
        sequenceHandler.incrementSequenceToDataMax(tableSequenceMap);
    }
}
