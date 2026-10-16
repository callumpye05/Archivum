package com.cal.archivum.security;

import com.cal.archivum.exception.EmailNotVerified;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@EnableWebSecurity
public class ArchivumSecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,DaoAuthenticationProvider authenticationProvider)
            throws Exception {

        return http.csrf(Customizer.withDefaults()).authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET,"/auth/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST,"/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/verify-email").permitAll()
                        .anyRequest().authenticated()).formLogin(form -> form
                        .loginProcessingUrl("/auth/login").successHandler((request, response, authentication) ->
                                response.setStatus(HttpStatus.OK.value()))
                        .failureHandler((request, response,exception) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            String code =exception instanceof EmailNotVerified ? "EMAIL_NOT_VERIFIED" : "INVALID_CREDENTIALS";
                            response.getWriter().write("{\"code\":\""+ code+ "\"}");
                        })
                        .permitAll())
                        .logout(logout -> logout.logoutUrl("/auth/logout").invalidateHttpSession(true).deleteCookies("JSESSIONID")
                                .logoutSuccessHandler((request, response, authentication) -> response.setStatus(HttpStatus.NO_CONTENT.value()))
                        )
                        .exceptionHandling(ex ->ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        ).authenticationProvider(authenticationProvider).build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider =new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        provider.setPreAuthenticationChecks(user ->{
            if (!user.isAccountNonLocked()){
                throw new LockedException("Account locked");
            }

            if (!user.isAccountNonExpired()) {
                throw new AccountExpiredException("Account expired");
            }
        });
        provider.setPostAuthenticationChecks(user ->{
            if (!user.isEnabled()){
                throw new EmailNotVerified("Email isn't verified");
            }

            if (!user.isCredentialsNonExpired()) {
                throw new CredentialsExpiredException("Credentials expired");
            }
        });
        return provider;
    }
}