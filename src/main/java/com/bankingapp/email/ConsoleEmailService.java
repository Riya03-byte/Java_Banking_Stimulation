package com.bankingapp.email;

public class ConsoleEmailService implements EmailService {
    @Override
    public void sendEmail(String to, String subject, String body) {
        System.out.println("--- Sending email (console) ---");
        System.out.println("To: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Body:\n" + body);
        System.out.println("--- End email ---");
    }
}
