import com.kakawait.spring.security.cas.client.JasigProxyTicketService;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Jonathan Coueraud
 */
public class JasigProxyTicketServiceTest {

    AttributePrincipal principal;
    URI uri;
    JasigProxyTicketService jasigProxyTicketService;

    @Before
    public void setUp() throws Throwable {
        principal = mock(AttributePrincipal.class);
        uri = new URI("http://foo");
        jasigProxyTicketService = new JasigProxyTicketService();
    }

    @After
    public void tearDown() {
        principal = null;
        uri = null;
        jasigProxyTicketService = null;
    }

    @Test
    public void testGetProxyTicket() throws Throwable {
        when(principal.getProxyTicketFor(uri.toString())).thenReturn("foo");
        assertThat(jasigProxyTicketService.getProxyTicket(principal, uri).getValue()).isEqualTo("foo");
    }

    @Test(expected = IOException.class)
    public void testGetProxyTicketNull() throws Throwable {
        when(principal.getProxyTicketFor(uri.toString())).thenReturn(null);
        jasigProxyTicketService.getProxyTicket(principal, uri);
    }
}
