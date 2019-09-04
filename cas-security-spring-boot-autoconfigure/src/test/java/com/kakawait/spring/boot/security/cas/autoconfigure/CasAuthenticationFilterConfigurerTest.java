package com.kakawait.spring.boot.security.cas.autoconfigure;

import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.cas.web.authentication.ServiceAuthenticationDetailsSource;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Thibaud Leprêtre
 */
public class CasAuthenticationFilterConfigurerTest {

    @Test
    public void configure_WithAnyParameters_InjectInsideCasAuthenticationFilter() {
        ProxyGrantingTicketStorage proxyGrantingTicketStorage = Mockito.mock(ProxyGrantingTicketStorage.class);

        ServiceAuthenticationDetailsSource serviceAuthenticationDetailsSource =
                Mockito.mock(ServiceAuthenticationDetailsSource.class);
        String proxyReceptorUrl = "dummyProxyReceptorUrl";
        AuthenticationFailureHandler authenticationFailureHandler = Mockito.mock(AuthenticationFailureHandler.class);
        AuthenticationSuccessHandler authenticationSuccessHandler = Mockito.mock(AuthenticationSuccessHandler.class);
        AuthenticationFailureHandler proxyAuthenticationFailureHandler =
                Mockito.mock(AuthenticationFailureHandler.class);
        RequestMatcher requestMatcher = Mockito.mock(RequestMatcher.class);

        CasAuthenticationFilter filter = new CasAuthenticationFilter();

        CasAuthenticationFilterConfigurer configurer = new CasAuthenticationFilterConfigurer();
        configurer.proxyGrantingTicketStorage(proxyGrantingTicketStorage)
                  .proxyReceptorUrl(proxyReceptorUrl)
                  .serviceAuthenticationDetailsSource(serviceAuthenticationDetailsSource)
                  .authenticationFailureHandler(authenticationFailureHandler)
                  .authenticationSuccessHandler(authenticationSuccessHandler)
                  .proxyAuthenticationFailureHandler(proxyAuthenticationFailureHandler)
                  .requiresAuthenticationRequestMatcher(requestMatcher);

        configurer.configure(filter);

        assertThat(filter)
                .hasFieldOrPropertyWithValue("proxyGrantingTicketStorage", proxyGrantingTicketStorage)
                .hasFieldOrPropertyWithValue("authenticationDetailsSource", serviceAuthenticationDetailsSource)
                .hasFieldOrPropertyWithValue("successHandler", authenticationSuccessHandler)
                .hasFieldOrPropertyWithValue("proxyFailureHandler", proxyAuthenticationFailureHandler)
                .hasFieldOrPropertyWithValue("requiresAuthenticationRequestMatcher", requestMatcher)
                .hasFieldOrPropertyWithValue("proxyReceptorMatcher",
                        new AntPathRequestMatcher("/**" + proxyReceptorUrl))
                .hasFieldOrPropertyWithValue("failureHandler.serviceTicketFailureHandler",
                        authenticationFailureHandler);
    }
}
