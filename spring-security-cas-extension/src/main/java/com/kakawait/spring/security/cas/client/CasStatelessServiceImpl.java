package com.kakawait.spring.security.cas.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;

import java.io.IOException;
import java.security.Principal;

/**
 * @author Jonathan Coueraud
 */
public class CasStatelessServiceImpl implements CasStatelessService {

    private final ProxyTicketRepository proxyTicketRepository;

    private final CasRequestFactory casRequestFactory;


    @Autowired
    public CasStatelessServiceImpl(ProxyTicketRepository proxyTicketRepository,
            CasRequestFactory casRequestFactory) {
        this.proxyTicketRepository = proxyTicketRepository;
        this.casRequestFactory = casRequestFactory;
    }

    @Override
    public HttpRequest createRequest(Principal principal, HttpRequest request) throws IOException {
        Ticket proxyTicket = proxyTicketRepository.getProxyTicket(principal, request.getURI());
        return casRequestFactory.createRequest(proxyTicket, request);
    }
}
