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
package org.dbflute.logic.generate.language.pkgstyle;

import org.dbflute.DfBuildProperties;
import org.dbflute.properties.DfLittleAdjustmentProperties;

/**
 * @author jflute
 * @since 1.0.5F (2014/05/04 Sunday)
 */
public class DfLanguagePropertyPackageResolverJava extends DfLanguagePropertyPackageResolver {

    protected String processLanguageType(String typeName, boolean exceptUtil) {
        if (!exceptUtil) {
            final String listType = processListType(typeName, exceptUtil, getListPackage(), "List");
            if (listType != null) {
                return listType;
            }
            final String mapType = processMapType(typeName, exceptUtil, getMapPackage(), "Map");
            if (mapType != null) {
                return mapType;
            }
        }
        if (typeName.equals("BigDecimal")) {
            return getBigDecimalPackage() + "." + typeName;
        }
        final DfLittleAdjustmentProperties prop = getLittleAdjustmentProperties();
        if (typeName.equals("LocalDate")) {
            return getJava8LocalDate();
        }
        if (typeName.equals("LocalDateTime")) {
            return getJava8LocalDateTime();
        }
        if (typeName.equals("Date")) {
            if (prop.isAvailableJava8TimeLocalDateEntity()) {
                if (prop.needsDateTreatedAsLocalDateTime()) {
                    return getJava8LocalDateTime();
                } else {
                    return getJava8LocalDate();
                }
            }
            if (!exceptUtil) {
                return "java.util." + typeName;
            }
        }
        if (typeName.equals("Timestamp")) {
            if (prop.isAvailableJava8TimeLocalDateEntity()) {
                return getJava8LocalDateTime();
            }
            return "java.sql." + typeName;
        }
        if (typeName.equals("Time")) {
            return "java.sql." + typeName;
        }
        return null;
    }

    protected String getListPackage() {
        return "java.util";
    }

    protected String getMapPackage() {
        return "java.util";
    }

    protected String getBigDecimalPackage() {
        return "java.math";
    }

    protected String getJava8LocalDate() {
        return "java.time.LocalDate";
    }

    protected String getJava8LocalDateTime() {
        return "java.time.LocalDateTime";
    }

    protected DfLittleAdjustmentProperties getLittleAdjustmentProperties() {
        return DfBuildProperties.getInstance().getLittleAdjustmentProperties();
    }
}
