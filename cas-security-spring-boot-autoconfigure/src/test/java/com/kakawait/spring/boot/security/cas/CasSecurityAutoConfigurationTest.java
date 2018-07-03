package com.kakawait.spring.boot.security.cas;


import com.kakawait.spring.security.cas.LaxServiceProperties;
import com.kakawait.spring.security.cas.client.ticket.AttributePrincipalProxyTicketProvider;
import com.kakawait.spring.security.cas.client.ticket.ProxyTicketProvider;
import com.kakawait.spring.security.cas.client.validation.AssertionProvider;
import com.kakawait.spring.security.cas.client.validation.SecurityContextHolderAssertionProvider;
import com.kakawait.spring.security.cas.web.authentication.CasLogoutSuccessHandler;
import com.kakawait.spring.security.cas.web.authentication.ProxyCallbackAndServiceAuthenticationDetailsSource;
import com.kakawait.spring.security.cas.web.authentication.RequestAwareCasLogoutSuccessHandler;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorageImpl;
import org.jasig.cas.client.validation.TicketValidator;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.authentication.ServiceAuthenticationDetailsSource;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.configuration.ObjectPostProcessorConfiguration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.net.URI;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Thibaud Leprêtre
 */
public class CasSecurityAutoConfigurationTest {

    private static final String CAS_SERVER_BASE_URL = "http://localhost:8443/cas";

    private static final String CAS_SERVICE_BASE_URL = "http://localhost:8080";

    private static final String CAS_SERVICE_LOGIN_URL = CAS_SERVICE_BASE_URL + "/login";

    private ConfigurableApplicationContext context;

    @After
    public void tearDown() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test(expected = NoSuchBeanDefinitionException.class)
    public void autoConfiguration_MissingCasServerBaseUrl_SkipAutoConfiguration() {
        load(new Properties(), EmptyConfiguration.class);

        context.getBean(CasSecurityAutoConfiguration.class);
    }

    @Test(expected = NoSuchBeanDefinitionException.class)
    public void autoConfiguration_DisableProperty_SkipAutoConfiguration() {
        Properties properties = new Properties();
        properties.put("security.cas.server.base-url", CAS_SERVER_BASE_URL);
        properties.put("security.cas.enabled", "false");
        load(properties, EmptyConfiguration.class);

        context.getBean(CasSecurityAutoConfiguration.class);
    }

    @Test
    public void autoConfigure_WithoutServiceBaseUrl_Exception() {
        Properties properties = getDefaultProperties();
        properties.remove("security.cas.service.base-url");

        assertThatThrownBy(() -> load(properties, EmptyConfiguration.class))
                .isInstanceOf(BeanCreationException.class)
                .hasRootCauseExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageEndingWith("java.lang.IllegalArgumentException: Cas service base url must not be null " +
                        "(ref property security.cas.service.base-url)");
    }

    @Test
    public void autoConfigure_DynamicModeWithoutServiceBaseUrl_NoException() {
        load(getDynamicModeProperties(), EmptyConfiguration.class);
    }

    @Test
    public void autoConfigure_StaticMode_DefaultBeans() {
        load(EmptyConfiguration.class);

        assertThat(context.getBean(ServiceProperties.class))
                .isExactlyInstanceOf(ServiceProperties.class)
                .hasFieldOrPropertyWithValue("service", CAS_SERVICE_LOGIN_URL);
        assertThat(context.getBean(CasAuthenticationEntryPoint.class))
                .isExactlyInstanceOf(CasAuthenticationEntryPoint.class);
        assertThat(context.getBean(ServiceAuthenticationDetailsSource.class))
                .isExactlyInstanceOf(ServiceAuthenticationDetailsSource.class);
        assertThat(context.getBean(CasLogoutSuccessHandler.class)).isExactlyInstanceOf(CasLogoutSuccessHandler.class);
    }

    @Test
    public void autoConfigure_DynamicMode_SpecificBeans() {
        load(getDynamicModeProperties(), EmptyConfiguration.class);

        assertThat(context.getBean(ServiceProperties.class))
                .isExactlyInstanceOf(LaxServiceProperties.class)
                .hasFieldOrPropertyWithValue("service", null);
        assertThat(context.getBean(CasLogoutSuccessHandler.class))
                .isExactlyInstanceOf(RequestAwareCasLogoutSuccessHandler.class);
        assertThat(context.getBean(ServiceAuthenticationDetailsSource.class))
                .isExactlyInstanceOf(ProxyCallbackAndServiceAuthenticationDetailsSource.class);
        assertThat(context.getBean(CasLogoutSuccessHandler.class))
                .isExactlyInstanceOf(RequestAwareCasLogoutSuccessHandler.class);
    }

