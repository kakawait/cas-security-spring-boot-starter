package com.kakawait.spring.security.cas.client;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.security.Principal;

/**
 * @author Jonathan Coueraud
 */
public interface CasStatefulService {

    HttpRequest createRequest(Principal principal, HttpRequest request) throws IOException;

    void saveCookie(Principal principal, HttpRequest request, ClientHttpResponse clientHttpResponse);
}
