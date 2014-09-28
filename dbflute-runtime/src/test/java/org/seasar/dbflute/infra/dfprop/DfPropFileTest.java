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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.seasar.dbflute.unit.core.PlainTestCase;

/**
 * @author jflute
 */
public class DfPropFileTest extends PlainTestCase {

    // ===================================================================================
    //                                                                      Switched Style
    //                                                                      ==============
    /**
     * <pre>
     * dbflute_exampledb
     *  |-dfprop
     *  |  |-maihama
     *  |  |  |-exampleMap.dfprop  // env
     *  |  |-exampleMap.dfprop     // main
     *  
     * env: default
     * </pre>
     * @throws Exception 
     */
    public void test_readMap_SwitchStyle_default() throws Exception {
        // ## Arrange ##
        DfPropFile propFile = createSwitchStylePropFile();

        // ## Act ##
        Map<String, Object> map = propFile.readMap("/dfprop/exampleMap.dfprop", null);

        // ## Assert ##
        log(map);
        assertEquals(2, map.size());
        assertEquals(map.get("main"), "mainValue");
        assertEquals(map.get("main*"), "mainValue*");
    }

    /**
     * <pre>
     * dbflute_exampledb
     *  |-dfprop
     *  |  |-maihama
     *  |  |  |-exampleMap.dfprop  // env
     *  |  |-exampleMap.dfprop     // main
     *  
     * env: maihama
     * </pre>
     * @throws Exception 
     */
    public void test_readMap_SwitchStyle_maihama() throws Exception {
        // ## Arrange ##
        DfPropFile propFile = createSwitchStylePropFile();

        // ## Act ##
        Map<String, Object> map = propFile.readMap("/dfprop/exampleMap.dfprop", "maihama");

        // ## Assert ##
        log(map);
        assertEquals(1, map.size());
        map.put("env", "envValue");
    }

    /**
     * <pre>
     * dbflute_exampledb
     *  |-dfprop
     *  |  |-maihama
     *  |  |  |-exampleMap.dfprop  // env
     *  |  |-exampleMap.dfprop     // main
     *  
     * env: noexists
     * </pre>
     * @throws Exception 
     */
    public void test_readMap_SwitchStyle_noexists() throws Exception {
        // ## Arrange ##
        DfPropFile propFile = createSwitchStylePropFile();

        // ## Act ##
        Map<String, Object> map = propFile.readMap("/dfprop/exampleMap.dfprop", "noexists");

        // ## Assert ##
        log(map);
        assertEquals(2, map.size());
        assertEquals(map.get("main"), "mainValue");
        assertEquals(map.get("main*"), "mainValue*");
    }

    protected DfPropFile createSwitchStylePropFile() {
        return new DfPropFile() {
            protected <ELEMENT> Map<String, ELEMENT> callReadingMapChecked(DfPropReadingMapHandler<ELEMENT> handler,
                    String path) {
                return prepareSwitchStyleMap(path);
            }
        };
    }

    @SuppressWarnings("unchecked")
    protected <ELEMENT> Map<String, ELEMENT> prepareSwitchStyleMap(String path) {
        Map<String, Object> mockMap = newLinkedHashMap();
        if ("/dfprop/exampleMap.dfprop".equals(path)) {
            mockMap.put("main", "mainValue");
            mockMap.put("main*", "mainValue*");
        } else if ("/dfprop/maihama/exampleMap.dfprop".equals(path)) {
            mockMap.put("env", "envValue");
        } else {
            mockMap = null;
        }
        return (Map<String, ELEMENT>) mockMap;
    }

    /**
     * <pre>
     * dbflute_exampledb
     *  |-dfprop
     *  |  |-maihama
     *  |  |  |-exampleMap.dfprop  // env
     *  |  |-exampleMap.dfprop     // main
     *  
     * env: noexists
     * </pre>
     * @throws Exception 
     */
    public void test_readMap_SwitchStyle_emptySwitch() throws Exception {
        // ## Arrange ##
        DfPropFile propFile = new DfPropFile() {
            @SuppressWarnings("unchecked")
            @Override
            protected <ELEMENT> Map<String, ELEMENT> callReadingMapChecked(DfPropReadingMapHandler<ELEMENT> handler,
                    String path) {
                Map<String, Object> mockMap = newLinkedHashMap();
                if ("/dfprop/exampleMap.dfprop".equals(path)) {
                    mockMap.put("main", "mainValue");
                    mockMap.put("main*", "mainValue*");
                } else if ("/dfprop/maihama/exampleMap.dfprop".equals(path)) {
                } else {
                    mockMap = null;
                }
                return (Map<String, ELEMENT>) mockMap;
            }
        };

        // ## Act ##
        Map<String, Object> map = propFile.readMap("/dfprop/exampleMap.dfprop", "maihama");

        // ## Assert ##
        log(map);
        assertHasZeroElement(map.keySet());
    }

