package com.eventmanager.rest;

import com.eventmanager.dto.CreateUserRequest;
import com.eventmanager.dto.UserDTO;
import com.eventmanager.entity.UserRole;
import com.eventmanager.exception.AuthenticationException;
import com.eventmanager.exception.DuplicateResourceException;
import com.eventmanager.exception.ResourceNotFoundException;
import com.eventmanager.service.AuthService;
import com.eventmanager.service.UserService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

// REST resource for user management (SuperAdmin only)
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    private static final String AUTH_COOKIE_NAME = "SESSION_TOKEN";

    @Inject
    private UserService userService;

    @Inject
    private AuthService authService;

    // Get all admins
    @GET
    public Response getAllAdmins(@CookieParam(AUTH_COOKIE_NAME) String sessionToken,
                                 @HeaderParam("Authorization") String authHeader) {
        try {
            validateSuperAdmin(sessionToken, authHeader);
            List<UserDTO> admins = userService.getAllAdmins();
            return Response.ok(admins).build();
        } catch (AuthenticationException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    // Get user counts
    @GET
    @Path("/counts")
    public Response getUserCounts(@CookieParam(AUTH_COOKIE_NAME) String sessionToken,
                                  @HeaderParam("Authorization") String authHeader) {
        try {
            validateSuperAdmin(sessionToken, authHeader);
            UserService.UserCounts counts = userService.getUserCounts();
            return Response.ok(Map.of(
                    "total", counts.total,
                    "admins", counts.admins,
                    "superadmins", counts.superadmins
            )).build();
        } catch (AuthenticationException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    // Get user by ID
    @GET
    @Path("/{id}")
    public Response getUserById(@PathParam("id") Long id,
                                @CookieParam(AUTH_COOKIE_NAME) String sessionToken,
                                @HeaderParam("Authorization") String authHeader) {
        try {
            validateSuperAdmin(sessionToken, authHeader);
            UserDTO user = userService.getUserById(id);
            return Response.ok(user).build();
        } catch (AuthenticationException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    // Create new admin
    @POST
    public Response createAdmin(@Valid CreateUserRequest request,
                                @CookieParam(AUTH_COOKIE_NAME) String sessionToken,
                                @HeaderParam("Authorization") String authHeader) {
        try {
            validateSuperAdmin(sessionToken, authHeader);
            UserDTO created = userService.createAdmin(request);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (AuthenticationException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (DuplicateResourceException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    // Update user
    @PUT
    @Path("/{id}")
    public Response updateUser(@PathParam("id") Long id,
                               @Valid UserDTO dto,
                               @CookieParam(AUTH_COOKIE_NAME) String sessionToken,
                               @HeaderParam("Authorization") String authHeader) {
        try {
            validateSuperAdmin(sessionToken, authHeader);
            UserDTO updated = userService.updateUser(id, dto);
            return Response.ok(updated).build();
        } catch (AuthenticationException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (DuplicateResourceException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    // Delete user
    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") Long id,
                               @CookieParam(AUTH_COOKIE_NAME) String sessionToken,
                               @HeaderParam("Authorization") String authHeader) {
        try {
            validateSuperAdmin(sessionToken, authHeader);
            userService.deleteUser(id);
            return Response.noContent().build();
        } catch (AuthenticationException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    // Lock/unlock user
    @PUT
    @Path("/{id}/lock")
    public Response setUserLocked(@PathParam("id") Long id,
                                  @QueryParam("locked") boolean locked,
                                  @CookieParam(AUTH_COOKIE_NAME) String sessionToken,
                                  @HeaderParam("Authorization") String authHeader) {
        try {
            validateSuperAdmin(sessionToken, authHeader);
            UserDTO updated = userService.setUserLocked(id, locked);
            return Response.ok(updated).build();
        } catch (AuthenticationException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    // Reset user password
    @POST
    @Path("/{id}/reset-password")
    public Response resetPassword(@PathParam("id") Long id,
                                  @CookieParam(AUTH_COOKIE_NAME) String sessionToken,
                                  @HeaderParam("Authorization") String authHeader) {
        try {
            validateSuperAdmin(sessionToken, authHeader);
            String newPassword = userService.resetUserPassword(id);
            return Response.ok(Map.of(
                    "message", "Password reset successfully",
                    "newPassword", newPassword
            )).build();
        } catch (AuthenticationException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (ResourceNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    // Helper: Validate that user is SuperAdmin
    private void validateSuperAdmin(String cookieToken, String authHeader) {
        String token = resolveToken(cookieToken, authHeader);
        if (token == null) {
            throw new AuthenticationException("Not authenticated");
        }

        if (!authService.hasRole(token, UserRole.SUPERADMIN)) {
            throw new AuthenticationException("Insufficient permissions. SuperAdmin required.");
        }
    }

    // Helper: Resolve token from cookie or header
    private String resolveToken(String cookieToken, String authHeader) {
        if (cookieToken != null && !cookieToken.isEmpty()) {
            return cookieToken;
        }
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
