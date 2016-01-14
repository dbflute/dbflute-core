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
package org.dbflute.friends.velocity;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.dbflute.DfBuildProperties;
import org.dbflute.exception.DfTemplateParsingException;
import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.properties.DfBasicProperties;

/**
 * The generator for DBFlute using Velocity.
 * @author modified by taktos (originated in Velocity)
 * @author modified by jflute (originated in Velocity)
 * @since 0.7.6 (2008/07/01 Tuesday)
 */
public class DfFlutistGenerator extends DfGenerator {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String OUTPUT_PATH = "output.path";
    public static final String TEMPLATE_PATH = "template.path";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Properties props = new Properties();
    protected Context controlContext;
    protected final Hashtable<String, Writer> writers = new Hashtable<String, Writer>();
    protected String outputEncoding;
    protected String inputEncoding;
    protected final List<String> parseFileNameList = new ArrayList<String>(); // *extension
    protected final List<String> skipFileNameList = new ArrayList<String>(); // *extension

    /** The engine instance of velocity. (NotNull: after initialization, Overridden: when initialization) */
    protected VelocityEngine velocityEngine;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfFlutistGenerator() {
        setDefaultProps();
    }

    protected void setDefaultProps() {
        props.put("path.output", "output");
        props.put("context.objects.strings", "org.apache.velocity.util.StringUtils");
        props.put("context.objects.files", "org.apache.velocity.texen.util.FileUtil");
        props.put("context.objects.properties", "org.apache.velocity.texen.util.PropertiesUtil");
    }

    // ===================================================================================
    //                                                                      Prepare Engine
    //                                                                      ==============
    public void initializeEngine() {
        velocityEngine = new VelocityEngine();
    }

    protected void initializeEngineIfNeeds() {
        if (velocityEngine == null) {
            initializeEngine();
        }
    }

    public void addProperty(String key, String value) {
        initializeEngineIfNeeds();
        velocityEngine.addProperty(key, value);
    }

    public void setProperty(String key, String value) {
        initializeEngineIfNeeds();
        velocityEngine.setProperty(key, value);
    }

    public VelocityEngine getVelocityEngine() {
        initializeEngineIfNeeds();
        return velocityEngine;
    }

    // ===================================================================================
    //                                                                     Engine Resource
    //                                                                     ===============
    public void setOutputPath(String outputPath) {
        props.put(OUTPUT_PATH, outputPath);
    }

    public String getOutputPath() {
        return props.getProperty(OUTPUT_PATH);
    }

    public void setTemplatePath(String templatePath) {
        props.put(TEMPLATE_PATH, templatePath);
    }

    public String getTemplatePath() {
        return props.getProperty(TEMPLATE_PATH);
    }

    public void setOutputEncoding(String outputEncoding) {
        this.outputEncoding = outputEncoding;
    }

    public void setInputEncoding(String inputEncoding) {
        this.inputEncoding = inputEncoding;
    }

    // ===================================================================================
    //                                                                      Parse Template
    //                                                                      ==============
    /**
     * Parse an input and write the output to an output file.  If the
     * output file parameter is null or an empty string the result is
     * returned as a string object.  Otherwise an empty string is returned.
     * @param inputTemplate input template
     * @param outputFile output file (NullAllowed: if you use as nested parsing, you should set null about this)
     */
    public String parse(String inputTemplate, String outputFile) throws Exception {
        return parse(inputTemplate, outputFile, null, null);
    }

    /**
     * Parse an input and write the output to an output file.  If the
     * output file parameter is null or an empty string the result is
     * returned as a string object.  Otherwise an empty string is returned.
     * You can add objects to the context with the objs Hashtable.
     * @param inputTemplate input template
     * @param outputFile output file. (NullAllowed: if you use as nested parsing, you should set null about this)
     * @param objectID id for object to be placed in the control context
     * @param object object to be placed in the context
     * @return String generated output from velocity
     */
    public String parse(String inputTemplate, String outputFile, String objectID, Object object) throws Exception {
        return parse(inputTemplate, null, outputFile, null, objectID, object);
    }

