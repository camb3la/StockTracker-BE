package com.example.Config;

import com.example.Security.JwtAuthenticationFilter;
import com.example.Security.JwtTokenProvider;
import com.example.Service.UserDetailService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailService userDetailService;

    public WebSecurityConfig(JwtTokenProvider jwtTokenProvider, UserDetailService userDetailService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailService = userDetailService;
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)  throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()  // Permette tutte le richieste senza autenticazione
                );

        http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, userDetailService) ,
                UsernamePasswordAuthenticationFilter.class);

        return http.build() ;
    }
}


