package edu.jhu.prim.map;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Test;

import edu.jhu.prim.Primitives;
import edu.jhu.util.Timer;

public class IntLongHashMapTest {

    @Test
    public void testPowersOf2() {
        IntLongHashMap map = new IntLongHashMap();
        int start = 0;
        int end = Primitives.INT_NUM_BITS;
        for (int i=start; i<end; i++) {
            int key = toInt(2) << i;
            System.out.print(key + " ");
            map.put(key, i);  
        }
        assertEquals(end - start, map.size());
        for (int i=start; i<end; i++) {
            int key = toInt(2) << i;
            assertEquals(i, map.get(key));  
        }
        System.out.println("");
    }

    private static int toInt(int d) {
        return (int)d;
    }

}
