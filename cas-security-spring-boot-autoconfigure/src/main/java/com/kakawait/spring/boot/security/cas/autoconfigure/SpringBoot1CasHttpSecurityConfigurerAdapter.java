package com.kakawait.spring.boot.security.cas.autoconfigure;

import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.List;

import static com.kakawait.spring.boot.security.cas.autoconfigure.SpringBoot1CasHttpSecurityConfigurerAdapter.SpringBoot1SecurityProperties.SECURITY_PROPERTIES_HEADERS_CLASS;

/**
 * @author Thibaud Lepretre
 */
@Order(CasSecurityProperties.CAS_AUTH_ORDER - 10)
class SpringBoot1CasHttpSecurityConfigurerAdapter extends CasSecurityConfigurerAdapter {

    private static final String SPRING_BOOT_WEB_SECURITY_CONFIGURATION_CLASS =
            "org.springframework.boot.autoconfigure.security.SpringBootWebSecurityConfiguration";

    private final SpringBoot1SecurityProperties securityProperties;

    SpringBoot1CasHttpSecurityConfigurerAdapter(SpringBoot1SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        if (securityProperties.isRequireSsl()) {
            http.requiresChannel().anyRequest().requiresSecure();
        }
        if (!securityProperties.isEnableCsrf()) {
            http.csrf().disable();
        }
        configureHeaders(http);
        if (securityProperties.getBasic().isEnabled()) {
            BasicAuthenticationFilter basicAuthFilter = new BasicAuthenticationFilter(
                    http.getSharedObject(ApplicationContext.class).getBean(AuthenticationManager.class));
            http.addFilterBefore(basicAuthFilter, CasAuthenticationFilter.class);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void configureHeaders(HttpSecurity http) throws Exception {
        Method method = ReflectionUtils.findMethod(Class.forName(SPRING_BOOT_WEB_SECURITY_CONFIGURATION_CLASS),
                "configureHeaders", HeadersConfigurer.class, Class.forName(SECURITY_PROPERTIES_HEADERS_CLASS));
        ReflectionUtils.invokeMethod(method, null, http.headers(), securityProperties.getHeaders());
    }

    @SuppressWarnings("ConstantConditions")
    static class SpringBoot1SecurityProperties {

        static final String SECURITY_PROPERTIES_HEADERS_CLASS =
                "org.springframework.boot.autoconfigure.security.SecurityProperties$Headers";

        private final Object securityProperties;

        SpringBoot1SecurityProperties(Object securityProperties) {
            this.securityProperties = securityProperties;
        }

        boolean isRequireSsl() {
            Method method = ReflectionUtils.findMethod(securityProperties.getClass(), "isRequireSsl");
            return (boolean) ReflectionUtils.invokeMethod(method, securityProperties);
        }

        boolean isEnableCsrf() {
            Method method = ReflectionUtils.findMethod(securityProperties.getClass(), "isEnableCsrf");
            return (boolean) ReflectionUtils.invokeMethod(method, securityProperties);
        }

        public Object getHeaders() {
            Method method = ReflectionUtils.findMethod(securityProperties.getClass(), "getHeaders");
            return ReflectionUtils.invokeMethod(method, securityProperties);
        }

        public Basic getBasic() {
            Method method = ReflectionUtils.findMethod(securityProperties.getClass(), "getBasic");
            return new Basic(ReflectionUtils.invokeMethod(method, securityProperties));
        }

        public User getUser() {
            Method method = ReflectionUtils.findMethod(securityProperties.getClass(), "getUser");
            return new User(ReflectionUtils.invokeMethod(method, securityProperties));
        }

        static class Basic {
            private final Object basicProperties;

            public Basic(Object basicProperties) {
                this.basicProperties = basicProperties;
            }

            public boolean isEnabled() {
                Method method = ReflectionUtils.findMethod(basicProperties.getClass(), "isEnabled");
                return (boolean) ReflectionUtils.invokeMethod(method, basicProperties);
            }
        }

        static class User {
            private final Object userProperties;

            public User(Object userProperties) {
                this.userProperties = userProperties;
            }

            @SuppressWarnings("unchecked")
            public List<String> getRoles() {
                Method method = ReflectionUtils.findMethod(userProperties.getClass(), "getRole");
                return (List<String>) ReflectionUtils.invokeMethod(method, userProperties);
            }
        }
    }
}
