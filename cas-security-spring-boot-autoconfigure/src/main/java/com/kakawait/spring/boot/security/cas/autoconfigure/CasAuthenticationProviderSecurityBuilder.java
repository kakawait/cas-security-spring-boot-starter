package com.kakawait.spring.boot.security.cas.autoconfigure;

import com.kakawait.spring.security.cas.authentication.DynamicProxyCallbackUrlCasAuthenticationProvider;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jasig.cas.client.validation.TicketValidator;
import org.springframework.context.MessageSource;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.authentication.StatelessTicketCache;
import org.springframework.security.config.annotation.SecurityBuilder;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;

/**
 * @author Thibaud Lepretre
 */
@Accessors(fluent = true)
@Setter
public class CasAuthenticationProviderSecurityBuilder implements SecurityBuilder<CasAuthenticationProvider> {

    private CasSecurityProperties.ServiceResolutionMode serviceResolutionMode;

    private AuthenticationUserDetailsService<CasAssertionAuthenticationToken> authenticationUserDetailsService;

    private GrantedAuthoritiesMapper grantedAuthoritiesMapper;

    private MessageSource messageSource;

    private StatelessTicketCache statelessTicketCache;

    private TicketValidator ticketValidator;

    private String key;

    @Override
    public CasAuthenticationProvider build() {
        CasAuthenticationProvider provider;
        if (serviceResolutionMode == CasSecurityProperties.ServiceResolutionMode.DYNAMIC) {
            provider = new DynamicProxyCallbackUrlCasAuthenticationProvider();
        } else {
            provider = new CasAuthenticationProvider();
        }
        provider.setAuthenticationUserDetailsService(authenticationUserDetailsService);
        provider.setKey(key);
        provider.setTicketValidator(ticketValidator);
        if (messageSource != null) {
            provider.setMessageSource(messageSource);
        }
        if (statelessTicketCache != null) {
            provider.setStatelessTicketCache(statelessTicketCache);
        }
        if (grantedAuthoritiesMapper != null) {
            provider.setAuthoritiesMapper(grantedAuthoritiesMapper);
        }
        return provider;
    }
}
