package org.evosuite.samples;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

public class SimpleTest extends TestCase
{
	
	private Simple simple;

	@Before
	public void setUp(){
		simple = new Simple();
	}

	@Test
	public void testSquare(){
		
		Simple simple = new Simple();
		assertEquals(1, simple.square(1));
		assertEquals(1, simple.square(-1));
	}

	@Test
	public void testF(){
		
		Simple simple = new Simple();
		assertEquals(1, simple.f(-1));
		assertEquals(12, simple.f(6));
	}

	@Test
	public void testSum(){
		
		Simple simple = new Simple();
		Collection<Number> c = new LinkedList<Number>();
		c.add(new Integer(3));
		c.add(new Integer(5));
		c.add(new Integer(8));
		assertEquals(16, simple.sum(c));
	}
	
}
