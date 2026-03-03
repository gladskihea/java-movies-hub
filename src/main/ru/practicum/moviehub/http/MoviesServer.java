package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MoviesServer {
    private final HttpServer server;

    public MoviesServer(MoviesStore store, int port) throws IOException {
        Gson gson = new Gson();
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.createContext("/movies", new MoviesHandler(store, gson));
    }

    public void start() {
        System.out.println("Сервер запущен...");
        server.start();
    }

    public void stop() {
        System.out.println("Сервер остановлен.");
        server.stop(0);
    }
}