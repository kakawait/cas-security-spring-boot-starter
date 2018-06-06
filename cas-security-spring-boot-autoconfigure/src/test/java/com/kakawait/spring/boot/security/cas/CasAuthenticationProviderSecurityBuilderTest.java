package com.kakawait.spring.boot.security.cas;

import com.kakawait.spring.security.cas.authentication.DynamicProxyCallbackUrlCasAuthenticationProvider;
import org.jasig.cas.client.validation.TicketValidator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.authentication.StatelessTicketCache;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;

import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Thibaud Leprêtre
 */
public class CasAuthenticationProviderSecurityBuilderTest {

    private CasAuthenticationProviderSecurityBuilder builder;

    @Before
    public void setUp() {
        builder = new CasAuthenticationProviderSecurityBuilder();
    }

    @Test
    public void build_Default_ResolutionModeStatic() {
        assertThat(builder.build()).isExactlyInstanceOf(CasAuthenticationProvider.class);
    }

    @Test
    public void build_WithDynamicResolutionMode_InstanceOfDynamicProxyCallbackUrlCasAuthenticationProvider() {
        builder.serviceResolutionMode(CasSecurityProperties.ServiceResolutionMode.DYNAMIC);

        assertThat(builder.build()).isExactlyInstanceOf(DynamicProxyCallbackUrlCasAuthenticationProvider.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void build_AnyParameters_InjectInsideCasAuthenticationProvider() {
        String key = "key";
        AuthenticationUserDetailsService userDetailsService = Mockito.mock(AuthenticationUserDetailsService.class);
        GrantedAuthoritiesMapper grantedAuthoritiesMapper = Mockito.mock(GrantedAuthoritiesMapper.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);
        StatelessTicketCache statelessTicketCache = Mockito.mock(StatelessTicketCache.class);
        TicketValidator ticketValidator = Mockito.mock(TicketValidator.class);

        builder.key(key)
               .authenticationUserDetailsService(userDetailsService)
               .grantedAuthoritiesMapper(grantedAuthoritiesMapper)
               .messageSource(messageSource)
               .statelessTicketCache(statelessTicketCache)
               .ticketValidator(ticketValidator);

        assertThat(builder.build())
                .extracting("key", "authenticationUserDetailsService", "authoritiesMapper", "statelessTicketCache",
                        "ticketValidator")
                .usingElementComparator((Comparator<Object>) (o1, o2) -> (o1 == o2) ? 0 : -1)
                .containsOnly(key, userDetailsService, grantedAuthoritiesMapper, statelessTicketCache, ticketValidator);

        assertThat(builder.build())
                .extracting("messages")
                .extracting("messageSource")
                .usingElementComparator((Comparator<Object>) (o1, o2) -> (o1 == o2) ? 0 : -1)
                .containsOnly(messageSource);
    }
}
