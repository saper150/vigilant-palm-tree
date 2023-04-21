package com.mycompany.app;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

class ParsingErrorExceptionException extends Exception {
}

public class JSONParser {

    static final int blockSize = 1024 * 500;

    static InputStream is;
    static byte[] buffer = new byte[1024 * 10_000];
    static int bufferEnd = 0;
    static int cursor = 0;

    static void ensureSize(int size) throws IOException {
        while (size >= bufferEnd) {
            if (buffer.length - bufferEnd <= blockSize) {

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

    static int toInt(int start, int end) {
        int res = 0;
        int pow = 1;
        for (int i = end - 1; i >= start; i--) {
            res += (buffer[i] - '0') * pow;
            pow *= 10;
        }

        return res;
    }

    static boolean isDigit(byte b) {
        return b >= '0' && b <= '9';
    }

    static int readNumber() throws IOException, ParsingErrorExceptionException {
        skipWhitespace();
        int start = cursor;
        while (isDigit(read())) {
        }

        int e = toInt(start - 1, cursor - 1);
        cursor--;
        return e;
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
