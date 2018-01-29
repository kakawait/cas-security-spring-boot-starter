import com.kakawait.spring.security.cas.client.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.security.Principal;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Jonathan Coueraud
 */
public class InMemoryHttpContextRepositoryTest {

    private Principal principal;
    private URI uri;
    private HttpContext context;

    @Before
    public void setUp() throws Throwable {
        principal = mock(Principal.class);
        uri = new URI("htp://test");
        context = mock(HttpContext.class);
    }

    @After
    public void tearDown() {
        principal = null;
        uri = null;
    }

    @Test
    public void testSaveWithoutFlush() throws Throwable {
        InMemoryHttpContextRepository repository = new InMemoryHttpContextRepository();

        Principal principal = mock(Principal.class);
        URI uri = new URI("http://test");
        HttpContext context = mock(HttpContext.class);
        when(context.getUri()).thenReturn(uri);
        when(context.getPrincipal()).thenReturn(principal);

        CookieWrapper cookie = createMockedCookie("foo", "bar", -1L);
        CookieWrapper cookieDiffVal = createMockedCookie("foo", "barbar", -1L);
        CookieWrapper cookieDiffExp = createMockedCookie("foo", "bar", 10);
        CookieWrapper cookieDiffName = createMockedCookie("foofoo", "bar", -1L);
        CookieWrapper cookieExpired = createMockedCookie("foofoo", "bar", 0);

        when(context.getCookies()).thenReturn(Collections.singleton(cookieExpired));
        repository.save(context);
        assertThat(repository.findByPrincipalAndUri(principal, uri).getCookies()).hasSize(0);

        when(context.getCookies()).thenReturn(Collections.singleton(cookie));
        repository.save(context);
        HttpContext byPrincipalAndUri = repository.findByPrincipalAndUri(principal, uri);
        assertThat(byPrincipalAndUri.getUri()).isEqualTo(uri);
        assertThat(byPrincipalAndUri.getPrincipal()).isEqualTo(principal);
        checkCookieIsContained(repository, principal, uri, cookie);

        when(context.getCookies()).thenReturn(Collections.singleton(cookieDiffExp));
        repository.save(context);
        Set<CookieWrapper> cookies = repository.findByPrincipalAndUri(principal, uri).getCookies();
        assertThat(cookies).hasSize(1);
        checkCookieIsContained(repository, principal, uri, cookieDiffExp);

        when(context.getCookies()).thenReturn(Collections.singleton(cookieDiffVal));
        repository.save(context);
        cookies = repository.findByPrincipalAndUri(principal, uri).getCookies();
        assertThat(cookies).hasSize(1);
        checkCookieIsContained(repository, principal, uri, cookieDiffVal);

        when(context.getCookies()).thenReturn(Collections.singleton(cookieDiffName));
        repository.save(context);
        assertThat(repository.findByPrincipalAndUri(principal, uri).getCookies()).hasSize(2);
        checkCookieIsContained(repository, principal, uri, cookieDiffVal);
        checkCookieIsContained(repository, principal, uri, cookieDiffName);
    }

    @Test
    public void testSaveWithFlush() throws Throwable {
        CurrentTimeMillisAdapter currentTimeMillisAdapter = mock(CurrentTimeMillisAdapter.class);
        InMemoryHttpContextRepository repository = new InMemoryHttpContextRepository(2, currentTimeMillisAdapter);

        when(currentTimeMillisAdapter.currentTimeMillis())
                .thenReturn(0L)
                .thenReturn(1L);

        Principal principal = mock(Principal.class);
        URI uri = new URI("http://test");
        HttpContext context = mock(HttpContext.class);
        when(context.getUri()).thenReturn(uri);
        when(context.getPrincipal()).thenReturn(principal);

        CookieWrapper cookie = createMockedCookie("foo", "bar", 1);
        CookieWrapper cookie2 = createMockedCookie("foo", "bar", 2);
        CookieWrapper cookie3 = createMockedCookie("foo", "bar", -1);

        when(context.getCookies()).thenReturn(Collections.singleton(cookie));
        repository.save(context);
        checkCookieIsContained(repository, principal, uri, cookie);

        when(context.getCookies()).thenReturn(Collections.singleton(cookie2));

        // first flush but not expired
        repository.save(context);
        checkCookieIsContained(repository, principal, uri, cookie);
        checkCookieIsContained(repository, principal, uri, cookie2);

        when(context.getCookies()).thenReturn(Collections.singleton(cookie3));

        // no flush
        repository.save(context);
        checkCookieIsContained(repository, principal, uri, cookie);
        checkCookieIsContained(repository, principal, uri, cookie2);
        checkCookieIsContained(repository, principal, uri, cookie3);

        // second flush & expired
        repository.save(context);
        assertThat(repository.findByPrincipalAndUri(principal, uri).getCookies()).hasSize(1);
        checkCookieIsContained(repository, principal, uri, cookie2);
        checkCookieIsContained(repository, principal, uri, cookie3);
    }

