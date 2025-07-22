package org.evosuite.testcase.javaparser;

import java.util.List;

import org.evosuite.testcase.TestCase;

import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;

public class GlobalContext {
	
	
	private List<TestCase> evoSuiteTestCases;
    private CombinedTypeSolver typeSolver;
    
    
    public GlobalContext (List<TestCase> evoSuiteTestCases, CombinedTypeSolver typeSolver){
    	
    	this.evoSuiteTestCases = evoSuiteTestCases;
    	this.typeSolver = typeSolver;
    	
    }
    
    public void add (TestCase tcas) {
    	
    	this.evoSuiteTestCases.add(tcas);
    }
    
    
    public CombinedTypeSolver getTypeSolver() {
    	
    	return this.typeSolver;
    }
    
    public List<TestCase> getTestCases(){
    	
    	return this.evoSuiteTestCases;
    }
    
    

}
