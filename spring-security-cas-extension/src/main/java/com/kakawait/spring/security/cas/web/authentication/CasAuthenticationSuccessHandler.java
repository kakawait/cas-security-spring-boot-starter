package com.kakawait.spring.security.cas.web.authentication;

import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Thibaud Leprêtre
 */
public class CasAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final String ticketParameterName;

    public CasAuthenticationSuccessHandler(String ticketParameterName) {
        Assert.notNull(ticketParameterName, "tickerParameterName must not be null!");
        this.ticketParameterName = ticketParameterName;
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
        String url = super.determineTargetUrl(request, response);
        String ticket = request.getParameter(ticketParameterName);
        if (ticket != null) {
            url = UriComponentsBuilder
                    .fromUriString(request.getRequestURL().toString())
                    .replaceQueryParam(ticketParameterName, new Object[0])
                    .build()
                    .toUriString();
        }
        return url;
    }
}
