package com.kakawait.spring.boot.security.cas.autoconfigure;

import com.kakawait.spring.boot.security.cas.autoconfigure.SpringBoot1CasHttpSecurityConfigurerAdapter.SpringBoot1SecurityProperties;
import com.kakawait.spring.security.cas.LaxServiceProperties;
import com.kakawait.spring.security.cas.client.ticket.AttributePrincipalProxyTicketProvider;
import com.kakawait.spring.security.cas.client.ticket.ProxyTicketProvider;
import com.kakawait.spring.security.cas.client.validation.AssertionProvider;
import com.kakawait.spring.security.cas.client.validation.SecurityContextHolderAssertionProvider;
import com.kakawait.spring.security.cas.web.RequestAwareCasAuthenticationEntryPoint;
import com.kakawait.spring.security.cas.web.authentication.CasLogoutSuccessHandler;
import com.kakawait.spring.security.cas.web.authentication.ProxyCallbackAndServiceAuthenticationDetailsSource;
import com.kakawait.spring.security.cas.web.authentication.RequestAwareCasLogoutSuccessHandler;
import lombok.Getter;
import lombok.NonNull;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorageImpl;
import org.jasig.cas.client.validation.ProxyList;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.ConditionalOnDefaultWebSecurity;
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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.kakawait.spring.boot.security.cas.autoconfigure.CasSecurityAutoConfiguration.CasLoginSecurityConfiguration;
import static com.kakawait.spring.boot.security.cas.autoconfigure.CasSecurityAutoConfiguration.DefaultCasSecurityConfigurerAdapter;
import static com.kakawait.spring.boot.security.cas.autoconfigure.CasSecurityAutoConfiguration.DynamicCasSecurityConfiguration;
import static com.kakawait.spring.boot.security.cas.autoconfigure.CasSecurityAutoConfiguration.StaticCasSecurityConfiguration;
import static com.kakawait.spring.boot.security.cas.autoconfigure.CasSecurityProperties.CAS_AUTH_ORDER;

