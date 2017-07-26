package com.kakawait.spring.security.cas.authentication;

import com.kakawait.spring.security.cas.web.authentication.ProxyCallbackAndServiceAuthenticationDetails;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * @author Thibaud Leprêtre
 */
public class DynamicProxyCallbackUrlCasAuthenticationProvider extends CasAuthenticationProvider {
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication.getDetails() instanceof ProxyCallbackAndServiceAuthenticationDetails &&
                getTicketValidator() instanceof Cas20ServiceTicketValidator) {
            String proxyCallbackUrl = ((ProxyCallbackAndServiceAuthenticationDetails) authentication.getDetails())
                    .getProxyCallbackUrl();
            ((Cas20ServiceTicketValidator) getTicketValidator()).setProxyCallbackUrl(proxyCallbackUrl);
        }
        return super.authenticate(authentication);
    }
}
