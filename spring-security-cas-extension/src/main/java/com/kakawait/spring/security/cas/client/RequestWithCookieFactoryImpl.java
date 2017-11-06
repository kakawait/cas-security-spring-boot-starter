package com.kakawait.spring.security.cas.client;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.support.HttpRequestWrapper;

import java.util.List;

/**
 * @author Jonathan Coueraud
 */
public class RequestWithCookieFactoryImpl implements RequestWithCookieFactory {

    @Override
    public HttpRequest createRequest(HttpRequest request, List<CookieWrapper> cookieWrappers) {
        HttpHeaders headers = request.getHeaders();

        cookieWrappers.stream()
                .map(c -> String.join("=", c.getName(), c.getValue()))
                .forEach(cookieHeader -> headers.add(HttpHeaders.COOKIE, cookieHeader));

        return new HttpRequestWrapper(request) {
            @Override
            public HttpHeaders getHeaders() {
                return headers;
            }
        };
    }
}
