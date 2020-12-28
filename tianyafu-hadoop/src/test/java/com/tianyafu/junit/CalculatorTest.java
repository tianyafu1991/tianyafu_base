package com.tianyafu.junit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CalculatorTest {

    Calculator calculator ;

    @Before
    public void setUp() throws Exception {
        System.out.println("setUp...........");
        calculator = new Calculator();
    }

    @After
    public void tearDown() throws Exception {
        calculator = null;
        System.out.println("tearDown..........");
    }

    @Test
    public void add() {
        int result = calculator.add(2, 5);
        assertEquals(7,result);
        System.out.println("add........");

    }

    @Test
    public void divide() {
        int  a = 10;

        int b = 5 ;
        int result ;
        if(b != 0){
            result = calculator.divide(a,b);
        }else{
            result = 0;
        }

        assertEquals(2,result);
        System.out.println("divide........");
    }
}