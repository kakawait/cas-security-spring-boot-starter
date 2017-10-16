package com.kakawait.spring.security.cas.client;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * @author Jonathan Coueraud
 */
public class CasInterceptor implements ClientHttpRequestInterceptor {
    final private CasRequestService casRequestService;
    final private CasRequestSpecification casRequestSpecification;
    final private AuthenticatedPrincipal authenticatedPrincipal;

    public CasInterceptor(AuthenticatedPrincipal authenticatedPrincipal,
            CasRequestService casRequestService,
            CasRequestSpecification casRequestSpecification) {
        this.authenticatedPrincipal = authenticatedPrincipal;
        this.casRequestService = casRequestService;
        this.casRequestSpecification = casRequestSpecification;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request,
            byte[] bytes,
            ClientHttpRequestExecution execution) throws IOException {

        if (!casRequestSpecification.doItNeedProxyTicket(request)) {
            return execution.execute(request, bytes);
        }

        return execution.execute(
                casRequestService.createRequest(authenticatedPrincipal.getAuthenticatedPrincipal(), request),
                bytes);
    }
}
