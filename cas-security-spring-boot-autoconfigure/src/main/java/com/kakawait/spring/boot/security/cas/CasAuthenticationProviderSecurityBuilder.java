package com.kakawait.spring.boot.security.cas;

import com.kakawait.security.cas.DynamicProxyCallbackUrlCasAuthenticationProvider;
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
 * @author Thibaud Leprêtre
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
    public CasAuthenticationProvider build() throws Exception {
        CasAuthenticationProvider provider;
        switch (serviceResolutionMode) {
            case DYNAMIC:
                provider = new DynamicProxyCallbackUrlCasAuthenticationProvider();
                break;
            default:
                provider = new CasAuthenticationProvider();
                break;
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
