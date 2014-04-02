/*
 * Copyright (C) 2014 Sebastien Diot.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blockwithme.apiparser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaConstructor;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaGenericDeclaration;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.JavaSource;
import com.thoughtworks.qdox.model.JavaType;
import com.thoughtworks.qdox.model.JavaTypeVariable;
import com.thoughtworks.qdox.model.impl.DefaultJavaType;

/**
 * Parses Java *source code* to extract the public API.
 *
 * @author monster
 */
public class APIParser {
    private static class Helper extends DefaultJavaType {
        /**  */
        private static final long serialVersionUID = 1L;

        private Helper() {
            super("");
        }

        public static <D extends JavaGenericDeclaration> JavaTypeVariable<D> resolve(
                final JavaType base,
                final List<JavaTypeVariable<D>> typeParameters) {
            return DefaultJavaType.resolve(base, typeParameters);
        }
    }

    private static String toString(final StringBuilder result,
            final JavaConstructor member) {
        result.setLength(0);
        result.append(member.getDeclaringClass().getFullyQualifiedName());
        result.append(".<init>(");
        String sep = "";
        for (final JavaParameter p : member.getParameters()) {
            result.append(sep);
            sep = ",";
            final JavaType originalType = p.getType();
            final JavaTypeVariable<?> typeVariable = Helper.resolve(
                    originalType, member.getTypeParameters());
            if (typeVariable == null) {
                result.append(originalType.getFullyQualifiedName());
            } else {
                final List<JavaType> bounds = typeVariable.getBounds();
                if ((bounds == null) || bounds.isEmpty()) {
                    result.append(originalType.getFullyQualifiedName());
                } else {
                    result.append(bounds.get(0).getFullyQualifiedName());
                }
            }
        }
        result.append(")");
        return result.toString();
    }

    private static String toString(final StringBuilder result,
            final JavaMethod member) {
        result.setLength(0);
        result.append(member.getDeclaringClass().getFullyQualifiedName());
        result.append(".");
        result.append(member.getName());
        result.append("(");
        String sep = "";
        for (final JavaParameter p : member.getParameters()) {
            result.append(sep);
            sep = ",";
            final JavaType originalType = p.getType();
            final JavaTypeVariable<?> typeVariable = Helper.resolve(
                    originalType, member.getTypeParameters());
            if (typeVariable == null) {
                result.append(originalType.getFullyQualifiedName());
            } else {
                final List<JavaType> bounds = typeVariable.getBounds();
                if ((bounds == null) || bounds.isEmpty()) {
                    result.append(originalType.getFullyQualifiedName());
                } else {
                    result.append(bounds.get(0).getFullyQualifiedName());
                }
            }
        }
        result.append("): ");
        result.append(member.getReturns().getFullyQualifiedName());
        return result.toString();
    }

    private static String toString(final StringBuilder result,
            final JavaField member) {
        result.setLength(0);
        result.append(member.getDeclaringClass().getFullyQualifiedName());
        result.append(".");
        result.append(member.getName());
        result.append(": ");
        result.append(member.getType().getFullyQualifiedName());
        return result.toString();
    }

    /** Parse the given directories. */
    private static void parse(final List<File> directories) {
        final JavaProjectBuilder builder = new JavaProjectBuilder();
        for (final File dir : directories) {
            builder.addSourceTree(dir);
        }
        final List<String> result = new ArrayList<>(1024 * 1024);
        final StringBuilder buf = new StringBuilder();
        for (final JavaSource source : builder.getSources()) {
            for (final JavaClass clazz : source.getClasses()) {
                for (final JavaField member : clazz.getFields()) {
                    if (member.isPublic()) {
                        result.add(toString(buf, member));
                    }
                }
                for (final JavaConstructor member : clazz.getConstructors()) {
                    if (member.isPublic()) {
                        result.add(toString(buf, member));
                    }
                }
                for (final JavaMethod member : clazz.getMethods()) {
                    if (member.isPublic()) {
                        result.add(toString(buf, member));
                    }
                }
            }
        }
        Collections.sort(result);
        for (final String s : result) {
            System.out.println(s);
        }
    }

    /** Main */
    public static void main(final String[] args) {
        final List<File> directories = new ArrayList<>();
        for (final String arg : args) {
            if ((arg != null) && !arg.isEmpty()) {
                final File dir = new File(arg);
                if (!dir.isDirectory()) {
                    throw new IllegalArgumentException("Not a directory: '"
                            + arg + "'");
                }
                if (!dir.canRead()) {
                    throw new IllegalArgumentException("Cannot read: '" + arg
                            + "'");
                }
                final File[] list = dir.listFiles();
                if (list.length == 0) {
                    throw new IllegalArgumentException("Empty: '" + arg + "'");
                }
                directories.add(dir);
            }
        }
        if (directories.isEmpty()) {
            throw new IllegalArgumentException("Nothing to do!");
        } else {
            parse(directories);
        }
    }
}
