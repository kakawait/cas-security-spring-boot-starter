package com.kakawait.spring.boot.security.cas.autoconfigure;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * @author Thibaud Leprêtre
 */
public abstract class CasSecurityConfigurerAdapter implements CasSecurityConfigurer {

    @Override
    public void configure(CasAuthenticationFilterConfigurer filter) {
    }

    @Override
    public void configure(CasSingleSignOutFilterConfigurer filter) {
    }

    @Override
    public void configure(CasAuthenticationProviderSecurityBuilder provider) {
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
    }

    @Override
    @Deprecated
    public void init(HttpSecurity http) throws Exception {
    }

    @Override
    public void configure(CasTicketValidatorBuilder ticketValidator) {
    }
}
