package com.springboot.EasyPay.Dto.response;

/**
 * Returned by GET /api/auth/user-details.
 * id = id of the HR / Manager / Employee record of the logged-in user (null for ADMIN).
 */
public record UserDetailsDto(String username, String role, Long id) {
}