package com.kakawait.spring.boot.security.cas;

import lombok.NonNull;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Thibaud Leprêtre
 */
class CasAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final String ticketParameterName;

    CasAuthenticationSuccessHandler(@NonNull String ticketParameterName) {
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