    // ===================================================================================
    //                                                                       Inherit Style
    //                                                                       =============
    /**
     * <pre>
     * dbflute_exampledb
     *  |-dfprop
     *  |  |-maihama
     *  |  |  |-exampleMap+.dfprop // env+
     *  |  |-exampleMap.dfprop     // main
     *  
     * env: default
     * </pre>
     * @throws Exception 
     */
    public void test_readMap_InheritStyle_default() throws Exception {
        // ## Arrange ##
        DfPropFile propFile = createInheritStylePropFile();

        // ## Act ##
        Map<String, Object> map = propFile.readMap("/dfprop/exampleMap.dfprop", null);

        // ## Assert ##
        log(map);
        assertEquals(2, map.size());
        assertEquals(map.get("main"), "mainValue");
        assertEquals(map.get("main*"), "mainValue*");
    }

    /**
     * <pre>
     * dbflute_exampledb
     *  |-dfprop
     *  |  |-maihama
     *  |  |  |-exampleMap+.dfprop // env
     *  |  |-exampleMap.dfprop     // main
     *  
     * env: maihama
     * </pre>
     * @throws Exception 
     */
    public void test_readMap_InheritStyle_maihama() throws Exception {
        // ## Arrange ##
        DfPropFile propFile = createInheritStylePropFile();

        // ## Act ##
        Map<String, Object> map = propFile.readMap("/dfprop/exampleMap.dfprop", "maihama");

        // ## Assert ##
        log(map);
        assertEquals(3, map.size());
        assertEquals(map.get("main"), "mainValue");
        assertEquals(map.get("main*"), "envValue*");
        assertEquals(map.get("env+"), "envPlusValue");
    }

    /**
     * <pre>
     * dbflute_exampledb
     *  |-dfprop
     *  |  |-maihama
     *  |  |  |-exampleMap+.dfprop // env+
     *  |  |-exampleMap.dfprop     // main
     *  
     * env: noexists
     * </pre>
     * @throws Exception 
     */
    public void test_readMap_InheritStyle_noexists() throws Exception {
        // ## Arrange ##
        DfPropFile propFile = createInheritStylePropFile();

        // ## Act ##
        Map<String, Object> map = propFile.readMap("/dfprop/exampleMap.dfprop", "noexists");

        // ## Assert ##
        log(map);
        assertEquals(2, map.size());
        assertEquals(map.get("main"), "mainValue");
        assertEquals(map.get("main*"), "mainValue*");
    }

    protected DfPropFile createInheritStylePropFile() {
        return new DfPropFile() {
            protected <ELEMENT> Map<String, ELEMENT> callReadingMapChecked(DfPropReadingMapHandler<ELEMENT> handler,
                    String path) {
                return prepareInheritStyleMap(path);
            }
        };
    }

    @SuppressWarnings("unchecked")
    protected <ELEMENT> Map<String, ELEMENT> prepareInheritStyleMap(String path) {
        Map<String, Object> mockMap = newLinkedHashMap();
        if ("/dfprop/exampleMap.dfprop".equals(path)) {
            mockMap.put("main", "mainValue");
            mockMap.put("main*", "mainValue*");
        } else if ("/dfprop/maihama/exampleMap+.dfprop".equals(path)) {
            mockMap.put("env+", "envPlusValue");
            mockMap.put("main*", "envValue*");
        } else {
            mockMap = null;
        }
        return (Map<String, ELEMENT>) mockMap;
    }

