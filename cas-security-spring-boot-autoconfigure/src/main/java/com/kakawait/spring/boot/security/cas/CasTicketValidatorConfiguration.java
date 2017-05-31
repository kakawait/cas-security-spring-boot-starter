package com.kakawait.spring.boot.security.cas;

import org.jasig.cas.client.validation.Cas10TicketValidator;
import org.jasig.cas.client.validation.Cas20ProxyTicketValidator;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.jasig.cas.client.validation.Cas30ProxyTicketValidator;
import org.jasig.cas.client.validation.TicketValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.cas.ServiceProperties;

import java.net.URI;

/**
 * @author Thibaud Leprêtre
 */
@ConditionalOnMissingBean(TicketValidator.class)
public class CasTicketValidatorConfiguration {

    private final CasSecurityProperties casSecurityProperties;

    public CasTicketValidatorConfiguration(CasSecurityProperties casSecurityProperties) {
        this.casSecurityProperties = casSecurityProperties;
    }

    @Bean
    @ConditionalOnProperty(value = "security.cas.server.protocol-version", havingValue = "3", matchIfMissing = true)
    TicketValidator cas30ProxyTicketValidator() {
        Cas20ServiceTicketValidator ticketValidator = new Cas30ProxyTicketValidator(
                casSecurityProperties.getServer().getBaseUrl().toASCIIString());
        ticketValidator.setProxyCallbackUrl(casSecurityProperties.getService().getPaths().getProxyCallback());
        return ticketValidator;
    }

    @Bean
    @ConditionalOnProperty(value = "security.cas.server.protocol-version", havingValue = "2")
    TicketValidator cas20ProxyTicketValidator() {
        Cas20ServiceTicketValidator ticketValidator = new Cas20ProxyTicketValidator(
                casSecurityProperties.getServer().getBaseUrl().toASCIIString());
        ticketValidator.setProxyCallbackUrl(casSecurityProperties.getService().getPaths().getProxyCallback());
        return ticketValidator;
    }

    @Bean
    @ConditionalOnProperty(value = "security.cas.server.protocol-version", havingValue = "1")
    TicketValidator Cas10TicketValidator() {
        return new Cas10TicketValidator(casSecurityProperties.getServer().getBaseUrl().toASCIIString());
    }
}
