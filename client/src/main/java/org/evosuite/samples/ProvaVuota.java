package org.evosuite.samples;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.evosuite.symbolic.TestCaseBuilder;
import org.junit.Test;


public class ProvaVuota {
	
	enum  Day {lunedi, martedi, mercoledi, giovedi, venerdi, sabato, domenica};
	
	private static final String FILE_PATH = "src/main/java/org/evosuite/samples/FieldProva.java";
	
	@Test
    public void testFoo() {
		
		
		
		
		File file = new File("src/main/java/org/evosuite/samples/FieldProva.java");
		
		
		
		//Day giorno = Day.lunedi;
		
		
		boolean x = false;
		boolean y = true;
		int z = 1;
		int f = 2;
		
		int h = -4;
		
		String prova = "prova";
		double d = -3.675989;
		
		int g = 4;
		
		int non;
		
		Class<?> clazz = String.class;
		
		Class<?> clazz1 = FieldProva.class;
		
		int[] array = new int[5];
		
		array[1] = z;
		
		f = array[1];
		
		int[][] vettore = new int[1][2];
		
		double pi = Math.PI;
		
		FieldProva catena = new FieldProva(f, g);
		
		catena.getField2().getY();
		
		FieldProva provaField = new FieldProva(f, pi);
		
		int risposta = FieldProva.ANSWER;
		
		int domanda = provaField.question;
		
		provaField.question = z;
		
		Field2 var = new Field2();
		
		provaField.question = var.x;
		
		int doppio = provaField.doubleInt(risposta);
		
		provaField.factorY(d);
		
		Math.sqrt(pi);
		
		
	}
	
	
	@Test 
	public void testBar() {
		
		boolean a = true;
		boolean b = false;
		char c = 'c';
		
		 float eee = 3.0F;
		
		String provalunga = "è una stringa più lunga";
		
		byte morse = 6;
		
		
		Class<?> clazz1 = Math.class;
		Class<?> clazz2 = provalunga.getClass();
		
		Math strano = null;
		
		
		
		Integer pazzia = null;
		
		List<String> lista  = null;
		
		String[][][] vettoreStringa = new String[7][12][36];
		
		
		
	}

}
