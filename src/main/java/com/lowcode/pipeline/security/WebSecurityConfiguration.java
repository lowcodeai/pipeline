package com.lowcode.pipeline.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {

    private final UserDetailsServiceImpl userDetailsService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private AuthenticationConfiguration authenticationConfiguration;

    public WebSecurityConfiguration(
            UserDetailsServiceImpl userDetailsService,
            BCryptPasswordEncoder bCryptPasswordEncoder,
            AuthenticationConfiguration authenticationConfiguration) {
        this.userDetailsService = userDetailsService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.authenticationConfiguration = authenticationConfiguration;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(registry ->{
                    registry
                            .requestMatchers(HttpMethod.POST, SecurityConstants.SIGN_UP_URL)
                            .permitAll();
                    registry
                            .requestMatchers(HttpMethod.POST, "/chat")
                            .authenticated();
                    registry
                            .anyRequest()
                            .authenticated();

                })
                .addFilter(new JWTAuthenticationFilter(authenticationManager()))
                .addFilter(new JWTAuthenticationVerficationFilter(authenticationManager()))
                .sessionManagement((var session)-> {
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173")); // your React dev URL(s)
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true); // if you ever send cookies

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
