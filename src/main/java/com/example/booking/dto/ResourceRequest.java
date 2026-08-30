package com.example.booking.dto;

import jakarta.validation.constraints.NotBlank;

public class ResourceRequest {

    @NotBlank(message = "Resource name is required")
    private String name;

    private String description;

    public ResourceRequest() {
    }

    public ResourceRequest(String name, String description) {
        this.name = name;
        this.description = description;
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
}
