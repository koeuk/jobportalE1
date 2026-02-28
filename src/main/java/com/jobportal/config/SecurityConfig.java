package com.jobportal.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Autowired
        private CustomAuthenticationSuccessHandler successHandler;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests((requests) -> requests
                                                .requestMatchers("/", "/login", "/register", "/jobs", "/jobs/search",
                                                                "/jobs/{id}",
                                                                "/css/**", "/js/**", "/images/**", "/uploads/**",
                                                                "/register/success")
                                                .permitAll()
                                                .requestMatchers("/jobs/{id}/apply", "/jobs/{id}/save",
                                                                "/profile/skills/add")
                                                .hasRole("JOB_SEEKER")
                                                .requestMatchers("/jobs/*/apply")
                                                .hasRole("JOB_SEEKER")
                                                .requestMatchers("/jobs/new", "/jobs/{id}/applicants",
                                                                "/jobs/{id}/edit", "/jobs/{id}/delete",
                                                                "/applications", "/applications/{id}")
                                                .hasRole("RECRUITER")
                                                .requestMatchers("/admin/**")
                                                .hasRole("RECRUITER")
                                                .requestMatchers("/dashboard", "/profile", "/profile/update")
                                                .authenticated()
                                                .anyRequest().authenticated())
                                .formLogin((form) -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .usernameParameter("email")
                                                .successHandler(successHandler)
                                                .failureUrl("/login?error=true")
                                                .permitAll())
                                .logout((logout) -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/?logout=true")
                                                .permitAll());

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
