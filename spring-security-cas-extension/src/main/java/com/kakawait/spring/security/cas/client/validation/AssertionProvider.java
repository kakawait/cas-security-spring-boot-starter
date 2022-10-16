package com.kakawait.spring.security.cas.client.validation;

import org.jasig.cas.client.validation.Assertion;

import javax.annotation.Nonnull;

/**
 * Assertion provider is simple interface that provides a way to get the current (user bounded) {@link Assertion}.
 *
 * @author Jonathan Coueraud
 * @author Thibaud Lepretre
 * @since 0.7.0
 */
public interface AssertionProvider {

    /**
     * Retrieve current request {@link Assertion}.
     * @return the current request {@link Assertion}.
     */
    @Nonnull
    Assertion getAssertion();
}
