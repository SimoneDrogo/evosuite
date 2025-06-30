package org.evosuite.testcase.javapareser;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.AssertStmt;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.symbolic.vm.math.EXP;
import org.evosuite.testcase.statements.*;
import org.evosuite.testcase.variable.ArrayReference;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.variable.VariableReferenceImpl;

public class VisitorCodeToTestCase {
	
	private static final String FILE_PATH = "src/main/java/org/evosuite/samples/ProvaVuota.java";
	
	
	public static void addJarsFromFolder(CombinedTypeSolver typeSolver, File jarFolder) {
        if (jarFolder.exists() && jarFolder.isDirectory()) {
            File[] jarFiles = jarFolder.listFiles((dir, name) -> name.endsWith(".jar"));
            if (jarFiles != null && jarFiles.length > 0) {
                for (File jar : jarFiles) {
                    try {
                        typeSolver.add(new JarTypeSolver(jar));
                        System.out.println("Added JAR: " + jar.getName());
                    } catch (Exception e) {
                        System.err.println("Failed to load JAR: " + jar.getName() + " - " + e.getMessage());
                    }
                }
            } else {
                System.out.println("No JAR files found in: " + jarFolder.getAbsolutePath());
            }
        } else {
            System.out.println("JAR folder does not exist or is not a directory: " + jarFolder.getAbsolutePath());
        }
    }
	
	
	private static class ObjecctCreationVisitor extends VoidVisitorAdapter <VisitorContext> {
		
