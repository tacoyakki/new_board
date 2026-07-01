package com.demomo.global.security.oauth;

import com.demomo.member.domain.Member;
import com.demomo.member.domain.Role;
import com.demomo.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {
    private static final String PROVIDER = "google";
    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User googleUser = super.loadUser(userRequest);
        String providerId = requiredAttribute(googleUser, "sub");
        String email = requiredAttribute(googleUser, "email");
        Member member = memberRepository.findByOauthProviderAndOauthProviderId(PROVIDER, providerId)
                .orElseGet(() -> createGoogleMember(email, providerId));

        Map<String, Object> attributes = new HashMap<>(googleUser.getAttributes());
        attributes.put("demomoUsername", member.getUsername());
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name())),
                attributes, "demomoUsername");
    }

    private Member createGoogleMember(String email, String providerId) {
        String username = memberRepository.existsByUsername(email)
                ? email + "#google-" + providerId.substring(0, Math.min(8, providerId.length()))
                : email;
        return memberRepository.save(Member.builder()
                .username(username)
                .role(Role.USER)
                .oauthProvider(PROVIDER)
                .oauthProviderId(providerId)
                .build());
    }

    private String requiredAttribute(OAuth2User user, String name) {
        Object value = user.getAttributes().get(name);
        if (!(value instanceof String text) || text.isBlank()) {
            throw new OAuth2AuthenticationException("Google 계정에서 " + name + " 정보를 받지 못했습니다.");
        }
        return text;
    }
}
