package org.evosuite.testcase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.stmt.AssertStmt;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import org.evosuite.testcase.statements.*;


public class VisitorCodeToTestCase {
	
	private static final String FILE_PATH = "src/main/java/org/evosuite/samples/ProvaVuota.java";
	
	
	
	private static class MethodCollector extends VoidVisitorAdapter<List<MethodDeclaration>> {

		  @Override
		  public void visit(MethodDeclaration md, List<MethodDeclaration> collector) {
				super.visit(md, collector);
				collector.add(md);
		  }
	 }
	
	
	private static class VariableDeclaratorCollector extends VoidVisitorAdapter<List<VariableDeclarator>> {

		  @Override
		  public void visit(VariableDeclarator vd, List<VariableDeclarator> collector) {
				super.visit(vd, collector);
				collector.add(vd);
		  }
	 }
	
	public static void main(String[] args) throws Exception {
		
		  CompilationUnit cu = StaticJavaParser.parse(Files.newInputStream(Paths.get(FILE_PATH)));
		  
		  List<MethodDeclaration> methods = new ArrayList<>();
		  
		  VoidVisitor<List<MethodDeclaration>> methodNameCollector = new MethodCollector();
		  methodNameCollector.visit(cu, methods);
		  
		  List<TestCase> evoTestCases = new ArrayList<>();
		  
		  for (MethodDeclaration md : methods) {
			    
			  TestCase test = new DefaultTestCase();
			  
			  
			  
			  
			 
			  
			  
			  
			  evoTestCases.add(test);
			  
			  
			}
		  
		  
		 
		  
	  }

}
