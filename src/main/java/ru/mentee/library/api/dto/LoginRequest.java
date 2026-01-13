package ru.mentee.library.api.dto;

public record LoginRequest(
    String username,
    String password
) {}