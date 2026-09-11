package com.cal.archivum.service.impl;


import com.cal.archivum.entity.User;
import com.cal.archivum.exception.UserNotFound;
import com.cal.archivum.exception.UserNotFoundByEmailOrUsername;
import com.cal.archivum.repository.UserRepository;

import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@NullMarked
public class ArchivumUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public ArchivumUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /*
    @Override
    public UserDetails loadUserByUsername(String identifier) {

        User user = userRepository
                .findByUserNameOrEmail(identifier, identifier)
                .orElseThrow(() ->
                        new UserNotFoundByEmailOrUsername(identifier));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUserName())
                .password(user.getUserHashedPassword())
                .roles("USER")
                .build();
    }
     */

    @Override
    public UserDetails loadUserByUsername(String identifier) {

        System.out.println("SECURITY: attempting login with " + identifier);

        User user = userRepository
                .findByUserNameOrEmail(identifier, identifier)
                .orElseThrow(() ->
                        new UserNotFoundByEmailOrUsername(identifier));

        System.out.println("SECURITY: found " + user.getUserName());
        System.out.println(
                "SECURITY: password format = "
                        + user.getUserHashedPassword().substring(
                        0,
                        Math.min(10, user.getUserHashedPassword().length())
                )
        );

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUserName())
                .password(user.getUserHashedPassword())
                .roles("USER")
                .build();
    }

}
