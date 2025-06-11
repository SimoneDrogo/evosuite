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
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.AssertStmt;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import org.evosuite.symbolic.TestCaseBuilder;
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
		  
		  VoidVisitor<List<VariableDeclarator>> variableCollector = new VariableDeclaratorCollector();
		  
		  List<TestCase> evoTestCases = new ArrayList<>();
		  
		  for (MethodDeclaration md : methods) {
			    
			    List<VariableDeclarator> declarations = new ArrayList<>();
			    variableCollector.visit(md, declarations);

			    // Creo un nuovo TestCase per ogni metodo (poi puoi decidere come modularlo)
			    DefaultTestCase testCase = new DefaultTestCase();
			    TestCaseBuilder builder = new TestCaseBuilder(testCase, 0);

			    for (VariableDeclarator vd : declarations) {
			        
			        Expression expr = vd.getInitializer().get();
			        
			        // Verifica che abbia un inizializzatore (per ora gestiamo solo variabili inizializzate)
			        if (vd.getInitializer().isPresent()) {
			            String initializer = expr.toString();
			            
			            if (expr.isIntegerLiteralExpr()) {
			                
			                try {
			                    int intValue = Integer.parseInt(initializer);	                	
			                    builder.appendIntPrimitive(intValue);
			                } catch (NumberFormatException e) {
			                    System.out.println("Valore int non parsabile: " + initializer);
			                }
			            }
			            
			            if (expr.isBooleanLiteralExpr()) {
			            	boolean boolValue = Boolean.parseBoolean(initializer);
			                builder.appendBooleanPrimitive(boolValue);
			            }
			            
			            
			            if (expr.isCharLiteralExpr()) {
			            	
			            	char charValue = initializer.charAt(1);
			            	builder.appendCharPrimitive(charValue);
			            	
			            }
			            
			            if (expr.isStringLiteralExpr()) {
			            	builder.appendStringPrimitive(initializer.substring(1, initializer.length() -1));
			            }
			            
			            if (vd.getType().toString().equals("float")) {
			            	
			            	float floatValue = Float.parseFloat(initializer);
			            	builder.appendFloatPrimitive(floatValue);  //capire perchè lo fa doppio (parsa sia il float che il doouble o l'int)
			            	
			            }
			            
			            if (expr.isDoubleLiteralExpr()){
			            	double doubleValue = Double.parseDouble(initializer);
			            	builder.appendDoublePrimitive(doubleValue);
			            }
			            
			            if (vd.getType().toString().equals("byte")) {
			            	
			            	byte byteValue = Byte.parseByte(initializer);
			            	builder.appendBytePrimitive(byteValue);  //stesso problema del float parsa sia il double che l'int
			            }
			            
			            if (expr.isClassExpr()) {
			            	
			            	Class<?> classValue = expr.getClass();
			            	builder.appendClassPrimitive(classValue);
			            }
			            
			            
			            
			            
			        }
			        
			    }

			    evoTestCases.add(testCase);
			    System.out.println("Creato TestCase: \n" + testCase);
			}

			System.out.print(evoTestCases);
	}
	
}

