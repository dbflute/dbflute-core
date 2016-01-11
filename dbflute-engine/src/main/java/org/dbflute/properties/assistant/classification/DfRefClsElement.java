/*
 * Copyright 2014-2016 the original author or authors.
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
package org.dbflute.properties.assistant.classification;

/**
 * @author jflute
 * @since 1.1.1 (2016/01/11 Monday)
 */
public class DfRefClsElement {

    public static final String KEY_REFCLS = "refCls"; // for webCls

    protected final String _projectName;
    protected final String _classificationName;
    protected final String _classificationType;

    public DfRefClsElement(String projectName, String classificationName, String classificationType) {
        _projectName = projectName;
        _classificationName = classificationName;
        _classificationType = classificationType;
    }

    public String getProjectName() {
        return _projectName;
    }

    public String getClassificationName() {
        return _classificationName;
    }

    public String getClassificationType() {
        return _classificationType;
    }
}
