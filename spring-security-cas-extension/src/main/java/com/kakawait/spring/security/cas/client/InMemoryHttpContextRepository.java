package com.kakawait.spring.security.cas.client;

import java.net.URI;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Jonathan Coueraud
 */
public class InMemoryHttpContextRepository implements HttpContextRepository {

    private static final int DEFAULT_FLUSH_INTERVAL = 1000;

    private final ConcurrentHashMap<String, CookieWrapper> cookies = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Principal, ConcurrentHashMap<URI, Collection<CookieWrapper>>>
            principalToUriToCookies = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<HttpContextId, Collection<CookieWrapper>> contextIdToCookies =
            new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Principal, Collection<URI>> principalToUris =
            new ConcurrentHashMap<>();

    private final DelayQueue<CookieExpiry> expiryQueue = new DelayQueue<>();

    private final ConcurrentHashMap<String, CookieExpiry> expiryMap = new ConcurrentHashMap<>();

    private int flushInterval = DEFAULT_FLUSH_INTERVAL;

    private AtomicInteger flushCounter = new AtomicInteger(0);

    @Override
    public HttpContext findByPrincipalAndUri(Principal principal, URI uri) {

        return Optional.ofNullable(contextIdToCookies.get(new HttpContextId(principal, uri)))
                .map(cookies -> {
                    HttpContext context = new HttpContext(principal, uri);
                    cookies.forEach(context::addCookie);
                    return context;
                })
                .orElse(null);
    }

    @Override
    public void save(HttpContext context) {

        if (this.flushCounter.incrementAndGet() >= this.flushInterval) {
            flush();
            this.flushCounter.set(0);
        }

        HttpContextId contextId = new HttpContextId(context.getPrincipal(), context.getUri());
        context.getCookies().forEach(cookie -> saveCookie(contextId, cookie));
    }

    @Override
    public void removeByPrincipal(Principal principal) {
        Collection<URI> uris = principalToUris.get(principal);
        uris = (uris == null) ? Collections.emptySet() : uris;

        Stream<HttpContextId> contextIdStream = uris.stream()
                .map(uri -> new HttpContextId(principal, uri));

        contextIdStream.forEach(this::removeCookie);
    }

    @Override
    public void removeByPrincipalUri(Principal principal, URI uri) {
        removeCookie(new HttpContextId(principal, uri));
    }

    private void flush() {
        CookieExpiry expiry = expiryQueue.poll();

        while (expiry != null) {
            removeCookie(expiry.getContextId(), expiry.getName());
            expiry = expiryQueue.poll();
        }
    }

    private CookieWrapper saveCookie(HttpContextId contextId, CookieWrapper cookie) {

        cookies.put(cookie.getName(), cookie);

        Principal principal = contextId.getPrincipal();
        if (!principalToUris.containsKey(principal)) {
            synchronized (principalToUris) {
                if (!principalToUris.containsKey(principal)) {
                    principalToUris.put(principal, new HashSet<>());
                }
            }
        }
        principalToUris.get(principal).add(contextId.getUri());

        if (!contextIdToCookies.containsKey(contextId)) {
            synchronized (contextIdToCookies) {
                if (!contextIdToCookies.containsKey(contextId)) {
                    contextIdToCookies.put(contextId, new HashSet<>());
                }
            }
        }
        contextIdToCookies.get(contextId).add(cookie);

        if (cookie.getExpiration() > 0) {
            CookieExpiry expiry =
                    new CookieExpiry(contextId, cookie.getName(), cookie.getExpiration());
            // Remove existing expiry for this token if present
            expiryQueue.remove(expiryMap.put(cookie.getName(), expiry));
            this.expiryQueue.put(expiry);
        }

        return cookie;
    }

    private Collection<CookieWrapper> removeCookie(HttpContextId contextId) {
        return Optional.ofNullable(contextIdToCookies.get(contextId))
                .map(cookies1 -> cookies1.stream()
                        .map(c -> removeCookie(contextId, c.getName()))
                        .collect(Collectors.toSet()))
                .orElseGet(Collections::emptySet);
    }

    private CookieWrapper removeCookie(HttpContextId contextId, String cookieName) {
        CookieWrapper removed = cookies.remove(cookieName);

        Optional.ofNullable(contextIdToCookies.get(contextId))
                .ifPresent(cookies -> {
                    cookies.remove(removed);

                    if (cookies.isEmpty()) {
                        contextIdToCookies.remove(contextId);
                        Optional.ofNullable(principalToUris.get(contextId.getPrincipal()))
                                .ifPresent(uris -> uris.remove(contextId.getUri()));
                    }
                });

        return removed;
    }

    private static class HttpContextId {

        private final Principal principal;

        private final URI uri;

        public HttpContextId(Principal principal, URI uri) {
            this.principal = principal;
            this.uri = uri;
        }

        public Principal getPrincipal() {
            return principal;
        }

        public URI getUri() {
            return uri;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            HttpContextId id = (HttpContextId) o;
            return Objects.equals(principal, id.principal) &&
                    Objects.equals(uri, id.uri);
        }

        @Override
        public int hashCode() {
            return Objects.hash(principal, uri);
        }
    }

    private static class CookieExpiry implements Delayed {

        private final HttpContextId contextId;

        private final long expiry;

        private final String name;

        public CookieExpiry(HttpContextId contextId, String name, long maxAge) {
            this.contextId = contextId;
            this.name = name;
            this.expiry = maxAge;
        }

        public long getDelay(TimeUnit unit) {
            return expiry - System.currentTimeMillis();
        }

        public String getName() {
            return name;
        }

        public HttpContextId getContextId() {
            return contextId;
        }

        @Override
        public int compareTo(Delayed o) {
            if (this == o) {
                return 0;
            }
            long diff = getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS);
            return ((diff == 0) ? 0 : ((diff < 0) ? -1 : 1));
        }
    }
}
