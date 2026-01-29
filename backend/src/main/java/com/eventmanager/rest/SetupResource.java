package com.eventmanager.rest;

import com.eventmanager.entity.User;
import com.eventmanager.repository.UserRepository;
import com.eventmanager.service.PasswordService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

// Temporary endpoint to reset superadmin password - REMOVE IN PRODUCTION
@Path("/setup")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SetupResource {

    @Inject
    private UserRepository userRepository;

    @Inject
    private PasswordService passwordService;

    @POST
    @Path("/reset-superadmin")
    @Transactional
    public Response resetSuperadminPassword() {
        User superadmin = userRepository.findByEmail("superadmin@eventmanager.se")
                .orElse(null);

        if (superadmin == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "SuperAdmin not found"))
                    .build();
        }

        // Set new password
        String newPassword = "Admin123!";
        String hash = passwordService.hashPassword(newPassword);
        
        superadmin.setPasswordHash(hash);
        superadmin.setMustChangePassword(true);
        superadmin.setLocked(false);
        superadmin.setFailedLoginAttempts(0);
        userRepository.update(superadmin);

        return Response.ok(Map.of(
                "message", "SuperAdmin password reset successfully",
                "email", "superadmin@eventmanager.se",
                "password", newPassword,
                "hash", hash
        )).build();
    }
}
