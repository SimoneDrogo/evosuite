package org.evosuite.testcase.javapareser;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.resolution.types.*;

import java.lang.reflect.*;

public class ResolvedTypeToReflectTypeConverter {

    public static Type toReflectType(ResolvedType resolvedType) throws ClassNotFoundException {
    	
    	System.out.println("Sono arrivato alla classe statica");

        if (resolvedType.isPrimitive()) {
            return mapPrimitive(resolvedType.asPrimitive());
        } 
        else if (resolvedType.isArray()) {
        	
            return mapArray(resolvedType.asArrayType());
        } 
        else if (resolvedType.isReferenceType()) {
        	
        	System.out.println("Sono nel branch ReferenceType");
        	
        	
            return mapReference(resolvedType.asReferenceType());
        } 
        else if (resolvedType.isTypeVariable()) {
            // Generics type variables — fallback: Object
            return Object.class;
        }
        else if (resolvedType.isWildcard()) {
            // Wildcard — fallback: Object
            return Object.class;
        }
        else {
            throw new UnsupportedOperationException("Type not supported: " + resolvedType.describe());
        }
    }

    private static Type mapPrimitive(ResolvedPrimitiveType primitiveType) {
        switch (primitiveType.getBoxTypeQName()) {
            case "java.lang.Boolean": return boolean.class;
            case "java.lang.Byte": return byte.class;
            case "java.lang.Character": return char.class;
            case "java.lang.Double": return double.class;
            case "java.lang.Float": return float.class;
            case "java.lang.Integer": return int.class;
            case "java.lang.Long": return long.class;
            case "java.lang.Short": return short.class;
            default:
                throw new IllegalArgumentException("Unknown primitive: " + primitiveType.describe());
        }
    }

    private static Type mapArray(ResolvedArrayType arrayType) throws ClassNotFoundException {
        Type componentType = toReflectType(arrayType.getComponentType());
        if (componentType instanceof Class) {
            return Array.newInstance((Class<?>) componentType, 0).getClass();
        } else {
            // Se non è una Class, EvoSuite non lo gestisce comunque: fallback Object[].class
            return Object[].class;
        }
    }

    private static Type mapReference(ResolvedReferenceType referenceType) throws ClassNotFoundException {
    	
    	
    	System.out.println("Parametri dei null:  " + referenceType.typeParametersValues());
    	
    	
    	
        String qualifiedName = referenceType.getQualifiedName();
        return Class.forName(qualifiedName);
        
   
    }
}
