package com.ace.movie_api.controllers;

import com.ace.movie_api.dto.MovieDto;
import com.ace.movie_api.service.MovieService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/movie")
public class MovieController {
    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping("/add-movie")
    public ResponseEntity<MovieDto> addMovie(@RequestPart MultipartFile file, @RequestPart MovieDto movieDto) throws JsonProcessingException {
//        MovieDto movieDto = convertToMovieDto(movieDtoObj);
        System.out.println(movieDto.getDirector());
        return new ResponseEntity<>(movieService.addMovie(movieDto, file), HttpStatus.CREATED);
    }

    private MovieDto convertToMovieDto(String movieDto) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(movieDto, MovieDto.class);
    }

    @GetMapping("{movieId}")
    public ResponseEntity<MovieDto> getMovieById(@PathVariable Integer movieId) {
        return ResponseEntity.ok(movieService.getMovie(movieId));
    }

    @GetMapping
    public ResponseEntity<List<MovieDto>> findAllMovies() {
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    @PutMapping("update/{movieId}")
    public ResponseEntity<MovieDto> updateMovie(@PathVariable Integer movieId, @RequestPart MultipartFile file, @RequestPart MovieDto movieDto) {
        if(file.isEmpty()) file = null;

        return ResponseEntity.ok(movieService.updateMovie(movieId, movieDto, file));
    }

    @DeleteMapping("delete/{movieId}")
    public ResponseEntity<Map<String, Object>> deleteMovie(@PathVariable Integer movieId) {
        String message = movieService.deleteMovie(movieId);
        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", message);
        return ResponseEntity.ok(response);
    }
}
