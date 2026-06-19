/*package com.restomgmt.site.user.services;

import java.security.Permission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restomgmt.site.user.models.User;
import com.restomgmt.site.user.models.Role;
import com.restomgmt.site.user.repositories.RoleRepository;
import com.restomgmt.site.user.repositories.UserRepository;
import com.restomgmt.site.user.services.UserService;

import io.jsonwebtoken.lang.Arrays;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

//the mapping used here allows security configuration to be flexible and powerful
//roles and privileges can be mixed and matched as much as we want to personalize

@Service("userDetailsService")
@Transactional
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;

    private final UserService service;

    private final MessageSource messages;

    private final RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
        throws UsernameNotFoundException {
            User user = userRepository.findByEmail("email@email.com");
            if(user == null) {
                return new org.springframework.security.core.userdetails.User(
                    "", "", true, true, true, true,
                    getAuthorities(Arrays.asList(
                        roleRepository.findByName("ROLE_USER")
                    )));
                
            }

            return new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPassword(), user.getEnabled(), true, true, true, getAuthorities(user.getRoles()));
        }

        private List<GrantedAuthority> getGrantedAuthorities(List<String> permissions) {
            List<GrantedAuthority> authorities = new ArrayList<>();
            for (String permission : permissions) {
                authorities.add(new SimpleGrantedAuthority(permission));
            }
            return authorities;
        }

        private Collection<? extends GrantedAuthority> getAuthorities(
            Collection<Role> roles) {
                return getGrantedAuthorites(getPermissions(roles));
        }

        private List<String> getPermissions(Collection<Role> roles) {

            List<String> permissions = new ArrayList<>();
            List<Permission> collection = new ArrayList<>();
            for (Role role : roles) {
                permissions.add(role.getName());
                collection.addAll(role.getPermissions());
            }
            for (Permission item : collection) {
                permissions.add(item.getName());
            }
            return permissions;
        }

}
*/