package com.kakawait.spring.security.cas.web.authentication;

import com.kakawait.spring.security.cas.LaxServiceProperties;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.cas.ServiceProperties;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Thibaud Leprêtre
 */
public class DefaultProxyCallbackAndServiceAuthenticationDetailsTest {

    private ServiceProperties serviceProperties;

    private MockHttpServletRequest request;

    @Before
    public void setUp() {
        serviceProperties = new LaxServiceProperties();
        request = new MockHttpServletRequest();
    }

    @Test
    public void getProxyCallbackUrl_NullProxyCallbackUri_Null() {
        ProxyCallbackAndServiceAuthenticationDetails authenticationDetails =
                new DefaultProxyCallbackAndServiceAuthenticationDetails(serviceProperties, request, null);

        assertThat(authenticationDetails.getProxyCallbackUrl()).isNull();
    }

    @Test
    public void getProxyCallbackUrl_AbsoluteProxyCallbackUri_NoTransformation() {
        ProxyCallbackAndServiceAuthenticationDetails authenticationDetails =
                new DefaultProxyCallbackAndServiceAuthenticationDetails(serviceProperties, request,
                        URI.create("http://localhost/cas/callback"));

        assertThat(authenticationDetails.getProxyCallbackUrl()).isEqualTo("http://localhost/cas/callback");
    }

    @Test
    public void getProxyCallbackUrl_ProxyCallbackUriAndWithoutContextPath_AppendToBaseUrl() {
        request.setRequestURI("/john/wick");
        ProxyCallbackAndServiceAuthenticationDetails authenticationDetails =
                new DefaultProxyCallbackAndServiceAuthenticationDetails(serviceProperties, request,
                        URI.create("/cas/callback"));

        assertThat(authenticationDetails.getProxyCallbackUrl()).isEqualTo("http://localhost/cas/callback");
    }

    @Test
    public void getProxyCallbackUrl_ProxyCallbackUriAndWithContextPath_AppendToBaseUrl() {
        request.setContextPath("/john");
        request.setRequestURI("/john/wick");
        ProxyCallbackAndServiceAuthenticationDetails authenticationDetails =
                new DefaultProxyCallbackAndServiceAuthenticationDetails(serviceProperties, request,
                        URI.create("/cas/callback"));

        assertThat(authenticationDetails.getProxyCallbackUrl()).isEqualTo("http://localhost/john/cas/callback");
    }

    @Test
    public void getServiceUrl_WithoutArtifactParameterQueryString_HttpServletRequestUrl() {
        request.setQueryString("foo=a&bar=b");
        ProxyCallbackAndServiceAuthenticationDetails authenticationDetails =
                new DefaultProxyCallbackAndServiceAuthenticationDetails(serviceProperties, request,
                        URI.create("/cas/callback"));

        assertThat(authenticationDetails.getServiceUrl()).isEqualTo("http://localhost?foo=a&bar=b");
    }

    @Test
    public void getServiceUrl_WithArtifactParameterQueryString_CleanQueryString() {
        request.setQueryString("foo=a&bar=b&"
                + serviceProperties.getArtifactParameter() + "=ST-21-c1gk6jBcfYnatLbNExfx-0623277bc36a");
        ProxyCallbackAndServiceAuthenticationDetails authenticationDetails =
                new DefaultProxyCallbackAndServiceAuthenticationDetails(serviceProperties, request,
                        URI.create("/cas/callback"));

        assertThat(authenticationDetails.getServiceUrl()).isEqualTo("http://localhost?foo=a&bar=b");
    }

}
