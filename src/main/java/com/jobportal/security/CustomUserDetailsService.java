package com.jobportal.security;

import com.jobportal.entity.Users;
import com.jobportal.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        String role = user.getUserType() != null
                ? "ROLE_" + user.getUserType().getUserTypeName().toUpperCase().replace(" ", "_")
                : "ROLE_USER";

        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(role)))
                .accountLocked(user.getIsActive() == null || !user.getIsActive())
                .disabled(user.getIsActive() == null || !user.getIsActive())
                .build();
    }
}
