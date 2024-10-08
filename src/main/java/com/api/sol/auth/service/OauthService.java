package com.api.sol.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Slf4j
@Service
public class OauthService {

    private static final String CONTENT_TYPE_HEADER_VALUE = "application/x-www-form-urlencoded;charset=utf-8";
    private static final String HEADER_TOKEN_PREFIX = "Bearer ";

    private static final String JSON_ATTRIBUTE_NAME_TOKEN = "access_token";
    private static final String JSON_ATTRIBUTE_NAME_ID = "id";
    private static final String JSON_ATTRIBUTE_NAME_PROPERTIES = "properties";
    private static final String JSON_ATTRIBUTE_NAME_NICKNAME = "nickname";

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String KAKAO_CLIENT_ID;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String KAKAO_CLIENT_SECRET;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String KAKAO_REDIRECT_URI;

    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String TOKEN_URI;

    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String USER_INFO_URI;

    private final RestTemplate restTemplate;

    public OauthService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public String getRedirectUrl() {
        return UriComponentsBuilder.fromHttpUrl("https://kauth.kakao.com/oauth/authorize")
                .queryParam("client_id", KAKAO_CLIENT_ID)
                .queryParam("redirect_uri", KAKAO_REDIRECT_URI)
                .queryParam("response_type", "code")
                .toUriString();
    }

    public Map<String, String> getMemberInfo(String code) throws JsonProcessingException {
        String accessToken = retrieveAccessToken(code);
        return fetchMemberInfo(accessToken);
    }

    private String retrieveAccessToken(String code) throws JsonProcessingException {
        ResponseEntity<String> response = restTemplate.exchange(TOKEN_URI, HttpMethod.POST,
                new HttpEntity<>(createTokenRequestBody(code), createHeaders()), String.class);

        return parseJson(response.getBody(), JSON_ATTRIBUTE_NAME_TOKEN);
    }

    private Map<String, String> fetchMemberInfo(String accessToken) throws JsonProcessingException {
        HttpEntity<MultiValueMap<String, String>> memberInfoRequest = new HttpEntity<>(createHeadersWithAuth(accessToken));
        ResponseEntity<String> response = restTemplate.exchange(USER_INFO_URI, HttpMethod.POST, memberInfoRequest, String.class);

        log.info("로그인 사용자 정보 : {}", response);
        return extractMemberInfo(response.getBody());
    }

    private MultiValueMap<String, String> createTokenRequestBody(String code) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", KAKAO_CLIENT_ID);
        body.add("redirect_uri", KAKAO_REDIRECT_URI);
        body.add("code", code);
        body.add("client_secret", KAKAO_CLIENT_SECRET); // KAKAO_CLIENT_SECRET 값을 직접 작성
        return body;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_HEADER_VALUE);
        return headers;
    }

    private HttpHeaders createHeadersWithAuth(String accessToken) {
        HttpHeaders headers = createHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, HEADER_TOKEN_PREFIX + accessToken);
        return headers;
    }

    private String parseJson(String jsonString, String attributeName) throws JsonProcessingException {
        return new ObjectMapper().readTree(jsonString).get(attributeName).asText();
    }

    private Map<String, String> extractMemberInfo(String jsonResponse) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        String clientId = jsonNode.get(JSON_ATTRIBUTE_NAME_ID).asText();
        String nickname = jsonNode.get(JSON_ATTRIBUTE_NAME_PROPERTIES).get(JSON_ATTRIBUTE_NAME_NICKNAME).asText();

        return Map.of(JSON_ATTRIBUTE_NAME_ID, clientId, JSON_ATTRIBUTE_NAME_NICKNAME, nickname);
    }

}
