package ru.mentee.timing;

public interface AuditService {
    void log(String action, String status);
    void log(String action, String status, String errorMessage);
}

