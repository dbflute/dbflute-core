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
 *
 * And the following license definition is for Apache Torque.
 * DBFlute modified this source code and redistribute as same license 'Apache'.
 * /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 *
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 * 
 * - - - - - - - - - -/
 */
package org.apache.torque.engine.database.model;

/* ====================================================================
 *
 * Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
import java.util.List;
import java.util.StringTokenizer;

import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.properties.DfLittleAdjustmentProperties;
import org.seasar.dbflute.util.Srl;

/**
 * A <code>NameGenerator</code> implementation for Java-esque names.
 * @author modified by jflute (originated in Apache Torque)
 */
public class JavaNameGenerator implements NameGenerator {

    /**
     * <code>inputs</code> should consist of two elements, the
     * original name of the database element and the method for
     * generating the name.  There are currently three methods:
     * <code>CONV_METHOD_NOCHANGE</code> - xml names are converted
     * directly to java names without modification.
     * <code>CONV_METHOD_UNDERSCORE</code> will capitalize the first
     * letter, remove underscores, and capitalize each letter before
     * an underscore.  All other letters are lowercased. "javaname"
     * works the same as the <code>CONV_METHOD_JAVANAME</code> method
     * but will not lowercase any characters.
     * @param inputs list expected to contain two parameters, element
     * 0 contains name to convert, element 1 contains method for conversion.
     * @return The generated name.
     * @see org.apache.torque.engine.database.model.NameGenerator
     */
    public String generateName(List<?> inputs) {
        String schemaName = (String) inputs.get(0);
        String method = (String) inputs.get(1);
        String javaName = null;

        if (CONV_METHOD_UNDERSCORE.equals(method)) {
            javaName = underscoreMethod(schemaName);
        } else if (CONV_METHOD_JAVANAME.equals(method)) {
            javaName = javanameMethod(schemaName);
        } else if (CONV_METHOD_NOCHANGE.equals(method)) {
            javaName = nochangeMethod(schemaName);
        } else {
            // if for some reason nothing is defined then we default
            // to the traditional method.
            javaName = underscoreMethod(schemaName);
        }

        return javaName;
    }

    /**
     * Converts a database schema name to java object name.  Removes
     * <code>STD_SEPARATOR_CHAR</code>, capitilizes first letter of
     * name and each letter after the <code>STD_SEPERATOR</code>,
     * converts the rest of the letters to lowercase.
     * @param schemaName name to be converted.
     * @return converted name.
     * @see org.apache.torque.engine.database.model.NameGenerator
     * @see #underscoreMethod(String)
     */
    protected String underscoreMethod(String schemaName) {
        StringBuffer name = new StringBuffer();
        StringTokenizer tok = new StringTokenizer(schemaName, String.valueOf(STD_SEPARATOR_CHAR));
        DfLittleAdjustmentProperties prop = DfBuildProperties.getInstance().getLittleAdjustmentProperties();
        boolean availableToLowerInGeneratorUnderscoreMethod = prop.isAvailableToLowerInGeneratorUnderscoreMethod();
        while (tok.hasMoreTokens()) {
            String namePart = ((String) tok.nextElement());
            if (availableToLowerInGeneratorUnderscoreMethod) {
                namePart = namePart.toLowerCase();
            }
            name.append(initCap(namePart));
        }
        return name.toString();
    }

    /**
     * Converts a database schema name to java object name.  Operates
     * same as underscoreMethod but does not convert anything to lowercase.
     * @param schemaName name to be converted.
     * @return converted name.
     * @see org.apache.torque.engine.database.model.NameGenerator
     * @see #underscoreMethod(String)
     */
    protected String javanameMethod(String schemaName) {
        StringBuffer name = new StringBuffer();
        StringTokenizer tok = new StringTokenizer(schemaName, String.valueOf(STD_SEPARATOR_CHAR));
        while (tok.hasMoreTokens()) {
            String namePart = (String) tok.nextElement();
            name.append(initCap(namePart));
        }
        return name.toString();
    }

    /**
     * Converts a database schema name to java object name.  In this
     * case no conversion is made.
     * @param name name to be converted.
     * @return The <code>name</code> parameter, unchanged.
     */
    protected final String nochangeMethod(String name) {
        return name;
    }

    public String initCap(String str) {
        return Srl.initCap(str);
    }
}
