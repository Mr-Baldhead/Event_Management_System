package com.eventmanager.exception;

// Exception thrown when a requested resource is not found
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceType;
    private final Long resourceId;

    public ResourceNotFoundException(String resourceType, Long resourceId) {
        super(resourceType + " not found with id: " + resourceId);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceType = null;
        this.resourceId = null;
    }

    public String getResourceType() {
        return resourceType;
    }

    public Long getResourceId() {
        return resourceId;
    }
}
