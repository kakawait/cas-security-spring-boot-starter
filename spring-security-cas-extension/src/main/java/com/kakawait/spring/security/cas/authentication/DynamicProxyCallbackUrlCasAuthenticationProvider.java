package com.kakawait.spring.security.cas.authentication;

import com.kakawait.spring.security.cas.client.validation.ProxyCallbackUrlAwareTicketValidator;
import com.kakawait.spring.security.cas.web.authentication.ProxyCallbackAndServiceAuthenticationDetails;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.core.Authentication;

/**
 * @author Thibaud Lepretre
 */
public class DynamicProxyCallbackUrlCasAuthenticationProvider extends CasAuthenticationProvider {
    @Override
    public Authentication authenticate(Authentication authentication) {
        if (authentication.getDetails() instanceof ProxyCallbackAndServiceAuthenticationDetails) {
            if (getTicketValidator() instanceof Cas20ServiceTicketValidator) {
                String proxyCallbackUrl =
                        ((ProxyCallbackAndServiceAuthenticationDetails) authentication.getDetails()).getProxyCallbackUrl();
                ((Cas20ServiceTicketValidator) getTicketValidator()).setProxyCallbackUrl(proxyCallbackUrl);
            } else if (getTicketValidator() instanceof ProxyCallbackUrlAwareTicketValidator) {
                String proxyCallbackUrl =
                        ((ProxyCallbackAndServiceAuthenticationDetails) authentication.getDetails()).getProxyCallbackUrl();
                ((ProxyCallbackUrlAwareTicketValidator) getTicketValidator()).setProxyCallbackUrl(proxyCallbackUrl);
            }
        }
        return super.authenticate(authentication);
    }
}
