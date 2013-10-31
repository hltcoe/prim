package edu.jhu.prim.util.math;

import static edu.jhu.prim.util.math.LogAddTable.LOG_ADD_MIN;
import static edu.jhu.prim.util.math.LogAddTable.logAdd;
import static edu.jhu.prim.util.math.LogAddTable.logSubtract;

import java.util.Random;

import org.junit.Test;

import edu.jhu.prim.util.math.FastMath;



public class LogAddTableTest {

    @Test
    public void testLogAddTable() {
        Random random = new Random();

        System.out.println("Log add test: ");
        double logAddDiff = 0;
        for (int i = 0; i < 100; i++) {
            double a = LOG_ADD_MIN * random.nextDouble();
            double b = a * random.nextDouble();
            System.out.println("a="+a + " b=" + b);
            double diff = logAdd(a, b) - FastMath.logAddExact(a, b);
            if (diff < 0)
                logAddDiff -= diff;
            else
                logAddDiff += diff;
        }
        System.out.println("Total log add difference: " + logAddDiff);
        System.out.println(Math.exp(-128));
    }
    
    @Test
    public void testLogSubtractTable() {
        Random random = new Random();

        System.out.println("Log subtract test: ");
        double logSubtractDiff = 0;
        for (int i = 0; i < 100; i++) {
            double b = LOG_ADD_MIN * random.nextDouble();
            double a = b * random.nextDouble();
            System.out.println("a="+a + " b=" + b);
            double diff = logSubtract(a, b) - FastMath.logSubtractExact(a, b);
            if (diff < 0)
                logSubtractDiff -= diff;
            else
                logSubtractDiff += diff;
        }
        System.out.println("Total log subtract difference: " + logSubtractDiff);
        System.out.println(Math.exp(-128));
    }
    
    @Test
    public void testAddSubtract() {
        double sum;
        System.out.println("sum: " + Math.log(0.2 + 0.3));
        sum = Double.NEGATIVE_INFINITY;
        sum = logAdd(sum, Math.log(0.2));
        sum = logAdd(sum, Math.log(0.3));
        System.out.println("sum: " + sum);
        sum = Double.NEGATIVE_INFINITY;
        sum = FastMath.logAddExact(sum, Math.log(0.2));
        sum = FastMath.logAddExact(sum, Math.log(0.3));
        System.out.println("sum: " + sum);
        sum = FastMath.logAddExact(sum, Math.log(0.1));
        sum = FastMath.logSubtractExact(sum, Math.log(0.1));
        System.out.println("sum: " + sum);
    }
    
    @Test
    public void test3() {
        double largestNegDouble = -Double.MAX_VALUE;
        System.out.println("Largest negative double: " + largestNegDouble);
        System.out.println("Log add of largest neg double with itself: " + logAdd(largestNegDouble, largestNegDouble));
        System.out.println("Min double: " + Double.MIN_VALUE);
        System.out.println("Log of min double: " + Math.log(Double.MIN_VALUE));
        System.out.println("Min 32-bit float: " + -Float.MAX_VALUE); //-3.4E38);
        //System.out.println("Min 32-bit float: " + -3.4E38);
    }
    
    @Test
    public void test4() {
        System.out.println(FastMath.logAddExact(Double.NEGATIVE_INFINITY, 0));
        System.out.println(FastMath.logAddExact(0, 0));
    }
    
}
