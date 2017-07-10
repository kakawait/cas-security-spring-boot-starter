package com.kakawait.spring.boot.security.cas;

import com.kakawait.spring.security.cas.LaxServiceProperties;
import com.kakawait.spring.security.cas.web.RequestAwareCasAuthenticationEntryPoint;
import com.kakawait.spring.security.cas.web.authentication.ProxyCallbackAndServiceAuthenticationDetailsSource;
import lombok.Getter;
import lombok.NonNull;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorageImpl;
import org.jasig.cas.client.validation.ProxyList;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.userdetails.AbstractCasAssertionUserDetailsService;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.authentication.ServiceAuthenticationDetailsSource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.kakawait.spring.boot.security.cas.CasSecurityAutoConfiguration.CasLoginSecurityConfiguration;
import static com.kakawait.spring.boot.security.cas.CasSecurityAutoConfiguration.DefaultCasSecurityConfigurerAdapter;
import static com.kakawait.spring.boot.security.cas.CasSecurityAutoConfiguration.DynamicCasSecurityConfiguration;
import static com.kakawait.spring.boot.security.cas.CasSecurityAutoConfiguration.StaticCasSecurityConfiguration;

/**
 * @author Thibaud Leprêtre
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass(EnableWebSecurity.class)
@AutoConfigureBefore(SecurityAutoConfiguration.class)
@Conditional(CasSecurityAutoConfiguration.CasSecurityCondition.class)
@EnableConfigurationProperties(CasSecurityProperties.class)
@Import({CasLoginSecurityConfiguration.class, CasAssertionUserDetailsServiceConfiguration.class,
        CasTicketValidatorConfiguration.class, DefaultCasSecurityConfigurerAdapter.class,
        DynamicCasSecurityConfiguration.class, StaticCasSecurityConfiguration.class})
@EnableWebSecurity
public class CasSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ServiceProperties.class)
    @ConditionalOnProperty(value = "security.cas.service.resolution-mode", havingValue = "static",
            matchIfMissing = true)
    ServiceProperties serviceProperties(CasSecurityProperties casSecurityProperties) {
        ServiceProperties serviceProperties = new ServiceProperties();

        URI baseUrl = casSecurityProperties.getService().getBaseUrl();
        serviceProperties.setService(buildUrl(baseUrl, casSecurityProperties.getService().getPaths().getLogin()));
        return serviceProperties;
    }

    @Bean
    @ConditionalOnMissingBean(ServiceProperties.class)
    @ConditionalOnProperty(value = "security.cas.service.resolution-mode", havingValue = "dynamic")
    ServiceProperties laxServiceProperties() {
        return new LaxServiceProperties();
    }

    @Bean
    @ConditionalOnMissingBean(ProxyGrantingTicketStorage.class)
    ProxyGrantingTicketStorage proxyGrantingTicketStorage() {
        return new ProxyGrantingTicketStorageImpl();
    }

    @Getter
    static abstract class AbstractCasSecurityConfiguration {
        private final CasSecurityProperties casSecurityProperties;

        private final ServiceProperties serviceProperties;

        private final String serverLoginUrl;

        AbstractCasSecurityConfiguration(CasSecurityProperties casSecurityProperties,
                ServiceProperties serviceProperties) {
            serverLoginUrl = UriComponentsBuilder
                    .fromUri(casSecurityProperties.getServer().getBaseUrl())
                    .path(casSecurityProperties.getServer().getPaths().getLogin())
                    .toUriString();
            this.casSecurityProperties = casSecurityProperties;
            this.serviceProperties = serviceProperties;
        }
    }

    @ConditionalOnProperty(value = "security.cas.service.resolution-mode", havingValue = "static",
            matchIfMissing = true)
    static class StaticCasSecurityConfiguration extends AbstractCasSecurityConfiguration {
        public StaticCasSecurityConfiguration(CasSecurityProperties casSecurityProperties,
                ServiceProperties serviceProperties) {
            super(casSecurityProperties, serviceProperties);
        }

        @Bean
        @ConditionalOnMissingBean(CasAuthenticationEntryPoint.class)
        CasAuthenticationEntryPoint casAuthenticationEntryPoint() {
            CasAuthenticationEntryPoint entryPoint = new CasAuthenticationEntryPoint();
            entryPoint.setServiceProperties(getServiceProperties());
            entryPoint.setLoginUrl(getServerLoginUrl());
            return entryPoint;
        }

        @Bean
        @ConditionalOnMissingBean(ServiceAuthenticationDetailsSource.class)
        ServiceAuthenticationDetailsSource serviceAuthenticationDetailsSource() {
            return new ServiceAuthenticationDetailsSource(getServiceProperties());
        }
    }

    @ConditionalOnProperty(value = "security.cas.service.resolution-mode", havingValue = "dynamic")
    static class DynamicCasSecurityConfiguration extends AbstractCasSecurityConfiguration {
        DynamicCasSecurityConfiguration(CasSecurityProperties casSecurityProperties,
                ServiceProperties serviceProperties) {
            super(casSecurityProperties, serviceProperties);
        }

        @Bean
        @ConditionalOnMissingBean(CasAuthenticationEntryPoint.class)
        CasAuthenticationEntryPoint casAuthenticationEntryPoint() {
            String loginPath = getCasSecurityProperties().getService().getPaths().getLogin();
            CasAuthenticationEntryPoint entryPoint = new RequestAwareCasAuthenticationEntryPoint(loginPath);
            entryPoint.setServiceProperties(getServiceProperties());
            entryPoint.setLoginUrl(getServerLoginUrl());
            return entryPoint;
        }

        @Bean
        @ConditionalOnMissingBean(ServiceAuthenticationDetailsSource.class)
        ServiceAuthenticationDetailsSource serviceAuthenticationDetailsSource(
                CasSecurityProperties casSecurityProperties) {
            String proxyCallbackPath = casSecurityProperties.getService().getPaths().getProxyCallback();
            return new ProxyCallbackAndServiceAuthenticationDetailsSource(getServiceProperties(), proxyCallbackPath);
        }
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    static class DefaultCasSecurityConfigurerAdapter extends CasSecurityConfigurerAdapter {

        private final CasSecurityProperties casSecurityProperties;

        private final AbstractCasAssertionUserDetailsService userDetailsService;

        private final ServiceAuthenticationDetailsSource authenticationDetailsSource;

        private final ProxyGrantingTicketStorage proxyGrantingTicketStorage;

        public DefaultCasSecurityConfigurerAdapter(CasSecurityProperties casSecurityProperties,
                AbstractCasAssertionUserDetailsService userDetailsService,
                ServiceAuthenticationDetailsSource authenticationDetailsSource,
                ProxyGrantingTicketStorage proxyGrantingTicketStorage) {
            this.casSecurityProperties = casSecurityProperties;
            this.userDetailsService = userDetailsService;
            this.authenticationDetailsSource = authenticationDetailsSource;
            this.proxyGrantingTicketStorage = proxyGrantingTicketStorage;
        }

        @Override
        public void configure(CasAuthenticationProviderSecurityBuilder provider) {
            provider.serviceResolutionMode(casSecurityProperties.getService().getResolutionMode())
                    .authenticationUserDetailsService(userDetailsService)
                    .key(casSecurityProperties.getKey());
        }

        @Override
        public void configure(CasAuthenticationFilterConfigurer filter) {
            filter.proxyReceptorUrl(casSecurityProperties.getService().getPaths().getProxyCallback())
                  .serviceAuthenticationDetailsSource(authenticationDetailsSource)
                  .proxyGrantingTicketStorage(proxyGrantingTicketStorage);
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            String logoutSuccessUrl = buildUrl(casSecurityProperties.getServer().getBaseUrl(),
                    casSecurityProperties.getServer().getPaths().getLogout());
            http.logout().permitAll().logoutSuccessUrl(logoutSuccessUrl);
        }

        @Override
        public void configure(CasTicketValidatorBuilder ticketValidator) {
            URI baseUrl = casSecurityProperties.getService().getBaseUrl();
            ticketValidator.protocolVersion(casSecurityProperties.getServer().getProtocolVersion());
            String proxyCallback = casSecurityProperties.getService().getPaths().getProxyCallback();
            if (proxyCallback != null) {
                ticketValidator.proxyCallbackUrl(buildUrl(baseUrl, proxyCallback));
            }
            if (!casSecurityProperties.getProxyValidation().isEnabled()) {
                ticketValidator.proxyChainsValidation(false);
            } else {
                List<String[]> proxyChains = casSecurityProperties
                        .getProxyValidation()
                        .getChains()
                        .stream()
                        .map(l -> l.toArray(new String[l.size()]))
                        .collect(Collectors.toList());
                ticketValidator.proxyChains(new ProxyList(proxyChains));
            }
            ticketValidator.proxyGrantingTicketStorage(proxyGrantingTicketStorage);
        }
    }

    @Order(SecurityProperties.BASIC_AUTH_ORDER - 1)
    static class CasLoginSecurityConfiguration extends WebSecurityConfigurerAdapter {

        private final List<CasSecurityConfigurer> configurers;

        private final CasSecurityProperties casSecurityProperties;

        public CasLoginSecurityConfiguration(List<CasSecurityConfigurer> configurers,
                CasSecurityProperties casSecurityProperties) {
            this.configurers = configurers;
            this.casSecurityProperties = casSecurityProperties;
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            String[] paths = getSecurePaths();
            if (paths.length > 0) {
                http.requestMatchers().antMatchers(paths);
                CasHttpSecurityConfigurer.cas().init(http);
                for (CasSecurityConfigurer configurer : configurers) {
                    configurer.init(http);
                    configurer.configure(http);
                }
            }
        }

        private String[] getSecurePaths() {
            Set<String> paths = new HashSet<>();
            for (String path : casSecurityProperties.getPaths()) {
                path = (path == null ? "" : path.trim());
                if (path.equals("/**")) {
                    return new String[] { path };
                }
                if (StringUtils.hasText(path)) {
                    paths.add(path);
                }
            }
            return paths.toArray(new String[paths.size()]);
        }
    }

    @SuppressWarnings("unused")
    static class CasSecurityCondition extends AllNestedConditions {

        public CasSecurityCondition() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @ConditionalOnProperty(value = "security.cas.enabled", havingValue = "true", matchIfMissing = true)
        static class EnabledProperty {}

        @ConditionalOnProperty(value = "security.cas.server.base-url")
        static class ServerInstanceProperty {}
    }

    static String buildUrl(URI baseUrl, @NonNull String path) {
        if (baseUrl != null) {
            return UriComponentsBuilder
                    .fromUri(baseUrl)
                    .path(path)
                    .toUriString();
        }
        return path;
    }

}
