package com.eventmanager.config;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

// CORS filter to allow Angular frontend to communicate with the API
@Provider
public class CorsFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) 
            throws IOException {
        
        // Allow requests from Angular dev server and production
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        
        // Allow common HTTP methods
        responseContext.getHeaders().add("Access-Control-Allow-Methods", 
            "GET, POST, PUT, DELETE, PATCH, OPTIONS");
        
        // Allow common headers
        responseContext.getHeaders().add("Access-Control-Allow-Headers", 
            "Origin, Content-Type, Accept, Authorization, X-Requested-With");
        
        // Allow credentials
        responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
        
        // Cache preflight response for 24 hours
        responseContext.getHeaders().add("Access-Control-Max-Age", "86400");
    }
}
