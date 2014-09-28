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
package org.seasar.dbflute.friends.velocity;

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
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.seasar.dbflute.DfBuildProperties;
import org.seasar.dbflute.exception.DfTemplateParsingException;
import org.seasar.dbflute.exception.factory.ExceptionMessageBuilder;
import org.seasar.dbflute.properties.DfBasicProperties;

/**
 * The Velocity generator for DBFlute.
 * @author modified by taktos (originated in Velocity)
 * @author modified by jflute
 * @since 0.7.6 (2008/07/01 Tuesday)
 */
public class DfFlutistGenerator extends DfGenerator {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /**
     * The generator tools used for creating additional
     * output withing the control template. This could
     * use some cleaning up.
     */
    private static final DfFlutistGenerator instance = new DfFlutistGenerator();

    /**
     * Where the texen output will placed.
     */
    public static final String OUTPUT_PATH = "output.path";

    /**
     * Where the velocity templates live.
     */
    public static final String TEMPLATE_PATH = "template.path";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /**
     * Default properties used by texen.
     */
    private Properties props = new Properties();

    /**
     * Context used for generating the texen output.
     */
    private Context controlContext;

    /**
     * Keep track of the file writers used for outputting
     * to files. If we come across a file writer more
     * then once then the additional output will be
     * appended to the file instead of overwritting
     * the contents.
     */
    private Hashtable<String, Writer> writers = new Hashtable<String, Writer>();

    /**
     * This is the encoding for the output file(s).
     */
    protected String outputEncoding;

    /**
     * This is the encoding for the input file(s)
     * (templates).
     */
    protected String inputEncoding;

    /**
     * The list of file name parsed. {DBFlute Original Attribute}
     */
    protected List<String> parseFileNameList = new ArrayList<String>();// [Extension]

    /**
     * The list of file name skipped. {DBFlute Original Attribute}
     */
    protected List<String> skipFileNameList = new ArrayList<String>();// [Extension]

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    /**
     * Default constructor.
     */
    private DfFlutistGenerator() {
        setDefaultProps();
    }

    /**
     * Create a new generator object with default properties.
     * @return Generator generator used in the control context.
     */
    public static DfFlutistGenerator getInstance() {
        return instance;
    }

    /**
     * Create a new Generator object with a given property
     * set. The property set will be duplicated.
     * @param props properties object to help populate the control context.
     */
    public DfFlutistGenerator(Properties props) {
        this.props = (Properties) props.clone();
    }

    /**
     * Set default properties.
     */
    protected void setDefaultProps() {
        props.put("path.output", "output");
        props.put("context.objects.strings", "org.apache.velocity.util.StringUtils");
        props.put("context.objects.files", "org.apache.velocity.texen.util.FileUtil");
        props.put("context.objects.properties", "org.apache.velocity.texen.util.PropertiesUtil");
    }