    /**
     * <pre>
     * dbflute_exampledb
     *  |-dfprop
     *  |  |-maihama
     *  |  |  |-exampleMap+.dfprop // env
     *  |  |-exampleMap.dfprop     // main
     *  
     * env: maihama
     * </pre>
     * @throws Exception 
     */
    public void test_readMap_InheritStyle_emptyInherit() throws Exception {
        // ## Arrange ##
        DfPropFile propFile = new DfPropFile() {
            @SuppressWarnings("unchecked")
            @Override
            protected <ELEMENT> Map<String, ELEMENT> callReadingMapChecked(DfPropReadingMapHandler<ELEMENT> handler,
                    String path) {
                Map<String, Object> mockMap = newLinkedHashMap();
                if ("/dfprop/exampleMap.dfprop".equals(path)) {
                    mockMap.put("main", "mainValue");
                    mockMap.put("main*", "mainValue*");
                } else if ("/dfprop/maihama/exampleMap+.dfprop".equals(path)) {
                } else {
                    mockMap = null;
                }
                return (Map<String, ELEMENT>) mockMap;
            }
        };

        // ## Act ##
        Map<String, Object> map = propFile.readMap("/dfprop/exampleMap.dfprop", "maihama");

        // ## Assert ##
        log(map);
        assertEquals(2, map.size());
        assertEquals(map.get("main"), "mainValue");
        assertEquals(map.get("main*"), "mainValue*");
    }

    /**
     * <pre>
     * dbflute_exampledb
     *  |-dfprop
     *  |  |-maihama
     *  |  |  |-exampleMap+.dfprop // env
     *  |  |-exampleMap.dfprop     // main
     *  |  |-exampleMap+.dfprop    // main+
     *  
     * env: maihama
     * </pre>
     * @throws Exception
     */
    public void test_readMap_InheritStyle_doubleInherit() throws Exception {
        // ## Arrange ##
        DfPropFile propFile = new DfPropFile() {
            @SuppressWarnings("unchecked")
            @Override
            protected <ELEMENT> Map<String, ELEMENT> callReadingMapChecked(DfPropReadingMapHandler<ELEMENT> handler,
                    String path) {
                Map<String, Object> mockMap = newLinkedHashMap();
                if ("/dfprop/exampleMap.dfprop".equals(path)) {
                    mockMap.put("main", "mainValue");
                    mockMap.put("main*", "mainValue*");
                } else if ("/dfprop/exampleMap+.dfprop".equals(path)) {
                    mockMap.put("main+", "mainPlusValue");
                    mockMap.put("main+*", "mainPlusValue*");
                } else if ("/dfprop/maihama/exampleMap+.dfprop".equals(path)) {
                    mockMap.put("main*", "envPlugValue*");
                    mockMap.put("env+", "envPlusValue");
                    mockMap.put("main+*", "envPlusValue*");
                } else {
                    mockMap = null;
                }
                return (Map<String, ELEMENT>) mockMap;
            }
        };

        // ## Act ##
        Map<String, Object> map = propFile.readMap("/dfprop/exampleMap.dfprop", "maihama");

        // ## Assert ##
        log(map);
        assertEquals(5, map.size());
        assertEquals(map.get("main"), "mainValue");
        assertEquals(map.get("main*"), "envPlugValue*");
        assertEquals(map.get("main+"), "mainPlusValue");
        assertEquals(map.get("env+"), "envPlusValue");
        assertEquals(map.get("main+*"), "envPlusValue*");
    }

    // ===================================================================================
    //                                                                           All Stars
    //                                                                           =========
    /**
     * <pre>
     * dbflute_exampledb
     *  |-dfprop
     *  |  |-maihama
     *  |  |  |-exampleMap.dfprop  // env
     *  |  |  |-exampleMap+.dfprop // env+
     *  |  |-exampleMap.dfprop     // main
     *  |  |-exampleMap+.dfprop    // main+
     *  
     * env: default
     * </pre>
     * @throws Exception 
     */
    public void test_readMap_AllStars_default() throws Exception {
        // ## Arrange ##
        DfPropFile propFile = createAllStarsPropFile();

        // ## Act ##
        Map<String, Object> map = propFile.readMap("/dfprop/exampleMap.dfprop", null);

        // ## Assert ##
        log(map);
        assertEquals(3, map.size());
        assertEquals(map.get("main"), "mainValue");
        assertEquals(map.get("main+"), "mainPlusValue");
        assertEquals(map.get("main*"), "mainValue*");
    }

    /**
     * <pre>
     * dbflute_exampledb
     *  |-dfprop
     *  |  |-maihama
     *  |  |  |-exampleMap.dfprop  // env
     *  |  |  |-exampleMap+.dfprop // env+
     *  |  |-exampleMap.dfprop     // main
     *  |  |-exampleMap+.dfprop    // main+
     *  
     * env: maihama
     * </pre>
     * @throws Exception 
     */
    public void test_readMap_AllStars_maihama() throws Exception {
        // ## Arrange ##
        DfPropFile propFile = createAllStarsPropFile();

        // ## Act ##
        Map<String, Object> map = propFile.readMap("/dfprop/exampleMap.dfprop", "maihama");

        // ## Assert ##
        log(map);
        assertEquals(3, map.size());
        assertEquals(map.get("env"), "envValue");
        assertEquals(map.get("env+"), "envPlusValue");
        assertEquals(map.get("main*"), "envValue*");
    }

