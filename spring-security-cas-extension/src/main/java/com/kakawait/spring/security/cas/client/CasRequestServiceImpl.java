package com.kakawait.spring.security.cas.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.security.Principal;

/**
 * @author Jonathan Coueraud
 */
public class CasRequestServiceImpl implements CasRequestService {

    private final ProxyTicketService proxyTicketService;

    private final CasClientProperties casClientProperties;

    @Autowired
    public CasRequestServiceImpl(ProxyTicketService proxyTicketService, CasClientProperties casClientProperties) {
        this.proxyTicketService = proxyTicketService;
        this.casClientProperties = casClientProperties;
    }

    @Override
    public HttpRequest createRequest(Principal principal, HttpRequest request) throws IOException {
        URI targetUri = request.getURI();
        Ticket proxyTicket = proxyTicketService.getProxyTicket(principal, targetUri);
        URI uri = UriComponentsBuilder.fromUri(targetUri)
                .queryParam(casClientProperties.getProxyTicketQueryKey(), proxyTicket.getValue())
                .build()
                .toUri();

        return new HttpRequestWrapper(request) {
            @Override
            public URI getURI() {
                return uri;
            }
        };
    }
}
