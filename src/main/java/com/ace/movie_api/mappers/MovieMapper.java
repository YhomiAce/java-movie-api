package com.ace.movie_api.mappers;

import com.ace.movie_api.dto.MovieDto;
import com.ace.movie_api.entities.Movie;
import jakarta.validation.constraints.NotNull;

public class MovieMapper {
    public static Movie mapDtoToMovie(MovieDto dto) {
        return new Movie(
                dto.getId(),
                dto.getTitle(),
                dto.getDirector(),
                dto.getStudio(),
                dto.getMovieCast(),
                dto.getReleaseYear(),
                dto.getPoster()
        );
    }

    public static MovieDto mapMovieToDto(Movie movie, String posterUrl) {
        return new MovieDto(
                movie.getId(),
                movie.getTitle(),
                movie.getDirector(),
                movie.getStudio(),
                movie.getMovieCast(),
                movie.getReleaseYear(),
                movie.getPoster(),
                posterUrl
        );
    }
}
