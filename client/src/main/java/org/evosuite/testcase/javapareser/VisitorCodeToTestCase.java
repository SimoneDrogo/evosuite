package org.evosuite.testcase.javapareser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.AssertStmt;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.symbolic.vm.math.EXP;
import org.evosuite.testcase.statements.*;


public class VisitorCodeToTestCase {
	
	private static final String FILE_PATH = "src/main/java/org/evosuite/samples/ProvaVuota.java";
	
	
	
	private static class ArrayCreationLevelVisitor extends VoidVisitorAdapter <TestCaseBuilder> {
		
		@Override
		public void visit(ArrayCreationLevel acl, TestCaseBuilder builder) {
			
			String lunghezza = acl.getDimension().get().toString();
			
			
			System.out.println(lunghezza);
			
		}
		
	}
	

	
	
	
	private static class ArrayCreationExprVisitor extends VoidVisitorAdapter <TestCaseBuilder> {
		
		@Override
		public void visit(ArrayCreationExpr ace, TestCaseBuilder builder) {

		    // Prima otteniamo il numero di livelli (dimensioni dell'array)
		    int dimensions = ace.getLevels().size();

		    // Ora risolviamo l'element type
		    ResolvedType resolvedType = ace.getElementType().resolve();

		    java.lang.reflect.Type typeForEvoSuite = null;

		    try {
		        typeForEvoSuite = ResolvedTypeToReflectTypeConverter.toReflectType(resolvedType);
		    } catch (ClassNotFoundException e) {
		        e.printStackTrace();
		        typeForEvoSuite = Object.class; // fallback
		    }

		    // Ora costruiamo il tipo completo di array riflessivo
		    for (int i = 0; i < dimensions; i++) {
		        typeForEvoSuite = java.lang.reflect.Array.newInstance((Class<?>)typeForEvoSuite, 0).getClass();
		    }
		    
		    

		    // Ora hai il tipo completo (es: int[][].class)
		    builder.appendArrayStmt(typeForEvoSuite, dimensions);
		    
		    ArrayCreationLevelVisitor creationVisitor = new ArrayCreationLevelVisitor();
		    
		    creationVisitor.visit(ace, builder);
		}

	}

	
	private static class VariableDeclaratorVisitor extends VoidVisitorAdapter <TestCaseBuilder> {

		  @Override
		  public void visit(VariableDeclarator vd, TestCaseBuilder builder) {
				super.visit(vd, builder);
				

		        if (vd.getInitializer().isPresent()) {
		        	
		        	Expression expr = vd.getInitializer().get();
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
		            	builder.appendBytePrimitive(byteValue);  //stesso problema del float parsa sia il byte che l'int
		            }
		            
		            if (expr.isClassExpr()) {
		            	

		            	String qualifiedName = expr.asClassExpr().getType().resolve().describe();
		            	

		            	// ora puoi fare
		            	Class<?> clazz = null;
						try {
							clazz = Class.forName(qualifiedName);
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		            	
		            	builder.appendClassPrimitive(clazz);
		            	
		            }
		            
		            if (expr.isNullLiteralExpr()) {
		                ResolvedType resolvedType = vd.getType().resolve();

		                java.lang.reflect.Type typeForEvoSuite = null;
		                
		                try {
		                    typeForEvoSuite = ResolvedTypeToReflectTypeConverter.toReflectType(resolvedType);
		                } catch (ClassNotFoundException e) {
		                    e.printStackTrace();
		                    typeForEvoSuite = Object.class; // fallback
		                }

		                builder.appendNull(typeForEvoSuite);
		            }

		            
		            
		            
					
			  }
		        
		      else {
		    	  
		    	  
		    	  String type = vd.getType().toString();

		    	    switch (type) {
		    	        case "int":
		    	            builder.appendIntPrimitive(0);
		    	            break;
		    	        case "boolean":
		    	            builder.appendBooleanPrimitive(false);
		    	            break;
		    	        case "char":
		    	            builder.appendCharPrimitive('\0');
		    	            break;
		    	        case "String":
		    	            builder.appendStringPrimitive(null);
		    	            break;
		    	        case "float":
		    	            builder.appendFloatPrimitive(0.0f);
		    	            break;
		    	        case "double":
		    	            builder.appendDoublePrimitive(0.0);
		    	            break;
		    	        case "byte":
		    	            builder.appendBytePrimitive((byte) 0);
		    	            break;
		    	        default:
		    	            System.out.println("Tipo non gestito: " + type);
		    	            break;
		    	    }
		    	  
		    	  
		    	  
		      }
		 }
	}
	
	
	private static class EnumVisitor extends VoidVisitorAdapter  <TestCaseBuilder> {
		
		@Override
		public void visit (EnumDeclaration ed, TestCaseBuilder builder) {
			
			
			
		}
	}
		
	
	
	private static class MethodVisitor extends VoidVisitorAdapter<List<TestCase>> {

		  @Override
		  public void visit(MethodDeclaration md, List<TestCase> collector) {
				super.visit(md, collector);
				
				VariableDeclaratorVisitor variableVisitor = new VariableDeclaratorVisitor();
				ArrayCreationExprVisitor arrayVisitor = new ArrayCreationExprVisitor();
				
			    
			    DefaultTestCase testCase = new DefaultTestCase();
			    TestCaseBuilder builder = new TestCaseBuilder(testCase, 0);

				variableVisitor.visit(md, builder);
				arrayVisitor.visit(md, builder);
				
				collector.add(testCase);
		  }
	 }
	
	
	
	
	public static void main(String[] args) throws Exception {

	    // 1. Configurazione PRIMA di ogni parsing
	    CombinedTypeSolver typeSolver = new CombinedTypeSolver();
	    typeSolver.add(new ReflectionTypeSolver());

	    JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
	    
	    ParserConfiguration parserConfiguration = new ParserConfiguration();
	    parserConfiguration.setSymbolResolver(symbolSolver);
	    
	    StaticJavaParser.setConfiguration(parserConfiguration);
	    
	    // 2. Ora fai il parse: a questo punto ogni nodo avrà il resolver
	    CompilationUnit cu = StaticJavaParser.parse(Files.newInputStream(Paths.get(FILE_PATH)));

	    // 3. Ora puoi passare cu ai tuoi visitor, e il resolve() funzionerà dentro il visitor
	    List<TestCase> evoSuiteTestCases = new ArrayList<>();
	    MethodVisitor visitor = new MethodVisitor();
	    visitor.visit(cu, evoSuiteTestCases);
	    
	    System.out.println(evoSuiteTestCases);
	}

}
		  
		  
		  
		  
		  
	
		  
		 
	