    /**
     * <pre>
     * dbflute_exampledb
     *  |-dfprop
     *  |  |-maihama
     *  |  |  |-exampleMap.dfprop  // env
     *  |  |  |-exampleMap+.dfprop // env+
     *  |  |-exampleMap.dfprop     // main
     *  |  |-exampleMap+.dfprop    // main+
     *  
     * env: noexists
     * </pre>
     * @throws Exception 
     */
    public void test_readMap_AllStars_noExists() throws Exception {
        // ## Arrange ##
        DfPropFile propFile = createAllStarsPropFile();

        // ## Act ##
        Map<String, Object> map = propFile.readMap("/dfprop/exampleMap.dfprop", "noexists");

        // ## Assert ##
        log(map);
        assertEquals(3, map.size());
        assertEquals(map.get("main"), "mainValue");
        assertEquals(map.get("main*"), "mainValue*");
        assertEquals(map.get("main+"), "mainPlusValue");
    }

    protected DfPropFile createAllStarsPropFile() {
        return new DfPropFile() {
            protected <ELEMENT> Map<String, ELEMENT> callReadingMapChecked(DfPropReadingMapHandler<ELEMENT> handler,
                    String path) {
                return prepareAllStarsMap(path);
            }
        };
    }

    @SuppressWarnings("unchecked")
    protected <ELEMENT> Map<String, ELEMENT> prepareAllStarsMap(String path) {
        Map<String, Object> mockMap = newLinkedHashMap();
        if ("/dfprop/exampleMap.dfprop".equals(path)) {
            mockMap.put("main", "mainValue");
            mockMap.put("main*", "mainValue*");
        } else if ("/dfprop/exampleMap+.dfprop".equals(path)) {
            mockMap.put("main+", "mainPlusValue");
        } else if ("/dfprop/maihama/exampleMap.dfprop".equals(path)) {
            mockMap.put("env", "envValue");
        } else if ("/dfprop/maihama/exampleMap+.dfprop".equals(path)) {
            mockMap.put("env+", "envPlusValue");
            mockMap.put("main*", "envValue*");
        } else {
            mockMap = null;
        }
        return (Map<String, ELEMENT>) mockMap;
    }

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    public void test_readMap_Option_returnsNullIfNotFound_exists() throws Exception {
        // ## Arrange ##
        DfPropFile propFile = new DfPropFile() {
            @SuppressWarnings("unchecked")
            @Override
            protected <ELEMENT> Map<String, ELEMENT> callReadingMapChecked(DfPropReadingMapHandler<ELEMENT> handler,
                    String path) {
                return (Map<String, ELEMENT>) newLinkedHashMap("foo", "bar");
            }
        }.returnsNullIfNotFound();

        // ## Act ##
        Map<String, Object> map = propFile.readMap("/dfprop/exampleMap.dfprop", null);

        // ## Assert ##
        log(map);
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals(map.get("foo"), "bar");
    }

