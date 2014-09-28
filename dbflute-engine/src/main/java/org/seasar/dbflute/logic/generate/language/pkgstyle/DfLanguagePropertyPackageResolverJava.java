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
package org.seasar.dbflute.logic.generate.language.pkgstyle;

import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.properties.DfLittleAdjustmentProperties;

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
        if (typeName.equals("JodaLocalDate")) {
            return getJodaLocalDate();
        }
        if (typeName.equals("JodaLocalDateTime")) {
            return getJodaLocalDateTime();
        }
        if (typeName.equals("Date")) {
            if (prop.isAvailableJava8TimeLocalDateEntity()) {
                return getJava8LocalDate();
            }
            if (prop.isAvailableJodaTimeLocalDateEntity()) {
                return getJodaLocalDate();
            }
            if (!exceptUtil) {
                return "java.util." + typeName;
            }
        }
        if (typeName.equals("Timestamp")) {
            if (prop.isAvailableJava8TimeLocalDateEntity()) {
                return getJava8LocalDateTime();
            }
            if (prop.isAvailableJodaTimeLocalDateEntity()) {
                return getJodaLocalDateTime();
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

    protected String getJodaLocalDate() {
        return "org.joda.time.LocalDate";
    }

    protected String getJodaLocalDateTime() {
        return "org.joda.time.LocalDateTime";
    }

    protected DfLittleAdjustmentProperties getLittleAdjustmentProperties() {
        return DfBuildProperties.getInstance().getLittleAdjustmentProperties();
    }
}
