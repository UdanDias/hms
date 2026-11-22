package com.hms.web;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;
@Target({ElementType.FIELD,ElementType.PARAMETER,ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=PhoneValidator.class)
public @interface ValidPhone {
 String message() default "Enter a 10-digit Sri Lankan phone number starting with 0, or an international number with + and its country code";
 Class<?>[] groups() default {};
 Class<? extends Payload>[] payload() default {};
}
