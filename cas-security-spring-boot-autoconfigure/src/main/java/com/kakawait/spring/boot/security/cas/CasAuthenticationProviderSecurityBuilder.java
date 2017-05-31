package com.kakawait.spring.boot.security.cas;

import com.kakawait.security.cas.DynamicProxyCallbackUrlCasAuthenticationProvider;
import lombok.NonNull;
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

    @NonNull
    private CasSecurityProperties.ServiceResolutionMode serviceResolutionMode;

    @NonNull
    private AuthenticationUserDetailsService<CasAssertionAuthenticationToken> authenticationUserDetailsService;

    @NonNull
    private GrantedAuthoritiesMapper grantedAuthoritiesMapper;

    @NonNull
    private MessageSource messageSource;

    @NonNull
    private StatelessTicketCache statelessTicketCache;

    @NonNull
    private TicketValidator ticketValidator;

    @NonNull
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
        provider.afterPropertiesSet();
        return provider;
    }
}
