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
public class CasStatelessServiceImplTest {

    private Principal principal;
    private HttpRequest httpRequest;
    private ProxyTicketRepository proxyTicketRepository;
    private CasClientProperties casClientProperties;
    private CasStatelessService casStatelessService;
    private CasRequestFactory casRequestFactory;

    @Before
    public void setUp() throws Throwable {
        principal = mock(Principal.class);

        Ticket proxyTicket = mock(Ticket.class);
        when(proxyTicket.getValue()).thenReturn("bar");

        URI uri = new URI("http://foo");

        httpRequest = mock(HttpRequest.class);
        when(httpRequest.getURI()).thenReturn(uri);

        proxyTicketRepository = mock(ProxyTicketRepository.class);
        when(proxyTicketRepository.getProxyTicket(principal, uri)).thenReturn(proxyTicket);

        casClientProperties = mock(CasClientProperties.class);
        when(casClientProperties.getProxyTicketQueryKey()).thenReturn("ticket");

        casRequestFactory = new CasRequestFactory(casClientProperties);

        casStatelessService = new CasStatelessServiceImpl(proxyTicketRepository, casRequestFactory);
    }

    @After
    public void tearDown() {
        principal = null;
        httpRequest = null;
        proxyTicketRepository = null;
        casClientProperties = null;
        casStatelessService = null;
    }

    @Test
    public void testCreateRequestWithPrincipal() throws Throwable {
        HttpRequest newHttpRequest = casStatelessService.createRequest(principal, httpRequest);
        String proxyTicketParamKey = casClientProperties.getProxyTicketQueryKey();
        URI newUri = newHttpRequest.getURI();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUri(newUri);

        String proxyTicketValue = uriComponentsBuilder.build().getQueryParams().getFirst(proxyTicketParamKey);
        assertThat(proxyTicketValue).isEqualTo("bar");

        URI shouldMatchOriginalUri = uriComponentsBuilder.replaceQueryParam(proxyTicketParamKey).build().toUri();
        assertThat(shouldMatchOriginalUri).isEqualTo(httpRequest.getURI());
    }
}
