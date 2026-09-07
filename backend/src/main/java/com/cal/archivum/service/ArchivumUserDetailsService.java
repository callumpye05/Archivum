package com.cal.archivum.service;


import com.cal.archivum.entity.User;
import com.cal.archivum.exception.UserNotFound;
import com.cal.archivum.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ArchivumUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public ArchivumUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String userName) throws Exception {

        User user = userRepository.findByUserName(userName).orElseThrow(() -> new UserNotFound(userName));

        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail()).password(user.getPasswordHash()).roles("USER").build();
    }

    @Override
    public UserDetails loadUserByEmail(String email) throws Exception {
        User user = userRepository.findByUserName(email).orElseThrow(() -> new UserNotFound(email));

        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail()).password(user.getPasswordHash()).roles("USER").build();
    }

}
