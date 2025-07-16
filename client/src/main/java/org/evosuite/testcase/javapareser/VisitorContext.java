package org.evosuite.testcase.javapareser;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.testcase.TestCase;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;

public class VisitorContext {
    private final MethodDeclaration methodUnderTest;
    private final TestCaseBuilder builder;
    private final VariabiliTracker tracker;
    private String variableName;
    private GlobalContext globalContext;
    

    public VisitorContext(MethodDeclaration methodUnderTest, TestCaseBuilder builder, GlobalContext globalContext) {
        this.methodUnderTest = methodUnderTest;
        this.builder = builder;
        this.tracker = new VariabiliTracker();
        this.globalContext = globalContext;
        
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
    
    public String getVariableName() {
    	
    	return this.variableName;
    }
    
    public void setVariableName(String name) {
    	
    	this.variableName = name;
    	
    }
    
    public GlobalContext getGlobalContext() {
    	
    	return this.globalContext;
    }
}

