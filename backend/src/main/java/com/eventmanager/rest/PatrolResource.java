package com.eventmanager.rest;

import com.eventmanager.dto.PatrolDTO;
import com.eventmanager.service.PatrolService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

// REST resource for Patrol (scout troop) endpoints
@Path("/patrols")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PatrolResource {

    @Inject
    private PatrolService patrolService;

    // GET /api/patrols - List all patrols
    @GET
    public List<PatrolDTO> getAllPatrols() {
        return patrolService.findAll();
    }

    // GET /api/patrols/{id} - Get patrol by ID
    @GET
    @Path("/{id}")
    public PatrolDTO getPatrolById(@PathParam("id") Long id) {
        return patrolService.findById(id);
    }

    // GET /api/patrols/{id}/with-participants - Get patrol with participants
    @GET
    @Path("/{id}/with-participants")
    public PatrolDTO getPatrolWithParticipants(@PathParam("id") Long id) {
        return patrolService.findByIdWithParticipants(id);
    }

    // GET /api/patrols/event/{eventId} - Get patrols by event
    @GET
    @Path("/event/{eventId}")
    public List<PatrolDTO> getPatrolsByEvent(@PathParam("eventId") Long eventId) {
        return patrolService.findByEventId(eventId);
    }

    // GET /api/patrols/search - Search patrols by name
    @GET
    @Path("/search")
    public List<PatrolDTO> searchPatrols(@QueryParam("name") String name) {
        if (name == null || name.isBlank()) {
            return patrolService.findAll();
        }
        return patrolService.searchByName(name);
    }

    // POST /api/patrols - Create new patrol
    @POST
    public Response createPatrol(@Valid PatrolDTO patrolDTO) {
        PatrolDTO created = patrolService.create(patrolDTO);
        return Response.created(URI.create("/api/patrols/" + created.getId()))
            .entity(created)
            .build();
    }

    // PUT /api/patrols/{id} - Update patrol
    @PUT
    @Path("/{id}")
    public PatrolDTO updatePatrol(@PathParam("id") Long id, @Valid PatrolDTO patrolDTO) {
        return patrolService.update(id, patrolDTO);
    }

    // DELETE /api/patrols/{id} - Delete patrol
    @DELETE
    @Path("/{id}")
    public Response deletePatrol(@PathParam("id") Long id) {
        patrolService.delete(id);
        return Response.noContent().build();
    }
}
