package com.kakawait.spring.security.cas.userdetails;

import org.jasig.cas.client.validation.Assertion;
import org.springframework.security.cas.userdetails.AbstractCasAssertionUserDetailsService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Thibaud Leprêtre
 */
public class GrantedAuthoritiesFromAssertionAttributesWithDefaultRolesUserDetailsService
        extends AbstractCasAssertionUserDetailsService {

    private static final String NON_EXISTENT_PASSWORD_VALUE = "NO_PASSWORD";

    private final String[] attributes;

    private final Collection<? extends GrantedAuthority> defaultGrantedAuthorities;

    private boolean toUppercase = true;

    public GrantedAuthoritiesFromAssertionAttributesWithDefaultRolesUserDetailsService(String[] attributes,
            Collection<? extends GrantedAuthority> defaultGrantedAuthorities) {
        this.attributes = attributes;
        this.defaultGrantedAuthorities = defaultGrantedAuthorities;
    }

    protected UserDetails loadUserDetails(Assertion assertion) {
        String username = assertion.getPrincipal().getName();
        if (!StringUtils.hasText(username)) {
            throw new UsernameNotFoundException("Unable to retrieve username from CAS assertion");
        }

        List<GrantedAuthority> authorities = Arrays
                .stream(attributes)
                .map(a -> assertion.getPrincipal().getAttributes().get(a))
                .filter(Objects::nonNull)
                .flatMap(v -> (v instanceof Collection) ? ((Collection<?>) v).stream() : Stream.of(v))
                .map(v -> toUppercase ? v.toString().toUpperCase() : v.toString())
                .map(r -> r.replaceFirst("^ROLE_", ""))
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .collect(Collectors.toList());

        authorities.addAll(defaultGrantedAuthorities);

        return new User(username, NON_EXISTENT_PASSWORD_VALUE, authorities);
    }
}
