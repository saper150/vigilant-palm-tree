package com.mycompany.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

class Request {
    static int getRegion(long request) {
        return (int) request;
    }

    static int getAtmId(long request) {
        return (int) (request >> 32 & 0b0011111111111111111111);
    }

    static int getRequestType(long request) {
        return (byte) ((request >> 62)) & 0b00000011;
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

    private static LongArray result = new LongArray();

    static long request = 0;

    static void key() throws IOException, ParsingErrorExceptionException {
        JSONParser.skipTo((byte) '"');

        if (JSONParser.compareKey(AtmParser.regionKeyBytes)) {
            JSONParser.skipTo((byte) ':');
            int region = JSONParser.readNumber();
            request = request | (((long) region) & 0xffffffffL);
            return;
        }

        if (JSONParser.compareKey(AtmParser.requestTypeBytes)) {
            JSONParser.skipTo((byte) ':');
            JSONParser.skipTo((byte) '"');
            request = request | ((((long) AtmParser.readRequestType()) & 0xffffffffL) << 62);
            return;
        }

        if (JSONParser.compareKey(AtmParser.atmIdKeyBytes)) {
            JSONParser.skipTo((byte) ':');
            int atmId = JSONParser.readNumber();
            request = request | (((long) atmId) << 32);
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

    static long parseObject() throws IOException, ParsingErrorExceptionException {
        request = 0;

        AtmParser.key();
        JSONParser.skipTo((byte) ',');
        AtmParser.key();
        JSONParser.skipTo((byte) ',');
        AtmParser.key();
        JSONParser.skipTo((byte) '}');

        return request;
    }

    static LongArray parse(InputStream is) throws IOException, ParsingErrorExceptionException {

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

public class AtmService {

    static final int MAX_SIZE = 10001;

    static LongArray[] MyArrayPool = new LongArray[MAX_SIZE];
    static int arrayPoolIndex = 0;

    static LongArray[] byRegions = new LongArray[MAX_SIZE];

    static final LongArray[] buckets = new LongArray[4];

    static byte[] added = new byte[MAX_SIZE];

    static final byte[] falseArray = new byte[MAX_SIZE];

    static {
        for (int i = 0; i < MAX_SIZE; i++) {
            MyArrayPool[i] = new LongArray();
        }

        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = new LongArray();
        }

    }

    public static void clearAdded() {
        System.arraycopy(falseArray, 0, added, 0, MAX_SIZE);
    }

    static void processBuckets() {

        clearAdded();
        for (int i = 0; i < buckets.length; i++) {
            LongArray bucket = buckets[i];

            for (int j = 0; j < bucket.size; j++) {
                Long request = bucket.get(j);

                int atmId = Request.getAtmId(request);
                int region = Request.getRegion(request);

                if (added[atmId] != 0) {
                    continue;
                }

                StaticBuilder.builder.append("{\"region\":");
                StaticBuilder.builder.append(region);
                StaticBuilder.builder.append(",\"atmId\":");
                StaticBuilder.builder.append(atmId);
                StaticBuilder.builder.append("},");
                added[atmId] = 1;

            }

        }
    }

    static String process(LongArray requests) {

        for (int i = 0; i < arrayPoolIndex; i++) {
            MyArrayPool[i].clear();
        }

        arrayPoolIndex = 0;

        Arrays.fill(byRegions, null);

        StaticBuilder.builder.setLength(0);

        StaticBuilder.builder.append('[');

        int maxRegion = 0;

        for (int i = 0; i < requests.size; i++) {

            long request = requests.get(i);
            int region = Request.getRegion(request);

            if (byRegions[region] == null) {
                byRegions[region] = MyArrayPool[arrayPoolIndex++];
            }

            maxRegion = Math.max(maxRegion, region);

            byRegions[region].add(request);
        }

        for (int i = 1; i < maxRegion + 1; i++) {
            LongArray byRegion = byRegions[i];

            if (byRegion == null) {
                continue;
            }

            for (int j = 0; j < buckets.length; j++) {
                buckets[j].clear();
            }

            for (int j = 0; j < byRegion.size; j++) {
                long request = byRegion.get(j);

                buckets[Request.getRequestType(request)].add(request);

            }

            processBuckets();

        }

        StaticBuilder.builder.replace(StaticBuilder.builder.length() - 1, StaticBuilder.builder.length(), "]");

        return StaticBuilder.builder.toString();
    }

}
