package com.example.Smartspend_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final EmailService emailService;

    public NotificationService(SimpMessagingTemplate messagingTemplate, EmailService emailService) {
        this.messagingTemplate = messagingTemplate;
        this.emailService = emailService;
    }

    /**
     * Checks spend against budget limits and sends real-time in-app and email alerts if thresholds are reached.
     */
    @SuppressWarnings("unused")
    public void checkAndSendBudgetAlert(String userEmail, String category, BigDecimal currentSpend, BigDecimal limit) {
        if (limit == null || limit.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        // Calculate usage percentage
        double percentage = (currentSpend.doubleValue() / limit.doubleValue()) * 100;

        if (percentage >= 100) {
            String message = String.format("Alert! You have exceeded your budget for %s. Limit: ₹%.2f, Spent: ₹%.2f",
                    category, limit, currentSpend);

            sendInAppNotification(userEmail, "BUDGET_EXCEEDED", message);
            sendEmailNotification(userEmail, "Budget Exceeded Alert", message);

        } else if (percentage >= 80) {
            String message = String.format("Warning! You have used %.1f%% of your budget for %s. Limit: ₹%.2f, Spent: ₹%.2f",
                    percentage, category, limit, currentSpend);

            sendInAppNotification(userEmail, "BUDGET_WARNING", message);
            sendEmailNotification(userEmail, "Budget Limit Warning", message);
        }
    }

    /**
     * Broadcasts real-time alert message to user's specific WebSocket topic.
     */
    public void sendInAppNotification(String userEmail, String type, String message) {
        Map<String, Object> notificationPayload = new HashMap<>();
        notificationPayload.put("type", type);
        notificationPayload.put("message", message);
        notificationPayload.put("timestamp", System.currentTimeMillis());

        // Sends to WebSocket topic: /topic/notifications/{userEmail}
        messagingTemplate.convertAndSend("/topic/notifications/" + userEmail, notificationPayload);
    }

    /**
     * Sends email notification for budget warnings/exceeded alerts.
     */
    private void sendEmailNotification(String userEmail, String subject, String body) {
        try {
            emailService.sendEmail(userEmail, subject, body);
        } catch (Exception e) {
            logger.error("Failed to send budget alert email to {}: {}", userEmail, e.getMessage());
        }
    }
}