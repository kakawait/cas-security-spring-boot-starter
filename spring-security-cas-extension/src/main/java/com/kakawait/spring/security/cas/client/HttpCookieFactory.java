package com.kakawait.spring.security.cas.client;

import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpResponse;

import java.net.HttpCookie;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Jonathan Coueraud
 */
public class HttpCookieFactory implements CookieFactory {

    @Override
    public CookieWrapper createCookie(String name, String value) {
        HttpCookie httpCookie = new HttpCookie(name, value);
        return new HttpCookieWrapper(httpCookie);
    }

    @Override
    public Set<CookieWrapper> parseCookie(ClientHttpResponse clientHttpResponse) {
        return clientHttpResponse.getHeaders()
                .get(HttpHeaders.COOKIE)
                .stream()
                .map(HttpCookie::parse)
                .flatMap(Collection::stream)
                .map(HttpCookieWrapper::new)
                .collect(Collectors.toSet());
    }
}
