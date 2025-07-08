package org.evosuite.samples;

public class FieldProva {
	
	
	private int x;
	
	private double y;
	
	private Field2 field;
	
	public FieldProva(int x, double y) {
		
		 this.x = x;
		 
		 this.y = y;
		 
		 this.field = new Field2();
		
	}
	
	public int doubleInt (int n) {
		
		return 2*n;
	}
	
	public void factorY (double factor) {
		
		this.y = y * factor;
	}
	
	public Field2 getField2 () {
		
		return this.field;
		
	}
	
	public static final int ANSWER = 42;
	public  int question = 420;
}
