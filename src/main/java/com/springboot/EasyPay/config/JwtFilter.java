package com.springboot.EasyPay.config;

import com.springboot.EasyPay.Utility.JwtUtility;
import com.springboot.EasyPay.service.MyUserDetailsService;
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

    private final JwtUtility jwtUtility;
    private final MyUserDetailsService myUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Lets read the token from the header (Authorization)
        final String authorizationHeader = request.getHeader("Authorization");
        String username = null;
        String token = "";

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
            // 1. Safely extract the token string without splitting issues
            token = authorizationHeader.substring(7).trim();

            // 2. CRITICAL FIX: Only parse if the token is a real JWT (not blank, null, or undefined strings)
            if (!token.isEmpty() && !token.equalsIgnoreCase("undefined") && !token.equalsIgnoreCase("null")) {
                username = jwtUtility.extractUsername(token);
            }

        }
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = myUserDetailsService.loadUserByUsername(username);

            if (jwtUtility.validateToken(token, userDetails.getUsername())) {

                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }

        }

        filterChain.doFilter(request, response);
    }

}
