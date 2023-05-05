package com.mycompany.app;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Collections;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import com.sun.net.httpserver.HttpServer;

import org.openjdk.jmh.runner.RunnerException;

public class App {

  public static void main(String[] args) throws IOException, RunnerException {

    Thread t = new Thread(() -> {
      try {
        createServer();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    });
    t.start();

    // Benchmarks.run();

  }

  static void createServer() throws IOException {

    HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
    server.createContext("/atms/calculateOrder", new RequestHandler((is) -> {
      return AtmService.process(AtmParser.parse(is));
    }));

    server.createContext("/onlinegame/calculate", new RequestHandler((is) -> {
      return Game.process(GameParser.parse(is));
    }));

    server.createContext("/transactions/report", new RequestHandler((is) -> {
      TransactionsParser.parse(is);
      return Transactions.getResults();
    }));

    server.setExecutor(null); // creates a default executor
    server.start();
  }

  interface HandlerAction {
    String action(InputStream is) throws IOException, ParsingErrorExceptionException;
  }

  static class RequestHandler implements HttpHandler {

    HandlerAction handlerAction;

    public RequestHandler(HandlerAction o) {
      this.handlerAction = o;
    }

    @Override
    public void handle(HttpExchange ctx) throws IOException {
      OutputStream os = ctx.getResponseBody();
      try {

        byte[] response = handlerAction.action(ctx.getRequestBody()).getBytes();
        ctx.getResponseHeaders().put("Content-Type", Collections.singletonList("application/json"));
        ctx.sendResponseHeaders(200, response.length);
        os.write(response);
      } catch (Exception e) {
        e.printStackTrace();

        ctx.sendResponseHeaders(500, 0);
      }
      os.close();

    }
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

    InputStream is = new ByteArrayInputStream(transactionExample.getBytes());
    TransactionsParser.parse(is);
    String s = Transactions.getResults();
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
    LongArray p = AtmParser.parse(is);
    String s = AtmService.process(p);
    System.out.println(s);
  }

}
