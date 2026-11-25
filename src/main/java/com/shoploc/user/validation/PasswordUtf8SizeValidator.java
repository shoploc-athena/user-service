package com.shoploc.user.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.nio.charset.StandardCharsets;

public class PasswordUtf8SizeValidator implements ConstraintValidator<PasswordUtf8Size, String> {
	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}
		return value.getBytes(StandardCharsets.UTF_8).length <= 72;
	}
}
