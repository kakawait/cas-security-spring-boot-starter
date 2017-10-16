package com.kakawait.spring.security.cas.client;

import java.security.Principal;

/**
 * @author Jonathan Coueraud
 */
public interface AuthenticatedPrincipal {

    Principal getAuthenticatedPrincipal();
}
