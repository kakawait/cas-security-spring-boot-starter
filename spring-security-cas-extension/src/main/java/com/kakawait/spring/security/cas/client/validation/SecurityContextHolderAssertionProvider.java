package com.kakawait.spring.security.cas.client.validation;

import org.jasig.cas.client.validation.Assertion;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * A standard implementation that rely on {@link SecurityContextHolder} to retrieve current request
 * {@link CasAuthenticationToken} that contains {@link Assertion} through {@link CasAuthenticationToken#getAssertion()}.
 *
 * @author Jonathan Coueraud
 * @author Thibaud Leprêtre
 * @see SecurityContextHolder
 * @see CasAuthenticationToken
 * @since 0.7.0
 */
public class SecurityContextHolderAssertionProvider implements AssertionProvider {

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if current {@link org.springframework.security.core.Authentication} retrieve from
     *                               {@link SecurityContext#getAuthentication()} is not instance of
     *                               {@link CasAuthenticationToken}.
     */
    @Nonnull
    @Override
    public Assertion getAssertion() {
        // @formatter:off
        return ((CasAuthenticationToken) Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(a -> a instanceof CasAuthenticationToken)
                .orElseThrow(() -> new IllegalStateException(
                        String.format("Unable to provide an %s with null or non %s authentication",
                                Assertion.class.getSimpleName(),
                                CasAuthenticationToken.class.getCanonicalName()))))
                .getAssertion();
        // @formatter:on
    }
}
