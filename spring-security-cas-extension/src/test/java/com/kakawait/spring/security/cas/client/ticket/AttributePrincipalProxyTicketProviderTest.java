package com.kakawait.spring.security.cas.client.ticket;

import com.kakawait.spring.security.cas.client.validation.AssertionProvider;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Thibaud Leprêtre
 */
@ExtendWith(MockitoExtension.class)
public class AttributePrincipalProxyTicketProviderTest {

    @Mock
    private AssertionProvider assertionProvider;

    private ProxyTicketProvider proxyTicketProvider;

    @BeforeEach
    public void setUp() {
        proxyTicketProvider = new AttributePrincipalProxyTicketProvider(assertionProvider);
    }

    @Test
    public void getProxyTicket_NullAttributePrincipal_IllegalStateException() {
        String service = "http://httpbin.org/get";

        Assertion assertion = mock(Assertion.class);
        when(assertionProvider.getAssertion()).thenReturn(assertion);
        when(assertion.getPrincipal()).thenReturn(null);

        assertThatThrownBy(() -> proxyTicketProvider.getProxyTicket(service))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Unable to provide a proxy ticket with null %s", AttributePrincipal.class.getSimpleName());

        verify(assertionProvider, times(1)).getAssertion();
        verify(assertion, times(1)).getPrincipal();
    }

    @Test
    public void getProxyTicket_ValidService_ProxyTicketValue() {
        String service = "http://httpbin.org/get";

        Assertion assertion = mock(Assertion.class);
        AttributePrincipal attributePrincipal = mock(AttributePrincipal.class);
        when(assertionProvider.getAssertion()).thenReturn(assertion);
        when(assertion.getPrincipal()).thenReturn(attributePrincipal);
        when(attributePrincipal.getProxyTicketFor(service)).thenReturn("ST-21-c1gk6jBcfYnatLbNExfx-0623277bc36a");

        assertThat(proxyTicketProvider.getProxyTicket(service)).isEqualTo("ST-21-c1gk6jBcfYnatLbNExfx-0623277bc36a");

        verify(assertionProvider, times(1)).getAssertion();
        verify(assertion, times(1)).getPrincipal();
        verify(attributePrincipal, times(1)).getProxyTicketFor(service);
    }
}
