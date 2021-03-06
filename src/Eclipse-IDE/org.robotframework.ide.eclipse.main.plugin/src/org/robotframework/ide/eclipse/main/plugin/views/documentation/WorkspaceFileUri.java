/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation;

import static com.google.common.collect.Lists.newArrayList;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.DocumentationsLinksSupport.OpenableUri;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.DocumentationsLinksSupport.UnableToOpenUriException;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;

public class WorkspaceFileUri implements OpenableUri {

    private static final String SCHEME = "file";
    private static final String SHOW_DOC_PARAM = "show_doc";
    private static final String SHOW_SRC_PARAM = "show_source";
    static final String SUITE_PARAM = "suite";
    static final String KEYWORD_PARAM = "keyword";
    static final String TEST_PARAM = "test";

    public static boolean isFileUri(final URI uri) {
        return uri.getScheme().equals(SCHEME) && uri.getQuery() == null;
    }

    public static boolean isFileDocUri(final URI uri) {
        return uri.getScheme().equals(SCHEME) && queryContainsParams(uri.getQuery(), SHOW_DOC_PARAM);
    }

    public static boolean isFileSrcUri(final URI uri) {
        return uri.getScheme().equals(SCHEME) && queryContainsParams(uri.getQuery(), SHOW_SRC_PARAM);
    }

    public static URI createFileUri(final IFile file) {
        return getFileUri(file);
    }

    public static URI createShowSuiteSourceUri(final IFile file) throws URISyntaxException {
        final Map<String, String> values = ImmutableMap.of(SHOW_SRC_PARAM, Boolean.toString(true), SUITE_PARAM, "");
        return createUri(getFileUri(file), values);
    }

    public static URI createShowSuiteDocUri(final IFile file) throws URISyntaxException {
        final Map<String, String> values = ImmutableMap.of(SHOW_DOC_PARAM, Boolean.toString(true), SUITE_PARAM, "");
        return createUri(getFileUri(file), values);
    }

    public static URI createShowKeywordSourceUri(final IFile file, final String keywordName) throws URISyntaxException {
        final Map<String, String> values = ImmutableMap.of(SHOW_SRC_PARAM, Boolean.toString(true), KEYWORD_PARAM,
                keywordName);
        return createUri(getFileUri(file), values);
    }

    public static URI createShowKeywordDocUri(final IFile file, final String keywordName) throws URISyntaxException {
        final Map<String, String> values = ImmutableMap.of(SHOW_DOC_PARAM, Boolean.toString(true), KEYWORD_PARAM,
                keywordName);
        return createUri(getFileUri(file), values);
    }

    public static URI createShowCaseSourceUri(final IFile file, final String testName) throws URISyntaxException {
        final Map<String, String> values = ImmutableMap.of(SHOW_SRC_PARAM, Boolean.toString(true), TEST_PARAM,
                testName);
        return createUri(getFileUri(file), values);
    }

    public static URI createShowCaseDocUri(final IFile file, final String testName) throws URISyntaxException {
        final Map<String, String> values = ImmutableMap.of(SHOW_DOC_PARAM, Boolean.toString(true), TEST_PARAM,
                testName);
        return createUri(getFileUri(file), values);
    }

    private static URI getFileUri(final IFile file) {
        return RedWorkspace.tryToGetLocalUri(file);
    }

    private static URI createUri(final URI uri, final Map<String, String> values) throws URISyntaxException {
        final String query = Joiner.on('&').withKeyValueSeparator('=').join(values);
        return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), query, uri.getFragment());
    }

    private static boolean queryContainsParams(final String query, final String... expectedParams) {
        final Map<String, String> paramsMap = extractParams(query);
        return paramsMap.keySet().containsAll(newArrayList(expectedParams));
    }

    private static Map<String, String> extractParams(final String query) {
        if (query == null) {
            return ImmutableMap.of();
        }
        return Splitter.on('&').trimResults().withKeyValueSeparator("=").split(query);
    }

    private final URI uri;

    private final FileConsumer fileConsumer;

    WorkspaceFileUri(final URI uri, final FileConsumer fileConsumer) {
        this.uri = uri;
        this.fileConsumer = fileConsumer;
    }

    @Override
    public void open() {
        try {
            // this is a copy of original uri with stripped query part; the query part
            // was causing the file not to be found under linux-based systems
            final URI fileUri = new URI("file", null, uri.getPath(), null, null);

            final Optional<IFile> file = new RedWorkspace().fileForUri(fileUri);

            fileConsumer.accept(file, extractParams(uri.getQuery()));
        } catch (final URISyntaxException e) {
            throw new UnableToOpenUriException("Cannot open uri: " + uri, e);
        }
    }

    @FunctionalInterface
    public static interface FileConsumer {

        public void accept(Optional<IFile> file, Map<String, String> params);
    }
}
