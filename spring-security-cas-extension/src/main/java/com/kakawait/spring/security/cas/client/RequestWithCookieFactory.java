package com.kakawait.spring.security.cas.client;

import org.springframework.http.HttpRequest;

import java.util.Set;

/**
 * @author Jonathan Coueraud
 */
public interface RequestWithCookieFactory {
    HttpRequest createRequest(HttpRequest request, Set<CookieWrapper> cookieWrappers);
}
