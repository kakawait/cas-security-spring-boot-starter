package com.kakawait.spring.security.cas.web.authentication;

import com.kakawait.spring.security.cas.LaxServiceProperties;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.web.authentication.ServiceAuthenticationDetails;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Thibaud Leprêtre
 */
public class ProxyCallbackAndServiceAuthenticationDetailsSourceTest {

    @Test
    public void buildDetails_WithUri_DefaultProxyCallbackAndServiceAuthenticationDetails() {
        ServiceProperties serviceProperties = new LaxServiceProperties();
        ProxyCallbackAndServiceAuthenticationDetailsSource authenticationDetailsSource =
                new ProxyCallbackAndServiceAuthenticationDetailsSource(serviceProperties, URI.create("/cas/callback"));

        MockHttpServletRequest context = new MockHttpServletRequest();

        ServiceAuthenticationDetails serviceAuthenticationDetails = authenticationDetailsSource.buildDetails(context);
        assertThat(serviceAuthenticationDetails)
                .isInstanceOf(DefaultProxyCallbackAndServiceAuthenticationDetails.class);
        assertThat(serviceAuthenticationDetails.getServiceUrl()).isEqualTo("http://localhost");
        assertThat(((ProxyCallbackAndServiceAuthenticationDetails) serviceAuthenticationDetails).getProxyCallbackUrl())
                .isEqualTo("http://localhost/cas/callback");
    }

    @Test
    public void buildDetails_WithCustomProxyCallbackAndServiceAuthenticationDetails_DelegateTo() {
        ServiceProperties serviceProperties = new LaxServiceProperties();
        ProxyCallbackAndServiceAuthenticationDetailsSource authenticationDetailsSource =
                new ProxyCallbackAndServiceAuthenticationDetailsSource(serviceProperties,
                        new ProxyCallbackAndServiceAuthenticationDetails() {
                            private static final long serialVersionUID = 197549373788141292L;

                            @Override
                            public String getProxyCallbackUrl() {
                                return "http://bat.man/callback";
                            }

                            @Override
                            public void setContext(HttpServletRequest context) {
                                // do nothing
                            }

                            @Override
                            public String getServiceUrl() {
                                return "http://bat.man/";
                            }
                        });

        ServiceAuthenticationDetails serviceAuthenticationDetails =
                authenticationDetailsSource.buildDetails(new MockHttpServletRequest());
        assertThat(serviceAuthenticationDetails).isInstanceOf(ProxyCallbackAndServiceAuthenticationDetails.class);
        assertThat(serviceAuthenticationDetails.getServiceUrl()).isEqualTo("http://bat.man/");
        assertThat(((ProxyCallbackAndServiceAuthenticationDetails) serviceAuthenticationDetails).getProxyCallbackUrl())
                .isEqualTo("http://bat.man/callback");
    }
}
