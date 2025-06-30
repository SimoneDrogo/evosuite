package org.evosuite.samples;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.symbolic.TestCaseBuilder;
import org.junit.Test;


public class ProvaVuota {
	
	@Test
    public void testFoo() {
		
		boolean x = false;
		boolean y = true;
		int z = 1;
		int f = 2;
		String prova = "prova";
		double d = 3.675989;
		
		int g = z+f;
		
		int non;
		
		Class<?> clazz = String.class;
		
		Class<?> clazz1 = FieldProva.class;
		
		int[] array = null;
		
		int[][] vettore = new int[1][2];
		
		double pi = Math.PI;
		
		FieldProva provaField = new FieldProva(z);
		
		int risposta = FieldProva.ANSWER;
		
		//int domanda = provaField.question;
		

	}
	
	
	@Test 
	public void testBar() {
		
		boolean a = true;
		boolean b = false;
		char c = 'c';
		
		float f = 3.0F;
		
		String provalunga = "è una stringa più lunga";
		
		byte morso = 6;
		
		
		Class<?> clazz1 = Math.class;
		Class<?> clazz2 = provalunga.getClass();
		
		Math strano = null;
		
		
		
		Integer pazzia = null;
		
		List<String> lista  = null;
		
		String[][][] vettoreStringa = new String[7][12][36];
		
		
		
	}

}
