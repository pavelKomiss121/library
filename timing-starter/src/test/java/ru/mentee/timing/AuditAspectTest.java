package ru.mentee.timing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(
    properties = "mentee.audit.enabled=true",
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    classes = {AuditAspectTest.TestConfig.class}
)
class AuditAspectTest {

    @Autowired
    private MyAuditedService myAuditedService;

    @MockitoBean
    private AuditService auditService;

    @Test
    @DisplayName("Should вызвать аспект для метода с аннотацией @Auditable")
    void shouldTriggerAspectForAuditableMethod() {
        myAuditedService.doSomething("test data");

        // Проверяем, что сервис аудита был вызван
        verify(auditService, times(1)).log(eq("DO_SOMETHING"), eq("SUCCESS"));
    }

    @Configuration
    @ComponentScan(basePackages = "ru.mentee.timing")
    @EnableAspectJAutoProxy
    static class TestConfig {
    }
}

