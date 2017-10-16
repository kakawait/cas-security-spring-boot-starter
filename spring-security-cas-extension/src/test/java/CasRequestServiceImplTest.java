import com.kakawait.spring.security.cas.client.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpRequest;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Jonathan Coueraud
 */
public class CasRequestServiceImplTest {

    private Principal principal;
    private HttpRequest httpRequest;
    private ProxyTicketService proxyTicketService;
    private CasClientProperties casClientProperties;
    private CasRequestService casRequestService;

    @Before
    public void setUp() throws Throwable {
        principal = mock(Principal.class);

        Ticket proxyTicket = mock(Ticket.class);
        when(proxyTicket.getValue()).thenReturn("bar");

        URI uri = new URI("http://foo");

        httpRequest = mock(HttpRequest.class);
        when(httpRequest.getURI()).thenReturn(uri);

        proxyTicketService = mock(ProxyTicketService.class);
        when(proxyTicketService.getProxyTicket(principal, uri)).thenReturn(proxyTicket);

        casClientProperties = mock(CasClientProperties.class);
        when(casClientProperties.getProxyTicketQueryKey()).thenReturn("ticket");

        casRequestService =
                new CasRequestServiceImpl(proxyTicketService, casClientProperties);
    }

    @After
    public void tearDown() {
        principal = null;
        httpRequest = null;
        proxyTicketService = null;
        casClientProperties = null;
        casRequestService = null;
    }

    @Test
    public void testCreateRequestWithPrincipal() throws Throwable {
        HttpRequest newHttpRequest = casRequestService.createRequest(principal, httpRequest);
        String proxyTicketParamKey = casClientProperties.getProxyTicketQueryKey();
        URI newUri = newHttpRequest.getURI();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUri(newUri);

        String proxyTicketValue = uriComponentsBuilder.build().getQueryParams().getFirst(proxyTicketParamKey);
        assertThat(proxyTicketValue).isEqualTo("bar");

        URI shouldMatchOriginalUri = uriComponentsBuilder.replaceQueryParam(proxyTicketParamKey).build().toUri();
        assertThat(shouldMatchOriginalUri).isEqualTo(httpRequest.getURI());
    }
}
