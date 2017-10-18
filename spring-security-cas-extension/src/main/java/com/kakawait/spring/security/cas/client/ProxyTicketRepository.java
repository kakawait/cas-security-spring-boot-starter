package com.kakawait.spring.security.cas.client;

import java.io.IOException;
import java.net.URI;
import java.security.Principal;

/**
 * @author Jonathan Coueraud
 */
public interface ProxyTicketRepository {
    Ticket getProxyTicket(Principal principal, URI targetUri) throws IOException;
}
