package com.docuquery.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Step 1 — Get Authorization header
        final String authHeader = request.getHeader("Authorization");

        // Step 2 — If no token, skip filter (public route)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Step 3 — Extract token (remove "Bearer " prefix)
        final String token = authHeader.substring(7);

        // Step 4 — Extract email from token
        final String email = jwtUtil.extractUsername(token);

        // Step 5 — If email found and user not already authenticated
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Step 6 — Load user from DB
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // Step 7 — Validate token
            if (jwtUtil.isTokenValid(token, userDetails)) {

                // Step 8 — Create auth token and set in SecurityContext
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Step 9 — Mark user as authenticated for this request
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Step 10 — Continue to next filter
        filterChain.doFilter(request, response);
    }
}