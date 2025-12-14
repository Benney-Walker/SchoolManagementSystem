package com.codewithben.schoolmanagementsystem.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class EntityID_generation {

    @Id
    private String entityName;

    private long code;

    public EntityID_generation(String entityName, long code) {
        this.entityName = entityName;
        this.code = code;
    }

    public EntityID_generation() {}

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }
}
