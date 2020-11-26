package com.kakawait.spring.boot.security.cas.autoconfigure;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.jasig.cas.client.configuration.ConfigurationKey;
import org.jasig.cas.client.configuration.ConfigurationKeys;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.validation.TicketValidator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Thibaud Leprêtre
 */
@Slf4j
public class CasHttpSecurityConfigurer extends AbstractHttpConfigurer<CasHttpSecurityConfigurer, HttpSecurity> {

    private final AuthenticationManager authenticationManager;

    private CasHttpSecurityConfigurerAdapter securityConfigurerAdapter;

    private boolean isInitialized = false;

    private CasHttpSecurityConfigurer() {
        this(null);
    }

    private CasHttpSecurityConfigurer(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public static AbstractHttpConfigurer<CasHttpSecurityConfigurer, HttpSecurity> cas() {
        return new CasHttpSecurityConfigurer();
    }

    public static AbstractHttpConfigurer<CasHttpSecurityConfigurer, HttpSecurity> cas(
            AuthenticationManager authenticationManager) {
        return new CasHttpSecurityConfigurer(authenticationManager);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated use {@link #configure(HttpSecurity)} instead.
     * Will not be removed but until this issue was not treat
     * https://github.com/spring-projects/spring-security/issues/4422 I still prefer using this
     * {@link SecurityConfigurerAdapter} directly like following
     *
     * <pre>{@code
     * CasHttpSecurityConfigurer.cas().configure(http);
     * }</pre>
     * <p>
     * instead of
     *
     * <pre>{@code
     * http.apply(CasHttpSecurityConfigurera.cas());
     * }</pre>
     */
    @Override
    @Deprecated
    public void init(HttpSecurity http) throws Exception {
        if (!isInitialized) {
            ApplicationContext context = http.getSharedObject(ApplicationContext.class);
            getCasHttpSecurityConfigurerAdapter(context).init(http);
            isInitialized = true;
        }
    }

    /**
     * {@inheritDoc}
     * In addition {@link #configure(HttpSecurity)} will call {@link #init(HttpSecurity)} in order to be used without
     * {@link HttpSecurity#apply(SecurityConfigurerAdapter)} usage, related to
     * https://github.com/spring-projects/spring-security/issues/4422 issue.
     * <p>
     * Thus when using
     *
     * <pre>{@code
     * CasHttpSecurityConfigurer.cas().configure(http);
     * }</pre>
     * <p>
     * {@code configure(http)} will also call {@link CasHttpSecurityConfigurerAdapter#init(HttpSecurity)} and no need
     * to write following duplicates
     *
     * <pre>{@code
     * CasHttpSecurityConfigurer.cas().init(http);
     * CasHttpSecurityConfigurer.cas().configure(http);
     * }</pre>
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {
        init(http);
        ApplicationContext context = http.getSharedObject(ApplicationContext.class);
        getCasHttpSecurityConfigurerAdapter(context).configure(http);
    }

    private CasHttpSecurityConfigurerAdapter getCasHttpSecurityConfigurerAdapter(ApplicationContext context) {
        if (securityConfigurerAdapter == null) {
            securityConfigurerAdapter = context
                    .getAutowireCapableBeanFactory()
                    .createBean(CasHttpSecurityConfigurerAdapter.class);
        }
        if (authenticationManager != null) {
            securityConfigurerAdapter.setAuthenticationManager(authenticationManager);
        }
        return securityConfigurerAdapter;
    }

    static class CasHttpSecurityConfigurerAdapter
            extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> implements InitializingBean {

        private final CasAuthenticationFilterConfigurer filterConfigurer = new CasAuthenticationFilterConfigurer();

        private final CasSingleSignOutFilterConfigurer singleSignOutFilterConfigurer =
                new CasSingleSignOutFilterConfigurer();

        private final CasAuthenticationProviderSecurityBuilder providerBuilder =
                new CasAuthenticationProviderSecurityBuilder();

        private final List<CasSecurityConfigurer> configurers;

        private final CasSecurityProperties casSecurityProperties;

        private final CasAuthenticationEntryPoint authenticationEntryPoint;

        private final ServiceProperties serviceProperties;

        private final TicketValidator ticketValidator;

        private final AuthenticationManagerBuilder authenticationManagerBuilder;

        private AuthenticationManager authenticationManager;

        private boolean authenticationManagerInitialized;

        public CasHttpSecurityConfigurerAdapter(List<CasSecurityConfigurer> configurers,
                CasSecurityProperties casSecurityProperties, CasAuthenticationEntryPoint authenticationEntryPoint,
                ServiceProperties serviceProperties, TicketValidator ticketValidator,
                ObjectPostProcessor<Object> objectPostProcessor) {
            this.configurers = configurers;
            this.casSecurityProperties = casSecurityProperties;
            this.authenticationEntryPoint = authenticationEntryPoint;
            this.serviceProperties = serviceProperties;
            this.ticketValidator = ticketValidator;
            authenticationManagerBuilder = new AuthenticationManagerBuilder(objectPostProcessor);
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            configurers.forEach(c -> {
                c.configure(filterConfigurer);
                c.configure(singleSignOutFilterConfigurer);
                c.configure(providerBuilder);
            });
        }

        @Override
        public void init(HttpSecurity http) throws Exception {
            CasAuthenticationFilter filter = new CasAuthenticationFilter();
            filter.setAuthenticationManager(authenticationManager());
            filter.setRequiresAuthenticationRequestMatcher(getAuthenticationRequestMatcher());
            filter.setServiceProperties(serviceProperties);
            filterConfigurer.configure(filter);

            SingleSignOutFilter singleSignOutFilter = new SingleSignOutFilter();
            try {
                ConfigurationKey<String> key = ConfigurationKeys.CAS_SERVER_URL_PREFIX;
                Method setter = singleSignOutFilter.getClass()
                        .getDeclaredMethod("set" + StringUtils.capitalize(key.getName()));
                setter.invoke(singleSignOutFilter, casSecurityProperties.getServer().getBaseUrl().toASCIIString());
            } catch (NoSuchMethodException | SecurityException e) {
                // since commit :
                // https://github.com/apereo/java-cas-client/commit/fdc948b8ec697be0ae04da2f91c66c6526d463b5#diff-676b9d196aacd4b54bc978c62ccbacd8d29552fcd694061355bd930405560fb5
                // setCasServerUrlPrefix(getString(ConfigurationKeys.CAS_SERVER_URL_PREFIX)); does NOT exists anymore
                logger.info(
                        "Since apereo CAS client 3.6.0 setCasServerUrlPrefix(getString(ConfigurationKeys.CAS_SERVER_URL_PREFIX)); does NOT exists anymore");
            }
            singleSignOutFilterConfigurer.configure(singleSignOutFilter);

            http.exceptionHandling().authenticationEntryPoint(authenticationEntryPoint)
                .and()
                .addFilterBefore(singleSignOutFilter, CsrfFilter.class)
                .addFilter(filter);
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            for (CasSecurityConfigurer configurer : configurers) {
                configurer.configure(http);
            }
        }

        void configure(AuthenticationManagerBuilder auth) throws Exception {
            CasAuthenticationProvider provider = providerBuilder.build();
            provider.setServiceProperties(serviceProperties);
            Field field = ReflectionUtils.findField(CasAuthenticationProvider.class, "ticketValidator");
            ReflectionUtils.makeAccessible(field);
            if (ReflectionUtils.getField(field, provider) == null) {
                provider.setTicketValidator(ticketValidator);
            }
            provider.afterPropertiesSet();
            auth.authenticationProvider(provider);
        }

        AuthenticationManager authenticationManager() throws Exception {
            if (!authenticationManagerInitialized) {
                configure(authenticationManagerBuilder);
                for (CasSecurityConfigurer configurer : configurers) {
                    configurer.configure(authenticationManagerBuilder);
                }
                authenticationManager = authenticationManagerBuilder.build();
                authenticationManagerInitialized = true;
            }
            return authenticationManager;
        }

        void setAuthenticationManager(@NonNull AuthenticationManager authenticationManager) {
            authenticationManagerInitialized = true;
            this.authenticationManager = authenticationManager;
        }

        private RequestMatcher getAuthenticationRequestMatcher() {
            return new AntPathRequestMatcher(casSecurityProperties.getService().getPaths().getLogin());
        }
    }

}
