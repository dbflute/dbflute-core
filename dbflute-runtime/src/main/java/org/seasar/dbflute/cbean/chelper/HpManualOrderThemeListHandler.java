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
package org.seasar.dbflute.cbean.chelper;

/**
 * The handler of theme list of free parameter for manual order.
 * @author jflute
 */
public interface HpManualOrderThemeListHandler {

    /**
     * @param themeKey The theme key of free parameter. (NotNull)
     * @param orderValue The registered order value. (NotNull)
     * @return The bound expression for registered order value. (NotNull)
     */
    String register(String themeKey, Object orderValue);
}
