/*
 * Copyright 2014-2020 the original author or authors.
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
package org.dbflute.logic.doc.supplement.author;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.function.Supplier;

import org.dbflute.optional.OptionalThing;

/**
 * @author cabos
 * @author deco
 * @author jflute
 */
public class DfDocumentAuthorLogic { // from DBFlute Intro

    private static final Supplier<String> _authorSupplier = new Supplier<String>() {

        private String _author;

        @Override
        public synchronized String get() {
            if (this._author == null) {
                this.loadAuthor();
            }
            return this._author;
        }

        private void loadAuthor() {
            String author = System.getProperty("user.name");
            if (author == null || author.isEmpty()) {
                throw new IllegalStateException("cannot load user name: " + author);
            }
            this._author = author;
        }
    };

    private static final Supplier<String> _gitBranchSupplier = new Supplier<String>() {

        private String _gitBranch;

        @Override
        public synchronized String get() {
            if (this._gitBranch == null) {
                this.loadGitBranch();
            }
            return this._gitBranch;
        }

        private void loadGitBranch() {
            // get branch from git
            final String command = "git symbolic-ref --short HEAD";
            final Runtime runtime = Runtime.getRuntime();
            final Process process;
            try {
                process = runtime.exec(command);
            } catch (IOException e) {
                throw new IllegalStateException("cannot execute git command: " + command, e);
            }

            // read branch
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName("UTF-8")))) {
                this._gitBranch = reader.readLine();
            } catch (IOException e) {
                throw new IllegalStateException("fail to read execute command result: " + command, e);
            }
        }
    };

    public String getAuthor() {
        return _authorSupplier.get();
    }

    public OptionalThing<String> getGitBranch() {
        try {
            return OptionalThing.ofNullable(_gitBranchSupplier.get(), () -> {
                throw new IllegalStateException("Cannot get branch name.");
            });
        } catch (IllegalStateException e) {
            return OptionalThing.ofNullable(null, () -> {
                throw e;
            });
        }
    }
}
