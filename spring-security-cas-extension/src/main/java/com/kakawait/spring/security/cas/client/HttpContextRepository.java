package com.kakawait.spring.security.cas.client;

import java.net.URI;
import java.security.Principal;

/**
 * @author Jonathan Coueraud
 */
public interface HttpContextRepository {

    HttpContext findByPrincipalAndUri(Principal principal, URI uri);

    void save(HttpContext httpContext);

    void removeByPrincipal(Principal principal);

    void removeByPrincipalUri(Principal principal, URI uri);
}
