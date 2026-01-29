package com.eventmanager.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

// Service for sending emails via Resend API
@ApplicationScoped
public class EmailService {

    private static final String RESEND_API_URL = "https://api.resend.com/emails";
    private static final String FROM_EMAIL = "noreply@baldheads.se";
    private static final String FROM_NAME = "Event Manager";

    private final HttpClient httpClient;

    public EmailService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    // Get API key from environment variable
    private String getApiKey() {
        String apiKey = System.getenv("RESEND_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("RESEND_API_KEY environment variable is not set");
        }
        return apiKey;
    }

    // Send welcome email to new admin
    public void sendWelcomeEmail(String toEmail, String firstName, String password) {
        String subject = "Välkommen till Event Manager";
        String htmlContent = buildWelcomeEmailHtml(firstName, toEmail, password);

        sendEmail(toEmail, subject, htmlContent);
    }

    // Send password reset email
    public void sendPasswordResetEmail(String toEmail, String firstName, String newPassword) {
        String subject = "Ditt lösenord har återställts";
        String htmlContent = buildPasswordResetEmailHtml(firstName, newPassword);

        sendEmail(toEmail, subject, htmlContent);
    }

    // Send email via Resend API
    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            JsonObject requestBody = Json.createObjectBuilder()
                    .add("from", FROM_NAME + " <" + FROM_EMAIL + ">")
                    .add("to", to)
                    .add("subject", subject)
                    .add("html", htmlContent)
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RESEND_API_URL))
                    .header("Authorization", "Bearer " + getApiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200 && response.statusCode() != 201) {
                throw new RuntimeException("Failed to send email. Status: " + response.statusCode() + 
                        ", Body: " + response.body());
            }

            System.out.println("Email sent successfully to: " + to);

        } catch (Exception e) {
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    // Build welcome email HTML
    private String buildWelcomeEmailHtml(String firstName, String email, String password) {
        String name = firstName != null && !firstName.isEmpty() ? firstName : "Användare";

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #416487; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 8px 8px; }
                    .credentials { background: white; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #416487; }
                    .credentials p { margin: 10px 0; }
                    .credentials strong { color: #416487; }
                    .button { display: inline-block; background: #416487; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; margin-top: 20px; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Välkommen till Event Manager</h1>
                    </div>
                    <div class="content">
                        <p>Hej %s,</p>
                        <p>Ett konto har skapats åt dig i Event Manager. Här är dina inloggningsuppgifter:</p>
                        
                        <div class="credentials">
                            <p><strong>E-post:</strong> %s</p>
                            <p><strong>Lösenord:</strong> %s</p>
                        </div>
                        
                        <p><strong>Viktigt:</strong> Du kommer att uppmanas att byta lösenord vid första inloggningen.</p>
                        
                        <p>Klicka på knappen nedan för att logga in:</p>
                        <a href="https://eventmanager.se/login" class="button">Logga in</a>
                        
                        <div class="footer">
                            <p>Detta är ett automatiskt meddelande. Svara inte på detta e-postmeddelande.</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(name, email, password);
    }

    // Build password reset email HTML
    private String buildPasswordResetEmailHtml(String firstName, String newPassword) {
        String name = firstName != null && !firstName.isEmpty() ? firstName : "Användare";

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #416487; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 8px 8px; }
                    .credentials { background: white; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #416487; }
                    .button { display: inline-block; background: #416487; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; margin-top: 20px; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Lösenord återställt</h1>
                    </div>
                    <div class="content">
                        <p>Hej %s,</p>
                        <p>Ditt lösenord har återställts. Här är ditt nya lösenord:</p>
                        
                        <div class="credentials">
                            <p><strong>Nytt lösenord:</strong> %s</p>
                        </div>
                        
                        <p><strong>Viktigt:</strong> Du kommer att uppmanas att byta lösenord vid nästa inloggning.</p>
                        
                        <a href="https://eventmanager.se/login" class="button">Logga in</a>
                        
                        <div class="footer">
                            <p>Om du inte begärt denna återställning, kontakta administratören omedelbart.</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(name, newPassword);
    }
}
