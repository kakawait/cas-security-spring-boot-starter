package com.kakawait.spring.boot.security.cas;

import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;

/**
 * @author Thibaud Leprêtre
 */
public abstract class CasSecurityConfigurerAdapter
        extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> implements CasSecurityConfigurer {

    @Override
    public void configure(CasAuthenticationFilterConfigurer filter) {
    }

    @Override
    public void configure(CasSingleSignOutFilterConfigurer filter) {
    }

    @Override
    public void configure(CasAuthenticationProviderSecurityBuilder provider) {
    }

}
