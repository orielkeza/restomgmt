package com.restomgmt.site.user.security;

import com.restomgmt.site.user.models.UserNew;
import com.restomgmt.site.user.repositories.UserNewRepository;
import com.restomgmt.site.user.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;

import java.util.ArrayList;

public class AuthenticationService implements UserDetailsService {
    
    @Autowired
    private UserNewRepository userNewRepository;

    @Autowired 
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String authenticate(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        final UserDetails userDetails = loadUserByUsername(username);
        return jwtUtil.generateToken(userDetails.getUsername());    
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserNew user = userNewRepository.findByUsername(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new org.springframework.security.core.userdetails.User(user.getUsername(),
                                                                      user.getPassword(),
                                                                      new ArrayList<>());
    }

    public UserNew registerUser (UserNew user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userNewRepository.save(user);
    }
}

/*public UserNew signup(RegisterUserDto input) {
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
*/