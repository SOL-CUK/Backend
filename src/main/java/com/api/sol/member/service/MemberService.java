package com.api.sol.member.service;

import com.api.sol.auth.dto.TokenResponseDto;
import com.api.sol.auth.jwt.JwtTokenProvider;
import com.api.sol.member.entity.Member;
import com.api.sol.member.entity.Role;
import com.api.sol.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberService {

    private static final String JSON_ATTRIBUTE_NAME_NICKNAME = "nickname";
    private static final String JSON_ATTRIBUTE_NAME_ID = "id";

    private final MemberRepository memberRepository;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public TokenResponseDto login(Map<String, String> memberInfo) {
        String clientId = memberInfo.get(JSON_ATTRIBUTE_NAME_ID);
        String nickname = memberInfo.get(JSON_ATTRIBUTE_NAME_NICKNAME);
        String accessToken = tokenProvider.generateAccessToken(clientId);

        Member member = memberRepository.findByClientId(clientId)
                .map(existingMember -> {
                    existingMember.updateNickname(nickname);
                    return existingMember;
                })
                .orElseGet(() -> createNewMember(clientId, nickname));

        return TokenResponseDto.builder()
                .accessToken(accessToken)
                .build();
    }

    private Member createNewMember(String clientId, String nickname) {
        Member newMember = Member.builder()
                .clientId(clientId)
                .nickname(nickname)
                .role(Role.USER)
                .build();
        return memberRepository.save(newMember);
    }
}

