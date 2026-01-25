package com.eventmanager.service;

import com.eventmanager.dto.RegistrationDTO;
import com.eventmanager.dto.RegistrationDTO.AllergyInfo;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Service for managing registrations and fetching participant data
 */
@Stateless
public class RegistrationService {

    @PersistenceContext(unitName = "eventPU")
    private EntityManager em;

    /**
     * Get all registrations for an event with participant details and allergies
     * Sorted by lastName, firstName
     */
    public List<RegistrationDTO> getRegistrationsForEvent(Long eventId) {
        // Get all registrations with participant info - sorted by lastName, firstName
        String sql = """
            SELECT 
                r.id as reg_id,
                r.status,
                r.registration_date,
                r.confirmation_date,
                r.notes,
                p.id as participant_id,
                p.first_name,
                p.last_name,
                p.email,
                p.phone,
                p.birth_date,
                p.personal_number,
                p.street_address,
                p.postal_code,
                p.city,
                p.guardian_name,
                p.guardian_email,
                p.guardian_phone,
                pat.name as patrol_name
            FROM registrations r
            JOIN participants p ON r.participant_id = p.id
            LEFT JOIN patrols pat ON p.patrol_id = pat.id
            WHERE r.event_id = ?
            ORDER BY p.last_name ASC, p.first_name ASC
            """;

        Query query = em.createNativeQuery(sql);
        query.setParameter(1, eventId);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        // Build map of registrations
        Map<Long, RegistrationDTO> registrationMap = new LinkedHashMap<>();

        for (Object[] row : results) {
            Long regId = ((Number) row[0]).longValue();

            RegistrationDTO dto = new RegistrationDTO();
            dto.setId(regId);
            dto.setEventId(eventId);
            dto.setStatus((String) row[1]);
            dto.setRegistrationDate(toLocalDateTime(row[2]));
            dto.setConfirmationDate(toLocalDateTime(row[3]));
            dto.setNotes((String) row[4]);
            dto.setParticipantId(((Number) row[5]).longValue());
            dto.setFirstName((String) row[6]);
            dto.setLastName((String) row[7]);
            dto.setEmail((String) row[8]);
            dto.setPhone((String) row[9]);
            dto.setBirthDate(row[10] != null ? row[10].toString() : null);
            dto.setPersonalNumber((String) row[11]);
            dto.setStreetAddress((String) row[12]);
            dto.setPostalCode((String) row[13]);
            dto.setCity((String) row[14]);
            dto.setGuardianName((String) row[15]);
            dto.setGuardianEmail((String) row[16]);
            dto.setGuardianPhone((String) row[17]);
            dto.setPatrolName((String) row[18]);
            dto.setAllergies(new ArrayList<>());

            registrationMap.put(dto.getParticipantId(), dto);
        }

        // Get allergies for all participants in this event
        if (!registrationMap.isEmpty()) {
            String allergySql = """
                SELECT 
                    pa.participant_id,
                    a.id as allergy_id,
                    a.name,
                    a.severity
                FROM participant_allergens pa
                JOIN allergens a ON pa.allergen_id = a.id
                WHERE pa.participant_id IN (
                    SELECT p.id FROM participants p
                    JOIN registrations r ON r.participant_id = p.id
                    WHERE r.event_id = ?
                )
                ORDER BY a.severity DESC, a.name
                """;

            Query allergyQuery = em.createNativeQuery(allergySql);
            allergyQuery.setParameter(1, eventId);

            @SuppressWarnings("unchecked")
            List<Object[]> allergyResults = allergyQuery.getResultList();

            for (Object[] row : allergyResults) {
                Long participantId = ((Number) row[0]).longValue();
                RegistrationDTO dto = registrationMap.get(participantId);

                if (dto != null) {
                    AllergyInfo allergy = new AllergyInfo(
                            ((Number) row[1]).longValue(),
                            (String) row[2],
                            (String) row[3]
                    );
                    dto.getAllergies().add(allergy);
                }
            }
        }

        return new ArrayList<>(registrationMap.values());
    }

    /**
     * Count registrations for an event
     */
    public int countRegistrationsForEvent(Long eventId) {
        Query query = em.createNativeQuery(
                "SELECT COUNT(*) FROM registrations WHERE event_id = ?");
        query.setParameter(1, eventId);
        return ((Number) query.getSingleResult()).intValue();
    }

