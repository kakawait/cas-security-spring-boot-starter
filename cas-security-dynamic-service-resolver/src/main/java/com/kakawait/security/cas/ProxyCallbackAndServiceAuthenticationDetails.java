package com.kakawait.security.cas;

import org.springframework.security.cas.web.authentication.ServiceAuthenticationDetails;

/**
 * @author Thibaud Leprêtre
 */
public interface ProxyCallbackAndServiceAuthenticationDetails extends ServiceAuthenticationDetails {
    String getProxyCallbackUrl();
}
