package org.evosuite.samples;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class TestSimple {


@Test
public void testSquarePositive() {
    Simple simple = new Simple();
    assertEquals(25, simple.square(5));
}

@Test
public void testSquareNegative() {
    Simple simple = new Simple();
    assertEquals(9, simple.square(-3));
}

@Test
public void testFWithNegativeInput() {
    Simple simple = new Simple();
    assertEquals(16, simple.f(-4));
}

@Test
public void testFWithZero() {
    Simple simple = new Simple();
    assertEquals(3, simple.f(0));
}

@Test
public void testFWithPositiveInRange() {
    Simple simple = new Simple();
    assertEquals(8, simple.f(5));
}

@Test
public void testFWithGreaterThanFive() {
    Simple simple = new Simple();
    assertEquals(14, simple.f(7));
}

@Test
public void testSumEmptyCollection() {
    Simple simple = new Simple();
    assertEquals(0, simple.sum(Collections.emptyList()));
}

@Test
public void testSumSingleElement() {
    Simple simple = new Simple();
    assertEquals(10, simple.sum(Collections.singletonList(10)));
}

@Test
public void testSumMultipleElements() {
    Simple simple = new Simple();
    
    int a[] = new int[2];
    
    a[0] = 1;
    a[1] = 2;
    
    Arrays.asList(a);
    
}

//@Test
//public void testSumWithMixedNumbers() {
//    Simple simple = new Simple();
//    assertEquals(-2, simple.sum(Arrays.asList(-3, 1, 0)));
//}

}

