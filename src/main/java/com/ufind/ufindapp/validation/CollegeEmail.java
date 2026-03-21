package com.ufind.ufindapp.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = CollegeEmailValidator.class)
@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
public @interface CollegeEmail {
    String message() default "Email must end with @icomp.ufam.edu.br or @ufam.edu.br";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
