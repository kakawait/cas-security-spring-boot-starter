package com.kakawait.spring.boot.security.cas;

/**
 * @author Thibaud Leprêtre
 */
public abstract class CasSecurityConfigurerAdapter implements CasSecurityConfigurer {

    @Override
    public void configure(CasAuthenticationFilterConfigurer filter) {
    }

    @Override
    public void configure(CasAuthenticationProviderSecurityBuilder provider) {
    }

}
