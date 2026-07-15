package com.restomgmt.site.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {
    
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) return false;

        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasNumber = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars()
            .anyMatch(c -> !Character.isLetterOrDigit(c));

        if (!hasUppercase || !hasNumber || !hasSpecial) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Password must contain at least one uppercase letter, " +
                "one number, and one special character"
            ).addConstraintViolation();
            return false;
        }

        return true;
    }

}
