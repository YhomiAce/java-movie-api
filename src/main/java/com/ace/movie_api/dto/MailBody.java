package com.ace.movie_api.dto;

import lombok.Builder;

@Builder
public record MailBody(
        String to,
        String subject,
        String text
) {
}
