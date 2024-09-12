package com.ace.movie_api.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieDto {
    private Integer id;

    @NotBlank(message = "Title should not not be empty")
    private String title;

    @NotEmpty()
    private String director;

    @NotBlank
    private String studio;

    private Set<String> movieCast;

    private Integer releaseYear;

    private String poster;

    private String posterUrl;
}
