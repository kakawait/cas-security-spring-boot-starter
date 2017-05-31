package com.kakawait.spring.boot.security.cas;

/**
 * @author Thibaud Leprêtre
 */
public interface CasSecurityConfigurer {

    void configure(CasAuthenticationFilterConfigurer filter);

    void configure(CasAuthenticationProviderSecurityBuilder provider);
}
