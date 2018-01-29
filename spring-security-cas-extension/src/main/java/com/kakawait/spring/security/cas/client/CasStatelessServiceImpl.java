package com.kakawait.spring.security.cas.client;

import org.springframework.http.HttpRequest;

import java.io.IOException;
import java.security.Principal;

/**
 * @author Jonathan Coueraud
 */
public class CasStatelessServiceImpl implements CasStatelessService {

    private final ProxyTicketRepository proxyTicketRepository;

    private final RequestWithProxyTicketFactory requestWithProxyTicketFactory;

    public CasStatelessServiceImpl(ProxyTicketRepository proxyTicketRepository,
            RequestWithProxyTicketFactory requestWithProxyTicketFactory) {
        this.proxyTicketRepository = proxyTicketRepository;
        this.requestWithProxyTicketFactory = requestWithProxyTicketFactory;
    }

    @Override
    public HttpRequest createRequest(Principal principal, HttpRequest request) throws IOException {
        Ticket proxyTicket = proxyTicketRepository.getProxyTicket(principal, request.getURI());
        return requestWithProxyTicketFactory.createRequest(proxyTicket, request);
    }
}
