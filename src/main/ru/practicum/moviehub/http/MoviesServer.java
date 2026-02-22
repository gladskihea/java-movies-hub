package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

public class MoviesServer {
    private final HttpServer server;
    private final MoviesStore store;

    public MoviesServer(MoviesStore store, int port) throws IOException {
        this.store = store;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.createContext("/movies", new MoviesHandler());
    }

    public void start() {
        System.out.println("Сервер запущен на порту " + server.getAddress().getPort());
        server.start();
    }

    public void stop() {
        System.out.println("Остановка сервера...");
        server.stop(0);
    }

    private class MoviesHandler extends BaseHttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();

            if ("GET".equals(method)) {
                List<Movie> movies = store.getAll();

                sendResponse(exchange, movies, 200);
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }
}