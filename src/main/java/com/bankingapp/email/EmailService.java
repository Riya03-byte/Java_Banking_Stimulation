package com.bankingapp.email;

public interface EmailService {
    void sendEmail(String to, String subject, String body) throws Exception;
}
