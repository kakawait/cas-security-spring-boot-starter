package com.kakawait.security.cas;

import org.springframework.security.web.util.UrlUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Thibaud Leprêtre
 */
public class DefaultProxyCallbackAndServiceAuthenticationDetails
        implements ProxyCallbackAndServiceAuthenticationDetails {

    private final HttpServletRequest context;

    private final String proxyCallbackPath;

    public DefaultProxyCallbackAndServiceAuthenticationDetails(HttpServletRequest context, String proxyCallbackPath) {
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
