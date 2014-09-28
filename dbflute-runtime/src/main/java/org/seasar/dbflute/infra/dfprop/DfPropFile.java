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
package org.seasar.dbflute.infra.dfprop;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.seasar.dbflute.exception.DfPropFileReadFailureException;
import org.seasar.dbflute.exception.MapListStringDuplicateEntryException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.helper.mapstring.MapListFile;
import org.seasar.dbflute.util.Srl;

/**
 * The file handling for DBFlute property (dfprop).
 * @author jflute
 * @since 0.9.6 (2009/10/28 Wednesday)
 */
public class DfPropFile {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected boolean _returnsNullIfNotFound;
    protected boolean _skipLineSeparator;
    protected boolean _checkDuplicateEntry;

    // ===================================================================================
    //                                                                                 Map
    //                                                                                 ===
    // -----------------------------------------------------
    //                                                  Read
    //                                                  ----
    /**
     * Read the map string file. <br />
     * If the type of values is various type, this method is available. <br />
     * A trimmed line that starts with '#' is treated as line comment. <br />
     * This is the most basic method here.
     * <pre>
     * map:{
     *     ; key1 = string-value1
     *     ; key2 = list:{element1 ; element2 }
     *     ; key3 = map:{key1 = value1 ; key2 = value2 }
     *     ; ... = ...
     * }
     * </pre>
     * And resolve property file, switched and extended file, for environment. <br />
     * It returns merged properties like this:
     * <pre>
     * for example:
     * 
     * [Switch Style]
     * dbflute_exampledb
     *  |-dfprop
     *  |  |-maihama
     *  |  |  |-exampleMap.dfprop  // env
     *  |  |-exampleMap.dfprop     // main
     *
     * if maihama, env
     * if default, main
     * 
     * [Inherit Style]
     * dbflute_exampledb
     *  |-dfprop
     *  |  |-maihama
     *  |  |  |-exampleMap+.dfprop // env+
     *  |  |-exampleMap.dfprop     // main
     * 
     * if maihama, main and env+
     * if default, main only
     * 
     * [All Stars]
     * dbflute_exampledb
     *  |-dfprop
     *  |  |-maihama
     *  |  |  |-exampleMap.dfprop  // env
     *  |  |  |-exampleMap+.dfprop // env+
     *  |  |-exampleMap.dfprop     // main
     *  |  |-exampleMap+.dfprop    // main+
     * 
     * if maihama, env and env+
     * if default, main and main+
     * </pre>
     * @param dfpropPath The path of DBFlute property file. (NotNull)
     * @param envType The environment type of DBFlute. (NullAllowed: if null, no environment file)
     * @return The read map. (NotNull: if not found, returns empty map)
     */
    public Map<String, Object> readMap(String dfpropPath, String envType) {
        assertDfpropPath(dfpropPath);
        return doReadMap(dfpropPath, envType, new DfPropReadingMapHandler<Object>() {
            public Map<String, Object> readMap(String path) throws FileNotFoundException, IOException {
                return actuallyReadMap(path);
            }
        });
    }

    protected Map<String, Object> actuallyReadMap(String path) throws FileNotFoundException, IOException {
        try {
            return createMapListFileStructural().readMap(createInputStream(path));
        } catch (MapListStringDuplicateEntryException e) {
            throwDfPropDuplicateEntryException(path, e);
            return null; // unreachable
        }
    }

    protected void throwDfPropDuplicateEntryException(String path, MapListStringDuplicateEntryException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Duplicate entry in the map file.");
        br.addItem("Advice");
        br.addElement("The entry keys in the map string should be unique.");
        br.addElement("For example:");
        br.addElement("  (x):");
        br.addElement("    Sea = map:{");
        br.addElement("        ; ...");
        br.addElement("    }");
        br.addElement("    Sea = map:{");
        br.addElement("        ; ...");
        br.addElement("    }");
        br.addElement("  (o):");
        br.addElement("    Land = map:{");
        br.addElement("        ; ...");
        br.addElement("    }");
        br.addElement("    Sea = map:{");
        br.addElement("        ; ...");
        br.addElement("    }");
        br.addItem("DfProp Path");
        br.addElement(path);
        final String msg = br.buildExceptionMessage();
        throw new DfPropFileReadFailureException(msg, e);
    }

