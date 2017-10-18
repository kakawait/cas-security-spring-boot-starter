package com.kakawait.spring.security.cas.client;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * @author Jonathan Coueraud
 */
public class CasStatelessInterceptor implements ClientHttpRequestInterceptor {
    final private CasStatelessService casStatelessService;
    final private AuthenticatedPrincipal authenticatedPrincipal;

    public CasStatelessInterceptor(AuthenticatedPrincipal authenticatedPrincipal,
            CasStatelessService casStatelessService) {
        this.authenticatedPrincipal = authenticatedPrincipal;
        this.casStatelessService = casStatelessService;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request,
            byte[] bytes,
            ClientHttpRequestExecution execution) throws IOException {

        return execution.execute(
                casStatelessService.createRequest(authenticatedPrincipal.getAuthenticatedPrincipal(), request),
                bytes);
    }
}
