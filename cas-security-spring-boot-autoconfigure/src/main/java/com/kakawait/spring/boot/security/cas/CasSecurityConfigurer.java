package com.kakawait.spring.boot.security.cas;

import org.springframework.security.config.annotation.SecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;

/**
 * @author Thibaud Leprêtre
 */
public interface CasSecurityConfigurer extends SecurityConfigurer<DefaultSecurityFilterChain, HttpSecurity> {

    void configure(CasAuthenticationFilterConfigurer filter);

    void configure(CasAuthenticationProviderSecurityBuilder provider);

    void configure(CasSingleSignOutFilterConfigurer filter);
}
