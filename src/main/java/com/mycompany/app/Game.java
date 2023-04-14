package com.mycompany.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import com.fasterxml.jackson.core.JsonProcessingException;

class GameParser {

    static byte[] buffer;
    static int cursor = 0;

    static byte[] groupCountBytes = "groupCount\"".getBytes();
    static byte[] clansBytes = "clans\"".getBytes();
    static byte[] numberOfPlayersBytes = "numberOfPlayers\"".getBytes();

    static byte[] pointsBytes = "points\"".getBytes();

    static String readString() throws IOException, ParsingErrorExceptionException {
        StringBuilder builder = new StringBuilder();

        while (true) {
            byte r = GameParser.buffer[GameParser.cursor++];

            if (r == '"') {
                return builder.toString();
            }
            builder.append((char) r);
        }
    }

    static int toInt(int start) {
        int res = 0;
        int pow = 1;
        for (int i = GameParser.cursor - 1; i >= start; i--) {
            res += (GameParser.buffer[i] - '0') * pow;
            pow *= 10;
        }

        return res;
    }

    static int readNumber() throws IOException, ParsingErrorExceptionException {
        int start;
        while (true) {
            byte r = GameParser.buffer[GameParser.cursor++];
            if (r != ' ') {
                GameParser.cursor--;
                start = GameParser.cursor;
                break;
            }
        }

        while (true) {
            byte r = GameParser.buffer[GameParser.cursor++];

            if (r == ',' || r == '}') {
                GameParser.cursor--;
                return toInt(start);
            }
        }
    }

    static boolean compareKey(byte[] a) {

        for (int i = 0; i < a.length; i++) {
            if (GameParser.buffer[GameParser.cursor + i] != a[i]) {
                return false;
            }
        }

        GameParser.cursor += a.length;
        return true;
    }

    static Clan parseObject() throws IOException,
            ParsingErrorExceptionException {

        Clan r = new Clan();
        objectKey(r);
        GameParser.skipTo((byte) ',');
        objectKey(r);

        GameParser.skipTo((byte) '}');

        return r;
    }

    static void skipTo(byte c) throws IOException, ParsingErrorExceptionException {
        while (GameParser.buffer[GameParser.cursor++] != c) {
        }
    }

    static Clan[] parseArray() throws IOException, ParsingErrorExceptionException {

        ArrayList<Clan> result = new ArrayList<>();

        while (true) {
            int read = GameParser.buffer[GameParser.cursor++];

            if (read == ']') {
                return result.toArray(new Clan[0]);
            } else if (read == '{') {
                result.add(parseObject());
            }
        }
    }

    static void objectKey(Clan c) throws IOException, ParsingErrorExceptionException {
        GameParser.skipTo((byte) '"');

        if (GameParser.compareKey(GameParser.numberOfPlayersBytes)) {
            GameParser.skipTo((byte) ':');
            c.numberOfPlayers = readNumber();
            return;
        }

        if (GameParser.compareKey(GameParser.pointsBytes)) {
            GameParser.skipTo((byte) ':');
            c.points = readNumber();
        }

    }

    static void mainObjectKey(Players p) throws IOException, ParsingErrorExceptionException {
        GameParser.skipTo((byte) '"');

        if (GameParser.compareKey(GameParser.groupCountBytes)) {
            GameParser.skipTo((byte) ':');
            p.groupCount = readNumber();
            return;
        }

        if (GameParser.compareKey(GameParser.clansBytes)) {
            GameParser.skipTo((byte) ':');
            p.clans = parseArray();
        }

    }

    static Players parse() throws IOException, ParsingErrorExceptionException {

        cursor = 0;

        Players p = new Players();

        GameParser.skipTo((byte) '{');
        mainObjectKey(p);
        GameParser.skipTo((byte) ',');
        mainObjectKey(p);

        return p;
    }

}

class Clan {
    public int numberOfPlayers;
    public int points;
}

class Players {

    public int groupCount;
    public Clan[] clans;

}

public class Game {

    static StringBuilder builder = new StringBuilder();
    static Clan[] result = new Clan[0];

    public static String process(Players l) throws JsonProcessingException {

        // long startTime = System.nanoTime();
        if (result.length < l.clans.length * 2) {
            result = new Clan[l.clans.length * 2];
        }

        int resultSize = 0;

        Arrays.sort(l.clans, (a, b) -> {
            int p = b.points - a.points;
            if (p != 0) {
                return p;
            }
            return a.numberOfPlayers - b.numberOfPlayers;
        });

        int enteredGroups = 0;
        int start = 0;

        while (enteredGroups < l.clans.length) {

            int avalibleSpace = l.groupCount;

            while (l.clans[start] == null) {
                start++;
            }

            for (int i = start; i < l.clans.length; i++) {

                Clan current = l.clans[i];

                if (current != null && current.numberOfPlayers <= avalibleSpace) {
                    avalibleSpace -= current.numberOfPlayers;
                    result[resultSize++] = current;
                    enteredGroups++;
                    l.clans[i] = null;

                    if (avalibleSpace == 0) {
                        break;
                    }
                }
            }

            result[resultSize++] = null;

        }

        String r = serialize(result, resultSize);

        // System.out.println((System.nanoTime() - startTime) / 1000);
        // builder.replace(builder.length() - 2, builder.length(), "]");
        return r;

    }

    static String serialize(Clan[] c, int size) throws JsonProcessingException {

        builder.setLength(0);

        if (size == 0) {
            return "[]";
        }

        builder.append("[[");

        for (int i = 0; i < size; i++) {
            Clan clan = c[i];
            if (clan == null) {
                builder.replace(builder.length() - 1, builder.length(), "],[");
            } else {
                builder.append("{\"numberOfPlayers\":");
                builder.append(clan.numberOfPlayers);
                builder.append(",\"points\":");
                builder.append(clan.points);
                builder.append("},");
            }
        }

        builder.replace(builder.length() - 2, builder.length(), "]");
        return builder.toString();

    }

    public static String getGroups(InputStream body) throws IOException, ParsingErrorExceptionException {

        GameParser.buffer = body.readAllBytes();

        // long t = System.nanoTime();
        Players l = GameParser.parse();

        // System.out.println((System.nanoTime() - t) / 1000);

        // Players l = new ObjectMapper().readValue(buffer, Players.class);
        return process(l);

    }

}
