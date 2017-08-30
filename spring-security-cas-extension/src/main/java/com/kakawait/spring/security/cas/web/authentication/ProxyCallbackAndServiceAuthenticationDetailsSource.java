package com.kakawait.spring.security.cas.web.authentication;

import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.web.authentication.ServiceAuthenticationDetails;
import org.springframework.security.cas.web.authentication.ServiceAuthenticationDetailsSource;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Thibaud Leprêtre
 */
public class ProxyCallbackAndServiceAuthenticationDetailsSource extends ServiceAuthenticationDetailsSource {
    private final ServiceProperties serviceProperties;

    private final URI proxyCallbackUri;

    public ProxyCallbackAndServiceAuthenticationDetailsSource(ServiceProperties serviceProperties,
            URI proxyCallbackUri) {
        super(serviceProperties);
        this.serviceProperties = serviceProperties;
        this.proxyCallbackUri = proxyCallbackUri;
    }

    @Override
    public ServiceAuthenticationDetails buildDetails(HttpServletRequest context) {
        return new DefaultProxyCallbackAndServiceAuthenticationDetails(serviceProperties, context, proxyCallbackUri);
    }
}
