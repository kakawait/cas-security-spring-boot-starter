package com.kakawait.spring.security.cas.client.ticket;

import com.kakawait.spring.security.cas.client.validation.AssertionProvider;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;

/**
 * @author Jonathan Coueraud
 * @author Thibaud Leprêtre
 */
public class AttributePrincipalProxyTicketProvider implements ProxyTicketProvider {
    private static final String EXCEPTION_MESSAGE = "Unable to provide a proxy ticket with null %s";

    private final AssertionProvider assertionProvider;

    public AttributePrincipalProxyTicketProvider(AssertionProvider assertionProvider) {
        this.assertionProvider = assertionProvider;
    }

    @Override
    public String getProxyTicket(String service) {
        Assertion assertion = assertionProvider.getAssertion();
        if (assertion == null) {
            throw new IllegalStateException(
                    String.format(EXCEPTION_MESSAGE, Assertion.class.getSimpleName()));
        }

        AttributePrincipal principal = assertion.getPrincipal();
        if (principal == null) {
            throw new IllegalStateException(String.format(EXCEPTION_MESSAGE, Assertion.class.getSimpleName()));
        }

        return principal.getProxyTicketFor(service);
    }
}
