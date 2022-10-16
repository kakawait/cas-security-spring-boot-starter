package com.kakawait.spring.security.cas.authentication;

import com.kakawait.spring.security.cas.web.authentication.ProxyCallbackAndServiceAuthenticationDetails;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.jasig.cas.client.validation.TicketValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.cas.web.authentication.ServiceAuthenticationDetails;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Thibaud Lepretre
 */
@ExtendWith(MockitoExtension.class)
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
        verifyNoInteractions(ticketValidator);
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
        verifyNoInteractions(ticketValidator);
    }

    @Test
    public void authenticate_WithNullTicketValidator_DoNothing() {
        DynamicProxyCallbackUrlCasAuthenticationProvider authenticationProvider =
                new DynamicProxyCallbackUrlCasAuthenticationProvider();
        authenticationProvider.setTicketValidator(null);

        when(authentication.getDetails()).thenReturn(new ProxyCallbackAndServiceAuthenticationDetails() {
            private static final long serialVersionUID = 2171667909966987793L;

            @Override
            public String getServiceUrl() {
                return "http://localhost";
            }

            @Override
            public String getProxyCallbackUrl() {
                return "http://localhost/cas/callback";
            }

            @Override
            public void setContext(HttpServletRequest context) {
                // do nothing
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
            private static final long serialVersionUID = 700055871632490815L;

            @Override
            public String getServiceUrl() {
                return "http://localhost";
            }

            @Override
            public String getProxyCallbackUrl() {
                return "http://localhost/cas/callback";
            }

            @Override
            public void setContext(HttpServletRequest context) {
                // do nothing
            }
        });

        authenticationProvider.authenticate(authentication);

        verify(authentication, times(1)).getDetails();
        verifyNoInteractions(ticketValidator);
    }

    @Test
    public void authenticate_EveryRequirements_TicketValidatorProxyCallbackUrlUpdated() {
        Cas20ServiceTicketValidator ticketValidator = mock(Cas20ServiceTicketValidator.class);

        DynamicProxyCallbackUrlCasAuthenticationProvider authenticationProvider =
                new DynamicProxyCallbackUrlCasAuthenticationProvider();
        authenticationProvider.setTicketValidator(ticketValidator);

        when(authentication.getDetails()).thenReturn(new ProxyCallbackAndServiceAuthenticationDetails() {
            private static final long serialVersionUID = -541835714542292545L;

            @Override
            public String getServiceUrl() {
                return "http://localhost";
            }

            @Override
            public String getProxyCallbackUrl() {
                return "http://localhost/cas/callback";
            }

            @Override
            public void setContext(HttpServletRequest context) {
                // do nothing
            }
        });
        doNothing().when(ticketValidator).setProxyCallbackUrl(anyString());

        authenticationProvider.authenticate(authentication);

        verify(authentication, times(2)).getDetails();
        verify(ticketValidator, times(1)).setProxyCallbackUrl(eq("http://localhost/cas/callback"));
    }
}
