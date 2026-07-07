package com.restomgmt.site.user.models;

import jakarta.persistence.*;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "roles")
//@Table(name = "users")
//public class UserNew implements UserDetails, CredentialsContainer {
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @NotBlank
    private @Email String email;

    @NotBlank
    private String password;
    
    @NotBlank
    @Column(unique = true, nullable = false)
    private String username;

    private Boolean enabled;
    private Boolean tokenExpired;

    @JsonIgnore
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