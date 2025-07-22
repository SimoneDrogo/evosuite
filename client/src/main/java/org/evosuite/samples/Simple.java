package org.evosuite.samples;

import java.util.Collection;

public class Simple
{

	public int square(int x)
	{
		int result = x * x;


		return result;
	}

	public int f(int x)
	{

		if (x < 0){

			return square(x);
			
		}else if ((x >= 0) && (x <= 5)){
			
			return x + 3;
			
		}else{
			
			return 2 * x;
			
		}
	}

	public int sum(Collection<Number> c)
	{
		int result = 0;

		for (Number n : c)
		{
			int value = n.intValue();


			result += value;
		}


		return result;
	}
}