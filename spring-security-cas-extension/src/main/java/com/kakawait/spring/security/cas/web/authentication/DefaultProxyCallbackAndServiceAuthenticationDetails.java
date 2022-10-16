package com.kakawait.spring.security.cas.web.authentication;

import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromContextPath;

/**
 * @author Thibaud Lepretre
 */
public class DefaultProxyCallbackAndServiceAuthenticationDetails
        implements ProxyCallbackAndServiceAuthenticationDetails {

    private static final long serialVersionUID = -88469969834244098L;

    private final transient ServiceProperties serviceProperties;

    private final URI proxyCallbackUri;

    protected transient HttpServletRequest context;

    public DefaultProxyCallbackAndServiceAuthenticationDetails(ServiceProperties serviceProperties, URI proxyCallbackUri) {
        this.serviceProperties = serviceProperties;
        this.proxyCallbackUri = proxyCallbackUri;
    }

    @Override
    public void setContext(HttpServletRequest context) {
        this.context = context;
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
