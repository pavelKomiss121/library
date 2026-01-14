package ru.mentee.timing;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class AuditAspect {

    private final AuditService auditService;

    public AuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        String action = auditable.action();
        try {
            Object result = joinPoint.proceed();
            auditService.log(action, "SUCCESS");
            return result;
        } catch (Exception e) {
            auditService.log(action, "FAILURE", e.getMessage());
            throw e;
        }
    }
}

