import com.kakawait.spring.security.cas.client.CookieWrapper;
import com.kakawait.spring.security.cas.client.RequestWithCookieFactoryImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Jonathan Coueraud
 */
public class RequestWithCookieFactoryImplTest {

    private CookieWrapper cookie;
    private Set<CookieWrapper> cookies;
    private HttpRequest request;
    private HttpHeaders headers;
    private RequestWithCookieFactoryImpl factory;

    @Before

    public void setUp() {
        cookie = mock(CookieWrapper.class);
        when(cookie.getValue()).thenReturn("foo");
        when(cookie.getName()).thenReturn("bar");

        cookies = Collections.singleton(cookie);

        headers = new HttpHeaders();

        request = new HttpRequest() {
            @Override
            public HttpMethod getMethod() {
                return HttpMethod.GET;
            }

            @Override
            public URI getURI() {
                try {
                    return new URI("");
                } catch (URISyntaxException e) {
                    // ok for test
                }
                return null;
            }

            @Override
            public HttpHeaders getHeaders() {
                return headers;
            }
        };

        factory = new RequestWithCookieFactoryImpl();
    }

    @After
    public void tearDown() {
        cookie = null;
        cookies = null;
        request = null;
        headers = null;
        factory = null;
    }

    @Test
    public void testCreateRequest() {
        HttpRequest request = factory.createRequest(this.request, cookies);

        assertThat(request.getURI()).isEqualTo(this.request.getURI());
        assertThat(request.getMethod()).isEqualTo(this.request.getMethod());
        assertThat(request.getHeaders().get(HttpHeaders.COOKIE)).contains("bar=foo");
    }
}
