package com.api.sol.member.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@Entity
@ToString
@Table(name  = "member")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String clientId;

    private String nickname;

    @Enumerated(EnumType.STRING)
    private Role role;

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

}
