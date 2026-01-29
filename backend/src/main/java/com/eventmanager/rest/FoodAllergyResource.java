package com.eventmanager.rest;

import com.eventmanager.entity.FoodAllergy;
import com.eventmanager.repository.FoodAllergyRepository;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

// REST resource for managing global food allergies
@Path("/food-allergies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FoodAllergyResource {

    @Inject
    private FoodAllergyRepository allergyRepository;

    // Get all food allergies
    @GET
    public Response getAllAllergies() {
        List<FoodAllergy> allergies = allergyRepository.findAll();
        return Response.ok(allergies.stream().map(this::toDTO).toList()).build();
    }

    // Get allergy by ID
    @GET
    @Path("/{id}")
    public Response getAllergyById(@PathParam("id") Long id) {
        return allergyRepository.findById(id)
                .map(allergy -> Response.ok(toDTO(allergy)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Food allergy not found"))
                        .build());
    }

    // Create new food allergy
    @POST
    @Transactional
    public Response createAllergy(FoodAllergyDTO dto) {
        if (dto == null || dto.name == null || dto.name.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Name is required"))
                    .build();
        }

        String name = dto.name.trim();

        // Check if name already exists
        if (allergyRepository.existsByName(name)) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", "This food allergy already exists"))
                    .build();
        }

        FoodAllergy allergy = new FoodAllergy(name);
        allergy.setSortOrder(allergyRepository.getMaxSortOrder() + 1);

        FoodAllergy saved = allergyRepository.save(allergy);
        return Response.status(Response.Status.CREATED).entity(toDTO(saved)).build();
    }

    // Update food allergy
    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateAllergy(@PathParam("id") Long id, FoodAllergyDTO dto) {
        if (dto == null || dto.name == null || dto.name.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Name is required"))
                    .build();
        }

        String name = dto.name.trim();

        return allergyRepository.findById(id)
                .map(allergy -> {
                    // Check if new name conflicts with existing
                    if (!allergy.getName().equals(name) && allergyRepository.existsByName(name)) {
                        return Response.status(Response.Status.CONFLICT)
                                .entity(Map.of("error", "This food allergy already exists"))
                                .build();
                    }

                    allergy.setName(name);
                    if (dto.sortOrder != null) {
                        allergy.setSortOrder(dto.sortOrder);
                    }

                    FoodAllergy updated = allergyRepository.update(allergy);
                    return Response.ok(toDTO(updated)).build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Food allergy not found"))
                        .build());
    }

    // Delete food allergy
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteAllergy(@PathParam("id") Long id) {
        if (allergyRepository.deleteById(id)) {
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error", "Food allergy not found"))
                .build();
    }

    // Convert entity to DTO
    private FoodAllergyDTO toDTO(FoodAllergy allergy) {
        FoodAllergyDTO dto = new FoodAllergyDTO();
        dto.id = allergy.getId();
        dto.name = allergy.getName();
        dto.sortOrder = allergy.getSortOrder();
        return dto;
    }

    // DTO class
    public static class FoodAllergyDTO {
        public Long id;
        public String name;
        public Integer sortOrder;
    }
}