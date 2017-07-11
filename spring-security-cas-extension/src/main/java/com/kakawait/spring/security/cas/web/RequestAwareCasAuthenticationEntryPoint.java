package com.kakawait.spring.security.cas.web;

import org.jasig.cas.client.util.CommonUtils;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Thibaud Leprêtre
 */
public class RequestAwareCasAuthenticationEntryPoint extends CasAuthenticationEntryPoint {

    private final String loginPath;

    public RequestAwareCasAuthenticationEntryPoint(String loginPath) {
        this.loginPath = loginPath;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.hasLength(getLoginUrl(), "loginUrl must be specified");
        Assert.notNull(getServiceProperties(), "serviceProperties must be specified");
    }

    @Override
    protected String createServiceUrl(HttpServletRequest request, HttpServletResponse response) {
        String serviceUrl = buildUrl(request, loginPath).orElse(loginPath);
        return CommonUtils.constructServiceUrl(null, response, serviceUrl, null,
                getServiceProperties().getServiceParameter(), getServiceProperties().getArtifactParameter(), true);
    }

    private static Optional<String> buildUrl(HttpServletRequest request, String path) {
        Assert.notNull(request, "request is required; it must not be null");
        Assert.isTrue(!StringUtils.isEmpty(path), "path is required, it must not be null or empty");
        try {
            URI uri = new URI(path);
            if (!uri.isAbsolute()) {
                return Optional.of(getBaseUrl(request, true) + path);
            }
        } catch (URISyntaxException e) {
            // Do nothing
        }
        return Optional.empty();
    }

    static String getBaseUrl(HttpServletRequest request, boolean withContextPath) {
        String uri = request.getRequestURI();
        StringBuilder url = new StringBuilder(request.getRequestURL());
        String contextPath = request.getContextPath();
        return  url.substring(0, url.length() - uri.length() + (withContextPath ? contextPath.length() : 0));
    }
}