    public void test_readMap_Option_returnsNullIfNotFound_empty() throws Exception {
        // ## Arrange ##
        DfPropFile propFile = new DfPropFile() {
            @Override
            protected <ELEMENT> Map<String, ELEMENT> callReadingMapChecked(DfPropReadingMapHandler<ELEMENT> handler,
                    String path) {
                return newLinkedHashMap();
            }
        }.returnsNullIfNotFound();

        // ## Act ##
        Map<String, Object> map = propFile.readMap("/dfprop/exampleMap.dfprop", null);

        // ## Assert ##
        log(map);
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    public void test_readMap_Option_returnsNullIfNotFound_noExists() throws Exception {
        // ## Arrange ##
        DfPropFile propFile = new DfPropFile().returnsNullIfNotFound();

        // ## Act ##
        Map<String, Object> map = propFile.readMap("/noexists/noexistsMap.dfprop", null);

        // ## Assert ##
        log(map);
        assertNull(map);
    }

    // ===================================================================================
    //                                                                           Read List
    //                                                                           =========
    public void test_readList_default() throws Exception {
        // ## Arrange ##
        DfPropFile propFile = new DfPropFile() {
            @Override
            protected List<Object> actuallyReadList(String path) throws FileNotFoundException, IOException {
                List<Object> list = newArrayList();
                list.add(1);
                list.add(2);
                list.add(3);
                return list;
            }
        };

        // ## Act ##
        List<Object> readList = propFile.readList("./dfprop/exampleList.dfprop", null);

        // ## Assert ##
        assertEquals(newArrayList(1, 2, 3), readList);
    }

    public void test_readList_switched() throws Exception {
        // ## Arrange ##
        DfPropFile propFile = new DfPropFile() {
            @Override
            protected List<Object> actuallyReadList(String path) throws FileNotFoundException, IOException {
                List<Object> list = newArrayList();
                if (path.contains("/maihama/")) {
                    list.add("foo");
                    list.add("bar");
                } else {
                    list.add(1);
                    list.add(2);
                    list.add(3);
                }
                return list;
            }
        };

        // ## Act ##
        List<Object> readList = propFile.readList("./dfprop/exampleList.dfprop", "maihama");

        // ## Assert ##
        assertEquals(newArrayList("foo", "bar"), readList);
    }

    // ===================================================================================
    //                                                                           Read List
    //                                                                           =========
    public void test_readString_default() throws Exception {
        // ## Arrange ##
        DfPropFile propFile = new DfPropFile() {
            @Override
            protected String actuallyReadString(String path) throws FileNotFoundException, IOException {
                return "foo\nbar";
            }
        };

        // ## Act ##
        String readString = propFile.readString("./dfprop/example.dfprop", null);

        // ## Assert ##
        assertEquals("foo\nbar", readString);
    }

    public void test_readString_switched() throws Exception {
        // ## Arrange ##
        DfPropFile propFile = new DfPropFile() {
            @Override
            protected String actuallyReadString(String path) throws FileNotFoundException, IOException {
                if (path.contains("/maihama/")) {
                    return "maihama\ndockside";
                } else {
                    return "foo\nbar";
                }
            }
        };

        // ## Act ##
        String readString = propFile.readString("./dfprop/example.dfprop", "maihama");

        // ## Assert ##
        assertEquals("maihama\ndockside", readString);
    }

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    public void test_actuallyReadMap_FileNotFound() throws Exception {
        // ## Arrange ##
        DfPropFile propFile = new DfPropFile();

        // ## Act ##
        try {
            propFile.actuallyReadMap("/noexists/noexistsMap.dfprop");
            // ## Assert ##
            fail();
        } catch (FileNotFoundException e) {
            log(e.getMessage());
        }
    }

    public void test_actuallyReadMapAsStringValue_FileNotFound() throws Exception {
        // ## Arrange ##
        DfPropFile propFile = new DfPropFile();

        // ## Act ##
        try {
            propFile.actuallyReadMapAsStringValue("/noexists/noexistsMap.dfprop");
            // ## Assert ##
            fail();
        } catch (FileNotFoundException e) {
            log(e.getMessage());
        }
    }

    public void test_actuallyReadMapAsStringListValue_FileNotFound() throws Exception {
        // ## Arrange ##
        DfPropFile propFile = new DfPropFile();

        // ## Act ##
        try {
            propFile.actuallyReadMapAsStringListValue("/noexists/noexistsMap.dfprop");
            // ## Assert ##
            fail();
        } catch (FileNotFoundException e) {
            log(e.getMessage());
        }
    }

    public void test_actuallyReadMapAsStringMapValue_FileNotFound() throws Exception {
        // ## Arrange ##
        DfPropFile propFile = new DfPropFile();

        // ## Act ##
        try {
            propFile.actuallyReadMapAsStringMapValue("/noexists/noexistsMap.dfprop");
            // ## Assert ##
            fail();
        } catch (FileNotFoundException e) {
            log(e.getMessage());
        }
    }

    public void test_actuallyReadList_FileNotFound() throws Exception {
        // ## Arrange ##
        DfPropFile propFile = new DfPropFile();

        // ## Act ##
        try {
            propFile.actuallyReadList("/noexists/noexistsMap.dfprop");
            // ## Assert ##
            fail();
        } catch (FileNotFoundException e) {
            log(e.getMessage());
        }
    }

    public void test_actuallyReadString_FileNotFound() throws Exception {
        // ## Arrange ##
        DfPropFile propFile = new DfPropFile();

        // ## Act ##
        try {
            propFile.actuallyReadString("/noexists/noexistsMap.dfprop");
            // ## Assert ##
            fail();
        } catch (FileNotFoundException e) {
            log(e.getMessage());
        }
    }
}
