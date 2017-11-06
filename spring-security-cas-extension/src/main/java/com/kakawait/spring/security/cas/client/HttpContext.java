package com.kakawait.spring.security.cas.client;

import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jonathan Coueraud
 */
public class HttpContext {

    private final URI uri;

    private final Principal principal;

    private final Set<CookieWrapper> cookieWrappers = new HashSet<>();

    public HttpContext(Principal principal, URI uri) {
        this.uri = uri;
        this.principal = principal;
    }

    public HttpContext addCookie(CookieWrapper cookieWrapper) {
        cookieWrappers.add(cookieWrapper);
        return this;
    }

    public List<CookieWrapper> getCookies() {
        return new ArrayList<>(cookieWrappers);
    }

    public URI getUri() {
        return uri;
    }

    public Principal getPrincipal() {
        return principal;
    }
}
