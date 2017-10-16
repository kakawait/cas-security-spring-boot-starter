package com.kakawait.spring.security.cas.client;

import org.springframework.http.HttpRequest;

/**
 * @author Jonathan Coueraud
 */
public class CasRequestSpecification {

    public boolean doItNeedProxyTicket(HttpRequest httpRequest) {
        return true;
    }
}
