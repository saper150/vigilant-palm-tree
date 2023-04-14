package com.mycompany.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

class Request {
    public int region;
    public int requestType;
    public int atmId;

    public void setRequestType(String string) {
        if (string.equals("STANDARD")) {
            requestType = 3;
        }

        if (string.equals("SIGNAL_LOW")) {
            requestType = 2;
        }

        if (string.equals("PRIORITY")) {
            requestType = 1;
        }

        requestType = 0;
    }
}

class AtmParser {

    static byte[] buffer;
    static int cursor = 0;

    static byte[] regionKeyBytes = "region\"".getBytes();
    static byte[] requestTypeBytes = "requestType\"".getBytes();
    static byte[] atmIdKeyBytes = "atmId\"".getBytes();

    static byte[] STANDARDBytes = "STANDARD\"".getBytes();
    static byte[] PRIORITYBytes = "PRIORITY\"".getBytes();
    static byte[] SignalLowBytes = "SIGNAL_LOW\"".getBytes();
    static byte[] FAILURE_RESTARTBytes = "FAILURE_RESTART\"".getBytes();

    static String readString() throws IOException, ParsingErrorExceptionException {
        StringBuilder builder = new StringBuilder();

        while (true) {
            byte r = AtmParser.buffer[AtmParser.cursor++];

            if (r == '"') {
                return builder.toString();
            }
            builder.append((char) r);
        }
    }

    static int toInt(int start) {
        int res = 0;
        int pow = 1;
        for (int i = AtmParser.cursor - 1; i >= start; i--) {
            res += (AtmParser.buffer[i] - '0') * pow;
            pow *= 10;
        }

        return res;
    }

    static int readNumber() throws IOException, ParsingErrorExceptionException {
        int start;
        while (true) {
            byte r = AtmParser.buffer[AtmParser.cursor++];
            if (r != ' ') {
                AtmParser.cursor--;
                start = AtmParser.cursor;
                break;
            }
        }

        while (true) {
            byte r = AtmParser.buffer[AtmParser.cursor++];

            if (r == ',') {
                AtmParser.cursor--;
                return toInt(start);
            } else if (r == '}') {
                AtmParser.cursor--;
                int e = toInt(start);
                return e;
            }
        }

    }

    static boolean compareKey(byte[] a) {

        for (int i = 0; i < a.length; i++) {
            if (AtmParser.buffer[AtmParser.cursor + i] != a[i]) {
                return false;
            }
        }

        AtmParser.cursor += a.length;
        return true;
    }

    static int readRequestType() throws ParsingErrorExceptionException {

        if (AtmParser.compareKey(AtmParser.STANDARDBytes)) {
            return 3;
        }

        if (AtmParser.compareKey(AtmParser.PRIORITYBytes)) {
            return 1;
        }

        if (AtmParser.compareKey(AtmParser.SignalLowBytes)) {
            return 2;
        }

        if (AtmParser.compareKey(AtmParser.FAILURE_RESTARTBytes)) {
            return 0;
        }

        throw new ParsingErrorExceptionException();

    }

    static void key(Request r, int i) throws IOException, ParsingErrorExceptionException {
        AtmParser.skipTo((byte) '"');

        switch (i) {
            case 0:
                if (AtmParser.compareKey(AtmParser.regionKeyBytes)) {
                    AtmParser.skipTo((byte) ':');
                    r.region = readNumber();
                    return;
                }

                if (AtmParser.compareKey(AtmParser.requestTypeBytes)) {
                    AtmParser.skipTo((byte) ':');
                    AtmParser.skipTo((byte) '"');
                    r.requestType = AtmParser.readRequestType();
                    return;
                }

                if (AtmParser.compareKey(AtmParser.atmIdKeyBytes)) {
                    AtmParser.skipTo((byte) ':');
                    r.atmId = readNumber();
                    return;
                }
                break;
            case 1:
                if (AtmParser.compareKey(AtmParser.requestTypeBytes)) {
                    AtmParser.skipTo((byte) ':');
                    AtmParser.skipTo((byte) '"');
                    r.requestType = AtmParser.readRequestType();
                    return;
                }

                if (AtmParser.compareKey(AtmParser.regionKeyBytes)) {
                    AtmParser.skipTo((byte) ':');
                    r.region = readNumber();
                    return;
                }

                if (AtmParser.compareKey(AtmParser.atmIdKeyBytes)) {
                    AtmParser.skipTo((byte) ':');
                    r.atmId = readNumber();
                    return;
                }
                break;

            case 2:
                if (AtmParser.compareKey(AtmParser.atmIdKeyBytes)) {
                    AtmParser.skipTo((byte) ':');
                    r.atmId = readNumber();
                    return;
                }

                if (AtmParser.compareKey(AtmParser.regionKeyBytes)) {
                    AtmParser.skipTo((byte) ':');
                    r.region = readNumber();
                    return;
                }

                if (AtmParser.compareKey(AtmParser.requestTypeBytes)) {
                    AtmParser.skipTo((byte) ':');
                    AtmParser.skipTo((byte) '"');
                    r.requestType = AtmParser.readRequestType();
                    return;
                }
                break;
        }

    }

