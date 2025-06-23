package org.evosuite.testcase.javapareser;

import com.github.javaparser.ast.body.MethodDeclaration;

public class VisitorContext {
    private final MethodDeclaration methodUnderTest;
    private final TestCaseBuilder builder;
    private final VariabiliTracker tracker;

    public VisitorContext(MethodDeclaration methodUnderTest, TestCaseBuilder builder) {
        this.methodUnderTest = methodUnderTest;
        this.builder = builder;
        this.tracker = new VariabiliTracker();
    }

    public MethodDeclaration getMethodUnderTest() {
        return methodUnderTest;
    }

    public TestCaseBuilder getBuilder() {
        return builder;
    }

    public VariabiliTracker getTracker() {
        return tracker;
    }
}

