/*
 * Copyright 2014-2018 the original author or authors.
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
package org.dbflute;

/**
 * The work dir of DBFlute Engine.
 * @author p1us2er0
 */
public class DfEngineWorkDir {

    /**
     * Resolve the path.
     * @param path The relative path from work directory of DBFlute Engine, e.g. "output/doc". (NotNull)
     * @return The resolved path that can be used in File and Path. (NotNull)
     */
    public static String toPath(String path) {
        String workDir = DfBuildProperties.getInstance().getWorkDir();
        return workDir + path;
    }
}