    static Request parseObject() throws IOException, ParsingErrorExceptionException {

        Request r = new Request();

        AtmParser.key(r, 0);
        AtmParser.skipTo((byte) ',');
        AtmParser.key(r, 1);
        AtmParser.skipTo((byte) ',');
        AtmParser.key(r, 2);

        AtmParser.skipTo((byte) '}');

        return r;
    }

    static void skipTo(byte c) throws IOException, ParsingErrorExceptionException {
        while (AtmParser.buffer[AtmParser.cursor++] != c) {
        }
    }

    static ArrayList<Request> parse() throws IOException, ParsingErrorExceptionException {
        ArrayList<Request> result = new ArrayList<>();

        AtmParser.skipTo((byte) '[');

        while (true) {
            int read = AtmParser.buffer[AtmParser.cursor++];

            if (read == ']') {
                return result;
            } else if (read == '{') {
                result.add(parseObject());
            }
        }
    }

}

public class AtmService {

    static ArrayList<Request>[] byRegion = new ArrayList[10000];
    static byte[] added = new byte[10000];
    static StringBuilder builder = new StringBuilder();

    public static void clear(byte[] array) {
        int len = array.length;

        if (len > 0) {
            array[0] = 0;
        }

        for (int i = 1; i < len; i += i) {
            System.arraycopy(array, 0, array, i, ((len - i) < i) ? (len - i) : i);
        }
    }

    static String process(ArrayList<Request> l) {

        Arrays.fill(AtmService.byRegion, null);
        AtmService.builder.setLength(0);
        builder.append('[');

        for (Request request : l) {
            if (AtmService.byRegion[request.region] == null) {
                AtmService.byRegion[request.region] = new ArrayList<>();
            }

            AtmService.byRegion[request.region].add(request);
        }

        for (int i = 1; i < AtmService.byRegion.length; i++) {
            ArrayList<Request> r = AtmService.byRegion[i];
            if (r == null) {
                break;
            }

            r.sort((a, b) -> {
                return a.requestType - b.requestType;
            });

            AtmService.clear(AtmService.added);
            for (Request rr : r) {
                if (AtmService.added[rr.atmId] != 0) {
                    continue;
                }

                AtmService.builder.append("{\"region\":");
                AtmService.builder.append(rr.region);
                AtmService.builder.append(",\"atmId\":");
                AtmService.builder.append(rr.atmId);
                AtmService.builder.append("},");

                AtmService.added[rr.atmId] = 1;
            }
        }

        builder.replace(builder.length() - 1, builder.length(), "]");

        return builder.toString();
    }

    static String transationList(InputStream body)
            throws IOException, ParsingErrorExceptionException {

        // long t = 0;

        AtmParser.buffer = body.readAllBytes();

        // for (int i = 0; i < 100; i++) {
        // long startTime = System.nanoTime();
        // Parser.cursor = 0;
        // ArrayList<Request> l = Parser.parse();
        // ArrayList<Response> result = Transaction.process(l);

        // long estimatedTime = System.nanoTime() - startTime;
        // t += estimatedTime;
        // }

        // System.out.println((t / 100) / 1000);

        AtmParser.cursor = 0;
        ArrayList<Request> l = AtmParser.parse();
        // Request[] l = new ObjectMapper().readValue(Parser.buffer, Request[].class);

        String result = AtmService.process(l);
        return result;

        // return new ObjectMapper().writeValueAsBytes(result);

    }

}
