package com.grown.smartoffice.support;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * USER 역할의 모의 인증 컨텍스트를 주입한다.
 * 기본 username은 user@grown.com.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@WithSecurityContext(factory = WithMockEmployeeUserSecurityContextFactory.class)
public @interface WithMockEmployeeUser {

    String email() default "user@grown.com";
}
