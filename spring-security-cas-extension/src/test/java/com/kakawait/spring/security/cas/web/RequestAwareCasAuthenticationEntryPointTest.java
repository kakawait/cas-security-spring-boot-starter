package com.kakawait.spring.security.cas.web;

import com.kakawait.spring.security.cas.LaxServiceProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Thibaud Leprêtre
 */
public class RequestAwareCasAuthenticationEntryPointTest {

    private static final String CAS_SERVER_LOGIN_URL = "http://cas.server.com/cas/login";

    @Test
    public void constructor_WithNullLoginPath_IllegalArgumentException() {
        assertThatThrownBy(() -> new RequestAwareCasAuthenticationEntryPoint(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void afterPropertiesSet_WithNullOrEmptyLoginUrl_IllegalArgumentException() {
        RequestAwareCasAuthenticationEntryPoint entryPoint =
                new RequestAwareCasAuthenticationEntryPoint(URI.create("/"));
        entryPoint.setLoginUrl(null);

        assertThatThrownBy(entryPoint::afterPropertiesSet).isInstanceOf(IllegalArgumentException.class);

        entryPoint.setLoginUrl("");
        assertThatThrownBy(entryPoint::afterPropertiesSet).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void afterPropertiesSet_WithNullLoginUrl_IllegalArgumentException() {
        RequestAwareCasAuthenticationEntryPoint entryPoint =
                new RequestAwareCasAuthenticationEntryPoint(URI.create("/"));
        entryPoint.setServiceProperties(null);

        assertThatThrownBy(entryPoint::afterPropertiesSet).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void createServiceUrl_AbsoluteUrlAsLoginPath_NoTransformation() {
        String loginPath = "http://localhost/my/custom/login/path";
        RequestAwareCasAuthenticationEntryPoint entryPoint =
                new RequestAwareCasAuthenticationEntryPoint(URI.create(loginPath));
        entryPoint.setLoginUrl(CAS_SERVER_LOGIN_URL);
        entryPoint.setServiceProperties(new LaxServiceProperties());
        entryPoint.afterPropertiesSet();

        MockHttpServletRequest request = new MockHttpServletRequest(HttpMethod.GET.name(), "/john/wick");

        String serviceUrl = entryPoint.createServiceUrl(request, new MockHttpServletResponse());

        assertThat(serviceUrl).isNotBlank().isEqualTo(loginPath);
    }

    @Test
    public void createServiceUrl_WithoutContextPath_AppendToBaseUrl() {
        String loginPath = "/my/custom/login/path";
        RequestAwareCasAuthenticationEntryPoint entryPoint =
                new RequestAwareCasAuthenticationEntryPoint(URI.create(loginPath));
        entryPoint.setLoginUrl(CAS_SERVER_LOGIN_URL);
        entryPoint.setServiceProperties(new LaxServiceProperties());
        entryPoint.afterPropertiesSet();

        MockHttpServletRequest request = new MockHttpServletRequest(HttpMethod.GET.name(), "/john/wick");

        String serviceUrl = entryPoint.createServiceUrl(request, new MockHttpServletResponse());

        assertThat(serviceUrl).isNotBlank().isEqualTo("http://localhost" + loginPath);
    }

    @Test
    public void createServiceUrl_WithContextPath_AppendToBaseUrl() {
        String loginPath = "/my/custom/login/path";
        RequestAwareCasAuthenticationEntryPoint entryPoint =
                new RequestAwareCasAuthenticationEntryPoint(URI.create(loginPath));
        entryPoint.setLoginUrl(CAS_SERVER_LOGIN_URL);
        entryPoint.setServiceProperties(new LaxServiceProperties());
        entryPoint.afterPropertiesSet();

        MockHttpServletRequest request = new MockHttpServletRequest(HttpMethod.GET.name(), "/john/wick");
        request.setContextPath("/john");

        String serviceUrl = entryPoint.createServiceUrl(request, new MockHttpServletResponse());

        assertThat(serviceUrl).isNotBlank().isEqualTo("http://localhost/john" + loginPath);
    }
}
