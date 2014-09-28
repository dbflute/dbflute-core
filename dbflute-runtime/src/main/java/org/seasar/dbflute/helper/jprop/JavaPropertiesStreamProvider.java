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
package org.seasar.dbflute.helper.jprop;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author jflute
 */
public interface JavaPropertiesStreamProvider {

    /**
     * Provider the stream for java properties. <br />
     * This method is called back twice times. <br />
     * So returned input stream should be always new-created stream. <br />
     * (The stream is closed after reading)
     * @return The new-created stream for java properties. (NotNull)
     * @throws IOException When it fails to provide the stream.
     */
    InputStream provideStream() throws IOException;
}
