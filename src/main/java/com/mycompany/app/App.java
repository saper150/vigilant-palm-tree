package com.mycompany.app;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.annotations.*;

/**
 * Hello world!
 *
 */

@State(Scope.Thread)
public class App {

    static String s = """
            [
            {
                "debitAccount": "32309111922661937852684864",
                "creditAccount": "06105023389842834748547303",
                "amount": 10.90
            },
            {
                "debitAccount": "31074318698137062235845814",
                "creditAccount": "66105036543749403346524547",
                "amount": 200.90
            },
            {
                "debitAccount": "66105036543749403346524547",
                "creditAccount": "32309111922661937852684864",
                "amount": 50.10
            }
            ]
            """;
    private static Transaction[] ee;

    public static void main(String[] args) throws IOException, ParsingErrorExceptionException, RunnerException {

        Options opt = new OptionsBuilder()
                .include(App.class.getSimpleName())
                .forks(1)
                .warmupIterations(0)
                .threads(1)
                .mode(Mode.AverageTime)
                .measurementIterations(3)
                .timeUnit(TimeUnit.MILLISECONDS)
                .build();

        new Runner(opt).run();

        // InputStream iss = new ByteArrayInputStream(s.getBytes());

        // org.openjdk.jmh.Main.main(args);
        // HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        // server.createContext("/atms/calculateOrder", new AtmHandler());
        // server.createContext("/onlinegame/calculate", new GameGandler());
        // server.createContext("/transactions/report", new TransactionsHandler());

        // server.setExecutor(null); // creates a default executor
        // server.start();

    }

    @Setup
    public static void set() throws IOException, ParsingErrorExceptionException {
        InputStream is = new FileInputStream("generated.json");
        ee = TransactionsParser.parse(is);

    }

    @Benchmark
    public static void myParser() throws IOException, ParsingErrorExceptionException {
        // InputStream is = new ByteArrayInputStream(s.getBytes("UTF-8"));
        String s = Transactions.process(ee);
    }

    // @Benchmark
    public static void jackson() throws IOException, ParsingErrorExceptionException {
        // InputStream is = new ByteArrayInputStream(s.getBytes("UTF-8"));
        InputStream is = new FileInputStream("generated.json");

        Transaction[] ee = new ObjectMapper().readValue(is, Transaction[].class);
        String s = Transactions.process(ee);

    }

    static class AtmHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            OutputStream os = t.getResponseBody();
            try {

                byte[] r = AtmService.transationList(t.getRequestBody()).getBytes();
                t.sendResponseHeaders(200, r.length);
                t.getResponseHeaders().put("Content-Type", Collections.singletonList("application/json"));
                os.write(r);
            } catch (Exception e) {
                e.printStackTrace();

                t.sendResponseHeaders(500, 0);
            }
            os.close();

        }
    }

    static class GameGandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            OutputStream os = t.getResponseBody();
            try {

                byte[] r = Game.getGroups(t.getRequestBody()).getBytes();
                t.getResponseHeaders().put("Content-Type", Collections.singletonList("application/json"));
                t.sendResponseHeaders(200, r.length);
                os.write(r);
            } catch (Exception e) {
                e.printStackTrace();

                t.sendResponseHeaders(500, 0);
            }
            os.close();

        }
    }

    // static class TransactionsHandler implements HttpHandler {
    // @Override
    // public void handle(HttpExchange t) throws IOException {
    // OutputStream os = t.getResponseBody();
    // try {

    // byte[] r = Transactions.process(t.getRequestBody()).getBytes();
    // t.getResponseHeaders().put("Content-Type",
    // Collections.singletonList("application/json"));
    // t.sendResponseHeaders(200, r.length);
    // os.write(r);
    // } catch (Exception e) {
    // e.printStackTrace();

    // t.sendResponseHeaders(500, 0);
    // }
    // os.close();

    // }
    // }
}
