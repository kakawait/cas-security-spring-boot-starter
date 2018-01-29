package com.kakawait.spring.security.cas.client;

/**
 * @author Jonathan Coueraud
 */
public interface CurrentTimeMillisAdapter {

    default long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}
