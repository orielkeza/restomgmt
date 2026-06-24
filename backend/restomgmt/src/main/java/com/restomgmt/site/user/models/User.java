package com.restomgmt.site.user.models;

import jakarta.persistence.*;

import java.util.Collection;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
//@Table(name = "users")
//public class UserNew implements UserDetails, CredentialsContainer {
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private @Email String email;
    private String password;
    
    @Column(unique = true, nullable = false)
    private String username;

    private Boolean enabled;
    private Boolean tokenExpired;

    @ManyToMany
    @JoinTable(
        name = "users_roles",
        joinColumns = @JoinColumn(
            name = "user_id", referencedColumnName = "id"
        ),
        inverseJoinColumns = @JoinColumn(
            name = "role_id", referencedColumnName ="id"
        )
    )
    private Collection<Role> roles;

    //getters and setters no longer necessary with lombok and constructor injection
}