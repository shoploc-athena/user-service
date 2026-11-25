package com.shoploc.user.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordUtf8SizeValidator.class)
public @interface PasswordUtf8Size {
	String message() default "Password UTF-8 encoding must not exceed 72 bytes";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
