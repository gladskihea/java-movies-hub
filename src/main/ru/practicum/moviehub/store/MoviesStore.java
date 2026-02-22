package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class MoviesStore {
    private final Map<Long, Movie> movies = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public List<Movie> getAll() {
        return new ArrayList<>(movies.values());
    }


    public Movie add(Movie movie) {
        long id = idGenerator.incrementAndGet();
        movie.setId(id);
        movies.put(id, movie);
        return movie;
    }

    public void deleteAll() {
        movies.clear();
        idGenerator.set(0);
    }
}