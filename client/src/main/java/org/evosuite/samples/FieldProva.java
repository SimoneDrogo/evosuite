package org.evosuite.samples;

public class FieldProva {
	
	
	private int x;
	
	private Double y;
	
	private Field2 field;
	
	public FieldProva(int x, double y) {
		
		 this.x = x;
		 
		 this.y = new Double(y);
		 
		 this.field = new Field2();
		
	}
	
	public int doubleInt (int n) {
		
		return 2*n;
	}
	
	public void setY(double x) {
		
		this.y =  new Double(x);
	}
	
	public Double getY(){
		
		return this.y;
		
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
