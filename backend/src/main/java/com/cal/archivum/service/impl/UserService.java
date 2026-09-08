package com.cal.archivum.service.impl;

import com.cal.archivum.dto.impl.CreateUserDto;
import com.cal.archivum.dto.impl.UpdateUserDto;
import com.cal.archivum.entity.User;
import com.cal.archivum.exception.EmailAlreadyUsed;
import com.cal.archivum.exception.UserNotFoundByEmailOrUsername;
import com.cal.archivum.exception.UsernameAlreadyUsed;
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

    public User createUser(CreateUserDto createUser) {
        return userRepo.save(fromCreateDto(createUser));
    }




    @Override

    public User updateUser(UpdateUserDto updateUser) {

        User user = getCurrentUser();
        updateFromDto(user , updateUser);
        if (updateUser.password() !=null) {
            updatePassword(user , updateUser.password());
        }
        return userRepo.save(user);

    }

    @Override

    public void deleteUser() {
        userRepo.delete(getCurrentUser());
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication =SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepo.findByEmail(email).orElseThrow(() -> new UserNotFoundByEmailOrUsername(email));
    }


    private User fromCreateDto(CreateUserDto dto) {
        if (userRepo.existsByEmail(dto.email())) {
            throw new EmailAlreadyUsed("This email is used");
        }

        if(userRepo.existsByUserName(dto.userName())) {
            throw new UsernameAlreadyUsed("Username is already used");
        }

        User user = new User();
        user.setEmail(dto.email());
        user.setUserName(dto.userName());
        user.setUserHashedPassword(passwordEncoder.encode(dto.password()));
        return user;
    }

    private void updateFromDto(User user, UpdateUserDto dto) {

        if (dto.email() != null) {
            if (userRepo.existsByEmailAndIdNot(dto.email(), user.getId())) {
                throw new EmailAlreadyUsed("This email is used");
            }
            user.setEmail(dto.email());
        }

        if(dto.userName() != null) {

            if(userRepo.existsByUserNameAndIdNot(dto.userName(), user.getId())) {
                throw new UsernameAlreadyUsed("Username is already used");
            }
            user.setUserName(dto.userName());
        }
    }

    private void updatePassword(User user, String rawPassword) {
        user.setUserHashedPassword(passwordEncoder.encode(rawPassword));
    }
}
