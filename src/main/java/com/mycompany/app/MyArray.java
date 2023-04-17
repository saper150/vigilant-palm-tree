package com.mycompany.app;

import java.util.Arrays;
import java.util.Comparator;

public class MyArray<T> {
    T[] data = (T[]) new Object[64];
    int size = 0;

    void ensureSize() {
        if (data.length == size) {
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

    void sort(Comparator<? super T> c) {
        Arrays.sort(data, 0, size, c);
    }

    void clear() {
        size = 0;
    }

}
