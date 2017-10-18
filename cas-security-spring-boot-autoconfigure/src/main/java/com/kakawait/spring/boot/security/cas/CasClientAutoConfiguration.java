package com.kakawait.spring.boot.security.cas;


import com.kakawait.spring.security.cas.client.*;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    public CasRequestFactory casRequestFactory(CasClientProperties casClientProperties) {
        return new CasRequestFactory(casClientProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public CasStatelessService casStatelessService(ProxyTicketRepository proxyTicketRepository,
            CasRequestFactory casRequestFactory) {
        return new CasStatelessServiceImpl(proxyTicketRepository, casRequestFactory);
    }

    @Bean
    public CasStatelessInterceptor casInterceptor(AuthenticatedPrincipal authenticatedPrincipal,
            CasStatelessService casStatelessService) {
        return new CasStatelessInterceptor(authenticatedPrincipal, casStatelessService);
    }

    @Bean
    @ConditionalOnMissingBean
    public RestTemplateCustomizer restTemplateCustomizer(final CasStatelessInterceptor casInterceptor) {
        return restTemplate -> {
            List<ClientHttpRequestInterceptor> list = new ArrayList<>(
                    restTemplate.getInterceptors());
            list.add(casInterceptor);
            restTemplate.setInterceptors(list);
        };
    }
}
