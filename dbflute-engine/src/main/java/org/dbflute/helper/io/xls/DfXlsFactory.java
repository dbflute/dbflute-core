/*
 * Copyright 2014-2015 the original author or authors.
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
package org.dbflute.helper.io.xls;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Workbook;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.util.DfCollectionUtil;
import org.dbflute.util.DfReflectionUtil;
import org.dbflute.util.DfReflectionUtil.ReflectionFailureException;

/**
 * @author p1us2er0
 * @since 1.1.0-SP8 (2015/08/21 Friday)
 */
public class DfXlsFactory {

    protected static final DfXlsFactory DF_XLS_FACTORY = new DfXlsFactory();
    protected static final String XSSF_WORKBOOK_NAME = "org.apache.poi.xssf.usermodel.XSSFWorkbook";
    protected static final String SXSSF_WORKBOOK_NAME = "org.apache.poi.xssf.streaming.SXSSFWorkbook";
    protected static final String XSSF_RICH_TEXT_STRING_NAME = "org.apache.poi.xssf.usermodel.XSSFRichTextString";
    protected static final Map<String, Class<?>> XSSF_TYPE_MAP = DfCollectionUtil.newHashMap();
    static {
        try {
            XSSF_TYPE_MAP.put(XSSF_WORKBOOK_NAME, Class.forName(XSSF_WORKBOOK_NAME));
            XSSF_TYPE_MAP.put(SXSSF_WORKBOOK_NAME, Class.forName(SXSSF_WORKBOOK_NAME));
            XSSF_TYPE_MAP.put(XSSF_RICH_TEXT_STRING_NAME, Class.forName(XSSF_RICH_TEXT_STRING_NAME));
        } catch (ClassNotFoundException ignore) {
            XSSF_TYPE_MAP.clear();
        }
    }

    private DfXlsFactory(){
    }

    public static DfXlsFactory instance() {
        return DF_XLS_FACTORY;
    }

    public String getDefaultFileExtension() {
        return XSSF_TYPE_MAP.isEmpty() ? ".xls" : ".xlsx";
    }

    public FileFilter createXlsFileFilter() {
        final List<String> fileExtensionList = DfCollectionUtil.newArrayList();
        fileExtensionList.add(".xls");
        fileExtensionList.add(".xlsx");
        return new FileFilter() {
            public boolean accept(File file) {
                return fileExtensionList.stream().anyMatch(fileExtension -> file.getName().endsWith(fileExtension));
            }
        };
    }

    public Workbook createWorkbook(File file) {
        try {
            if (file.getName().endsWith(".xlsx")) {
                return createXSSFWorkbook(file);
            }
            return file.exists() && file.length() > 0 ? new HSSFWorkbook(new FileInputStream(file)) : new HSSFWorkbook();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create workbook: " + file, e);
        }
    }

    protected Workbook createXSSFWorkbook(File file) {
        if (XSSF_TYPE_MAP.isEmpty()) {
            throwXSSFTypeNotFoundException(file.getName());
            return null; // unreachable
        }

        try {
            final Class<?>[] argTypes = new Class<?>[] { InputStream.class };
            final Constructor<?> constructor = DfReflectionUtil.getConstructor(XSSF_TYPE_MAP.get(XSSF_WORKBOOK_NAME), argTypes);
            if (file.exists() && file.length() > 0) {
                return (Workbook) DfReflectionUtil.newInstance(constructor, new Object[] {new FileInputStream(file)});
            }
            return (Workbook) DfReflectionUtil.newInstance(XSSF_TYPE_MAP.get(SXSSF_WORKBOOK_NAME));
        } catch (IOException | ReflectionFailureException e) {
            throw new IllegalStateException("Failed to create workbook: " + file, e);
        }
    }

    public RichTextString createRichTextString(Workbook workbook, String str) {
        if (workbook.getClass().getName().equals(XSSF_WORKBOOK_NAME)) {
            return createXSSFRichTextString(workbook, str);
        }
        return new HSSFRichTextString(str);
    }

    protected RichTextString createXSSFRichTextString(Workbook workbook, String str) {
        if (XSSF_TYPE_MAP.isEmpty()) {
            throwXSSFTypeNotFoundException(workbook.toString());
            return null; // unreachable
        }

        try {
            final Class<?>[] argTypes = new Class<?>[] { String.class };
            final Constructor<?> constructor = DfReflectionUtil.getConstructor(XSSF_TYPE_MAP.get(XSSF_RICH_TEXT_STRING_NAME), argTypes);
            return (RichTextString) DfReflectionUtil.newInstance(constructor, new Object[] {str});
        } catch (ReflectionFailureException e) {
            throw new IllegalStateException("Failed to create richText", e);
        }
    }


    protected void throwXSSFTypeNotFoundException(String resourceFile) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found poi-ooxml.");
        br.addItem("Advice");
        br.addElement("You should put the poi-ooxml jar file to the 'extlib' directory");
        br.addElement("on DBFlute client when you use xlsx handling.");
        br.addElement("For example:");
        br.addElement("  {DBFluteClient}");
        br.addElement("    |-dfprop");
        br.addElement("    |-extlib");
        br.addElement("    |  |-poi-ooxml-3.12.jar");
        br.addElement("    |  |-poi-ooxml-schemas-3.12.jar");
        br.addElement("    |  |-stax-api-1.0.1.jar");
        br.addElement("    |  |-xmlbeans-2.6.0.jar");
        br.addElement("    |-...");
        br.addItem("Resource File");
        br.addElement(resourceFile);
        final String msg = br.buildExceptionMessage();
        throw new IllegalStateException(msg);
    }
}