    /**
     * Read the map string file as string value. <br />
     * If the type of all values is string type, this method is available. <br />
     * A trimmed line that starts with '#' is treated as line comment.
     * <pre>
     * e.g.
     * map:{
     *     ; key1 = string-value1
     *     ; key2 = string-value2
     *     ; ... = ...
     * }
     * </pre>
     * <p>
     * And resolve property file for environment.
     * (see the {@link #readMap(String, String)})
     * </p>
     * @param dfpropPath The path of DBFlute property file. (NotNull)
     * @param envType The environment type of DBFlute. (NullAllowed: if null, no environment file)
     * @return The read map whose values is string. (NotNull: if not found, returns empty map)
     */
    public Map<String, String> readMapAsStringValue(String dfpropPath, String envType) {
        assertDfpropPath(dfpropPath);
        return doReadMap(dfpropPath, envType, new DfPropReadingMapHandler<String>() {
            public Map<String, String> readMap(String path) throws FileNotFoundException, IOException {
                return actuallyReadMapAsStringValue(path);
            }
        });
    }

    protected Map<String, String> actuallyReadMapAsStringValue(String path) throws FileNotFoundException, IOException {
        try {
            return createMapListFileStructural().readMapAsStringValue(createInputStream(path));
        } catch (MapListStringDuplicateEntryException e) {
            throwDfPropDuplicateEntryException(path, e);
            return null; // unreachable
        }
    }

    /**
     * Read the map string file as string list value. <br />
     * If the type of all values is string list type, this method is available. <br />
     * A trimmed line that starts with '#' is treated as line comment.
     * <pre>
     * e.g.
     * map:{
     *     ; key1 = list:{string-element1 ; string-element2 ; ...}
     *     ; key2 = list:{string-element1 ; string-element2 ; ...}
     *     ; ... = list:{...}
     * }
     * </pre>
     * <p>
     * And resolve property file for environment.
     * (see the {@link #readMap(String, String)})
     * </p>
     * @param dfpropPath The path of DBFlute property file. (NotNull)
     * @param envType The environment type of DBFlute. (NullAllowed: if null, no environment file)
     * @return The read map whose values is string list. (NotNull: if not found, returns empty map)
     */
    public Map<String, List<String>> readMapAsStringListValue(String dfpropPath, String envType) {
        assertDfpropPath(dfpropPath);
        return doReadMap(dfpropPath, envType, new DfPropReadingMapHandler<List<String>>() {
            public Map<String, List<String>> readMap(String path) throws IOException {
                return actuallyReadMapAsStringListValue(path);
            }
        });
    }

    protected Map<String, List<String>> actuallyReadMapAsStringListValue(String path) throws FileNotFoundException,
            IOException {
        try {
            return createMapListFileStructural().readMapAsStringListValue(createInputStream(path));
        } catch (MapListStringDuplicateEntryException e) {
            throwDfPropDuplicateEntryException(path, e);
            return null; // unreachable
        }
    }

    /**
     * Read the map string file as string map value. <br />
     * If the type of all values is string map type, this method is available. <br />
     * A trimmed line that starts with '#' is treated as line comment.
     * <pre>
     * e.g.
     * map:{
     *     ; key1 = map:{string-key1 = string-value1 ; string-key2 = string-value2 }
     *     ; key2 = map:{string-key1 = string-value1 ; string-key2 = string-value2 }
     *     ; ... = map:{...}
     * }
     * </pre>
     * <p>
     * And resolve property file for environment.
     * (see the {@link #readMap(String, String)})
     * </p>
     * @param dfpropPath The path of DBFlute property file. (NotNull)
     * @param envType The environment type of DBFlute. (NullAllowed: if null, no environment file)
     * @return The read map whose values is string map. (NotNull: if not found, returns empty map)
     */
    public Map<String, Map<String, String>> readMapAsStringMapValue(String dfpropPath, String envType) {
        assertDfpropPath(dfpropPath);
        return doReadMap(dfpropPath, envType, new DfPropReadingMapHandler<Map<String, String>>() {
            public Map<String, Map<String, String>> readMap(String path) throws IOException {
                return actuallyReadMapAsStringMapValue(path);
            }
        });
    }

    protected Map<String, Map<String, String>> actuallyReadMapAsStringMapValue(String path)
            throws FileNotFoundException, IOException {
        try {
            return createMapListFileStructural().readMapAsStringMapValue(createInputStream(path));
        } catch (MapListStringDuplicateEntryException e) {
            throwDfPropDuplicateEntryException(path, e);
            return null; // unreachable
        }
    }

