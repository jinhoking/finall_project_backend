package com.boot.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 🌟 이 이름표가 붙은 메서드는 실행 즉시 관제판에 기록됩니다!
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {
    String action(); // 어떤 작업을 했는지 명시 (예: "자산 등록 완료")
    String type() default "system"; // 로그 타입 (system, success, warning 중 택1)
}