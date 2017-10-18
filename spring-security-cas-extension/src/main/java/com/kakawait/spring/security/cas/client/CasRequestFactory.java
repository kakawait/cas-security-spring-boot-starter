package com.kakawait.spring.security.cas.client;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * @author Jonathan Coueraud
 */
public class CasRequestFactory {

    private final CasClientProperties casClientProperties;

    public CasRequestFactory(CasClientProperties casClientProperties) {
        this.casClientProperties = casClientProperties;
    }

    HttpRequest createRequest(Ticket proxyTicket, HttpRequest request) {
        URI targetUri = request.getURI();
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
