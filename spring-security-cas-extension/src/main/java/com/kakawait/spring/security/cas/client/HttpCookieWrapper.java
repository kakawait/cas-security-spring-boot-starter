package com.kakawait.spring.security.cas.client;

import java.net.HttpCookie;

/**
 * @author Jonathan Coueraud
 */
public class HttpCookieWrapper implements CookieWrapper {

    private final HttpCookie httpCookie;

    public HttpCookieWrapper(HttpCookie httpCookie) {
        this.httpCookie = httpCookie;
    }

    @Override
    public String getValue() {
        return httpCookie.getValue();
    }

    @Override
    public String getName() {
        return httpCookie.getName();
    }


    /**
     * Returns the maximum age of the cookie, specified in milliseconds. By default,
     * {@code -1} indicating the cookie will persist until browser shutdown.
     *
     * @return  an integer specifying the maximum age of the cookie in seconds
     */
    @Override
    public long getExpiration() {
        long maxAge = httpCookie.getMaxAge();
        return ((maxAge > 0) ? maxAge * 1000 : maxAge);
    }
}
