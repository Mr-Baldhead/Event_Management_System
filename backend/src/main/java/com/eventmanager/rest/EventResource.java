package com.eventmanager.rest;

import com.eventmanager.dto.EventDTO;
import com.eventmanager.dto.EventPatchDTO;
import com.eventmanager.service.EventService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * REST resource for Event operations
 */
@Path("/events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventResource {

    @Inject
    private EventService eventService;

    @GET
    public List<EventDTO> getAllEvents() {
        return eventService.findAll();
    }

    @GET
    @Path("/{id}")
    public Response getEvent(@PathParam("id") Long id) {
        return eventService.findByIdOptional(id)
                .map(event -> Response.ok(event).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    public Response createEvent(@Valid EventDTO eventDTO) {
        EventDTO created = eventService.create(eventDTO);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateEvent(@PathParam("id") Long id, @Valid EventDTO eventDTO) {
        return eventService.updateOptional(id, eventDTO)
                .map(updated -> Response.ok(updated).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    /**
     * PATCH endpoint for partial updates (e.g., toggle active status)
     */
    @PATCH
    @Path("/{id}")
    public Response patchEvent(@PathParam("id") Long id, EventPatchDTO patchDTO) {
        return eventService.patch(id, patchDTO)
                .map(updated -> Response.ok(updated).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @DELETE
    @Path("/{id}")
    public Response deleteEvent(@PathParam("id") Long id) {
        boolean deleted = eventService.deleteIfExists(id);
        if (deleted) {
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}