package com.zosh.response;

import com.zosh.domain.USER_ROLE;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String message;
    private USER_ROLE role;
}
