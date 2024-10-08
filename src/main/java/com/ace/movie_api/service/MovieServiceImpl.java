package com.ace.movie_api.service;

import com.ace.movie_api.dto.MovieDto;
import com.ace.movie_api.dto.MoviePageResponse;
import com.ace.movie_api.entities.Movie;
import com.ace.movie_api.exceptions.MovieNotFoundException;
import com.ace.movie_api.mappers.MovieMapper;
import com.ace.movie_api.repositories.MovieRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MovieServiceImpl implements MovieService {
    private final MovieRepository movieRepository;
    private final FileService fileService;

    public MovieServiceImpl(MovieRepository movieRepository, FileService fileService) {
        this.movieRepository = movieRepository;
        this.fileService = fileService;
    }

    @Value("${project.poster}")
    private String path;

    @Value("${base.url}")
    private String baseUrl;

    @Override
    public MovieDto addMovie(MovieDto movieDto, MultipartFile file) {
        try {
            // Upload the file
            String filename = fileService.uploadFile(path, file);

            // set the value of field poster with the file name
            movieDto.setPoster(filename);

            // map dto to Movie object
            Movie movie = MovieMapper.mapDtoToMovie(movieDto);

            // save the movie object
            Movie savedMovie = movieRepository.save(movie);

            // generate the posterUrl
            String posterUrl = getPosterUrl(filename);

            // map movie object to movie dto
            return MovieMapper.mapMovieToDto(savedMovie, posterUrl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private String getPosterUrl(String filename) {
        return baseUrl + "/file/" + filename;
    }

    @Override
    public MovieDto getMovie(Integer movieId) {
        // check the data in the db if exist, fetch data by id
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new MovieNotFoundException("Movie Not Found"));

        // generate poster url
        String posterUrl = getPosterUrl(movie.getPoster());

        // map to MovieDto
        return MovieMapper.mapMovieToDto(movie, posterUrl);
    }

    @Override
    public List<MovieDto> getAllMovies() {
        // fetch all movies from db
        List<Movie> movies = movieRepository.findAll();

        // iterate through and generate posterUrl for each movie and map to MovieDto
        return movies.stream().map(movie -> {
            String posterUrl = getPosterUrl(movie.getPoster());
            return MovieMapper.mapMovieToDto(movie, posterUrl);
        }).collect(Collectors.toList());
    }

    @Override
    public MovieDto updateMovie(Integer movieId, MovieDto movieDto, MultipartFile file) {
        try {
            // Check if movie exist with the given Id
            Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new MovieNotFoundException("Movie Not Found"));

            // if file is null do nothing
            String fileName = movie.getPoster();

            // if file delete existing file and upload new file
            if (file != null) {
                Files.deleteIfExists(Paths.get(path + File.separator + fileName));
                fileName = fileService.uploadFile(path, file);
            }


            // generate movie dto poster value
            movieDto.setPoster(fileName);
            movieDto.setId(movie.getId());

            // map to movie object
            Movie movieData = MovieMapper.mapDtoToMovie(movieDto);

            // save movie and generate posterUrl
            Movie savedMovie = movieRepository.save(movieData);
            String posterUrl = getPosterUrl(fileName);

            // map to movieDto
            return MovieMapper.mapMovieToDto(savedMovie, posterUrl);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String deleteMovie(Integer movieId) {
        try {
            // Check if movie exist with the given Id
            Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new MovieNotFoundException("Movie Not Found"));

            // Delete the file associated with this movie
            Files.deleteIfExists(Paths.get(path + File.separator + movie.getPoster()));
            // Delete movie
            movieRepository.delete(movie);
            return "Movie deleted successfully";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MoviePageResponse getPaginatedMovies(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Movie> moviePages = movieRepository.findAll(pageable);
        List<Movie> movies = moviePages.getContent();
        List<MovieDto> movieDtos = movies.stream().map(movie -> {
            return MovieMapper.mapMovieToDto(movie, getPosterUrl(movie.getPoster()));
        }).collect(Collectors.toList());
        return new MoviePageResponse(
                movieDtos,
                pageNumber,
                pageSize,
                moviePages.getTotalElements(),
                moviePages.getTotalPages(),
                moviePages.isLast()
        );
    }

    @Override
    public MoviePageResponse getAllMoviesWithPaginationAndSorting(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Movie> moviePage = movieRepository.findAll(pageable);
        List<Movie> movies = moviePage.getContent();
        List<MovieDto> movieDtos = movies.stream().map(movie -> {
            return MovieMapper.mapMovieToDto(movie, getPosterUrl(movie.getPoster()));
        }).collect(Collectors.toList());

        return new MoviePageResponse(
                movieDtos,
                pageNumber,
                pageSize,
                moviePage.getTotalElements(),
                moviePage.getTotalPages(),
                moviePage.isLast()
        );
    }
}
