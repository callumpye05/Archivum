package com.cal.archivum.service;

import com.cal.archivum.entity.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public interface UserDetailsService {

    public UserDetails loadUserByUsername(String userName) throws Exception;
    public UserDetails loadUserByEmail(String userName) throws Exception;
}
