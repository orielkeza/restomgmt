package com.restomgmt.site.user.models;

import java.time.LocalDate;
import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class RoleNew {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unqiue = true, nullable = false)
    private RoleEnum name;

    @column(nullable = false)
    private String description;

    @CreationTimeStamp
    @Column(updateable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @column(name = "updated_at")
    private Date updatedAt;

    //add the getters and the setters

    public RoleNew() {}

    public RoleNew(Long id, RoleEnum name, String description, Date createdAt, Date updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public RoleEnum getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}