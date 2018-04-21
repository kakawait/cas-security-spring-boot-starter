package com.kakawait.spring.security.cas.client;

import com.kakawait.spring.security.cas.client.ticket.ProxyTicketProvider;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

/**
 * Implementation of {@link ClientHttpRequestInterceptor} to apply Proxy ticket query parameter.
 *
 * @see ProxyTicketProvider
 * @author Jonathan Coueraud
 * @author Thibaud Leprêtre
 * @since 0.7.0
 */
public class CasAuthorizationInterceptor implements ClientHttpRequestInterceptor {

    private final ServiceProperties serviceProperties;

    private final ProxyTicketProvider proxyTicketProvider;

    public CasAuthorizationInterceptor(ServiceProperties serviceProperties,
            ProxyTicketProvider proxyTicketProvider) {
        this.serviceProperties = serviceProperties;
        this.proxyTicketProvider = proxyTicketProvider;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if proxy ticket retrieves from {@link ProxyTicketProvider#getProxyTicket(String)}
     *                               is null or blank
     */
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        String service = request.getURI().toASCIIString();
        String proxyTicket = proxyTicketProvider.getProxyTicket(service);
        if (!StringUtils.hasText(proxyTicket)) {
            throw new IllegalStateException(
                    String.format("Proxy ticket provider returned a null proxy ticket for service %s.", service));
        }
        URI uri = UriComponentsBuilder
                .fromUri(request.getURI())
                .replaceQueryParam(serviceProperties.getArtifactParameter(), proxyTicket)
                .build().toUri();
        return execution.execute(new HttpRequestWrapper(request) {
            @Override
            public URI getURI() {
                return uri;
            }
        }, body);
    }
}
