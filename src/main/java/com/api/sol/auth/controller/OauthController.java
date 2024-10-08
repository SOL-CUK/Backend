package com.api.sol.auth.controller;

import com.api.sol.auth.dto.TokenResponseDto;
import com.api.sol.auth.service.OauthService;
import com.api.sol.member.service.MemberService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth2/kakao")
public class OauthController {

    private final OauthService oauthService;
    private final MemberService memberService;

    @GetMapping
    public RedirectView getLoginPage() {
        String redirectUrl = oauthService.getRedirectUrl();
        log.info("{}", redirectUrl);
        return new RedirectView(redirectUrl);
    }

    @GetMapping("/code")
    public ResponseEntity<TokenResponseDto> callBack(@RequestParam(required = false) String code) throws JsonProcessingException {
        Map<String, String> memberInfo = oauthService.getMemberInfo(code);
        TokenResponseDto tokenResponse = memberService.login(memberInfo);

        return ResponseEntity.ok(tokenResponse);
    }

}
