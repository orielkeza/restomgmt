package com.restomgmt.site.user.security;

import com.restomgmt.site.user.models.UserNew;
import org.springframework.*;
import com.restomgmt.site.user.repositories.clientRepo;
import java.util.*;

public UserNew signup(RegisterUserDto input) {
    Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.USER);

    if (optionalRole.isEmpty()) {
        return null;
    }

    var user = new UserNew ()
        .setFullName(input.getFullName())
        .setEmail(input.getEmail())
        .setPassword(passwordEncoder.encode(input.getPassword())) //this is bcencrypting
        .setRole(optionalRole.get());

        return userNewRepository.save(user);
    }
