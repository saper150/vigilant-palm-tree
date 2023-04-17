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

    private static int toInt(int start, int end) {
        int res = 0;
        int pow = 1;
        for (int i = end - 1; i >= start; i--) {
            res += (buffer[i] - '0') * pow;
            pow *= 10;
        }

        return res;
    }

    static int readNumber() throws IOException, ParsingErrorExceptionException {
        byte[] b = buffer;
        skipWhitespace();
        int start = cursor;
        while (Character.isDigit((char) read())) {
        }

        int e = toInt(start - 1, cursor - 1);
        cursor--;
        return e;

        // while (true) {
        // byte r = GameParser.buffer[GameParser.cursor++];

        // if (r == ',' || r == '}') {
        // GameParser.cursor--;
        // // return toInt(start);
        // }
        // }
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

    static void skipWhitespace() throws IOException, ParsingErrorExceptionException {

        while (true) {
            byte r = read();
            if (!isWhitespace(r)) {
                break;
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

    private static byte[] debitAccountBytes = "debitAccount\"".getBytes();
    private static byte[] creditAccountBytes = "creditAccount\"".getBytes();
    private static byte[] amountBytes = "amount\"".getBytes();

    static Transaction[] parse(InputStream s) throws IOException, ParsingErrorExceptionException {
        JSONParser.reset(s);
        return readArray();
    }

    private static Transaction[] readArray() throws IOException, ParsingErrorExceptionException {
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

    private static void readAccount(Transaction tr, int ee) throws IOException, ParsingErrorExceptionException {

        JSONParser.skipTo((byte) '"');
        JSONParser.ensureSize(JSONParser.cursor + 26);

        switch (ee) {
            case 0:
                for (int i = 0; i < 16; i++) {
                    tr.credit1 = tr.credit1
                            | ((JSONParser.buffer[JSONParser.cursor + i] - '0') & 0xffffffffL) << (60 - (i * 4));
                }

                for (int i = 16; i < 26; i++) {
                    tr.credit2 = tr.credit2
                            | ((JSONParser.buffer[JSONParser.cursor + i] - '0') & 0xffffffffL) << (60 - (i * 4));
                }
                break;
            case 1:
                for (int i = 0; i < 16; i++) {
                    tr.debit1 = tr.debit1
                            | ((JSONParser.buffer[JSONParser.cursor + i] - '0') & 0xffffffffL) << (60 - (i * 4));
                }

                for (int i = 16; i < 26; i++) {
                    tr.debit2 = tr.debit2
                            | ((JSONParser.buffer[JSONParser.cursor + i] - '0') & 0xffffffffL) << (60 - ((i - 16) * 4));
                }

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

        while (true) {
            byte r = JSONParser.read();

            if (r == '}' || JSONParser.isWhitespace(r) || r == ',') {
                JSONParser.cursor--;
                String s = new String(JSONParser.buffer, start, JSONParser.cursor - start);
                return (long) (Float.parseFloat(s) * 100);
            }
        }
    }

    private static void key(Transaction r) throws IOException, ParsingErrorExceptionException {
        JSONParser.skipTo((byte) '"');

        if (JSONParser.compareKey(creditAccountBytes)) {
            JSONParser.skipTo((byte) ':');
            readAccount(r, 0);
            // r.creditAccount = "";
            return;
        }

        if (JSONParser.compareKey(debitAccountBytes)) {
            JSONParser.skipTo((byte) ':');
            readAccount(r, 1);
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

class GameParser {

    private static byte[] groupCountBytes = "groupCount\"".getBytes();
    private static byte[] clansBytes = "clans\"".getBytes();
    private static byte[] numberOfPlayersBytes = "numberOfPlayers\"".getBytes();
    private static byte[] pointsBytes = "points\"".getBytes();

    private static MyArray<Clan> result = new MyArray<Clan>();

    static Players parse(InputStream is) throws IOException, ParsingErrorExceptionException {
        JSONParser.reset(is);
        result.clear();

        Players players = new Players();

        JSONParser.skipTo((byte) '{');
        mainObjectKey(players);
        JSONParser.skipTo((byte) ',');
        mainObjectKey(players);

        return players;
    }

    private static void mainObjectKey(Players p) throws IOException, ParsingErrorExceptionException {
        JSONParser.skipTo((byte) '"');

        if (JSONParser.compareKey(groupCountBytes)) {
            JSONParser.skipTo((byte) ':');
            p.groupCount = JSONParser.readNumber();
            return;
        }

        if (JSONParser.compareKey(clansBytes)) {
            JSONParser.skipTo((byte) ':');
            p.clans = parseArray();
        }

    }

    private static MyArray<Clan> parseArray() throws IOException, ParsingErrorExceptionException {

        JSONParser.skipTo((byte) '[');

        byte read = JSONParser.skipToEther((byte) '{', (byte) ']');
        if (read == ']') {
            return result;
        } else if (read == '{') {
            result.add(parseObject());
        }

        while (true) {

            byte readInner = JSONParser.skipToEther((byte) ',', (byte) ']');
            if (readInner == ']') {
                return result;
            } else if (readInner == ',') {
                JSONParser.skipTo((byte) '{');
                result.add(parseObject());
            }

        }
    }

    private static Clan parseObject() throws IOException, ParsingErrorExceptionException {
        Clan r = new Clan();

        key(r);
        JSONParser.skipTo((byte) ',');
        key(r);

        JSONParser.skipTo((byte) '}');
        return r;
    }

    private static void key(Clan r) throws IOException, ParsingErrorExceptionException {
        JSONParser.skipTo((byte) '"');

        if (JSONParser.compareKey(numberOfPlayersBytes)) {
            JSONParser.skipTo((byte) ':');
            r.numberOfPlayers = JSONParser.readNumber();
            // r.creditAccount = "";
            return;
        }

        if (JSONParser.compareKey(pointsBytes)) {
            JSONParser.skipTo((byte) ':');
            r.points = JSONParser.readNumber();
            return;
        }

    }

}

class AtmParser {

    static byte[] regionKeyBytes = "region\"".getBytes();
    static byte[] requestTypeBytes = "requestType\"".getBytes();
    static byte[] atmIdKeyBytes = "atmId\"".getBytes();

    static byte[] STANDARDBytes = "STANDARD\"".getBytes();
    static byte[] PRIORITYBytes = "PRIORITY\"".getBytes();
    static byte[] SignalLowBytes = "SIGNAL_LOW\"".getBytes();
    static byte[] FAILURE_RESTARTBytes = "FAILURE_RESTART\"".getBytes();

    private static MyArray<Request> result = new MyArray<Request>();

    static void key(Request r, int i) throws IOException, ParsingErrorExceptionException {
        JSONParser.skipTo((byte) '"');

        if (JSONParser.compareKey(AtmParser.regionKeyBytes)) {
            JSONParser.skipTo((byte) ':');
            r.region = JSONParser.readNumber();
            return;
        }

        if (JSONParser.compareKey(AtmParser.requestTypeBytes)) {
            JSONParser.skipTo((byte) ':');
            JSONParser.skipTo((byte) '"');
            r.requestType = AtmParser.readRequestType();
            return;
        }

        if (JSONParser.compareKey(AtmParser.atmIdKeyBytes)) {
            JSONParser.skipTo((byte) ':');
            r.atmId = JSONParser.readNumber();
            return;
        }
    }

    static int readRequestType() throws ParsingErrorExceptionException, IOException {

        if (JSONParser.compareKey(AtmParser.STANDARDBytes)) {
            return 3;
        }

        if (JSONParser.compareKey(AtmParser.PRIORITYBytes)) {
            return 1;
        }

        if (JSONParser.compareKey(AtmParser.SignalLowBytes)) {
            return 2;
        }

        if (JSONParser.compareKey(AtmParser.FAILURE_RESTARTBytes)) {
            return 0;
        }

        throw new ParsingErrorExceptionException();

    }

    static Request parseObject() throws IOException, ParsingErrorExceptionException {

        Request r = new Request();

        JSONParser.skipTo((byte) '"');

        if (JSONParser.compareKey(AtmParser.regionKeyBytes)) {
            JSONParser.skipTo((byte) ':');
            r.region = JSONParser.readNumber();
        } else

        if (JSONParser.compareKey(AtmParser.requestTypeBytes)) {
            JSONParser.skipTo((byte) ':');
            JSONParser.skipTo((byte) '"');
            r.requestType = AtmParser.readRequestType();
        } else

        if (JSONParser.compareKey(AtmParser.atmIdKeyBytes)) {
            JSONParser.skipTo((byte) ':');
            r.atmId = JSONParser.readNumber();
        }

        // AtmmParser.key(r, 0);
        JSONParser.skipTo((byte) ',');

        JSONParser.skipTo((byte) '"');

        if (JSONParser.compareKey(AtmParser.requestTypeBytes)) {
            JSONParser.skipTo((byte) ':');
            JSONParser.skipTo((byte) '"');
            r.requestType = AtmParser.readRequestType();
        } else if (JSONParser.compareKey(AtmParser.regionKeyBytes)) {
            JSONParser.skipTo((byte) ':');
            r.region = JSONParser.readNumber();
        } else

        if (JSONParser.compareKey(AtmParser.atmIdKeyBytes)) {
            JSONParser.skipTo((byte) ':');
            r.atmId = JSONParser.readNumber();
        }

        // AtmmParser.key(r, 1);
        JSONParser.skipTo((byte) ',');

        JSONParser.skipTo((byte) '"');

        if (JSONParser.compareKey(AtmParser.atmIdKeyBytes)) {
            JSONParser.skipTo((byte) ':');
            r.atmId = JSONParser.readNumber();
        } else

        if (JSONParser.compareKey(AtmParser.regionKeyBytes)) {
            JSONParser.skipTo((byte) ':');
            r.region = JSONParser.readNumber();
        } else

        if (JSONParser.compareKey(AtmParser.requestTypeBytes)) {
            JSONParser.skipTo((byte) ':');
            JSONParser.skipTo((byte) '"');
            r.requestType = AtmParser.readRequestType();
        }

        // AtmmParser.key(r, 2);

        JSONParser.skipTo((byte) '}');

        return r;
    }

    static MyArray<Request> parse(InputStream is) throws IOException, ParsingErrorExceptionException {

        JSONParser.reset(is);
        result.clear();

        JSONParser.skipTo((byte) '[');

        byte read = JSONParser.skipToEther((byte) '{', (byte) ']');
        if (read == ']') {
            return result;
        } else if (read == '{') {
            result.add(parseObject());
        }

        while (true) {

            byte readInner = JSONParser.skipToEther((byte) ',', (byte) ']');
            if (readInner == ']') {
                return result;
            } else if (readInner == ',') {
                JSONParser.skipTo((byte) '{');
                result.add(parseObject());
            }

        }
    }

}