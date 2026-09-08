package com.cal.archivum.service.impl;

import com.cal.archivum.dto.impl.CreateUserDto;
import com.cal.archivum.dto.impl.UpdateUserDto;
import com.cal.archivum.entity.User;
import com.cal.archivum.exception.UserNotFoundByEmailOrUsername;
import com.cal.archivum.repository.UserRepository;
import com.cal.archivum.service.IUserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService {

    private final UserRepository userRepo;


    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    //TODO : prevent duplicate emails and users in registration
    public User createUser(CreateUserDto createUser) {
            return userRepo.save(fromCreateDto(createUser));
    }

    @Override
    //TODO : Prevent duplicate emails and users during pdate
    public User updateUser(UpdateUserDto updateUser) {

        Authentication authenticated= SecurityContextHolder.getContext().getAuthentication();
        String email = authenticated.getName();
        User user = userRepo.findByEmail(email).orElseThrow(()-> new UserNotFoundByEmailOrUsername(email));
        updateFromDto(user , updateUser);
        if (updateUser.password() !=null) {
            updatePassword(user , updateUser.password());
        }
        return userRepo.save(user);

    }

    @Override
    //TODO : Simplify with getCurrentUser
    public void deleteUser() {
        Authentication authentication =SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepo.findByEmail(email).orElseThrow(() -> new UserNotFoundByEmailOrUsername(email));
        userRepo.delete(user);
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication =SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepo.findByEmail(email).orElseThrow(() -> new UserNotFoundByEmailOrUsername(email));
    }


    private User fromCreateDto(CreateUserDto dto) {
        User user = new User();
        user.setEmail(dto.email());
        user.setUserName(dto.userName());
        user.setUserHashedPassword(passwordEncoder.encode(dto.password()));
        return user;
    }

    private void updateFromDto(User user, UpdateUserDto dto) {

        if (dto.email() != null) {
            user.setEmail(dto.email());
        }

        if (dto.userName() != null) {
            user.setUserName(dto.userName());
        }
    }

    private void updatePassword(User user, String rawPassword) {
        user.setUserHashedPassword(passwordEncoder.encode(rawPassword));
    }
}
