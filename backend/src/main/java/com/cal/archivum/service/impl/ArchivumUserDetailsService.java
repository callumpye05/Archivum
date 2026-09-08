package com.cal.archivum.service.impl;


import com.cal.archivum.entity.User;
import com.cal.archivum.exception.UserNotFound;
import com.cal.archivum.exception.UserNotFoundByEmailOrUsername;
import com.cal.archivum.repository.UserRepository;
import com.cal.archivum.service.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class ArchivumUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public ArchivumUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String userName) throws Exception {

        User user = userRepository.findByUserName(userName).orElseThrow(() -> new UserNotFoundByEmailOrUsername(userName));

        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail()).password(user.getUserHashedPassword()).roles("USER").build();
    }

    @Override
    public UserDetails loadUserByEmail(String email) throws Exception {
        User user = userRepository.findByUserName(email).orElseThrow(() -> new UserNotFoundByEmailOrUsername(email));

        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail()).password(user.getUserHashedPassword()).roles("USER").build();
    }

}
