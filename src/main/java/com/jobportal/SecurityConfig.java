package com.jobportal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.jobportal.jwt.JwtAuthenticationEntryPoint;
import com.jobportal.jwt.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint point;
    private final JwtAuthenticationFilter filter;
    private final Environment env;

    public SecurityConfig(JwtAuthenticationEntryPoint point, JwtAuthenticationFilter filter, Environment env) {
        this.point = point;
        this.filter = filter;
        this.env = env;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        boolean isDevMode = env.acceptsProfiles(Profiles.of("dev")); // Use Profiles.of() for compatibility

        http.csrf(csrf -> csrf.disable());

        if (isDevMode) {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); // Allow all in dev mode
        } else {
            http.authorizeHttpRequests(auth -> auth
                    .requestMatchers("/auth/login", "/users/register", "/users/verifyOtp/**", "/users/sendOtp/**",
                            "/users/changePass")
                    .permitAll()
                    .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex.authenticationEntryPoint(point))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

            http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
    }
}
