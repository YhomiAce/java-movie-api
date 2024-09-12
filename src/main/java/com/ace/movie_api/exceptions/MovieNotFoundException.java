package com.ace.movie_api.exceptions;

public class MovieNotFoundException extends  RuntimeException{
    public MovieNotFoundException(String message) {
        super(message);
    }
}
