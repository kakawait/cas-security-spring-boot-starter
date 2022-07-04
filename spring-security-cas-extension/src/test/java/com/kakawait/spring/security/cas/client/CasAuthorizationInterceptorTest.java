package com.kakawait.spring.security.cas.client;

import com.kakawait.spring.security.cas.client.ticket.ProxyTicketProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.security.cas.ServiceProperties;

import java.io.IOException;
import java.net.URI;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Thibaud Leprêtre
 */
@ExtendWith(MockitoExtension.class)
public class CasAuthorizationInterceptorTest {

    @Mock
    private ProxyTicketProvider proxyTicketProvider;

    private ClientHttpRequestInterceptor casAuthorizationInterceptor;

    @Captor
    private ArgumentCaptor<HttpRequest> httpRequestArgumentCaptor;

    @BeforeEach
    public void setUp() {
        ServiceProperties serviceProperties = new ServiceProperties();
        serviceProperties.setService("http://localhost:8080/");
        casAuthorizationInterceptor = new CasAuthorizationInterceptor(serviceProperties, proxyTicketProvider);
    }

    @Test
    public void intercept_NullProxyTicket_IllegalStateException() throws IOException {
        String service = "http://httpbin.org/get";

        ClientHttpRequestExecution clientHttpRequestExecution = mock(ClientHttpRequestExecution.class);
        ClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create(service));

        when(proxyTicketProvider.getProxyTicket(service)).thenReturn(null);

        assertThatThrownBy(() -> casAuthorizationInterceptor.intercept(request, null, clientHttpRequestExecution))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(format("Proxy ticket provider returned a null proxy ticket for service %s.", service));

        verify(proxyTicketProvider, times(1)).getProxyTicket(service);
        verify(clientHttpRequestExecution, never()).execute(request, null);
    }

    @Test
    public void intercept_BasicService_ProxyTicketAsQueryParameter() throws IOException {
        String service = "http://httpbin.org/get";

        ClientHttpRequestExecution clientHttpRequestExecution = mock(ClientHttpRequestExecution.class);
        ClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create(service));

        String proxyTicket = "PT-21-c1gk6jBcfYnatLbNExfx-0623277bc36a";
        when(proxyTicketProvider.getProxyTicket(service)).thenReturn(proxyTicket);

        casAuthorizationInterceptor.intercept(request, null, clientHttpRequestExecution);

        verify(proxyTicketProvider, times(1)).getProxyTicket(service);
        verify(clientHttpRequestExecution, times(1)).execute(httpRequestArgumentCaptor.capture(), isNull());

        assertThat(httpRequestArgumentCaptor.getValue().getURI().toASCIIString())
                .isEqualTo(service + "?ticket=" + proxyTicket);
    }

    @Test
    public void intercept_ServiceWithExistingQueryParameters_ProxyTicketAsQueryParameter() throws IOException {
        String service = "http://httpbin.org/get?a=test&x=test2";

        ClientHttpRequestExecution clientHttpRequestExecution = mock(ClientHttpRequestExecution.class);
        ClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create(service));

        String proxyTicket = "PT-21-c1gk6jBcfYnatLbNExfx-0623277bc36a";
        when(proxyTicketProvider.getProxyTicket(service)).thenReturn(proxyTicket);

        casAuthorizationInterceptor.intercept(request, null, clientHttpRequestExecution);

        verify(proxyTicketProvider, times(1)).getProxyTicket(service);
        verify(clientHttpRequestExecution, times(1)).execute(httpRequestArgumentCaptor.capture(), isNull());

        assertThat(httpRequestArgumentCaptor.getValue().getURI().toASCIIString())
                .isEqualTo(service + "&ticket=" + proxyTicket);
    }

    @Test
    public void intercept_ServiceWithExistingQueryParameters_ProxyTicketAsQueryEscapedParameter() throws IOException {
        String service = "http://httpbin.org/get?a=test&x=test2&c=%22test3%22";

        ClientHttpRequestExecution clientHttpRequestExecution = mock(ClientHttpRequestExecution.class);
        ClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create(service));

        String proxyTicket = "PT-21-c1gk6jBcfYnatLbNExfx-0623277bc36a";
        when(proxyTicketProvider.getProxyTicket(service)).thenReturn(proxyTicket);

        casAuthorizationInterceptor.intercept(request, null, clientHttpRequestExecution);

        verify(proxyTicketProvider, times(1)).getProxyTicket(service);
        verify(clientHttpRequestExecution, times(1)).execute(httpRequestArgumentCaptor.capture(), isNull());

        assertThat(httpRequestArgumentCaptor.getValue().getURI().toASCIIString())
                .isEqualTo(service + "&ticket=" + proxyTicket);
    }

    @Test
    public void intercept_ServiceWithConflictQueryParameter_QueryParameterOverride() throws IOException {
        String service = "http://httpbin.org/get?ticket=bob";

        ClientHttpRequestExecution clientHttpRequestExecution = mock(ClientHttpRequestExecution.class);
        ClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create(service));

        String proxyTicket = "PT-21-c1gk6jBcfYnatLbNExfx-0623277bc36a";
        when(proxyTicketProvider.getProxyTicket(service)).thenReturn(proxyTicket);

        casAuthorizationInterceptor.intercept(request, null, clientHttpRequestExecution);

        verify(proxyTicketProvider, times(1)).getProxyTicket(service);
        verify(clientHttpRequestExecution, times(1)).execute(httpRequestArgumentCaptor.capture(), isNull());

        assertThat(httpRequestArgumentCaptor.getValue().getURI().toASCIIString())
                .isEqualTo("http://httpbin.org/get?ticket=" + proxyTicket);
    }
}
