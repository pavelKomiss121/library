package ru.mentee.timing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jAuditService implements AuditService {

    private static final Logger log = LoggerFactory.getLogger(Slf4jAuditService.class);

    @Override
    public void log(String action, String status) {
        log.info("Audit: action={}, status={}", action, status);
    }

    @Override
    public void log(String action, String status, String errorMessage) {
        log.info("Audit: action={}, status={}, error={}", action, status, errorMessage);
    }
}