    // ===================================================================================
    //                                                                                List
    //                                                                                ====
    // -----------------------------------------------------
    //                                                  Read
    //                                                  ----
    /**
     * Read the list string file. <br />
     * If the type of values is various type, this method is available. <br />
     * A trimmed line that starts with '#' is treated as line comment. <br />
     * <pre>
     * list:{
     *     ; element1
     *     ; list:{element2-1 ; element2-2 }
     *     ; map:{key3-1 = value3-1 ; key3-2 = value3-2 }
     *     ; ... = ...
     * }
     * </pre>
     * @param dfpropPath The path of DBFlute property file. (NotNull)
     * @param envType The environment type of DBFlute. (NullAllowed: if null, no environment file)
     * @return The read list of object. (NotNull: if not found, returns empty list)
     */
    public List<Object> readList(String dfpropPath, String envType) {
        assertDfpropPath(dfpropPath);
        return doReadList(dfpropPath, envType, new DfPropReadingListHandler<Object>() {
            public List<Object> readList(String path) throws FileNotFoundException, IOException {
                return actuallyReadList(path);
            }
        });
    }

    protected List<Object> actuallyReadList(String path) throws FileNotFoundException, IOException {
        try {
            return createMapListFileStructural().readList(createInputStream(path));
        } catch (MapListStringDuplicateEntryException e) {
            throwDfPropDuplicateEntryException(path, e);
            return null; // unreachable
        }
    }

    // ===================================================================================
    //                                                                              String
    //                                                                              ======
    // -----------------------------------------------------
    //                                                  Read
    //                                                  ----
    /**
     * Read the string file. <br />
     * A trimmed line that starts with '#' is treated as line comment.
     * @param dfpropPath The path of DBFlute property file. (NotNull)
     * @param envType The environment type of DBFlute. (NullAllowed: if null, no environment file)
     * @return The read string. (NotNull: if not found, returns empty string)
     */
    public String readString(String dfpropPath, String envType) {
        assertDfpropPath(dfpropPath);
        return doReadString(dfpropPath, envType, new DfPropReadingStringHandler() {
            public String readString(String path) throws FileNotFoundException, IOException {
                return actuallyReadString(path);
            }
        });
    }

    protected String actuallyReadString(String path) throws FileNotFoundException, IOException {
        return createMapListFilePlain().readString(createInputStream(path));
    }

    // ===================================================================================
    //                                                                       Reading Logic
    //                                                                       =============
    // -----------------------------------------------------
    //                                                   Map
    //                                                   ---
    protected <ELEMENT> Map<String, ELEMENT> doReadMap(String dfpropPath, String envType,
            DfPropReadingMapHandler<ELEMENT> handler) {
        // *see the JavaDoc of readMap() for the detail
        Map<String, ELEMENT> map = null;
        if (envType != null) {
            final String envPath = deriveEnvPath(dfpropPath, envType);
            map = callReadingMapChecked(handler, envPath);
            if (map != null) { // environment driven
                // dfprop
                //  |-env
                //  |  |-foo.dfprop  // *base map
                //  |  |-foo+.dfprop // if exists
                //  |-foo.dfprop
                //  |-foo+.dfprop    // no target
                resolveOutsidePropInheritMap(handler, envPath, map);
            } else { // top driven in environment
                // dfprop
                //  |-env
                //  |  |-foo+.dfprop // if exists (first priority)
                //  |-foo.dfprop     // *base map
                //  |-foo+.dfprop    // if exists (second priority)
                map = callReadingMapChecked(handler, dfpropPath);
                if (map != null) {
                    resolveOutsidePropInheritMap(handler, dfpropPath, map);
                    resolveOutsidePropInheritMap(handler, envPath, map);
                }
            }
        } else { // no environment type
            // dfprop
            //  |-env
            //  |  |-...         // no target
            //  |-foo.dfprop     // *base map
            //  |-foo+.dfprop    // if exists
            map = callReadingMapChecked(handler, dfpropPath);
            if (map != null) { // top driven without environment
                resolveOutsidePropInheritMap(handler, dfpropPath, map);
            }
        }
        if (map == null && !_returnsNullIfNotFound) {
            map = new LinkedHashMap<String, ELEMENT>(2); // not read-only just in case
        }
        return map;
    }

    protected <ELEMENT> Map<String, ELEMENT> callReadingMapChecked(DfPropReadingMapHandler<ELEMENT> handler, String path) {
        try {
            return handler.readMap(path);
        } catch (FileNotFoundException ignored) {
            return null;
        } catch (IOException e) {
            throwDfPropFileReadFailureException(path, e);
            return null; // unreachable
        }
    }

    protected <ELEMENT> boolean resolveOutsidePropInheritMap(DfPropReadingMapHandler<ELEMENT> handler, String path,
            Map<String, ELEMENT> map) {
        if (map == null) { // no parent
            return false;
        }
        final String inheritPath = deriveInheritPath(path);
        if (inheritPath == null) {
            return false;
        }
        final Map<String, ELEMENT> inheritMap = callReadingMapChecked(handler, inheritPath);
        if (inheritMap == null) {
            return false;
        }
        map.putAll(inheritMap);
        return true;
    }

