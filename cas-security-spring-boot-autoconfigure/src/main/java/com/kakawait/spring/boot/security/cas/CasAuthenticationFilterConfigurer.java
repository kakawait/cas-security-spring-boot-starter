package com.kakawait.spring.boot.security.cas;

import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.cas.web.authentication.ServiceAuthenticationDetailsSource;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

/**
 * @author Thibaud Leprêtre
 */
@Setter
@Accessors(fluent = true)
public class CasAuthenticationFilterConfigurer {

    @NonNull
    private AuthenticationFailureHandler authenticationFailureHandler;

    @NonNull
    private AuthenticationFailureHandler proxyAuthenticationFailureHandler;

    @NonNull
    private String proxyReceptorUrl;

    @NonNull
    private ProxyGrantingTicketStorage proxyGrantingTicketStorage;

    @NonNull
    private ServiceAuthenticationDetailsSource serviceAuthenticationDetailsSource;

    void configure(CasAuthenticationFilter filter) {
        if (authenticationFailureHandler != null) {
            filter.setAuthenticationFailureHandler(authenticationFailureHandler);
        }
        if (proxyAuthenticationFailureHandler != null) {
            filter.setProxyAuthenticationFailureHandler(proxyAuthenticationFailureHandler);
        }
        if (proxyReceptorUrl != null) {
            filter.setProxyReceptorUrl(proxyReceptorUrl);
        }
        if (proxyGrantingTicketStorage != null) {
            filter.setProxyGrantingTicketStorage(proxyGrantingTicketStorage);
        }
        if (serviceAuthenticationDetailsSource != null) {
            filter.setAuthenticationDetailsSource(serviceAuthenticationDetailsSource);
        }
    }
}
