package ru.mentee.library.api.dto;

public record TokenResponse(
    String accessToken,
    String refreshToken
) {}