package com.kakawait.spring.security.cas.web.authentication;

import org.springframework.security.cas.web.authentication.ServiceAuthenticationDetails;

/**
 * @author Thibaud Leprêtre
 */
public interface ProxyCallbackAndServiceAuthenticationDetails extends ServiceAuthenticationDetails {
    String getProxyCallbackUrl();
}
