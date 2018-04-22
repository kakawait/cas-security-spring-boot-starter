package com.kakawait.spring.security.cas.web.authentication;

import com.kakawait.spring.security.cas.LaxServiceProperties;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.cas.ServiceProperties;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Thibaud Leprêtre
 */
public class ProxyCallbackAndServiceAuthenticationDetailsSourceTest {

    @Test
    public void buildDetails_WithRequest_DefaultProxyCallbackAndServiceAuthenticationDetails() {
        ServiceProperties serviceProperties = new LaxServiceProperties();
        ProxyCallbackAndServiceAuthenticationDetailsSource authenticationDetailsSource =
                new ProxyCallbackAndServiceAuthenticationDetailsSource(serviceProperties, URI.create("/cas/callback"));

        assertThat(authenticationDetailsSource.buildDetails(new MockHttpServletRequest()))
                .isInstanceOf(DefaultProxyCallbackAndServiceAuthenticationDetails.class);
    }
}
