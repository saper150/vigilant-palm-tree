package com.mycompany.app;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Benchmark;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class Benchmarks {

    static void run() throws RunnerException {

        Options opt = new OptionsBuilder()
                .include(Benchmarks.class.getSimpleName())
                .forks(1)
                .warmupIterations(1)
                .threads(1)
                .mode(Mode.AverageTime)
                .measurementIterations(3)
                .timeUnit(TimeUnit.MILLISECONDS)
                .build();

        new Runner(opt).run();
    }

    // @Benchmark
    public static void AtmE2E() throws IOException {
        InputStream is = new FileInputStream("AtmGenerated.json");

        URL url = new URL("http://localhost:8080/atms/calculateOrder");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");

        connection.setUseCaches(false);
        connection.setDoOutput(true);

        is.transferTo(connection.getOutputStream());
        is.close();
        // DataOutputStream wr = new DataOutputStream (connection.getOutputStream());

        // wr.writeBytes(urlParameters);
        // wr.close();
        connection.getInputStream().readAllBytes();

    }

    @Benchmark
    public static void TransactionE2E() throws IOException {
        InputStream is = new FileInputStream("TransactionGenerated.json");

        URL url = new URL("http://localhost:8080/transactions/report");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");

        connection.setUseCaches(false);
        connection.setDoOutput(true);

        is.transferTo(connection.getOutputStream());
        is.close();
        // DataOutputStream wr = new DataOutputStream (connection.getOutputStream());

        // wr.writeBytes(urlParameters);
        // wr.close();
        connection.getInputStream().readAllBytes();

    }

    // @Benchmark
    public static void GameE2E() throws IOException {
        InputStream is = new FileInputStream("GameGenerated.json");

        URL url = new URL("http://localhost:8080/onlinegame/calculate");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");

        connection.setUseCaches(false);
        connection.setDoOutput(true);

        is.transferTo(connection.getOutputStream());
        is.close();
        // DataOutputStream wr = new DataOutputStream (connection.getOutputStream());

        // wr.writeBytes(urlParameters);
        // wr.close();
        connection.getInputStream().readAllBytes();

    }

    // @Benchmark
    public static void transactionsBenchmark() throws IOException, ParsingErrorExceptionException {
        InputStream is = new FileInputStream("TransactionGenerated.json");
        TransactionsParser.parse(is);
        String s = Transactions.getResults();
    }

    // @Benchmark
    public static void gameBenchmark() throws IOException, ParsingErrorExceptionException {
        InputStream is = new FileInputStream("GameGenerated.json");
        Players players = GameParser.parse(is);

        String s = Game.process(players);
    }

    // @Benchmark
    public static void atmBenchmark() throws IOException, ParsingErrorExceptionException {
        InputStream is = new FileInputStream("AtmGenerated.json");
        LongArray ee = AtmParser.parse(is);
        String s = AtmService.process(ee);

    }
}