    /**
     * Parse an input and write the output to an output file.  If the
     * output file parameter is null or an empty string the result is
     * returned as a string object.  Otherwise an empty string is returned.
     * You can add objects to the context with the objs Hashtable.
     * @param inputTemplate input template
     * @param specifiedInputEncoding inputEncoding template encoding
     * @param outputFile output file (NullAllowed: if you use as nested parsing, you should set null about this)
     * @param specifiedOutputEncoding outputEncoding encoding of output file
     * @param objectID id for object to be placed in the control context
     * @param object object to be placed in the context
     * @return String generated output from velocity
     */
    public String parse(String inputTemplate, String specifiedInputEncoding, String outputFile, String specifiedOutputEncoding,
            String objectID, Object object) throws Exception {
        if (inputTemplate == null) {
            String msg = "The argument 'inputTemplate' should not be null: outputFile=" + outputFile;
            throw new IllegalArgumentException(msg);
        }
        if (objectID != null && object != null) {
            controlContext.put(objectID, object);
        }
        if (specifiedInputEncoding == null || specifiedInputEncoding.trim().length() == 0) {
            specifiedInputEncoding = this.inputEncoding;
        }
        if (specifiedOutputEncoding == null || specifiedOutputEncoding.trim().length() == 0) {
            specifiedOutputEncoding = this.outputEncoding;
        }
        try {
            final Template template = getTemplate(inputTemplate, specifiedInputEncoding);
            parseFileNameList.add(outputFile);

            final VelocityContext vc = new VelocityContext(controlContext);
            final StringWriter sw = new StringWriter();
            template.merge(vc, sw);
            final String newContent = resolveLineSeparatorIfNeeds(sw.toString());

            // return the contents as string if no output
            if (outputFile == null || outputFile.isEmpty()) {
                return newContent;
            }

            final File oldFile = new File(buildOutputFullPath(outputFile));
            if (oldFile.exists()) { // then it might skip to generate if it's completely same
                final String oldContent = new String(getBytes(oldFile), specifiedOutputEncoding);
                if (newContent.equals(oldContent)) {
                    skipFileNameList.add(oldFile.getName());
                    return "";
                }
            }

            final Writer writer = findOutputWriter(outputFile, specifiedOutputEncoding);
            writer.write(newContent);

            // this is commented out because it is closed in shutdown();
            //writer.close();
        } catch (Throwable e) {
            throwTemplateParsingException(inputTemplate, specifiedInputEncoding, e);
        }
        return "";
    }

    protected Writer findOutputWriter(String outputFile, String specifiedOutputEncoding) throws Exception {
        final String uniqueKey = buildOutputFullPath(outputFile);
        final Writer writer;
        if (writers.get(outputFile) == null) {
            // We have never seen this file before so create a new file writer for it.
            writer = getWriter(getOutputPath() + File.separator + outputFile, specifiedOutputEncoding);

            // Place the file writer in our collection of file writers.
            writers.put(uniqueKey, writer);
        } else {
            writer = (Writer) writers.get(uniqueKey);
        }
        return writer;
    }

    protected String buildOutputFullPath(String outputFile) {
        return getOutputPath() + "/" + outputFile;
    }

    protected String resolveLineSeparatorIfNeeds(String contents) {
        if (isConvertSourceCodeLineSeparator()) {
            if (isSourceLineSeparatorLf()) {
                final String sourceLineSeparator = getSourceLineSeparator();
                return contents.replaceAll("\r\n", sourceLineSeparator);
            } else if (isSourceLineSeparatorCrLf()) {
                final String sourceLineSeparator = getSourceLineSeparator();
                return contents.replaceAll("\r\n", "\n").replaceAll("\n", sourceLineSeparator);
            }
            // basically LF or CRLF
        }
        return contents;
    }

    protected void throwTemplateParsingException(String inputTemplate, String specifiedInputEncoding, Throwable e) {
        rethrowIfNestedException(inputTemplate, specifiedInputEncoding, e);
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to parse the input template.");
        br.addItem("Input Template");
        br.addElement(inputTemplate + " (" + specifiedInputEncoding + ")");
        final Throwable cause;
        if (e instanceof MethodInvocationException) {
            cause = ((MethodInvocationException) e).getWrappedThrowable();
        } else {
            cause = e;
        }
        final String msg = br.buildExceptionMessage();
        throw new DfTemplateParsingException(msg, cause);
    }

    protected void rethrowIfNestedException(String inputTemplate, String specifiedInputEncoding, Throwable e) {
        // avoid being duplicate wrapping
        if (e instanceof DfTemplateParsingException) {
            throw (DfTemplateParsingException) e;
        }
        if (e.getCause() instanceof DfTemplateParsingException) {
            throw (DfTemplateParsingException) e.getCause();
        }
        if (e instanceof MethodInvocationException) {
            final Throwable wrapped = ((MethodInvocationException) e).getWrappedThrowable();
            if (wrapped instanceof DfTemplateParsingException) {
                throw (DfTemplateParsingException) wrapped;
            }
        }
    }

