package com.eventmanager.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

// JAX-RS Application configuration - sets the base path for all REST endpoints
@ApplicationPath("/api")
public class JaxRsApplication extends Application {
    // All REST resources are automatically discovered via CDI
}
