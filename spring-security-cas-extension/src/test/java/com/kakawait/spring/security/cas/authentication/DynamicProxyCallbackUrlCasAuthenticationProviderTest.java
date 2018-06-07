package com.kakawait.spring.security.cas.authentication;

import com.kakawait.spring.security.cas.web.authentication.ProxyCallbackAndServiceAuthenticationDetails;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.jasig.cas.client.validation.TicketValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.cas.web.authentication.ServiceAuthenticationDetails;
import org.springframework.security.core.Authentication;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Thibaud Leprêtre
 */
@RunWith(MockitoJUnitRunner.class)
public class DynamicProxyCallbackUrlCasAuthenticationProviderTest {

    @Mock
    private Authentication authentication;

    @Test
    public void authenticate_WithNullDetails_DoNothing() {
        TicketValidator ticketValidator = mock(TicketValidator.class);

        DynamicProxyCallbackUrlCasAuthenticationProvider authenticationProvider =
                new DynamicProxyCallbackUrlCasAuthenticationProvider();
        authenticationProvider.setTicketValidator(ticketValidator);

        when(authentication.getDetails()).thenReturn(null);

        authenticationProvider.authenticate(authentication);

        verify(authentication, times(1)).getDetails();
        verifyZeroInteractions(ticketValidator);
    }

    @Test
    public void authenticate_WithDetailsNotInstanceOfProxyCallbackAndServiceAuthenticationDetails_DoNothing() {
        TicketValidator ticketValidator = mock(TicketValidator.class);

        DynamicProxyCallbackUrlCasAuthenticationProvider authenticationProvider =
                new DynamicProxyCallbackUrlCasAuthenticationProvider();
        authenticationProvider.setTicketValidator(ticketValidator);

        when(authentication.getDetails()).thenReturn((ServiceAuthenticationDetails) () -> "http://localhost");

        authenticationProvider.authenticate(authentication);

        verify(authentication, times(1)).getDetails();
        verifyZeroInteractions(ticketValidator);
    }

    @Test
    public void authenticate_WithNullTicketValidator_DoNothing() {
        DynamicProxyCallbackUrlCasAuthenticationProvider authenticationProvider =
                new DynamicProxyCallbackUrlCasAuthenticationProvider();
        authenticationProvider.setTicketValidator(null);

        when(authentication.getDetails()).thenReturn(new ProxyCallbackAndServiceAuthenticationDetails() {
            @Override
            public String getServiceUrl() {
                return "http://localhost";
            }

            @Override
            public String getProxyCallbackUrl() {
                return "http://localhost/cas/callback";
            }
        });

        authenticationProvider.authenticate(authentication);

        verify(authentication, times(1)).getDetails();
    }

    @Test
    public void authenticate_WithTicketValidatorNotInstanceOfCas20ServiceTicketValidator_DoNothing() {
        TicketValidator ticketValidator = mock(TicketValidator.class);

        DynamicProxyCallbackUrlCasAuthenticationProvider authenticationProvider =
                new DynamicProxyCallbackUrlCasAuthenticationProvider();
        authenticationProvider.setTicketValidator(ticketValidator);

        when(authentication.getDetails()).thenReturn(new ProxyCallbackAndServiceAuthenticationDetails() {
            @Override
            public String getServiceUrl() {
                return "http://localhost";
            }

            @Override
            public String getProxyCallbackUrl() {
                return "http://localhost/cas/callback";
            }
        });

        authenticationProvider.authenticate(authentication);

        verify(authentication, times(1)).getDetails();
        verifyZeroInteractions(ticketValidator);
    }

    @Test
    public void authenticate_EveryRequirements_TicketValidatorProxyCallbackUrlUpdated() {
        Cas20ServiceTicketValidator ticketValidator = mock(Cas20ServiceTicketValidator.class);

        DynamicProxyCallbackUrlCasAuthenticationProvider authenticationProvider =
                new DynamicProxyCallbackUrlCasAuthenticationProvider();
        authenticationProvider.setTicketValidator(ticketValidator);

        when(authentication.getDetails()).thenReturn(new ProxyCallbackAndServiceAuthenticationDetails() {
            @Override
            public String getServiceUrl() {
                return "http://localhost";
            }

            @Override
            public String getProxyCallbackUrl() {
                return "http://localhost/cas/callback";
            }
        });
        doNothing().when(ticketValidator).setProxyCallbackUrl(anyString());

        authenticationProvider.authenticate(authentication);

        verify(authentication, times(2)).getDetails();
        verify(ticketValidator, times(1)).setProxyCallbackUrl(eq("http://localhost/cas/callback"));
    }
}
