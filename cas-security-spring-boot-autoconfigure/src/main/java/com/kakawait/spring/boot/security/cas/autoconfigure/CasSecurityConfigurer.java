package com.kakawait.spring.boot.security.cas.autoconfigure;

import org.springframework.security.config.annotation.SecurityBuilder;
import org.springframework.security.config.annotation.SecurityConfigurer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * @author Thibaud Leprêtre
 */
public interface CasSecurityConfigurer {

    void configure(CasAuthenticationFilterConfigurer filter);

    void configure(CasAuthenticationProviderSecurityBuilder provider);

    void configure(CasSingleSignOutFilterConfigurer filter);

    void configure(HttpSecurity http) throws Exception;

    /**
     * @deprecated Use {@link SecurityConfigurer#configure(SecurityBuilder)} instead.
     * Will be removed on release 1.0.0!
     */
    @Deprecated
    void init(HttpSecurity http) throws Exception;

    void configure(CasTicketValidatorBuilder ticketValidator);

    void configure(AuthenticationManagerBuilder auth) throws Exception;
}
