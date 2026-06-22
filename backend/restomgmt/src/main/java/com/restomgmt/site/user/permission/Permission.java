package com.restomgmt.site.user.permission;

import com.restomgmt.site.user.models.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Permission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;

    @ManyToMany(mappedBy = "permissions")
    private Collection<Role> roles;
    /*
    public Permission() {}

    public Permission(String name) {
        this.name = name;
    }
    */
}
