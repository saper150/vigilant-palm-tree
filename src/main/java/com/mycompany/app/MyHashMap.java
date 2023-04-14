package com.mycompany.app;

public class MyHashMap {
    private static final int DEFAULT_CAPACITY = 128;
    private static final double DEFAULT_LOAD_FACTOR = 0.3;

    private Account[] table;
    private int size;
    private double loadFactor;
    private int threshold;

    public MyHashMap() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public MyHashMap(int initialCapacity, double loadFactor) {
        table = new Account[initialCapacity];
        size = 0;
        this.loadFactor = loadFactor;
        threshold = (int) (initialCapacity * loadFactor);
    }

    private void put(Account acc) {

        int index = hash(acc.account);
        while (table[index] != null) {
            if (++index >= table.length) {
                index = 0;
            }
        }

        table[index] = acc;

    }

    public Account get(String key) {
        int index = hash(key);
        while (table[index] != null && !table[index].account.equals(key)) {

            if (++index >= table.length) {
                index = 0;
            }
        }

        if (table[index] != null) {
            return table[index];
        } else {
            Account account = new Account();
            account.account = key;
            table[index] = account;
            if (++size >= threshold) {
                resize();
            }
            return account;

        }
    }

    private int hash(String key) {
        return key.hashCode() & (table.length - 1);
        // int hash = 0;
        // for (int i = 0; i < key.length(); i++) {
        // hash = hash * 31 + key.charAt(i);
        // }
        // return hash & (table.length - 1);
    }

    public Account[] toArray() {
        Account[] res = new Account[size];
        int i = 0;
        for (Account account : table) {
            if (account != null) {
                res[i++] = account;
            }
        }
        return res;
    }

    private void resize() {
        Account[] oldTable = table;
        table = new Account[table.length * 2];
        threshold = (int) (table.length * loadFactor);

        for (Account entry : oldTable) {
            if (entry != null) {
                put(entry);
            }
        }
    }

}