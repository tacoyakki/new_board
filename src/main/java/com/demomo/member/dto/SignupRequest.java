package com.demomo.member.dto;

import com.demomo.member.domain.Role;

public record SignupRequest(String username, String password, Role role) {
}
