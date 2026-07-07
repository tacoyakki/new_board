package com.demomo.global.security.oauth;

import com.demomo.global.security.jwt.JwtUtil;
import com.demomo.member.domain.Member;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtUtil jwtUtil;
    private final OAuth2UserService oAuth2UserService;

    @Value("${app.oauth2.success-redirect-uri}")
    private String successRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User user = (OAuth2User) authentication.getPrincipal();
        String providerId = user.getAttribute("sub");
        String email = user.getAttribute("email");
        if (providerId == null || email == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "OAuth 회원 정보를 확인할 수 없습니다.");
            return;
        }
        Member member = oAuth2UserService.findOrCreateGoogleMember(email, providerId);
        String username = member.getUsername();
        String role = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .findFirst().orElse("ROLE_USER").replaceFirst("^ROLE_", "");
        String token = jwtUtil.createAccessToken(username, role);
        String redirect = UriComponentsBuilder.fromUriString(successRedirectUri)
                .fragment("oauth_token=" + token).build(true).toUriString();
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        response.sendRedirect(redirect);
    }
}
