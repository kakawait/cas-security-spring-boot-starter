package com.kakawait.spring.security.cas.web.authentication;

import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.*;

/**
 * @author Thibaud Leprêtre
 */
class DefaultProxyCallbackAndServiceAuthenticationDetails implements ProxyCallbackAndServiceAuthenticationDetails {

    private final transient ServiceProperties serviceProperties;

    private final transient HttpServletRequest context;

    private final URI proxyCallbackUri;

    DefaultProxyCallbackAndServiceAuthenticationDetails(ServiceProperties serviceProperties, HttpServletRequest context,
            URI proxyCallbackUri) {
        this.serviceProperties = serviceProperties;
        this.context = context;
        this.proxyCallbackUri = proxyCallbackUri;
    }

    @Override
    public String getProxyCallbackUrl() {
        if (proxyCallbackUri == null) {
            return null;
        }
        if (proxyCallbackUri.isAbsolute()) {
            return proxyCallbackUri.toASCIIString();
        }
        return fromContextPath(context).path(proxyCallbackUri.toASCIIString()).toUriString();
    }

    @Override
    public String getServiceUrl() {
        String query = removeArtifactParameterFromQueryString(context.getQueryString());
        return UrlUtils.buildFullRequestUrl(context.getScheme(), context.getServerName(),
                context.getServerPort(), context.getRequestURI(), StringUtils.hasText(query) ? query : null);
    }

    private String removeArtifactParameterFromQueryString(String queryString) {
        return UriComponentsBuilder
                .newInstance()
                .query(queryString)
                .replaceQueryParam(serviceProperties.getArtifactParameter(), new Object[0])
                .build()
                .toString()
                .replaceFirst("^\\?", "");
    }
}
