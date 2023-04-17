package com.mycompany.app;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.openjdk.jmh.annotations.Benchmark;
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

  private static Transaction[] parsedTransaction;
  private static MyArray<Request> parsedAtm;
  private static Players players;

  public static void main(String[] args) throws IOException, ParsingErrorExceptionException, RunnerException {

    Options opt = new OptionsBuilder()
        .include(App.class.getSimpleName())
        .forks(1)
        .warmupIterations(1)
        .threads(1)
        .mode(Mode.AverageTime)
        .measurementIterations(3)
        .timeUnit(TimeUnit.MILLISECONDS)
        .build();

    new Runner(opt).run();

    // transactionsTest();

    gameTest();
    gameTest();

    gameBenchmark();
    gameBenchmark();

    gameBenchmark();

    gameBenchmark();
    gameBenchmark();

    // AtmTest();

    // myParser();

    // org.openjdk.jmh.Main.main(args);
    // HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
    // server.createContext("/atms/calculateOrder", new AtmHandler());
    // server.createContext("/onlinegame/calculate", new GameGandler());
    // server.createContext("/transactions/report", new TransactionsHandler());

    // server.setExecutor(null); // creates a default executor
    // server.start();

  }

  static void transactionsTest() throws IOException, ParsingErrorExceptionException {

    String transactionExample = """
        [
            {
              "debitAccount": "32309111922661937852684864",
              "creditAccount": "06105023389842834748547303",
              "amount": 10.90
            },
            {
              "debitAccount": "99074318698137062235845814",
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

    InputStream is = new ByteArrayInputStream(transactionExample.getBytes());
    parsedTransaction = TransactionsParser.parse(is);
    String s = Transactions.process(parsedTransaction);
    System.out.println(s);
  }

  static void gameTest() throws IOException, ParsingErrorExceptionException {

    String gameExample = """
        {
            "groupCount": 6,
            "clans": [
              {
                "numberOfPlayers": 4,
                "points": 50
              },
              {
                "numberOfPlayers": 2,
                "points": 70
              },
              {
                "numberOfPlayers": 6,
                "points": 60
              },
              {
                "numberOfPlayers": 1,
                "points": 15
              },
              {
                "numberOfPlayers": 5,
                "points": 40
              },
              {
                "numberOfPlayers": 3,
                "points": 45
              },
              {
                "numberOfPlayers": 1,
                "points": 12
              },
              {
                "numberOfPlayers": 4,
                "points": 40
              }
            ]
          }

                """;

    InputStream is = new ByteArrayInputStream(gameExample.getBytes());
    Players p = GameParser.parse(is);
    String s = Game.process(p);
    System.out.println(s);
  }

  static void AtmTest() throws IOException, ParsingErrorExceptionException {
    String gameExample = """
        [
          {
            "region": 1,
            "requestType": "STANDARD",
            "atmId": 2
          },
          {
            "region": 1,
            "requestType": "STANDARD",
            "atmId": 1
          },
          {
            "region": 2,
            "requestType": "PRIORITY",
            "atmId": 3
          },
          {
            "region": 3,
            "requestType": "STANDARD",
            "atmId": 4
          },
          {
            "region": 4,
            "requestType": "STANDARD",
            "atmId": 5
          },
          {
            "region": 5,
            "requestType": "PRIORITY",
            "atmId": 2
          },
          {
            "region": 5,
            "requestType": "STANDARD",
            "atmId": 1
          },
          {
            "region": 3,
            "requestType": "SIGNAL_LOW",
            "atmId": 2
          },
          {
            "region": 2,
            "requestType": "SIGNAL_LOW",
            "atmId": 1
          },
          {
            "region": 3,
            "requestType": "FAILURE_RESTART",
            "atmId": 1
          }
        ]

                  """;

    InputStream is = new ByteArrayInputStream(gameExample.getBytes());
    MyArray<Request> p = AtmParser.parse(is);
    String s = AtmService.process(p);
    System.out.println(s);
  }

  @Setup
  public static void set() throws IOException, ParsingErrorExceptionException {
    // parsedAtm = AtmmParser.parse(new FileInputStream("AtmGenerated.json"));
    // // eee = AtmmParser.parse(is);

    // parsedTransaction = TransactionsParser.parse(new
    // FileInputStream("generated.json"));
    players = GameParser.parse(new FileInputStream("GameGenerated.json"));

  }

  // @Benchmark
  public static void transactionsBenchmark() throws IOException, ParsingErrorExceptionException {
    // InputStream is = new FileInputStream("generated.json");
    // parsedTransaction = TransactionsParser.parse(is);
    String s = Transactions.process(parsedTransaction);
  }

  @Benchmark
  public static void gameBenchmark() throws IOException, ParsingErrorExceptionException {
    InputStream is = new FileInputStream("GameGenerated.json");
    players = GameParser.parse(is);

    String s = Game.process(players);
  }

  // @Benchmark
  public static void atmBenchmark() throws IOException, ParsingErrorExceptionException {
    // InputStream is = new FileInputStream("AtmGenerated.json");
    // ArrayList<Request> ee = AtmmParser.parse(is);
    String s = AtmService.process(parsedAtm);

  }

  static class AtmHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange t) throws IOException {
      OutputStream os = t.getResponseBody();
      try {

        // byte[] r = AtmService.transationList(t.getRequestBody()).getBytes();
        // t.sendResponseHeaders(200, r.length);
        // t.getResponseHeaders().put("Content-Type",
        // Collections.singletonList("application/json"));
        // os.write(r);
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

        // byte[] r = Game.getGroups(t.getRequestBody()).getBytes();
        // t.getResponseHeaders().put("Content-Type",
        // Collections.singletonList("application/json"));
        // t.sendResponseHeaders(200, r.length);
        // os.write(r);
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
