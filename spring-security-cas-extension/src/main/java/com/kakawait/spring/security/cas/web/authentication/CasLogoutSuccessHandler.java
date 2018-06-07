package com.kakawait.spring.security.cas.web.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Thibaud Leprêtre
 */
public class CasLogoutSuccessHandler extends AbstractAuthenticationTargetUrlRequestHandler
        implements LogoutSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(CasLogoutSuccessHandler.class);

    protected final URI casLogout;

    protected final ServiceProperties serviceProperties;

    public CasLogoutSuccessHandler(URI casLogout, ServiceProperties serviceProperties) {
        this.casLogout = casLogout;
        this.serviceProperties = serviceProperties;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse, Authentication authentication)
            throws IOException, ServletException {
        super.handle(httpServletRequest, httpServletResponse, authentication);
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(casLogout);
        addLogoutServiceParameter(builder, serviceProperties.getService());
        return builder.build().toUriString();
    }

    protected void addLogoutServiceParameter(UriComponentsBuilder builder, String service) {
        if (service != null) {
            try {
                builder.replaceQueryParam(serviceProperties.getServiceParameter(), encode(service, UTF_8.toString()));
            } catch (UnsupportedEncodingException e) {
                logger.error("Unable to encode service url {}", service, e);
            }
        }
    }
}
