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
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.torque.engine.EngineException;

/**
 * @author modified by jflute (originated in Apache Torque)
 */
public class NameFactory {

    /**
     * The fully qualified class name of the Java name generator.
     */
    public static final String JAVA_GENERATOR = JavaNameGenerator.class.getName();

    /**
     * The fully qualified class name of the constraint name generator.
     */
    public static final String CONSTRAINT_GENERATOR = ConstraintNameGenerator.class.getName();

    /**
     * The single instance of this class.
     */
    private static NameFactory instance = new NameFactory();

    /**
     * The cache of <code>NameGenerator</code> algorithms in use for
     * name generation, keyed by fully qualified class name.
     */
    private Hashtable<String, NameGenerator> algorithms;

    /**
     * Creates a new instance with storage for algorithm implementations.
     */
    protected NameFactory() {
        algorithms = new Hashtable<String, NameGenerator>(5);
    }

    /**
     * Factory method which retrieves an instance of the named generator.
     * @param name The fully qualified class name of the name generation algorithm to retrieve.
     * @return A name generator
     */
    protected NameGenerator getAlgorithm(String name) throws EngineException {
        synchronized (algorithms) {
            NameGenerator algorithm = (NameGenerator) algorithms.get(name);
            if (algorithm == null) {
                try {
                    algorithm = (NameGenerator) Class.forName(name).newInstance();
                } catch (InstantiationException e) {
                    throw new EngineException("Unable to instantiate class " + name
                            + ": Make sure it's in your run-time classpath", e);
                } catch (Exception e) {
                    throw new EngineException(e);
                }
                algorithms.put(name, algorithm);
            }
            return algorithm;
        }
    }

    /**
     * Given a list of <code>String</code> objects, implements an
     * algorithm which produces a name.
     * @param algorithmName The fully qualified class name of the
     * {@link org.apache.torque.engine.database.model.NameGenerator}
     * implementation to use to generate names.
     * @param inputs Inputs used to generate a name.
     * @return The generated name.
     * @throws EngineException an exception
     */
    public static String generateName(String algorithmName, List<?> inputs) throws EngineException {
        final NameGenerator algorithm = instance.getAlgorithm(algorithmName);
        return algorithm.generateName(inputs);
    }

    public static String generateJavaNameByMethodUnderscore(String name) {
        try {
            final NameGenerator algorithm = instance.getAlgorithm(NameFactory.JAVA_GENERATOR);
            final List<String> inputs = new ArrayList<String>();
            inputs.add(name);
            inputs.add(NameGenerator.CONV_METHOD_UNDERSCORE);
            return algorithm.generateName(inputs);
        } catch (EngineException e) {
            String msg = "generateName() threw the exception: name=" + name;
            throw new RuntimeException(msg, e);
        }
    }
}
