package com.kakawait.spring.security.cas.client;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Jonathan Coueraud
 */
public class CasStatefulServiceImpl implements CasStatefulService {

    private final CasStatelessService casStatelessService;

    private final HttpContextRepository httpContextRepository;

    private final RequestWithCookieFactory requestWithCookieFactory;

    private final CookieFactory cookieFactory;

    public CasStatefulServiceImpl(CasStatelessService casStatelessService,
            HttpContextRepository httpContextRepository,
            RequestWithCookieFactory requestWithCookieFactory,
            CookieFactory cookieFactory) {
        this.casStatelessService = casStatelessService;
        this.httpContextRepository = httpContextRepository;
        this.requestWithCookieFactory = requestWithCookieFactory;
        this.cookieFactory = cookieFactory;
    }

    @Override
    public HttpRequest createRequest(Principal principal, HttpRequest request) throws IOException {
        List<CookieWrapper> cookies =
                Optional.ofNullable(httpContextRepository.findByPrincipalAndUri(principal, request.getURI()))
                        .map(HttpContext::getCookies).orElse(Collections.emptyList());

        return (cookies.isEmpty() ?
                casStatelessService.createRequest(principal, request) :
                requestWithCookieFactory.createRequest(request, cookies));
    }

    @Override
    public void saveCookie(Principal principal, HttpRequest request, ClientHttpResponse httpResponse) {
        Set<CookieWrapper> cookieWrappers = cookieFactory.parseCookie(httpResponse);
        URI uri = request.getURI();
        HttpContext httpContext = Optional.ofNullable(httpContextRepository.findByPrincipalAndUri(principal, uri))
                .orElse(new HttpContext(principal, uri));
        cookieWrappers.forEach(httpContext::addCookie);
        httpContextRepository.save(httpContext);
    }
}