/**
 * @author Thibaud Lepretre
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass(EnableWebSecurity.class)
@Conditional(CasSecurityCondition.class)
@EnableConfigurationProperties(CasSecurityProperties.class)
@Import({CasLoginSecurityConfiguration.class, CasAssertionUserDetailsServiceConfiguration.class,
        CasTicketValidatorConfiguration.class, DefaultCasSecurityConfigurerAdapter.class,
        DynamicCasSecurityConfiguration.class, StaticCasSecurityConfiguration.class})
public class CasSecurityAutoConfiguration {

    private static String buildUrl(@NonNull URI baseUrl, @NonNull String path) {
        return UriComponentsBuilder.fromUri(baseUrl).path(path).toUriString();
    }

    @Bean
    @ConditionalOnMissingBean(ServiceProperties.class)
    @ConditionalOnProperty(value = "security.cas.service.resolution-mode", havingValue = "static",
            matchIfMissing = true)
    ServiceProperties serviceProperties(CasSecurityProperties casSecurityProperties) {
        ServiceProperties serviceProperties = new ServiceProperties();

        URI baseUrl = casSecurityProperties.getService().getBaseUrl();
        Assert.notNull(baseUrl, "Cas service base url must not be null (ref property security.cas.service.base-url)");

        serviceProperties.setService(buildUrl(baseUrl, casSecurityProperties.getService().getPaths().getLogin()));
        serviceProperties.setAuthenticateAllArtifacts(true);
        serviceProperties.afterPropertiesSet();
        return serviceProperties;
    }

    @Bean
    @ConditionalOnMissingBean(ServiceProperties.class)
    @ConditionalOnProperty(value = "security.cas.service.resolution-mode", havingValue = "dynamic")
    ServiceProperties laxServiceProperties() {
        LaxServiceProperties serviceProperties = new LaxServiceProperties();
        serviceProperties.setAuthenticateAllArtifacts(true);
        serviceProperties.afterPropertiesSet();
        return serviceProperties;
    }

    @Bean
    @ConditionalOnMissingBean(ProxyGrantingTicketStorage.class)
    ProxyGrantingTicketStorage proxyGrantingTicketStorage() {
        return new ProxyGrantingTicketStorageImpl();
    }

    @Bean
    @ConditionalOnMissingBean(AssertionProvider.class)
    AssertionProvider securityContextHolderAssertionProvider() {
        return new SecurityContextHolderAssertionProvider();
    }

    @Bean
    @ConditionalOnMissingBean(ProxyTicketProvider.class)
    ProxyTicketProvider attributePrincipalProxyTicketProvider(AssertionProvider assertionProvider) {
        return new AttributePrincipalProxyTicketProvider(assertionProvider);
    }

    @Bean
    @ConditionalOnClass(name = "org.springframework.boot.autoconfigure.security.SpringBootWebSecurityConfiguration")
    CasSecurityConfigurer springBoot1CasSecurityConfigurerAdapter(SecurityProperties securityProperties) {
        return new SpringBoot1CasHttpSecurityConfigurerAdapter(new SpringBoot1SecurityProperties(securityProperties));
    }

    @Getter
    abstract static class AbstractCasSecurityConfiguration {
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

        @Bean
        @ConditionalOnMissingBean(LogoutSuccessHandler.class)
        LogoutSuccessHandler casLogoutSuccessHandler(CasSecurityProperties casSecurityProperties,
                ServiceProperties serviceProperties) {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUri(casSecurityProperties.getServer().getBaseUrl())
                    .path(casSecurityProperties.getServer().getPaths().getLogout());
            return new CasLogoutSuccessHandler(builder.build().toUri(), serviceProperties);
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
            CasAuthenticationEntryPoint entryPoint = new RequestAwareCasAuthenticationEntryPoint(URI.create(loginPath));
            entryPoint.setServiceProperties(getServiceProperties());
            entryPoint.setLoginUrl(getServerLoginUrl());
            return entryPoint;
        }

        @Bean
        @ConditionalOnMissingBean(ServiceAuthenticationDetailsSource.class)
        ServiceAuthenticationDetailsSource serviceAuthenticationDetailsSource(
                CasSecurityProperties casSecurityProperties) {
            String proxyCallbackPath = casSecurityProperties.getService().getPaths().getProxyCallback();
            URI proxyCallbackUri = null;
            if (proxyCallbackPath != null) {
                URI callbackBaseUrl = casSecurityProperties.getService().getCallbackBaseUrl();
                proxyCallbackUri = callbackBaseUrl != null
                        ? UriComponentsBuilder.fromUri(callbackBaseUrl).path(proxyCallbackPath).build().toUri()
                        : URI.create(proxyCallbackPath);
            }
            return new ProxyCallbackAndServiceAuthenticationDetailsSource(getServiceProperties(), proxyCallbackUri);
        }

        @Bean
        @ConditionalOnMissingBean(LogoutSuccessHandler.class)
        LogoutSuccessHandler casLogoutSuccessHandler(CasSecurityProperties casSecurityProperties,
                ServiceProperties serviceProperties) {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUri(casSecurityProperties.getServer().getBaseUrl())
                    .path(casSecurityProperties.getServer().getPaths().getLogout());
            return new RequestAwareCasLogoutSuccessHandler(builder.build().toUri(), serviceProperties);
        }
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    static class DefaultCasSecurityConfigurerAdapter extends CasSecurityConfigurerAdapter {

        private final CasSecurityProperties casSecurityProperties;

        private final AbstractCasAssertionUserDetailsService userDetailsService;

        private final ServiceAuthenticationDetailsSource authenticationDetailsSource;

        private final ProxyGrantingTicketStorage proxyGrantingTicketStorage;

        private final LogoutSuccessHandler logoutSuccessHandler;

        public DefaultCasSecurityConfigurerAdapter(CasSecurityProperties casSecurityProperties,
                AbstractCasAssertionUserDetailsService userDetailsService,
                ServiceAuthenticationDetailsSource authenticationDetailsSource,
                ProxyGrantingTicketStorage proxyGrantingTicketStorage, LogoutSuccessHandler logoutSuccessHandler) {
            this.casSecurityProperties = casSecurityProperties;
            this.userDetailsService = userDetailsService;
            this.authenticationDetailsSource = authenticationDetailsSource;
            this.proxyGrantingTicketStorage = proxyGrantingTicketStorage;
            this.logoutSuccessHandler = logoutSuccessHandler;
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
            http.logout().permitAll().logoutSuccessHandler(logoutSuccessHandler);
        }

        @Override
        public void configure(CasTicketValidatorBuilder ticketValidator) {
            URI baseUrl = (casSecurityProperties.getService().getCallbackBaseUrl() != null)
                    ? casSecurityProperties.getService().getCallbackBaseUrl()
                    : casSecurityProperties.getService().getBaseUrl();
            ticketValidator.protocolVersion(casSecurityProperties.getServer().getProtocolVersion());
            String proxyCallback = casSecurityProperties.getService().getPaths().getProxyCallback();
            if (baseUrl != null && proxyCallback != null) {
                String proxyCallbackUrl = buildUrl(baseUrl, proxyCallback);
                ticketValidator.proxyCallbackUrl(proxyCallbackUrl);
            }
            if (!casSecurityProperties.getProxyValidation().isEnabled()) {
                ticketValidator.proxyChainsValidation(false);
            } else {
                List<String[]> proxyChains = casSecurityProperties
                        .getProxyValidation()
                        .getChains()
                        .stream()
                        .map(l -> l.toArray(new String[0]))
                        .collect(Collectors.toList());
                ticketValidator.proxyChains(new ProxyList(proxyChains));
            }
            ticketValidator.proxyGrantingTicketStorage(proxyGrantingTicketStorage);
        }

    }

    @ConditionalOnDefaultWebSecurity
    static class CasLoginSecurityConfiguration {

        private final CasSecurityProperties casSecurityProperties;

        public CasLoginSecurityConfiguration(CasSecurityProperties casSecurityProperties) {
            this.casSecurityProperties = casSecurityProperties;
        }

        @Bean
        @Order(CAS_AUTH_ORDER)
        SecurityFilterChain casLoginSecurityFilterChain(HttpSecurity http) throws Exception {
            String[] paths = getSecurePaths();
            if (paths.length > 0) {
                http.requestMatchers().antMatchers(paths);
                CasHttpSecurityConfigurer.cas().configure(http);

                CasSecurityProperties.SecurityAuthorizeMode mode = casSecurityProperties.getAuthorization().getMode();
                if (mode == CasSecurityProperties.SecurityAuthorizeMode.ROLE) {
                    String[] roles = casSecurityProperties.getAuthorization().getRoles();
                    http.authorizeRequests().anyRequest().hasAnyRole(roles);
                } else if (mode == CasSecurityProperties.SecurityAuthorizeMode.AUTHENTICATED) {
                    http.authorizeRequests().anyRequest().authenticated();
                } else if (mode == CasSecurityProperties.SecurityAuthorizeMode.NONE) {
                    http.authorizeRequests().anyRequest().permitAll();
                }
            }
            return http.build();
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
            // Add login, logout and proxy-callback paths in order to be handled by CasAuthenticationFilter.
            // Without authentication will be broken.
            paths.add(casSecurityProperties.getService().getPaths().getLogin());
            paths.add(casSecurityProperties.getService().getPaths().getLogout());
            paths.add(casSecurityProperties.getService().getPaths().getProxyCallback());
            // Prevent having null value that will cause IllegalArgumentException
            paths.remove(null);
            return paths.toArray(new String[0]);
        }
    }

}
