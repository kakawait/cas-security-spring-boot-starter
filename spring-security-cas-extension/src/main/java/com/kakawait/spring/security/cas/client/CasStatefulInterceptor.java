package com.kakawait.spring.security.cas.client;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.security.Principal;

/**
 * @author Jonathan Coueraud
 */
public class CasStatefulInterceptor implements ClientHttpRequestInterceptor {

    final private CasStatefulService casStatefulService;

    final private AuthenticatedPrincipal authenticatedPrincipal;

    public CasStatefulInterceptor(CasStatefulService casStatefulService,
            AuthenticatedPrincipal authenticatedPrincipal) {
        this.casStatefulService = casStatefulService;
        this.authenticatedPrincipal = authenticatedPrincipal;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws
            IOException {

        Principal principal = authenticatedPrincipal.getAuthenticatedPrincipal();
        request = casStatefulService.createRequest(principal, request);

        ClientHttpResponse clientHttpResponse = execution.execute(request, body);

        casStatefulService.saveCookie(principal, request, clientHttpResponse);

        return clientHttpResponse;
    }
}
