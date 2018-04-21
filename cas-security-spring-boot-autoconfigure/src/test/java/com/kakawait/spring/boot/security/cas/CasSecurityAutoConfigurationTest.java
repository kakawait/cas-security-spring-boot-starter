package com.kakawait.spring.boot.security.cas;


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
    public void test() {
        load(EmptyConfiguration.class);
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
