package com.boot.security.aop;

import com.boot.security.annotation.AuditLog;
import com.boot.security.service.SecurityAuditService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class SecurityAuditAspect {

    private final SecurityAuditService securityAuditService;

    // 기존 로직: 성공 시 기록 (DML 등)
    @AfterReturning(pointcut = "@annotation(auditAnnotation)")
    public void logAuditActivity(JoinPoint joinPoint, AuditLog auditAnnotation) {
        String action = auditAnnotation.action();
        String type = auditAnnotation.type();
        String logMessage = "[AOP 자동감지] " + action;
        securityAuditService.addLog(logMessage, type);
    }

    // 🌟 [추가] 비정상 접근(예외 발생) 감지 로직
    // Service 계층에서 에러가 던져지면 실행됩니다.
    @AfterThrowing(pointcut = "execution(* com.boot.security.service.*.*(..))", throwing = "ex")
    public void logSecurityViolation(JoinPoint joinPoint, Exception ex) {
        String type = "warning"; // 프론트의 type-warning 클래스와 매핑
        String methodName = joinPoint.getSignature().getName();
        String errorMsg = ex.getMessage();

        // 특정 예외들에 대해서만 보안 위협으로 간주
        if (ex instanceof IllegalArgumentException || ex instanceof RuntimeException) {
            String logMessage = "[보안 위협] " + methodName + " 실패 : " + errorMsg;

            // SecurityAuditService에 로그를 남기면 자동으로 abnormalCount가 올라가도록 설계되어야 함
            securityAuditService.addLog(logMessage, type);
        }
    }
}