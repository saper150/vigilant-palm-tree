package com.mycompany.app;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

class Transaction {

    public String debitAccount;
    public String creditAccount;
    public long amount;
}

class Account {
    public String account;
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
            sb.append(account.account);
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
        Arrays.sort(accounts, (a, b) -> a.account.compareTo(b.account));
        // System.out.println("sort time:" + (System.nanoTime() - t) / 1_000_000.0);

    }

    static Account[] processTransactions(Transaction[] transactions) {

        double tt = System.nanoTime();

        // HashMap<String, Account> map = new HashMap<>();

        MyHashMap map = new MyHashMap();

        for (Transaction t : transactions) {

            Account debit = map.get(t.debitAccount);
            Account credit = map.get(t.creditAccount);

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

        // System.out.println("process time:" + (System.nanoTime() - tt) / 1_000_000.0);

        sort(arr);
        return arr;

    }

    static String process(Transaction[] transactions)
            throws IOException, ParsingErrorExceptionException {
        // System.out.println("-------");

        // Transaction[] transactions = parse(s);
        // double t = System.nanoTime();
        Account[] c = processTransactions(transactions);
        // System.out.println("process time:" + (System.nanoTime() - t) / 1_000_000.0);

        // double t2 = System.nanoTime();
        String ss = serialize(c);
        // System.out.println("serialize time:" + (System.nanoTime() - t2) /
        // 1_000_000.0);

        return ss;
    }

}
