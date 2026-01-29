package com.eventmanager.rest;

import com.eventmanager.dto.*;
import com.eventmanager.exception.AuthenticationException;
import com.eventmanager.service.AuthService;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.Map;

// REST resource for authentication endpoints
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final String AUTH_COOKIE_NAME = "SESSION_TOKEN";
    private static final int COOKIE_MAX_AGE = 30 * 60; // 30 minutes

    @Inject
    private AuthService authService;

    @Context
    private HttpServletRequest httpRequest;

    // Login endpoint
    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest request) {
        try {
            String ipAddress = getClientIpAddress();
            String userAgent = httpRequest.getHeader("User-Agent");

            LoginResponse response = authService.login(request, ipAddress, userAgent);

            // Create session cookie
            NewCookie sessionCookie = new NewCookie.Builder(AUTH_COOKIE_NAME)
                    .value(response.getToken())
                    .path("/")
                    .maxAge(COOKIE_MAX_AGE)
                    .httpOnly(true)
                    .secure(false) // Set to true in production with HTTPS
                    .sameSite(NewCookie.SameSite.LAX)
                    .build();

            return Response.ok(response)
                    .cookie(sessionCookie)
                    .build();

        } catch (AuthenticationException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    // Logout endpoint
    @POST
    @Path("/logout")
    public Response logout(@CookieParam(AUTH_COOKIE_NAME) String sessionToken) {
        if (sessionToken != null && !sessionToken.isEmpty()) {
            authService.logout(sessionToken);
        }

        // Clear cookie
        NewCookie clearedCookie = new NewCookie.Builder(AUTH_COOKIE_NAME)
                .value("")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .build();

        return Response.ok(Map.of("message", "Logged out successfully"))
                .cookie(clearedCookie)
                .build();
    }

    // Get current user
    @GET
    @Path("/me")
    public Response getCurrentUser(@CookieParam(AUTH_COOKIE_NAME) String sessionToken,
                                   @HeaderParam("Authorization") String authHeader) {
        try {
            String token = resolveToken(sessionToken, authHeader);
            if (token == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "Not authenticated"))
                        .build();
            }

            UserDTO user = authService.getCurrentUser(token);
            return Response.ok(user).build();

        } catch (AuthenticationException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    // Change password
    @POST
    @Path("/change-password")
    public Response changePassword(@CookieParam(AUTH_COOKIE_NAME) String sessionToken,
                                   @HeaderParam("Authorization") String authHeader,
                                   @Valid ChangePasswordRequest request) {
        try {
            String token = resolveToken(sessionToken, authHeader);
            if (token == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("error", "Not authenticated"))
                        .build();
            }

            UserDTO user = authService.getCurrentUser(token);
            authService.changePassword(user.getId(), request);

            return Response.ok(Map.of("message", "Password changed successfully")).build();

        } catch (AuthenticationException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    // Validate session
    @GET
    @Path("/validate")
    public Response validateSession(@CookieParam(AUTH_COOKIE_NAME) String sessionToken,
                                    @HeaderParam("Authorization") String authHeader) {
        String token = resolveToken(sessionToken, authHeader);
        if (token == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("valid", false))
                    .build();
        }

        boolean valid = authService.validateSession(token).isPresent();
        return Response.ok(Map.of("valid", valid)).build();
    }

    // Helper: Resolve token from cookie or header
    private String resolveToken(String cookieToken, String authHeader) {
        // First try cookie
        if (cookieToken != null && !cookieToken.isEmpty()) {
            return cookieToken;
        }

        // Then try Authorization header
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }

    // Helper: Get client IP address
    private String getClientIpAddress() {
        String xForwardedFor = httpRequest.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return httpRequest.getRemoteAddr();
    }
}
