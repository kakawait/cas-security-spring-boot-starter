package com.kakawait.spring.boot.security.cas.autoconfigure;

import com.kakawait.spring.security.cas.userdetails.GrantedAuthoritiesFromAssertionAttributesWithDefaultRolesUserDetailsService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.cas.userdetails.AbstractCasAssertionUserDetailsService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Thibaud Leprêtre
 */
@ConditionalOnMissingBean(AbstractCasAssertionUserDetailsService.class)
class CasAssertionUserDetailsServiceConfiguration {

    @Bean
    AbstractCasAssertionUserDetailsService defaultRolesAuthenticationUserDetailsService(
            CasSecurityProperties casSecurityProperties) {
        Set<SimpleGrantedAuthority> authorities = Arrays.stream(casSecurityProperties.getUser().getDefaultRoles())
                .map(r -> r.replaceFirst("^ROLE_", ""))
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .collect(Collectors.toSet());
        String[] attributes = casSecurityProperties.getUser().getRolesAttributes();
        return new GrantedAuthoritiesFromAssertionAttributesWithDefaultRolesUserDetailsService(attributes, authorities);
    }
}
