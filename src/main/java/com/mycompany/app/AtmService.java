package com.mycompany.app;

import java.io.IOException;
import java.io.InputStream;
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

public class AtmService {

    static final int MAX_SIZE = 10001;

    static MyArray<Request>[] MyArrayPool = new MyArray[MAX_SIZE];
    static int arrayPoolIndex = 0;

    static MyArray<Request>[] byRegion = new MyArray[MAX_SIZE];

    static final MyArray<Request>[] buckets = new MyArray[4];

    static byte[] added = new byte[MAX_SIZE];

    static final byte[] falseArray = new byte[MAX_SIZE];

    static StringBuilder builder = new StringBuilder();

    static {
        for (int i = 0; i < MAX_SIZE; i++) {
            MyArrayPool[i] = new MyArray<>();
        }

        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = new MyArray<>();
        }

    }

    public static void clearAdded() {

        System.arraycopy(falseArray, 0, added, 0, MAX_SIZE);

        // for (int i = 0; i < arr.length; i++) {
        // arr[i] = 0;
        // }

        // int len = array.length;

        // if (len > 0) {
        // array[0] = 0;
        // }

        // for (int i = 1; i < len; i += i) {
        // System.arraycopy(array, 0, array, i, ((len - i) < i) ? (len - i) : i);
        // }
    }

    static String process(MyArray<Request> l) {

        for (int i = 0; i < arrayPoolIndex; i++) {
            MyArrayPool[i].clear();
        }

        arrayPoolIndex = 0;

        Arrays.fill(AtmService.byRegion, null);

        AtmService.builder.setLength(0);

        builder.append('[');

        int maxRegion = 0;

        for (int i = 0; i < l.size; i++) {
            Request request = l.get(i);
            if (AtmService.byRegion[request.region] == null) {
                AtmService.byRegion[request.region] = MyArrayPool[arrayPoolIndex++];
            }
            maxRegion = Math.max(maxRegion, request.region);

            AtmService.byRegion[request.region].add(request);
        }

        for (int i = 1; i < maxRegion + 1; i++) {
            MyArray<Request> byRegion = AtmService.byRegion[i];
            if (byRegion == null) {
                continue;
            }

            for (int j = 0; j < buckets.length; j++) {
                buckets[j].clear();
            }

            for (int j = 0; j < byRegion.size; j++) {
                Request request = byRegion.get(j);
                buckets[request.requestType].add(request);

            }

            // byRegion.sort((a, b) -> {
            // return a.requestType - b.requestType;
            // });
            AtmService.clearAdded();

            for (MyArray<Request> bucket : buckets) {
                for (int j = 0; j < bucket.size; j++) {

                    Request rr = bucket.get(j);

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

        }

        builder.replace(builder.length() - 1, builder.length(), "]");

        return builder.toString();
    }

}
