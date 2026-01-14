package ru.mentee.timing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimingService {

    private static final Logger log = LoggerFactory.getLogger(TimingService.class);

    public void logExecutionTime(String methodName, long executionTimeMs) {
        log.info("Method '{}' executed in {} ms", methodName, executionTimeMs);
    }
}

