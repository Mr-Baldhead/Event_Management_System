package com.eventmanager.rest;

import com.eventmanager.entity.Troop;
import com.eventmanager.repository.TroopRepository;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

// REST resource for managing global troops
@Path("/troops")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TroopResource {

    @Inject
    private TroopRepository troopRepository;

    // Get all troops
    @GET
    public Response getAllTroops() {
        List<Troop> troops = troopRepository.findAll();
        return Response.ok(troops.stream().map(this::toDTO).toList()).build();
    }

    // Get troop by ID
    @GET
    @Path("/{id}")
    public Response getTroopById(@PathParam("id") Long id) {
        return troopRepository.findById(id)
                .map(troop -> Response.ok(toDTO(troop)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Troop not found"))
                        .build());
    }

    // Create new troop
    @POST
    @Transactional
    public Response createTroop(TroopDTO dto) {
        if (dto == null || dto.name == null || dto.name.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Name is required"))
                    .build();
        }

        String name = dto.name.trim();

        // Check if name already exists
        if (troopRepository.existsByName(name)) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", "A troop with this name already exists"))
                    .build();
        }

        Troop troop = new Troop(name);
        troop.setSortOrder(troopRepository.getMaxSortOrder() + 1);

        Troop saved = troopRepository.save(troop);
        return Response.status(Response.Status.CREATED).entity(toDTO(saved)).build();
    }

    // Update troop
    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateTroop(@PathParam("id") Long id, TroopDTO dto) {
        if (dto == null || dto.name == null || dto.name.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Name is required"))
                    .build();
        }

        String name = dto.name.trim();

        return troopRepository.findById(id)
                .map(troop -> {
                    // Check if new name conflicts with existing
                    if (!troop.getName().equals(name) && troopRepository.existsByName(name)) {
                        return Response.status(Response.Status.CONFLICT)
                                .entity(Map.of("error", "A troop with this name already exists"))
                                .build();
                    }

                    troop.setName(name);
                    if (dto.sortOrder != null) {
                        troop.setSortOrder(dto.sortOrder);
                    }

                    Troop updated = troopRepository.update(troop);
                    return Response.ok(toDTO(updated)).build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Troop not found"))
                        .build());
    }

    // Delete troop
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteTroop(@PathParam("id") Long id) {
        if (troopRepository.deleteById(id)) {
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error", "Troop not found"))
                .build();
    }

    // Convert entity to DTO
    private TroopDTO toDTO(Troop troop) {
        TroopDTO dto = new TroopDTO();
        dto.id = troop.getId();
        dto.name = troop.getName();
        dto.sortOrder = troop.getSortOrder();
        return dto;
    }

    // DTO class
    public static class TroopDTO {
        public Long id;
        public String name;
        public Integer sortOrder;
    }
}