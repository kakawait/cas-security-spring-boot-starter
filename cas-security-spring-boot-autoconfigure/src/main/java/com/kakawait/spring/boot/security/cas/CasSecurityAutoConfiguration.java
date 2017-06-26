package com.kakawait.spring.boot.security.cas;

import com.kakawait.security.cas.LaxServiceProperties;
import com.kakawait.security.cas.ProxyCallbackAndServiceAuthenticationDetailsSource;
import com.kakawait.security.cas.RequestAwareCasAuthenticationEntryPoint;
import lombok.Getter;
import lombok.NonNull;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorageImpl;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.validation.TicketValidator;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.SecurityAuthorizeMode;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.userdetails.AbstractCasAssertionUserDetailsService;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.cas.web.authentication.ServiceAuthenticationDetailsSource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

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

    static class DefaultCasSecurityConfigurerAdapter extends CasSecurityConfigurerAdapter {

        private final CasSecurityProperties casSecurityProperties;

        private final AbstractCasAssertionUserDetailsService userDetailsService;

        private final TicketValidator ticketValidator;

        private final ServiceAuthenticationDetailsSource authenticationDetailsSource;

        public DefaultCasSecurityConfigurerAdapter(CasSecurityProperties casSecurityProperties,
                AbstractCasAssertionUserDetailsService userDetailsService, TicketValidator ticketValidator,
                ServiceAuthenticationDetailsSource authenticationDetailsSource) {
            this.casSecurityProperties = casSecurityProperties;
            this.userDetailsService = userDetailsService;
            this.ticketValidator = ticketValidator;
            this.authenticationDetailsSource = authenticationDetailsSource;
        }

        @Override
        public void configure(CasAuthenticationProviderSecurityBuilder provider) {
            provider.serviceResolutionMode(casSecurityProperties.getService().getResolutionMode())
                    .authenticationUserDetailsService(userDetailsService)
                    .key(casSecurityProperties.getKey())
                    .ticketValidator(ticketValidator);
        }

        @Override
        public void configure(CasAuthenticationFilterConfigurer filter) {
            filter.proxyReceptorUrl(casSecurityProperties.getService().getPaths().getProxyCallback())
                  .serviceAuthenticationDetailsSource(authenticationDetailsSource)
                  .proxyGrantingTicketStorage(new ProxyGrantingTicketStorageImpl());
        }
    }

    @Order(SecurityProperties.BASIC_AUTH_ORDER - 1)
    static class CasLoginSecurityConfiguration extends WebSecurityConfigurerAdapter {

        private final CasAuthenticationFilterConfigurer filterConfigurer = new CasAuthenticationFilterConfigurer();

        private final CasSingleSignOutFilterConfigurer singleSignOutFilterConfigurer =
                new CasSingleSignOutFilterConfigurer();

        private final CasAuthenticationProviderSecurityBuilder providerBuilder =
                new CasAuthenticationProviderSecurityBuilder();

        private final List<CasSecurityConfigurer> configurers;

        private final SecurityProperties securityProperties;

        private final CasSecurityProperties casSecurityProperties;

        private final CasAuthenticationEntryPoint authenticationEntryPoint;

        private final ServiceProperties serviceProperties;

        private final AuthenticationManager authenticationManager;

        private final CasSecurityProperties.Service.Paths paths;

        public CasLoginSecurityConfiguration(List<CasSecurityConfigurer> configurers,
                SecurityProperties securityProperties, CasSecurityProperties casSecurityProperties,
                CasAuthenticationEntryPoint authenticationEntryPoint, ServiceProperties serviceProperties,
                AuthenticationManager authenticationManager) {
            this.configurers = configurers;
            this.securityProperties = securityProperties;
            this.casSecurityProperties = casSecurityProperties;
            this.authenticationEntryPoint = authenticationEntryPoint;
            this.serviceProperties = serviceProperties;
            this.authenticationManager = authenticationManager;
            paths = casSecurityProperties.getService().getPaths();
        }

        @PostConstruct
        private void init() {
            configurers.forEach(c -> {
                c.configure(filterConfigurer);
                c.configure(singleSignOutFilterConfigurer);
                c.configure(providerBuilder);
            });
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            CasAuthenticationFilter filter = new CasAuthenticationFilter();
            filterConfigurer.configure(filter);
            filter.setAuthenticationManager(authenticationManager());
            filter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher(paths.getLogin()));

            SingleSignOutFilter singleSignOutFilter = new SingleSignOutFilter();
            singleSignOutFilterConfigurer.configure(singleSignOutFilter);

            String[] paths = getSecurePaths();
            if (paths.length > 0) {
                http.authorizeRequests().antMatchers(paths).authenticated()
                    .and()
                    .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint)
                    .and()
                    .addFilterBefore(singleSignOutFilter, CsrfFilter.class)
                    .addFilter(filter);
                if (securityProperties.getBasic().isEnabled()) {
                    BasicAuthenticationFilter basicAuthFilter = new BasicAuthenticationFilter(authenticationManager);
                    http.addFilterBefore(basicAuthFilter, CasAuthenticationFilter.class);
                }
                SecurityAuthorizeMode mode = casSecurityProperties.getAuthorizeMode();
                if (mode == SecurityAuthorizeMode.ROLE) {
                    List<String> roles = securityProperties.getUser().getRole();
                    http.authorizeRequests().anyRequest().hasAnyRole(roles.toArray(new String[roles.size()]));
                } else if (mode == SecurityAuthorizeMode.AUTHENTICATED) {
                    http.authorizeRequests().anyRequest().authenticated();
                }
            }
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            CasAuthenticationProvider provider = providerBuilder.build();
            provider.setServiceProperties(serviceProperties);
            auth.authenticationProvider(provider);
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
