package com.kakawait.spring.security.cas.web.authentication;

import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.web.authentication.ServiceAuthenticationDetails;
import org.springframework.security.cas.web.authentication.ServiceAuthenticationDetailsSource;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Thibaud Lepretre
 */
public class ProxyCallbackAndServiceAuthenticationDetailsSource extends ServiceAuthenticationDetailsSource {

    private final ProxyCallbackAndServiceAuthenticationDetails serviceAuthenticationDetails;

    public ProxyCallbackAndServiceAuthenticationDetailsSource(ServiceProperties serviceProperties,
            ProxyCallbackAndServiceAuthenticationDetails serviceAuthenticationDetails) {
        super(serviceProperties);
        this.serviceAuthenticationDetails = serviceAuthenticationDetails;
    }

    public ProxyCallbackAndServiceAuthenticationDetailsSource(ServiceProperties serviceProperties,
            URI proxyCallbackUri) {
        super(serviceProperties);
        serviceAuthenticationDetails =
                new DefaultProxyCallbackAndServiceAuthenticationDetails(serviceProperties, proxyCallbackUri);
    }

    @Override
    public ServiceAuthenticationDetails buildDetails(HttpServletRequest context) {
        serviceAuthenticationDetails.setContext(context);
        return serviceAuthenticationDetails;
    }
}
