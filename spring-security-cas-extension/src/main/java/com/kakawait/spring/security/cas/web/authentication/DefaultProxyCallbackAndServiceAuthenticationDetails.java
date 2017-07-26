package com.kakawait.spring.security.cas.web.authentication;

import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Thibaud Leprêtre
 */
class DefaultProxyCallbackAndServiceAuthenticationDetails
        implements ProxyCallbackAndServiceAuthenticationDetails {

    private final ServiceProperties serviceProperties;

    private final transient HttpServletRequest context;

    private final String proxyCallbackPath;

    DefaultProxyCallbackAndServiceAuthenticationDetails(ServiceProperties serviceProperties, HttpServletRequest context,
            String proxyCallbackPath) {
        this.serviceProperties = serviceProperties;
        this.context = context;
        this.proxyCallbackPath = proxyCallbackPath;
    }

    @Override
    public String getProxyCallbackUrl() {
        if (proxyCallbackPath == null) {
            return null;
        }
        String path = context.getContextPath() + proxyCallbackPath;
        return UrlUtils.buildFullRequestUrl(context.getScheme(), context.getServerName(),
                context.getServerPort(), path, null);
    }

    @Override
    public String getServiceUrl() {
        String query = UriComponentsBuilder
                .newInstance()
                .query(context.getQueryString())
                .replaceQueryParam(serviceProperties.getArtifactParameter(), new Object[0])
                .build()
                .toString()
                .replaceFirst("^\\?", "");
        return UrlUtils.buildFullRequestUrl(context.getScheme(), context.getServerName(),
                context.getServerPort(), context.getRequestURI(), StringUtils.hasText(query) ? query : null);
    }
}
