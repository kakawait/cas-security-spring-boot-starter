package com.kakawait.spring.security.cas.client;

import com.kakawait.spring.security.cas.client.ticket.ProxyTicketProvider;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

/**
 * @author Thibaud Leprêtre
 */
public class CasAuthorizationInterceptor implements ClientHttpRequestInterceptor {

    private final ServiceProperties serviceProperties;

    private final ProxyTicketProvider proxyTicketProvider;

    public CasAuthorizationInterceptor(ServiceProperties serviceProperties,
            ProxyTicketProvider proxyTicketProvider) {
        this.serviceProperties = serviceProperties;
        this.proxyTicketProvider = proxyTicketProvider;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        String proxyTicket = proxyTicketProvider.getProxyTicket(request.getURI().toASCIIString());
        URI uri = UriComponentsBuilder
                .fromUri(request.getURI())
                .queryParam(serviceProperties.getArtifactParameter(), proxyTicket)
                .build().toUri();
        return execution.execute(new HttpRequestWrapper(request) {
            @Override
            public URI getURI() {
                return uri;
            }
        }, body);
    }
}
