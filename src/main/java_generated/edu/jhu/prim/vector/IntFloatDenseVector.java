package edu.jhu.prim.vector;

import java.util.Arrays;

import edu.jhu.prim.Primitives;
import edu.jhu.prim.arrays.FloatArrays;
import edu.jhu.prim.util.Lambda;
import edu.jhu.prim.util.Lambda.FnIntFloatToFloat;
import edu.jhu.prim.util.Lambda.FnIntFloatToVoid;
import edu.jhu.prim.util.SafeCast;
import edu.jhu.prim.vector.IntFloatHashVector.SparseBinaryOpApplier;

//TODO: Implement Iterable<IntFloatEntry>.
public class IntFloatDenseVector extends AbstractIntFloatVector implements IntFloatVector {

    private static final long serialVersionUID = 1L;

    /** The value given to non-explicit entries in the vector. */
    private static final float missingEntries = 0;
    /** The internal array representing this vector. */
    private float[] elements;
    /** The index after the last explicit entry in the vector. */
    private int idxAfterLast;

    public IntFloatDenseVector() {
        this(8);
    }
    
    public IntFloatDenseVector(int initialCapacity) {
        elements = new float[initialCapacity];
        idxAfterLast = 0;
    }

    public IntFloatDenseVector(float[] elements) {
        this.elements = elements;
        idxAfterLast = elements.length;
    }
    
    /** Gets a deep copy of this vector. */
    public IntFloatDenseVector(IntFloatDenseVector other) {
        this.elements = FloatArrays.copyOf(other.elements);
        this.idxAfterLast = other.idxAfterLast;
    }

    /** Copy constructor. */
    public IntFloatDenseVector(IntFloatVector other) {
        // TODO: Exploit the number of non-zero entries in other.
        this();
        final IntFloatDenseVector thisVec = this; 
        other.iterate(new FnIntFloatToVoid() {
            @Override
            public void call(int idx, float val) {
                thisVec.set(idx, val);
            }
        });
    }
    
    /** Copy factory method. */
    @Override
    public IntFloatVector copy() {
        return new IntFloatDenseVector(this);
    }
    
    /**
     * Gets the i'th entry in the vector.
     * @param i The index of the element to get.
     * @return The value of the element to get.
     */
    public float get(int i) {
        if (i < 0 || i >= idxAfterLast) {
            return missingEntries;
        }
        return elements[i];
    }
    
    /**
     * Sets the i'th entry of the vector to the given value.
     * @param i The index to set.
     * @param value The value to set.
     */
    public float set(int idx, float value) {
        int i = idx;
        idxAfterLast = Math.max(idxAfterLast, i + 1);
        ensureCapacity(idxAfterLast);
        float old = elements[i];
        elements[i] = value;
        return old;
    }

    public void add(int idx, float value) {
        int i = idx;
        idxAfterLast = Math.max(idxAfterLast, i + 1);
        ensureCapacity(idxAfterLast);
        elements[i] += value;
    }

    public void scale(float multiplier) {
        for (int i=0; i<idxAfterLast; i++) {
            elements[i] *= multiplier;
        }
    }

    @Override
    public float dot(float[] other) {
        int max = Math.min(idxAfterLast, other.length);
        float dot = 0;
        for (int i=0; i<max; i++) {
            dot += elements[i] * other[i];
        }
        return dot;
    }

    @Override
    public float dot(IntFloatVector y) {
        if (y instanceof IntFloatDenseVector){
            IntFloatDenseVector other = (IntFloatDenseVector) y;
            int max = Math.min(idxAfterLast, other.idxAfterLast);
            float dot = 0;
            for (int i=0; i<max; i++) {
                dot += elements[i] * y.get(i);
            }
            return dot;
        } else {
            return y.dot(this);
        }
    }

    @Override
    public void apply(FnIntFloatToFloat function) {
        for (int i=0; i<idxAfterLast; i++) {
            elements[i] = function.call(i, elements[i]);
        }
    }
    
    @Override
    public void iterate(FnIntFloatToVoid function) {
        for (int i=0; i<idxAfterLast; i++) {
            function.call(i, elements[i]);
        }
    }

    /** Updates this vector to be the entrywise sum of this vector with the other. */
    public void add(IntFloatVector other) {
        if (other instanceof IntFloatUnsortedVector) {
            IntFloatUnsortedVector vec = (IntFloatUnsortedVector) other;
            for (int i=0; i<vec.top; i++) {
                this.add(vec.idx[i], vec.vals[i]);
            }
        } else {
            //  TODO: Add special case for IntFloatDenseVector.
            other.iterate(new SparseBinaryOpApplier(this, new Lambda.FloatAdd()));
        }
    }
    
    /** Updates this vector to be the entrywise difference of this vector with the other. */
    public void subtract(IntFloatVector other) {
        if (other instanceof IntFloatUnsortedVector) {
            IntFloatUnsortedVector vec = (IntFloatUnsortedVector) other;
            for (int i=0; i<vec.top; i++) {
                this.add(vec.idx[i], -vec.vals[i]);
            }
        } else {
            // TODO: Add special case for IntFloatDenseVector.
            other.iterate(new SparseBinaryOpApplier(this, new Lambda.FloatSubtract()));
        }
    }
    
    /** Updates this vector to be the entrywise product (i.e. Hadamard product) of this vector with the other. */
    public void product(IntFloatVector other) {
        // TODO: Add special case for IntFloatDenseVector.
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
    public int lookupIndex(float value, float delta) {
        for (int i=0; i<elements.length; i++) {
            if (Primitives.equals(elements[i], value, delta)) {
                return i;
            }
        }
        return -1;
    }

    /** Gets a NEW array containing all the elements in this vector. */
    public float[] toNativeArray() {
        return Arrays.copyOf(elements, idxAfterLast);
    }
    
    /** Gets the INTERNAL representation of this vector. */
    public float[] getInternalElements() {
        return elements;
    }

    /**
     * Gets the number of explicit entries in this vector.
     * 
     * For a dense vector, this is just the size of the vector.
     * 
     * For a sparse vector, this is the number of entries which are explicitly
     * represented in the vector.
     * 
     * The contract of this method is that a call to this.apply(fn) will visit n
     * entries where n = this.getNumExplicitEntries().
     * 
     * @return The number of explicit entries.
     */
    public int getNumExplicitEntries() {
        return idxAfterLast;
    }
    
    /**
     * Gets the number of implicit entries.
     * 
     * For a dense vector, this is just the size of the vector.
     * 
     * For a sparse vector, this is index after the last explicit entry in the
     * vector. This corresponds to (1 + i) where i is the highest index
     * explicitly represented.
     * 
     * The contract of this method is that for any j >=
     * this.getNumImplicitEntries(), this.get(j) will return 0.
     * 
     * @return The number of implicit entries.
     */
    public int getNumImplicitEntries() {
        return idxAfterLast;
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
    public static float[] ensureCapacity(float[] elements, int size) {
        if (size > elements.length) {
            float[] tmp = new float[size*2];
            System.arraycopy(elements, 0, tmp, 0, elements.length);
            elements = tmp;
        }
        return elements;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("IntFloatDenseVector [");
        for (int i=0; i<idxAfterLast; i++) {
            sb.append(elements[i]);
            if (i < idxAfterLast - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public float infinityNorm() {
        float maxAbs = 0;
        for (int i=0; i<idxAfterLast; i++) {
            float abs = Math.abs(elements[i]);
            if (abs > maxAbs) {
                maxAbs = abs;
            }
        }
        return maxAbs;
    }
    
}
