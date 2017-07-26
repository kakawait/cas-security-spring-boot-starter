package com.kakawait.spring.security.cas.web.authentication;

import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.web.authentication.ServiceAuthenticationDetails;
import org.springframework.security.cas.web.authentication.ServiceAuthenticationDetailsSource;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Thibaud Leprêtre
 */
public class ProxyCallbackAndServiceAuthenticationDetailsSource extends ServiceAuthenticationDetailsSource {
    private final ServiceProperties serviceProperties;

    private final String proxyCallbackPath;

    public ProxyCallbackAndServiceAuthenticationDetailsSource(ServiceProperties serviceProperties,
            String proxyCallbackPath) {
        super(serviceProperties);
        this.serviceProperties = serviceProperties;
        this.proxyCallbackPath = proxyCallbackPath;
    }

    @Override
    public ServiceAuthenticationDetails buildDetails(HttpServletRequest context) {
        return new DefaultProxyCallbackAndServiceAuthenticationDetails(serviceProperties, context, proxyCallbackPath);
    }
}
