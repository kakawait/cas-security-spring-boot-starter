package com.kakawait.spring.security.cas.client.validation;

/**
 * @author Thibaud Lepretre
 */
public interface ProxyCallbackUrlAwareTicketValidator {
    void setProxyCallbackUrl(String proxyCallbackUrl);
}
