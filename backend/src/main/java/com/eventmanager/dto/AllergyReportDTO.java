package com.eventmanager.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for allergy report data
 */
public class AllergyReportDTO {
    
    private Long eventId;
    private String eventName;
    private List<AllergyGroupDTO> allergies;
    private int totalParticipantsWithAllergies;
    private LocalDateTime generatedAt;

    // Constructors
    public AllergyReportDTO() {
    }

    public AllergyReportDTO(Long eventId, String eventName, List<AllergyGroupDTO> allergies, 
                            int totalParticipantsWithAllergies) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.allergies = allergies;
        this.totalParticipantsWithAllergies = totalParticipantsWithAllergies;
        this.generatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public List<AllergyGroupDTO> getAllergies() {
        return allergies;
    }

    public void setAllergies(List<AllergyGroupDTO> allergies) {
        this.allergies = allergies;
    }

    public int getTotalParticipantsWithAllergies() {
        return totalParticipantsWithAllergies;
    }

    public void setTotalParticipantsWithAllergies(int totalParticipantsWithAllergies) {
        this.totalParticipantsWithAllergies = totalParticipantsWithAllergies;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    /**
     * Inner class for allergy group data
     */
    public static class AllergyGroupDTO {
        private String allergyName;
        private int count;
        private List<AllergyParticipantDTO> participants;

        public AllergyGroupDTO() {
        }

        public AllergyGroupDTO(String allergyName, int count, List<AllergyParticipantDTO> participants) {
            this.allergyName = allergyName;
            this.count = count;
            this.participants = participants;
        }

        public String getAllergyName() {
            return allergyName;
        }

        public void setAllergyName(String allergyName) {
            this.allergyName = allergyName;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public List<AllergyParticipantDTO> getParticipants() {
            return participants;
        }

        public void setParticipants(List<AllergyParticipantDTO> participants) {
            this.participants = participants;
        }
    }

    /**
     * Inner class for participant data in allergy report
     */
    public static class AllergyParticipantDTO {
        private Long id;
        private String fullName;
        private String patrol;
        private String otherInfo;

        public AllergyParticipantDTO() {
        }

        public AllergyParticipantDTO(Long id, String fullName, String patrol, String otherInfo) {
            this.id = id;
            this.fullName = fullName;
            this.patrol = patrol;
            this.otherInfo = otherInfo;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getPatrol() {
            return patrol;
        }

        public void setPatrol(String patrol) {
            this.patrol = patrol;
        }

        public String getOtherInfo() {
            return otherInfo;
        }

        public void setOtherInfo(String otherInfo) {
            this.otherInfo = otherInfo;
        }
    }
}
