package com.kakawait.spring.security.cas.web.authentication;

import com.kakawait.spring.security.cas.LaxServiceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.net.URI;

import javax.servlet.ServletException;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Thibaud Leprêtre
 */
public class RequestAwareCasLogoutSuccessHandlerTest {

    private static final URI casLogout = URI.create("http://cas.server/cas/logout");

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    @BeforeEach
    public void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    public void onLogoutSuccess_WithService_UseHttpServletRequestAsService()
            throws IOException, ServletException {
        LaxServiceProperties serviceProperties = new LaxServiceProperties();
        CasLogoutSuccessHandler logoutSuccessHandler = new RequestAwareCasLogoutSuccessHandler(casLogout,
                serviceProperties);
        logoutSuccessHandler.onLogoutSuccess(request, response, null);

        String service = encode(request.getRequestURL().toString(), UTF_8.toString());
        assertThat(response.getRedirectedUrl())
                .isEqualTo(casLogout.toASCIIString() + "?service=" + service);
    }
}
