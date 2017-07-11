package com.kakawait.spring.security.cas.web.authentication;

import org.springframework.security.web.util.UrlUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Thibaud Leprêtre
 */
class DefaultProxyCallbackAndServiceAuthenticationDetails
        implements ProxyCallbackAndServiceAuthenticationDetails {

    private final transient HttpServletRequest context;

    private final String proxyCallbackPath;

    DefaultProxyCallbackAndServiceAuthenticationDetails(HttpServletRequest context, String proxyCallbackPath) {
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
        return UrlUtils.buildFullRequestUrl(context.getScheme(), context.getServerName(),
                context.getServerPort(), context.getRequestURI(), null);
    }
}
