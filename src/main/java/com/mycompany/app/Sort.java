package com.mycompany.app;

import java.util.Comparator;

interface LongComparator {

    int compare(long a, long b);

}

class Sort {

    private static final int MAX_INSERTION_SORT_SIZE = 17;

    static <T> void doublePivotQuickSort(T[] A, int left, int right, Comparator<T> comp) {

        int length = right - left + 1;
        if (length < MAX_INSERTION_SORT_SIZE) {
            insertionSort(A, left, right, comp);
            return;
        }

        if (right > left) {
            // Choose outermost elements as pivots
            if (comp.compare(A[right], A[left]) < 0)
                swap(A, left, right);

            T p = A[left];
            T q = A[right];

            // Partition A according to invariant below
            int l = left + 1, g = right - 1, k = l;
            while (k <= g) {
                if (comp.compare(A[k], p) < 0) {
                    swap(A, k, l);
                    ++l;
                } else if (comp.compare(q, A[k]) <= 0) {
                    while (comp.compare(q, A[g]) < 0 && k < g)
                        --g;
                    swap(A, k, g);
                    --g;
                    if (comp.compare(A[k], p) < 0) {
                        swap(A, k, l);
                        ++l;
                    }
                }
                ++k;
            }
            --l;
            ++g;

            // Swap pivots to final place
            swap(A, left, l);
            swap(A, right, g);

            // Recursively sort partitions
            doublePivotQuickSort(A, left, l - 1, comp);
            doublePivotQuickSort(A, l + 1, g - 1, comp);
            doublePivotQuickSort(A, g + 1, right, comp);
        }
    }

    static <T> void swap(T[] A, int i, int j) {
        final T tmp = A[i];
        A[i] = A[j];
        A[j] = tmp;
    }

    private static <T> void insertionSort(T[] a, int low, int high, Comparator<T> comp) {

        for (int i = low, j = i; i < high; j = ++i) {
            T ai = a[i + 1];
            while (comp.compare(ai, a[j]) < 0) {
                a[j + 1] = a[j];
                if (j-- == low) {
                    break;
                }
            }
            a[j + 1] = ai;
        }
    }

}

class LongSort {

    private static final int MAX_INSERTION_SORT_SIZE = 16;

    static void doublePivotQuickSort(long[] A, int left, int right, LongComparator comp) {

        int length = right - left + 1;

        if (length <= MAX_INSERTION_SORT_SIZE) {
            insertionSort(A, left, right, comp);
            return;
        }

        if (right > left) {
            // Choose outermost elements as pivots
            if (comp.compare(A[right], A[left]) < 0)
                swap(A, left, right);

            long p = A[left];
            long q = A[right];

            // Partition A according to invariant below
            int l = left + 1, g = right - 1, k = l;
            while (k <= g) {
                if (comp.compare(A[k], p) < 0) {
                    swap(A, k, l);
                    ++l;
                } else if (comp.compare(q, A[k]) <= 0) {
                    while (comp.compare(q, A[g]) < 0 && k < g)
                        --g;
                    swap(A, k, g);
                    --g;
                    if (comp.compare(A[k], p) < 0) {
                        swap(A, k, l);
                        ++l;
                    }
                }
                ++k;
            }
            --l;
            ++g;

            // Swap pivots to final place
            swap(A, left, l);
            swap(A, right, g);

            // Recursively sort partitions
            doublePivotQuickSort(A, left, l - 1, comp);
            doublePivotQuickSort(A, l + 1, g - 1, comp);
            doublePivotQuickSort(A, g + 1, right, comp);
        }
    }

    static void swap(long[] A, int i, int j) {
        final long tmp = A[i];
        A[i] = A[j];
        A[j] = tmp;
    }

    private static void insertionSort(long[] a, int low, int high, LongComparator comp) {

        for (int i = low, j = i; i < high; j = ++i) {
            long ai = a[i + 1];
            while (comp.compare(ai, a[j]) < 0) {
                a[j + 1] = a[j];
                if (j-- == low) {
                    break;
                }
            }
            a[j + 1] = ai;
        }
    }

}
