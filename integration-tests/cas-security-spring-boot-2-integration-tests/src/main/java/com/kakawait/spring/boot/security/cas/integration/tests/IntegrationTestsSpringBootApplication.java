package com.kakawait.spring.boot.security.cas.integration.tests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.ForwardedHeaderFilter;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * @author Thibaud Leprêtre
 */
@SpringBootApplication
@EnableGlobalMethodSecurity(securedEnabled = true)
public class IntegrationTestsSpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntegrationTestsSpringBootApplication.class, args);
    }

    @Bean
    FilterRegistrationBean forwardedHeaderFilter() {
        FilterRegistrationBean filterRegBean = new FilterRegistrationBean();
        filterRegBean.setFilter(new ForwardedHeaderFilter());
        filterRegBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return filterRegBean;
    }

    @RestController
    static class IndexController {

        @RequestMapping
        public Identity index(Authentication authentication) {
            if (authentication != null && StringUtils.hasText(authentication.getName())) {
                return new Identity(authentication.getName(), getProxyGrantingTicket(authentication).orElse(null));
            }
            throw new IllegalStateException();
        }

        private Optional<AttributePrincipal> getAttributePrincipal(Object o) {
            if (!(o instanceof CasAuthenticationToken)) {
                return Optional.empty();
            }
            return Optional.of(((CasAuthenticationToken) o).getAssertion().getPrincipal());
        }

        /**
         * Hacky code please do not use that in production
         */
        @SuppressWarnings("Duplicates")
        private Optional<String> getProxyGrantingTicket(Authentication authentication) {
            Optional<AttributePrincipal> attributePrincipal = getAttributePrincipal(authentication);
            if (!attributePrincipal.isPresent() || !(attributePrincipal.get() instanceof AttributePrincipalImpl)) {
                return Optional.empty();
            }
            Field field = ReflectionUtils.findField(AttributePrincipalImpl.class, "proxyGrantingTicket");
            ReflectionUtils.makeAccessible(field);
            return Optional.ofNullable(ReflectionUtils.getField(field, attributePrincipal.get())).map(Object::toString);
        }

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Identity {
            private String username;

            private String ptg;
        }
    }

}
