package com.kakawait.spring.security.cas.client;

import org.springframework.http.HttpRequest;

import java.io.IOException;
import java.security.Principal;

/**
 * @author Jonathan Coueraud
 */
public interface CasStatelessService {
    HttpRequest createRequest(Principal principal, HttpRequest request) throws IOException;
}