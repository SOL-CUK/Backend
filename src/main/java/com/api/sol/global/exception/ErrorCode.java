package com.api.sol.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    SUCCESS(true, 1000, "요청에 성공했습니다."),

    EMPTY_JWT(false, 2001, "토큰을 입력해주세요."),
    INVALID_JWT(false, 2002, "유효하지 않은 토큰입니다.");

    private final boolean isSuccess;

    private final int code;

    private final String message;

}
