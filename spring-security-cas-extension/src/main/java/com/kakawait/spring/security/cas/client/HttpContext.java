package com.kakawait.spring.security.cas.client;

import java.net.URI;
import java.security.Principal;
import java.util.*;

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

    public HttpContext addCookies(Collection<CookieWrapper> collection) {
        cookieWrappers.addAll(collection);
        return this;
    }

    public Set<CookieWrapper> getCookies() {
        return new HashSet<>(cookieWrappers);
    }

    public URI getUri() {
        return uri;
    }

    public Principal getPrincipal() {
        return principal;
    }
}