		@Override
		public void visit (ObjectCreationExpr oc, VisitorContext context) {
			
			super.visit(oc, context);
			
			System.out.println("Sono arrivato al visit");
			
			NodeList<Expression> parameters = oc.getArguments();
			
			ResolvedConstructorDeclaration resolved = oc.resolve(); 
			
			String qualifiedName = oc.resolve().declaringType().getQualifiedName();
			
			VariableReference[] parametersVr = new VariableReference[resolved.getNumberOfParams()];
			
			Class<?> clazz = null;
			
			try {
				clazz = Class.forName(qualifiedName);

			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			List<Class<?>> paramTypes = new ArrayList<>();
			
			int i = 0;
			
			for (Expression parameter : parameters) {
				
				System.out.println("Sono arrivato al for");
				
				
				
				VariableReference vr = context.getTracker().getRefernce(parameter.asNameExpr().getNameAsString());			
				parametersVr[i] = vr;
				
				ResolvedType paramType = resolved.getParam(i).getType();
				String className = "";

				if (paramType.isPrimitive()) {
				    String simpleName = paramType.describe();
				    if (simpleName.equals("int")) {
				        className = "int";
				    } else if (simpleName.equals("boolean")) {
				        className = "boolean";
				    } else if (simpleName.equals("double")) {
				        className = "double";
				    } else if (simpleName.equals("long")) {
				        className = "long";
				    } else if (simpleName.equals("char")) {
				        className = "char";
				    } else if (simpleName.equals("float")) {
				        className = "float";
				    } else if (simpleName.equals("short")) {
				        className = "short";
				    } else if (simpleName.equals("byte")) {
				        className = "byte";
				    } else {
				        throw new IllegalArgumentException("Tipo primitivo non gestito: " + simpleName);
				    }
				} else {
				    className = paramType.asReferenceType().getQualifiedName();
				}
				
				Class<?> clazzParam = null;
				
				if (className.equals("int")) {
				    clazzParam = int.class;
				} else if (className.equals("boolean")) {
					clazzParam = boolean.class;
				} else if (className.equals("double")) {
					clazzParam = double.class;
				} else if (className.equals("long")) {
					clazzParam = long.class;
				} else if (className.equals("char")) {
					clazzParam = char.class;
				} else if (className.equals("float")) {
					clazzParam = float.class;
				} else if (className.equals("short")) {
					clazzParam = short.class;
				} else if (className.equals("byte")) {
					clazzParam = byte.class;
				} else {
				    try {
						clazzParam = Class.forName(className);
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				
				paramTypes.add(clazzParam);
			
				i++;
				
			}
			
			Constructor<?> constructor = null;
			
			try {
				constructor = clazz.getDeclaredConstructor(paramTypes.toArray(new Class<?>[0]));
			} catch (NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			VariableReference vr = context.getBuilder().appendConstructor(constructor, parametersVr);
			
			context.getTracker().aggiungiVariabile(context.getVariableName(),vr);
			
			
			
			
		}
	}
	

	private static class ArrayCreationLevelVisitor extends VoidVisitorAdapter <ArrayList<Integer>> {
	
		
		@Override
		public void visit(ArrayCreationLevel acl, ArrayList<Integer> lengths) {
			
			super.visit(acl, lengths);
			
			int length = Integer.parseInt(acl.getDimension().get().toString());
			
			
			lengths.add(length);
		
		}
		
	}
	

	
	
	
	private static class ArrayCreationExprVisitor extends VoidVisitorAdapter <VisitorContext> {
		
		@Override
		public void visit(ArrayCreationExpr ace, VisitorContext context) {
			
			super.visit(ace, context);

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
		    
		    ArrayList<Integer> lengths = new ArrayList();
		    
		    
		    ArrayCreationLevelVisitor creationVisitor = new ArrayCreationLevelVisitor();
		    
		    creationVisitor.visit(ace, lengths);
		    
		    Object[] arrayOggetti = lengths.toArray();
		    
		    int[] arrayPrimitivi = new int[arrayOggetti.length];
		    for (int i = 0; i < arrayOggetti.length; i++) {
		        arrayPrimitivi[i] = Integer.parseInt(arrayOggetti[i].toString());
		    }
		    
		    ArrayReference ar = context.getBuilder().appendArrayStmt(typeForEvoSuite, arrayPrimitivi);
		    
		    String name = ar.getName();	
		    
		    context.getTracker().aggiungiVariabile(name, ar);
		    
		}

	}

	
	private static class VariableDeclaratorVisitor extends VoidVisitorAdapter <VisitorContext> {

		  @Override
		  public void visit(VariableDeclarator vd, VisitorContext context) {
				super.visit(vd, context);
				
				ObjecctCreationVisitor objectCreationVisitor = new ObjecctCreationVisitor();
				
				
				

		        if (vd.getInitializer().isPresent()) {
		        	
		        	Expression expr = vd.getInitializer().get();
		            String initializer = expr.toString();
		            String name = vd.getNameAsString();
		            
		            
		            if (expr.isIntegerLiteralExpr()) {
		                
		                try {
		                    int intValue = Integer.parseInt(initializer);
		                    
		                    VariableReference vr = context.getBuilder().appendIntPrimitive(intValue);
		                    
		                    
		                    
		                    context.getTracker().aggiungiVariabile(name, vr);

		                    System.out.print(name);
		                    
		                } catch (NumberFormatException e) {
		                    System.out.println("Valore int non parsabile: " + initializer);
		                }
		            }
		            
		            if (expr.isBooleanLiteralExpr()) {
		            	boolean boolValue = Boolean.parseBoolean(initializer);
		            	
		            	
		            	
		            	
		            	VariableReference vr = context.getBuilder().appendBooleanPrimitive(boolValue);
	                    
	                    
	                    
	                    context.getTracker().aggiungiVariabile(name, vr);

	                    System.out.print(name);
		            }
		            
		            
		            if (expr.isCharLiteralExpr()) {
		            	
		            	char charValue = initializer.charAt(1);
		            	VariableReference vr = context.getBuilder().appendCharPrimitive(charValue);
	                    
	                    
	                    
	                    context.getTracker().aggiungiVariabile(name, vr);

	                    System.out.print(name);
		            	
		            }
		            
		            if (expr.isStringLiteralExpr()) {
		            	
		            	VariableReference vr = context.getBuilder().appendStringPrimitive(initializer.substring(1, initializer.length() -1));
	                    
	                   
	                    
	                    context.getTracker().aggiungiVariabile(name, vr);

	                    System.out.print(name);
	                    
		            	
		            }
		            
		            if (vd.getType().toString().equals("float")) {
		            	
		            	float floatValue = Float.parseFloat(initializer);
		            	
		            	VariableReference vr = context.getBuilder().appendFloatPrimitive(floatValue);
	                    
	                    
	                    
	                    context.getTracker().aggiungiVariabile(name, vr);

	                    System.out.print(name);
		            	
		            }
		            
		            if (expr.isDoubleLiteralExpr()){
		            	double doubleValue = Double.parseDouble(initializer);
		            	
		            	VariableReference vr = context.getBuilder().appendDoublePrimitive(doubleValue);
	                    
	                    
	                    context.getTracker().aggiungiVariabile(name, vr);

	                    System.out.print(name);
		            }
		            
		            if (vd.getType().toString().equals("byte")) {
		            	
		            	byte byteValue = Byte.parseByte(initializer);
		            	
		            	VariableReference vr = context.getBuilder().appendBytePrimitive(byteValue);
	                    
	                    
	                    context.getTracker().aggiungiVariabile(name, vr);

	                    System.out.print(name);
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
		            	
						VariableReference vr = context.getBuilder().appendClassPrimitive(clazz);
	                    
	                    
	                    
	                    context.getTracker().aggiungiVariabile(name, vr);

	                    System.out.print(name);
		            	
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

		                VariableReference vr = context.getBuilder().appendNull(typeForEvoSuite);
	                    
	                    
	                    context.getTracker().aggiungiVariabile(name, vr);

	                    System.out.print(name);
		            }
		            
		            if (expr.isFieldAccessExpr()) {
		                FieldAccessExpr fieldAccessExpr = expr.asFieldAccessExpr();
		                
		                
		                
		                try {
		                    ResolvedValueDeclaration resolvedField = fieldAccessExpr.resolve();

		                    if (resolvedField.isField()) {
		                        ResolvedFieldDeclaration resolvedFieldDecl = resolvedField.asField();

		                        try {
		                            Class<?> declaringClass = Class.forName(resolvedFieldDecl.declaringType().getQualifiedName());
		                            Field javaField = declaringClass.getField(resolvedFieldDecl.getName());
		                           
		                            if (resolvedField.asField().isStatic()) {
		                            VariableReference vr = context.getBuilder().appendStaticFieldStmt(javaField);
		                            context.getTracker().aggiungiVariabile(name, vr);
		                            }
		                            else {
		                            	
		                            	Expression scope = fieldAccessExpr.getScope();
		                            	
		                            	String variableName = "";

		                            	if (scope.isNameExpr()) {
		                            	    variableName = scope.asNameExpr().getNameAsString();
		                            	} else if (scope.isFieldAccessExpr()) {
		                            	    // per accessi concatenati tipo: this.foo.bar
		                            	    variableName = scope.asFieldAccessExpr().toString(); 
		                            	} else {
		                            	    variableName = scope.toString(); // fallback
		                            	}
		                            	
		                            	VariableReference vrReciver = context.getTracker().getRefernce(variableName);
		                            	
		                            	
		                            	VariableReference vr = context.getBuilder().appendFieldStmt(vrReciver, javaField);
			                            context.getTracker().aggiungiVariabile(name, vr);
	
		                            }


		                        } catch (ClassNotFoundException | NoSuchFieldException e) {
		                            //System.err.println("Errore nel riflettere sul campo: " + resolvedFieldDecl.getQualifiedName());
		                            e.printStackTrace();
		                        }

		                    }
		                    
		                    
		                    
		                } catch (UnsolvedSymbolException e) {
		                    //System.err.println("Errore nel risolvere FieldAccessExpr: " + fieldAccessExpr);
		                    e.printStackTrace();
		                }
		            }
		            
		            
		            if (expr.isObjectCreationExpr()) {
		            	
		            	context.setVariableName(name);
		            	
		            	objectCreationVisitor.visit(vd, context);
		            	
		            	context.setVariableName("");
		            }
		            
		            
		            if ()
		            
		            
		            
					
			  }
		        
		      else {
		    	  
		    	  
		    	  String type = vd.getType().toString();

		    	  VariableReference vr;

		    	  if (type.equals("int")) {
		    	      vr = context.getBuilder().appendIntPrimitive(0);
		    	  } else if (type.equals("boolean")) {
		    	      vr = context.getBuilder().appendBooleanPrimitive(false);
		    	  } else if (type.equals("char")) {
		    	      vr = context.getBuilder().appendCharPrimitive('\0');
		    	  } else if (type.equals("String")) {
		    	      vr = context.getBuilder().appendStringPrimitive(null);
		    	  } else if (type.equals("float")) {
		    	      vr = context.getBuilder().appendFloatPrimitive(0.0f);
		    	  } else if (type.equals("double")) {
		    	      vr = context.getBuilder().appendDoublePrimitive(0.0);
		    	  } else if (type.equals("byte")) {
		    	      vr = context.getBuilder().appendBytePrimitive((byte) 0);
		    	  } else {
		    	      System.out.println("Tipo non gestito: " + type);
		    	      vr = null;
		    	  }

		    	  if (vr != null) {
		    	      String name = vr.getName();
		    	      context.getTracker().aggiungiVariabile(name, vr);
		    	  }
		    	  
		    	  
		    	  
		      }
		 }
	}
	
	
	private static class EnumVisitor extends VoidVisitorAdapter <VisitorContext> {
		
		@Override
		public void visit (EnumDeclaration ed, VisitorContext context) {
			
			
			
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
			    VisitorContext context = new VisitorContext(md, builder);

				variableVisitor.visit(md, context);
				arrayVisitor.visit(md, context);
				
				
				
				collector.add(testCase);
		  }
	 }
	
	
	
	
	public static void main(String[] args) throws Exception {

	    // 1. Configurazione PRIMA di ogni parsing
	    CombinedTypeSolver typeSolver = new CombinedTypeSolver();
	    typeSolver.add(new ReflectionTypeSolver());
	    
	   String sourcePath = "src/main/java";
	   String jarLibsPath = "";
	   
	   
	    //Mio codice sorgente
	    typeSolver.add(new JavaParserTypeSolver(new File(sourcePath)));
        // Add external JARs
        File libDir = new File(jarLibsPath);
        addJarsFromFolder(typeSolver, libDir);

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