    public String parse(String controlTemplate, Context controlContext) throws Exception {
        this.controlContext = controlContext;
        fillContextDefaults(this.controlContext);
        fillContextProperties(this.controlContext);

        final Template template = getTemplate(controlTemplate, inputEncoding);
        final StringWriter sw = new StringWriter();
        template.merge(controlContext, sw);

        return sw.toString();
    }

    protected Context getContext(Hashtable<?, ?> objs) {
        fillContextHash(controlContext, objs);
        return controlContext;
    }

    protected void fillContextHash(Context context, Hashtable<?, ?> objs) {
        Enumeration<?> enu = objs.keys();
        while (enu.hasMoreElements()) {
            String key = enu.nextElement().toString();
            context.put(key, objs.get(key));
        }
    }

    protected void fillContextDefaults(Context context) {
        context.put("generator", this);
        context.put("outputDirectory", getOutputPath());
    }

    protected void fillContextProperties(Context context) {
        final Enumeration<?> enu = props.propertyNames();
        while (enu.hasMoreElements()) {
            final String nm = (String) enu.nextElement();
            if (nm.startsWith("context.objects.")) {
                final String contextObj = props.getProperty(nm);
                final int colon = nm.lastIndexOf('.');
                final String contextName = nm.substring(colon + 1);
                try {
                    final Class<?> cls = Class.forName(contextObj);
                    final Object o = cls.newInstance();
                    context.put(contextName, o);
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to create the instance: " + contextObj, e);
                }
            }
        }
    }

    public void shutdown() {
        final Iterator<Writer> iterator = writers.values().iterator();
        while (iterator.hasNext()) {
            Writer writer = (Writer) iterator.next();
            try {
                writer.flush();
                writer.close();
            } catch (Exception e) {
                /* do nothing */
            }
        }
        // clear the file writers cache
        writers.clear();
    }

    public Writer getWriter(String path, String encoding) throws Exception {
        Writer writer;
        if (encoding == null || encoding.length() == 0 || encoding.equals("8859-1") || encoding.equals("8859_1")) {
            writer = new FileWriter(path);
        } else {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), encoding));
        }
        return writer;
    }

    public Template getTemplate(String templateName, String encoding) throws Exception {
        initializeEngineIfNeeds();
        final Template template;
        if (encoding == null || encoding.length() == 0 || encoding.equals("8859-1") || encoding.equals("8859_1")) {
            template = velocityEngine.getTemplate(templateName);
        } else {
            template = velocityEngine.getTemplate(templateName, encoding);
        }
        return template;
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected byte[] getBytes(File file) throws IOException {
        return getBytes(create(file));
    }

    protected final byte[] getBytes(InputStream is) throws IOException {
        byte[] bytes = null;
        byte[] buf = new byte[8192];
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int n = 0;
            while ((n = is.read(buf, 0, buf.length)) != -1) {
                baos.write(buf, 0, n);
            }
            bytes = baos.toByteArray();
        } catch (IOException e) {
            throw e;
        } finally {
            if (is != null) {
                close(is);
            }
        }
        return bytes;
    }

    public static void close(InputStream is) throws IOException {
        if (is == null) {
            return;
        }
        try {
            is.close();
        } catch (IOException e) {
            throw e;
        }
    }

    protected FileInputStream create(File file) throws IOException {
        return new FileInputStream(file);
    }

    public List<String> getParseFileNameList() {
        return parseFileNameList;
    }

    public List<String> getSkipFileNameList() {
        return skipFileNameList;
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
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

    protected String getSourceLineSeparator() {
        return getBasicProperties().getSourceCodeLineSeparator();
    }

    protected boolean isConvertSourceCodeLineSeparator() {
        return getBasicProperties().isConvertSourceCodeLineSeparator();
    }

    protected boolean isSourceLineSeparatorLf() {
        return getBasicProperties().isSourceCodeLineSeparatorLf();
    }

    protected boolean isSourceLineSeparatorCrLf() {
        return getBasicProperties().isSourceCodeLineSeparatorCrLf();
    }

    protected boolean isSkipGenerateIfSameFile() {
        return getProperties().getLittleAdjustmentProperties().isSkipGenerateIfSameFile();
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "outputEncoding=" + outputEncoding + ", inputEncoding=" + inputEncoding + " skipFileNameList=" + skipFileNameList;
    }
}
