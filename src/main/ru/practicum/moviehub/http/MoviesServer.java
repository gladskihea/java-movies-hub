package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MoviesServer {
    private final HttpServer server;
    private final MoviesStore store;
    private final Gson gson;

    public MoviesServer(MoviesStore store, int port) throws IOException {
        this.store = store;
        this.gson = new Gson();
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.createContext("/movies", new MoviesHandler(store, gson));
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    private static class MoviesHandler extends BaseHttpHandler {
        private final MoviesStore store;

        public MoviesHandler(MoviesStore store, Gson gson) {
            super(gson);
            this.store = store;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String query = exchange.getRequestURI().getQuery();
            String[] pathParts = path.split("/");

            if (method.equals("GET") && pathParts.length == 2) {
                handleGet(exchange, query);
            } else if (method.equals("POST") && pathParts.length == 2) {
                handlePost(exchange);
            } else if (pathParts.length == 3 && pathParts[1].equals("movies")) {
                handleWithId(exchange, method, pathParts[2]);
            } else {
                sendResponse(exchange, new ErrorResponse("Метод не поддерживается"), 405);
            }
        }

        private void handleGet(HttpExchange exchange, String query) throws IOException {
            if (query != null && query.startsWith("year=")) {
                try {
                    int year = Integer.parseInt(query.split("=")[1]);
                    sendResponse(exchange, store.getByYear(year), 200);
                } catch (Exception e) {
                    sendResponse(exchange, new ErrorResponse("Некорректный параметр запроса - 'year'"), 400);
                }
            } else {
                sendResponse(exchange, store.getAll(), 200);
            }
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            String ct = exchange.getRequestHeaders().getFirst("Content-Type");
            if (ct == null || !ct.contains("application/json")) {
                sendResponse(exchange, null, 415);
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Movie movie = gson.fromJson(body, Movie.class);
            List<String> errors = validate(movie);
            if (!errors.isEmpty()) {
                sendResponse(exchange, new ErrorResponse("Ошибка валидации", errors), 422);
            } else {
                sendResponse(exchange, store.add(movie), 201);
            }
        }

        private void handleWithId(HttpExchange exchange, String method, String idStr) throws IOException {
            try {
                long id = Long.parseLong(idStr);
                if (method.equals("GET")) {
                    Optional<Movie> m = store.getById(id);
                    if (m.isPresent()) sendResponse(exchange, m.get(), 200);
                    else sendResponse(exchange, new ErrorResponse("Фильм не найден"), 404);
                } else if (method.equals("DELETE")) {
                    if (store.delete(id)) sendResponse(exchange, null, 204);
                    else sendResponse(exchange, new ErrorResponse("Фильм не найден"), 404);
                }
            } catch (NumberFormatException e) {
                sendResponse(exchange, new ErrorResponse("Некорректный ID"), 400);
            }
        }

        private List<String> validate(Movie m) {
            List<String> errors = new ArrayList<>();
            if (m.getTitle() == null || m.getTitle().isBlank()) {
                errors.add("название не должно быть пустым");
            } else if (m.getTitle().length() > 100) {
                errors.add("название <= 100 символов");
            }
            int curYear = LocalDate.now().getYear();
            if (m.getYear() < 1888 || m.getYear() > curYear + 1) {
                errors.add("год от 1888 до " + (curYear + 1));
            }
            return errors;
        }
    }
}