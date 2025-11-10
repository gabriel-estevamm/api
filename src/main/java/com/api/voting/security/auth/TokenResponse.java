package com.api.voting.security.auth;

public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds) {}