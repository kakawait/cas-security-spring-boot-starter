package com.kakawait.spring.security.cas.web.authentication;

import com.kakawait.spring.security.cas.LaxServiceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.cas.ServiceProperties;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;

import static java.net.URLEncoder.*;
import static java.nio.charset.StandardCharsets.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Thibaud Leprêtre
 */
public class CasLogoutSuccessHandlerTest {

    private static final URI casLogout = URI.create("http://cas.server/cas/logout");

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    @BeforeEach
    public void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    public void onLogoutSuccess_WithService_ServiceAsQueryParameterValue()
            throws IOException, ServletException {
        ServiceProperties serviceProperties = new ServiceProperties();
        serviceProperties.setService("http://localhost/john/wick?foo=a&bar=b");
        CasLogoutSuccessHandler logoutSuccessHandler = new CasLogoutSuccessHandler(casLogout, serviceProperties);
        logoutSuccessHandler.onLogoutSuccess(request, response, null);

        String service = encode(serviceProperties.getService(), UTF_8.toString());
        assertThat(response.getRedirectedUrl())
                .isEqualTo(casLogout.toASCIIString() + "?service=" + service);
    }

}
