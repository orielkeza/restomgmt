package com.restomgmt.site.user.services;

import com.restomgmt.site.user.models.UserNew;
import com.restomgmt.site.user.repositories.UserNewRepository;

import io.swagger.v3.oas.annotations.Parameter;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {
    //@Autowired
    @Parameter
    private UserNewRepository userRepository;

    public UserNew addUser (UserNew user) {
        return userRepository.save(user);
    }

    public List<UserNew> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<UserNew> findUserById(Long id) {
        return Optional.ofNullable(userRepository.getReferenceById(id));
    }

    public void deleteUser(UserNew user) {
        userRepository.delete(user);
    }

    public void updateUser(UserNew user) {
        userRepository.save(user);
    }
}