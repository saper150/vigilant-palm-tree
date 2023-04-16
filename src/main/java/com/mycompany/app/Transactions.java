package com.mycompany.app;

import java.io.IOException;
import java.util.Arrays;

class Transaction {
    long debit1;
    long debit2;

    long credit1;
    long credit2;

    public long amount;
}

class Account {

    long account1;
    long account2;

    public int debitCount;
    public int creditCount;
    public long balance;
}

public class Transactions {

    static String serialize(Account[] acc) {

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (Account account : acc) {

            sb.append("{\"account\":\"");

            for (int i = 0; i < 16; i++) {
                sb.append((char) (((account.account1 >> (60 - i * 4)) & 0b00001111) + '0'));
            }
            for (int i = 0; i < 10; i++) {
                sb.append((char) (((account.account2 >> (60 - i * 4)) & 0b00001111) + '0'));
            }

            sb.append("\",\"debitCount\":");
            sb.append(account.debitCount);
            sb.append(",\"creditCount\":");
            sb.append(account.creditCount);
            sb.append(",\"balance\":");
            sb.append(((float) account.balance) / 100);
            sb.append('}');
            sb.append(',');
        }
        sb.replace(sb.length() - 1, sb.length(), "]");
        return sb.toString();
    }

    static void sort(Account[] accounts) {
        // double t = System.nanoTime();
        Arrays.sort(accounts, (a, b) -> {

            int c = Long.compareUnsigned(a.account1, b.account1);
            if (c == 0) {
                return Long.compareUnsigned(a.account2, b.account2);
            }
            return c;
        });
        // System.out.println("sort time:" + (System.nanoTime() - t) / 1_000_000.0);

    }

    static Account[] processTransactions(Transaction[] transactions) {

        double tt = System.nanoTime();

        // HashMap<String, Account> map = new HashMap<>();

        MyHashMap map = new MyHashMap();

        for (Transaction t : transactions) {

            Account debit = map.get(t.debit1, t.debit2);
            Account credit = map.get(t.credit1, t.credit2);

            // Account debit = map.computeIfAbsent(t.debitAccount, (k) -> {
            // Account a = new Account();
            // a.account = k;
            // return a;
            // });
            // Account credit = map.computeIfAbsent(t.creditAccount, (k) -> {
            // Account a = new Account();
            // a.account = k;
            // return a;
            // });

            debit.balance -= (t.amount);
            debit.debitCount++;

            credit.balance += (t.amount);
            credit.creditCount++;

        }

        // Account[] arr = map.values().toArray(new Account[0]);

        Account[] arr = map.toArray();
        // System.out.println(map.collisions);
        // System.out.println("process time:" + (System.nanoTime() - tt) / 1_000_000.0);

        sort(arr);
        return arr;

    }

    static String process(Transaction[] transactions)
            throws IOException, ParsingErrorExceptionException {

        Account[] c = processTransactions(transactions);

        String ss = serialize(c);

        return ss;
    }

}
