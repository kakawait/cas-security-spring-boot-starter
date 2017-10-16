import com.kakawait.spring.security.cas.client.ProxyTicket;
import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * @author Jonathan Coueraud
 */
public class ProxyTicketTest {

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTicketImplValue() throws Throwable {
        new ProxyTicket(null);
    }

    @Test
    public void testGetValue() {
        ProxyTicket ticket = new ProxyTicket("foo");
        assertThat(ticket.getValue()).isEqualTo("foo");
    }

    @Test
    public void testEqual() {
        ProxyTicket proxyTicket = new ProxyTicket("foo");
        ProxyTicket proxyTicket1 = new ProxyTicket("foo");
        assertThat(proxyTicket).isEqualTo(proxyTicket1);
    }

    @Test
    public void testHashCode() {
        ProxyTicket proxyTicket = new ProxyTicket("foo");
        ProxyTicket proxyTicket1 = new ProxyTicket("foo");
        assertThat(proxyTicket.hashCode()).isEqualTo(proxyTicket1.hashCode());
    }

}
