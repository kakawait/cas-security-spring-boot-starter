package com.kakawait.spring.security.cas.client;

import org.springframework.http.client.ClientHttpResponse;

import java.util.Set;

/**
 * @author Jonathan Coueraud
 */
public interface CookieFactory {

    CookieWrapper createCookie(String name, String value);

    Set<CookieWrapper> parseCookie(ClientHttpResponse clientHttpResponse);
}
