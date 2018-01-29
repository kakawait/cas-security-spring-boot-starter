package com.kakawait.spring.security.cas.client;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.*;

/**
 * @author Jonathan Coueraud
 */

/**
 * Annotation to mark a RestTemplate bean to be configured to use a CasClient
 *
 * @author Jonathan Coueraud
 */

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Qualifier
public @interface Cas {
}
