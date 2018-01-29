package com.kakawait.spring.boot.security.cas;


import com.kakawait.spring.security.cas.client.*;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Jonathan Coueraud
 */
@ConditionalOnClass(RestTemplate.class)
@Configuration
@Import(CasClientAutoConfiguration.StatefulConfiguration.class)
public class CasClientAutoConfiguration {

    @Cas
    @Autowired(required = false)
    private List<RestTemplate> restTemplates = Collections.emptyList();

    @Bean
    public SmartInitializingSingleton casRestTemplateInitializer(final List<RestTemplateCustomizer> customizers) {
        return () -> {
            for (RestTemplate restTemplate : CasClientAutoConfiguration.this.restTemplates) {
                for (RestTemplateCustomizer customizer : customizers) {
                    customizer.customize(restTemplate);
                }
            }
        };
    }

    @Bean
    @Qualifier("casClientHttpRequestInterceptor")
    public RestTemplateCustomizer restTemplateCustomizer(final List<ClientHttpRequestInterceptor> casInterceptors) {
        return restTemplate -> {
            List<ClientHttpRequestInterceptor> list = new ArrayList<>(
                    restTemplate.getInterceptors());
            list.addAll(casInterceptors);
            restTemplate.setInterceptors(list);
        };
    }

    @Configuration
    public static class StatelessConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public AuthenticatedPrincipal authenticatedPrincipal() {
            return () -> (Principal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }

        @Bean
        @ConditionalOnMissingBean
        public ProxyTicketRepository proxyTicketService() {
            return new JasigProxyTicketRepository();
        }

        @Bean
        @ConditionalOnMissingBean
        @ConfigurationProperties(prefix = "cas.client")
        public CasClientProperties casClientProperties() {
            return new CasClientProperties();
        }

        @Bean
        @ConditionalOnMissingBean
        public RequestWithProxyTicketFactory casRequestFactory(CasClientProperties casClientProperties) {
            return new RequestWithProxyTicketFactory(casClientProperties);
        }

        @Bean
        @ConditionalOnMissingBean
        public CasStatelessService casStatelessService(ProxyTicketRepository proxyTicketRepository,
                RequestWithProxyTicketFactory requestWithProxyTicketFactory) {
            return new CasStatelessServiceImpl(proxyTicketRepository, requestWithProxyTicketFactory);
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnProperty(value = "cas.client.stateful", havingValue = "false", matchIfMissing = true)
        public CasStatelessInterceptor casStatelessInterceptor(AuthenticatedPrincipal authenticatedPrincipal,
                CasStatelessService casStatelessService) {
            return new CasStatelessInterceptor(authenticatedPrincipal, casStatelessService);
        }
    }

    @Configuration
    @ConditionalOnProperty(value = "cas.client.stateful")
    @Import(StatelessConfiguration.class)
    public static class StatefulConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public HttpContextRepository contextRepository() {
            return new InMemoryHttpContextRepository();
        }

        @Bean
        @ConditionalOnMissingBean
        public RequestWithCookieFactory requestWithCookieFactory() {
            return new RequestWithCookieFactoryImpl();
        }

        @Bean
        @ConditionalOnMissingBean
        public CookieFactory cookieFactory() {
            return new HttpCookieFactory();
        }

        @Bean
        @ConditionalOnMissingBean
        public CasStatefulService casStatefulService(CasStatelessService casStatelessService,
                HttpContextRepository contextRepository, RequestWithCookieFactory requestWithCookieFactory, CookieFactory cookieFactory) {
            return new CasStatefulServiceImpl(casStatelessService, contextRepository, requestWithCookieFactory, cookieFactory);
        }

        @Bean
        @ConditionalOnMissingBean
        public CasStatefulInterceptor casStatefulInterceptor(AuthenticatedPrincipal authenticatedPrincipal, CasStatefulService casStatefulService) {
            return new CasStatefulInterceptor(casStatefulService, authenticatedPrincipal);
        }
    }
}
