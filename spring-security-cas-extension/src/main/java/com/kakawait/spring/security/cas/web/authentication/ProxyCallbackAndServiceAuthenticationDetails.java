package com.kakawait.spring.security.cas.web.authentication;

import org.springframework.security.cas.web.authentication.ServiceAuthenticationDetails;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Thibaud Lepretre
 */
public interface ProxyCallbackAndServiceAuthenticationDetails extends ServiceAuthenticationDetails {
    String getProxyCallbackUrl();

    void setContext(HttpServletRequest context);
}
