package org.evosuite.testcase.javapareser;

import static org.mockito.ArgumentMatchers.intThat;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.AssertStmt;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.runtime.testdata.EvoSuiteFile;
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
	
	
	private static Field getFieldByExpression (FieldAccessExpr fieldAccessExpr) {
		
                	ResolvedValueDeclaration resolvedField = fieldAccessExpr.resolve();

                    ResolvedFieldDeclaration resolvedFieldDecl = resolvedField.asField();

                    Class<?> declaringClass = null;
					try {
						declaringClass = Class.forName(resolvedFieldDecl.declaringType().getQualifiedName());
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                    Field javaField = null;
					try {
						javaField = declaringClass.getField(resolvedFieldDecl.getName());
					} catch (NoSuchFieldException | SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                    
                    return javaField;

	}
	
	private static VariableReference getReferenceByExpression (FieldAccessExpr fieldAccessExpr, VisitorContext context) {
		
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
    	
    	return vrReciver;
		
		
	}
	
	
	
	private static class MethodCallExprVisitor extends VoidVisitorAdapter<VisitorContext> {
		
		
		@Override
	    public void visit(MethodCallExpr mce, VisitorContext context) {
			
			super.visit(mce, context);
			
			Optional<Expression> scopeOpt = mce.getScope();
	    	
	    	String variableName = "";
	    	
	    	VariableReference vrReciver;
	    	
	    	
	    	if (scopeOpt.isPresent()) {
	    		
	         Expression scope = scopeOpt.get();

	    	if (scope.isNameExpr()) {
	    	    variableName = scope.asNameExpr().getNameAsString();
	    	} else {
	    	    variableName = scope.toString(); // fallback
	    	}
	    	
	    	 vrReciver = context.getTracker().getRefernce(variableName);
	    	
	    	}
	    	else {
	    		
	    		 vrReciver = null;
	    		
	    	}
	    	
	    	ResolvedMethodDeclaration resolved = mce.resolve();
	        String qualifiedName = resolved.declaringType().getQualifiedName();

	        Class<?> clazz = null;

	        try {
	            clazz = Class.forName(qualifiedName);
	        } catch (ClassNotFoundException e) {
	            e.printStackTrace();
	        }
	        
	        
	        NodeList<Expression> parameters = mce.getArguments();
            VariableReference[] parametersVr = new VariableReference[resolved.getNumberOfParams()];
            List<Class<?>> paramTypes = new ArrayList<>();
            
            int i = 0;
            for (Expression parameter : parameters) {
                

                VariableReference vr = context.getTracker().getRefernce(parameter.asNameExpr().getNameAsString());
                parametersVr[i] = vr;

                ResolvedType paramType = resolved.getParam(i).getType();
                String className = "";

                if (paramType.isPrimitive()) {
                    String simpleName = paramType.describe();
                    switch (simpleName) {
                        case "int":
                            className = "int";
                            break;
                        case "boolean":
                            className = "boolean";
                            break;
                        case "double":
                            className = "double";
                            break;
                        case "long":
                            className = "long";
                            break;
                        case "char":
                            className = "char";
                            break;
                        case "float":
                            className = "float";
                            break;
                        case "short":
                            className = "short";
                            break;
                        case "byte":
                            className = "byte";
                            break;
                        default:
                            throw new IllegalArgumentException("Tipo primitivo non gestito: " + simpleName);
                    }
                } else {
                    className = paramType.asReferenceType().getQualifiedName();
                }

                Class<?> clazzParam = null;

                switch (className) {
                    case "int":
                        clazzParam = int.class;
                        break;
                    case "boolean":
                        clazzParam = boolean.class;
                        break;
                    case "double":
                        clazzParam = double.class;
                        break;
                    case "long":
                        clazzParam = long.class;
                        break;
                    case "char":
                        clazzParam = char.class;
                        break;
                    case "float":
                        clazzParam = float.class;
                        break;
                    case "short":
                        clazzParam = short.class;
                        break;
                    case "byte":
                        clazzParam = byte.class;
                        break;
                    default:
                        try {
                            clazzParam = Class.forName(className);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        break;
                }

                paramTypes.add(clazzParam);
                i++;
            }
            
            
            Method method = null;
            
            
            try {
				method = clazz.getMethod(mce.getNameAsString(), paramTypes.toArray(new Class<?> [0]) );
			} catch (NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            VariableReference vr = context.getBuilder().appendMethod(vrReciver, method, parametersVr);
            
            //context.getTracker().aggiungiVariabile(null, vr);
	        
	
		}
		
		
	}
	
	
	
	
	
	private static class ObjectCreationVisitor extends VoidVisitorAdapter<VisitorContext> {

	    @Override
	    public void visit(ObjectCreationExpr oc, VisitorContext context) {
	        super.visit(oc, context);

	        

	        if (oc.getType().getNameAsString().equals("File")) {
	            if (!oc.getArguments().isEmpty()) {
	                Expression arg = oc.getArgument(0);

	                if (arg.isStringLiteralExpr()) {
	                    StringLiteralExpr strExpr = arg.asStringLiteralExpr();
	                    String path = strExpr.getValue();
	                    System.out.println("Percorso del file: " + path);

	                    EvoSuiteFile file = new EvoSuiteFile(path);
	                    context.getBuilder().appendFileNamePrimitive(file);
	                }
	            }
	        } else {
	        	
	        	
	        	

		        ResolvedConstructorDeclaration resolved = oc.resolve();
		        String qualifiedName = resolved.declaringType().getQualifiedName();

		        Class<?> clazz = null;

		        try {
		            clazz = Class.forName(qualifiedName);
		        } catch (ClassNotFoundException e) {
		            e.printStackTrace();
		        }
	        	
	            NodeList<Expression> parameters = oc.getArguments();
	            VariableReference[] parametersVr = new VariableReference[resolved.getNumberOfParams()];
	            List<Class<?>> paramTypes = new ArrayList<>();

	            int i = 0;
	            for (Expression parameter : parameters) {
	                

	                VariableReference vr = context.getTracker().getRefernce(parameter.asNameExpr().getNameAsString());
	                parametersVr[i] = vr;

	                ResolvedType paramType = resolved.getParam(i).getType();
	                String className = "";

	                if (paramType.isPrimitive()) {
	                    String simpleName = paramType.describe();
	                    switch (simpleName) {
	                        case "int":
	                            className = "int";
	                            break;
	                        case "boolean":
	                            className = "boolean";
	                            break;
	                        case "double":
	                            className = "double";
	                            break;
	                        case "long":
	                            className = "long";
	                            break;
	                        case "char":
	                            className = "char";
	                            break;
	                        case "float":
	                            className = "float";
	                            break;
	                        case "short":
	                            className = "short";
	                            break;
	                        case "byte":
	                            className = "byte";
	                            break;
	                        default:
	                            throw new IllegalArgumentException("Tipo primitivo non gestito: " + simpleName);
	                    }
	                } else {
	                    className = paramType.asReferenceType().getQualifiedName();
	                }

	                Class<?> clazzParam = null;

	                switch (className) {
	                    case "int":
	                        clazzParam = int.class;
	                        break;
	                    case "boolean":
	                        clazzParam = boolean.class;
	                        break;
	                    case "double":
	                        clazzParam = double.class;
	                        break;
	                    case "long":
	                        clazzParam = long.class;
	                        break;
	                    case "char":
	                        clazzParam = char.class;
	                        break;
	                    case "float":
	                        clazzParam = float.class;
	                        break;
	                    case "short":
	                        clazzParam = short.class;
	                        break;
	                    case "byte":
	                        clazzParam = byte.class;
	                        break;
	                    default:
	                        try {
	                            clazzParam = Class.forName(className);
	                        } catch (ClassNotFoundException e) {
	                            e.printStackTrace();
	                        }
	                        break;
	                }

	                paramTypes.add(clazzParam);
	                i++;
	            }

	            Constructor<?> constructor = null;

	            try {
	                constructor = clazz.getDeclaredConstructor(paramTypes.toArray(new Class<?>[0]));
	            } catch (NoSuchMethodException | SecurityException e) {
	                e.printStackTrace();
	            }

	            VariableReference vr = context.getBuilder().appendConstructor(constructor, parametersVr);
	            context.getTracker().aggiungiVariabile(context.getVariableName(), vr);
	        }
	    }
	}

	
	
	
	private static class AssignExprVisitor extends VoidVisitorAdapter <VisitorContext> {
		
		@Override
		public void visit (AssignExpr ae, VisitorContext context) {
			
			super.visit(ae, context);
			
			Expression target = ae.getTarget();
			
			String varName = "";

	        if (target.isNameExpr()) {
	            varName = target.asNameExpr().getNameAsString();
	            
	        } else if (target.isFieldAccessExpr()) {
	        	
	        	
	        	FieldAccessExpr fieldAccessExpr = ae.getTarget().asFieldAccessExpr();
	        	
	        	Field javaField = getFieldByExpression(fieldAccessExpr);
                
                
            	VariableReference vrReciver = getReferenceByExpression(fieldAccessExpr, context);
            	
            	if (ae.getValue().isNameExpr()) {
            		
	            	VariableReference vrValue = context.getTracker().getRefernce(ae.getValue().asNameExpr().getNameAsString());
	                            	
	            	context.getBuilder().appendAssignment(vrReciver, javaField, vrValue);
            	}
            	
            	else if (ae.getValue().isFieldAccessExpr()) {
            		
            		FieldAccessExpr fieldAccessExprSrc = ae.getValue().asFieldAccessExpr();
            		
            		Field javaFieldSrc = getFieldByExpression(fieldAccessExprSrc);
            		
            		VariableReference vrSrc = getReferenceByExpression(fieldAccessExprSrc, context);
            		
            		context.getBuilder().appendAssignment(vrReciver, javaField, vrSrc, javaFieldSrc);
            		
            	}
  
	           
	        }
	        
	        else if (ae.getTarget().isArrayAccessExpr()) {
	        	
	        	String arrayName = ae.getTarget().asArrayAccessExpr().getName().toString();
	        	
	        	System.out.println(arrayName);
	        	
	        	ArrayReference ar = (ArrayReference) context.getTracker().getRefernce(arrayName);
	        	
	        	int index = Integer.parseInt(ae.getTarget().asArrayAccessExpr().getIndex().toString());
	        	
	        	if (ae.getValue().isNameExpr()) {
	        		
	        		String varNameReciver = ae.getValue().asNameExpr().getNameAsString();
	        		
	        		VariableReference vrReciver = context.getTracker().getRefernce(varNameReciver);
	        		 
	        		context.getBuilder().appendAssignment(ar, index, vrReciver); 
	        		 		
	        		
	        	}
	        	
	        	
	        }
	        
	        VariableReference vrReciver = context.getTracker().getRefernce(varName);
	        
	        if(ae.getValue().isArrayAccessExpr()) {
	        	
	        	String arrayName = ae.getValue().asArrayAccessExpr().getName().toString();
	        	
	        	ArrayReference ar = (ArrayReference) context.getTracker().getRefernce(arrayName);
	        	
	        	int index = Integer.parseInt(ae.getValue().asArrayAccessExpr().getIndex().toString());
	        	
	        	context.getBuilder().appendAssignment(vrReciver, ar, index);
	        }
			

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
		    
		    String name = context.getVariableName();	
		    
		    context.getTracker().aggiungiVariabile(name, ar);
		    
		}

	}

	
	private static class VariableDeclaratorVisitor extends VoidVisitorAdapter <VisitorContext> {

		  @Override
		  public void visit(VariableDeclarator vd, VisitorContext context) {
				super.visit(vd, context);
				
				ObjectCreationVisitor objectCreationVisitor = new ObjectCreationVisitor();
				
				ArrayCreationExprVisitor arrayVisitor = new ArrayCreationExprVisitor();
				
				
				

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
		                
		                Field javaField = getFieldByExpression(fieldAccessExpr);
		                           
	                            if (fieldAccessExpr.resolve().asField().isStatic()) {
	                            	
	                            VariableReference vr = context.getBuilder().appendStaticFieldStmt(javaField);
	                            context.getTracker().aggiungiVariabile(name, vr);
	                            
	                            }
	                            else {
	                            	
	                           
	                            	VariableReference vrReciver = getReferenceByExpression(fieldAccessExpr, context);
	                            	
	                            	
	                            	VariableReference vr = context.getBuilder().appendFieldStmt(vrReciver, javaField);
		                            context.getTracker().aggiungiVariabile(name, vr);

	                            }

		            }
		            
		            
		            if (expr.isArrayCreationExpr()) {
		            	
		            	context.setVariableName(name);
		            	
		            	arrayVisitor.visit(vd, context);
		            	
		            	context.setVariableName("");
		            	
		            	
		            }
		            
		            
		            if (expr.isObjectCreationExpr()) {
		            	
		            	context.setVariableName(name);
		            	
		            	objectCreationVisitor.visit(vd, context);
		            	
		            	context.setVariableName("");
		            }
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
				
				AssignExprVisitor assignVisitor = new AssignExprVisitor();
				
				MethodCallExprVisitor callVisitor = new MethodCallExprVisitor();
				
				
				
			    
			    DefaultTestCase testCase = new DefaultTestCase();
			    TestCaseBuilder builder = new TestCaseBuilder(testCase, 0);
			    VisitorContext context = new VisitorContext(md, builder);

				variableVisitor.visit(md, context);
				
				assignVisitor.visit(md, context);
				
				callVisitor.visit(md, context);
				
				
				
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