    // ===================================================================================
    //                                                                               Parse
    //                                                                               =====
    /**
     * Parse an input and write the output to an output file.  If the
     * output file parameter is null or an empty string the result is
     * returned as a string object.  Otherwise an empty string is returned.
     * @param inputTemplate input template
     * @param outputFile output file (NullAllowed: If you use as nested parsing, you should set null about this.)
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
     * @param outputFile output file. (NullAllowed: If you use as nested parsing, you should set null about this.)
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
     * @param outputFile output file (NullAllowed: If you use as nested parsing, you should set null about this.)
     * @param specifiedOutputEncoding outputEncoding encoding of output file
     * @param objectID id for object to be placed in the control context
     * @param object object to be placed in the context
     * @return String generated output from velocity
     */
    public String parse(String inputTemplate, String specifiedInputEncoding, String outputFile,
            String specifiedOutputEncoding, String objectID, Object object) throws Exception {
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

            final File oldFile = new File(getOutputPath() + "/" + outputFile);
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
        Writer writer = null;
        if (writers.get(outputFile) == null) {
            // We have never seen this file before so create a new file writer for it.
            writer = getWriter(getOutputPath() + File.separator + outputFile, specifiedOutputEncoding);

            // Place the file writer in our collection of file writers.
            writers.put(outputFile, writer);
        } else {
            writer = (Writer) writers.get(outputFile);
        }
        return writer;
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

    /**
     * Parse the control template and merge it with the control
     * context. This is the starting point in texen.
     * @param controlTemplate control template
     * @param controlContext control context
     * @return String generated output
     */
    public String parse(String controlTemplate, Context controlContext) throws Exception {
        this.controlContext = controlContext;
        fillContextDefaults(this.controlContext);
        fillContextProperties(this.controlContext);

        Template template = getTemplate(controlTemplate, inputEncoding);
        StringWriter sw = new StringWriter();
        template.merge(controlContext, sw);

        return sw.toString();
    }

    /**
     * Create a new context and fill it with the elements of the
     * objs Hashtable.  Default objects and objects that comes from
     * the properties of this Generator object is also added.
     * @param objs objects to place in the control context
     * @return Context context filled with objects
     */
    protected Context getContext(Hashtable<?, ?> objs) {
        fillContextHash(controlContext, objs);
        return controlContext;
    }

    /**
     * Add all the contents of a Hashtable to the context.
     * @param context context to fill with objects
     * @param objs source of objects
     */
    protected void fillContextHash(Context context, Hashtable<?, ?> objs) {
        Enumeration<?> enu = objs.keys();
        while (enu.hasMoreElements()) {
            String key = enu.nextElement().toString();
            context.put(key, objs.get(key));
        }
    }

    /**
     * Add properties that will aways be in the context by default
     * @param context control context to fill with default values.
     */
    protected void fillContextDefaults(Context context) {
        context.put("generator", instance);
        context.put("outputDirectory", getOutputPath());
    }

    /**
     * Add objects to the context from the current properties.
     * @param context control context to fill with objects
     *                that are specified in the default.properties
     *                file
     */
    protected void fillContextProperties(Context context) {
        Enumeration<?> enu = props.propertyNames();

        while (enu.hasMoreElements()) {
            String nm = (String) enu.nextElement();
            if (nm.startsWith("context.objects.")) {

                String contextObj = props.getProperty(nm);
                int colon = nm.lastIndexOf('.');
                String contextName = nm.substring(colon + 1);

                try {
                    Class<?> cls = Class.forName(contextObj);
                    Object o = cls.newInstance();
                    context.put(contextName, o);
                } catch (Exception e) {
                    e.printStackTrace();
                    //TO DO: Log Something Here
                }
            }
        }
    }

    /**
     * Properly shut down the generator, right now
     * this is simply flushing and closing the file
     * writers that we have been holding on to.
     */
    public void shutdown() {
        Iterator<Writer> iterator = writers.values().iterator();

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

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    protected String ln() {
        return "\n";
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

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    /**
     * Set the template path, where Texen will look
     * for Velocity templates.
     *
     * @param templatePath template path for velocity templates.
     */
    public void setTemplatePath(String templatePath) {
        props.put(TEMPLATE_PATH, templatePath);
    }

    /**
     * Get the template path.
     *
     * @return String template path for velocity templates.
     */
    public String getTemplatePath() {
        return props.getProperty(TEMPLATE_PATH);
    }

    /**
     * Set the output path for the generated
     * output.
     * @param outputPath output path for texen output.
     */
    public void setOutputPath(String outputPath) {
        props.put(OUTPUT_PATH, outputPath);
    }

    /**
     * Get the output path for the generated
     * output.
     *
     * @return String output path for texen output.
     */
    public String getOutputPath() {
        return props.getProperty(OUTPUT_PATH);
    }

    /**
     * Set the output encoding.
     */
    public void setOutputEncoding(String outputEncoding) {
        this.outputEncoding = outputEncoding;
    }

    /**
     * Set the input (template) encoding.
     * @param inputEncoding Input encoding
     */
    public void setInputEncoding(String inputEncoding) {
        this.inputEncoding = inputEncoding;
    }

    /**
     * Returns a writer, based on encoding and path.
     * @param path      path to the output file
     * @param encoding  output encoding
     */
    public Writer getWriter(String path, String encoding) throws Exception {
        Writer writer;
        if (encoding == null || encoding.length() == 0 || encoding.equals("8859-1") || encoding.equals("8859_1")) {
            writer = new FileWriter(path);
        } else {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), encoding));
        }
        return writer;
    }

    /**
     * Returns a template, based on encoding and path.
     * @param templateName  name of the template
     * @param encoding      template encoding
     */
    public Template getTemplate(String templateName, String encoding) throws Exception {
        Template template;
        if (encoding == null || encoding.length() == 0 || encoding.equals("8859-1") || encoding.equals("8859_1")) {
            template = Velocity.getTemplate(templateName);
        } else {
            template = Velocity.getTemplate(templateName, encoding);
        }
        return template;
    }

    public List<String> getParseFileNameList() {
        return parseFileNameList;
    }

    public void setParseFileNameList(List<String> parseFileNameList) {
        this.parseFileNameList = parseFileNameList;
    }

    public List<String> getSkipFileNameList() {
        return skipFileNameList;
    }

    public void setSkipFileNameList(List<String> skipFileNameList) {
        this.skipFileNameList = skipFileNameList;
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
        return "outputEncoding=" + outputEncoding + ", inputEncoding=" + inputEncoding + " skipFileNameList="
                + skipFileNameList;
    }
}
