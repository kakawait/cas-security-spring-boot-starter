package com.kakawait.spring.boot.security.cas;


import com.kakawait.spring.security.cas.client.AuthenticatedPrincipal;
import com.kakawait.spring.security.cas.client.Cas;
import com.kakawait.spring.security.cas.client.CasClientProperties;
import com.kakawait.spring.security.cas.client.CasInterceptor;
import com.kakawait.spring.security.cas.client.CasRequestService;
import com.kakawait.spring.security.cas.client.CasRequestServiceImpl;
import com.kakawait.spring.security.cas.client.CasRequestSpecification;
import com.kakawait.spring.security.cas.client.JasigProxyTicketService;
import com.kakawait.spring.security.cas.client.ProxyTicketService;
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
    public ProxyTicketService proxyTicketService() {
        return new JasigProxyTicketService();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "cas.client")
    public CasClientProperties casClientProperties() {
        return new CasClientProperties();
    }


    @Bean
    @ConditionalOnMissingBean
    public CasRequestService casRequestService(ProxyTicketService proxyTicketService,
            CasClientProperties casClientProperties) {
        return new CasRequestServiceImpl(proxyTicketService, casClientProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public CasRequestSpecification casRequestSpecification() {
        return new CasRequestSpecification();
    }

    @Bean
    public CasInterceptor casInterceptor(AuthenticatedPrincipal authenticatedPrincipal,
            CasRequestService casRequestService,
            CasRequestSpecification casRequestSpecification) {
        return new CasInterceptor(authenticatedPrincipal, casRequestService, casRequestSpecification);
    }

    @Bean
    @ConditionalOnMissingBean
    public RestTemplateCustomizer restTemplateCustomizer(final CasInterceptor casInterceptor) {
        return restTemplate -> {
            List<ClientHttpRequestInterceptor> list = new ArrayList<>(
                    restTemplate.getInterceptors());
            list.add(casInterceptor);
            restTemplate.setInterceptors(list);
        };
    }
}
