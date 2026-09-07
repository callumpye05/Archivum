package com.cal.security;

import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class ArchivumSecurityConfig {

    //copied from Servlet web with lambda chaining
    @Bean
    @Order(2147483642)
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) {
        return http.authorizeHttpRequests((requests) -> ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)requests.anyRequest()).authenticated())
        .formLogin(Customizer.withDefaults())
        .httpBasic(Customizer.withDefaults()).build();
    }
}