    @Test
    public void testRemoveByPrincipal() throws Throwable {
        InMemoryHttpContextRepository repository = new InMemoryHttpContextRepository();

        Principal principal = mock(Principal.class);
        URI uri = new URI("http://test");
        HttpContext context = mock(HttpContext.class);
        when(context.getUri()).thenReturn(uri);
        when(context.getPrincipal()).thenReturn(principal);

        URI uri1 = new URI("http://test1");
        HttpContext contextSamePrincipal = mock(HttpContext.class);
        when(contextSamePrincipal.getUri()).thenReturn(uri1);
        when(contextSamePrincipal.getPrincipal()).thenReturn(principal);

        Principal principal1 = mock(Principal.class);
        HttpContext contextSameUri = mock(HttpContext.class);
        when(contextSameUri.getUri()).thenReturn(uri);
        when(contextSameUri.getPrincipal()).thenReturn(principal1);
        when(contextSameUri.getCookies()).thenReturn(Collections.singleton(mock(CookieWrapper.class)));

        repository.save(context);
        repository.save(contextSamePrincipal);
        repository.save(contextSameUri);

        repository.removeByPrincipal(principal);

        assertThat(repository.findByPrincipalAndUri(principal, uri)).isNull();
        assertThat(repository.findByPrincipalAndUri(principal, uri1)).isNull();
        assertThat(repository.findByPrincipalAndUri(principal1, uri)).isNotNull();
    }

    @Test
    public void testRemoveByPrincipalUri() throws Throwable {
        InMemoryHttpContextRepository repository = new InMemoryHttpContextRepository();

        Principal principal = mock(Principal.class);
        URI uri = new URI("http://test");
        HttpContext context = mock(HttpContext.class);
        when(context.getUri()).thenReturn(uri);
        when(context.getPrincipal()).thenReturn(principal);

        URI uri1 = new URI("http://test1");
        HttpContext contextSamePrincipal = mock(HttpContext.class);
        when(contextSamePrincipal.getUri()).thenReturn(uri1);
        when(contextSamePrincipal.getPrincipal()).thenReturn(principal);

        Principal principal1 = mock(Principal.class);
        HttpContext contextSameUri = mock(HttpContext.class);
        when(contextSameUri.getUri()).thenReturn(uri);
        when(contextSameUri.getPrincipal()).thenReturn(principal1);
        when(contextSameUri.getCookies()).thenReturn(Collections.singleton(mock(CookieWrapper.class)));

        repository.save(context);
        repository.save(contextSamePrincipal);
        repository.save(contextSameUri);

        repository.removeByPrincipalUri(principal, uri);

        assertThat(repository.findByPrincipalAndUri(principal, uri)).isNull();
        assertThat(repository.findByPrincipalAndUri(principal, uri1)).isNotNull();
        assertThat(repository.findByPrincipalAndUri(principal1, uri)).isNotNull();
    }

    private CookieWrapper createMockedCookie(String name, String value, long expiration) {
        CookieWrapper cookie = mock(CookieWrapper.class);
        when(cookie.getName()).thenReturn(name);
        when(cookie.getValue()).thenReturn(value);
        when(cookie.getExpiration()).thenReturn(expiration);

        return cookie;
    }

    private void checkCookieIsContained(HttpContextRepository repository,
            Principal principal,
            URI uri,
            CookieWrapper cookie) {
        assertThat(repository.findByPrincipalAndUri(principal, uri)
                .getCookies()
                .stream()
                .filter(c -> (Objects.equals(c.getName(), cookie.getName()) && Objects.equals(c.getValue(),
                        cookie.getValue())))
                .count()).isEqualTo(1);
    }

}
