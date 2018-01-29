import com.kakawait.spring.security.cas.client.CookieWrapper;
import com.kakawait.spring.security.cas.client.HttpCookieFactory;
import com.kakawait.spring.security.cas.client.HttpCookieWrapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpResponse;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Jonathan Coueraud
 */
public class HttpCookieFactoryTest {

    private HttpCookieFactory httpCookieFactory;

    @Before
    public void setUp() {
        httpCookieFactory = new HttpCookieFactory();
    }

    @After
    public void tearDown() {
        httpCookieFactory = null;
    }

    @Test
    public void testCreateCookie() {
        CookieWrapper cookie = httpCookieFactory.createCookie("foo", "bar");
        assertThat(cookie.getName()).isEqualTo("foo");
        assertThat(cookie.getValue()).isEqualTo("bar");
        assertThat(cookie.getExpiration()).isEqualTo(-1);
    }

    @Test
    public void testParseCookie() {
        // ClientHttpResponse clientHttpResponse
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, "foo=bar");
        when(response.getHeaders()).thenReturn(headers);
        Set<CookieWrapper> cookieWrappers = httpCookieFactory.parseCookie(response);

        assertThat(cookieWrappers).hasSize(1);

        cookieWrappers.stream().findFirst().ifPresent(c -> {
            assertThat(c.getName()).isEqualTo("foo");
            assertThat(c.getValue()).isEqualTo("bar");
        });
    }
}
