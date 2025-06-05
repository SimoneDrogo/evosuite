package org.evosuite.testcase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.AssertStmt;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;


public class VisitorCodeToTestCase {
	
	private static final String FILE_PATH = "";
	
	private  CompilationUnit cu = null;
	
	
	public void init() {
		
		  try {
			cu = StaticJavaParser.parse(Files.newInputStream(Paths.get(FILE_PATH)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}

	
	
	private static class AssertNameCollector extends VoidVisitorAdapter<List<AssertStmt>> {

		  @Override
		  public void visit(AssertStmt as, List<AssertStmt> collector) {
				super.visit(as, collector);
				collector.add(as);
		    }
		  }

}
