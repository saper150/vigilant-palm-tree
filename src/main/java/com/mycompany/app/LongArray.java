package com.mycompany.app;

public class LongArray {
    long[] data = new long[64];
    int size = 0;

    void ensureSize() {
        if (data.length == size) {
            long[] newData = new long[data.length * 2];
            System.arraycopy(data, 0, newData, 0, data.length);
            data = newData;
        }
    }

    void add(long toAdd) {
        ensureSize();
        data[size++] = toAdd;
    }

    int size() {
        return size;
    }

    long get(int i) {
        return data[i];
    }

    void clear() {
        size = 0;
    }

}
