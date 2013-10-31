package edu.jhu.prim.vector;

import java.util.Arrays;

import edu.jhu.prim.Primitives;
import edu.jhu.prim.arrays.DoubleArrays;
import edu.jhu.prim.util.Lambda;
import edu.jhu.prim.util.Lambda.FnLongDoubleToDouble;
import edu.jhu.prim.util.SafeCast;
import edu.jhu.prim.vector.LongDoubleHashVector.SparseBinaryOpApplier;


public class LongDoubleDenseVector implements LongDoubleVector {

    private static final long serialVersionUID = 1L;

    /** The value given to non-explicit entries in the vector. */
    private static final double missingEntries = 0;
    /** The internal array representing this vector. */
    private double[] elements;
    /** The index after the last explicit entry in the vector. */
    private int idxAfterLast;

    public LongDoubleDenseVector() {
        this(8);
    }
    
    public LongDoubleDenseVector(int initialCapacity) {
        elements = new double[initialCapacity];
        idxAfterLast = 0;
    }

    public LongDoubleDenseVector(double[] elements) {
        this.elements = elements;
        idxAfterLast = elements.length;
    }
    
    /** Copy constructor. */
    public LongDoubleDenseVector(LongDoubleDenseVector other) {
        this.elements = DoubleArrays.copyOf(other.elements);
        this.idxAfterLast = other.idxAfterLast;
    }
    
    /**
     * Gets the i'th entry in the vector.
     * @param i The index of the element to get.
     * @return The value of the element to get.
     */
    public double get(long i) {
        if (i < 0 || i >= idxAfterLast) {
            return missingEntries;
        }
        return elements[SafeCast.safeLongToInt(i)];
    }
    
    /**
     * Sets the i'th entry of the vector to the given value.
     * @param i The index to set.
     * @param value The value to set.
     */
    public void set(long idx, double value) {
        int i = SafeCast.safeLongToInt(idx);
        idxAfterLast = Math.max(idxAfterLast, i + 1);
        ensureCapacity(idxAfterLast);
        elements[i] = value;
    }

    public void add(long idx, double value) {
        int i = SafeCast.safeLongToInt(idx);
        idxAfterLast = Math.max(idxAfterLast, i + 1);
        ensureCapacity(idxAfterLast);
        elements[i] += value;
    }

    public void scale(double multiplier) {
        for (int i=0; i<idxAfterLast; i++) {
            elements[i] *= multiplier;
        }
    }

    @Override
    public double dot(double[] other) {
        int max = Math.min(idxAfterLast, other.length);
        double dot = 0;
        for (int i=0; i<max; i++) {
            dot += elements[i] * other[i];
        }
        return dot;
    }

    @Override
    public double dot(LongDoubleVector y) {
        if (y instanceof LongDoubleSortedVector || y instanceof LongDoubleHashVector) {
            return y.dot(this);
        } else if (y instanceof LongDoubleDenseVector){
            LongDoubleDenseVector other = (LongDoubleDenseVector) y;
            int max = Math.min(idxAfterLast, other.idxAfterLast);
            double dot = 0;
            for (int i=0; i<max; i++) {
                dot += elements[i] * y.get(i);
            }
            return dot;
        } else {
            double dot = 0;
            for (int i=0; i<idxAfterLast; i++) {
                dot += elements[i] * y.get(i);
            }
            return dot;
        }
    }
    
    @Override
    public void apply(FnLongDoubleToDouble function) {
        for (int i=0; i<idxAfterLast; i++) {
            elements[i] = function.call(i, elements[i]);
        }
    }

    /** Updates this vector to be the entrywise sum of this vector with the other. */
    public void add(LongDoubleVector other) {
        // TODO: Add special case for LongDoubleDenseVector.
        other.apply(new SparseBinaryOpApplier(this, new Lambda.DoubleAdd()));
    }
    
    /** Updates this vector to be the entrywise difference of this vector with the other. */
    public void subtract(LongDoubleVector other) {
        // TODO: Add special case for LongDoubleDenseVector.
        other.apply(new SparseBinaryOpApplier(this, new Lambda.DoubleSubtract()));
    }
    
    /** Updates this vector to be the entrywise product (i.e. Hadamard product) of this vector with the other. */
    public void product(LongDoubleVector other) {
        // TODO: Add special case for LongDoubleDenseVector.
        for (int i=0; i<idxAfterLast; i++) {
            elements[i] *= other.get(i);
        }
    }
    
    /**
     * Gets the index of the first element in this vector with the specified
     * value, or -1 if it is not present.
     * 
     * @param value The value to search for.
     * @param delta The delta with which to evaluate equality.
     * @return The index or -1 if not present.
     */
    public int lookupIndex(double value, double delta) {
        for (int i=0; i<elements.length; i++) {
            if (Primitives.equals(elements[i], value, delta)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Gets a NEW array containing all the elements in this array list.
     * @return The new array containing the elements in this list.
     */
    public double[] toNativeArray() {
        return Arrays.copyOf(elements, idxAfterLast);
    }
    
    /**
     * Trims the internal array to exactly the size of the list.
     */
    public void trimToSize() {
        if (idxAfterLast != elements.length) { 
            elements = Arrays.copyOf(elements, idxAfterLast);
        }
    }

    /**
     * Ensure that the internal array has space to contain the specified number of elements.
     * @param size The number of elements. 
     */
    private void ensureCapacity(int size) {
        elements = ensureCapacity(elements, size);
    }
    
    /**
     * Ensure that the array has space to contain the specified number of elements.
     * @param elements The array.
     * @param size The number of elements. 
     */
    public static double[] ensureCapacity(double[] elements, int size) {
        if (size > elements.length) {
            double[] tmp = new double[size*2];
            System.arraycopy(elements, 0, tmp, 0, elements.length);
            elements = tmp;
        }
        return elements;
    }

}
