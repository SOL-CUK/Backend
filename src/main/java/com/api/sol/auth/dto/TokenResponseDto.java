package com.api.sol.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class TokenResponseDto {

    private String accessToken;

    // private String refreshToken;

}
