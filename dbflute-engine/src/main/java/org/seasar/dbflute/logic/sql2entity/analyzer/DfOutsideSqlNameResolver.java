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
package org.seasar.dbflute.logic.sql2entity.analyzer;

import java.util.ArrayList;
import java.util.List;

import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.exception.DfIllegalAutoNamingClassNameException;
import org.seasar.dbflute.properties.DfBasicProperties;
import org.seasar.dbflute.util.DfStringUtil;

/**
 * @author jflute
 * @since 0.9.5 (2009/04/10 Friday)
 */
public class DfOutsideSqlNameResolver {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String ENTITY_MARK = "df:entity";
    public static final String PMB_MARK = "df:pmb";

    // ===================================================================================
    //                                                                             Resolve
    //                                                                             =======
    public String resolveEntityNameIfNeeds(String className, String fileName) {
        return resolveObjectNameIfNeeds(className, fileName, ENTITY_MARK, "");
    }

    public String resolvePmbNameIfNeeds(String className, String fileName) {
        return resolveObjectNameIfNeeds(className, fileName, PMB_MARK, "Pmb");
    }

    protected String resolveObjectNameIfNeeds(String className, String fileName, String mark, String suffix) {
        if (className == null || className.trim().length() == 0) {
            String msg = "The argument[className] should not be null or empty: " + className;
            throw new IllegalArgumentException(msg);
        }
        if (!className.equalsIgnoreCase(mark)) {
            if (className.contains(":") || className.contains(";")) {
                throwIllegalAutoNamingClassNameException(className, fileName, mark);
            }
            return className;
        }
        if (fileName == null || fileName.trim().length() == 0) {
            String msg = "The argument[fileName] should not be null or empty: " + fileName;
            throw new IllegalArgumentException(msg);
        }
        fileName = DfStringUtil.replace(fileName, "\\", "/");
        if (fileName.contains("/")) {
            fileName = fileName.substring(fileName.lastIndexOf("/") + "/".length());
        }
        if (!fileName.endsWith(".sql")) {
            String msg = "The SQL file should ends '.sql' if you use auto-naming:";
            msg = msg + " className=" + className + " fileName=" + fileName;
            throw new IllegalStateException(msg);
        }
        final int beginIndex;
        {
            // search both types 'fooBhv_', 'fooBhvAp_'
            final String bhvSuffix = "Bhv_";
            if (fileName.contains(bhvSuffix)) {
                beginIndex = fileName.indexOf(bhvSuffix) + bhvSuffix.length();
            } else {
                final String additionalSuffix = getBasicProperties().getApplicationBehaviorAdditionalSuffix();
                final String bhvApSuffix = additionalSuffix + "_";
                if (fileName.contains(bhvApSuffix)) {
                    beginIndex = fileName.indexOf(bhvApSuffix) + bhvApSuffix.length();
                } else {
                    beginIndex = 0;
                }
            }
        }
        String tmp = fileName.substring(beginIndex);
        int endIndex = tmp.indexOf("_");
        if (endIndex < 0) {
            endIndex = tmp.indexOf(".sql");
        }
        if (endIndex < 0) { // basically no way because it has already been checked
            String msg = "The SQL file should ends '.sql' if you use auto-naming:";
            msg = msg + " className=" + className + " fileName=" + fileName;
            throw new IllegalStateException(msg);
        }
        if (endIndex == 0) {
            String msg = "The name of SQL file have an unexpected underscore:";
            msg = msg + " className=" + className + " fileName=" + fileName;
            throw new IllegalStateException(msg);
        }
        tmp = tmp.substring(0, endIndex);
        final char[] charArray = tmp.toCharArray();
        final List<Character> charList = new ArrayList<Character>();
        boolean beginTarget = false;
        for (char c : charArray) {
            if (Character.isUpperCase(c)) {
                beginTarget = true;
            }
            if (beginTarget) {
                charList.add(c);
            }
        }
        if (charList.isEmpty()) {
            for (char c : charArray) {
                charList.add(c);
            }
        }
        final StringBuilder sb = new StringBuilder();
        for (Character c : charList) {
            sb.append(c);
        }
        sb.append(suffix);
        return DfStringUtil.initCap(sb.toString());
    }

    protected void throwIllegalAutoNamingClassNameException(String className, String fileName, String mark) {
        final boolean entity = ENTITY_MARK.equalsIgnoreCase(mark);
        final String targetName = entity ? "customize-entity" : "parameter-bean";
        String msg = "Look! Read the message below." + ln();
        msg = msg + "/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *" + ln();
        msg = msg + "The className for auto-naming(for " + targetName + ") is invalid." + ln();
        msg = msg + ln();
        msg = msg + "[Advice]" + ln();
        msg = msg + "Your className contained colon or semicolon but it was not auto-naming mark." + ln();
        msg = msg + "The auto-naming marks are '" + mark + "'." + ln();
        msg = msg + "  For example(@SQL):" + ln();
        msg = msg + "    /- - - - - - - - - - - - - - - - - - - - " + ln();
        if (entity) {
            msg = msg + "    -- #" + mark + "#" + ln();
        } else {
            msg = msg + "    -- !" + mark + "!" + ln();
            msg = msg + "    -- !!Integer memberId!!" + ln();
            msg = msg + "    -- !!String memberName!!" + ln();
            msg = msg + "    -- ..." + ln();
        }
        msg = msg + "    " + ln();
        msg = msg + "    select * from ..." + ln();
        msg = msg + "    - - - - - - - - - -/" + ln();
        msg = msg + "Confirm your auto-naming class definition if you want to use auto-naming." + ln();
        msg = msg + ln();
        msg = msg + "[Wrong Class Name]" + ln() + className + ln();
        msg = msg + ln();
        msg = msg + "[SQL File]" + ln() + fileName + ln();
        msg = msg + "* * * * * * * * * */";
        throw new DfIllegalAutoNamingClassNameException(msg);
    }

    protected String ln() {
        return "\n";
    }

    // ===================================================================================
    //                                                                          Properties
    //                                                                          ==========
    protected DfBuildProperties getProperties() {
        return DfBuildProperties.getInstance();
    }

    protected DfBasicProperties getBasicProperties() {
        return getProperties().getBasicProperties();
    }
}
