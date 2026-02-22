package ru.practicum.moviehub;

import ru.practicum.moviehub.http.MoviesServer;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;

public class MovieHubApp {
    // Добавляем throws IOException к методу main
    public static void main(String[] args) throws IOException {
        final MoviesServer server = new MoviesServer(new MoviesStore(), 8080);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Завершение работы...");
            server.stop();
        }));

        server.start();
    }
}