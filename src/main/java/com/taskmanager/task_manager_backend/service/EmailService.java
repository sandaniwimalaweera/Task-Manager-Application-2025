package com.taskmanager.task_manager_backend.service;

import com.taskmanager.task_manager_backend.model.Task;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.name}")
    private String fromName;

    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }

    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("HTML email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to: {}", to, e);
        } catch (Exception e) {
            log.error("Unexpected error sending email to: {}", to, e);
        }
    }

    public void sendTaskDueReminderEmail(String to, Task task) {
        String subject = "Task Due Tomorrow: " + task.getTitle();
        String htmlContent = buildTaskReminderEmailHtml(task);
        sendHtmlEmail(to, subject, htmlContent);
    }

    public void sendTaskOverdueEmail(String to, Task task) {
        String subject = "Task Overdue: " + task.getTitle();
        String htmlContent = buildTaskOverdueEmailHtml(task);
        sendHtmlEmail(to, subject, htmlContent);
    }

    private String buildTaskReminderEmailHtml(Task task) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");
        String dueDate = task.getDueDate().format(formatter);

        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }" +
                ".content { background-color: #f9f9f9; padding: 20px; border-radius: 5px; margin-top: 20px; }" +
                ".task-details { background-color: white; padding: 15px; border-left: 4px solid #4CAF50; margin: 15px 0; }" +
                ".priority { display: inline-block; padding: 5px 10px; border-radius: 3px; font-weight: bold; }" +
                ".priority-high { background-color: #ff4444; color: white; }" +
                ".priority-medium { background-color: #ffbb33; color: white; }" +
                ".priority-low { background-color: #00C851; color: white; }" +
                ".footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>Task Due Tomorrow</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Hi there,</p>" +
                "<p>This is a friendly reminder that the following task is due tomorrow:</p>" +
                "<div class='task-details'>" +
                "<h2>" + task.getTitle() + "</h2>" +
                "<p><strong>Priority:</strong> <span class='priority priority-" + task.getPriority().toString().toLowerCase() + "'>" + task.getPriority() + "</span></p>" +
                "<p><strong>Due Date:</strong> " + dueDate + "</p>" +
                (task.getDescription() != null ? "<p><strong>Description:</strong> " + task.getDescription() + "</p>" : "") +
                "<p><strong>Status:</strong> " + task.getStatus() + "</p>" +
                "</div>" +
                "<p>Don't forget to complete this task on time!</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>This is an automated email from Task Manager Application</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private String buildTaskOverdueEmailHtml(Task task) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");
        String dueDate = task.getDueDate().format(formatter);

        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background-color: #ff4444; color: white; padding: 20px; text-align: center; }" +
                ".content { background-color: #f9f9f9; padding: 20px; border-radius: 5px; margin-top: 20px; }" +
                ".task-details { background-color: white; padding: 15px; border-left: 4px solid #ff4444; margin: 15px 0; }" +
                ".footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>Task Overdue!</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Hi there,</p>" +
                "<p>The following task is now overdue:</p>" +
                "<div class='task-details'>" +
                "<h2>" + task.getTitle() + "</h2>" +
                "<p><strong>Was Due:</strong> " + dueDate + "</p>" +
                (task.getDescription() != null ? "<p><strong>Description:</strong> " + task.getDescription() + "</p>" : "") +
                "<p><strong>Status:</strong> " + task.getStatus() + "</p>" +
                "</div>" +
                "<p>Please complete this task as soon as possible!</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>This is an automated email from Task Manager Application</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}