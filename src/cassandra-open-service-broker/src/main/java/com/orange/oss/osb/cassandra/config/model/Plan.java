package com.orange.oss.osb.cassandra.config.model;

import java.util.HashMap;
import java.util.Map;

public class Plan {

    private static final String PLAN_NAME_DEFAULT = "default";

    private String id;

    private String name = PLAN_NAME_DEFAULT;

    private String description;

    private Map<String, Object> metadata = new HashMap<>();

    private Boolean bindable;

    private Boolean free = Boolean.TRUE;

    public static String getPlanNameDefault() {
        return PLAN_NAME_DEFAULT;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public Boolean getBindable() {
        return bindable;
    }

    public void setBindable(Boolean bindable) {
        this.bindable = bindable;
    }

    public Boolean getFree() {
        return free;
    }

    public void setFree(Boolean free) {
        this.free = free;
    }

    public Plan() {
    }

}
