package com.kakawait.spring.security.cas.client.validation;

/**
 * @author Thibaud Leprêtre
 */
public interface ProxyCallbackUrlAwareTicketValidator {
    void setProxyCallbackUrl(String proxyCallbackUrl);
}
