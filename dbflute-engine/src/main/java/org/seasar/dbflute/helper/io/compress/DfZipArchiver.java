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
package org.seasar.dbflute.helper.io.compress;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.9.9.7A (2012/07/15 Sunday)
 */
public class DfZipArchiver {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final File _zipFile;
    protected boolean _suppressCompressSubDir;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public DfZipArchiver(File zipFile) {
        _zipFile = zipFile;
    }

    // ===================================================================================
    //                                                                            Compress
    //                                                                            ========
    /**
     * Compress the directory's elements to archive file.
     * @param baseDir The base directory to compress. (NotNull)
     * @param filter The file filter, which doesn't need to accept the base directory. (NotNull)
     */
    public void compress(File baseDir, FileFilter filter) {
        if (baseDir == null) {
            String msg = "The argument 'baseDir' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!baseDir.isDirectory()) {
            String msg = "The baseDir should be directory but not: " + baseDir;
            throw new IllegalArgumentException(msg);
        }
        if (!baseDir.exists()) {
            String msg = "Not found the baseDir in the file system: " + baseDir;
            throw new IllegalArgumentException(msg);
        }
        OutputStream out = null;
        ZipArchiveOutputStream archive = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(_zipFile));
            archive = new ZipArchiveOutputStream(out);
            archive.setEncoding("UTF-8");

            addAll(archive, baseDir, baseDir, filter);

            archive.finish();
            archive.flush();
            out.flush();
        } catch (IOException e) {
            String msg = "Failed to compress the files to " + _zipFile.getPath();
            throw new IllegalStateException(msg, e);
        } finally {
            if (archive != null) {
                try {
                    archive.close();
                } catch (IOException ignored) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    protected void addAll(ArchiveOutputStream archive, File topDir, File targetFile, FileFilter filter)
            throws IOException {
        if (_suppressCompressSubDir && isSubDir(topDir, targetFile)) {
            return; // sub directory
        }
        if (!isTopDir(topDir, targetFile) && !filter.accept(targetFile)) {
            return; // not top directory and cannot accept it
        }
        if (targetFile.isDirectory()) {
            final File[] listFiles = targetFile.listFiles();
            if (listFiles == null || listFiles.length == 0) {
                addDir(archive, topDir, targetFile);
            } else {
                for (File elementFile : listFiles) {
                    addAll(archive, topDir, elementFile, filter);
                }
            }
        } else {
            addFile(archive, topDir, targetFile);
        }
    }

    protected boolean isSubDir(File topDir, File targetFile) {
        return targetFile.isDirectory() && !topDir.equals(targetFile);
    }

    protected boolean isTopDir(File topDir, File targetFile) {
        return targetFile.isDirectory() && topDir.equals(targetFile);
    }

    protected void addDir(ArchiveOutputStream archive, File topDir, File targetDir) throws IOException {
        final String name = buildEntryName(topDir, targetDir, true);
        archive.putArchiveEntry(new ZipArchiveEntry(name));
        archive.closeArchiveEntry();
    }

    protected void addFile(ArchiveOutputStream archive, File topDir, File targetFile) throws IOException {
        final String name = buildEntryName(topDir, targetFile, false);
        archive.putArchiveEntry(new ZipArchiveEntry(name));
        FileInputStream ins = null;
        try {
            ins = new FileInputStream(targetFile);
            IOUtils.copy(ins, archive);
        } finally {
            if (ins != null) {
                try {
                    ins.close();
                } catch (IOException ignored) {
                }
            }
        }
        archive.closeArchiveEntry();
    }

    protected String buildEntryName(File topDir, File targetFile, boolean dir) {
        final String path = resolveAbsolutePath(targetFile);
        String name = path.substring(resolveAbsolutePath(topDir).length());
        if (name.startsWith("/")) {
            name = name.substring("/".length());
        }
        if (dir) {
            name = name + "/";
        }
        return name;
    }

    // ===================================================================================
    //                                                                             Extract
    //                                                                             =======
    /**
     * Extract the archive file to the directory.
     * @param baseDir The base directory to compress. (NotNull)
     * @param filter The file filter, which doesn't need to accept the base directory. (NotNull)
     */
    public void extract(File baseDir, FileFilter filter) {
        if (baseDir == null) {
            String msg = "The argument 'baseDir' should not be null.";
            throw new IllegalArgumentException(msg);
        }
        if (baseDir.exists() && !baseDir.isDirectory()) {
            String msg = "The baseDir should be directory but not: " + baseDir;
            throw new IllegalArgumentException(msg);
        }
        baseDir.mkdirs();
        final String baseDirPath = resolvePath(baseDir);
        InputStream ins = null;
        ZipArchiveInputStream archive = null;
        try {
            ins = new FileInputStream(_zipFile);
            archive = new ZipArchiveInputStream(ins, "UTF-8", true);
            ZipArchiveEntry entry;
            while ((entry = archive.getNextZipEntry()) != null) {
                final String entryName = resolveFileSeparator(entry.getName()); // just in case
                final File file = new File(baseDirPath + "/" + entryName);
                if (!filter.accept(file)) {
                    continue;
                }
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    OutputStream out = null;
                    try {
                        out = new FileOutputStream(file);
                        IOUtils.copy(archive, out);
                        out.close();
                    } catch (IOException e) {
                        String msg = "Failed to IO-copy the file: " + file.getPath();
                        throw new IllegalStateException(msg, e);
                    } finally {
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException ignored) {
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            String msg = "Failed to extract the files from " + _zipFile.getPath();
            throw new IllegalArgumentException(msg, e);
        } finally {
            if (archive != null) {
                try {
                    archive.close();
                } catch (IOException ignored) {
                }
            }
            if (ins != null) {
                try {
                    ins.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected String resolvePath(File file) {
        return resolveFileSeparator(file.getPath());
    }

    protected String resolveAbsolutePath(File file) {
        return resolveFileSeparator(file.getAbsolutePath());
    }

    protected String resolveFileSeparator(String path) {
        return Srl.replace(path, "\\", "/");
    }

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    public DfZipArchiver suppressCompressSubDir() {
        _suppressCompressSubDir = true;
        return this;
    }
}
