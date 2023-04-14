package com.mycompany.app;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

class ParsingErrorExceptionException extends Exception {
}

public class JSONParser {

    static final int blockSize = 1024 * 20;

    static InputStream is;
    static byte[] buffer = new byte[blockSize * 10];
    static int bufferEnd = 0;
    static int cursor = 0;

    static void ensureSize(int size) throws IOException {
        while (size >= bufferEnd) {
            if (buffer.length - bufferEnd < blockSize) {

                byte[] newBuffer = new byte[buffer.length * 2];
                System.arraycopy(buffer, 0, newBuffer, 0, bufferEnd);
                buffer = newBuffer;
            }
            bufferEnd += is.read(buffer, bufferEnd, blockSize);
        }

    }

    static String readString() throws IOException, ParsingErrorExceptionException {
        skipTo((byte) '"');
        int start = cursor;

        while (true) {
            byte r = read();

            if (r == '"') {
                // return "";
                return new String(buffer, start, cursor - start - 1, StandardCharsets.US_ASCII);
            }
        }
    }

    static boolean isWhitespace(byte r) {
        return r == 0x20 || r == 0x0A || r == 0x09 || r == 0x0D;
    }

    static int readInt(int start, int end) {
        int res = 0;
        int pow = 1;
        for (int i = end - 1; i >= start; i--) {
            res += (buffer[i] - '0') * pow;
            pow *= 10;
        }

        return res;
    }

    static boolean compareKey(byte[] a) throws IOException {
        ensureSize(cursor + a.length);
        for (int i = 0; i < a.length; i++) {
            if (buffer[cursor + i] != a[i]) {
                return false;
            }
        }

        cursor += a.length;
        return true;
    }

    static void skipTo(byte c) throws IOException, ParsingErrorExceptionException {
        while (true) {
            byte r = read();
            if (r == c) {
                return;
            }
            if (!isWhitespace(r)) {
                throw new ParsingErrorExceptionException();
            }
        }
    }

    static byte skipToEther(byte a, byte b) throws IOException, ParsingErrorExceptionException {

        while (true) {
            byte r = read();
            if (r == a || r == b) {
                return r;
            }
            if (!isWhitespace(r)) {
                throw new ParsingErrorExceptionException();
            }
        }

    }

    static byte read() throws IOException {

        ensureSize(cursor);
        return buffer[cursor++];
    }

    static void reset(InputStream s) {
        is = s;
        bufferEnd = 0;
        cursor = 0;
    }

}

class TransactionsParser {

    static byte[] debitAccountBytes = "debitAccount\"".getBytes();
    static byte[] creditAccountBytes = "creditAccount\"".getBytes();
    static byte[] amountBytes = "amount\"".getBytes();

    static Transaction[] parse(InputStream s) throws IOException, ParsingErrorExceptionException {
        JSONParser.reset(s);
        return readArray();
    }

    static Transaction[] readArray() throws IOException, ParsingErrorExceptionException {
        JSONParser.skipTo((byte) '[');

        ArrayList<Transaction> result = new ArrayList<>();
        byte read = JSONParser.skipToEther((byte) '{', (byte) ']');
        if (read == ']') {
            return result.toArray(new Transaction[0]);
        } else if (read == '{') {
            result.add(parseObject());
        }

        while (true) {

            byte readInner = JSONParser.skipToEther((byte) ',', (byte) ']');
            if (readInner == ']') {
                return result.toArray(new Transaction[0]);
            } else if (readInner == ',') {
                JSONParser.skipTo((byte) '{');
                result.add(parseObject());
            }

        }
    }

    static long parseAmount() throws IOException, ParsingErrorExceptionException {

        int start = 0;

        while (true) {
            byte r = JSONParser.read();

            if (!JSONParser.isWhitespace(r)) {
                start = JSONParser.cursor - 1;
                break;
            }
        }

        while (true) {
            byte r = JSONParser.read();

            if (r == '}' || JSONParser.isWhitespace(r) || r == ',') {
                JSONParser.cursor--;
                String s = new String(JSONParser.buffer, start, JSONParser.cursor - start);
                return (long) (Float.parseFloat(s) * 100);
            }
        }
    }

    static void key(Transaction r) throws IOException, ParsingErrorExceptionException {
        JSONParser.skipTo((byte) '"');

        if (JSONParser.compareKey(creditAccountBytes)) {
            JSONParser.skipTo((byte) ':');
            r.creditAccount = JSONParser.readString();
            return;
        }

        if (JSONParser.compareKey(debitAccountBytes)) {
            JSONParser.skipTo((byte) ':');
            r.debitAccount = JSONParser.readString();
            return;
        }

        if (JSONParser.compareKey(amountBytes)) {
            JSONParser.skipTo((byte) ':');
            r.amount = parseAmount();
            return;
        }

    }

    private static Transaction parseObject() throws IOException, ParsingErrorExceptionException {

        Transaction r = new Transaction();

        TransactionsParser.key(r);
        JSONParser.skipTo((byte) ',');
        TransactionsParser.key(r);
        JSONParser.skipTo((byte) ',');
        TransactionsParser.key(r);

        JSONParser.skipTo((byte) '}');

        return r;
    }
}
