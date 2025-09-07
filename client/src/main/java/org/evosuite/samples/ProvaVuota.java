package org.evosuite.samples;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.testcase.variable.VariableReference;
import org.junit.Test;

import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;



public class ProvaVuota {
	
	private static enum  Day {lunedi, martedi, mercoledi, giovedi, venerdi, sabato, domenica};
	
	
	
	@Test
    public void testFoo() {
		
		
		
		
		File file = new File("src/main/java/org/evosuite/samples/FieldProva.java");
		
		String jarLibsPath = "C:/Users/simon/.m2/repository/junit/junit/4.13.2/";
		   
		   
	    
        File libDir = new File(jarLibsPath);
        
        
		
		Day giorno = Day.lunedi;
		
		
		
		
		boolean x = false;
		boolean y = true;
		int z = 1;
		int f = 2;
		
		int h = -4;
		
		String prova = "prova";
		double d = -3.675989;
		
		int g = 4;
		
		int non = 0;
		
		Class<?> clazz = String.class;
		
		Class<?> clazz1 = FieldProva.class;
		
		int[] array = new int[5];
		
		array[1] = z;
		
		f = array[1];
		
		int[][] vettore = new int[1][2];
		
		
		
		vettore[0][2] = 3;
		
		vettore[1][1] = non;
		
		double pi = Math.PI;
		
		FieldProva catena = new FieldProva(f, g);
		
		catena.getField2().getY().intValue();
		
		catena.getField2().getY().hashCode();
		
		catena.getY().hashCode();
		
		catena.setY(pi);
		
		
		
		catena.getY().hashCode();
		
		Field2 f2 = catena.getField2();
		
		Integer inter = f2.getY();
		
		int hash = inter.hashCode();
		
		FieldProva catena1 = new FieldProva (hash, 2.1);
		
		catena1.getField2().getY().hashCode();
		
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
		
		char rrt = 'é';
		
		float eee = -3.0F;
		
		String provalunga = "è una stringa più lunga";
		
		byte morse = 6;
		
		
		
		
		
		
		Class<?> clazz1 = Math.class;
		Class<?> clazz2 = provalunga.getClass();
		
		Math strano = null;
		
		
		
		Integer pazzia = null;
		
		List<Class> lista  = new ArrayList();
		
		HashMap<Integer, Double>   mappa  = null;
		
		Class<?> classona = null;
		
		HashMap<Integer, String> mappina = new HashMap();
		
		String[][][] vettoreStringa = new String[7][12][36];
		
		lista.add(clazz2);
		
		
		
	}
	
	@Test(timeout = 4000)
	  public void test04()  throws Throwable  {
	      EvoComplex evoComplex0 = new EvoComplex(1, (String) null);
	      Double double0 = new Double(1);
	      Long long0 = new Long(1);
	      evoComplex0.ingest((String) null, double0, long0);
	      evoComplex0.ingest((String) null, double0, long0);
	      evoComplex0.ingest((String) null, double0, long0);
	      String string0 = evoComplex0.explain("");
	      assertEquals("User=<anon> phase=WARMUP mode=MEAN decay=0.960 window=8 score=0.115 | n=3 recentVol=0.000 next\u22480.115", string0);
	  }

}
