        package com.taskmanager.task_manager_backend.config;

        import com.taskmanager.task_manager_backend.service.AuthenticationService;
        import jakarta.servlet.FilterChain;
        import jakarta.servlet.ServletException;
        import jakarta.servlet.http.HttpServletRequest;
        import jakarta.servlet.http.HttpServletResponse;
        import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
        import org.springframework.security.core.context.SecurityContextHolder;
        import org.springframework.security.core.userdetails.UserDetails;
        import org.springframework.security.core.userdetails.UserDetailsService;
        import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
        import org.springframework.stereotype.Component;
        import org.springframework.web.filter.OncePerRequestFilter;

        import java.io.IOException;

        @Component
        public class JwtRequestFilter extends OncePerRequestFilter {

            private final UserDetailsService userDetailsService;
            private final AuthenticationService authenticationService;

            public JwtRequestFilter(UserDetailsService userDetailsService,
                                    AuthenticationService authenticationService) {
                this.userDetailsService = userDetailsService;
                this.authenticationService = authenticationService;
            }

            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain) throws ServletException, IOException {

                final String requestTokenHeader = request.getHeader("Authorization");

                System.out.println("=== JWT Filter Debug ===");
                System.out.println("Request URI: " + request.getRequestURI());
                System.out.println("Authorization Header: " + (requestTokenHeader != null ? "Present" : "Missing"));

                String username = null;
                String jwtToken = null;

                if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
                    jwtToken = requestTokenHeader.substring(7);
                    System.out.println("JWT Token extracted: " + jwtToken.substring(0, Math.min(20, jwtToken.length())) + "...");

                    try {
                        username = authenticationService.extractUsername(jwtToken);
                        System.out.println("Username extracted from JWT: " + username);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Unable to get JWT Token: " + e.getMessage());
                        logger.warn("Unable to get JWT Token");
                    } catch (Exception e) {
                        System.err.println("JWT Token error: " + e.getMessage());
                        logger.warn("JWT Token has expired or is invalid");
                    }
                } else {
                    System.out.println("No Bearer token found in Authorization header");
                }

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    System.out.println("Attempting to load user details for: " + username);

                    try {
                        UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                        System.out.println("User details loaded successfully");

                        if (authenticationService.validateToken(jwtToken, userDetails)) {
                            System.out.println("JWT Token validated successfully");

                            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails, null, userDetails.getAuthorities());

                            usernamePasswordAuthenticationToken
                                    .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                            System.out.println("Authentication set in SecurityContext");
                        } else {
                            System.err.println("JWT Token validation failed");
                        }
                    } catch (Exception e) {
                        System.err.println("Error loading user details: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    if (username == null) {
                        System.out.println("Username is null - skipping authentication");
                    }
                    if (SecurityContextHolder.getContext().getAuthentication() != null) {
                        System.out.println("Authentication already present in SecurityContext");
                    }
                }

                System.out.println("=== End JWT Filter Debug ===\n");
                chain.doFilter(request, response);
            }
        }