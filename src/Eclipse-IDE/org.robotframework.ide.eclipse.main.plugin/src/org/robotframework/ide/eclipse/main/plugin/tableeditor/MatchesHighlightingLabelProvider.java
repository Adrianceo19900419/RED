/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import java.util.Collection;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.jface.viewers.StylersDisposingLabelProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment.MatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment.MatchesProvider;

import com.google.common.collect.Range;

public abstract class MatchesHighlightingLabelProvider extends StylersDisposingLabelProvider {

    private final MatchesProvider matchesProvider;

    private final Styler styler;

    public MatchesHighlightingLabelProvider(final MatchesProvider matchesProvider) {
        this.matchesProvider = matchesProvider;
        this.styler = Stylers.Common.MATCH_STYLER;
    }

    protected final StyledString highlightMatches(final StyledString label) {
        if (label == null) {
            return new StyledString();
        }
        return highlightMatches(label, 0, label.getString());
    }

    protected final StyledString highlightMatches(final StyledString label, final int shift, final String modelContent) {
        if (label == null) {
            return new StyledString();
        }
        final MatchesCollection matches = matchesProvider.getMatches();
        if (matches == null) {
            return label;
        }
        final Collection<Range<Integer>> ranges = matches.getRanges(modelContent);
        if (ranges == null) {
            return label;
        }
        for (final Range<Integer> range : ranges) {
            label.setStyle(range.lowerEndpoint() + shift, range.upperEndpoint() - range.lowerEndpoint(), styler);
        }
        return label;
    }
}
