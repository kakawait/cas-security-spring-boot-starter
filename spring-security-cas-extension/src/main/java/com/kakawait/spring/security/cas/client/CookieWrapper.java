package com.kakawait.spring.security.cas.client;

/**
 * @author Jonathan Coueraud
 */
public interface CookieWrapper {

    String getValue();

    String getName();

    long getExpiration();
}
