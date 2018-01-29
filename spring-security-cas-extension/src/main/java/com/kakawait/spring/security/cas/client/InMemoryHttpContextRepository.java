package com.kakawait.spring.security.cas.client;

import java.net.URI;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Jonathan Coueraud
 */
public class InMemoryHttpContextRepository implements HttpContextRepository {

    private static final int DEFAULT_FLUSH_INTERVAL = 1000;

    private static final CurrentTimeMillisAdapter CURRENT_TIME_MILLIS_ADAPTER = new CurrentTimeMillisAdapter(){};

    private final ConcurrentHashMap<HttpContextId, Map<String, CookieWrapper>> contextIdToCookies =
            new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Principal, Set<URI>> principalToUris =
            new ConcurrentHashMap<>();

    private final DelayQueue<CookieExpiry> expiryQueue = new DelayQueue<>();

    private final ConcurrentHashMap<String, CookieExpiry> expiryMap = new ConcurrentHashMap<>();

    private AtomicInteger flushCounter = new AtomicInteger(0);

    private final int flushInterval;

    private final CurrentTimeMillisAdapter currentTimeMillisAdapter;

    public InMemoryHttpContextRepository() {
        this(DEFAULT_FLUSH_INTERVAL, CURRENT_TIME_MILLIS_ADAPTER);
    }

    public InMemoryHttpContextRepository(int flushInterval) {
        this(flushInterval, CURRENT_TIME_MILLIS_ADAPTER);
    }

    public InMemoryHttpContextRepository(CurrentTimeMillisAdapter currentTimeMillisAdapter) {
        this(DEFAULT_FLUSH_INTERVAL, currentTimeMillisAdapter);
    }

    public InMemoryHttpContextRepository(int flushInterval,
            CurrentTimeMillisAdapter currentTimeMillisAdapter) {
        this.flushInterval = ((flushInterval > 0) ? flushInterval : DEFAULT_FLUSH_INTERVAL);
        this.currentTimeMillisAdapter = currentTimeMillisAdapter;
    }

    @Override
    public HttpContext findByPrincipalAndUri(Principal principal, URI uri) {

        return Optional.ofNullable(contextIdToCookies.get(new HttpContextId(principal, uri)))
                .map(Map::values)
                .map(cookies -> {
                    HttpContext context = new HttpContext(principal, uri);
                    context.addCookies(cookies);
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
        saveCookies(contextId, context.getCookies());
    }

    @Override
    public void removeByPrincipal(Principal principal) {
        Optional.ofNullable(principalToUris.remove(principal))
                .ifPresent(uris -> uris.stream()
                        .map(uri -> new HttpContextId(principal, uri))
                        .forEach(contextIdToCookies::remove));
    }

    @Override
    public void removeByPrincipalUri(Principal principal, URI uri) {
        removeCookie(Collections.singleton(new HttpContextId(principal, uri)));
    }

    private void flush() {
        CookieExpiry expiry = expiryQueue.poll();

        while (expiry != null) {
            removeCookie(expiry.getContextId(), expiry.getName());
            expiry = expiryQueue.poll();
        }
    }

    private void saveCookies(HttpContextId contextId, Set<CookieWrapper> cookies) {

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
                    contextIdToCookies.put(contextId, new HashMap<>());
                }
            }
        }

        cookies.forEach(cookie -> {

            if (cookie.getExpiration() == 0) return;

            contextIdToCookies.get(contextId).put(cookie.getName(), cookie);

            if (cookie.getExpiration() > 0) {
                CookieExpiry expiry =
                        new CookieExpiry(contextId, cookie.getName(), cookie.getExpiration());
                // Remove existing expiry for this token if present
                expiryQueue.remove(expiryMap.put(cookie.getName(), expiry));
                this.expiryQueue.put(expiry);
            }
        });
    }

    private void removeCookie(Set<HttpContextId> contextIds) {
        contextIds.forEach(contextId -> {
            contextIdToCookies.remove(contextId);
            Optional.ofNullable(principalToUris.get(contextId.getPrincipal()))
                    .ifPresent(uris -> uris.remove(contextId.getUri()));
        });

        // principalToUris.values().removeIf(Set::isEmpty);
        // potentiellement couteux pour quel gain ? pas de clean dans
        // org.springframework.security.oauth2.provider.token.store
    }

    private void removeCookie(HttpContextId contextId, String cookieName) {
        Optional.ofNullable(contextIdToCookies.get(contextId))
                .ifPresent(cookies -> {
                    cookies.remove(cookieName);

                    if (cookies.isEmpty()) {
                        contextIdToCookies.remove(contextId);
                        Optional.ofNullable(principalToUris.get(contextId.getPrincipal()))
                                .ifPresent(uris -> uris.remove(contextId.getUri()));
                    }
                });
    }

    private class HttpContextId {

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

    private class CookieExpiry implements Delayed {

        private final HttpContextId contextId;

        private final long expiry;

        private final String name;

        public CookieExpiry(HttpContextId contextId, String name, long expiry) {
            this.contextId = contextId;
            this.name = name;
            this.expiry = expiry;
        }

        public long getDelay(TimeUnit unit) {
            return expiry - currentTimeMillisAdapter.currentTimeMillis();
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
