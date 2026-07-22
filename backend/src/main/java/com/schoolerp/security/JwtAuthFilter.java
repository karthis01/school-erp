package com.schoolerp.security;

import com.schoolerp.master.service.SessionService;
import com.schoolerp.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final SessionService sessionService;

    public JwtAuthFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService, SessionService sessionService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.sessionService = sessionService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        try {
            if (!jwtUtil.isTokenStructurallyValid(jwt)) {
                filterChain.doFilter(request, response);
                return;
            }

            if (jwtUtil.isSuperAdmin(jwt)) {
                authenticateSuperAdmin(jwt);
                filterChain.doFilter(request, response);
                return;
            }

            String schoolCode = jwtUtil.extractSchoolCode(jwt);
            String username = jwtUtil.extractUsername(jwt);
            String jti = jwtUtil.extractJti(jwt);

            if (schoolCode == null || username == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // Set tenant BEFORE touching the (tenant-scoped) user repository.
            TenantContext.setCurrentSchool(schoolCode);

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                // This token has been superseded by a newer login for the same user - reject it.
                if (!sessionService.isTokenStillActive(schoolCode, username, jti)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtil.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            // Invalid token - leave unauthenticated, downstream will 401/403
            filterChain.doFilter(request, response);
        } finally {
            // Critical: threads are pooled, never let a tenant leak onto the next request.
            TenantContext.clear();
        }
    }

    private void authenticateSuperAdmin(String jwt) {
        String username = jwtUtil.extractUsername(jwt);
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    username, null, List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }
}
