package com.mycompany.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import com.fasterxml.jackson.core.JsonProcessingException;

class Clan {
    public int numberOfPlayers;
    public int points;
    boolean deleted = false;
}

class Players {

    public int groupCount;
    public MyArray<Clan> clans;

}

public class Game {

    static StringBuilder builder = new StringBuilder();
    static ArrayList<Clan> result = new ArrayList<Clan>();

    static final float[] ratios = new float[] { 1.0f, 0.05f, 0.02f, 0.01f };
    static int[] breakpoints = new int[ratios.length];
    static int[] groupStarts = new int[ratios.length];
    static MyArray<Clan>[] groups = new MyArray[ratios.length];

    static {
        for (int i = 1; i < breakpoints.length; i++) {
            groups[i] = new MyArray<>();
        }

    }

    static Clan findClan(MyArray<Clan> group, int start, int avalibleSpace) {
        for (int i = start; i < group.size(); i++) {
            Clan c = group.get(i);

            if (!c.deleted && c.numberOfPlayers <= avalibleSpace) {
                return c;
            }
        }
        return null;
    }

    public static String process(Players players) throws JsonProcessingException {

        builder.setLength(0);
        builder.append("[[");

        for (int i = 1; i < breakpoints.length; i++) {
            breakpoints[i] = (int) (players.groupCount * ratios[i]);
            groupStarts[i] = 0;
            groups[i].clear();
        }

        for (int i = 0; i < players.clans.size(); i++) {
            Clan clan = players.clans.get(i);
            for (int j = 1; j < ratios.length; j++) {
                if (clan.numberOfPlayers <= breakpoints[j]) {
                    groups[j].add(clan);
                }
            }

        }

        groupStarts[0] = 0;
        groups[0] = players.clans;

        for (int i = 0; i < ratios.length; i++) {
            SortClans.sort(groups[i]);

            // groups[i].sort((a, b) -> {
            // int p = b.points - a.points;
            // if (p != 0) {
            // return p;
            // }
            // return a.numberOfPlayers - b.numberOfPlayers;
            // });
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

                MyArray<Clan> groupToSearch = groups[groupToSearchIndex];

                for (int i = groupStarts[groupToSearchIndex]; i < groupToSearch.size(); i++) {
                    if (groupToSearch.get(i).deleted) {
                        groupStarts[groupToSearchIndex]++;
                    } else {
                        break;
                    }
                }

                Clan c = findClan(groupToSearch, groupStarts[groupToSearchIndex], avalibleSpace);
                if (c != null) {
                    c.deleted = true;

                    builder.append("{\"numberOfPlayers\":");
                    builder.append(c.numberOfPlayers);
                    builder.append(",\"points\":");
                    builder.append(c.points);
                    builder.append("},");

                    avalibleSpace -= c.numberOfPlayers;
                    enteredGroups++;
                } else {
                    break;
                }

            }
            builder.replace(builder.length() - 2, builder.length(), "]");

            // result.add(null);

        }

        if (builder.length() <= 2) {
            return "[]";
        }

        builder.replace(builder.length() - 2, builder.length(), "]");
        return builder.toString();

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

}
