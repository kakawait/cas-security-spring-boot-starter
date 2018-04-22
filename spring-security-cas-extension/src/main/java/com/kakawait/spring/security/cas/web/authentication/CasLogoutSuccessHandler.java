package com.kakawait.spring.security.cas.web.authentication;

import com.kakawait.spring.security.cas.LaxServiceProperties;
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromContextPath;

/**
 * @author Thibaud Leprêtre
 */
public class CasLogoutSuccessHandler extends AbstractAuthenticationTargetUrlRequestHandler
        implements LogoutSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(CasLogoutSuccessHandler.class);

    private final URI casLogout;

    private final ServiceProperties serviceProperties;

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

        boolean dynamicServiceResolution = false;
        if (serviceProperties instanceof LaxServiceProperties) {
            dynamicServiceResolution = ((LaxServiceProperties) serviceProperties).isDynamicServiceResolution();
        }

        String service = dynamicServiceResolution ? fromContextPath(request).build().toUriString()
                                                  : serviceProperties.getService();

        if (service != null) {
            try {
                builder.queryParam(serviceProperties.getServiceParameter(), encode(service, UTF_8.toString()));
            } catch (UnsupportedEncodingException e) {
                logger.warn("Unable to encode service url", e);
            }
        }
        return builder.build().toUriString();
    }
}
