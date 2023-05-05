package com.mycompany.app;

import java.util.Comparator;

public class MyArray<T> {
    @SuppressWarnings("unchecked")
    T[] data = (T[]) new Object[64];
    int size = 0;

    void ensureSize() {
        if (data.length == size) {
            @SuppressWarnings("unchecked")
            T[] newData = (T[]) new Object[data.length * 2];
            System.arraycopy(data, 0, newData, 0, data.length);
            data = newData;
        }
    }

    void add(T toAdd) {
        ensureSize();
        data[size++] = toAdd;
    }

    int size() {
        return size;
    }

    T get(int i) {
        return data[i];
    }

    void sort(Comparator<T> comp) {
        Sort.doublePivotQuickSort(data, 0, size - 1, comp);
    }

    void clear() {
        size = 0;
    }

}
