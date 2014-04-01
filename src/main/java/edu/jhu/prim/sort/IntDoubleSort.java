package edu.jhu.prim.sort;

import edu.jhu.prim.arrays.DoubleArrays;
import edu.jhu.prim.arrays.IntArrays;

public class IntDoubleSort {

    public IntDoubleSort() {
        // private constructor
    }

    /* ------------------- Ints and Doubles --------------- */
    
    /**
     * Performs an in-place quick sort on values. All the sorting operations on values
     * are mirrored in index. Sorts in descending order.
     */
    public static void sortValuesDesc(double[] values, int[] index) {
        DoubleArrays.scale(values, (double) -1);
        sortValuesAsc(values, index);
        DoubleArrays.scale(values, (double) -1);
    }
    
    /**
     * Performs an in-place quick sort on values. All the sorting operations on values
     * are mirrored in index. Sorts in ascending order.
     */
    public static void sortValuesAsc(double[] values, int[] index) {
        quicksortValues(values, index, 0, index.length - 1);
    }

    private static void quicksortValues(double[] array, int[] index, int left, int right) {
        if (left < right) {
            // Choose a pivot index.
            // --> Here we choose the rightmost element which does the least
            // amount of work if the array is already sorted.
            int pivotIndex = right;
            // Partition the array so that everything less than
            // values[pivotIndex] is on the left of pivotNewIndex and everything
            // greater than or equal is on the right.
            int pivotNewIndex = partitionValues(array, index, left, right, pivotIndex);
            // Recurse on the left and right sides.
            quicksortValues(array, index, left, pivotNewIndex - 1);
            quicksortValues(array, index, pivotNewIndex + 1, right);
        }
    }
    
    private static int partitionValues(double[] array, int[] index, int left, int right, int pivotIndex) {
        double pivotValue = array[pivotIndex];
        // Move the pivot value to the rightmost position.
        swap(array, index, pivotIndex, right);
        // For each position between left and right, swap all the values less
        // than or equal to the pivot value to the left side.
        int storeIndex = left;
        for (int i=left; i<right; i++) {
            if (array[i] <= pivotValue) {
                swap(array, index, i, storeIndex);
                storeIndex++;
            }
        }
        // Move the pivot value back to the split point.
        swap(array, index, storeIndex, right);
        return storeIndex;
    }

    /**
     * Performs an in-place quick sort on {@code index}. All the sorting operations on {@code index}
     * are mirrored in {@code values}.
     * Sorts in descending order.
     */
    public static void sortIndexDesc(int[] index, double[] values) {
        IntArrays.scale(index, (int) -1);
        sortIndexAsc(index, values);
        IntArrays.scale(index, (int) -1);
    }
    
    /**
     * Performs an in-place quick sort on {@code index} on the positions up to but not
     * including {@code top}. All the sorting operations on {@code index}
     * are mirrored in {@code values}.
     * Sorts in descending order.
     */
    public static void sortIndexDesc(int[] index, double[] values, int top) {
        IntArrays.scale(index, (int) -1);
        sortIndexAsc(index, values, top - 1);
        IntArrays.scale(index, (int) -1);
    }
    
    /**
     * Performs an in-place quick sort on {@code index}. All the sorting operations on {@code index}
     * are mirrored in {@code values}.
     * Sorts in ascending order.
     */
    public static void sortIndexAsc(int[] index, double[] values) {
        quicksortIndex(index, values, 0, index.length - 1);
    }

    /**
     * Performs an in-place quick sort on {@code index} on the positions up to but not
     * including {@code top}. All the sorting operations on {@code index} are mirrored in {@code values}.
     * Sorts in ascending order.
     * @return {@code index} - sorted.
     */
    public static void sortIndexAsc(int[] index, double[] values, int top) {
        assert top <= index.length;
        quicksortIndex(index, values, 0, top - 1);
    }

    private static void quicksortIndex(int[] array, double[] values, int left, int right) {
        if (left < right) {
            // Choose a pivot index.
            // --> Here we choose the rightmost element which does the least
            // amount of work if the array is already sorted.
            int pivotIndex = right;
            // Partition the array so that everything less than
            // values[pivotIndex] is on the left of pivotNewIndex and everything
            // greater than or equal is on the right.
            int pivotNewIndex = partitionIndex(array, values, left, right, pivotIndex);
            // Recurse on the left and right sides.
            quicksortIndex(array, values, left, pivotNewIndex - 1);
            quicksortIndex(array, values, pivotNewIndex + 1, right);
        }
    }
    
    private static int partitionIndex(int[] array, double[] values, int left, int right, int pivotIndex) {
        int pivotValue = array[pivotIndex];
        // Move the pivot value to the rightmost position.
        swap(values, array, pivotIndex, right);
        // For each position between left and right, swap all the values less
        // than or equal to the pivot value to the left side.
        int storeIndex = left;
        for (int i=left; i<right; i++) {
            if (array[i] <= pivotValue) {
                swap(values, array, i, storeIndex);
                storeIndex++;
            }
        }
        // Move the pivot value back to the split point.
        swap(values, array, storeIndex, right);
        return storeIndex;
    }
        
    /**
     * Swaps the elements at positions i and j in both the values and index array, which must be the same length.
     * @param values An array of values.
     * @param index An array of indices.
     * @param i The position of the first element to swap.
     * @param j The position of the second element to swap.
     */
    private static void swap(double[] values, int[] index, int i, int j) {
        swap(values, i, j);
        swap(index, i, j);
    }
    
    /* ----------------------------------------------------- */

    /**
     * Swaps the elements at positions i and j.
     */
    private static void swap(double[] array, int i, int j) {
        double valAtI = array[i];
        array[i] = array[j];
        array[j] = valAtI;
    }

    /*  */
    
    /**
     * Swaps the elements at positions i and j.
     */
    private static void swap(int[] array, int i, int j) {
        int valAtI = array[i];
        array[i] = array[j];
        array[j] = valAtI;
    }

    /*  */

    /**
     * Gets an array where array[i] = i.
     * @param values The length of the index array will be values.length.
     * @return The new index array.
     */
    public static int[] getIntIndexArray(double[] values) {
        return getIntIndexArray(values.length);
    }
    
    /**
     * Gets an array where array[i] = i.
     * @param length The length of the array.
     * @return The new index array.
     */
    public static int[] getIntIndexArray(int length) {
        int[] index = new int[length];
        for (int i=0; i<index.length; i++) {
            // TODO: This should maybe be a safe cast for the benefit of non-IntDouble classes.
            index[i] = (int) i;
        }
        return index;
    }

}
