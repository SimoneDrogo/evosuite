package org.evosuite.testcase.javaparser;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.testcase.TestCase;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;

public class VisitorContext {
    
    private  TestCaseBuilder builder;
    private  VariabiliTracker tracker;
    private String variableName;

    private List<TestCase> evoSuiteTestCases;
    

    public VisitorContext(List<TestCase> evoSuiteTestCases) {
 
    	this.evoSuiteTestCases = evoSuiteTestCases;
        //this.globalContext = globalContext;
        
    }
    
    
	public void add (TestCase tcas) {
	    	
	    	this.evoSuiteTestCases.add(tcas);
	}
	
	
	public List<TestCase> getTestCases(){
	    	
	    	return this.evoSuiteTestCases;
	    }

    
    public TestCaseBuilder getBuilder() {
        return builder;
    }
    
    public void setBuilder (TestCaseBuilder builder){
    	
    	this.builder = builder;
    }

    public VariabiliTracker getTracker() {
        return tracker;
    }
    
    public void setTracker(VariabiliTracker tracker){
    	
    	this.tracker = tracker;
    	
    	
    }
    
    public String getVariableName() {
    	
    	return this.variableName;
    }
    
    public void setVariableName(String name) {
    	
    	this.variableName = name;
    	
    }
    

}

