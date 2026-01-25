package com.eventmanager.service;

import com.eventmanager.dto.AllergyReportDTO;
import com.eventmanager.dto.AllergyReportDTO.AllergyGroupDTO;
import com.eventmanager.dto.AllergyReportDTO.AllergyParticipantDTO;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Service for generating allergy reports.
 * Only shows participants WITH allergies, sorted by lastName, firstName.
 */
@Stateless
public class AllergyReportService {

    @PersistenceContext(unitName = "eventPU")
    private EntityManager em;

    /**
     * Generate allergy report data for an event
     */
    public AllergyReportDTO generateReport(Long eventId) {
        // Get event name
        Query eventQuery = em.createNativeQuery("SELECT name FROM events WHERE id = ?");
        eventQuery.setParameter(1, eventId);
        List<?> eventResult = eventQuery.getResultList();
        
        if (eventResult.isEmpty()) {
            throw new IllegalArgumentException("Event not found: " + eventId);
        }
        String eventName = (String) eventResult.get(0);

        // Get all allergens with participants - sorted by lastName, firstName within each group
        String sql = """
            SELECT 
                a.name AS allergen_name,
                p.id AS participant_id,
                p.first_name,
                p.last_name,
                pat.name AS patrol_name,
                a.description AS allergen_description
            FROM participant_allergens pa
            JOIN participants p ON pa.participant_id = p.id
            JOIN allergens a ON pa.allergen_id = a.id
            LEFT JOIN patrols pat ON p.patrol_id = pat.id
            JOIN registrations r ON r.participant_id = p.id AND r.event_id = ?
            WHERE r.status IN ('CONFIRMED', 'PENDING')
            ORDER BY a.name ASC, p.last_name ASC, p.first_name ASC
            """;

        Query query = em.createNativeQuery(sql);
        query.setParameter(1, eventId);
        
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        // Group by allergen
        Map<String, List<AllergyParticipantDTO>> allergenGroups = new LinkedHashMap<>();
        Set<Long> uniqueParticipantIds = new HashSet<>();

        for (Object[] row : results) {
            String allergenName = (String) row[0];
            Long participantId = ((Number) row[1]).longValue();
            String firstName = (String) row[2];
            String lastName = (String) row[3];
            String patrolName = (String) row[4];
            String allergenDescription = (String) row[5];

            // Format: "Efternamn Förnamn"
            String fullName = lastName + " " + firstName;
            
            AllergyParticipantDTO participant = new AllergyParticipantDTO(
                participantId, fullName, patrolName, allergenDescription
            );

            allergenGroups.computeIfAbsent(allergenName, k -> new ArrayList<>()).add(participant);
            uniqueParticipantIds.add(participantId);
        }

        // Convert to AllergyGroupDTOs
        List<AllergyGroupDTO> groups = new ArrayList<>();
        for (Map.Entry<String, List<AllergyParticipantDTO>> entry : allergenGroups.entrySet()) {
            groups.add(new AllergyGroupDTO(
                entry.getKey(),
                entry.getValue().size(),
                entry.getValue()
            ));
        }

        return new AllergyReportDTO(eventId, eventName, groups, uniqueParticipantIds.size());
    }

    /**
     * Generate Excel file for allergy report
     */
    public byte[] generateExcel(Long eventId) throws IOException {
        AllergyReportDTO report = generateReport(eventId);
        
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Allergirapport");

            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle allergyHeaderStyle = createAllergyHeaderStyle(workbook);

            int rowNum = 0;

            // Title
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Allergirapport - " + report.getEventName());
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

            // Generated date
            Row dateRow = sheet.createRow(rowNum++);
            dateRow.createCell(0).setCellValue("Genererad: " + 
                report.getGeneratedAt().toLocalDate().toString() + " " +
                report.getGeneratedAt().toLocalTime().toString().substring(0, 5));

            // Empty row
            rowNum++;

            // Summary
            Row summaryRow = sheet.createRow(rowNum++);
            summaryRow.createCell(0).setCellValue("Totalt antal deltagare med allergier:");
            Cell countCell = summaryRow.createCell(1);
            countCell.setCellValue(report.getTotalParticipantsWithAllergies());

            // Empty row
            rowNum++;

            // Check if there are any allergies
            if (report.getAllergies().isEmpty()) {
                Row noDataRow = sheet.createRow(rowNum++);
                noDataRow.createCell(0).setCellValue("Inga allergier registrerade för detta event.");
            } else {
                // For each allergy group
                for (AllergyGroupDTO group : report.getAllergies()) {
                    // Allergy header
                    Row allergyRow = sheet.createRow(rowNum++);
                    Cell allergyCell = allergyRow.createCell(0);
                    allergyCell.setCellValue(group.getAllergyName() + " " + group.getCount() + " st.");
                    allergyCell.setCellStyle(allergyHeaderStyle);
                    sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 1));

                    // Participants (no column headers - matches the design)
                    for (AllergyParticipantDTO participant : group.getParticipants()) {
                        Row dataRow = sheet.createRow(rowNum++);
                        
                        Cell nameCell = dataRow.createCell(0);
                        nameCell.setCellValue(participant.getFullName());
                        nameCell.setCellStyle(dataStyle);
                        
                        Cell patrolCell = dataRow.createCell(1);
                        patrolCell.setCellValue(participant.getPatrol() != null ? participant.getPatrol() : "");
                        patrolCell.setCellStyle(dataStyle);
                    }

                    // Empty row after each group
                    rowNum++;
                }
            }

            // Auto-size columns
            sheet.setColumnWidth(0, 8000);
            sheet.setColumnWidth(1, 8000);

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Generate CSV file for allergy report
     */
    public String generateCSV(Long eventId) {
        AllergyReportDTO report = generateReport(eventId);
        StringBuilder csv = new StringBuilder();

        // BOM for Excel UTF-8 compatibility
        csv.append("\uFEFF");

        // Header
        csv.append("Allergi;Namn;Kår/Patrull\n");

        // Data
        for (AllergyGroupDTO group : report.getAllergies()) {
            for (AllergyParticipantDTO participant : group.getParticipants()) {
                csv.append(escapeCsv(group.getAllergyName())).append(";");
                csv.append(escapeCsv(participant.getFullName())).append(";");
                csv.append(escapeCsv(participant.getPatrol() != null ? participant.getPatrol() : ""));
                csv.append("\n");
            }
        }

        return csv.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(";") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createAllergyHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        // Dark blue/teal color matching the design
        style.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
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
}
