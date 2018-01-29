import com.kakawait.spring.security.cas.client.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.net.URI;
import java.security.Principal;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Jonathan Coueraud
 */
public class CasStatefulServiceImplTest {

    private Principal principal;
    private HttpRequest request;
    private HttpContextRepository contextRepository;
    private CasStatelessService casStatelessService;
    private Set<CookieWrapper> cookies;
    private HttpContext contextWithCookie;
    private HttpContext context;
    private RequestWithCookieFactory requestWithCookieFactory;
    private CookieFactory cookieFactory;
    private CasStatefulService casStatefulService;
    private ClientHttpResponse response;

    @Before
    public void setUp() {

        principal = mock(Principal.class);

        request = mock(HttpRequest.class);

        response = mock(ClientHttpResponse.class);

        contextRepository = mock(HttpContextRepository.class);

        casStatelessService = mock(CasStatelessService.class);

        cookies = Collections.singleton(mock(CookieWrapper.class));

        cookieFactory = mock(CookieFactory.class);
        when(cookieFactory.parseCookie(response)).thenReturn(cookies);

        contextWithCookie = mock(HttpContext.class);
        when(contextWithCookie.getCookies()).thenReturn(cookies);

        context = mock(HttpContext.class);
        when(context.getCookies()).thenReturn(Collections.emptySet());

        requestWithCookieFactory = mock(RequestWithCookieFactory.class);

        casStatefulService =
                new CasStatefulServiceImpl(casStatelessService, contextRepository, requestWithCookieFactory,
                        cookieFactory);
    }

    @After
    public void tearDown() {
        contextRepository = null;
        casStatefulService = null;
        contextWithCookie = null;
        context = null;
        requestWithCookieFactory = null;
        casStatefulService = null;
        cookieFactory = null;
    }

    @Test
    public void testCreateRequestWithCookieContext() throws Throwable {
        when(contextRepository.findByPrincipalAndUri(any(Principal.class), any(URI.class))).thenReturn(
                contextWithCookie);

        HttpRequest newRequest = mock(HttpRequest.class);
        when(requestWithCookieFactory.createRequest(request, cookies)).thenReturn(newRequest);

        assertThat(casStatefulService.createRequest(principal, request)).isEqualTo(newRequest);
    }

    @Test
    public void testCreateRequestWithNoCookieContext() throws Throwable {
        when(contextRepository.findByPrincipalAndUri(any(Principal.class), any(URI.class))).thenReturn(context);

        HttpRequest newRequest = mock(HttpRequest.class);
        when(casStatelessService.createRequest(principal, request)).thenReturn(newRequest);

        assertThat(casStatefulService.createRequest(principal, request)).isEqualTo(newRequest);
    }

    @Test
    public void testSaveCookie() {
        when(contextRepository.findByPrincipalAndUri(eq(principal), any(URI.class))).thenReturn(context);

        casStatefulService.saveCookie(principal, request, response);

        verify(context).addCookies(cookies);
        verify(contextRepository).save(context);
    }
}
