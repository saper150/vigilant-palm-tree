package com.mycompany.app;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

class Account {

    long account1;
    long account2;

    public int debitCount;
    public int creditCount;
    public long balance;
}

class TransactionsParser {

    private static byte[] debitAccountBytes = "debitAccount\"".getBytes(StandardCharsets.US_ASCII);
    private static byte[] creditAccountBytes = "creditAccount\"".getBytes(StandardCharsets.US_ASCII);
    private static byte[] amountBytes = "amount\"".getBytes(StandardCharsets.US_ASCII);

    static private long account1;
    static private long account2;

    static long debit1;
    static long debit2;

    static long credit1;
    static long credit2;

    static long amount;

    static void parse(InputStream s) throws IOException, ParsingErrorExceptionException {
        JSONParser.reset(s);
        readArray();
    }

    private static void readArray() throws IOException, ParsingErrorExceptionException {
        JSONParser.skipTo((byte) '[');

        byte read = JSONParser.skipToEther((byte) '{', (byte) ']');
        if (read == ']') {
            return;
        } else if (read == '{') {
            parseObject();
        }

        while (true) {

            byte readInner = JSONParser.skipToEther((byte) ',', (byte) ']');
            if (readInner == ']') {
                return;
            } else if (readInner == ',') {
                JSONParser.skipTo((byte) '{');
                parseObject();
            }

        }
    }

    private static void readAccount() throws IOException, ParsingErrorExceptionException {

        JSONParser.skipTo((byte) '"');
        JSONParser.ensureSize(JSONParser.cursor + 26);

        account1 = 0;
        for (int i = 0; i < 16; i++) {
            account1 = account1
                    | ((JSONParser.buffer[JSONParser.cursor + i] - ('0')) & 0xffffffffL) << (60 - (i * 4));
        }

        account2 = 0;
        for (int i = 0; i < 10; i++) {
            account2 = account2
                    | ((JSONParser.buffer[JSONParser.cursor + i + 16] - ('0')) & 0xffffffffL) << (60 - (i * 4));
        }
        JSONParser.cursor += 27;

    }

    private static long parseAmount() throws IOException, ParsingErrorExceptionException {

        int start = 0;

        while (true) {
            byte r = JSONParser.read();

            if (!JSONParser.isWhitespace(r)) {
                start = JSONParser.cursor - 1;
                break;
            }
        }

        int dotPosition = 0;
        boolean useFallback = false;
        while (true) {
            byte r = JSONParser.read();

            if (r == '}' || JSONParser.isWhitespace(r) || r == ',') {
                JSONParser.cursor--;

                int fractionSize = JSONParser.cursor - dotPosition;

                if (fractionSize > 2 || fractionSize == 0) {
                    useFallback = true;
                }

                if (useFallback) {
                    String s = new String(JSONParser.buffer, start, JSONParser.cursor - start,
                            StandardCharsets.US_ASCII);
                    return (long) (Float.parseFloat(s) * 100);
                }

                if (dotPosition != 0) {
                    int whole = JSONParser.toInt(start, dotPosition - 1);
                    int fraction = JSONParser.toInt(dotPosition, JSONParser.cursor);

                    return whole * 100 + fraction * (1 + ((2 - fractionSize) * 9));

                } else {
                    return JSONParser.toInt(start, JSONParser.cursor);
                }
            }

            if (r == '.') {
                if (dotPosition != 0) {
                    useFallback = true;
                }
                dotPosition = JSONParser.cursor;
            }

            if (!JSONParser.isDigit(r) && r != '.') {
                useFallback = true;
            }

        }
    }

    private static void key() throws IOException, ParsingErrorExceptionException {
        JSONParser.skipTo((byte) '"');

        if (JSONParser.compareKey(creditAccountBytes)) {
            JSONParser.skipTo((byte) ':');
            readAccount();

            credit1 = account1;
            credit2 = account2;
            return;
        }

        if (JSONParser.compareKey(debitAccountBytes)) {
            JSONParser.skipTo((byte) ':');
            readAccount();
            debit1 = account1;
            debit2 = account2;
            return;
        }

        if (JSONParser.compareKey(amountBytes)) {
            JSONParser.skipTo((byte) ':');
            amount = parseAmount();
            return;
        }

        throw new ParsingErrorExceptionException();

    }

    private static void parseObject() throws IOException, ParsingErrorExceptionException {

        TransactionsParser.key();
        JSONParser.skipTo((byte) ',');
        TransactionsParser.key();
        JSONParser.skipTo((byte) ',');
        TransactionsParser.key();

        Transactions.addTransaction(credit1, credit2, debit1, debit2, amount);
        JSONParser.skipTo((byte) '}');

    }
}

public class Transactions {

    static MyHashMap map = new MyHashMap();

    static String serialize(MyArray<Account> acc) {
        if (acc.size == 0) {
            return "[]";
        }
        StaticBuilder.builder.setLength(0);
        StaticBuilder.builder.append('[');

        for (int i = 0; i < acc.size; i++) {

            Account account = acc.get(i);

            StaticBuilder.builder.append("{\"account\":\"");

            for (int j = 0; j < 16; j++) {
                StaticBuilder.builder.append((char) (((account.account1 >> (60 - j * 4)) & 0b00001111) + '0'));
            }
            for (int j = 0; j < 10; j++) {
                StaticBuilder.builder.append((char) (((account.account2 >> (60 - j * 4)) & 0b00001111) + '0'));
            }
            long whole = account.balance / 100;
            long fraction = Math.abs(account.balance) % 100;

            StaticBuilder.builder.append("\",\"debitCount\":");
            StaticBuilder.builder.append(account.debitCount);
            StaticBuilder.builder.append(",\"creditCount\":");
            StaticBuilder.builder.append(account.creditCount);
            StaticBuilder.builder.append(",\"balance\":");
            StaticBuilder.builder.append(whole);
            StaticBuilder.builder.append('.');
            if (fraction < 10) {
                StaticBuilder.builder.append('0');
            }
            StaticBuilder.builder.append(fraction);
            StaticBuilder.builder.append('}');
            StaticBuilder.builder.append(',');
        }
        StaticBuilder.builder.replace(StaticBuilder.builder.length() - 1, StaticBuilder.builder.length(), "]");
        return StaticBuilder.builder.toString();
    }

    static void sort(MyArray<Account> accounts) {
        accounts.sort((a, b) -> {
            int c = Long.compareUnsigned(a.account1, b.account1);
            if (c == 0) {
                return Long.compareUnsigned(a.account2, b.account2);
            }
            return c;
        });
    }

    static void addTransaction(long credit1, long credit2, long debit1, long debit2, long amount) {

        Account credit = map.get(credit1, credit2);
        Account debit = map.get(debit1, debit2);

        debit.balance -= (amount);
        debit.debitCount++;

        credit.balance += (amount);
        credit.creditCount++;

    }

    static String getResults() {
        MyArray<Account> arr = map.toArray();
        sort(arr);
        String result = serialize(arr);
        map = new MyHashMap();
        return result;
    }

}
