package com.mycompany.app;

public class MyHashMap {
    private static final int DEFAULT_CAPACITY = 2048;

    private Account[] table = new Account[DEFAULT_CAPACITY];
    private int tableSizeMinusOne = table.length - 1;
    private int size = 0;
    private final double loadFactor = 0.3;
    private int threshold = (int) (DEFAULT_CAPACITY * loadFactor);

    private void put(Account acc) {

        int index = hash(acc.account1, acc.account2);
        while (table[index] != null) {
            if (++index >= table.length) {
                index = 0;
            }
        }

        table[index] = acc;

    }

    public Account get(long key1, long key2) {

        int index = hash(key1, key2);

        while (true) {

            if (table[index] == null) {
                Account account = new Account();
                account.account1 = key1;
                account.account2 = key2;
                table[index] = account;
                if (++size >= threshold) {
                    resize();
                }
                return account;
            }

            if (table[index].account1 == key1 && table[index].account2 == key2) {
                return table[index];
            }

            if (++index >= table.length) {
                index = 0;
            }
        }
    }

    private int hash(long key1, long key2) {

        return (int) ((key1 ^ (key2 >> 33) ^ (key1 >> 32)) & tableSizeMinusOne);
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
        tableSizeMinusOne = table.length - 1;
        threshold = (int) (table.length * loadFactor);

        for (Account entry : oldTable) {
            if (entry != null) {
                put(entry);
            }
        }
    }

}