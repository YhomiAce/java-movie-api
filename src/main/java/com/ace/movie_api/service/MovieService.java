package com.ace.movie_api.service;

import com.ace.movie_api.dto.MovieDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MovieService {
    MovieDto addMovie(MovieDto movieDto, MultipartFile file);

    MovieDto getMovie(Integer movieId);

    List<MovieDto> getAllMovies();

    MovieDto updateMovie(Integer movieId, MovieDto movieDto, MultipartFile file);

    String deleteMovie(Integer movieId);
}
