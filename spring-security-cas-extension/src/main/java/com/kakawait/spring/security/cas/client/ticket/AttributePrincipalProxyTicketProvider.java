package com.kakawait.spring.security.cas.client.ticket;

import com.kakawait.spring.security.cas.client.validation.AssertionProvider;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;
import org.springframework.util.Assert;

/**
 * A standard implementation of {@link ProxyTicketProvider} that rely on
 * {@link AttributePrincipal#getProxyTicketFor(String)}.
 *
 * @see AssertionProvider
 * @author Jonathan Coueraud
 * @author Thibaud Lepretre
 * @since 0.7.0
 */
public class AttributePrincipalProxyTicketProvider implements ProxyTicketProvider {
    private static final String EXCEPTION_MESSAGE = "Unable to provide a proxy ticket with null %s";

    private final AssertionProvider assertionProvider;

    public AttributePrincipalProxyTicketProvider(AssertionProvider assertionProvider) {
        this.assertionProvider = assertionProvider;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code service} is null or blank
     * @throws IllegalStateException    if {@link Assertion} from {@link AssertionProvider#getAssertion()} is
     *                                  {@code null} or {@link AttributePrincipal} from previous
     *                                  {@link Assertion#getPrincipal()} is {@code null}.
     */
    @Override
    public String getProxyTicket(String service) {
        Assert.hasText(service, "service cannot not be null or blank");
        Assertion assertion = assertionProvider.getAssertion();

        AttributePrincipal principal = assertion.getPrincipal();
        if (principal == null) {
            throw new IllegalStateException(String.format(EXCEPTION_MESSAGE, AttributePrincipal.class.getSimpleName()));
        }

        return principal.getProxyTicketFor(service);
    }
}
