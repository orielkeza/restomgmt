package com.restomgmt.site.user.models;

//import java.time.LocalDate;
import jakarta.persistence.*;

//to extend to userdetails
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.CredentialsContainer;

import java.util.Collection;
//import java.util.List;
//import java.util.Objects;

import jakarta.validation.constraints.Email;

@Entity
//@Table(name = "users")
//public class UserNew implements UserDetails, CredentialsContainer {
public class UserNew {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String fullName;
    private String email;
    private String password;
    private String username;
    private boolean enabled;
    private boolean tokenExpired;
    //private Boolean accountNonExpired;
    //private Boolean credentialsNonExpired;
    //private Boolean accountNonLocked;
    //private Collection<? extends GrantedAuthority> authorities;  //used in Spring Security to manage user permissions and roles

    /* 
    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "role_id", referencedColumnName = "id", nullable = false)
    private RoleNew role;
    */

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
    private Collection<RoleNew> roles;

    public UserNew () {}

    public UserNew (String fullName,
                    String email,
                    String username,
                    String password //in case of passkeys
                    )
    {
        this.fullName = fullName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.enabled = true;
        this.tokenExpired = false;
        //this.accountNonExpired = true;
        //this.credentialsNonExpired = true;
        //this.accountNonLocked = true;
        //this.authorities = authorities;
    }

    //getters and setters

    public Long getId () {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    //@Override
    public String getPassword() {
        return password;
    }

    //@Override
    public String getUsername() {
        return username;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(@Email String email) {
        this.email = email;
    }


    public void setPassword(String password) {
        this.password = password;
    }


    public void setUsername(String username){
        this.username = username;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean getEnabled(){
        return enabled;
    }

    public void setToken(boolean expired) {
        this.tokenExpired = expired;
    }

    public boolean getTokenExpired(){
        return tokenExpired;
    }

    public Collection<RoleNew> getRoles(){
        return roles;
    }
//not too sure how this plays into it all
/* @Override
public User registerNewUserAccount(UserDto accountDto) throws EmailExistsException {
 
    if (emailExist(accountDto.getEmail())) {
        throw new EmailExistsException
          ("There is an account with that email adress: " + accountDto.getEmail());
    }
    User user = new User();

    user.setFirstName(accountDto.getFirstName());
    user.setLastName(accountDto.getLastName());
    user.setPassword(passwordEncoder.encode(accountDto.getPassword()));
    user.setEmail(accountDto.getEmail());

    user.setRoles(Arrays.asList(roleRepository.findByName("ROLE_USER")));
    return repository.save(user);
}*/

    /*@Override
    public Collection <? extends GrantedAuthority> getAuthorities(){
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired(){
        return accountNonExpired;
    }

    public void setAccountExpiry(boolean nonExpired) {
        this.accountNonExpired = nonExpired;
    }

    @Override
    public boolean isCredentialsNonExpired(){
        return credentialsNonExpired;
    }

    public void setCredentialsExpiry(boolean nonExpired) {
        this.credentialsNonExpired = nonExpired;
    }

    @Override
    public boolean isEnabled(){
        return enabled;
    }

    public void eraseCredentials(){

    }*/
}