package com.mycompany.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

class Clan {
    static public int getNumberOfPlayers(long clan) {
        return ((int) (clan >> 32) & 0b11111111111100000000000000000000) >> (32 - 12);
    }

    static public int getPoints(long clan) {

        return ((int) (clan >> 32)) & 0b00000000000011111111111111111111;
    }

    static public int getDeletedIndex(long clan) {
        return (int) clan;
    }

}

class Players {
    public int groupCount;
    public LongArray clans;
}

class GameParser {

    private static byte[] groupCountBytes = "groupCount\"".getBytes();
    private static byte[] clansBytes = "clans\"".getBytes();
    private static byte[] numberOfPlayersBytes = "numberOfPlayers\"".getBytes();
    private static byte[] pointsBytes = "points\"".getBytes();

    private static LongArray result = new LongArray();

    private static long clan = 0;

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

    private static LongArray parseArray() throws IOException, ParsingErrorExceptionException {

        JSONParser.skipTo((byte) '[');

        byte read = JSONParser.skipToEther((byte) '{', (byte) ']');
        if (read == ']') {
            return result;
        } else if (read == '{') {
            long clan = parseObject();
            clan = clan | ((((long) result.size) & 0xffffffffL));

            result.add(clan);
        }

        while (true) {

            byte readInner = JSONParser.skipToEther((byte) ',', (byte) ']');
            if (readInner == ']') {
                return result;
            } else if (readInner == ',') {
                JSONParser.skipTo((byte) '{');
                long clan = parseObject();
                clan = clan | ((((long) result.size) & 0xffffffffL));
                result.add(clan);
            }

        }
    }

    private static long parseObject() throws IOException, ParsingErrorExceptionException {
        clan = 0;
        key();
        JSONParser.skipTo((byte) ',');
        key();
        JSONParser.skipTo((byte) '}');
        return clan;
    }

    private static void key() throws IOException, ParsingErrorExceptionException {
        JSONParser.skipTo((byte) '"');

        if (JSONParser.compareKey(numberOfPlayersBytes)) {
            JSONParser.skipTo((byte) ':');
            clan = clan | ((((long) JSONParser.readNumber()) & 0xffffffffL) << 64 - 12);
            return;
        }

        if (JSONParser.compareKey(pointsBytes)) {
            JSONParser.skipTo((byte) ':');
            clan = clan | ((((long) JSONParser.readNumber()) & 0xffffffffL) << 64 - (12 + 20));
            return;
        }

    }

}

public class Game {
    static ArrayList<Clan> result = new ArrayList<Clan>();

    static final float[] ratios = new float[] { 1.0f, 0.03f, 0.01f };
    static int[] breakpoints = new int[ratios.length];
    static int[] groupStarts = new int[ratios.length];
    static LongArray[] groups = new LongArray[ratios.length];
    static boolean[] deleted = new boolean[0];

    static {
        for (int i = 1; i < breakpoints.length; i++) {
            groups[i] = new LongArray();
        }

    }

    static long findClan(LongArray group, int start, int avalibleSpace) {
        for (int i = start; i < group.size(); i++) {
            long c = group.get(i);

            if (!deleted[Clan.getDeletedIndex(c)] && Clan.getNumberOfPlayers(c) <= avalibleSpace) {
                return c;
            }
        }

        return 0;
    }

    static void appendClan(long clan) {
        StaticBuilder.builder.append("{\"numberOfPlayers\":");
        StaticBuilder.builder.append(Clan.getNumberOfPlayers(clan));
        StaticBuilder.builder.append(",\"points\":");
        StaticBuilder.builder.append(Clan.getPoints(clan));
        StaticBuilder.builder.append("},");
    }

    public static String process(Players players) {

        if (players.clans.size() == 0) {
            return "[]";
        }

        if (players.clans.size() > deleted.length) {
            deleted = new boolean[Clan.getDeletedIndex(players.clans.get(players.clans.size() - 1)) + 1];
        }

        Arrays.fill(deleted, 0, players.clans.size(), false);

        StaticBuilder.builder.setLength(0);
        StaticBuilder.builder.append("[[");

        for (int i = 1; i < breakpoints.length; i++) {
            breakpoints[i] = (int) (players.groupCount * ratios[i]);
            groupStarts[i] = 0;
            groups[i].clear();
        }

        for (int i = 0; i < players.clans.size(); i++) {
            long clan = players.clans.get(i);
            for (int j = 1; j < ratios.length; j++) {
                if (Clan.getNumberOfPlayers(clan) <= breakpoints[j]) {
                    groups[j].add(clan);
                }
            }

        }

        groupStarts[0] = 0;
        groups[0] = players.clans;

        for (int i = 0; i < ratios.length; i++) {
            groups[i].sort((a, b) -> {
                int p = Clan.getPoints(b) - Clan.getPoints(a);
                if (p != 0) {
                    return p;
                }
                return Clan.getNumberOfPlayers(a) - Clan.getNumberOfPlayers(b);
            });
        }

        int enteredGroups = 0;

        while (enteredGroups < players.clans.size()) {
            int avalibleSpace = players.groupCount;

            while (avalibleSpace > 0) {
                int groupToSearchIndex = 0;

                for (int i = ratios.length - 1; i >= 0; i--) {
                    if (avalibleSpace <= breakpoints[i]) {
                        groupToSearchIndex = i;
                        break;
                    }
                }

                LongArray groupToSearch = groups[groupToSearchIndex];

                for (int i = groupStarts[groupToSearchIndex]; i < groupToSearch.size(); i++) {
                    if (deleted[Clan.getDeletedIndex(groupToSearch.get(i))]) {
                        groupStarts[groupToSearchIndex]++;
                    } else {
                        break;
                    }
                }

                long clan = findClan(groupToSearch, groupStarts[groupToSearchIndex], avalibleSpace);
                if (clan != 0) {
                    deleted[Clan.getDeletedIndex(clan)] = true;
                    appendClan(clan);
                    avalibleSpace -= Clan.getNumberOfPlayers(clan);
                    enteredGroups++;
                } else {
                    break;
                }

            }
            StaticBuilder.builder.replace(StaticBuilder.builder.length() - 1, StaticBuilder.builder.length(), "],[");

        }

        if (StaticBuilder.builder.length() <= 2) {
            return "[]";
        }

        StaticBuilder.builder.replace(StaticBuilder.builder.length() - 2, StaticBuilder.builder.length(), "]");
        return StaticBuilder.builder.toString();

    }

}
