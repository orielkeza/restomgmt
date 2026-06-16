package com.restomgmt.site.user.permission;

import com.restomgmt.site.user.models.RoleNew;
import jakarta.persistence.*;

import java.util.Collection;

public class Permission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;

    @ManyToMany(mappedBy = "permissions")
    private Collection<RoleNew> roles;

    public Permission() {}

    public Permission(String name) {
        this.name = name;
    }

}