    /**
     * Count confirmed registrations for an event
     */
    public int countConfirmedRegistrations(Long eventId) {
        Query query = em.createNativeQuery(
                "SELECT COUNT(*) FROM registrations WHERE event_id = ? AND status = 'CONFIRMED'");
        query.setParameter(1, eventId);
        return ((Number) query.getSingleResult()).intValue();
    }

    /**
     * Delete a registration
     */
    @Transactional
    public boolean deleteRegistration(Long eventId, Long registrationId) {
        // Verify registration belongs to event
        Query checkQuery = em.createNativeQuery(
                "SELECT COUNT(*) FROM registrations WHERE id = ? AND event_id = ?");
        checkQuery.setParameter(1, registrationId);
        checkQuery.setParameter(2, eventId);

        int count = ((Number) checkQuery.getSingleResult()).intValue();
        if (count == 0) {
            return false;
        }

        // Get participant ID before deleting registration
        Query participantQuery = em.createNativeQuery(
                "SELECT participant_id FROM registrations WHERE id = ?");
        participantQuery.setParameter(1, registrationId);
        Long participantId = ((Number) participantQuery.getSingleResult()).longValue();

        // Delete registration
        Query deleteRegQuery = em.createNativeQuery(
                "DELETE FROM registrations WHERE id = ?");
        deleteRegQuery.setParameter(1, registrationId);
        deleteRegQuery.executeUpdate();

        // Check if participant has other registrations
        Query otherRegQuery = em.createNativeQuery(
                "SELECT COUNT(*) FROM registrations WHERE participant_id = ?");
        otherRegQuery.setParameter(1, participantId);
        int otherRegistrations = ((Number) otherRegQuery.getSingleResult()).intValue();

        // If no other registrations, optionally delete participant
        // For now, we keep the participant for data integrity
        // Uncomment below to also delete participant:
        /*
        if (otherRegistrations == 0) {
            // Delete participant allergies first
            Query deleteAllergiesQuery = em.createNativeQuery(
                "DELETE FROM participant_allergens WHERE participant_id = ?");
            deleteAllergiesQuery.setParameter(1, participantId);
            deleteAllergiesQuery.executeUpdate();

            // Delete participant
            Query deletePartQuery = em.createNativeQuery(
                "DELETE FROM participants WHERE id = ?");
            deletePartQuery.setParameter(1, participantId);
            deletePartQuery.executeUpdate();
        }
        */

        return true;
    }

    /**
     * Generate Excel file with participant list
     */
    public byte[] generateExcel(Long eventId) throws IOException {
        List<RegistrationDTO> registrations = getRegistrationsForEvent(eventId);

        // Get event name
        Query eventQuery = em.createNativeQuery("SELECT name FROM events WHERE id = ?");
        eventQuery.setParameter(1, eventId);
        String eventName = (String) eventQuery.getSingleResult();

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Deltagare");

            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            int rowNum = 0;

            // Title row
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Deltagarlista - " + eventName);
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);

            // Empty row
            rowNum++;

            // Header row
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Efternamn", "Förnamn", "Adress", "Postnummer", "Ort", "Kår/Patrull", "E-post", "Mobil", "Målsman"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            for (RegistrationDTO reg : registrations) {
                Row dataRow = sheet.createRow(rowNum++);

                createCell(dataRow, 0, reg.getLastName(), dataStyle);
                createCell(dataRow, 1, reg.getFirstName(), dataStyle);
                createCell(dataRow, 2, reg.getStreetAddress(), dataStyle);
                createCell(dataRow, 3, reg.getPostalCode(), dataStyle);
                createCell(dataRow, 4, reg.getCity(), dataStyle);
                createCell(dataRow, 5, reg.getPatrolName(), dataStyle);
                createCell(dataRow, 6, reg.getEmail(), dataStyle);
                createCell(dataRow, 7, reg.getPhone(), dataStyle);
                createCell(dataRow, 8, reg.getGuardianName(), dataStyle);
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    // Helper to convert SQL timestamp to LocalDateTime
    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof Timestamp) {
            return ((Timestamp) value).toLocalDateTime();
        }
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        return null;
    }
}