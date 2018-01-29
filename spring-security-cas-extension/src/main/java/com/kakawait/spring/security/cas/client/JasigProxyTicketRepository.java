package com.kakawait.spring.security.cas.client;

import org.jasig.cas.client.authentication.AttributePrincipal;

import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.Optional;

/**
 * @author Jonathan Coueraud
 */
public class JasigProxyTicketRepository implements ProxyTicketRepository {

    @Override
    public Ticket getProxyTicket(Principal principal, URI targetUri) throws IOException {
        AttributePrincipal attributePrincipal;
        try {
            attributePrincipal = (AttributePrincipal) principal;
        } catch (ClassCastException e) {
            throw new IOException(
                    "The provided principal is not a AttributePrincipal. Use another proxyTicketService or provide a AttributePrincipal instance");
        }

        return Optional.ofNullable(attributePrincipal.getProxyTicketFor(targetUri.toString()))
                .map(ProxyTicket::new)
                .orElseThrow(IOException::new);
    }
}
