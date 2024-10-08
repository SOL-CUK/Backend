package com.api.sol.auth.service;

import com.api.sol.auth.entity.CustomUserDetails;
import com.api.sol.member.entity.Member;
import com.api.sol.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String clientId) throws UsernameNotFoundException {

        Member member = memberRepository.findByClientId(clientId).orElseThrow(
                () -> new UsernameNotFoundException("User not found with clientId: " + clientId));

        return new CustomUserDetails(member);
    }

}
