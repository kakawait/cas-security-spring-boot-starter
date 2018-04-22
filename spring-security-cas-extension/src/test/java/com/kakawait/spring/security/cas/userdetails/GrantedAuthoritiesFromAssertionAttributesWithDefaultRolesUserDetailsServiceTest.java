package com.kakawait.spring.security.cas.userdetails;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Thibaud Leprêtre
 */
@RunWith(MockitoJUnitRunner.class)
public class GrantedAuthoritiesFromAssertionAttributesWithDefaultRolesUserDetailsServiceTest {

    private static final Collection<? extends GrantedAuthority> DEFAULT_ROLES = Collections.unmodifiableCollection(
            new ArrayList<SimpleGrantedAuthority>() {{
                add(new SimpleGrantedAuthority("ROLE_USER"));
            }});

    private static final Map<String, Object> ATTRIBUTES = Collections.unmodifiableMap(new HashMap<String, Object>() {{
        put("foo", "bar");
        put("role", "role_member");
        put("group", "admin");
    }});

    @Mock
    private Assertion assertion;

    @Mock
    private AttributePrincipal principal;

    @Test
    public void loadUserDetails_NullOrBlankPrincipalName_UsernameNotFoundException() {
        // JDK10 var needed :)
        GrantedAuthoritiesFromAssertionAttributesWithDefaultRolesUserDetailsService userDetailsService =
                new GrantedAuthoritiesFromAssertionAttributesWithDefaultRolesUserDetailsService(new String[] {"role"},
                        DEFAULT_ROLES);

        when(assertion.getPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn(null);
        assertThatThrownBy(() -> userDetailsService.loadUserDetails(assertion))
                .isInstanceOf(UsernameNotFoundException.class);
        verify(assertion, times(1)).getPrincipal();
        verify(principal, times(1)).getName();

        reset(assertion, principal);
        when(assertion.getPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("");
        assertThatThrownBy(() -> userDetailsService.loadUserDetails(assertion))
                .isInstanceOf(UsernameNotFoundException.class);
        verify(assertion, times(1)).getPrincipal();
        verify(principal, times(1)).getName();

        reset(assertion, principal);
        when(assertion.getPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("    ");
        assertThatThrownBy(() -> userDetailsService.loadUserDetails(assertion))
                .isInstanceOf(UsernameNotFoundException.class);
        verify(assertion, times(1)).getPrincipal();
        verify(principal, times(1)).getName();
    }

    @Test
    public void loadUserDetails_WithoutAttributes_OnlyDefaultRoles() {
        // JDK10 var needed :)
        GrantedAuthoritiesFromAssertionAttributesWithDefaultRolesUserDetailsService userDetailsService =
                new GrantedAuthoritiesFromAssertionAttributesWithDefaultRolesUserDetailsService(null, DEFAULT_ROLES);

        when(assertion.getPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("JohnWick");

        assertThat(userDetailsService.loadUserDetails(assertion).getAuthorities())
                .extracting("authority", String.class)
                .containsExactly("ROLE_USER");

        verify(assertion, times(2)).getPrincipal();
        verify(principal, times(1)).getName();
        verify(principal, times(1)).getAttributes();
    }

    @Test
    public void loadUserDetails_WithNonMatchingAttributes_OnlyDefaultRoles() {
        // JDK10 var needed :)
        GrantedAuthoritiesFromAssertionAttributesWithDefaultRolesUserDetailsService userDetailsService =
                new GrantedAuthoritiesFromAssertionAttributesWithDefaultRolesUserDetailsService(
                        new String[] {"doesNotExists"}, DEFAULT_ROLES);

        when(assertion.getPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("JohnWick");
        when(principal.getAttributes()).thenReturn(ATTRIBUTES);

        assertThat(userDetailsService.loadUserDetails(assertion).getAuthorities())
                .extracting("authority", String.class)
                .containsExactly("ROLE_USER");

        verify(assertion, times(2)).getPrincipal();
        verify(principal, times(1)).getName();
        verify(principal, times(1)).getAttributes();
    }

    @Test
    public void loadUserDetails_WithMatchingAttributes_MergedRoles() {
        // JDK10 var needed :)
        GrantedAuthoritiesFromAssertionAttributesWithDefaultRolesUserDetailsService userDetailsService =
                new GrantedAuthoritiesFromAssertionAttributesWithDefaultRolesUserDetailsService(
                        new String[] {"role", "group"}, DEFAULT_ROLES);

        when(assertion.getPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("JohnWick");
        when(principal.getAttributes()).thenReturn(ATTRIBUTES);

        assertThat(userDetailsService.loadUserDetails(assertion).getAuthorities())
                .extracting("authority", String.class)
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_MEMBER", "ROLE_ADMIN");

        verify(assertion, times(2)).getPrincipal();
        verify(principal, times(1)).getName();
        verify(principal, times(1)).getAttributes();
    }
}