    @Test
    public void autoConfigure_EmptyConfiguration_ProxyGrantingTicketStorageImplBean() {
        load(EmptyConfiguration.class);

        ProxyGrantingTicketStorage proxyGrantingTicketStorage = context.getBean(ProxyGrantingTicketStorage.class);
        assertThat(proxyGrantingTicketStorage).isExactlyInstanceOf(ProxyGrantingTicketStorageImpl.class);

        assertThat(context.getBean(TicketValidator.class))
                .hasFieldOrPropertyWithValue("proxyGrantingTicketStorage", proxyGrantingTicketStorage);
    }

    @Test
    public void autoConfiguration_EmptyConfiguration_SecurityContextHolderAssertionProviderBean() {
        load(EmptyConfiguration.class);

        assertThat(context.getBean(AssertionProvider.class)).isInstanceOf(SecurityContextHolderAssertionProvider.class);
    }

    @Test
    public void autoConfiguration_EmptyConfiguration_AttributePrincipalProxyTicketProviderBean() {
        load(EmptyConfiguration.class);

        assertThat(context.getBean(ProxyTicketProvider.class))
                .isInstanceOf(AttributePrincipalProxyTicketProvider.class);
    }

    @Test
    public void autoConfigure_WithProxyCallbackPathAndCallbackUrl_AbsoluteProxyCallbackUri() {
        Properties properties = getDynamicModeProperties();
        properties.put("security.cas.service.paths.proxy-callback", "/cas/proxy-callback");
        properties.put("security.cas.service.callback-base-url", "http://app:8081/test/");

        load(properties, EmptyConfiguration.class);
        assertThat(context.getBean(ProxyCallbackAndServiceAuthenticationDetailsSource.class))
                .hasFieldOrPropertyWithValue("proxyCallbackUri", URI.create("http://app:8081/test/cas/proxy-callback"));
    }

    @Test
    public void autoConfigure_WithProxyCallbackPathButWithoutCallbackUrl_RelativeProxyCallbackUri() {
        Properties properties = getDynamicModeProperties();
        properties.put("security.cas.service.paths.proxy-callback", "/cas/proxy-callback");

        load(properties, EmptyConfiguration.class);
        assertThat(context.getBean(ProxyCallbackAndServiceAuthenticationDetailsSource.class))
                .hasFieldOrPropertyWithValue("proxyCallbackUri", URI.create("/cas/proxy-callback"));
    }

    @Test
    public void autoConfigure_EmptyConfiguration_DefaultCasSecurityConfigurerAdapterBean() {
        load(EmptyConfiguration.class);

        String beanName ="com.kakawait.spring.boot.security.cas.CasSecurityAutoConfiguration$" +
                "DefaultCasSecurityConfigurerAdapter";
        context.getBean(beanName);
    }

    @Configuration
    static class EmptyConfiguration {}

    private Properties getDefaultProperties() {
        Properties properties = new Properties();
        properties.put("security.cas.server.base-url", CAS_SERVER_BASE_URL);
        properties.put("security.cas.service.base-url", CAS_SERVICE_BASE_URL);

        return properties;
    }

    private Properties getDynamicModeProperties() {
        Properties properties = new Properties();
        properties.put("security.cas.server.base-url", CAS_SERVER_BASE_URL);
        properties.put("security.cas.service.resolution-mode", "dynamic");

        return properties;
    }

    private void load(Class<?>... configs) {
        load(getDefaultProperties(), configs);
    }

    private void load(Properties properties, Class<?>... configs) {
        Class[] securityConfigurationsClasses = {SecurityFilterAutoConfiguration.class,
                ObjectPostProcessorConfiguration.class, AuthenticationConfiguration.class,
                WebSecurityConfiguration.class};

        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.getEnvironment().getPropertySources().addFirst(new PropertiesPropertySource("test", properties));
        context.register(configs);
        context.register(DummyAuthenticationManagerConfiguration.class);
        context.register(securityConfigurationsClasses);
        context.register(CasSecurityAutoConfiguration.class);
        context.setServletContext(new MockServletContext());
        context.refresh();
        this.context = context;
    }

    private static class DummyAuthenticationManagerConfiguration {
        @Bean
        AuthenticationManager authenticationManager() {
            return authentication -> new TestingAuthenticationToken("kakawait", "secret");
        }
    }
}
