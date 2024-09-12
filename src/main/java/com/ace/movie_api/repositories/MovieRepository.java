package com.ace.movie_api.repositories;

import com.ace.movie_api.entities.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Integer> {
}
