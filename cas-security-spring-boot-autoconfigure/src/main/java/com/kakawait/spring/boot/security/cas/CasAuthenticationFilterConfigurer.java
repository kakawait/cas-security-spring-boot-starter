package com.kakawait.spring.boot.security.cas;

import lombok.Setter;
import lombok.experimental.Accessors;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.cas.web.authentication.ServiceAuthenticationDetailsSource;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * @author Thibaud Leprêtre
 */
@Setter
@Accessors(fluent = true)
public class CasAuthenticationFilterConfigurer {

    private RequestMatcher requiresAuthenticationRequestMatcher;

    private AuthenticationFailureHandler authenticationFailureHandler;

    private AuthenticationFailureHandler proxyAuthenticationFailureHandler;

    private AuthenticationSuccessHandler authenticationSuccessHandler;

    private ProxyGrantingTicketStorage proxyGrantingTicketStorage;

    private ServiceAuthenticationDetailsSource serviceAuthenticationDetailsSource;

    private String proxyReceptorUrl;

    void configure(CasAuthenticationFilter filter) {
        if (requiresAuthenticationRequestMatcher != null) {
            filter.setRequiresAuthenticationRequestMatcher(requiresAuthenticationRequestMatcher);
        }
        if (authenticationFailureHandler != null) {
            filter.setAuthenticationFailureHandler(authenticationFailureHandler);
        }
        if (proxyAuthenticationFailureHandler != null) {
            filter.setProxyAuthenticationFailureHandler(proxyAuthenticationFailureHandler);
        }
        if (authenticationSuccessHandler != null) {
            filter.setAuthenticationSuccessHandler(authenticationSuccessHandler);
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
