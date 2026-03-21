package com.ufind.ufindapp.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CollegeEmailValidator implements ConstraintValidator<CollegeEmail, String> {

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String normalizedEmail = email.trim().toLowerCase();
        return normalizedEmail.endsWith("@icomp.ufam.edu.br") || 
               normalizedEmail.endsWith("@ufam.edu.br");
    }
}
