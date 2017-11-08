import com.kakawait.spring.security.cas.client.AuthenticatedPrincipal;
import com.kakawait.spring.security.cas.client.CasStatefulInterceptor;
import com.kakawait.spring.security.cas.client.CasStatefulService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Jonathan Coueraud
 */
public class CasStatefulInterceptorTest {

    private Principal principal;
    private HttpRequest request;
    private byte[] body;
    private ClientHttpRequestExecution execution;
    private CasStatefulService casStatefulService;
    private AuthenticatedPrincipal authenticatedPrincipal;
    private CasStatefulInterceptor interceptor;
    private HttpRequest newRequest;
    private ClientHttpResponse response;

    @Before
    public void setUp() throws Throwable {
        principal = mock(Principal.class);

        request = mock(HttpRequest.class);

        newRequest = mock(HttpRequest.class);

        response = mock(ClientHttpResponse.class);

        body = "foo".getBytes();

        authenticatedPrincipal = mock(AuthenticatedPrincipal.class);
        when(authenticatedPrincipal.getAuthenticatedPrincipal()).thenReturn(principal);

        casStatefulService = mock(CasStatefulService.class);
        when(casStatefulService.createRequest(principal, request)).thenReturn(newRequest);

        execution = mock(ClientHttpRequestExecution.class);
        when(execution.execute(newRequest, body)).thenReturn(response);

        interceptor = new CasStatefulInterceptor(casStatefulService, authenticatedPrincipal);
    }

    @After
    public void tearDown() {
        principal = null;
        request = null;
        newRequest = null;
        response = null;
        body = null;
        execution = null;
        casStatefulService = null;
        authenticatedPrincipal = null;
        interceptor = null;
    }

    @Test
    public void testIntercept() throws Throwable {
        ClientHttpResponse intercept = interceptor.intercept(request, body, execution);

        assertThat(intercept).isEqualTo(response);
        verify(casStatefulService).saveCookie(principal, newRequest, response);
    }
}
