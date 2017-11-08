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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HttpContext context = (HttpContext) o;
        return Objects.equals(uri, context.uri) &&
                Objects.equals(principal, context.principal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, principal);
    }
}
