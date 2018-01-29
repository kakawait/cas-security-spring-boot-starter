package com.kakawait.spring.security.cas.client;

/**
 * @author Jonathan Coueraud
 */
public interface CookieWrapper {

    String getValue();

    String getName();

    /**
     * Returns the time that this cookie expires, in the same format as {@link
     * System#currentTimeMillis()}.
     * This is -1 if the cookie is not persistent, in which case it will expire at the end of the current session.
     **/
    long getExpiration();
}