    // -----------------------------------------------------
    //                                                  List
    //                                                  ----
    protected <ELEMENT> List<ELEMENT> doReadList(String dfpropPath, String envType,
            DfPropReadingListHandler<ELEMENT> handler) {
        // extended list is not supported
        List<ELEMENT> list = null;
        if (envType != null) {
            final String envPath = deriveEnvPath(dfpropPath, envType);
            list = callReadingListChecked(handler, envPath);
            if (list == null) {
                list = callReadingListChecked(handler, dfpropPath);
            }
        } else {
            list = callReadingListChecked(handler, dfpropPath);
        }
        if (list == null && !_returnsNullIfNotFound) {
            list = new ArrayList<ELEMENT>(2); // not read-only just in case
        }
        return list;
    }

    protected <ELEMENT> List<ELEMENT> callReadingListChecked(DfPropReadingListHandler<ELEMENT> handler, String path) {
        try {
            return handler.readList(path);
        } catch (FileNotFoundException ignored) {
            return null;
        } catch (IOException e) {
            throwDfPropFileReadFailureException(path, e);
            return null; // unreachable
        }
    }

    // -----------------------------------------------------
    //                                                String
    //                                                ------
    protected String doReadString(String dfpropPath, String envType, DfPropReadingStringHandler handler) {
        // extended string is not supported
        String str = null;
        if (envType != null) {
            final String envPath = deriveEnvPath(dfpropPath, envType);
            str = callReadingStringChecked(handler, envPath);
            if (str == null) {
                str = callReadingStringChecked(handler, dfpropPath);
            }
        } else {
            str = callReadingStringChecked(handler, dfpropPath);
        }
        if (str == null && !_returnsNullIfNotFound) {
            str = "";
        }
        return str;
    }

    protected String callReadingStringChecked(DfPropReadingStringHandler handler, String envPath) {
        try {
            return handler.readString(envPath);
        } catch (FileNotFoundException ignored) {
            return null;
        } catch (IOException e) {
            throwDfPropFileReadFailureException(envPath, e);
            return null; // unreachable
        }
    }

    // ===================================================================================
    //                                                                         Derive Path
    //                                                                         ===========
    protected String deriveEnvPath(String dfpropPath, String envType) {
        final String basePath;
        final String pureFileName;
        if (dfpropPath.contains("/")) {
            basePath = Srl.substringLastFront(dfpropPath, "/");
            pureFileName = Srl.substringLastRear(dfpropPath, "/");
        } else {
            basePath = ".";
            pureFileName = dfpropPath;
        }
        return basePath + "/" + envType + "/" + pureFileName;
    }

    protected String deriveInheritPath(String path) {
        final String allowedExt = getInheritAllowedExt();
        if (!path.endsWith(allowedExt)) { // out of target e.g. .dataprop
            return null;
        }
        return path.substring(0, path.length() - allowedExt.length()) + "+" + allowedExt;
    }

    protected String getInheritAllowedExt() {
        return ".dfprop";
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected InputStream createInputStream(String path) throws FileNotFoundException {
        return new FileInputStream(path);
    }

    protected void throwDfPropFileReadFailureException(String path, IOException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to read the DBFlute property file.");
        br.addItem("Advice");
        br.addElement("Make sure the map-string is correct in the file.");
        br.addElement("For exapmle, the number of start and end braces are the same.");
        br.addItem("DBFlute Property");
        br.addElement(path);
        final String msg = br.buildExceptionMessage();
        throw new DfPropFileReadFailureException(msg, e);
    }

    // ===================================================================================
    //                                                                       Map List File
    //                                                                       =============
    protected MapListFile createMapListFilePlain() {
        final MapListFile mapListFile = newMapListFile();
        if (_checkDuplicateEntry) {
            mapListFile.checkDuplicateEntry();
        }
        return mapListFile;
    }

    protected MapListFile createMapListFileStructural() {
        final MapListFile file = createMapListFilePlain();
        if (_skipLineSeparator) {
            file.skipLineSeparator();
        }
        return file;
    }

    protected MapListFile newMapListFile() {
        return new MapListFile();
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    protected void assertDfpropPath(String dfpropPath) {
        if (dfpropPath == null || dfpropPath.trim().length() == 0) {
            String msg = "The argument 'dfpropPath' should not be null or empty: " + dfpropPath;
            throw new IllegalArgumentException(msg);
        }
    }

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    public DfPropFile returnsNullIfNotFound() {
        _returnsNullIfNotFound = true;
        return this;
    }

    public DfPropFile skipLineSeparator() {
        _skipLineSeparator = true;
        return this;
    }

    public DfPropFile checkDuplicateEntry() {
        _checkDuplicateEntry = true;
        return this;
    }
}