package com.uos.picobox.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uos.picobox.global.utils.SessionUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class SessionFilter extends OncePerRequestFilter {
    private final AntPathRequestMatcher customerMatcher = new AntPathRequestMatcher("/api/protected/**");
    private final AntPathRequestMatcher adminMatcher = new AntPathRequestMatcher("/api/admin/**");
    private final AntPathRequestMatcher guestMatcher = new AntPathRequestMatcher("/api/guest/**");
    private final SessionUtils sessionUtils;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (customerMatcher.matches(request)) {
            try {
                authenticateBySession(request, response, "customerSession", "CUSTOMER");
            } catch (IOException e) {
                return;
            }

        }
        else if (adminMatcher.matches(request)) {
            try {
                authenticateBySession(request, response, "adminSession", "ADMIN");
            } catch (IOException e) {
                return;
            }
        }
        else if (guestMatcher.matches(request)) {
            try {
                authenticateBySession(request, response, "guestSession", "GUEST");
            } catch (IOException e) {
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateBySession(HttpServletRequest request, HttpServletResponse response, String cacheName, String role) throws ServletException, IOException {
        String sessionId = request.getHeader("Authorization");
        if (sessionId == null) {
            authenticationEntryPoint.commence(
                    request,
                    response,
                    new InsufficientAuthenticationException("Authorization 헤더가 존재하지 않습니다.")
            );
            throw new IOException("Authorization 헤더가 존재하지 않습니다.");
        }

        String loginId = sessionUtils.existSession(cacheName, sessionId);
        List<SimpleGrantedAuthority> authorities =
                Collections.singletonList(new SimpleGrantedAuthority(role));

        // Authentication 객체 생성
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(loginId, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
