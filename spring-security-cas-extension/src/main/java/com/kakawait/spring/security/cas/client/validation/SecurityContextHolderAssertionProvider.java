package com.kakawait.spring.security.cas.client.validation;

import org.jasig.cas.client.validation.Assertion;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * @author Jonathan Coueraud
 * @author Thibaud Leprêtre
 */
public class SecurityContextHolderAssertionProvider implements AssertionProvider {
    @Override
    public Assertion getAssertion() {
        return ((CasAuthenticationToken) Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(a -> a instanceof CasAuthenticationToken)
                .orElseThrow(() -> new IllegalStateException(
                        String.format("Unable to provide an %s with null or non %s authentication",
                                Assertion.class.getSimpleName(),
                                CasAuthenticationToken.class.getCanonicalName()))))
                .getAssertion();
    }
}
