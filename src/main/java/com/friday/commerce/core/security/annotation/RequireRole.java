package com.friday.commerce.core.security.annotation;


import com.friday.commerce.core.security.model.UserRole;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RequireRole {
    UserRole[] value() default {};
    boolean allowAnonymous() default false;
}
