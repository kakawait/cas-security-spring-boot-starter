package com.kakawait.spring.boot.security.cas;


import com.kakawait.spring.security.cas.client.ticket.AttributePrincipalProxyTicketProvider;
import com.kakawait.spring.security.cas.client.ticket.ProxyTicketProvider;
import com.kakawait.spring.security.cas.client.validation.AssertionProvider;
import com.kakawait.spring.security.cas.client.validation.SecurityContextHolderAssertionProvider;
import org.junit.After;
import org.junit.Test;
import org.springframework.boot.autoconfigure.security.SecurityFilterAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.configuration.ObjectPostProcessorConfiguration;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Thibaud Leprêtre
 */
public class CasSecurityAutoConfigurationTest {

    private ConfigurableApplicationContext context;

    @After
    public void tearDown() {
        if (this.context != null) {
            this.context.close();
        }
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

    @Configuration
    static class EmptyConfiguration {
    }

    private void load(Class<?>... configs) {
        Properties properties = new Properties();
        properties.put("security.cas.server.base-url", "http://localhost:8888/cas");

        load(properties, configs);
    }

    private void load(Properties properties, Class<?>... configs) {
        Class[] securityConfigurationsClasses = {SecurityFilterAutoConfiguration.class,
                ObjectPostProcessorConfiguration.class, AuthenticationConfiguration.class};

        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.getEnvironment().getPropertySources().addFirst(new PropertiesPropertySource("test", properties));
        context.register(configs);
        context.register(securityConfigurationsClasses);
        context.register(CasSecurityAutoConfiguration.class);
        context.setServletContext(new MockServletContext());
        context.refresh();
        this.context = context;
    }
}
