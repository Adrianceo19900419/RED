/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jface.text.rules.IToken;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class ExecutableCallInSettingsRule extends ExecutableCallRule {

    public static ExecutableCallRule forExecutableInTestSetupOrTeardown(final IToken textToken,
            final IToken gherkinToken, final IToken quoteToken, final IToken embeddedVariablesToken) {
        return new ExecutableCallInSettingsRule(textToken, gherkinToken, quoteToken, embeddedVariablesToken,
                EnumSet.of(RobotTokenType.TEST_CASE_SETTING_SETUP_KEYWORD_NAME,
                        RobotTokenType.TEST_CASE_SETTING_SETUP_KEYWORD_ARGUMENT,
                        RobotTokenType.TEST_CASE_SETTING_TEARDOWN_KEYWORD_NAME,
                        RobotTokenType.TEST_CASE_SETTING_TEARDOWN_KEYWORD_ARGUMENT),

                elem -> elem.getTypes().contains(RobotTokenType.TEST_CASE_SETTING_SETUP)
                        || elem.getTypes().contains(RobotTokenType.TEST_CASE_SETTING_TEARDOWN),

                elem -> elem.getTypes().contains(RobotTokenType.TEST_CASE_SETTING_SETUP_KEYWORD_NAME)
                        || elem.getTypes().contains(RobotTokenType.TEST_CASE_SETTING_TEARDOWN_KEYWORD_NAME));
    }

    public static ExecutableCallRule forExecutableInKeywordTeardown(final IToken textToken, final IToken gherkinToken,
            final IToken quoteToken, final IToken embeddedVariablesToken) {
        return new ExecutableCallInSettingsRule(textToken, gherkinToken, quoteToken, embeddedVariablesToken,
                EnumSet.of(RobotTokenType.KEYWORD_SETTING_TEARDOWN_KEYWORD_NAME,
                        RobotTokenType.KEYWORD_SETTING_TEARDOWN_KEYWORD_ARGUMENT),

                elem -> elem.getTypes().contains(RobotTokenType.KEYWORD_SETTING_TEARDOWN),

                elem -> elem.getTypes().contains(RobotTokenType.KEYWORD_SETTING_TEARDOWN_KEYWORD_NAME));
    }

    public static ExecutableCallRule forExecutableInGeneralSettingsSetupsOrTeardowns(final IToken textToken,
            final IToken gherkinToken, final IToken quoteToken, final IToken embeddedVariablesToken) {
        return new ExecutableCallInSettingsRule(textToken, gherkinToken, quoteToken, embeddedVariablesToken,
                EnumSet.of(RobotTokenType.SETTING_SUITE_SETUP_KEYWORD_NAME,
                        RobotTokenType.SETTING_SUITE_SETUP_KEYWORD_ARGUMENT,
                        RobotTokenType.SETTING_SUITE_TEARDOWN_KEYWORD_NAME,
                        RobotTokenType.SETTING_SUITE_TEARDOWN_KEYWORD_ARGUMENT,
                        RobotTokenType.SETTING_TEST_SETUP_KEYWORD_NAME,
                        RobotTokenType.SETTING_TEST_SETUP_KEYWORD_ARGUMENT,
                        RobotTokenType.SETTING_TEST_TEARDOWN_KEYWORD_NAME,
                        RobotTokenType.SETTING_TEST_TEARDOWN_KEYWORD_ARGUMENT),

                elem -> elem.getTypes().contains(RobotTokenType.SETTING_SUITE_SETUP_DECLARATION)
                        || elem.getTypes().contains(RobotTokenType.SETTING_SUITE_TEARDOWN_DECLARATION)
                        || elem.getTypes().contains(RobotTokenType.SETTING_TEST_SETUP_DECLARATION)
                        || elem.getTypes().contains(RobotTokenType.SETTING_TEST_TEARDOWN_DECLARATION),

                elem -> elem.getTypes().contains(RobotTokenType.SETTING_SUITE_SETUP_KEYWORD_NAME)
                        || elem.getTypes().contains(RobotTokenType.SETTING_SUITE_TEARDOWN_KEYWORD_NAME)
                        || elem.getTypes().contains(RobotTokenType.SETTING_TEST_SETUP_KEYWORD_NAME)
                        || elem.getTypes().contains(RobotTokenType.SETTING_TEST_TEARDOWN_KEYWORD_NAME));
    }

    private final Predicate<IRobotLineElement> isKeywordName;

    protected ExecutableCallInSettingsRule(final IToken textToken, final IToken gherkinToken, final IToken quoteToken,
            final IToken embeddedVariablesToken, final EnumSet<RobotTokenType> acceptableTypes,
            final Predicate<IRobotLineElement> shouldStopOnElement, final Predicate<IRobotLineElement> isKeywordName) {
        super(textToken, gherkinToken, quoteToken, embeddedVariablesToken, acceptableTypes, shouldStopOnElement);
        this.isKeywordName = isKeywordName;
    }

    @Override
    protected boolean shouldBeColored(final IRobotLineElement token, final List<RobotLine> context,
            final Predicate<IRobotLineElement> shouldStopOnElement) {
        final List<RobotToken> tokensBefore = getPreviousTokensInThisExecutable(token, context, shouldStopOnElement);
        return (tokensBefore.isEmpty() && isKeywordName.test(token)) || isNestedKeyword(token, context, tokensBefore);
    }
}
